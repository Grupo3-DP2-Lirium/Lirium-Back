package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.CreateUserReportRequest;
import org.example.springboot_backend.dto.UserReportResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.model.UserReport;
import org.example.springboot_backend.repository.UserReportRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserReportService {

    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private UserRepository userRepository;

    public UserReportResponse createReport(String reporterUserId, CreateUserReportRequest request) {
        UserReport report = new UserReport(
            reporterUserId,
            request.getReportedUserId(),
            request.getReason(),
            request.getDescription(),
            request.getContentType(),
            request.getContentId()
        );

        UserReport savedReport = userReportRepository.save(report);
        return convertToResponse(savedReport);
    }

    public List<UserReportResponse> getAllReports() {
        List<UserReport> reports = userReportRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 1000)).getContent();
        return reports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Page<UserReportResponse> getAllReportsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserReport> reports = userReportRepository.findAllByOrderByCreatedAtDesc(pageable);
        return reports.map(this::convertToResponse);
    }

    public List<UserReportResponse> getReportsByStatus(String status) {
        List<UserReport> reports = userReportRepository.findByStatus(status);
        return reports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<UserReportResponse> getPendingReports() {
        List<UserReport> reports = userReportRepository.findPendingReports();
        return reports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Optional<UserReportResponse> getReportById(Long id) {
        return userReportRepository.findById(id)
                .map(this::convertToResponse);
    }

    public UserReportResponse updateReportStatus(Long id, String status, String adminNotes, String resolvedBy) {
        UserReport report = userReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

        report.setStatus(status);
        if (adminNotes != null) {
            report.setAdminNotes(adminNotes);
        }
        if (resolvedBy != null) {
            report.setResolvedBy(resolvedBy);
        }
        if ("RESOLVED".equals(status) || "DISMISSED".equals(status)) {
            report.setResolvedAt(LocalDateTime.now());
        }

        UserReport updatedReport = userReportRepository.save(report);
        return convertToResponse(updatedReport);
    }

    public List<UserReportResponse> getReportsByUser(String userId) {
        List<UserReport> reports = userReportRepository.findByReporterUserId(userId);
        return reports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public long getReportsCountByStatus(String status) {
        return userReportRepository.countByStatus(status);
    }

    public void deleteReport(Long id) {
        userReportRepository.deleteById(id);
    }

    private UserReportResponse convertToResponse(UserReport report) {
        UserReportResponse response = new UserReportResponse(report);
        
        // Obtener información del usuario que reporta
        try {
            UUID reporterUUID = UUID.fromString(report.getReporterUserId());
            Optional<User> reporterUser = userRepository.findById(reporterUUID);
            if (reporterUser.isPresent()) {
                User user = reporterUser.get();
                response.setReporterUserName(user.getFirstName() + " " + user.getFirstLastName());
                response.setReporterEmail(user.getEmail());
            }
        } catch (IllegalArgumentException e) {
            // Si no es un UUID válido, usar el ID como está
            response.setReporterUserName(report.getReporterUserId());
        }
        
        // Obtener información del usuario reportado
        try {
            UUID reportedUUID = UUID.fromString(report.getReportedUserId());
            Optional<User> reportedUser = userRepository.findById(reportedUUID);
            if (reportedUser.isPresent()) {
                User user = reportedUser.get();
                response.setReportedUserName(user.getFirstName() + " " + user.getFirstLastName());
                response.setReportedEmail(user.getEmail());
            }
        } catch (IllegalArgumentException e) {
            // Si no es un UUID válido, usar el ID como está
            response.setReportedUserName(report.getReportedUserId());
        }
        
        return response;
    }
}