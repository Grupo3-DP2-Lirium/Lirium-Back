package org.example.springboot_backend.controller;

import org.example.springboot_backend.dto.CreateUserReportRequest;
import org.example.springboot_backend.dto.UserReportResponse;
import org.example.springboot_backend.service.UserReportService;
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
@RequestMapping("/api/user-reports")
@CrossOrigin(origins = "*")
public class UserReportController {

    @Autowired
    private UserReportService userReportService;

    @PostMapping
    public ResponseEntity<?> createReport(
            @Valid @RequestBody CreateUserReportRequest request,
            Authentication authentication) {
        try {
            String reporterUserId = authentication.getName();
            UserReportResponse report = userReportService.createReport(reporterUserId, request);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al crear el reporte: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<UserReportResponse>> getAllReports() {
        try {
            List<UserReportResponse> reports = userReportService.getAllReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<UserReportResponse>> getAllReportsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<UserReportResponse> reports = userReportService.getAllReportsPaginated(page, size);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<UserReportResponse>> getPendingReports() {
        try {
            List<UserReportResponse> reports = userReportService.getPendingReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserReportResponse>> getReportsByStatus(@PathVariable String status) {
        try {
            List<UserReportResponse> reports = userReportService.getReportsByStatus(status);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserReportResponse> getReportById(@PathVariable Long id) {
        try {
            return userReportService.getReportById(id)
                    .map(report -> ResponseEntity.ok(report))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReportStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String status = request.get("status");
            String adminNotes = request.get("adminNotes");
            String resolvedBy = authentication.getName();

            UserReportResponse updatedReport = userReportService.updateReportStatus(id, status, adminNotes, resolvedBy);
            return ResponseEntity.ok(updatedReport);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al actualizar el reporte: " + e.getMessage()));
        }
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<UserReportResponse>> getMyReports(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<UserReportResponse> reports = userReportService.getReportsByUser(userId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getReportsStats() {
        try {
            Map<String, Long> stats = Map.of(
                "pending", userReportService.getReportsCountByStatus("PENDING"),
                "resolved", userReportService.getReportsCountByStatus("RESOLVED"),
                "dismissed", userReportService.getReportsCountByStatus("DISMISSED")
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        try {
            userReportService.deleteReport(id);
            return ResponseEntity.ok(Map.of("message", "Reporte eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al eliminar el reporte: " + e.getMessage()));
        }
    }
}