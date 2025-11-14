package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.AuditLog;
import org.example.springboot_backend.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(String action, String userEmail, String details) {
        AuditLog auditLog = new AuditLog(action, userEmail, details);
        auditLogRepository.save(auditLog);
    }

    public void log(String action, String userEmail, String details, String ipAddress) {
        AuditLog auditLog = new AuditLog(action, userEmail, details);
        auditLog.setIpAddress(ipAddress);
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> getLogs(String action, String userEmail, 
                                   LocalDateTime startDate, LocalDateTime endDate, 
                                   Pageable pageable) {
        return auditLogRepository.findByFilters(action, userEmail, startDate, endDate, pageable);
    }

    public List<String> getAvailableActions() {
        return auditLogRepository.findDistinctActions();
    }
}
