package org.example.springboot_backend.controller;

import org.example.springboot_backend.entity.Role;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.UserStatus;
import org.example.springboot_backend.repository.RoleRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/admin-setup")
@CrossOrigin(origins = "*")
public class AdminSetupController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody Map<String, String> request) {
        try {
            String email = request.getOrDefault("email", "admin@lirium.com");
            String password = request.getOrDefault("password", "admin123");
            String firstName = request.getOrDefault("firstName", "Admin");
            String lastName = request.getOrDefault("lastName", "Sistema");

            // Verificar si ya existe un usuario con ese email
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Ya existe un usuario con el email: " + email));
            }

            // Buscar o crear rol ADMIN
            Optional<Role> adminRoleOpt = roleRepository.findByName("ADMIN");
            Role adminRole;
            
            if (adminRoleOpt.isEmpty()) {
                adminRole = new Role();
                adminRole.setName("ADMIN");
                adminRole = roleRepository.save(adminRole);
            } else {
                adminRole = adminRoleOpt.get();
            }

            // Crear usuario administrador
            User admin = new User();
            admin.setFirstName(firstName);
            admin.setFirstLastName(lastName);
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setStatus(UserStatus.ACTIVE);
            admin.setUsedSpace(0.0);
            admin.setTotalCapacity(999999.0);
            admin.setDocumentariesPurchased(999);
            admin.setDocumentariesAvailable(999);
            admin.setCreatedDate(LocalDate.now());
            admin.setUpdatedDate(LocalDate.now());
            admin.setLastSessionDate(LocalDateTime.now());
            
            // Asignar rol
            admin.setRoles(Set.of(adminRole));

            // Guardar usuario
            User savedAdmin = userRepository.save(admin);

            return ResponseEntity.ok(Map.of(
                "message", "Administrador creado exitosamente",
                "userId", savedAdmin.getIdUser().toString(),
                "email", savedAdmin.getEmail(),
                "name", savedAdmin.getFullName(),
                "role", "ADMIN",
                "warning", "IMPORTANTE: Cambiar la contraseña después del primer login"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Error al crear administrador: " + e.getMessage()));
        }
    }

    @GetMapping("/check-admin")
    public ResponseEntity<?> checkAdminExists() {
        try {
            // Buscar usuarios con rol ADMIN
            Optional<Role> adminRole = roleRepository.findByName("ADMIN");
            
            if (adminRole.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "hasAdmin", false,
                    "message", "No existe el rol ADMIN"
                ));
            }

            // Contar usuarios con rol ADMIN
            long adminCount = userRepository.countByRolesContaining(adminRole.get());

            return ResponseEntity.ok(Map.of(
                "hasAdmin", adminCount > 0,
                "adminCount", adminCount,
                "message", adminCount > 0 ? 
                    "Existen " + adminCount + " administradores" : 
                    "No hay administradores registrados"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Error al verificar administradores: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-admin-password")
    public ResponseEntity<?> resetAdminPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String newPassword = request.get("newPassword");

            if (email == null || newPassword == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email y nueva contraseña son requeridos"));
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            
            // Verificar que el usuario tenga rol ADMIN
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> "ADMIN".equals(role.getName()));

            if (!isAdmin) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "El usuario no tiene permisos de administrador"));
            }

            // Actualizar contraseña
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setUpdatedDate(LocalDate.now());
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                "message", "Contraseña actualizada exitosamente",
                "email", email
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Error al actualizar contraseña: " + e.getMessage()));
        }
    }
}