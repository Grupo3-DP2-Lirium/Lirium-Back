package org.example.springboot_backend.controller;

import org.example.springboot_backend.dto.UserAdminResponse;
import org.example.springboot_backend.dto.UserDetailDTO;
import org.example.springboot_backend.dto.UserListDTO;
import org.example.springboot_backend.dto.UserSearchFiltersDTO;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.UserStatus;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controlador para la gestión de usuarios por parte del administrador
 * Incluye funcionalidades para listar, buscar, filtrar y ver detalles de usuarios
 */
@RestController
@RequestMapping("/api/admin/users")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserRepository userRepository;

    /**
     * HU37 - Listar todos los usuarios con paginación y filtros
     * HU39 - Búsqueda y filtrado de usuarios
     * 
     * @param filters Filtros de búsqueda encapsulados en DTO
     * @return Página de usuarios con metadatos de paginación
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(UserSearchFiltersDTO filters) {
        try {
            Page<UserListDTO> userPage = adminUserService.getUsersWithFilters(filters);
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", userPage.getContent());
            response.put("currentPage", userPage.getNumber());
            response.put("totalItems", userPage.getTotalElements());
            response.put("totalPages", userPage.getTotalPages());
            response.put("pageSize", userPage.getSize());
            response.put("hasNext", userPage.hasNext());
            response.put("hasPrevious", userPage.hasPrevious());
            response.put("isFirst", userPage.isFirst());
            response.put("isLast", userPage.isLast());
            
            // Información adicional sobre filtros aplicados
            Map<String, Object> appliedFilters = new HashMap<>();
            appliedFilters.put("search", filters.getSearch());
            appliedFilters.put("status", filters.getStatus());
            appliedFilters.put("role", filters.getRole());
            appliedFilters.put("planType", filters.getPlanType());
            appliedFilters.put("createdDateFrom", filters.getCreatedDateFrom());
            appliedFilters.put("createdDateTo", filters.getCreatedDateTo());
            appliedFilters.put("lastSessionFrom", filters.getLastSessionFrom());
            appliedFilters.put("lastSessionTo", filters.getLastSessionTo());
            appliedFilters.put("usedSpaceMin", filters.getUsedSpaceMin());
            appliedFilters.put("usedSpaceMax", filters.getUsedSpaceMax());
            appliedFilters.put("sortBy", filters.getSortBy());
            appliedFilters.put("sortDirection", filters.getSortDirection());
            
            response.put("appliedFilters", appliedFilters);
            response.put("success", true);
            response.put("message", "Usuarios obtenidos exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener la lista de usuarios: " + e.getMessage());
            errorResponse.put("users", null);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }

    /**
     * HU38 - Obtener detalle completo de un usuario específico
     * 
     * @param userId ID del usuario a consultar
     * @return Detalle completo del usuario
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable UUID userId) {
        try {
            Optional<UserDetailDTO> userDetailOpt = adminUserService.getUserDetail(userId);
            
            if (userDetailOpt.isEmpty()) {
                Map<String, Object> notFoundResponse = new HashMap<>();
                notFoundResponse.put("success", false);
                notFoundResponse.put("message", "Usuario no encontrado con ID: " + userId);
                notFoundResponse.put("user", null);
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(notFoundResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Detalle del usuario obtenido exitosamente");
            response.put("user", userDetailOpt.get());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener el detalle del usuario: " + e.getMessage());
            errorResponse.put("user", null);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }

    /**
     * Endpoint para obtener estadísticas generales de usuarios (útil para dashboard admin)
     * 
     * @return Estadísticas generales del sistema
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUsersStatistics() {
        try {
            long totalUsers = adminUserService.getTotalUsersCount();
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalUsers", totalUsers);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estadísticas obtenidas exitosamente");
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener estadísticas: " + e.getMessage());
            errorResponse.put("statistics", null);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }

    /**
     * Endpoint simple para obtener todos los usuarios sin paginación (para casos específicos)
     * 
     * @return Lista completa de usuarios
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllUsersSimple() {
        try {
            List<UserListDTO> users = adminUserService.getAllUsers();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lista completa de usuarios obtenida exitosamente");
            response.put("users", users);
            response.put("totalCount", users.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener la lista completa de usuarios: " + e.getMessage());
            errorResponse.put("users", null);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
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