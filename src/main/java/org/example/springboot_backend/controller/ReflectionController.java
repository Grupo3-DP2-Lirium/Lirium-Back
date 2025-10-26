package org.example.springboot_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot_backend.dto.MemoryCreateRequest;
import org.example.springboot_backend.dto.MemoryResponse;
import org.example.springboot_backend.entity.Memory;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.MemoryOriginType;
import org.example.springboot_backend.repository.MemoryRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.IMemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reflections")
@CrossOrigin(origins = "*")
@Tag(name = "Reflections", description = "API para gestión de reflexiones personales")
public class ReflectionController {

    @Autowired
    private IMemoryService memoryService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MemoryRepository memoryRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Crear una nueva reflexión personal",
        description = "Permite al usuario crear una reflexión personal con texto, imágenes, videos o audios. " +
                     "La reflexión se almacena en un espacio personal privado que se crea automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reflexión creada exitosamente",
                    content = @Content(schema = @Schema(implementation = MemoryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "413", description = "Archivo demasiado grande o sin espacio suficiente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> createReflection(
            @Parameter(description = "Datos de la reflexión en formato JSON", required = true)
            @RequestPart("reflection") String reflectionJson,
            
            @Parameter(description = "Archivos multimedia opcionales (imágenes, videos, audios)")
            @RequestParam(value = "files", required = false) List<MultipartFile> filesList,
            
            Authentication authentication) {
        
        try {
            // Parsear el JSON de la reflexión
            ObjectMapper objectMapper = new ObjectMapper();
            MemoryCreateRequest request = objectMapper.readValue(reflectionJson, MemoryCreateRequest.class);
            
            // Forzar el tipo como REFLECTION
            request.setType(MemoryOriginType.REFLECTION);
            
            // Obtener el usuario autenticado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Convertir lista de archivos a array
            MultipartFile[] files = filesList != null ? 
                filesList.toArray(new MultipartFile[0]) : new MultipartFile[0];
            
            // Crear la reflexión
            MemoryResponse response = memoryService.createReflection(request, files, user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reflexión creada exitosamente",
                "data", response
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error creando reflexión: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error creando reflexión: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping
    @Operation(
        summary = "Obtener reflexiones del usuario",
        description = "Recupera todas las reflexiones personales del usuario autenticado, " +
                     "paginadas y ordenadas por fecha de creación descendente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reflexiones recuperadas exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getUserReflections(
            @Parameter(description = "Número de página (basado en 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            
            Authentication authentication) {
        
        try {
            // Obtener el usuario autenticado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            // Obtener las reflexiones del usuario
            Page<MemoryResponse> reflections = memoryService.listUserReflections(user, page, size);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reflexiones recuperadas exitosamente",
                "data", Map.of(
                    "content", reflections.getContent(),
                    "totalElements", reflections.getTotalElements(),
                    "totalPages", reflections.getTotalPages(),
                    "currentPage", reflections.getNumber(),
                    "size", reflections.getSize(),
                    "first", reflections.isFirst(),
                    "last", reflections.isLast(),
                    "hasNext", reflections.hasNext(),
                    "hasPrevious", reflections.hasPrevious()
                )
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo reflexiones: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error obteniendo reflexiones: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/stats")
    @Operation(
        summary = "Obtener estadísticas de reflexiones",
        description = "Obtiene estadísticas básicas sobre las reflexiones del usuario."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas recuperadas exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getReflectionStats(Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            // Obtener primera página para estadísticas básicas
            Page<MemoryResponse> reflections = memoryService.listUserReflections(user, 0, 1);
            
            long totalReflections = reflections.getTotalElements();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Estadísticas recuperadas exitosamente",
                "data", Map.of(
                    "totalReflections", totalReflections,
                    "hasReflections", totalReflections > 0
                )
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo estadísticas: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error obteniendo estadísticas: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }

    @DeleteMapping("/{reflectionId}")
    @Operation(
        summary = "Eliminar una reflexión personal",
        description = "Elimina una reflexión específica y todos sus archivos asociados. " +
                     "Solo el autor de la reflexión puede eliminarla."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reflexión eliminada exitosamente"),
        @ApiResponse(responseCode = "400", description = "ID inválido o reflexión no encontrada"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para eliminar esta reflexión"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> deleteReflection(
            @Parameter(description = "ID de la reflexión a eliminar", required = true)
            @PathVariable UUID reflectionId,
            
            Authentication authentication) {
        
        try {
            // Obtener el usuario autenticado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Eliminar la reflexión
            memoryService.deleteReflection(reflectionId, user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reflexión eliminada exitosamente",
                "reflectionId", reflectionId
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error eliminando reflexión: " + e.getMessage());
            
            String errorMessage = e.getMessage();
            int statusCode = 400;
            
            if (errorMessage.contains("not found")) {
                statusCode = 404;
            } else if (errorMessage.contains("not authorized") || errorMessage.contains("not belong")) {
                statusCode = 403;
            }
            
            return ResponseEntity.status(statusCode).body(Map.of(
                "success", false,
                "message", "Error eliminando reflexión: " + errorMessage,
                "error", e.getClass().getSimpleName(),
                "reflectionId", reflectionId
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error inesperado eliminando reflexión: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor",
                "error", e.getClass().getSimpleName(),
                "reflectionId", reflectionId
            ));
        }
    }

    @PutMapping(value = "/{reflectionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Editar una reflexión personal",
        description = "Permite editar una reflexión existente, incluyendo título, descripción, " +
                     "agregar nuevos archivos multimedia y eliminar archivos existentes. " +
                     "Solo el autor de la reflexión puede editarla."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reflexión editada exitosamente",
                    content = @Content(schema = @Schema(implementation = MemoryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para editar esta reflexión"),
        @ApiResponse(responseCode = "404", description = "Reflexión no encontrada"),
        @ApiResponse(responseCode = "413", description = "Archivo demasiado grande o sin espacio suficiente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> updateReflection(
            @Parameter(description = "ID de la reflexión a editar", required = true)
            @PathVariable UUID reflectionId,
            
            @Parameter(description = "Datos actualizados de la reflexión en formato JSON", required = true)
            @RequestPart("reflection") String reflectionJson,
            
            @Parameter(description = "Nuevos archivos multimedia a agregar (opcionales)")
            @RequestParam(value = "files", required = false) List<MultipartFile> filesList,
            
            @Parameter(description = "Lista de IDs de archivos a eliminar (opcionales)")
            @RequestParam(value = "filesToDelete", required = false) List<String> filesToDeleteIds,
            
            Authentication authentication) {
        
        try {
            // Parsear el JSON de la reflexión
            ObjectMapper objectMapper = new ObjectMapper();
            MemoryCreateRequest request = objectMapper.readValue(reflectionJson, MemoryCreateRequest.class);
            
            // Asegurar que el tipo sea REFLECTION
            request.setType(MemoryOriginType.REFLECTION);
            
            // Obtener el usuario autenticado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Convertir lista de archivos a array
            MultipartFile[] files = filesList != null ? 
                filesList.toArray(new MultipartFile[0]) : new MultipartFile[0];
            
            // Preparar lista de archivos a eliminar
            List<org.example.springboot_backend.dto.FileDeleteRequest> filesToDelete = null;
            if (filesToDeleteIds != null && !filesToDeleteIds.isEmpty()) {
                filesToDelete = filesToDeleteIds.stream()
                    .map(id -> {
                        org.example.springboot_backend.dto.FileDeleteRequest deleteRequest = 
                            new org.example.springboot_backend.dto.FileDeleteRequest();
                        deleteRequest.setId(id);
                        return deleteRequest;
                    })
                    .toList();
            }
            
            // Actualizar la reflexión
            MemoryResponse response = memoryService.updateReflection(reflectionId, request, files, filesToDelete, user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reflexión editada exitosamente",
                "data", response
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error editando reflexión: " + e.getMessage());
            
            String errorMessage = e.getMessage();
            int statusCode = 400;
            
            if (errorMessage.contains("not found")) {
                statusCode = 404;
            } else if (errorMessage.contains("not authorized") || errorMessage.contains("not belong")) {
                statusCode = 403;
            } else if (errorMessage.contains("storage") || errorMessage.contains("space")) {
                statusCode = 413;
            }
            
            return ResponseEntity.status(statusCode).body(Map.of(
                "success", false,
                "message", "Error editando reflexión: " + errorMessage,
                "error", e.getClass().getSimpleName(),
                "reflectionId", reflectionId
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error inesperado editando reflexión: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor",
                "error", e.getClass().getSimpleName(),
                "reflectionId", reflectionId
            ));
        }
    }

    @GetMapping("/{reflectionId}")
    @Operation(
        summary = "Obtener una reflexión específica",
        description = "Recupera los detalles completos de una reflexión específica. " +
                     "Solo el autor puede acceder a sus reflexiones."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reflexión recuperada exitosamente",
                    content = @Content(schema = @Schema(implementation = MemoryResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para ver esta reflexión"),
        @ApiResponse(responseCode = "404", description = "Reflexión no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getReflection(
            @Parameter(description = "ID de la reflexión a obtener", required = true)
            @PathVariable UUID reflectionId,
            
            Authentication authentication) {
        
        try {
            // Obtener el usuario autenticado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Obtener la reflexión usando el repositorio
            Memory reflection = memoryRepository.findById(reflectionId)
                .orElseThrow(() -> new RuntimeException("Reflection not found"));
            
            // Verificar que sea una reflexión
            if (reflection.getType() != MemoryOriginType.REFLECTION) {
                throw new RuntimeException("Memory is not a reflection");
            }
            
            // Verificar que el usuario sea el autor
            if (!reflection.getAuthor().getIdUser().equals(user.getIdUser())) {
                throw new RuntimeException("User is not authorized to view this reflection");
            }
            
            // Construir respuesta usando el método privado del servicio
            // Como no podemos acceder al método privado, vamos a construir la respuesta aquí
            MemoryResponse response = new MemoryResponse();
            response.setIdMemory(reflection.getIdMemory());
            response.setType(reflection.getType());
            response.setTitle(reflection.getTitle());
            response.setDescription(reflection.getDescription());
            response.setPhotoDate(reflection.getPhotoDate());
            response.setLocation(reflection.getLocation());
            response.setVisible(reflection.isVisible());
            response.setTags(reflection.getTags());
            response.setAssociatedQuestion(reflection.getAssociatedQuestion());
            response.setTotalUsedSpace(reflection.getTotalUsedSpace() != null ? reflection.getTotalUsedSpace() / (1024 * 1024) : 0.0);
            response.setCreatedDate(reflection.getCreatedDate());
            response.setEsLineaTiempo(reflection.getEsLineaTiempo() != null ? reflection.getEsLineaTiempo() : false);
            response.setLatitude(reflection.getLatitude());
            response.setLongitude(reflection.getLongitude());

            if (reflection.getFiles() != null && !reflection.getFiles().isEmpty()) {
                List<org.example.springboot_backend.dto.FileResponse> fileResponses = reflection.getFiles().stream()
                    .map(file -> {
                        org.example.springboot_backend.dto.FileResponse fileResponse = new org.example.springboot_backend.dto.FileResponse();
                        fileResponse.setIdFile(file.getIdFile());
                        fileResponse.setFileName(file.getFileName());
                        fileResponse.setOriginalFileName(file.getOriginalFileName());
                        fileResponse.setFileType(file.getFileType());
                        fileResponse.setMimeType(file.getMimeType());
                        fileResponse.setFileUrl(file.getFileUrl());
                        fileResponse.setFileSize(file.getFileSize() != null ? file.getFileSize() / (1024 * 1024) : 0.0);
                        fileResponse.setUploadedDate(file.getUploadedDate());
                        return fileResponse;
                    })
                    .toList();
                response.setFiles(fileResponses);
            } else {
                response.setFiles(List.of());
            }

            response.setCategorias(
                reflection.getCategorias() == null ? List.of()
                    : reflection.getCategorias().stream().map(Enum::name).map(String::toLowerCase).toList()
            );
            response.setMomentos(
                reflection.getMomentos() == null ? List.of()
                    : reflection.getMomentos().stream().map(Enum::name).map(String::toLowerCase).toList()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reflexión recuperada exitosamente",
                "data", response
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error obteniendo reflexión: " + e.getMessage());
            
            String errorMessage = e.getMessage();
            int statusCode = 400;
            
            if (errorMessage.contains("not found")) {
                statusCode = 404;
            } else if (errorMessage.contains("not authorized")) {
                statusCode = 403;
            }
            
            return ResponseEntity.status(statusCode).body(Map.of(
                "success", false,
                "message", "Error obteniendo reflexión: " + errorMessage,
                "error", e.getClass().getSimpleName(),
                "reflectionId", reflectionId
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error inesperado obteniendo reflexión: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error interno del servidor",
                "error", e.getClass().getSimpleName(),
                "reflectionId", reflectionId
            ));
        }
    }
}