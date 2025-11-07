package org.example.springboot_backend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.springboot_backend.dto.AuditLogResponse;
import org.example.springboot_backend.entity.AuditLog;
import org.example.springboot_backend.enums.AuditAction;
import org.example.springboot_backend.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Registra una acción de auditoría
     */
    public void log(AuditAction action, String entityType, String entityId, String details) {
        log(action, entityType, entityId, details, true, null);
    }

    /**
     * Registra una acción de auditoría con estado de éxito/error
     */
    public void log(AuditAction action, String entityType, String entityId, String details, Boolean success, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setDetails(details);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);

            // Obtener información del usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                auditLog.setUserEmail(authentication.getName());
            }

            // Obtener IP del request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIp(request));
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // No lanzar excepción para no interrumpir el flujo principal
            System.err.println("Error al guardar audit log: " + e.getMessage());
        }
    }

    /**
     * Obtiene logs con filtros y paginación
     */
    public Page<AuditLogResponse> getLogs(
            AuditAction action,
            String userEmail,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogRepository.findByFilters(action, userEmail, startDate, endDate, pageable);
        return logs.map(this::toResponse);
    }

    /**
     * Obtiene todos los logs con paginación
     */
    public Page<AuditLogResponse> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return logs.map(this::toResponse);
    }

    /**
     * Convierte entidad a DTO
     */
    private AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setIdAuditLog(log.getIdAuditLog());
        response.setAction(log.getAction());
        response.setUserEmail(log.getUserEmail());
        response.setUserId(log.getUserId());
        response.setIpAddress(log.getIpAddress());
        response.setEntityType(log.getEntityType());
        response.setEntityId(log.getEntityId());
        response.setDetails(log.getDetails());
        response.setCreatedAt(log.getCreatedAt());
        response.setSuccess(log.getSuccess());
        response.setErrorMessage(log.getErrorMessage());
        return response;
    }

    /**
     * Obtiene la IP real del cliente
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
