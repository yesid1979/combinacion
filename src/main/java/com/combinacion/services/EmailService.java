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

    private static final List<String> SCOPES = Arrays.asList(
            GmailScopes.GMAIL_SEND
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
        
        Credential credential = flow.loadCredential("user_v2");
        if (credential == null || (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60 && credential.getRefreshToken() == null)) {
            System.err.println("¡ADVERTENCIA! El token de Gmail no existe o expiro y no tiene refresh token. El correo NO se enviará.");
            throw new Exception("Token de Gmail no válido o expirado.");
        }
        
        // Refrescar manualmente si es necesario (el cliente lo hace solo, pero aquí aseguramos)
        if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
            credential.refreshToken();
        }
        
        return credential;
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
}
