package org.example.springboot_backend.controller;

import org.example.springboot_backend.entity.AuditLog;
import org.example.springboot_backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@CrossOrigin(origins = "*")
@Tag(name = "Admin - Audit Logs", description = "Endpoints para gestión de logs de auditoría")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Obtener logs de auditoría", description = "Obtiene una lista paginada de logs con filtros opcionales")
    public ResponseEntity<Page<AuditLog>> getLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> logs = auditLogService.getLogs(action, userEmail, startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/actions")
    @Operation(summary = "Obtener acciones disponibles", description = "Obtiene la lista de todas las acciones registradas en los logs")
    public ResponseEntity<List<String>> getAvailableActions() {
        List<String> actions = auditLogService.getAvailableActions();
        return ResponseEntity.ok(actions);
    }
}
