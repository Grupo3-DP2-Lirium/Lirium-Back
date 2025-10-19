package org.example.springboot_backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/image-enhancer")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class ImageEnhanceController {

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    @Value("${ml.service.api.key:Ng0lWVACXQH+ruVvzQJAgEVz1oAFGs93aMFpcUyhHxo=}")
    private String mlServiceApiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping(value = "/enhance", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enhanceImage(@RequestPart("file") MultipartFile image) {
        try {
            // Validaciones
            if (image.isEmpty()) {
                return ResponseEntity.badRequest().body("No image provided");
            }

            if (!image.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("File must be an image");
            }

            if (image.getSize() > 1024 * 1024) {
                return ResponseEntity.badRequest().body("File must be less than 1MB");
            }

            // Prepara headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-API-Key", mlServiceApiKey);

            // Construir body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String enhanceUrl = mlServiceUrl + "/api/v1/enhance";

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    enhanceUrl,
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class
            );

            // Devuelve la imagen mejorada
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header("Content-Disposition", "attachment; filename=enhanced_" + image.getOriginalFilename())
                    .body(response.getBody());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading image: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error enhancing image: " + e.getMessage());
        }
    }

    // Endpoint para probar conectividad
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                mlServiceUrl + "/api/v1/health", 
                String.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("ML service not available: " + e.getMessage());
        }
    }
}