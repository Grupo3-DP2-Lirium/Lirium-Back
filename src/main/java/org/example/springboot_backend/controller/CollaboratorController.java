package org.example.springboot_backend.controller;

import org.example.springboot_backend.dto.*;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.security.CustomUserDetails;
import org.example.springboot_backend.service.ICollaboratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/collaborators")
@CrossOrigin(origins = "*")
public class CollaboratorController {

    @Autowired
    private ICollaboratorService collaboratorService;

    @PostMapping("/invite-code")
    public ResponseEntity<InviteCodeResponse> generateInviteCode(
            @RequestBody InviteCodeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            User user = userDetails.getUser();
            InviteCodeResponse response = collaboratorService.generateInviteCode(request, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error en generateInviteCode: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/my-collaborations")
public ResponseEntity<List<MemorialResponse>> getMyCollaborations(
        @AuthenticationPrincipal CustomUserDetails userDetails
) {
    try {
        // ✅ DEBUG: Verificar que userDetails existe
        if (userDetails == null) {
            System.err.println("❌ userDetails es NULL");
            return ResponseEntity.status(401).body(Collections.emptyList());
        }
        
        User user = userDetails.getUser();
        
        // ✅ DEBUG: Verificar que user existe y tiene ID
        if (user == null) {
            System.err.println("❌ user es NULL");
            return ResponseEntity.status(401).body(Collections.emptyList());
        }
        
        System.out.println("📋 Usuario autenticado:");
        System.out.println("   - ID: " + user.getIdUser());
        System.out.println("   - Email: " + user.getEmail());
        System.out.println("   - Nombre: " + user.getFirstName());
        
        List<MemorialResponse> response = collaboratorService.getMyCollaborations(user);
        
        System.out.println("✅ Colaboraciones encontradas: " + response.size());
        
        return ResponseEntity.ok(response != null ? response : Collections.emptyList());
        
    } catch (Exception e) {
        System.err.println("❌ Error obteniendo colaboraciones: " + e.getMessage());
        e.printStackTrace();
        
        // ✅ Retornar 500 en lugar de 400 para errores internos
        return ResponseEntity.status(500).body(Collections.emptyList());
    }
}
    
    @GetMapping("/validate-code/{code}")
    public ResponseEntity<?> validateCode(@PathVariable String code) {
        try {
            InviteCodeValidationResponse response = collaboratorService.validateInviteCode(code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error validando código: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/accept-code/{code}")
    public ResponseEntity<?> acceptInviteCode(
            @PathVariable String code,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            User user = userDetails.getUser();
            CollaboratorResponse response = collaboratorService.acceptInviteCode(code, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error aceptando código: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/memorial/{memorialId}")
    public ResponseEntity<List<CollaboratorResponse>> getCollaborators(
            @PathVariable String memorialId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            User user = userDetails.getUser();
            UUID uuid = UUID.fromString(memorialId);
            List<CollaboratorResponse> response = collaboratorService.getCollaborators(uuid, user);
            return ResponseEntity.ok(response != null ? response : Collections.emptyList());
        } catch (IllegalArgumentException e) {
            System.err.println("❌ ID inválido: " + e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (RuntimeException e) {
            System.err.println("❌ Error listando colaboradores: " + e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    @PutMapping("/{collaboratorId}")
    public ResponseEntity<CollaboratorResponse> updateCollaborator(
            @PathVariable Long collaboratorId,
            @RequestBody Map<String, Boolean> permissions,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            User user = userDetails.getUser();
            CollaboratorResponse response = collaboratorService.updateCollaborator(
                    collaboratorId,
                    permissions.get("canEdit"),
                    permissions.get("canComment"),
                    user
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error actualizando colaborador: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{collaboratorId}")
    public ResponseEntity<Void> removeCollaborator(
            @PathVariable Long collaboratorId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            User user = userDetails.getUser();
            collaboratorService.removeCollaborator(collaboratorId, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ Error eliminando colaborador: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/invite-codes/{memorialId}")
    public ResponseEntity<List<InviteCodeResponse>> getInviteCodes(
            @PathVariable String memorialId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            User user = userDetails.getUser();
            UUID uuid = UUID.fromString(memorialId);
            List<InviteCodeResponse> response = collaboratorService.getInviteCodes(uuid, user);
            return ResponseEntity.ok(response != null ? response : Collections.emptyList());
        } catch (IllegalArgumentException e) {
            System.err.println("❌ ID inválido: " + e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (RuntimeException e) {
            System.err.println("❌ Error listando códigos: " + e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    @DeleteMapping("/invite-code/{code}")
    public ResponseEntity<Void> revokeInviteCode(
            @PathVariable String code,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            User user = userDetails.getUser();
            collaboratorService.revokeInviteCode(code, user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("❌ Error revocando código: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/invite-by-email")
public ResponseEntity<?> inviteByEmail(
        @RequestBody Map<String, Object> request,
        @AuthenticationPrincipal CustomUserDetails userDetails
) {
    try {
        User user = userDetails.getUser();
        
        String memorialIdStr = (String) request.get("memorialId");
        String inviteeEmail = (String) request.get("email");
        Boolean canEdit = (Boolean) request.get("canEdit");
        Boolean canComment = (Boolean) request.get("canComment");
        
        UUID memorialId = UUID.fromString(memorialIdStr);
        
        collaboratorService.inviteByEmail(
            memorialId,
            inviteeEmail,
            canEdit,
            canComment,
            user
        );
        
        return ResponseEntity.ok(Map.of(
            "message", "Invitación enviada exitosamente",
            "email", inviteeEmail
        ));
        
    } catch (RuntimeException e) {
        System.err.println("❌ Error en inviteByEmail: " + e.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        
    } catch (Exception e) {
        System.err.println("❌ Error inesperado: " + e.getMessage());
        return ResponseEntity.status(500).body(Map.of("message", "Error interno del servidor"));
    }
}
}