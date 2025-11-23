package org.example.springboot_backend.controller;

import org.example.springboot_backend.service.GoogleAnalyticsService;
import org.example.springboot_backend.service.GoogleAnalyticsService.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
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
    
    // ==================== ENDPOINTS EXISTENTES ====================
    
    @GetMapping("/stats")
    @Operation(summary = "Estadísticas generales")
    public ResponseEntity<AnalyticsStats> getAnalyticsStats() {
        try {
            return ResponseEntity.ok(analyticsService.getAnalyticsStats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/daily-users")
    @Operation(summary = "Usuarios por día")
    public ResponseEntity<List<DailyMetric>> getDailyUsers(
            @RequestParam(defaultValue = "30") int days) {
        try {
            return ResponseEntity.ok(analyticsService.getDailyUsers(days));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/top-pages")
    @Operation(summary = "Páginas más visitadas")
    public ResponseEntity<List<PageMetric>> getTopPages(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(analyticsService.getTopPages(limit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/devices")
    @Operation(summary = "Estadísticas por dispositivo")
    public ResponseEntity<List<DeviceMetric>> getDeviceStats() {
        try {
            return ResponseEntity.ok(analyticsService.getDeviceStats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/locations")
    @Operation(summary = "Ubicaciones principales")
    public ResponseEntity<List<LocationMetric>> getTopLocations(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(analyticsService.getTopLocations(limit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ==================== NUEVOS ENDPOINTS ====================
    
    @GetMapping("/traffic-hours")
    @Operation(summary = "Tráfico por hora del día")
    public ResponseEntity<List<HourlyMetric>> getTrafficByHour() {
        try {
            return ResponseEntity.ok(analyticsService.getTrafficByHour());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/events")
    @Operation(summary = "Eventos principales")
    public ResponseEntity<List<EventMetric>> getTopEvents(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(analyticsService.getTopEvents(limit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/sources")
    @Operation(summary = "Fuentes de tráfico")
    public ResponseEntity<List<SourceMetric>> getTrafficSources(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(analyticsService.getTrafficSources(limit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/browsers")
    @Operation(summary = "Navegadores más usados")
    public ResponseEntity<List<BrowserMetric>> getTopBrowsers(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(analyticsService.getTopBrowsers(limit));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}