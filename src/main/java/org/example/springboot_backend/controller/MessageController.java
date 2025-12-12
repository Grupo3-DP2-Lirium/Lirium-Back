package org.example.springboot_backend.controller;

import org.example.springboot_backend.dto.CreateMessageRequest;
import org.example.springboot_backend.dto.MessageResponse;
import org.example.springboot_backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<?> createMessage(
            @Valid @RequestBody CreateMessageRequest request,
            Authentication authentication) {
        try {
            String senderUserId = authentication.getName();
            MessageResponse message = messageService.createMessage(senderUserId, request);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al crear el mensaje: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getAllMessages() {
        try {
            List<MessageResponse> messages = messageService.getAllMessages();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<MessageResponse>> getAllMessagesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<MessageResponse> messages = messageService.getAllMessagesPaginated(page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MessageResponse>> searchMessages(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<MessageResponse> messages = messageService.getMessagesWithFilters(
                status, priority, category, search, page, size);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<List<MessageResponse>> getUnreadMessages() {
        try {
            List<MessageResponse> messages = messageService.getUnreadMessages();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MessageResponse>> getMessagesByStatus(@PathVariable String status) {
        try {
            List<MessageResponse> messages = messageService.getMessagesByStatus(status);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable Long id) {
        try {
            return messageService.getMessageById(id)
                    .map(message -> ResponseEntity.ok(message))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String adminUserId = authentication.getName();
            MessageResponse message = messageService.markAsRead(id, adminUserId);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al marcar como leído: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/reply")
    public ResponseEntity<?> replyToMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String adminUserId = authentication.getName();
            String response = request.get("response");
            
            if (response == null || response.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "La respuesta es requerida"));
            }

            MessageResponse message = messageService.replyToMessage(id, adminUserId, response);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al responder mensaje: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateMessageStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String adminUserId = authentication.getName();
            String status = request.get("status");

            MessageResponse message = messageService.updateMessageStatus(id, status, adminUserId);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al actualizar estado: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getMessageStats() {
        try {
            Map<String, Long> stats = messageService.getMessageStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my-messages")
    public ResponseEntity<List<MessageResponse>> getMyMessages(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<MessageResponse> messages = messageService.getMessagesByUser(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        try {
            messageService.deleteMessage(id);
            return ResponseEntity.ok(Map.of("message", "Mensaje eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al eliminar mensaje: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        try {
            long count = messageService.getMessageCount();
            return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Endpoint funcionando correctamente",
                "messageCount", count,
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "status", "ERROR",
                        "message", "Error en el endpoint: " + e.getMessage(),
                        "error", e.getClass().getSimpleName()
                    ));
        }
    }

    @PostMapping("/create-sample")
    public ResponseEntity<?> createSampleMessages() {
        try {
            messageService.createSampleMessages();
            return ResponseEntity.ok(Map.of("message", "Mensajes de ejemplo creados exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al crear mensajes de ejemplo: " + e.getMessage()));
        }
    }
}