package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot_backend.dto.CreateCapsuleRequest;
import org.example.springboot_backend.dto.CapsuleResponse;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.CapsuleStatus;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.CapsuleService;
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
@RequestMapping("/api/capsules")
@CrossOrigin(origins = "*")
@Tag(name = "Capsules", description = "Capsule generation and management (vertical videos)")
@SecurityRequirement(name = "Bearer Authentication")
public class CapsuleController {

    @Autowired
    private CapsuleService capsuleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemorialRepository memorialRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Crea una cápsula en estado DRAFT
     */
    @PostMapping
    @Operation(summary = "Create a new capsule draft",
            description = "Creates a capsule in DRAFT state based on user prompt")
    public ResponseEntity<?> createCapsule(
            @RequestBody CreateCapsuleRequest request,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);

            CapsuleResponse response = capsuleService.createCapsuleDraft(request, userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Capsule draft created successfully",
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
     * Inicia la generación de la cápsula (selección automática + video vertical)
     */
    @PostMapping("/{capsuleId}/generate")
    @Operation(summary = "Generate capsule video",
            description = "Starts the video generation process with AI-selected memories")
    public ResponseEntity<?> generateCapsule(
            @PathVariable UUID capsuleId,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            CapsuleResponse response = capsuleService.startCapsuleGeneration(capsuleId, userId);

            // Notificar inicio de procesamiento
            Memorial memorial = memorialRepository.findById(response.getMemorialId())
                    .orElseThrow(() -> new RuntimeException("Memorial not found"));

            notificationService.createNotification(
                    user,
                    org.example.springboot_backend.enums.NotificationType.DOCUMENTARY,
                    "Cápsula en proceso",
                    String.format("Tu cápsula '%s' de %s se está generando. Te notificaremos cuando esté lista.",
                            response.getTitle(), memorial.getName()),
                    response.getIdCapsule().getMostSignificantBits()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Capsule generation started",
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
     * Publica cápsula en el memorial
     */
    @PostMapping("/{capsuleId}/publish")
    @Operation(summary = "Publish capsule",
            description = "Publish a COMPLETED capsule to the memorial profile")
    public ResponseEntity<?> publishCapsule(
            @PathVariable UUID capsuleId,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            CapsuleResponse response = capsuleService.publishCapsule(capsuleId, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Capsule published successfully",
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
     * Actualiza una cápsula
     */
    @PutMapping("/{capsuleId}")
    @Operation(summary = "Update capsule",
            description = "Update capsule details (title, description, filter, music)")
    public ResponseEntity<?> updateCapsule(
            @PathVariable UUID capsuleId,
            @RequestBody CreateCapsuleRequest request,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            CapsuleResponse response = capsuleService.updateCapsule(capsuleId, request, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Capsule updated successfully",
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
     * Obtiene cápsulas por estado
     */
    @GetMapping("/memorial/{memorialId}/by-status")
    @Operation(summary = "Get capsules by status",
            description = "Get capsules filtered by status (DRAFT, COMPLETED, PUBLISHED)")
    public ResponseEntity<?> getCapsulesByStatus(
            @PathVariable UUID memorialId,
            @RequestParam String status) {
        try {
            CapsuleStatus capsuleStatus = CapsuleStatus.valueOf(status.toUpperCase());
            List<CapsuleResponse> responses = capsuleService.getCapsulesByMemorialAndStatus(
                    memorialId, capsuleStatus);

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

    /**
     * Obtiene una cápsula por ID
     */
    @GetMapping("/{capsuleId}")
    @Operation(summary = "Get capsule status",
            description = "Get the current status and details of a capsule")
    public ResponseEntity<?> getCapsuleStatus(@PathVariable UUID capsuleId) {
        try {
            CapsuleResponse response = capsuleService.getCapsuleStatus(capsuleId);
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

    /**
     * Obtiene todas las cápsulas de un memorial
     */
    @GetMapping("/memorial/{memorialId}")
    @Operation(summary = "Get capsules by memorial",
            description = "Get all capsules created for a specific memorial")
    public ResponseEntity<?> getCapsulesByMemorial(@PathVariable UUID memorialId) {
        try {
            List<CapsuleResponse> responses = capsuleService.getCapsulesByMemorial(memorialId);
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

    /**
     * Obtiene mis cápsulas
     */
    @GetMapping("/my-capsules")
    @Operation(summary = "Get my capsules",
            description = "Get all capsules created by the current user")
    public ResponseEntity<?> getMyCapsules(Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            List<CapsuleResponse> responses = capsuleService.getCapsulesByUser(userId);
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

    /**
     * Cancela procesamiento de cápsula
     */
    @PostMapping("/{capsuleId}/cancel")
    @Operation(summary = "Cancel capsule processing",
            description = "Cancel a capsule that is pending or processing")
    public ResponseEntity<?> cancelCapsule(
            @PathVariable UUID capsuleId,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            capsuleService.cancelCapsule(capsuleId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Capsule cancelled successfully",
                    "capsuleId", capsuleId
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
     * Elimina una cápsula
     */
    @DeleteMapping("/{capsuleId}")
    @Operation(summary = "Delete capsule",
            description = "Delete a capsule and its associated video file")
    public ResponseEntity<?> deleteCapsule(
            @PathVariable UUID capsuleId,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            capsuleService.deleteCapsule(capsuleId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Capsule deleted successfully",
                    "capsuleId", capsuleId
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
     * Catálogo de filtros disponibles
     */
    @GetMapping("/filters")
    @Operation(summary = "Get available filters",
            description = "Get list of available visual filters for capsules")
    public ResponseEntity<?> getFilters() {
        try {
            List<Map<String, Object>> filters = List.of(
                    Map.of(
                            "id", "NATURAL",
                            "name", "Natural",
                            "description", "Sin filtro, colores originales"
                    ),
                    Map.of(
                            "id", "VIVID",
                            "name", "Vivid",
                            "description", "Colores vibrantes y saturados"
                    ),
                    Map.of(
                            "id", "DRAMATIC",
                            "name", "Dramatic",
                            "description", "Alto contraste, sombras profundas"
                    ),
                    Map.of(
                            "id", "YELLOW",
                            "name", "Yellow",
                            "description", "Tonos cálidos amarillos/dorados"
                    ),
                    Map.of(
                            "id", "MONO",
                            "name", "Mono",
                            "description", "Blanco y negro clásico"
                    ),
                    Map.of(
                            "id", "SILVERTONE",
                            "name", "Silvertone",
                            "description", "Tonos plateados/azulados"
                    )
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", filters
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