package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot_backend.dto.PublicMemorialDto;
import org.example.springboot_backend.dto.ShareLinkResponse;
import org.example.springboot_backend.service.MemorialShareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Memorial Share", description = "Endpoints para compartir memoriales públicamente")
public class MemorialShareController {
    
    private final MemorialShareService memorialShareService;
    
    public MemorialShareController(MemorialShareService memorialShareService) {
        this.memorialShareService = memorialShareService;
    }
    
    /**
     * Crea un enlace compartible para un memorial
     * POST /api/memorials/{id}/share
     */
    @PostMapping("/memorials/{id}/share")
    @Operation(summary = "Crear enlace compartible", 
               description = "Genera un enlace público único para compartir un memorial")
    public ResponseEntity<ShareLinkResponse> createShareLink(@PathVariable("id") UUID memorialId) {
        try {
            ShareLinkResponse response = memorialShareService.createShareLink(memorialId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Obtiene la información pública de un memorial compartido
     * GET /api/public/m/{slug}
     */
    @GetMapping("/public/m/{slug}")
    @Operation(summary = "Ver memorial compartido", 
               description = "Obtiene la información pública de un memorial usando su slug compartido (sin autenticación)")
    public ResponseEntity<PublicMemorialDto> getPublicMemorial(@PathVariable("slug") String slug) {
        try {
            PublicMemorialDto memorial = memorialShareService.getPublicMemorialBySlug(slug);
            return ResponseEntity.ok(memorial);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
