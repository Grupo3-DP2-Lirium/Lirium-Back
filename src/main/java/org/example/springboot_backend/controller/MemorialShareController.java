package org.example.springboot_backend.controller;

import org.example.springboot_backend.dto.PublicMemorialDto;
import org.example.springboot_backend.dto.ShareLinkResponse;
import org.example.springboot_backend.service.MemorialShareService;
import org.example.springboot_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MemorialShareController {
    
    private final MemorialShareService memorialShareService;
    
    @Autowired
    private NotificationService notificationService;
    
    public MemorialShareController(MemorialShareService memorialShareService) {
        this.memorialShareService = memorialShareService;
    }
    
    @PostMapping("/memorials/{id}/share")
    public ResponseEntity<ShareLinkResponse> createShareLink(@PathVariable("id") UUID memorialId) {
        try {
            ShareLinkResponse response = memorialShareService.createShareLink(memorialId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/public/m/{slug}")
    public ResponseEntity<PublicMemorialDto> getPublicMemorial(@PathVariable("slug") String slug) {
        try {
            PublicMemorialDto memorial = memorialShareService.getPublicMemorialBySlug(slug);
            return ResponseEntity.ok(memorial);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
