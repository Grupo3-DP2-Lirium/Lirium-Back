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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
     * Valida si un memorial tiene suficientes recuerdos para crear documental
     */
    public Map<String, Object> validateMemorialForDocumentary(UUID memorialId) {
        Memorial memorial = memorialRepository.findById(memorialId)
                .orElseThrow(() -> new RuntimeException("Memorial not found"));

        Page<Memory> memoryPage = memoryRepository.findTimelineMemories(memorialId, Pageable.unpaged());
        List<Memory> timelineMemories = memoryPage.getContent();

        // Contar solo recuerdos con archivos (imágenes/videos)
        long memoriesWithFiles = timelineMemories.stream()
                .filter(Memory::hasFiles)
                .count();

        int minimumRequired = 3;
        boolean isValid = memoriesWithFiles >= minimumRequired;

        Map<String, Object> validation = new HashMap<>();
        validation.put("isValid", isValid);
        validation.put("currentMemories", (int) memoriesWithFiles);
        validation.put("minimumRequired", minimumRequired);
        validation.put("memorialName", memorial.getName());
        validation.put("message", isValid
                ? String.format("El perfil de %s tiene %d recuerdos disponibles", memorial.getName(), (int) memoriesWithFiles)
                : String.format("Para crear un documental memorable sobre %s, necesitamos al menos %d recuerdos que nos ayuden a reconstruir su historia de manera significativa. Actualmente este perfil tiene %d recuerdos.",
                memorial.getName(), minimumRequired, (int) memoriesWithFiles)
        );

        return validation;
    }

    /**
     * Crea un documental en estado DRAFT (sin generar video todavía)
     */
    @Transactional
    public DocumentaryResponse createDocumentaryDraft(CreateDocumentaryRequest request, UUID userId) {
        // Validar memorial
        Memorial memorial = memorialRepository.findById(request.getMemorialId())
                .orElseThrow(() -> new RuntimeException("Memorial not found"));

        // Validar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validar permisos
        if (!memorial.getUser().getIdUser().equals(userId) && !memorial.isCollaborative()) {
            throw new RuntimeException("You don't have permission to create a documentary for this memorial");
        }

        // Validar que tenga suficientes recuerdos
        Map<String, Object> validation = validateMemorialForDocumentary(request.getMemorialId());
        if (!(Boolean) validation.get("isValid")) {
            throw new RuntimeException(validation.get("message").toString());
        }

        // Obtener recuerdos para calcular totalMemories
        Page<Memory> memoryPage = memoryRepository.findTimelineMemories(
                request.getMemorialId(),
                Pageable.unpaged()
        );
        List<Memory> memories = memoryPage.getContent().stream()
                .filter(Memory::hasFiles)
                .collect(Collectors.toList());

        // Filtrar excluidos si los hay
        if (request.getExcludedMemoryIds() != null && !request.getExcludedMemoryIds().isEmpty()) {
            memories = memories.stream()
                    .filter(m -> !request.getExcludedMemoryIds().contains(m.getIdMemory()))
                    .collect(Collectors.toList());
        }

        // Limitar a 50 máximo
        if (memories.size() > 50) {
            memories = memories.subList(0, 50);
        }

        // Crear entidad Documentary en estado DRAFT
        Documentary documentary = new Documentary();
        documentary.setMemorial(memorial);
        documentary.setCreatedBy(user);
        documentary.setTitle(request.getTitle() != null ? request.getTitle() :
                "Documental de " + memorial.getName());
        documentary.setDescription(request.getDescription());
        documentary.setNarrativeFocus(request.getNarrativeFocus());
        documentary.setEmotionalTone(request.getEmotionalTone());
        documentary.setDurationPerMemory(request.getDurationPerMemory());
        documentary.setMusicTrack(request.getMusicTrack());
        documentary.setStyleFilter(request.getStyleFilter());
        documentary.setTransitionType(request.getTransitionType());
        documentary.setResolution(request.getResolution());
        documentary.setStatus(DocumentaryStatus.DRAFT); // ✨ Estado inicial: DRAFT
        documentary.setProgress(0);
        documentary.setTotalMemories(memories.size());

        // Guardar IDs de memorias
        String memoryIds = memories.stream()
                .map(m -> m.getIdMemory().toString())
                .collect(Collectors.joining(","));
        documentary.setMemoryIds(memoryIds);

        // Guardar en BD
        documentary = documentaryRepository.save(documentary);

        System.out.println("✅ Documentary draft created: " + documentary.getIdDocumentary());

        return mapToResponse(documentary);
    }

    /**
     * Inicia la generación del video (cambia de DRAFT a PROCESSING)
     */
    @Transactional
    public DocumentaryResponse startDocumentaryGeneration(UUID documentaryId, UUID userId) {
        Documentary documentary = documentaryRepository.findById(documentaryId)
                .orElseThrow(() -> new RuntimeException("Documentary not found"));

        // Validar permisos
        if (!documentary.getCreatedBy().getIdUser().equals(userId)) {
            throw new RuntimeException("You don't have permission to generate this documentary");
        }

        // Validar estado
        if (documentary.getStatus() != DocumentaryStatus.DRAFT) {
            throw new RuntimeException("Documentary must be in DRAFT state to start generation");
        }

        // Cambiar estado a PROCESSING
        documentary.setStatus(DocumentaryStatus.PROCESSING);
        documentary.setProgress(0);
        documentary.setProcessingStarted(LocalDateTime.now());
        documentaryRepository.save(documentary);

        // Iniciar procesamiento asíncrono
        documentaryProcessingService.processDocumentary(documentary.getIdDocumentary());

        System.out.println("🎬 Documentary generation started: " + documentaryId);

        return mapToResponse(documentary);
    }

    /**
     * Publica un documental completado en el perfil
     */
    @Transactional
    public DocumentaryResponse publishDocumentary(UUID documentaryId, UUID userId) {
        Documentary documentary = documentaryRepository.findById(documentaryId)
                .orElseThrow(() -> new RuntimeException("Documentary not found"));

        // Validar permisos
        if (!documentary.getCreatedBy().getIdUser().equals(userId)) {
            throw new RuntimeException("You don't have permission to publish this documentary");
        }

        // Validar que esté completado
        if (documentary.getStatus() != DocumentaryStatus.COMPLETED) {
            throw new RuntimeException("Documentary must be COMPLETED before publishing");
        }

        // Cambiar estado a PUBLISHED
        documentary.setStatus(DocumentaryStatus.PUBLISHED);
        documentary.setPublishedDate(LocalDateTime.now());
        documentaryRepository.save(documentary);

        System.out.println("📢 Documentary published: " + documentaryId);

        return mapToResponse(documentary);
    }

    /**
     * Actualizar documental (solo si está en DRAFT o COMPLETED)
     */
    @Transactional
    public DocumentaryResponse updateDocumentary(UUID documentaryId, CreateDocumentaryRequest request, UUID userId) {
        Documentary documentary = documentaryRepository.findById(documentaryId)
                .orElseThrow(() -> new RuntimeException("Documentary not found"));

        // Validar permisos
        if (!documentary.getCreatedBy().getIdUser().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this documentary");
        }

        // Solo se puede editar en DRAFT o COMPLETED
        if (documentary.getStatus() != DocumentaryStatus.DRAFT &&
                documentary.getStatus() != DocumentaryStatus.COMPLETED) {
            throw new RuntimeException("Cannot edit documentary in current state");
        }

        // Actualizar campos editables
        if (request.getTitle() != null) {
            documentary.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            documentary.setDescription(request.getDescription());
        }
        if (request.getNarrativeFocus() != null) {
            documentary.setNarrativeFocus(request.getNarrativeFocus());
        }
        if (request.getEmotionalTone() != null) {
            documentary.setEmotionalTone(request.getEmotionalTone());
        }
        if (request.getMusicTrack() != null) {
            documentary.setMusicTrack(request.getMusicTrack());
        }
        if (request.getStyleFilter() != null) {
            documentary.setStyleFilter(request.getStyleFilter());
        }

        documentary.setUpdatedDate(LocalDateTime.now());
        documentaryRepository.save(documentary);

        System.out.println("✏️ Documentary updated: " + documentaryId);

        return mapToResponse(documentary);
    }

    /**
     * Obtener documentales por memorial y estado
     */
    public List<DocumentaryResponse> getDocumentariesByMemorialAndStatus(UUID memorialId, DocumentaryStatus status) {
        List<Documentary> documentaries = documentaryRepository.findByMemorial_IdMemorialAndStatus(memorialId, status);
        return documentaries.stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(DocumentaryResponse::getUpdatedDate).reversed())
                .collect(Collectors.toList());
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
                .sorted(Comparator.comparing(DocumentaryResponse::getUpdatedDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los documentales creados por un usuario
     */
    public List<DocumentaryResponse> getDocumentariesByUser(UUID userId) {
        List<Documentary> documentaries = documentaryRepository.findByCreatedBy_IdUser(userId);
        return documentaries.stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(DocumentaryResponse::getUpdatedDate).reversed())
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

        // Solo se puede cancelar si está procesando
        if (documentary.getStatus() != DocumentaryStatus.PROCESSING) {
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
        response.setNarrativeFocus(documentary.getNarrativeFocus());
        response.setEmotionalTone(documentary.getEmotionalTone());
        response.setStyleFilter(documentary.getStyleFilter());
        response.setStatus(documentary.getStatus());
        response.setProgress(documentary.getProgress());
        response.setVideoUrl(documentary.getVideoUrl());
        response.setThumbnailUrl(documentary.getThumbnailUrl());
        response.setVideoSize(documentary.getVideoSize());
        response.setVideoDuration(documentary.getVideoDuration());
        response.setTotalMemories(documentary.getTotalMemories());
        response.setErrorMessage(documentary.getErrorMessage());
        response.setCreatedDate(documentary.getCreatedDate());
        response.setProcessingCompleted(documentary.getProcessingCompleted());
        response.setPublishedDate(documentary.getPublishedDate());
        response.setUpdatedDate(documentary.getUpdatedDate());
        return response;
    }
}