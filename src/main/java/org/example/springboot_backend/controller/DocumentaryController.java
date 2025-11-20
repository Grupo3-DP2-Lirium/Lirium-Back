package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot_backend.dto.CreateDocumentaryRequest;
import org.example.springboot_backend.dto.DocumentaryResponse;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.DocumentaryStatus;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.DocumentaryService;
import org.example.springboot_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documentaries")
@CrossOrigin(origins = "*")
@Tag(name = "Documentaries", description = "Documentary generation and management")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentaryController {

    @Autowired
    private DocumentaryService documentaryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemorialRepository memorialRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Validar si un memorial tiene suficientes recuerdos
     */
    @GetMapping("/validate-memorial/{memorialId}")
    @Operation(summary = "Validate memorial for documentary",
            description = "Check if memorial has enough memories (min 50) for documentary creation")
    public ResponseEntity<?> validateMemorial(@PathVariable UUID memorialId) {
        try {
            Map<String, Object> validation = documentaryService.validateMemorialForDocumentary(memorialId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", validation
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * ✅ ACTUALIZADO: Ahora crea en estado DRAFT y NO inicia procesamiento
     */
    @PostMapping
    @Operation(summary = "Create a new documentary draft",
            description = "Creates a documentary in DRAFT state from memorial configuration")
    public ResponseEntity<?> createDocumentary(
            @RequestBody CreateDocumentaryRequest request,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);

            // Crear documental en estado DRAFT (sin procesar aún)
            DocumentaryResponse response = documentaryService.createDocumentaryDraft(request, userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Documentary draft created successfully",
                    "data", response
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * ✨ NUEVO: Iniciar generación del documental (cuando usuario presiona "Generar")
     */
    @PostMapping("/{documentaryId}/generate")
    @Operation(summary = "Generate documentary video",
            description = "Starts the video generation process for a DRAFT documentary")
    public ResponseEntity<?> generateDocumentary(
            @PathVariable UUID documentaryId,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            DocumentaryResponse response = documentaryService.startDocumentaryGeneration(documentaryId, userId);

            // Notificar inicio de procesamiento
            Memorial memorial = memorialRepository.findById(response.getMemorialId())
                    .orElseThrow(() -> new RuntimeException("Memorial not found"));

            notificationService.createNotification(
                    user,
                    org.example.springboot_backend.enums.NotificationType.DOCUMENTARY,
                    "Documental en proceso",
                    String.format("Tu documental de '%s' se está generando. Te notificaremos cuando esté listo.",
                            memorial.getName()),
                    response.getIdDocumentary().getMostSignificantBits()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Documentary generation started",
                    "data", response
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * ✨ NUEVO: Publicar documental en el perfil
     */
    @PostMapping("/{documentaryId}/publish")
    @Operation(summary = "Publish documentary",
            description = "Publish a COMPLETED documentary to the memorial profile")
    public ResponseEntity<?> publishDocumentary(
            @PathVariable UUID documentaryId,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            DocumentaryResponse response = documentaryService.publishDocumentary(documentaryId, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Documentary published successfully",
                    "data", response
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * ✨ NUEVO: Actualizar documental (editar título, descripción, etc.)
     */
    @PutMapping("/{documentaryId}")
    @Operation(summary = "Update documentary",
            description = "Update documentary details (title, description, settings)")
    public ResponseEntity<?> updateDocumentary(
            @PathVariable UUID documentaryId,
            @RequestBody CreateDocumentaryRequest request,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            DocumentaryResponse response = documentaryService.updateDocumentary(documentaryId, request, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Documentary updated successfully",
                    "data", response
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * ✨ NUEVO: Obtener documentales por estado (drafts o published)
     */
    @GetMapping("/memorial/{memorialId}/by-status")
    @Operation(summary = "Get documentaries by status",
            description = "Get documentaries filtered by status (DRAFT or PUBLISHED)")
    public ResponseEntity<?> getDocumentariesByStatus(
            @PathVariable UUID memorialId,
            @RequestParam String status) {
        try {
            DocumentaryStatus documentaryStatus = DocumentaryStatus.valueOf(status.toUpperCase());
            List<DocumentaryResponse> responses = documentaryService.getDocumentariesByMemorialAndStatus(
                    memorialId, documentaryStatus);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/{documentaryId}")
    @Operation(summary = "Get documentary status",
            description = "Get the current status and details of a documentary")
    public ResponseEntity<?> getDocumentaryStatus(@PathVariable UUID documentaryId) {
        try {
            DocumentaryResponse response = documentaryService.getDocumentaryStatus(documentaryId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/memorial/{memorialId}")
    @Operation(summary = "Get documentaries by memorial",
            description = "Get all documentaries created for a specific memorial")
    public ResponseEntity<?> getDocumentariesByMemorial(@PathVariable UUID memorialId) {
        try {
            List<DocumentaryResponse> responses = documentaryService.getDocumentariesByMemorial(memorialId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/my-documentaries")
    @Operation(summary = "Get my documentaries",
            description = "Get all documentaries created by the current user")
    public ResponseEntity<?> getMyDocumentaries(Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            List<DocumentaryResponse> responses = documentaryService.getDocumentariesByUser(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @PostMapping("/{documentaryId}/cancel")
    @Operation(summary = "Cancel documentary processing",
            description = "Cancel a documentary that is pending or processing")
    public ResponseEntity<?> cancelDocumentary(
            @PathVariable UUID documentaryId,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            documentaryService.cancelDocumentary(documentaryId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Documentary cancelled successfully",
                    "documentaryId", documentaryId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @DeleteMapping("/{documentaryId}")
    @Operation(summary = "Delete documentary",
            description = "Delete a documentary and its associated video file")
    public ResponseEntity<?> deleteDocumentary(
            @PathVariable UUID documentaryId,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            documentaryService.deleteDocumentary(documentaryId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Documentary deleted successfully",
                    "documentaryId", documentaryId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Nuevo catálogo de música
     */
    @GetMapping("/music-catalog")
    @Operation(summary = "Get available music tracks",
            description = "Get list of available background music for documentaries")
    public ResponseEntity<?> getMusicCatalog() {
        try {
            List<Map<String, Object>> musicTracks = List.of(
                    Map.of(
                            "id", "music/calm-emotional-cello.mp3",
                            "previewId", "music/previews/calm-emotional-cello_preview_20s_64k.mp3",
                            "name", "Calm Emotional Cello",
                            "description", "Cello emotivo y calmado",
                            "duration", "3:45",
                            "mood", "emotional"
                    ),
                    Map.of(
                            "id", "music/piano-classical-music.mp3",
                            "previewId", "music/previews/piano-classical_preview_20s_64k.mp3",
                            "name", "Piano Classical Music",
                            "description", "Piano clásico elegante",
                            "duration", "4:20",
                            "mood", "classical"
                    ),
                    Map.of(
                            "id", "music/simple-happy-acoustic.mp3",
                            "previewId", "music/previews/simple-happy-acoustic_preview_20s_64k.mp3",
                            "name", "Simple Happy Acoustic",
                            "description", "Acústica alegre y simple",
                            "duration", "3:15",
                            "mood", "happy"
                    ),
                    Map.of(
                            "id", "music/mystic-melody.mp3",
                            "previewId", "music/previews/mystic-melody_preview_20s_64k.mp3",
                            "name", "Mystic Melody",
                            "description", "Melodía mística y contemplativa",
                            "duration", "4:05",
                            "mood", "mystic"
                    ),
                    Map.of(
                            "id", "music/land-of-tranquility.mp3",
                            "previewId", "music/previews/land-of-tranquility_preview_20s_64k.mp3",
                            "name", "Land of Tranquility",
                            "description", "Ambiente de tranquilidad y paz",
                            "duration", "4:30",
                            "mood", "peaceful"
                    )
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", musicTracks
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    private UUID getUserIdFromAuth(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getIdUser();
    }
}