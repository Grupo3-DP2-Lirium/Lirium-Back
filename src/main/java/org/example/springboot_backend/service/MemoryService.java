package org.example.springboot_backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.springboot_backend.dto.FileDeleteRequest;
import org.example.springboot_backend.dto.FileResponse;
import org.example.springboot_backend.dto.MemoriesByTypeResponse;
import org.example.springboot_backend.dto.MemoryCreateRequest;
import org.example.springboot_backend.dto.MemoryLiteResponse;
import org.example.springboot_backend.dto.MemoryResponse;
import org.example.springboot_backend.entity.Answer;
import org.example.springboot_backend.entity.File;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.Memory;
import org.example.springboot_backend.entity.Question;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.CategoriaEnum;
import org.example.springboot_backend.enums.MomentoEnum;
import org.example.springboot_backend.repository.AnswerRepository;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.MemoryRepository;
import org.example.springboot_backend.repository.QuestionRepository;
import org.example.springboot_backend.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


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

    @Autowired
    private AIClassificationService aiService;

    @Override
    public MemoryResponse createMemory(MemoryCreateRequest request, MultipartFile[] files, User author) {
        try {
            //System.out.println("=== INICIO createMemory ===");
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

            // 1️⃣ Clasificar con IA y setear categorías/momentos
            if ((request.getTitle() != null && !request.getTitle().isBlank()) ||
                    (request.getDescription() != null && !request.getDescription().isBlank())) {

                //System.out.println("🤖 Iniciando clasificación con IA...");
                //System.out.println("   Título: " + request.getTitle());
                //System.out.println("   Descripción: " + (request.getDescription() != null ? request.getDescription().substring(0, Math.min(50, request.getDescription().length())) + "..." : "null"));

                try {
                    var out = aiService.clasificar(request.getTitle(), request.getDescription());

                    if (out == null) {
                        System.err.println("❌ aiService.clasificar() retornó null");
                        memory.setCategorias(List.of(CategoriaEnum.OTROS));
                        memory.setMomentos(List.of(MomentoEnum.COTIDIANO));
                    } else {
                        System.out.println("✅ Clasificación recibida: " + out);

                        List<CategoriaEnum> categorias = out.getOrDefault("categorias", List.of("otros"))
                                .stream()
                                .map(this::safeCat)
                                .filter(Objects::nonNull)
                                .distinct()
                                .limit(3)
                                .collect(Collectors.toCollection(ArrayList::new)); // ✅ ArrayList mutable

                        List<MomentoEnum> momentos = out.getOrDefault("momentos", List.of("cotidiano"))
                                .stream()
                                .map(this::safeMom)
                                .filter(Objects::nonNull)
                                .distinct()
                                .limit(3)
                                .collect(Collectors.toCollection(ArrayList::new)); // ✅ ArrayList mutable

                        memory.setCategorias(categorias.isEmpty() ? new ArrayList<>(List.of(CategoriaEnum.OTROS)) : categorias); // ✅
                        memory.setMomentos(momentos.isEmpty() ? new ArrayList<>(List.of(MomentoEnum.COTIDIANO)) : momentos); // ✅

                        System.out.println("✅ Categorías asignadas: " + memory.getCategorias());
                        System.out.println("✅ Momentos asignados: " + memory.getMomentos());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Excepción al clasificar con IA: " + e.getClass().getName());
                    System.err.println("❌ Mensaje: " + e.getMessage());
                    e.printStackTrace();

                    memory.setCategorias(List.of(CategoriaEnum.OTROS));
                    memory.setMomentos(List.of(MomentoEnum.COTIDIANO));
                }
            }

            // 2️⃣ Determinar si va en línea de tiempo (método nuevo)
            try {
                System.out.println("⏰ Determinando si va en línea de tiempo...");
                boolean vaEnTimeline = aiService.debeIrEnLineaTiempo(
                        request.getTitle(),
                        request.getDescription(),
                        request.getPhotoDate()
                );
                memory.setEsLineaTiempo(vaEnTimeline);
                System.out.println("✅ Línea de tiempo: " + vaEnTimeline);
            } catch (Exception e) {
                System.err.println("❌ Error determinando timeline: " + e.getMessage());
                // Fallback: si tiene fecha, va en timeline
                memory.setEsLineaTiempo(request.getPhotoDate() != null);
            }

            List<File> savedFiles = new ArrayList<>();
            if (files != null && files.length > 0) {
                try {
                    savedFiles = storageService.processFiles(files, memory);
                    Double totalSpace = storageService.calculateTotalSpace(savedFiles);
                    memory.setTotalUsedSpace(totalSpace);
                    memory = memoryRepository.save(memory);


                    storageService.increaseUserUsedSpace(author, totalSpace);

                } catch (Exception e) {
                    System.err.println("❌ Error procesando archivos: " + e.getClass().getName());
                    System.err.println("❌ Mensaje: " + e.getMessage());
                    e.printStackTrace();
                    throw e; // Re-lanzar para que se capture arriba
                }
            }

            MemoryResponse response = buildResponse(memory, savedFiles);
            //System.out.println("=== FIN createMemory (EXITOSO) ===");

            return response;

        } catch (Exception e) {
            System.err.println("❌❌❌ ERROR FATAL en createMemory ❌❌❌");
            System.err.println("Tipo: " + e.getClass().getName());
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("=== FIN createMemory (ERROR) ===");
            throw e; // Re-lanzar para que el controller lo maneje
        }
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

            //categorías/momentos en listado por memorial
            r.setCategorias(
                    memory.getCategorias() == null ? List.of()
                            : memory.getCategorias().stream().map(Enum::name).map(String::toLowerCase).toList()
            );
            r.setMomentos(
                    memory.getMomentos() == null ? List.of()
                            : memory.getMomentos().stream().map(Enum::name).map(String::toLowerCase).toList()
            );


            return r;
        });
    }

    private void validateRequest(MemoryCreateRequest request, User author) {
        // Validation logic can be added here in the future if needed
    }

    private CategoriaEnum safeCat(String s) {
        if (s == null || s.isBlank()) {
            //System.err.println("⚠️ safeCat recibió null/blank, retornando OTROS");
            return CategoriaEnum.OTROS;
        }

        try {
            // Convertir snake_case a SCREAMING_SNAKE_CASE
            String normalized = s.trim().toUpperCase();
            CategoriaEnum result = CategoriaEnum.valueOf(normalized);
            //System.out.println("✅ safeCat: '" + s + "' -> " + result);
            return result;
        } catch(IllegalArgumentException e) {
            //System.err.println("⚠️ Categoría no reconocida: '" + s + "' - usando OTROS");
            return CategoriaEnum.OTROS;
        }
    }

    private MomentoEnum safeMom(String s) {
        if (s == null || s.isBlank()) {
            //System.err.println("⚠️ safeMom recibió null/blank, retornando COTIDIANO");
            return MomentoEnum.COTIDIANO;
        }

        try {
            // Convertir snake_case a SCREAMING_SNAKE_CASE
            String normalized = s.trim().toUpperCase();
            MomentoEnum result = MomentoEnum.valueOf(normalized);
            //System.out.println("✅ safeMom: '" + s + "' -> " + result);
            return result;
        } catch(IllegalArgumentException e) {
            //System.err.println("⚠️ Momento no reconocido: '" + s + "' - usando COTIDIANO");
            return MomentoEnum.COTIDIANO;
        }
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
        memory.setLatitude(request.getLatitude());
        memory.setLongitude(request.getLongitude());

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
        response.setEsLineaTiempo(memory.getEsLineaTiempo() != null ? memory.getEsLineaTiempo() : false);
        response.setLatitude(memory.getLatitude());
        response.setLongitude(memory.getLongitude());

        List<FileResponse> fileResponses = files.stream()
            .map(this::buildFileResponse)
            .toList();
        response.setFiles(fileResponses);

        //mapear enums -> strings para la respuesta
        response.setCategorias(
                memory.getCategorias() == null ? List.of()
                        : memory.getCategorias().stream().map(Enum::name).map(String::toLowerCase).toList()
        );
        response.setMomentos(
                memory.getMomentos() == null ? List.of()
                        : memory.getMomentos().stream().map(Enum::name).map(String::toLowerCase).toList()
        );


        return response;
    }

    private FileResponse buildFileResponse(File file) {
        FileResponse response = new FileResponse();
        response.setIdFile(file.getIdFile());
        response.setFileName(file.getFileName());
        response.setOriginalFileName(file.getOriginalFileName());
        response.setFileType(file.getFileType());
        response.setMimeType(file.getMimeType());

        // ✅ USAR DIRECTAMENTE LA URL DE AZURE (ya es pública)
        response.setFileUrl(file.getFileUrl());

        response.setFileSize(file.getFileSize() != null ? file.getFileSize() / (1024 * 1024) : 0.0); // Convert bytes to MB
        response.setUploadedDate(file.getUploadedDate());
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

            //ategorías/momentos en listado por autor
            r.setCategorias(
                    memory.getCategorias() == null ? List.of()
                            : memory.getCategorias().stream().map(Enum::name).map(String::toLowerCase).toList()
            );
            r.setMomentos(
                    memory.getMomentos() == null ? List.of()
                            : memory.getMomentos().stream().map(Enum::name).map(String::toLowerCase).toList()
            );


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

        System.out.println("filesToDelete es nulo? " + (filesToDelete == null));
        System.out.println("filesToDelete tiene elementos? " + 
                   (filesToDelete != null ? filesToDelete.size() : "null"));

        // Eliminar archivos indicados
        if (filesToDelete != null && !filesToDelete.isEmpty()) {
            System.out.println("Archivos a eliminar: " + filesToDelete.size());

            for (FileDeleteRequest fDel : filesToDelete) {
                try {
                    UUID fileId = UUID.fromString(fDel.getId());
                    System.out.println("Buscando archivo con ID: " + fileId);

                    memory.getFiles().stream()
                        .filter(f -> f.getIdFile().equals(fileId))
                        .findFirst()
                        .ifPresentOrElse(fileEntity -> {
                            try {
                                System.out.println("Eliminando archivo: " + fileEntity.getFileName());
                                storageService.deleteFile(fileEntity);
                                memory.getFiles().remove(fileEntity);
                            } catch (Exception e) {
                                System.err.println("Error eliminando archivo " + fileEntity.getFileName() + ": " + e.getMessage());
                            }
                        }, () -> {
                            System.out.println("No se encontró archivo con ID: " + fileId + " dentro de la memoria.");
                        });

                } catch (IllegalArgumentException e) {
                    System.err.println("ID inválido (no es un UUID): " + fDel.getId());
                }
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

        // re-clasificar si cambian título o descripción
        boolean needsReclass = false;
        if (request.getTitle() != null) needsReclass = true;
        if (request.getDescription() != null) needsReclass = true;

        if (needsReclass) {
            try {
                System.out.println("🤖 Re-clasificando memoria actualizada...");
                var out = aiService.clasificar(
                        memory.getTitle(),
                        memory.getDescription()
                );

                if (out != null) {
                    // Usamos ArrayList mutable en lugar de toList()
                    memory.setCategorias(
                            out.getOrDefault("categorias", List.of("otros"))
                                    .stream()
                                    .map(this::safeCat)
                                    .filter(Objects::nonNull)
                                    .distinct()
                                    .limit(3)
                                    .collect(Collectors.toCollection(ArrayList::new))
                    );

                    memory.setMomentos(
                            out.getOrDefault("momentos", List.of("cotidiano"))
                                    .stream()
                                    .map(this::safeMom)
                                    .filter(Objects::nonNull)
                                    .distinct()
                                    .limit(3)
                                    .collect(Collectors.toCollection(ArrayList::new))
                    );

                    System.out.println("✅ Re-clasificación exitosa");
                }

                // 2️⃣ Re-evaluar línea de tiempo
                System.out.println("⏰ Re-evaluando línea de tiempo...");
                boolean vaEnTimeline = aiService.debeIrEnLineaTiempo(
                        memory.getTitle(),
                        memory.getDescription(),
                        memory.getPhotoDate()
                );
                memory.setEsLineaTiempo(vaEnTimeline);
                System.out.println("✅ Línea de tiempo actualizada: " + vaEnTimeline);

            } catch (Exception e) {
                System.err.println("❌ Error en re-clasificación: " + e.getMessage());
                e.printStackTrace();
                // Mantener las categorías existentes si falla
            }
        }

        // Guardar cambios en DB
        memoryRepository.save(memory);

        // Construir respuesta
        return buildResponse(memory, memory.getFiles());
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
        response.setEsLineaTiempo(memory.getEsLineaTiempo() != null ? memory.getEsLineaTiempo() : false);

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
        File firstFile = memory.getFiles().get(0); // Asegúrate de importar tu entidad
        return firstFile.getFileType();            // ej. "image" | "video" | "audio"
    }

    private String normalizeUiType(String rawType) {
        if (rawType == null || rawType.isBlank()) return "texto";
        String t = rawType.toLowerCase();
        if (t.contains("image") || t.equals("img") || t.equals("photo") || t.equals("picture")) return "foto";
        if (t.contains("video")) return "video";
        if (t.contains("audio") || t.equals("sound") || t.equals("voice")) return "audios";
        if (t.equals("text")) return "texto";
        return "texto";
    }

    // Lite DTO
    private MemoryLiteResponse toLite(Memory memory, String rawType) {
        MemoryLiteResponse m = new MemoryLiteResponse();
        m.setIdMemory(memory.getIdMemory());
        m.setTitle(memory.getTitle());
        m.setDescription(memory.getDescription());
        m.setPhotoDate(memory.getPhotoDate());
        m.setCreatedDate(memory.getCreatedDate());
        m.setFileType(normalizeUiType(rawType));
        if (memory.getFiles() != null && !memory.getFiles().isEmpty()) {
            File f0 = memory.getFiles().get(0);
            m.setFirstFileUrl(f0.getFileUrl());
        }
        return m;
    }


    public Map<String, Map<String, List<MemoryLiteResponse>>>
    listGroupedByCategoryAndType(UUID memorialId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Memory> result = memoryRepository
                .findByMemorial_IdMemorialOrderByCreatedDateDesc(memorialId, pageable);

        Map<String, Map<String, List<MemoryLiteResponse>>> out = new LinkedHashMap<>();

        for (Memory mem : result.getContent()) {
            String rawType = determineMemoryType(mem);
            String uiType = normalizeUiType(rawType);

            List<String> catIds = (mem.getCategorias() == null || mem.getCategorias().isEmpty())
                    ? List.of("otros")
                    : mem.getCategorias().stream().map(e -> e.name().toLowerCase()).toList();

            for (String cat : catIds) {
                out.computeIfAbsent(cat, k -> {
                    Map<String, List<MemoryLiteResponse>> m = new LinkedHashMap<>();
                    m.put("video", new ArrayList<>());
                    m.put("foto",  new ArrayList<>());
                    m.put("texto", new ArrayList<>());
                    m.put("audios",new ArrayList<>());
                    return m;
                });

                out.get(cat).get(uiType).add(toLite(mem, rawType));
            }
        }

        return out;
    }

    @Override
    public Map<String, Map<String, List<MemoryLiteResponse>>> listGroupedByMomentsAndType(UUID memorialId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Memory> result = memoryRepository
                .findByMemorial_IdMemorialOrderByCreatedDateDesc(memorialId, pageable);

        Map<String, Map<String, List<MemoryLiteResponse>>> out = new LinkedHashMap<>();

        for (Memory mem : result.getContent()) {
            String rawType = determineMemoryType(mem);
            String uiType = normalizeUiType(rawType);

            List<String> momIds = (mem.getMomentos() == null || mem.getMomentos().isEmpty())
                    ? List.of("otros")
                    : mem.getMomentos().stream().map(e -> e.name().toLowerCase()).toList();

            for (String mom : momIds) {
                out.computeIfAbsent(mom, k -> {
                    Map<String, List<MemoryLiteResponse>> m = new LinkedHashMap<>();
                    m.put("video", new ArrayList<>());
                    m.put("foto",  new ArrayList<>());
                    m.put("texto", new ArrayList<>());
                    m.put("audios",new ArrayList<>());
                    return m;
                });

                out.get(mom).get(uiType).add(toLite(mem, rawType));
            }
        }

        return out;
    }

    @Override
    public List<MemoryResponse> findTimelineMemories(UUID memorialId, int page, int size) {
        //Pageable SIN el Sort (porque ya está en la query nativa)
        Pageable pageable = PageRequest.of(page, size);

        Page<Memory> result = memoryRepository.findTimelineMemories(memorialId, pageable);

        return result.getContent()
                .stream()
                .map(this::buildMemoryResponse)
                .toList();
    }

    // === MÉTODOS PARA REFLEXIONES ===

    @Override
    public MemoryResponse createReflection(MemoryCreateRequest request, MultipartFile[] files, User author) {
        try {
            System.out.println("🌱 Creando reflexión para usuario: " + author.getEmail());
            
            // Obtener o crear el espacio personal de reflexiones
            Memorial personalSpace = getOrCreatePersonalReflectionSpace(author);
            
            // Establecer el memorial y el tipo como reflexión
            request.setMemorialId(personalSpace.getIdMemorial());
            request.setType(org.example.springboot_backend.enums.MemoryOriginType.REFLECTION);
            
            // Usar el método existente para crear la memoria
            MemoryResponse response = createMemory(request, files, author);
            
            System.out.println("✅ Reflexión creada exitosamente: " + response.getIdMemory());
            return response;
            
        } catch (Exception e) {
            System.err.println("❌ Error creando reflexión: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating reflection: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<MemoryResponse> listUserReflections(User user, int page, int size) {
        try {
            System.out.println("📋 Listando reflexiones para usuario: " + user.getEmail());
            
            Memorial personalSpace = getPersonalReflectionSpace(user);
            if (personalSpace == null) {
                System.out.println("ℹ️ Usuario no tiene espacio de reflexiones, retornando página vacía");
                return Page.empty();
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Memory> result = memoryRepository.findByMemorial_IdMemorialAndTypeOrderByCreatedDateDesc(
                personalSpace.getIdMemorial(), 
                org.example.springboot_backend.enums.MemoryOriginType.REFLECTION, 
                pageable
            );
            
            Page<MemoryResponse> reflections = result.map(memory -> {
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
                r.setTotalUsedSpace(memory.getTotalUsedSpace() != null ? memory.getTotalUsedSpace() / (1024 * 1024) : 0.0);
                r.setCreatedDate(memory.getCreatedDate());
                r.setEsLineaTiempo(memory.getEsLineaTiempo() != null ? memory.getEsLineaTiempo() : false);
                r.setLatitude(memory.getLatitude());
                r.setLongitude(memory.getLongitude());

                if (memory.getFiles() != null && !memory.getFiles().isEmpty()) {
                    r.setFiles(memory.getFiles().stream()
                        .map(this::buildFileResponse)
                        .toList());
                } else {
                    r.setFiles(List.of());
                }

                r.setCategorias(
                    memory.getCategorias() == null ? List.of()
                        : memory.getCategorias().stream().map(Enum::name).map(String::toLowerCase).toList()
                );
                r.setMomentos(
                    memory.getMomentos() == null ? List.of()
                        : memory.getMomentos().stream().map(Enum::name).map(String::toLowerCase).toList()
                );

                return r;
            });
            
            System.out.println("✅ Encontradas " + reflections.getTotalElements() + " reflexiones");
            return reflections;
            
        } catch (Exception e) {
            System.err.println("❌ Error listando reflexiones: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error listing reflections: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el espacio personal de reflexiones del usuario, lo crea si no existe
     */
    private Memorial getOrCreatePersonalReflectionSpace(User user) {
        Memorial existing = memorialRepository.findByUserAndIsJournalTrue(user);
        
        if (existing != null) {
            System.out.println("✅ Espacio de reflexiones existente encontrado: " + existing.getIdMemorial());
            return existing;
        }
        
        System.out.println("🆕 Creando nuevo espacio de reflexiones para: " + user.getEmail());
        
        Memorial personalSpace = new Memorial();
        personalSpace.setUser(user);
        personalSpace.setName("Mis Reflexiones Personales");
        personalSpace.setNickname("Reflexiones de " + user.getFirstName());
        personalSpace.setDescription("Espacio personal para reflexiones, pensamientos y momentos íntimos. Solo tú puedes ver y agregar contenido aquí.");
        personalSpace.setRelationType("Personal");
        personalSpace.setCollaborative(false); // Solo el usuario puede agregar contenido
        personalSpace.setJournal(true); // Marcarlo como diario personal
        personalSpace.setUsedSpace(0.0);
        personalSpace.setCreatedDate(LocalDateTime.now());
        personalSpace.setUpdatedDate(LocalDateTime.now());
        
        Memorial saved = memorialRepository.save(personalSpace);
        System.out.println("✅ Espacio de reflexiones creado: " + saved.getIdMemorial());
        
        return saved;
    }

    /**
     * Obtiene el espacio personal de reflexiones del usuario si existe
     */
    private Memorial getPersonalReflectionSpace(User user) {
        return memorialRepository.findByUserAndIsJournalTrue(user);
    }

    @Override
    @Transactional
    public void deleteReflection(UUID reflectionId, User user) {
        try {
            System.out.println("🗑️ Eliminando reflexión: " + reflectionId + " para usuario: " + user.getEmail());
            
            // Buscar la reflexión
            Memory reflection = memoryRepository.findById(reflectionId)
                .orElseThrow(() -> new RuntimeException("Reflection not found"));
            
            // Verificar que sea una reflexión
            if (reflection.getType() != org.example.springboot_backend.enums.MemoryOriginType.REFLECTION) {
                throw new RuntimeException("Memory is not a reflection");
            }
            
            // Verificar que el usuario sea el autor
            if (!reflection.getAuthor().getIdUser().equals(user.getIdUser())) {
                throw new RuntimeException("User is not authorized to delete this reflection");
            }
            
            // Verificar que pertenezca al espacio personal del usuario
            Memorial personalSpace = getPersonalReflectionSpace(user);
            if (personalSpace == null || !reflection.getMemorial().getIdMemorial().equals(personalSpace.getIdMemorial())) {
                throw new RuntimeException("Reflection does not belong to user's personal space");
            }
            
            // Calcular espacio total de archivos antes de eliminar
            double totalSpaceToFree = 0.0;
            if (reflection.getFiles() != null && !reflection.getFiles().isEmpty()) {
                totalSpaceToFree = storageService.calculateTotalSpace(reflection.getFiles());
                
                // Eliminar archivos del almacenamiento
                for (File file : reflection.getFiles()) {
                    try {
                        storageService.deleteFile(file);
                        System.out.println("✅ Archivo eliminado: " + file.getFileName());
                    } catch (Exception e) {
                        System.err.println("⚠️ Error eliminando archivo " + file.getFileName() + ": " + e.getMessage());
                        // Continúa con la eliminación aunque falle algún archivo
                    }
                }
            }
            
            // Eliminar la reflexión de la base de datos
            memoryRepository.delete(reflection);
            
            // Liberar espacio del usuario
            if (totalSpaceToFree > 0) {
                storageService.decreaseUserUsedSpace(user, totalSpaceToFree);
                System.out.println("✅ Liberado espacio del usuario: " + (totalSpaceToFree / (1024 * 1024)) + " MB");
            }
            
            System.out.println("✅ Reflexión eliminada exitosamente: " + reflectionId);
            
        } catch (Exception e) {
            System.err.println("❌ Error eliminando reflexión: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting reflection: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public MemoryResponse updateReflection(UUID reflectionId, MemoryCreateRequest request, MultipartFile[] files, List<FileDeleteRequest> filesToDelete, User user) {
        try {
            System.out.println("✏️ Editando reflexión: " + reflectionId + " para usuario: " + user.getEmail());
            
            // Buscar la reflexión
            Memory reflection = memoryRepository.findById(reflectionId)
                .orElseThrow(() -> new RuntimeException("Reflection not found"));
            
            // Verificar que sea una reflexión
            if (reflection.getType() != org.example.springboot_backend.enums.MemoryOriginType.REFLECTION) {
                throw new RuntimeException("Memory is not a reflection");
            }
            
            // Verificar que el usuario sea el autor
            if (!reflection.getAuthor().getIdUser().equals(user.getIdUser())) {
                throw new RuntimeException("User is not authorized to edit this reflection");
            }
            
            // Verificar que pertenezca al espacio personal del usuario
            Memorial personalSpace = getPersonalReflectionSpace(user);
            if (personalSpace == null || !reflection.getMemorial().getIdMemorial().equals(personalSpace.getIdMemorial())) {
                throw new RuntimeException("Reflection does not belong to user's personal space");
            }
            
            // Actualizar campos básicos si se proporcionan
            boolean contentChanged = false;
            if (request.getTitle() != null && !request.getTitle().equals(reflection.getTitle())) {
                reflection.setTitle(request.getTitle());
                contentChanged = true;
            }
            if (request.getDescription() != null && !request.getDescription().equals(reflection.getDescription())) {
                reflection.setDescription(request.getDescription());
                contentChanged = true;
            }
            if (request.getTags() != null) {
                reflection.setTags(request.getTags());
            }
            if (request.getLocation() != null) {
                reflection.setLocation(request.getLocation());
            }
            if (request.getPhotoDate() != null) {
                reflection.setPhotoDate(request.getPhotoDate());
            }
            if (request.getLatitude() != null) {
                reflection.setLatitude(request.getLatitude());
            }
            if (request.getLongitude() != null) {
                reflection.setLongitude(request.getLongitude());
            }
            
            reflection.setVisible(request.isVisible());
            reflection.setUpdatedDate(LocalDateTime.now());
            
            // Eliminar archivos indicados
            final double[] spaceFreed = {0.0}; // Array para hacer la variable mutable en lambda
            if (filesToDelete != null && !filesToDelete.isEmpty()) {
                System.out.println("🗑️ Eliminando " + filesToDelete.size() + " archivos");
                
                for (FileDeleteRequest fDel : filesToDelete) {
                    try {
                        UUID fileId = UUID.fromString(fDel.getId());
                        
                        reflection.getFiles().stream()
                            .filter(f -> f.getIdFile().equals(fileId))
                            .findFirst()
                            .ifPresentOrElse(fileEntity -> {
                                try {
                                    spaceFreed[0] += fileEntity.getFileSize() != null ? fileEntity.getFileSize() : 0.0;
                                    storageService.deleteFile(fileEntity);
                                    reflection.getFiles().remove(fileEntity);
                                    System.out.println("✅ Archivo eliminado: " + fileEntity.getFileName());
                                } catch (Exception e) {
                                    System.err.println("⚠️ Error eliminando archivo " + fileEntity.getFileName() + ": " + e.getMessage());
                                }
                            }, () -> {
                                System.out.println("⚠️ No se encontró archivo con ID: " + fileId);
                            });
                            
                    } catch (IllegalArgumentException e) {
                        System.err.println("❌ ID inválido (no es un UUID): " + fDel.getId());
                    }
                }
            }
            
            // Procesar archivos nuevos
            List<File> newFiles = new ArrayList<>();
            double spaceUsed = 0.0;
            if (files != null && files.length > 0) {
                System.out.println("📎 Agregando " + files.length + " archivos nuevos");
                
                // Validar espacio disponible
                double totalFilesSize = storageService.calculateTotalFilesSize(files);
                storageService.validateUserStorageCapacity(user, totalFilesSize);
                
                // Procesar archivos y agregarlos a la reflexión
                newFiles = storageService.processFiles(files, reflection);
                spaceUsed = storageService.calculateTotalSpace(newFiles);
                
                newFiles.forEach(reflection::addFile);
            }
            
            // Reclasificar si cambió el contenido
            if (contentChanged) {
                try {
                    System.out.println("🤖 Re-clasificando reflexión actualizada...");
                    var classificationResult = aiService.clasificar(reflection.getTitle(), reflection.getDescription());
                    
                    if (classificationResult != null) {
                        reflection.setCategorias(
                            classificationResult.getOrDefault("categorias", List.of("otros"))
                                .stream()
                                .map(this::safeCat)
                                .filter(Objects::nonNull)
                                .distinct()
                                .limit(3)
                                .collect(Collectors.toCollection(ArrayList::new))
                        );
                        
                        reflection.setMomentos(
                            classificationResult.getOrDefault("momentos", List.of("cotidiano"))
                                .stream()
                                .map(this::safeMom)
                                .filter(Objects::nonNull)
                                .distinct()
                                .limit(3)
                                .collect(Collectors.toCollection(ArrayList::new))
                        );
                        
                        System.out.println("✅ Re-clasificación exitosa");
                    }
                    
                    // Re-evaluar línea de tiempo
                    boolean vaEnTimeline = aiService.debeIrEnLineaTiempo(
                        reflection.getTitle(),
                        reflection.getDescription(),
                        reflection.getPhotoDate()
                    );
                    reflection.setEsLineaTiempo(vaEnTimeline);
                    
                } catch (Exception e) {
                    System.err.println("⚠️ Error en re-clasificación: " + e.getMessage());
                    // Mantener las categorías existentes si falla
                }
            }
            
            // Actualizar espacio total usado de la reflexión
            if (reflection.getFiles() != null) {
                Double totalSpace = storageService.calculateTotalSpace(reflection.getFiles());
                reflection.setTotalUsedSpace(totalSpace);
            }
            
            // Actualizar espacio del usuario
            double netSpaceChange = spaceUsed - spaceFreed[0];
            if (netSpaceChange > 0) {
                storageService.increaseUserUsedSpace(user, netSpaceChange);
            } else if (netSpaceChange < 0) {
                storageService.decreaseUserUsedSpace(user, Math.abs(netSpaceChange));
            }
            
            // Guardar cambios
            Memory savedReflection = memoryRepository.save(reflection);
            
            // Construir respuesta
            MemoryResponse response = buildResponse(savedReflection, savedReflection.getFiles());
            
            System.out.println("✅ Reflexión actualizada exitosamente: " + reflectionId);
            return response;
            
        } catch (Exception e) {
            System.err.println("❌ Error editando reflexión: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating reflection: " + e.getMessage(), e);
        }
    }

}