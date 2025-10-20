package org.example.springboot_backend.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {



    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Servir archivos estáticos desde el directorio de almacenamiento local
        String storageDir = System.getProperty("user.dir") + "/storage/";
        
        registry.addResourceHandler("/storage/**")
                .addResourceLocations("file:" + storageDir);
                

    }
}