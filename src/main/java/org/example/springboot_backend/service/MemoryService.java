package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.*;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.repository.*;
import org.example.springboot_backend.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class MemoryService implements IMemoryService {

    @Autowired
    private MemoryRepository memoryRepository;

    @Autowired
    private MemorialRepository memorialRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private StorageService storageService;
    
    @Override
    public MemoryResponse createMemory(MemoryCreateRequest request, MultipartFile[] files, User author) {
        validateRequest(request, author);
        
        // Validar espacio disponible antes de procesar archivos
        if (files != null && files.length > 0) {
            double totalFilesSize = storageService.calculateTotalFilesSize(files);
            storageService.validateUserStorageCapacity(author, totalFilesSize);
        }
        
        Memorial memorial = memorialRepository.findById(request.getMemorialId())
            .orElseThrow(() -> new RuntimeException("Memorial not found"));

        Memory memory = buildMemoryFromRequest(request, memorial, author);
        memory = memoryRepository.save(memory);

        List<File> savedFiles = new ArrayList<>();
        if (files != null && files.length > 0) {
            savedFiles = storageService.processFiles(files, memory);
            Double totalSpace = storageService.calculateTotalSpace(savedFiles);
            memory.setTotalUsedSpace(totalSpace);
            memory = memoryRepository.save(memory);
            
            // Actualizar el espacio usado del usuario
            storageService.increaseUserUsedSpace(author, totalSpace);
        }

        return buildResponse(memory, savedFiles);
    }

    @Override
    public Page<MemoryResponse> listByMemorial(UUID memorialId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Memory> result = memoryRepository.findByMemorial_IdMemorialOrderByCreatedDateDesc(memorialId, pageable);

        return result.map(memory -> {
            MemoryResponse r = new MemoryResponse();
            r.setIdMemory(memory.getIdMemory());
            r.setType(memory.getType());
            r.setTitle(memory.getTitle());
            r.setDescription(memory.getDescription());
            r.setPhotoDate(memory.getPhotoDate());
            r.setLocation(memory.getLocation());
            r.setVisible(memory.isVisible());
            r.setTags(memory.getTags());
            r.setAssociatedQuestion(memory.getAssociatedQuestion());
            r.setTotalUsedSpace(memory.getTotalUsedSpace() != null ? memory.getTotalUsedSpace() / (1024 * 1024) : 0.0); // Convert bytes to MB
            r.setCreatedDate(memory.getCreatedDate());

            if (memory.getFiles() != null && !memory.getFiles().isEmpty()) {
                // ✅ USAR EL MÉTODO buildFileResponse EXISTENTE QUE MANTIENE LAS URLs DE AZURE
                r.setFiles(memory.getFiles().stream()
                    .map(this::buildFileResponse)
                    .toList());
            } else {
                r.setFiles(List.of());
            }

            return r;
        });
    }

    private void validateRequest(MemoryCreateRequest request, User author) {
        // Validation logic can be added here in the future if needed
    }

    private Memory buildMemoryFromRequest(MemoryCreateRequest request, Memorial memorial, User author) {
        Memory memory = new Memory();
        memory.setMemorial(memorial);
        memory.setType(request.getType());
        memory.setTitle(request.getTitle());
        memory.setDescription(request.getDescription());
        memory.setPhotoDate(request.getPhotoDate());
        memory.setLocation(request.getLocation());
        memory.setVisible(request.isVisible());
        memory.setAuthor(author);
        memory.setAssociatedQuestion(request.getAssociatedQuestion());
        memory.setTags(request.getTags());
        memory.setCreatedDate(LocalDateTime.now());

        if (request.getQuestionId() != null) {
            Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));
            memory.setQuestion(question);
        }

        if (request.getAnswerId() != null) {
            Answer answer = answerRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new RuntimeException("Answer not found"));
            memory.setAnswer(answer);
        }

        return memory;
    }

    private MemoryResponse buildResponse(Memory memory, List<File> files) {
        MemoryResponse response = new MemoryResponse();
        response.setIdMemory(memory.getIdMemory());
        response.setType(memory.getType());
        response.setTitle(memory.getTitle());
        response.setDescription(memory.getDescription());
        response.setPhotoDate(memory.getPhotoDate());
        response.setLocation(memory.getLocation());
        response.setVisible(memory.isVisible());
        response.setTags(memory.getTags());
        response.setAssociatedQuestion(memory.getAssociatedQuestion());
        response.setTotalUsedSpace(memory.getTotalUsedSpace() != null ? memory.getTotalUsedSpace() / (1024 * 1024) : 0.0); // Convert bytes to MB
        response.setCreatedDate(memory.getCreatedDate());

        List<FileResponse> fileResponses = files.stream()
            .map(this::buildFileResponse)
            .toList();
        response.setFiles(fileResponses);

        return response;
    }



    // Obtener todas las memorias del autor ordenadas por fecha de creación descendente
    @Override
    public List<MemoryResponse> listByAuthor(User author) {
        // Obtener todas las memorias del autor ordenadas por fecha de creación descendente
        List<Memory> memories = memoryRepository.findByAuthorOrderByCreatedDateDesc(author);

        // Mapear a MemoryResponse incluyendo archivos en Base64
        return memories.stream().map(memory -> {
            MemoryResponse r = new MemoryResponse();
            r.setIdMemory(memory.getIdMemory());
            r.setType(memory.getType());
            r.setTitle(memory.getTitle());
            r.setDescription(memory.getDescription());
            r.setPhotoDate(memory.getPhotoDate());
            r.setLocation(memory.getLocation());
            r.setVisible(memory.isVisible());
            r.setTags(memory.getTags());
            r.setAssociatedQuestion(memory.getAssociatedQuestion());
            r.setTotalUsedSpace(memory.getTotalUsedSpace());
            r.setCreatedDate(memory.getCreatedDate());

            if (memory.getFiles() != null && !memory.getFiles().isEmpty()) {
                List<FileResponse> fileResponses = memory.getFiles().stream()
                    .map(this::buildFileResponse)
                    .toList();
                r.setFiles(fileResponses);
            } else {
                r.setFiles(List.of());
            }

            return r;
        }).toList();
    }

    @Override
    @Transactional
    public MemoryResponse updateMemory(
            UUID memoryId,
            MemoryCreateRequest request,
            MultipartFile[] files,
            List<FileDeleteRequest> filesToDelete, // <-- archivos a eliminar
            User author
    ) {
        // Buscar memoria existente
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new RuntimeException("Memory not found"));

        // Verificar que el usuario sea el autor
        if (!memory.getAuthor().getIdUser().equals(author.getIdUser())) {
            throw new RuntimeException("User is not the author of this memory");
        }

        // Actualizar campos básicos
        if (request.getTitle() != null) memory.setTitle(request.getTitle());
        if (request.getDescription() != null) memory.setDescription(request.getDescription());
        if (request.getTags() != null) memory.setTags(request.getTags());
        if (request.getLocation() != null) memory.setLocation(request.getLocation());
        if (request.getPhotoDate() != null) memory.setPhotoDate(request.getPhotoDate());
        memory.setVisible(request.isVisible());
        memory.setUpdatedDate(LocalDateTime.now());

        // Eliminar archivos indicados
        if (filesToDelete != null && !filesToDelete.isEmpty()) {
            for (FileDeleteRequest fDel : filesToDelete) {
                memory.getFiles().stream()
                    .filter(f -> f.getIdFile().equals(fDel.getId()))
                    .findFirst()
                    .ifPresent(fileEntity -> {
                        try {
                            storageService.deleteFile(fileEntity);  // <-- llama a tu método completo
                            memory.getFiles().remove(fileEntity); // Quita de la lista local
                        } catch (Exception e) {
                            System.err.println("Error deleting file: " + fileEntity.getFileName() + " -> " + e.getMessage());
                        }
                    });
            }
        }

        // Procesar archivos nuevos
        List<File> savedFiles = new ArrayList<>();
        if (files != null && files.length > 0) {
            // Validar espacio disponible
            double totalFilesSize = storageService.calculateTotalFilesSize(files);
            storageService.validateUserStorageCapacity(author, totalFilesSize);

            // Procesar archivos y agregarlos a la memoria
            savedFiles = storageService.processFiles(files, memory);
            savedFiles.forEach(memory::addFile);

            // Actualizar espacio usado del usuario
            storageService.increaseUserUsedSpace(author, storageService.calculateTotalSpace(savedFiles));
        }

        // Guardar cambios en DB
        memoryRepository.save(memory);

        // Construir respuesta
        return buildResponse(memory, memory.getFiles());
    }

    // Nuevos métodos para visualizar recuerdos organizados
    
    @Override
    public MemoriesOrganizedResponse getMemoriesOrganized(UUID memorialId, String filterType, String sortBy, String sortOrder, int page, int size, User user) {
        // Verificar acceso al memorial
        Memorial memorial = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new RuntimeException("Memorial not found"));
        
        // TODO: Verificar permisos de acceso al memorial
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Memory> memoriesPage;
        
        // Aplicar filtros y ordenamiento
        switch (filterType.toLowerCase()) {
            case "images":
                memoriesPage = memoryRepository.findByMemorialAndFileType(memorialId, "image", pageable);
                break;
            case "videos":
                memoriesPage = memoryRepository.findByMemorialAndFileType(memorialId, "video", pageable);
                break;
            case "documents":
                memoriesPage = memoryRepository.findByMemorialAndFileType(memorialId, "document", pageable);
                break;
            default:
                memoriesPage = memoryRepository.findByMemorial_IdMemorialOrderByCreatedDateDesc(memorialId, pageable);
        }
        
        List<MemoryResponse> memoryResponses = memoriesPage.getContent().stream()
            .map(memory -> buildMemoryResponse(memory))
            .toList();
        
        // Crear metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalImages", memoryRepository.countByMemorialAndFileType(memorialId, "image"));
        metadata.put("totalVideos", memoryRepository.countByMemorialAndFileType(memorialId, "video"));
        metadata.put("totalDocuments", memoryRepository.countByMemorialAndFileType(memorialId, "document"));
        
        return new MemoriesOrganizedResponse(
            memoryResponses,
            metadata,
            (int) memoriesPage.getTotalElements(),
            memoriesPage.getTotalPages(),
            page,
            filterType,
            sortBy
        );
    }
    
    @Override
    public MemoriesByTypeResponse getMemoriesByType(UUID memorialId, User user) {
        // Verificar acceso al memorial
        Memorial memorial = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new RuntimeException("Memorial not found"));
        
        List<Memory> allMemories = memoryRepository.findByMemorial_IdMemorialOrderByCreatedDateDesc(memorialId);
        
        Map<String, List<MemoryResponse>> memoriesByType = new HashMap<>();
        Map<String, Integer> countByType = new HashMap<>();
        
        // Agrupar por tipo de archivo principal
        for (Memory memory : allMemories) {
            String type = determineMemoryType(memory);
            
            memoriesByType.computeIfAbsent(type, k -> new ArrayList<>())
                .add(buildMemoryResponse(memory));
            
            countByType.put(type, countByType.getOrDefault(type, 0) + 1);
        }
        
        return new MemoriesByTypeResponse(memoriesByType, countByType, allMemories.size());
    }
    
    @Override
    public MemoriesByTimelineResponse getMemoriesByTimeline(UUID memorialId, String year, String month, User user) {
        // Verificar acceso al memorial
        Memorial memorial = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new RuntimeException("Memorial not found"));
        
        List<Memory> allMemories = memoryRepository.findByMemorial_IdMemorialOrderByCreatedDateDesc(memorialId);
        
        Map<String, Map<String, List<MemoryResponse>>> memoriesByTimeline = new HashMap<>();
        Map<String, Integer> countByYear = new HashMap<>();
        Map<String, Map<String, Integer>> countByMonth = new HashMap<>();
        
        for (Memory memory : allMemories) {
            LocalDate date = memory.getPhotoDate() != null ? memory.getPhotoDate() : memory.getCreatedDate().toLocalDate();
            String yearStr = String.valueOf(date.getYear());
            String monthStr = String.format("%02d", date.getMonthValue());
            
            // Filtrar por año y mes si se especifican
            if (year != null && !yearStr.equals(year)) continue;
            if (month != null && !monthStr.equals(month)) continue;
            
            memoriesByTimeline.computeIfAbsent(yearStr, k -> new HashMap<>())
                .computeIfAbsent(monthStr, k -> new ArrayList<>())
                .add(buildMemoryResponse(memory));
            
            countByYear.put(yearStr, countByYear.getOrDefault(yearStr, 0) + 1);
            countByMonth.computeIfAbsent(yearStr, k -> new HashMap<>())
                .put(monthStr, countByMonth.get(yearStr).getOrDefault(monthStr, 0) + 1);
        }
        
        return new MemoriesByTimelineResponse(memoriesByTimeline, countByYear, countByMonth, allMemories.size());
    }
    
    @Override
    public MemoriesByThemesResponse getMemoriesByThemes(UUID memorialId, User user) {
        // Verificar acceso al memorial
        Memorial memorial = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new RuntimeException("Memorial not found"));
        
        List<Memory> allMemories = memoryRepository.findByMemorial_IdMemorialOrderByCreatedDateDesc(memorialId);
        
        Map<String, List<MemoryResponse>> memoriesByTheme = new HashMap<>();
        Map<String, Integer> countByTheme = new HashMap<>();
        Set<String> allThemes = new HashSet<>();
        
        for (Memory memory : allMemories) {
            List<String> tags = memory.getTags() != null ? memory.getTags() : List.of("Sin tema");
            
            if (tags.isEmpty()) {
                tags = List.of("Sin tema");
            }
            
            for (String tag : tags) {
                allThemes.add(tag);
                memoriesByTheme.computeIfAbsent(tag, k -> new ArrayList<>())
                    .add(buildMemoryResponse(memory));
                countByTheme.put(tag, countByTheme.getOrDefault(tag, 0) + 1);
            }
        }
        
        return new MemoriesByThemesResponse(memoriesByTheme, countByTheme, new ArrayList<>(allThemes), allMemories.size());
    }
    
    @Override
    public MemoriesByMomentsResponse getMemoriesByMoments(UUID memorialId, User user) {
        // Verificar acceso al memorial
        Memorial memorial = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new RuntimeException("Memorial not found"));
        
        List<Memory> allMemories = memoryRepository.findByMemorial_IdMemorialOrderByCreatedDateDesc(memorialId);
        
        Map<String, List<MemoryResponse>> memoriesByMoment = new HashMap<>();
        Map<String, Integer> countByMoment = new HashMap<>();
        Set<String> allMoments = new HashSet<>();
        
        for (Memory memory : allMemories) {
            String moment = determineMemoryMoment(memory);
            allMoments.add(moment);
            
            memoriesByMoment.computeIfAbsent(moment, k -> new ArrayList<>())
                .add(buildMemoryResponse(memory));
            countByMoment.put(moment, countByMoment.getOrDefault(moment, 0) + 1);
        }
        
        return new MemoriesByMomentsResponse(memoriesByMoment, countByMoment, new ArrayList<>(allMoments), allMemories.size());
    }
    
    // Métodos auxiliares
    
    private MemoryResponse buildMemoryResponse(Memory memory) {
        MemoryResponse response = new MemoryResponse();
        response.setIdMemory(memory.getIdMemory());
        response.setType(memory.getType());
        response.setTitle(memory.getTitle());
        response.setDescription(memory.getDescription());
        response.setPhotoDate(memory.getPhotoDate());
        response.setLocation(memory.getLocation());
        response.setVisible(memory.isVisible());
        response.setTags(memory.getTags());
        response.setAssociatedQuestion(memory.getAssociatedQuestion());
        response.setTotalUsedSpace(memory.getTotalUsedSpace() != null ? memory.getTotalUsedSpace() / (1024 * 1024) : 0.0);
        response.setCreatedDate(memory.getCreatedDate());

        if (memory.getFiles() != null && !memory.getFiles().isEmpty()) {
            response.setFiles(memory.getFiles().stream()
                .map(this::buildFileResponse)
                .toList());
        } else {
            response.setFiles(List.of());
        }

        return response;
    }
    
    private String determineMemoryType(Memory memory) {
        if (memory.getFiles() == null || memory.getFiles().isEmpty()) {
            return "text";
        }
        
        // Determinar tipo basado en el primer archivo
        File firstFile = memory.getFiles().get(0);
        return firstFile.getFileType();
    }
    
    private String determineMemoryMoment(Memory memory) {
        // Determinar momento basado en la pregunta asociada o tags
        if (memory.getAssociatedQuestion() != null) {
            String question = memory.getAssociatedQuestion().toLowerCase();
            if (question.contains("infancia") || question.contains("niño")) return "Infancia";
            if (question.contains("juventud") || question.contains("adolescencia")) return "Juventud";
            if (question.contains("familia")) return "Familia";
            if (question.contains("trabajo") || question.contains("carrera")) return "Carrera";
            if (question.contains("viaje")) return "Viajes";
            if (question.contains("celebración") || question.contains("fiesta")) return "Celebraciones";
        }
        
        // Determinar por tags
        if (memory.getTags() != null) {
            for (String tag : memory.getTags()) {
                String tagLower = tag.toLowerCase();
                if (tagLower.contains("infancia")) return "Infancia";
                if (tagLower.contains("familia")) return "Familia";
                if (tagLower.contains("trabajo")) return "Carrera";
                if (tagLower.contains("viaje")) return "Viajes";
                if (tagLower.contains("celebración")) return "Celebraciones";
            }
        }
        
        return "Otros momentos";
    }

    private FileResponse buildFileResponse(File file) {
        FileResponse response = new FileResponse();
        response.setIdFile(file.getIdFile());
        response.setFileName(file.getFileName());
        response.setOriginalFileName(file.getOriginalFileName());
        response.setFileType(file.getFileType());
        response.setMimeType(file.getMimeType());

        // Para almacenamiento local, generar URL accesible desde el servidor
        String fileUrl;
        if ("local".equals(file.getStorageProvider()) || file.getStorageProvider() == null) {
            // Generar URL del servidor local para archivos locales
            // Usar 10.0.2.2 para que sea accesible desde el emulador de Android
            fileUrl = "http://10.0.2.2:8080/storage/" + file.getFileUrl().replace("\\", "/");
        } else {
            // Para Azure u otros proveedores, usar la URL directa
            fileUrl = file.getFileUrl();
        }

        response.setFileUrl(fileUrl);
        response.setFileSize(file.getFileSize() != null ? file.getFileSize() / (1024 * 1024) : 0.0); // Convert bytes to MB
        response.setUploadedDate(file.getUploadedDate());
        return response;
    }

}