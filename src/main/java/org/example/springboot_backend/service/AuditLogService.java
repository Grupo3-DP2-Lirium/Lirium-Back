package org.example.springboot_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.springboot_backend.dto.AuditLogResponse;
import org.example.springboot_backend.model.AuditLog;
import org.example.springboot_backend.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired(required = false)
    private HttpServletRequest request;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Transactional
    public void logAction(String action, String entityType, String entityId, 
                         String description, Map<String, Object> details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        
        AuditLog log = new AuditLog();
        log.setAdminId(authentication.getName());
        log.setAdminEmail(authentication.getName());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setIpAddress(getClientIpAddress());
        log.setTimestamp(LocalDateTime.now());
        
        if (details != null && !details.isEmpty()) {
            try {
                log.setDetails(objectMapper.writeValueAsString(details));
            } catch (Exception e) {
                log.setDetails("Error serializing details: " + e.getMessage());
            }
        }
        
        auditLogRepository.save(log);
    }
    
    @Transactional
    public void logUserAction(String action, String userId, String description) {
        logAction(action, "USER", userId, description, null);
    }
    
    @Transactional
    public void logUserAction(String action, String userId, String description, 
                             Map<String, Object> details) {
        logAction(action, "USER", userId, description, details);
    }
    
    public Page<AuditLogResponse> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable)
            .map(this::convertToResponse);
    }
    
    public Page<AuditLogResponse> getLogsByFilters(
            String adminId, 
            String action, 
            String entityType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page, 
            int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findByFilters(
            adminId, action, entityType, startDate, endDate, pageable
        ).map(this::convertToResponse);
    }
    
    private AuditLogResponse convertToResponse(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setAdminId(log.getAdminId());
        response.setAdminEmail(log.getAdminEmail());
        response.setAction(log.getAction());
        response.setEntityType(log.getEntityType());
        response.setEntityId(log.getEntityId());
        response.setDescription(log.getDescription());
        response.setIpAddress(log.getIpAddress());
        response.setTimestamp(log.getTimestamp());
        response.setDetails(log.getDetails());
        return response;
    }
    
    private String getClientIpAddress() {
        if (request == null) {
            return "unknown";
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
