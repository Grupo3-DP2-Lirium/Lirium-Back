package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.example.springboot_backend.entity.File;
import org.example.springboot_backend.repository.FileRepository;
import org.example.springboot_backend.service.storage.FileStorageService;
import org.example.springboot_backend.service.storage.StorageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileRepository fileRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file to specified folder")
    public ResponseEntity<?> uploadFile(
            @RequestParam("folder") String folder,
            @RequestParam("file") MultipartFile file) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            
            StorageResult result = fileStorageService.uploadFile(file, folder);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result.getErrorMessage());
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        }
    }

    // Download
    @GetMapping("/download")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> downloadFile(
            @RequestParam String path,
            @RequestParam String name) {
        try {
            Path filePath = Paths.get(path, name);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("File not found: " + filePath.toString());
            }

            // Carga el archivo como recurso
            byte[] data = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(data);

            // Detecta MIME type
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + name + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading file: " + e.getMessage());
        }
    }

    @GetMapping("/view/{fileId}")
    public ResponseEntity<Resource> viewFile(@PathVariable UUID fileId) {
        try {
            // Buscar archivo en la base de datos
            File fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

            // Extraer folder y filename del storagePath
            String storagePath = fileEntity.getStoragePath();
            Path path = Paths.get(storagePath);
            String folder = path.getParent().toString().replace("\\", "/");
            String fileName = path.getFileName().toString();

            // Usar el servicio para descargar
            ResponseEntity<Resource> response = fileStorageService.downloadFile(folder, fileName);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileEntity.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(response.getBody());
            }
            
            return response;

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFileById(@PathVariable UUID fileId) {
        try {
            File fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

            String storagePath = fileEntity.getStoragePath();
            Path path = Paths.get(storagePath);
            String folder = path.getParent().toString().replace("\\", "/");
            String fileName = path.getFileName().toString();

            ResponseEntity<Resource> response = fileStorageService.downloadFile(folder, fileName);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileEntity.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + fileEntity.getOriginalFileName() + "\"")
                    .body(response.getBody());
            }
            
            return response;

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}