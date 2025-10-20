package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.FileDeleteRequest;
import org.example.springboot_backend.dto.FileResponse;
import org.example.springboot_backend.dto.MemoryCreateRequest;
import org.example.springboot_backend.dto.MemoryResponse;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.example.springboot_backend.enums.CategoriaEnum;
import org.example.springboot_backend.enums.MomentoEnum;


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
        validateRequest(request, author);
        
        // Validar espacio disponible antes de procesar archivos
        if (files != null && files.length > 0) {
            double totalFilesSize = storageService.calculateTotalFilesSize(files);
            storageService.validateUserStorageCapacity(author, totalFilesSize);
        }
        
        Memorial memorial = memorialRepository.findById(request.getMemorialId())
            .orElseThrow(() -> new RuntimeException("Memorial not found"));

        Memory memory = buildMemoryFromRequest(request, memorial, author);

        //llama a IA y setea categorías/momentos antes de guardar
        if ((request.getTitle() != null && !request.getTitle().isBlank()) ||
                (request.getDescription() != null && !request.getDescription().isBlank())) {

            var out = aiService.clasificar(request.getTitle(), request.getDescription());

            memory.setCategorias(
                    out.getOrDefault("categorias", List.of("otros"))
                            .stream().map(this::safeCat)
                            .distinct().limit(3).toList()
            );

            memory.setMomentos(
                    out.getOrDefault("momentos", List.of("cotidiano"))
                            .stream().map(this::safeMom)
                            .distinct().limit(3).toList()
            );
        }



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

    private CategoriaEnum safeCat(String s){
        try { return CategoriaEnum.valueOf(s.trim().toUpperCase()); }
        catch(Exception e){ return CategoriaEnum.OTROS; }
    }

    private MomentoEnum safeMom(String s){
        try { return MomentoEnum.valueOf(s.trim().toUpperCase()); }
        catch(Exception e){ return MomentoEnum.COTIDIANO; }
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
            var out = aiService.clasificar(
                    memory.getTitle(),
                    memory.getDescription()
            );
            memory.setCategorias(
                    out.getOrDefault("categorias", List.of("otros"))
                            .stream().map(this::safeCat).distinct().limit(3).toList()
            );
            memory.setMomentos(
                    out.getOrDefault("momentos", List.of("cotidiano"))
                            .stream().map(this::safeMom).distinct().limit(3).toList()
            );
        }



        // Guardar cambios en DB
        memoryRepository.save(memory);

        // Construir respuesta
        return buildResponse(memory, memory.getFiles());
    }

}