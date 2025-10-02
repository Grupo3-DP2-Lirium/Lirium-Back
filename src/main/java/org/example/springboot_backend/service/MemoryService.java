package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.FileResponse;
import org.example.springboot_backend.dto.MemoryCreateRequest;
import org.example.springboot_backend.dto.MemoryResponse;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.enums.MemoryOriginType;
import org.example.springboot_backend.repository.*;
import org.example.springboot_backend.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    // AGREGAR: Inyectar la ruta base desde las properties
    @Value("${app.storage.base-path:D:\\DP2\\}")
    private String basePath;

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
                r.setFiles(memory.getFiles().stream().map(f -> {
                    FileResponse fr = new FileResponse();
                    fr.setIdFile(f.getIdFile());
                    fr.setFileName(f.getFileName());
                    fr.setOriginalFileName(f.getOriginalFileName());
                    fr.setFileType(f.getFileType());
                    fr.setMimeType(f.getMimeType());

                    String fileUrl = f.getFileUrl();
                    if (!fileUrl.startsWith("D:") && !fileUrl.startsWith("D\\")) {
                        fileUrl = basePath + "\\" + fileUrl;
                    }
                    // Normalizar TODAS las barras a backslash
                    fileUrl = fileUrl.replace("/", "\\");
                    fr.setFileUrl(fileUrl);

                    fr.setFileSize(f.getFileSize() != null ? f.getFileSize() / (1024 * 1024) : 0.0); // Convert bytes to MB
                    fr.setUploadedDate(f.getUploadedDate());
                    return fr;
                }).toList());
            } else {
                r.setFiles(List.of());
            }

            return r;
        });
    }

    private void validateRequest(MemoryCreateRequest request, User author) {
        if (request.getType() == MemoryOriginType.QUESTION_RESPONSE) {
            if (request.getQuestionId() == null || request.getAnswerId() == null) {
                throw new RuntimeException("QuestionId and AnswerId are required for QUESTION_RESPONSE type");
            }
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

    private FileResponse buildFileResponse(File file) {
        FileResponse response = new FileResponse();
        response.setIdFile(file.getIdFile());
        response.setFileName(file.getFileName());
        response.setOriginalFileName(file.getOriginalFileName());
        response.setFileType(file.getFileType());
        response.setMimeType(file.getMimeType());

        String fileUrl = file.getFileUrl();

        // Agregar base path si no existe
        if (!fileUrl.startsWith("D:") && !fileUrl.startsWith("D\\")) {
            fileUrl = basePath + "\\" + fileUrl;
        }

        // ✅ Normalizar TODAS las barras a backslash
        fileUrl = fileUrl.replace("/", "\\");

        response.setFileUrl(fileUrl);

        response.setFileSize(file.getFileSize() != null ? file.getFileSize() / (1024 * 1024) : 0.0); // Convert bytes to MB
        response.setUploadedDate(file.getUploadedDate());
        return response;
    }
}