package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.springboot_backend.dto.ReminderRequest;
import org.example.springboot_backend.dto.ReminderResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
@CrossOrigin(origins = "*")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtiene todos los recordatorios del usuario autenticado
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getReminders(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<ReminderResponse> reminders = reminderService.getRemindersByUser(user);
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching reminders: " + e.getMessage());
        }
    }

    /**
     * Obtiene recordatorios próximos (dentro de los próximos N días)
     * Por defecto: 7 días
     */
    @GetMapping(value = "/upcoming", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getUpcomingReminders(
            @RequestParam(defaultValue = "7") int days,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<ReminderResponse> reminders = reminderService.getActiveUpcomingReminders(user, days);
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error fetching upcoming reminders: " + e.getMessage());
        }
    }

    /**
     * Crea un nuevo recordatorio
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> createReminder(
            @RequestBody ReminderRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ReminderResponse reminder = reminderService.createReminder(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(reminder);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error creating reminder: " + e.getMessage());
        }
    }

    /**
     * Actualiza un recordatorio existente
     */
    @PutMapping(value = "/{reminderId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> updateReminder(
            @PathVariable Long reminderId,
            @RequestBody ReminderRequest request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ReminderResponse reminder = reminderService.updateReminder(reminderId, request, user);
            return ResponseEntity.ok(reminder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error updating reminder: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error updating reminder: " + e.getMessage());
        }
    }

    /**
     * Activa o desactiva un recordatorio
     */
    @PatchMapping(value = "/{reminderId}/toggle", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> toggleReminderActive(
            @PathVariable Long reminderId,
            @RequestBody Map<String, Boolean> payload,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean active = payload.getOrDefault("active", true);
            ReminderResponse reminder = reminderService.toggleReminderActive(reminderId, active, user);
            return ResponseEntity.ok(reminder);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error toggling reminder: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error toggling reminder: " + e.getMessage());
        }
    }

    /**
     * Elimina un recordatorio
     */
    @DeleteMapping(value = "/{reminderId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> deleteReminder(
            @PathVariable Long reminderId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            reminderService.deleteReminder(reminderId, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reminder deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error deleting reminder: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error deleting reminder: " + e.getMessage());
        }
    }
}