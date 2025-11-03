package org.example.springboot_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options;

            // Intentar cargar desde classpath (funciona tanto en dev como en producción JAR)
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
                // Fallback: intentar usar credenciales del entorno (Google Cloud)
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();
                System.out.println("✅ Firebase inicializado con credenciales por entorno");
            }

            FirebaseApp.initializeApp(options);
        }
    }
}