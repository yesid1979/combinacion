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

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        InputStream in = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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

    public static String getOrCreateFolder(String folderName, String parentId) throws Exception {
        Drive driveService = getDriveService();
        String query = "mimeType='application/vnd.google-apps.folder' and name='" + folderName.replace("'", "\\'") + "' and trashed=false";
        if (parentId != null && !parentId.isEmpty()) {
            query += " and '" + parentId + "' in parents";
        } else {
            query += " and 'root' in parents";
        }
        
        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }

        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        if (parentId != null && !parentId.isEmpty()) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }

        File folder = driveService.files().create(fileMetadata).setFields("id").execute();
        return folder.getId();
    }

    public static String uploadOrUpdateFile(java.io.File file, String fileName, String mimeType, String parentId) throws Exception {
        Drive driveService = getDriveService();
        String query = "name='" + fileName.replace("'", "\\'") + "' and trashed=false";
        if (parentId != null && !parentId.isEmpty()) {
            query += " and '" + parentId + "' in parents";
        }

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        FileContent mediaContent = new FileContent(mimeType, file);
        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            // Existe, lo actualizamos
            String fileId = result.getFiles().get(0).getId();
            File updatedFile = new File();
            // Para update, usamos el contenido pero no le pasamos metadatos que cambien de padre
            File newFile = driveService.files().update(fileId, updatedFile, mediaContent).setFields("id").execute();
            return newFile.getId();
        } else {
            // No existe, lo creamos
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            if (parentId != null && !parentId.isEmpty()) {
                fileMetadata.setParents(Collections.singletonList(parentId));
            }
            File newFile = driveService.files().create(fileMetadata, mediaContent).setFields("id").execute();
            return newFile.getId();
        }
    }

    public static String uploadStreamToDrive(java.io.InputStream in, long length, String fileName, String mimeType, String parentId) throws Exception {
        Drive driveService = getDriveService();
        String query = "name='" + fileName.replace("'", "\\'") + "' and trashed=false";
        if (parentId != null && !parentId.isEmpty()) {
            query += " and '" + parentId + "' in parents";
        }

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        InputStreamContent mediaContent = new InputStreamContent(mimeType, in);
        mediaContent.setLength(length);
        if (result.getFiles() != null && !result.getFiles().isEmpty()) {
            // Existe, lo actualizamos
            String fileId = result.getFiles().get(0).getId();
            File updatedFile = new File();
            File newFile = driveService.files().update(fileId, updatedFile, mediaContent).setFields("id").execute();
            return newFile.getId();
        } else {
            // No existe, lo creamos
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            if (parentId != null && !parentId.isEmpty()) {
                fileMetadata.setParents(Collections.singletonList(parentId));
            }
            File newFile = driveService.files().create(fileMetadata, mediaContent).setFields("id").execute();
            return newFile.getId();
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
}
