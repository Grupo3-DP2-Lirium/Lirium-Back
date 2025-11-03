package org.example.springboot_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.json:}")
    private String firebaseCredentialsJson;

    @PostConstruct
    public void initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options;

            // Prioridad 1: Leer desde propiedad de Spring (producción)
            if (firebaseCredentialsJson != null && !firebaseCredentialsJson.trim().isEmpty()) {
                try (InputStream serviceAccount = new ByteArrayInputStream(
                        firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8))) {
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    System.out.println("✅ Firebase inicializado con credenciales desde application.properties");
                }
            } 
            // Prioridad 2: Leer desde classpath (desarrollo local)
            else {
                InputStream serviceAccount = getClass().getResourceAsStream("/firebase.json");
                
                if (serviceAccount != null) {
                    try {
                        options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .build();
                        System.out.println("✅ Firebase inicializado con credenciales desde classpath");
                    } finally {
                        serviceAccount.close();
                    }
                } else {
                    throw new IOException("No se encontraron credenciales de Firebase. " +
                            "Configure firebase.credentials.json en application.properties o incluya firebase.json en resources/");
                }
            }

            FirebaseApp.initializeApp(options);
        }
    }
}