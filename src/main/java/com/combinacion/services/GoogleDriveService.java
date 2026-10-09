package com.combinacion.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleDriveService {
    private static final String APPLICATION_NAME = "Gestor Contratacion Drive";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/credencialescontratacion.json";
    
    // Necesitamos los mismos scopes que EmailService para reutilizar el token
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/gmail.send",
            DriveScopes.DRIVE
    );

    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        InputStream in = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new java.io.FileNotFoundException("ERROR CRÍTICO: Falta el archivo de credenciales de Google Drive (" + CREDENTIALS_FILE_PATH + "). Por favor ubíquelo en src/main/resources.");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new JDBCDataStoreFactory())
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8889).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user_v2");
    }

    private static Drive driveServiceInstance = null;

    public static Drive getDriveService() throws Exception {
        if (driveServiceInstance == null) {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            driveServiceInstance = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        return driveServiceInstance;
    }

    private static final java.util.Map<String, String> FOLDER_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public interface DriveCallable<T> {
        T call() throws Exception;
    }

    public static <T> T executeWithRetry(DriveCallable<T> callable) throws Exception {
        int maxRetries = 3;
        long waitTime = 1000;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return callable.call();
            } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException ge) {
                int code = ge.getStatusCode();
                boolean isRateLimit = (code == 429 || code == 503 || code == 500)
                        || (code == 403 && ge.getMessage() != null && (ge.getMessage().contains("rateLimit") || ge.getMessage().contains("userRateLimitExceeded") || ge.getMessage().contains("quotaExceeded")));
                if (isRateLimit && attempt < maxRetries) {
                    System.err.println("Google Drive API limit/error (" + code + "). Reintento " + attempt + "/" + maxRetries + " en " + waitTime + "ms...");
                    Thread.sleep(waitTime);
                    waitTime *= 2;
                } else {
                    throw ge;
                }
            } catch (java.io.IOException ioe) {
                if (attempt < maxRetries) {
                    System.err.println("Google Drive IO error: " + ioe.getMessage() + ". Reintento " + attempt + "/" + maxRetries + " en " + waitTime + "ms...");
                    Thread.sleep(waitTime);
                    waitTime *= 2;
                } else {
                    throw ioe;
                }
            }
        }
        return callable.call();
    }

    public static String getOrCreateFolder(String folderName, String parentId) throws Exception {
        String cacheKey = (parentId != null && !parentId.isEmpty() ? parentId : "root") + "::" + folderName;
        String cachedId = FOLDER_CACHE.get(cacheKey);
        if (cachedId != null && !cachedId.trim().isEmpty()) {
            return cachedId;
        }

        Drive driveService = getDriveService();
        String cleanFolderName = folderName.replace("\\", "\\\\").replace("'", "\\'");
        String query = "mimeType='application/vnd.google-apps.folder' and name='" + cleanFolderName + "' and trashed=false";
        if (parentId != null && !parentId.isEmpty()) {
            query += " and '" + parentId + "' in parents";
        }
        
        FileList result = null;
        try {
            final String finalQuery = query;
            result = executeWithRetry(() -> driveService.files().list()
                    .setQ(finalQuery)
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute());
        } catch (Exception qEx) {
            System.err.println("Aviso Drive: No se pudo buscar carpeta '" + folderName + "': " + qEx.getMessage());
        }

        if (result != null && result.getFiles() != null && !result.getFiles().isEmpty()) {
            String folderId = result.getFiles().get(0).getId();
            FOLDER_CACHE.put(cacheKey, folderId);
            return folderId;
        }

        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        if (parentId != null && !parentId.isEmpty()) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }

        File folder = executeWithRetry(() -> driveService.files().create(fileMetadata).setFields("id").execute());
        String folderId = folder.getId();
        FOLDER_CACHE.put(cacheKey, folderId);
        return folderId;
    }

    public static String uploadOrUpdateFile(java.io.File file, String fileName, String mimeType, String parentId) throws Exception {
        Drive driveService = getDriveService();
        String cleanQueryName = fileName.replace("\\", "\\\\").replace("'", "\\'");
        String query = "name='" + cleanQueryName + "' and trashed=false";
        if (parentId != null && !parentId.isEmpty()) {
            query += " and '" + parentId + "' in parents";
        }

        FileList result = null;
        try {
            final String finalQuery = query;
            result = executeWithRetry(() -> driveService.files().list()
                    .setQ(finalQuery)
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute());
        } catch (Exception qEx) {
            System.err.println("Aviso Drive: No se pudo consultar archivo previo '" + fileName + "': " + qEx.getMessage());
        }

        FileContent mediaContent = new FileContent(mimeType, file);
        if (result != null && result.getFiles() != null && !result.getFiles().isEmpty()) {
            try {
                // Existe con el mismo nombre, lo actualizamos
                String fileId = result.getFiles().get(0).getId();
                File updatedFile = new File();
                File newFile = executeWithRetry(() -> driveService.files().update(fileId, updatedFile, mediaContent).setFields("id").execute());
                return newFile.getId();
            } catch (Exception updEx) {
                System.err.println("Aviso Drive: No se pudo actualizar '" + fileName + "', creando archivo nuevo: " + updEx.getMessage());
            }
        }

        // No existe o falló la actualización, lo creamos
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        if (parentId != null && !parentId.isEmpty()) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }
        File newFile = executeWithRetry(() -> driveService.files().create(fileMetadata, mediaContent).setFields("id").execute());
        return newFile.getId();
    }

    /**
     * Busca en la carpeta Drive un archivo cuyo nombre coincida con el patrón (prefijo),
     * elimina el archivo anterior si existe con nombre diferente al nuevo, y sube el archivo
     * con el nombre correcto. Útil cuando el nombre del archivo puede cambiar (ej: consecutivo XXXX -> 0233).
     *
     * @param file      Archivo local a subir
     * @param fileName  Nombre definitivo del archivo en Drive
     * @param mimeType  Tipo MIME del archivo
     * @param parentId  ID de la carpeta destino en Drive
     * @param namePrefix Prefijo del nombre para buscar archivos anteriores (ej: "3. DS-4121-")
     */
    public static String uploadOrReplaceByPattern(java.io.File file, String fileName, String mimeType, String parentId, String namePrefix) throws Exception {
        Drive driveService = getDriveService();

        // 1. Buscar archivos en la carpeta que coincidan con el prefijo
        if (namePrefix != null && !namePrefix.isEmpty() && parentId != null && !parentId.isEmpty()) {
            String patternQuery = "'" + parentId + "' in parents and trashed=false and mimeType='" + mimeType + "'";
            FileList existing = driveService.files().list()
                    .setQ(patternQuery)
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute();

            if (existing.getFiles() != null) {
                for (File f : existing.getFiles()) {
                    // Si el nombre empieza con el prefijo pero NO es el nombre correcto, lo eliminamos
                    if (f.getName() != null && f.getName().startsWith(namePrefix) && !f.getName().equals(fileName)) {
                        try {
                            driveService.files().delete(f.getId()).execute();
                            System.out.println("Drive: Archivo antiguo eliminado: " + f.getName());
                        } catch (Exception ignore) {
                            System.err.println("Drive: No se pudo eliminar archivo antiguo: " + f.getName());
                        }
                    }
                }
            }
        }

        // 2. Subir/actualizar con el nombre correcto
        return uploadOrUpdateFile(file, fileName, mimeType, parentId);
    }

    public static String uploadStreamToDrive(java.io.InputStream in, long length, String fileName, String mimeType, String parentId) throws Exception {
        java.io.File tempFile = java.io.File.createTempFile("gdrive_up_", ".tmp");
        try {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                byte[] buf = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buf)) != -1) {
                    fos.write(buf, 0, bytesRead);
                }
            }
            return uploadOrUpdateFile(tempFile, fileName, mimeType, parentId);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public static InputStream downloadFile(String fileId) throws Exception {
        Drive driveService = getDriveService();
        return driveService.files().get(fileId).executeMediaAsInputStream();
    }

    public static void setPublicViewPermission(String fileId) throws Exception {
        Drive driveService = getDriveService();
        Permission permission = new Permission()
                .setType("anyone")
                .setRole("reader");
        driveService.permissions().create(fileId, permission).execute();
    }

    public static FileList getFilesInFolder(String folderId) throws Exception {
        Drive driveService = getDriveService();
        String query = "'" + folderId + "' in parents and trashed=false";
        return driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name, mimeType)")
                .execute();
    }

    public static String extractIdFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        if (url.contains("id=")) {
            return url.split("id=")[1].split("&")[0];
        } else if (url.contains("/folders/")) {
            return url.split("/folders/")[1].split("\\?")[0].split("/")[0];
        } else if (url.contains("/file/d/")) {
            return url.split("/file/d/")[1].split("/")[0];
        }
        return null;
    }

    public static String copyFile(String fileId, String newName, String parentId) throws Exception {
        Drive driveService = getDriveService();
        File copiedFile = new File();
        copiedFile.setName(newName);
        if (parentId != null && !parentId.isEmpty()) {
            copiedFile.setParents(Collections.singletonList(parentId));
        }
        File result = driveService.files().copy(fileId, copiedFile).execute();
        return result.getId();
    }
}
