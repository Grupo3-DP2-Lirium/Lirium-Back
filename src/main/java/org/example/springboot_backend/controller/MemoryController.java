package org.example.springboot_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.springboot_backend.dto.MemoryCreateRequest;
import org.example.springboot_backend.dto.MemoryResponse;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.IMemoryService;
import org.example.springboot_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/memories")
@CrossOrigin(origins = "*")
public class MemoryController {
    
    @Autowired
    private IMemoryService memoryService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MemorialRepository memorialRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * ✅ ACTUALIZADO: Crea notificación detallada según tipo de archivo
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> createMemory(
            @RequestPart("memory") String memoryJson,
            @RequestParam(value = "files", required = false) List<MultipartFile> filesList,
            Authentication authentication) {
        
        try {
            MultipartFile[] files = null;
            if (filesList != null && !filesList.isEmpty()) {
                files = filesList.toArray(new MultipartFile[0]);
            }
            
            System.out.println("DEBUG MemoryController - Received files: " + 
                (filesList != null ? filesList.size() : 0));
            
            MemoryCreateRequest request = objectMapper.readValue(memoryJson, MemoryCreateRequest.class);
            
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Crear memoria
            MemoryResponse response = memoryService.createMemory(request, files, user);
            
            // ✅ NUEVO: Analizar archivos y crear mensaje detallado
            String memoryTitle = request.getTitle() != null ? request.getTitle() : "Nueva memoria";
            String notificationMessage = buildNotificationMessage(memoryTitle, files);
            
            // Crear notificación con detalles
            notificationService.notifyMemoryCreatedDetailed(
                user, 
                notificationMessage,
                response.getIdMemory().getMostSignificantBits()
            );
            
            // ✅ Si el memorial es colaborativo, notificar al dueño
            Memorial memorial = memorialRepository.findById(request.getMemorialId())
                .orElseThrow(() -> new RuntimeException("Memorial not found"));
            
            if (memorial.isCollaborative() && !memorial.getUser().getIdUser().equals(user.getIdUser())) {
                String collaboratorMessage = String.format(
                    "%s %s agregó '%s' %s", 
                    user.getFirstName(), 
                    user.getFirstLastName(),
                    memoryTitle,
                    getFileTypeSummary(files)
                );
                notificationService.notifyCollaborators(memorial, user, collaboratorMessage);
            }
            
            System.out.println("✅ Memoria creada y notificaciones enviadas: " + memoryTitle);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * ✅ Construye mensaje inteligente según tipo de archivos
     */
    private String buildNotificationMessage(String title, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return String.format("Agregaste '%s'", title);
        }
        
        int imageCount = 0;
        int videoCount = 0;
        int audioCount = 0;
        int documentCount = 0;
        
        for (MultipartFile file : files) {
            String mimeType = file.getContentType();
            if (mimeType != null) {
                if (mimeType.startsWith("image/")) {
                    imageCount++;
                } else if (mimeType.startsWith("video/")) {
                    videoCount++;
                } else if (mimeType.startsWith("audio/")) {
                    audioCount++;
                } else {
                    documentCount++;
                }
            }
        }
        
        StringBuilder message = new StringBuilder(String.format("Agregaste '%s' con ", title));
        boolean needsComma = false;
        
        if (imageCount > 0) {
            message.append(imageCount).append(imageCount == 1 ? " imagen" : " imágenes");
            needsComma = true;
        }
        
        if (videoCount > 0) {
            if (needsComma) message.append(", ");
            message.append(videoCount).append(videoCount == 1 ? " video" : " videos");
            needsComma = true;
        }
        
        if (audioCount > 0) {
            if (needsComma) message.append(", ");
            message.append(audioCount).append(audioCount == 1 ? " audio" : " audios");
            needsComma = true;
        }
        
        if (documentCount > 0) {
            if (needsComma) message.append(", ");
            message.append(documentCount).append(documentCount == 1 ? " documento" : " documentos");
        }
        
        return message.toString();
    }
    
    /**
     * ✅ Resumen corto de archivos para notificación a colaboradores
     */
    private String getFileTypeSummary(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return "";
        }
        
        boolean hasImages = false;
        boolean hasVideos = false;
        
        for (MultipartFile file : files) {
            String mimeType = file.getContentType();
            if (mimeType != null) {
                if (mimeType.startsWith("image/")) hasImages = true;
                if (mimeType.startsWith("video/")) hasVideos = true;
            }
        }
        
        if (hasImages && hasVideos) {
            return "con imágenes y videos";
        } else if (hasImages) {
            return "con imágenes";
        } else if (hasVideos) {
            return "con videos";
        } else {
            return "con archivos";
        }
    }
    
    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Page<MemoryResponse>> listByMemorial(
            @RequestParam UUID memorialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MemoryResponse> resp = memoryService.listByMemorial(memorialId, page, size);
        return ResponseEntity.ok(resp);
    }
}