package org.example.springboot_backend.config;

import com.google.analytics.data.v1beta.*;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Configuración de Google Analytics Data API
 */
@Configuration
public class GoogleAnalyticsConfig {
    
    @Value("${google.analytics.credentials.path}")
    private Resource credentialsResource;
    
    @Bean
    public BetaAnalyticsDataClient betaAnalyticsDataClient() throws IOException {
        GoogleCredentials credentials;
        
        try (InputStream credentialsStream = credentialsResource.getInputStream()) {
            credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped("https://www.googleapis.com/auth/analytics.readonly");
        }
        
        BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        
        return BetaAnalyticsDataClient.create(settings);
    }
}