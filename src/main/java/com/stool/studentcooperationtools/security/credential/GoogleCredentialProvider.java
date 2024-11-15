package com.stool.studentcooperationtools.security.credential;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.slides.v1.SlidesScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.Getter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Collections;
import java.util.List;

@Getter
@Component
public class GoogleCredentialProvider {
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final List<String> SCOPES = Collections.singletonList(SlidesScopes.PRESENTATIONS);

    private final String credentialsFilePath;

    private final String tokensDirectoryPath;

    private final String credentialsforupdateFilePath;

    private Credential credential;

    private HttpCredentialsAdapter credentialsAdapter;

    public GoogleCredentialProvider(@Value("${google.slides.credentials-file-path}") String credentialsFilePath,
                                    @Value("${google.slides.tokens-directory-path}") String tokensDirectoryPath,
                                    @Value("${google.slides.credentials-forupdate-file-path}") String credentialsforupdateFilePath) throws IOException {
        this.credentialsFilePath = credentialsFilePath;
        this.tokensDirectoryPath = tokensDirectoryPath;
        this.credentialsforupdateFilePath = credentialsforupdateFilePath;
    }

    public void initializeCredential(String userId) throws IOException {
        this.credential = authorize(userId);
    }

    public void initializeCredentialAdapter() throws IOException {
        this.credentialsAdapter = (HttpCredentialsAdapter) getHttpRequestInitializer();
    }

    private Credential authorize(String userId) {
        try(InputStream in = GoogleCredentialProvider.class.getResourceAsStream(credentialsFilePath)) {

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize(userId);
        } catch (IOException e) {
            throw new BeanCreationException(e.getMessage());
        }
    }

    private HttpRequestInitializer getHttpRequestInitializer() {
        try(InputStream in = GoogleCredentialProvider.class.getResourceAsStream(credentialsforupdateFilePath)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                    .createScoped(List.of(DriveScopes.DRIVE_FILE));
            return new HttpCredentialsAdapter(
                    credentials);
        }catch (IOException e) {
            throw new BeanCreationException(e.getMessage(), e.getCause());
        }
    }

}