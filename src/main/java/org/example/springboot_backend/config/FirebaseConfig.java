package org.example.springboot_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options;

            // Si existe el archivo de credenciales local (modo desarrollo)
            File credentialsFile = new File("src/main/resources/firebase.json");

            if (credentialsFile.exists()) {
                try (FileInputStream serviceAccount = new FileInputStream(credentialsFile)) {
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    System.out.println("✅ Firebase inicializado con credenciales locales");
                }
            } else {
                // Si no existe, intenta usar las credenciales del entorno (modo producción)
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();
                System.out.println("✅ Firebase inicializado con credenciales por entorno");
            }

            FirebaseApp.initializeApp(options);
        }
    }
}