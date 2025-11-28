package org.example.springboot_backend.config;

import com.google.analytics.data.v1beta.*;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Configuración de Google Analytics Data API
 * Soporta credenciales inline (desde secrets) o desde archivo classpath
 */
@Configuration
public class GoogleAnalyticsConfig {
    
    @Value("${google.analytics.credentials.json:}")
    private String googleAnalyticsCredentialsJson;
    
    @Value("${google.analytics.enabled:true}")
    private boolean googleAnalyticsEnabled;
    
    @Bean
    public BetaAnalyticsDataClient betaAnalyticsDataClient() throws IOException {
        if (!googleAnalyticsEnabled) {
            System.out.println("⚠️ Google Analytics está deshabilitado");
            return null;
        }
        
        GoogleCredentials credentials;
        
        // Prioridad 1: Leer desde propiedad de Spring (producción - JSON inline)
        if (googleAnalyticsCredentialsJson != null && !googleAnalyticsCredentialsJson.trim().isEmpty()) {
            try (InputStream credentialsStream = new ByteArrayInputStream(
                    googleAnalyticsCredentialsJson.getBytes(StandardCharsets.UTF_8))) {
                credentials = GoogleCredentials.fromStream(credentialsStream)
                        .createScoped("https://www.googleapis.com/auth/analytics.readonly");
                System.out.println("✅ Google Analytics inicializado con credenciales desde application.properties");
            }
        } 
        // Prioridad 2: Leer desde classpath (desarrollo local)
        else {
            InputStream credentialsStream = getClass().getResourceAsStream("/lirium-3c939-32ad6310d074.json");
            
            if (credentialsStream != null) {
                try {
                    credentials = GoogleCredentials.fromStream(credentialsStream)
                            .createScoped("https://www.googleapis.com/auth/analytics.readonly");
                    System.out.println("✅ Google Analytics inicializado con credenciales desde classpath");
                } finally {
                    credentialsStream.close();
                }
            } else {
                System.out.println("⚠️ No se encontraron credenciales de Google Analytics. El servicio no estará disponible.");
                return null;
            }
        }
        
        BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        
        return BetaAnalyticsDataClient.create(settings);
    }
}