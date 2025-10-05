package org.example.springboot_backend.controller;

import org.example.springboot_backend.service.DataSeederService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seed")
@CrossOrigin(origins = "*")
@Profile("dev") // Solo disponible en perfil de desarrollo
public class SeederController {

    @Autowired
    private DataSeederService seederService;

    /**
     * Sembrar solo roles básicos del sistema
     */
    @PostMapping("/roles")
    public ResponseEntity<Map<String, Object>> seedRoles() {
        try {
            seederService.seedRoles();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Roles seeded successfully",
                "operation", "seed_roles"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error seeding roles: " + e.getMessage(),
                "operation", "seed_roles"
            ));
        }
    }

    /**
     * Sembrar usuarios de prueba
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> seedUsers() {
        try {
            seederService.seedUsers();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Users seeded successfully",
                "operation", "seed_users"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error seeding users: " + e.getMessage(),
                "operation", "seed_users"
            ));
        }
    }

    /**
     * Sembrar memoriales para usuarios existentes
     */
    @PostMapping("/memorials")
    public ResponseEntity<Map<String, Object>> seedMemorials() {
        try {
            seederService.seedMemorials();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Memorials seeded successfully",
                "operation", "seed_memorials"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error seeding memorials: " + e.getMessage(),
                "operation", "seed_memorials"
            ));
        }
    }

    /**
     * Sembrar memorias para memoriales existentes
     */
    @PostMapping("/memories")
    public ResponseEntity<Map<String, Object>> seedMemories() {
        try {
            seederService.seedMemories();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Memories seeded successfully",
                "operation", "seed_memories"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error seeding memories: " + e.getMessage(),
                "operation", "seed_memories"
            ));
        }
    }

    /**
     * Sembrar todos los datos de una vez (roles, usuarios, memoriales, memorias)
     */
    @PostMapping("/all")
    public ResponseEntity<Map<String, Object>> seedAll() {
        try {
            seederService.seedAll();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All data seeded successfully",
                "operation", "seed_all",
                "description", "Created roles, users, memorials, and memories"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error seeding all data: " + e.getMessage(),
                "operation", "seed_all"
            ));
        }
    }

    /**
     * Crear un usuario completo con memoriales y memorias aleatorias
     * Este es el endpoint especial que solicitaste
     */
    @PostMapping("/complete-user")
    public ResponseEntity<Map<String, Object>> createCompleteUser() {
        try {
            Map<String, Object> result = seederService.createCompleteUserWithData();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Complete user with data created successfully",
                "operation", "create_complete_user",
                "data", result
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error creating complete user: " + e.getMessage(),
                "operation", "create_complete_user"
            ));
        }
    }

    /**
     * Limpiar todos los datos de prueba (excepto roles)
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearData() {
        try {
            seederService.clearAllData();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All test data cleared successfully",
                "operation", "clear_data",
                "note", "Roles were preserved as they are essential for the system"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error clearing data: " + e.getMessage(),
                "operation", "clear_data"
            ));
        }
    }

    /**
     * Endpoint informativo sobre las opciones disponibles
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSeederInfo() {
        return ResponseEntity.ok(Map.of(
            "available_endpoints", Map.of(
                "POST /api/seed/roles", "Create basic system roles (USER, ADMIN, PREMIUM)",
                "POST /api/seed/users", "Create test users (including admin user)",
                "POST /api/seed/memorials", "Create sample memorials for existing users",
                "POST /api/seed/memories", "Create sample memories for existing memorials",
                "POST /api/seed/all", "Create all test data in correct order",
                "POST /api/seed/complete-user", "Create one user with random memorials and memories",
                "DELETE /api/seed/clear", "Clear all test data (preserve roles)",
                "GET /api/seed/info", "Show this information"
            ),
            "profile", "dev",
            "note", "These endpoints are only available in development profile",
            "usage_example", Map.of(
                "complete_setup", "POST /api/seed/all",
                "single_user_test", "POST /api/seed/complete-user",
                "cleanup", "DELETE /api/seed/clear"
            )
        ));
    }
}