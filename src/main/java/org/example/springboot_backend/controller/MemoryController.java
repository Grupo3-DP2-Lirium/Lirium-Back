package org.example.springboot_backend.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.example.springboot_backend.dto.FileDeleteRequest;
import org.example.springboot_backend.dto.MemoriesByTypeResponse;
import org.example.springboot_backend.dto.MemoryCreateRequest;
import org.example.springboot_backend.dto.MemoryLiteResponse;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

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

    @GetMapping("/my-memories")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> listByAuthor(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            var memories = memoryService.listByAuthor(user); // retorna List<MemoryResponse> con archivos en Base64
            return ResponseEntity.ok(memories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Update memory
    @PutMapping(value = "/{memoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> updateMemory(
            @PathVariable UUID memoryId,
            @RequestPart("memory") String memoryJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @RequestPart(value = "filesToDelete", required = false) String filesToDeleteJson, // <-- nuevo
            Authentication authentication) {
        try {
            // Parse JSON a MemoryCreateRequest
            MemoryCreateRequest request = objectMapper.readValue(memoryJson, MemoryCreateRequest.class);

            // Parse JSON de archivos a eliminar (opcional)
            List<FileDeleteRequest> filesToDelete = new ArrayList<>();
            if (filesToDeleteJson != null && !filesToDeleteJson.isEmpty()) {
                filesToDelete = Arrays.asList(objectMapper.readValue(filesToDeleteJson, FileDeleteRequest[].class));
            }

            // Obtener usuario logueado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Llamar al servicio de actualización
            MemoryResponse response = memoryService.updateMemory(memoryId, request, files, filesToDelete, user);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating memory: " + e.getMessage());
        }
    }

    // Delete memory
    @DeleteMapping("/{memoryId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> deleteMemory(
            @PathVariable UUID memoryId,
            Authentication authentication) {
        try {
            // Obtener usuario logueado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Llamar al servicio de eliminación
            memoryService.deleteMemory(memoryId, user);

            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (RuntimeException e) {
            // Manejar errores específicos
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body("Memory not found: " + e.getMessage());
            } else if (e.getMessage().contains("not have permission")) {
                return ResponseEntity.status(403).body("Forbidden: " + e.getMessage());
            }
            return ResponseEntity.badRequest().body("Error deleting memory: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/grouped-by-category")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<java.util.Map<String, java.util.Map<String, java.util.List<MemoryLiteResponse>>>> getGroupedByCategory(
            @RequestParam UUID memorialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            Authentication authentication
    ) {
        // (opcional) validar acceso con usuario
        String userEmail = authentication.getName();
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var data = memoryService.listGroupedByCategoryAndType(memorialId, page, size);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/grouped-by-moment")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<java.util.Map<String, java.util.Map<String, java.util.List<MemoryLiteResponse>>>> getGroupedByMoment(
            @RequestParam UUID memorialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            Authentication authentication
    ) {
        // (opcional) validar acceso con usuario
        String userEmail = authentication.getName();
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var data = memoryService.listGroupedByMomentsAndType(memorialId, page, size);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/timeline/{memorialId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<MemoryResponse>> getTimeline(
            @PathVariable UUID memorialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(memoryService.findTimelineMemories(memorialId, page, size));
    }

    @GetMapping("/by-type/{memorialId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<MemoriesByTypeResponse> getMemoriesByType(
            @PathVariable UUID memorialId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(memoryService.getMemoriesByType(memorialId, user));
    }

}