package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.CreateDocumentaryRequest;
import org.example.springboot_backend.dto.DocumentaryResponse;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.enums.DocumentaryStatus;
import org.example.springboot_backend.repository.DocumentaryRepository;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.MemoryRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentaryService {

    @Autowired
    private DocumentaryRepository documentaryRepository;

    @Autowired
    private MemorialRepository memorialRepository;

    @Autowired
    private MemoryRepository memoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentaryProcessingService documentaryProcessingService;

    /**
     * Crea un nuevo documental y lo pone en cola para procesamiento
     */
    @Transactional
    public DocumentaryResponse createDocumentary(CreateDocumentaryRequest request, UUID userId) {
        // Validar memorial
        Memorial memorial = memorialRepository.findById(request.getMemorialId())
                .orElseThrow(() -> new RuntimeException("Memorial not found"));

        // Validar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validar que el usuario tenga acceso al memorial
        if (!memorial.getUser().getIdUser().equals(userId) && !memorial.isCollaborative()) {
            throw new RuntimeException("You don't have permission to create a documentary for this memorial");
        }

        // Obtener recuerdos de la línea de tiempo
        Page<Memory> memoryPage = memoryRepository.findTimelineMemories(
                request.getMemorialId(),
                Pageable.unpaged()  // Esto trae TODOS los resultados sin paginación
        );
        List<Memory> memories = memoryPage.getContent();

        // Filtrar recuerdos excluidos si los hay
        if (request.getExcludedMemoryIds() != null && !request.getExcludedMemoryIds().isEmpty()) {
            memories = memories.stream()
                    .filter(m -> !request.getExcludedMemoryIds().contains(m.getIdMemory()))
                    .collect(Collectors.toList());
        }

        // Validar que haya al menos 3 recuerdos con archivos
        List<Memory> memoriesWithFiles = memories.stream()
                .filter(Memory::hasFiles)
                .collect(Collectors.toList());

        if (memoriesWithFiles.size() < 3) {
            throw new RuntimeException("Need at least 3 memories with media to create a documentary");
        }

        // Limitar a máximo 50 recuerdos
        if (memoriesWithFiles.size() > 50) {
            memoriesWithFiles = memoriesWithFiles.subList(0, 50);
        }

        // Crear entidad Documentary
        Documentary documentary = new Documentary();
        documentary.setMemorial(memorial);
        documentary.setCreatedBy(user);
        documentary.setTitle(request.getTitle() != null ? request.getTitle() :
                "Documental de " + memorial.getName());
        documentary.setDescription(request.getDescription());
        documentary.setDurationPerMemory(request.getDurationPerMemory());
        documentary.setMusicTrack(request.getMusicTrack());
        documentary.setStyleFilter(request.getStyleFilter());
        documentary.setTransitionType(request.getTransitionType());
        documentary.setResolution(request.getResolution());
        documentary.setStatus(DocumentaryStatus.PENDING);
        documentary.setProgress(0);
        documentary.setTotalMemories(memoriesWithFiles.size());

        // Guardar IDs de las memorias incluidas
        String memoryIds = memoriesWithFiles.stream()
                .map(m -> m.getIdMemory().toString())
                .collect(Collectors.joining(","));
        documentary.setMemoryIds(memoryIds);

        // Guardar en BD
        documentary = documentaryRepository.save(documentary);

        // Iniciar procesamiento asíncrono
        documentaryProcessingService.processDocumentary(documentary.getIdDocumentary());

        return mapToResponse(documentary);
    }

    /**
     * Obtener el estado de un documental
     */
    public DocumentaryResponse getDocumentaryStatus(UUID documentaryId) {
        Documentary documentary = documentaryRepository.findById(documentaryId)
                .orElseThrow(() -> new RuntimeException("Documentary not found"));

        return mapToResponse(documentary);
    }

    /**
     * Obtener todos los documentales de un memorial
     */
    public List<DocumentaryResponse> getDocumentariesByMemorial(UUID memorialId) {
        List<Documentary> documentaries = documentaryRepository.findByMemorial_IdMemorial(memorialId);
        return documentaries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los documentales creados por un usuario
     */
    public List<DocumentaryResponse> getDocumentariesByUser(UUID userId) {
        List<Documentary> documentaries = documentaryRepository.findByCreatedBy_IdUser(userId);
        return documentaries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancelar un documental en proceso
     */
    @Transactional
    public void cancelDocumentary(UUID documentaryId, UUID userId) {
        Documentary documentary = documentaryRepository.findById(documentaryId)
                .orElseThrow(() -> new RuntimeException("Documentary not found"));

        // Validar permisos
        if (!documentary.getCreatedBy().getIdUser().equals(userId)) {
            throw new RuntimeException("You don't have permission to cancel this documentary");
        }

        // Solo se puede cancelar si está pendiente o procesando
        if (documentary.getStatus() != DocumentaryStatus.PENDING &&
                documentary.getStatus() != DocumentaryStatus.PROCESSING) {
            throw new RuntimeException("Documentary cannot be cancelled in current state");
        }

        documentary.setStatus(DocumentaryStatus.CANCELLED);
        documentary.setUpdatedDate(LocalDateTime.now());
        documentaryRepository.save(documentary);
    }

    /**
     * Eliminar un documental
     */
    @Transactional
    public void deleteDocumentary(UUID documentaryId, UUID userId) {
        Documentary documentary = documentaryRepository.findById(documentaryId)
                .orElseThrow(() -> new RuntimeException("Documentary not found"));

        // Validar permisos
        if (!documentary.getCreatedBy().getIdUser().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this documentary");
        }

        // TODO: Eliminar video de Azure Blob Storage si existe
        if (documentary.getVideoUrl() != null) {
            // Implementar eliminación del video
        }

        documentaryRepository.delete(documentary);
    }

    /**
     * Mapear entidad a DTO de respuesta
     */
    private DocumentaryResponse mapToResponse(Documentary documentary) {
        DocumentaryResponse response = new DocumentaryResponse();
        response.setIdDocumentary(documentary.getIdDocumentary());
        response.setMemorialId(documentary.getMemorial().getIdMemorial());
        response.setMemorialName(documentary.getMemorial().getName());
        response.setTitle(documentary.getTitle());
        response.setDescription(documentary.getDescription());
        response.setStatus(documentary.getStatus());
        response.setProgress(documentary.getProgress());
        response.setVideoUrl(documentary.getVideoUrl());
        response.setVideoSize(documentary.getVideoSize());
        response.setVideoDuration(documentary.getVideoDuration());
        response.setTotalMemories(documentary.getTotalMemories());
        response.setErrorMessage(documentary.getErrorMessage());
        response.setCreatedDate(documentary.getCreatedDate());
        response.setProcessingCompleted(documentary.getProcessingCompleted());
        return response;
    }
}