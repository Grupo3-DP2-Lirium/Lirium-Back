package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.CreateCapsuleRequest;
import org.example.springboot_backend.dto.CapsuleResponse;
import org.example.springboot_backend.entity.Capsule;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.Memory;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.CapsuleFilter;
import org.example.springboot_backend.enums.CapsuleStatus;
import org.example.springboot_backend.repository.CapsuleRepository;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.MemoryRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CapsuleService {

    @Autowired
    private CapsuleRepository capsuleRepository;

    @Autowired
    private MemorialRepository memorialRepository;

    @Autowired
    private MemoryRepository memoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AIClassificationService aiService;

    @Autowired
    private CapsuleProcessingService processingService;

    /**
     * Crea una cápsula en estado DRAFT
     */
    @Transactional
    public CapsuleResponse createCapsuleDraft(CreateCapsuleRequest request, UUID userId) {
        System.out.println("🎬 Creating capsule draft for memorial: " + request.getMemorialId());

        Memorial memorial = memorialRepository.findById(request.getMemorialId())
                .orElseThrow(() -> new RuntimeException("Memorial not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validar que el usuario sea el dueño del memorial
        if (!memorial.getUser().getIdUser().equals(userId)) {
            throw new RuntimeException("User is not the owner of this memorial");
        }

        // Validar prompt
        if (request.getUserPrompt() == null || request.getUserPrompt().isBlank()) {
            throw new RuntimeException("User prompt is required");
        }

        // Crear entidad
        Capsule capsule = new Capsule();
        capsule.setMemorial(memorial);
        capsule.setCreatedBy(user);
        capsule.setUserPrompt(request.getUserPrompt());
        capsule.setTitle(request.getTitle() != null ? request.getTitle() : generateTitleFromPrompt(request.getUserPrompt()));
        capsule.setDescription(request.getDescription());
        capsule.setMusicTrack(request.getMusicTrack());

        // Parsear filtro
        CapsuleFilter filter = CapsuleFilter.NATURAL;
        if (request.getFilter() != null) {
            try {
                filter = CapsuleFilter.valueOf(request.getFilter().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("⚠️ Invalid filter: " + request.getFilter() + ", using NATURAL");
            }
        }
        capsule.setFilter(filter);

        capsule.setStatus(CapsuleStatus.DRAFT);
        capsule.setProgress(0);

        Capsule saved = capsuleRepository.save(capsule);
        System.out.println("✅ Capsule draft created: " + saved.getIdCapsule());

        return mapToResponse(saved);
    }

    /**
     * Inicia la generación de la cápsula (selección automática + video vertical)
     * FIXED: Ejecuta el procesamiento asíncrono DESPUÉS del commit de la transacción
     */
    @Transactional
    public CapsuleResponse startCapsuleGeneration(UUID capsuleId, UUID userId) {
        System.out.println("🚀 Starting capsule generation: " + capsuleId);

        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new RuntimeException("Capsule not found"));

        // Validar permisos
        if (!capsule.getCreatedBy().getIdUser().equals(userId)) {
            throw new RuntimeException("User is not the creator of this capsule");
        }

        // Validar estado
        if (capsule.getStatus() != CapsuleStatus.DRAFT && capsule.getStatus() != CapsuleStatus.FAILED) {
            throw new RuntimeException("Capsule is not in DRAFT or FAILED state");
        }

        // SELECCIÓN AUTOMÁTICA DE RECUERDOS CON IA
        List<Memory> selectedMemories = selectMemoriesWithAI(capsule);

        if (selectedMemories.isEmpty()) {
            String errorMsg = String.format(
                    "No se encontraron recuerdos relacionados con '%s' en este memorial. ",
                    capsule.getUserPrompt()
            );
            throw new RuntimeException(errorMsg);
        }

        // Limitar a máximo 12 recuerdos
        if (selectedMemories.size() > 12) {
            selectedMemories = selectedMemories.subList(0, 12);
        }

        // Guardar IDs de los recuerdos seleccionados
        String memoryIds = selectedMemories.stream()
                .map(m -> m.getIdMemory().toString())
                .collect(Collectors.joining(","));

        capsule.setMemoryIds(memoryIds);
        capsule.setTotalMemories(selectedMemories.size());
        capsule.setStatus(CapsuleStatus.PROCESSING);
        capsule.setProcessingStarted(LocalDateTime.now());
        capsule.setProgress(0);
        capsule.setErrorMessage(null);

        Capsule saved = capsuleRepository.save(capsule);

        //FIX: Asegurar que memoryIds se persista ANTES del async
        capsuleRepository.flush();

        final UUID capsuleIdToProcess = saved.getIdCapsule();

        //FIX: Registrar callback para ejecutar DESPUÉS del commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                System.out.println("✅ Transaction committed, starting async processing for: " + capsuleIdToProcess);
                processingService.processCapsule(capsuleIdToProcess);
            }
        });

        System.out.println("✅ Capsule generation scheduled with " + selectedMemories.size() + " memories");
        return mapToResponse(saved);
    }

    /**
     *  SELECCIÓN AUTOMÁTICA DE RECUERDOS CON IA
     */
    private List<Memory> selectMemoriesWithAI(Capsule capsule) {
        System.out.println("🤖 Selecting memories with AI for prompt: " + capsule.getUserPrompt());

        List<Memory> allMemories = memoryRepository.findByMemorial_IdMemorialOrderByCreatedDateDesc(
                        capsule.getMemorial().getIdMemorial()
                ).stream()
                .filter(Memory::isVisible)
                .filter(Memory::hasFiles)
                .collect(Collectors.toList());

        if (allMemories.isEmpty()) {
            throw new RuntimeException("Memorial has no visible memories with files");
        }

        System.out.println("📦 Total memories available: " + allMemories.size());

        // IA selecciona las más relevantes (sin límite rígido)
        List<Memory> selectedMemories = aiService.seleccionarRecuerdosParaCapsula(
                capsule.getUserPrompt(),
                capsule.getMemorial().getName(),
                allMemories,
                20 // Suficientes candidatas para elegir
        );

        if (selectedMemories.isEmpty()) {
            String errorMsg = String.format(
                    "No se encontraron recuerdos relacionados con '%s' en este memorial. " +
                            "Intenta con otros temas o verifica que existan recuerdos sobre ese tema.",
                    capsule.getUserPrompt()
            );
            throw new RuntimeException(errorMsg);
        }

        // Limitar inteligentemente a 12 archivos totales para máx 1 min
        return limitToMaxFilesIntelligent(selectedMemories, 12);
    }

    /**
     * Limita selección maximizando el uso de los 12 slots disponibles
     */
    private List<Memory> limitToMaxFilesIntelligent(List<Memory> memories, int maxFiles) {
        List<Memory> result = new ArrayList<>();
        int totalFiles = 0;

        for (Memory memory : memories) {
            if (!memory.hasFiles()) continue;

            int filesInMemory = memory.getFiles().size();

            // Agregar si cabe completa
            if (totalFiles + filesInMemory <= maxFiles) {
                result.add(memory);
                totalFiles += filesInMemory;
                System.out.println("   ✅ Memory " + memory.getIdMemory() +
                        ": " + filesInMemory + " files (total: " + totalFiles + "/" + maxFiles + ")");
            } else {
                int remaining = maxFiles - totalFiles;
                System.out.println("   ⏭️ Skipped memory " + memory.getIdMemory() +
                        " (" + filesInMemory + " files, only " + remaining + " slots left)");
            }

            if (totalFiles >= maxFiles) {
                System.out.println("✅ Reached maximum of " + maxFiles + " files");
                break;
            }
        }

        if (result.isEmpty()) {
            throw new RuntimeException("Could not select any memories within file limit");
        }

        System.out.println("📊 Final: " + result.size() + " memories → " + totalFiles + " files");
        return result;
    }

    /**
     * Publica la cápsula en el memorial
     */
    @Transactional
    public CapsuleResponse publishCapsule(UUID capsuleId, UUID userId) {
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new RuntimeException("Capsule not found"));

        if (!capsule.getCreatedBy().getIdUser().equals(userId)) {
            throw new RuntimeException("User is not the creator of this capsule");
        }

        if (capsule.getStatus() != CapsuleStatus.COMPLETED) {
            throw new RuntimeException("Capsule is not completed yet");
        }

        capsule.setStatus(CapsuleStatus.PUBLISHED);
        capsule.setPublishedDate(LocalDateTime.now());

        Capsule saved = capsuleRepository.save(capsule);
        System.out.println("✅ Capsule published: " + saved.getIdCapsule());

        return mapToResponse(saved);
    }

    /**
     * Actualiza una cápsula (título, descripción, filtro, música)
     */
    @Transactional
    public CapsuleResponse updateCapsule(UUID capsuleId, CreateCapsuleRequest request, UUID userId) {
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new RuntimeException("Capsule not found"));

        if (!capsule.getCreatedBy().getIdUser().equals(userId)) {
            throw new RuntimeException("User is not the creator of this capsule");
        }

        // Solo se puede editar en estado DRAFT o COMPLETED
        if (capsule.getStatus() == CapsuleStatus.PROCESSING) {
            throw new RuntimeException("Cannot update capsule while processing");
        }

        if (request.getTitle() != null) {
            capsule.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            capsule.setDescription(request.getDescription());
        }
        if (request.getMusicTrack() != null) {
            capsule.setMusicTrack(request.getMusicTrack());
        }
        if (request.getFilter() != null) {
            try {
                capsule.setFilter(CapsuleFilter.valueOf(request.getFilter().toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.err.println("⚠️ Invalid filter, keeping current");
            }
        }

        Capsule saved = capsuleRepository.save(capsule);
        System.out.println("✅ Capsule updated: " + saved.getIdCapsule());

        return mapToResponse(saved);
    }

    /**
     * Obtiene el estado de una cápsula
     */
    @Transactional(readOnly = true)
    public CapsuleResponse getCapsuleStatus(UUID capsuleId) {
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new RuntimeException("Capsule not found"));
        return mapToResponse(capsule);
    }

    /**
     * Obtiene todas las cápsulas de un memorial
     */
    @Transactional(readOnly = true)
    public List<CapsuleResponse> getCapsulesByMemorial(UUID memorialId) {
        return capsuleRepository.findByMemorial_IdMemorialOrderByCreatedDateDesc(memorialId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene cápsulas por memorial y estado
     */
    @Transactional(readOnly = true)
    public List<CapsuleResponse> getCapsulesByMemorialAndStatus(UUID memorialId, CapsuleStatus status) {
        return capsuleRepository.findByMemorial_IdMemorialAndStatusOrderByCreatedDateDesc(memorialId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las cápsulas creadas por un usuario
     */
    @Transactional(readOnly = true)
    public List<CapsuleResponse> getCapsulesByUser(UUID userId) {
        return capsuleRepository.findByCreatedBy_IdUserOrderByCreatedDateDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancela una cápsula en proceso
     */
    @Transactional
    public void cancelCapsule(UUID capsuleId, UUID userId) {
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new RuntimeException("Capsule not found"));

        if (!capsule.getCreatedBy().getIdUser().equals(userId)) {
            throw new RuntimeException("User is not the creator of this capsule");
        }

        if (capsule.getStatus() != CapsuleStatus.PROCESSING && capsule.getStatus() != CapsuleStatus.DRAFT) {
            throw new RuntimeException("Capsule cannot be cancelled in current state");
        }

        capsule.setStatus(CapsuleStatus.CANCELLED);
        capsule.setErrorMessage("Cancelled by user");
        capsuleRepository.save(capsule);

        System.out.println("✅ Capsule cancelled: " + capsuleId);
    }

    /**
     * Elimina una cápsula
     */
    @Transactional
    public void deleteCapsule(UUID capsuleId, UUID userId) {
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new RuntimeException("Capsule not found"));

        if (!capsule.getCreatedBy().getIdUser().equals(userId)) {
            throw new RuntimeException("User is not the creator of this capsule");
        }

        // TODO: Eliminar video de Azure si existe
        if (capsule.getVideoUrl() != null) {
            System.out.println("⚠️ TODO: Delete video from Azure: " + capsule.getVideoUrl());
        }

        capsuleRepository.delete(capsule);
        System.out.println("✅ Capsule deleted: " + capsuleId);
    }

    // ==================== HELPERS ====================

    private CapsuleResponse mapToResponse(Capsule capsule) {
        CapsuleResponse response = new CapsuleResponse();
        response.setIdCapsule(capsule.getIdCapsule());
        response.setMemorialId(capsule.getMemorial().getIdMemorial());
        response.setMemorialName(capsule.getMemorial().getName());
        response.setUserPrompt(capsule.getUserPrompt());
        response.setTitle(capsule.getTitle());
        response.setDescription(capsule.getDescription());
        response.setMusicTrack(capsule.getMusicTrack());
        response.setFilter(capsule.getFilter().name());
        response.setStatus(capsule.getStatus());
        response.setProgress(capsule.getProgress());
        response.setVideoUrl(capsule.getVideoUrl());
        response.setThumbnailUrl(capsule.getThumbnailUrl());
        response.setVideoSize(capsule.getVideoSize());
        response.setVideoDuration(capsule.getVideoDuration());
        response.setTotalMemories(capsule.getTotalMemories());
        response.setErrorMessage(capsule.getErrorMessage());
        response.setCreatedDate(capsule.getCreatedDate());
        response.setProcessingCompleted(capsule.getProcessingCompleted());
        response.setPublishedDate(capsule.getPublishedDate());
        response.setUpdatedDate(capsule.getUpdatedDate());
        return response;
    }

    private String generateTitleFromPrompt(String prompt) {
        // Capitalizar primera letra y limitar a 50 caracteres
        if (prompt == null || prompt.isBlank()) {
            return "Mi Cápsula";
        }
        String title = prompt.trim();
        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        return title.length() > 50 ? title.substring(0, 47) + "..." : title;
    }
}