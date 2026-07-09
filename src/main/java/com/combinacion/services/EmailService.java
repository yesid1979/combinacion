package com.combinacion.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Base64;

public class EmailService {

    private static final String APPLICATION_NAME = "Gestor Contratacion";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + "/.credentials/gmail-java";

    private static final List<String> SCOPES = Arrays.asList(
            GmailScopes.GMAIL_SEND,
            DriveScopes.DRIVE
    );
    private static final String CREDENTIALS_FILE_PATH = "/credencialescontratacion.json";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        InputStream in = EmailService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource no encontrado: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new JDBCDataStoreFactory())
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8889).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static Gmail getGmailService() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static Message createMessageWithEmail(MimeMessage emailContent) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public static boolean sendEmail(String toEmailAddress, String subject, String bodyText) {
        try {
            Gmail service = getGmailService();

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage email = new MimeMessage(session);
            email.setFrom(new InternetAddress("me")); // "me" representa el correo autenticado
            email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(toEmailAddress));
            email.setSubject(subject);
            email.setText(bodyText, "UTF-8");

            Message message = createMessageWithEmail(email);
            message = service.users().messages().send("me", message).execute();

            System.out.println("Email enviado correctamente. Message ID: " + message.getId());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private static Drive getDriveService() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static String createFolder(String folderName) {
        try {
            Drive driveService = getDriveService();
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(folderName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            com.google.api.services.drive.model.File folder = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("Carpeta creada con exito! ID: " + folder.getId());
            return folder.getId();
        } catch (Exception e) {
            System.err.println("Error al crear la carpeta en Drive:");
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando prueba de creacion de carpeta en Drive...");
            String folderId = createFolder("pruebas cuenta de cobro");
            if (folderId != null) {
                System.out.println("¡Prueba superada! Ve a tu Google Drive y busca la carpeta.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
