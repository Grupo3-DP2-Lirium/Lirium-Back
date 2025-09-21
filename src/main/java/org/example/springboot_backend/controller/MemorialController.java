package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/memorials")
@CrossOrigin(origins = "*")
public class MemorialController {

    @Autowired
    private MemorialRepository memorialRepository;

    @Autowired
    private UserRepository userRepository;

    // Crear memorial
    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> createMemorial(@RequestBody Memorial memorial, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            memorial.setUser(user);
            memorial.setCreatedDate(LocalDateTime.now());
            memorial.setUpdatedDate(LocalDateTime.now());

            Memorial saved = memorialRepository.save(memorial);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating memorial: " + e.getMessage());
        }
    }

    // Listar memorials del usuario autenticado
    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getMyMemorials(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Memorial> memorials = memorialRepository.findByUser(user);
            return ResponseEntity.ok(memorials);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching memorials: " + e.getMessage());
        }
    }

    // Obtener memorial por ID
    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getMemorial(@PathVariable UUID id, Authentication authentication) {
        return memorialRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar memorial
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> deleteMemorial(@PathVariable UUID id, Authentication authentication) {
        try {
            if (!memorialRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            memorialRepository.deleteById(id);
            return ResponseEntity.ok("Memorial deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting memorial: " + e.getMessage());
        }
    }
}
