package org.example.springboot_backend.controller;

import org.example.springboot_backend.dto.AuditLogResponse;
import org.example.springboot_backend.enums.AuditAction;
import org.example.springboot_backend.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Obtiene logs con filtros opcionales
     * GET /api/admin/audit-logs?page=0&size=20&action=USER_LOGIN&userEmail=test@example.com&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getLogs(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Registrar que un admin está consultando los logs
        auditLogService.log(AuditAction.ADMIN_VIEW_LOGS, "AuditLog", null, 
            "Admin consultó logs con filtros: action=" + action + ", userEmail=" + userEmail);
        
        Page<AuditLogResponse> logs = auditLogService.getLogs(action, userEmail, startDate, endDate, page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * Obtiene todas las acciones disponibles para filtrar
     */
    @GetMapping("/actions")
    public ResponseEntity<AuditAction[]> getAvailableActions() {
        return ResponseEntity.ok(AuditAction.values());
    }

    /**
     * Endpoint de prueba para crear un log manualmente
     */
    @PostMapping("/test")
    public ResponseEntity<String> createTestLog() {
        auditLogService.log(
            AuditAction.ADMIN_VIEW_LOGS,
            "Test",
            "test-123",
            "Log de prueba creado manualmente desde el endpoint de test"
        );
        return ResponseEntity.ok("Log de prueba creado exitosamente");
    }
}
