package org.example.springboot_backend.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.example.springboot_backend.dto.UserAdminResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.AuditAction;
import org.example.springboot_backend.enums.UserStatus;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
@Tag(name = "Admin - Users", description = "Endpoints para administración de usuarios")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminUserController {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminUserController(UserRepository userRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')") // TODO: Descomentar cuando tengas usuario ADMIN
    @Operation(summary = "Listar todos los usuarios", description = "Obtiene una lista paginada de todos los usuarios del sistema")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<User> usersPage = userRepository.findAll(pageable);
            
            Page<UserAdminResponse> response = usersPage.map(this::mapToAdminResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener usuarios: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    // @PreAuthorize("hasRole('ADMIN')") // TODO: Descomentar cuando tengas usuario ADMIN
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los detalles de un usuario específico")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            return ResponseEntity.ok(mapToAdminResponse(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/disable")
    // @PreAuthorize("hasRole('ADMIN')") // TODO: Descomentar cuando tengas usuario ADMIN
    @Operation(summary = "Deshabilitar usuario", description = "Cambia el estado del usuario a SUSPENDED")
    public ResponseEntity<?> disableUser(@PathVariable UUID userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            if (user.getStatus() == UserStatus.SUSPENDED) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El usuario ya está deshabilitado"));
            }
            
            user.setStatus(UserStatus.SUSPENDED);
            user.setUpdatedDate(LocalDate.now());
            userRepository.save(user);
            
            // Registrar en auditoría
            auditLogService.log(
                AuditAction.ADMIN_USER_DISABLE,
                "User",
                userId.toString(),
                "Usuario " + user.getEmail() + " fue deshabilitado por un administrador"
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuario deshabilitado exitosamente");
            response.put("user", mapToAdminResponse(user));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al deshabilitar usuario: " + e.getMessage()));
        }
    }

    @PutMapping("/{userId}/enable")
    // @PreAuthorize("hasRole('ADMIN')") // TODO: Descomentar cuando tengas usuario ADMIN
    @Operation(summary = "Habilitar usuario", description = "Cambia el estado del usuario a ACTIVE")
    public ResponseEntity<?> enableUser(@PathVariable UUID userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            if (user.getStatus() == UserStatus.ACTIVE) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El usuario ya está activo"));
            }
            
            user.setStatus(UserStatus.ACTIVE);
            user.setUpdatedDate(LocalDate.now());
            userRepository.save(user);
            
            // Registrar en auditoría
            auditLogService.log(
                AuditAction.ADMIN_USER_ENABLE,
                "User",
                userId.toString(),
                "Usuario " + user.getEmail() + " fue habilitado por un administrador"
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuario habilitado exitosamente");
            response.put("user", mapToAdminResponse(user));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al habilitar usuario: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    // @PreAuthorize("hasRole('ADMIN')") // TODO: Descomentar cuando tengas usuario ADMIN
    @Operation(summary = "Estadísticas de usuarios", description = "Obtiene estadísticas generales de usuarios")
    public ResponseEntity<?> getUserStats() {
        try {
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
            long suspendedUsers = userRepository.countByStatus(UserStatus.SUSPENDED);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", totalUsers);
            stats.put("active", activeUsers);
            stats.put("suspended", suspendedUsers);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    @GetMapping("/check-role")
    @Operation(summary = "Verificar rol del usuario actual", description = "Endpoint temporal para verificar roles")
    public ResponseEntity<?> checkRole(org.springframework.security.core.Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Map<String, Object> info = new HashMap<>();
            info.put("email", user.getEmail());
            info.put("roles", user.getRoles().stream()
                .map(role -> role.getName())
                .toList());
            info.put("authorities", authentication.getAuthorities().toString());
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    private UserAdminResponse mapToAdminResponse(User user) {
        UserAdminResponse response = new UserAdminResponse();
        response.setIdUser(user.getIdUser());
        response.setFirstName(user.getFirstName());
        response.setFirstLastName(user.getFirstLastName());
        response.setSecondLastName(user.getSecondLastName());
        response.setEmail(user.getEmail());
        response.setStatus(user.getStatus().name());
        response.setUsedSpace(user.getUsedSpace());
        response.setTotalCapacity(user.getTotalCapacity());
        response.setCreatedDate(user.getCreatedDate());
        response.setUpdatedDate(user.getUpdatedDate());
        response.setLastSessionDate(user.getLastSessionDate());
        
        // Obtener roles
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            response.setRoleName(user.getRoles().iterator().next().getName());
        }
        
        return response;
    }
}
