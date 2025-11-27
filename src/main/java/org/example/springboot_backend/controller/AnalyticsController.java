package org.example.springboot_backend.controller;

import org.example.springboot_backend.service.GoogleAnalyticsService;
import org.example.springboot_backend.service.GoogleAnalyticsService.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalyticsController {
    
    @Autowired
    private GoogleAnalyticsService analyticsService;
    
    // ==================== ENDPOINTS PRINCIPALES CON FILTRO ====================
    
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas generales con filtro de tiempo")
    public ResponseEntity<AnalyticsStats> getAnalyticsStats(
            @Parameter(description = "Número de días a consultar (7, 30, 90)")
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(analyticsService.getAnalyticsStats(days));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/daily-users")
    @Operation(summary = "Usuarios por día con filtro de tiempo")
    public ResponseEntity<List<DailyMetric>> getDailyUsers(
            @Parameter(description = "Número de días a consultar")
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(analyticsService.getDailyUsers(days));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/devices")
    @Operation(summary = "Estadísticas por dispositivo")
    public ResponseEntity<List<DeviceMetric>> getDeviceStats(
            @Parameter(description = "Número de días a consultar")
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(analyticsService.getDeviceStats(days));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/locations")
    @Operation(summary = "Ubicaciones principales (sin 'not set')")
    public ResponseEntity<List<LocationMetric>> getTopLocations(
            @Parameter(description = "Número de días a consultar")
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(analyticsService.getTopLocations(days, limit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/traffic-hours")
    @Operation(summary = "Tráfico por hora del día")
    public ResponseEntity<List<HourlyMetric>> getTrafficByHour(
            @Parameter(description = "Número de días a consultar")
            @RequestParam(defaultValue = "7") int days) {
        try {
            return ResponseEntity.ok(analyticsService.getTrafficByHour(days));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/events")
    @Operation(summary = "Eventos principales (filtrados, sin eventos técnicos)")
    public ResponseEntity<List<EventMetric>> getTopEvents(
            @Parameter(description = "Número de días a consultar")
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(analyticsService.getTopEvents(days, limit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/retention")
    @Operation(summary = "Retención de usuarios (usuarios que regresan)")
    public ResponseEntity<RetentionMetric> getUserRetention(
            @Parameter(description = "Número de días a consultar")
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(analyticsService.getUserRetention(days));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/engagement-weekday")
    @Operation(summary = "Engagement por día de la semana")
    public ResponseEntity<List<WeekdayMetric>> getEngagementByWeekday(
            @Parameter(description = "Número de días a consultar")
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(analyticsService.getEngagementByWeekday(days));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}