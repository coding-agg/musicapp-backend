package com.example.musicapp.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "Music App Drive Uploader";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    // 🔁 REPLACE this with your actual folder ID
    private static final String DRIVE_FOLDER_ID = "1s7M3NcHh9klJt0q83QAJ7IwVlX9YdPRK";

    public Drive getDriveService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);

        if (in == null) {
            throw new IOException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        var credential = new com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp(
                flow,
                new com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver.Builder()
                        .setPort(8888)
                        .setCallbackPath("/oauth2callback")
                        .build()
        ).authorize("user");


        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String uploadToDrive(java.io.File file) throws IOException, GeneralSecurityException {
        Drive driveService = getDriveService();

        File fileMetadata = new File();
        fileMetadata.setName(file.getName());

        // ✅ Set the target folder ID in Drive
        fileMetadata.setParents(Collections.singletonList(DRIVE_FOLDER_ID));

        FileContent mediaContent = new FileContent("audio/mpeg", file);

        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return uploadedFile.getId();
    }
}
