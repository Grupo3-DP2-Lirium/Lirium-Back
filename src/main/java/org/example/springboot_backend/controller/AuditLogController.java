package org.example.springboot_backend.controller;

import org.example.springboot_backend.dto.AuditLogResponse;
import org.example.springboot_backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {
    
    @Autowired
    private AuditLogService auditLogService;
    
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<AuditLogResponse> logs = auditLogService.getAllLogs(page, size);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/filter")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByFilters(
            @RequestParam(required = false) String adminId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<AuditLogResponse> logs = auditLogService.getLogsByFilters(
            adminId, action, entityType, startDate, endDate, page, size
        );
        return ResponseEntity.ok(logs);
    }
}
