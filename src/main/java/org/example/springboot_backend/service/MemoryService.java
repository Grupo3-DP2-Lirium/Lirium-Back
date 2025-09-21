package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.FileResponse;
import org.example.springboot_backend.dto.MemoryCreateRequest;
import org.example.springboot_backend.dto.MemoryResponse;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.enums.MemoryOriginType;
import org.example.springboot_backend.exception.InsufficientStorageException;
import org.example.springboot_backend.repository.*;
import org.example.springboot_backend.service.storage.FileStorageService;
import org.example.springboot_backend.service.storage.StorageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class MemoryService implements IMemoryService {

    @Autowired
    private MemoryRepository memoryRepository;

    @Autowired
    private MemorialRepository memorialRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public MemoryResponse createMemory(MemoryCreateRequest request, MultipartFile[] files, User author) {
        validateRequest(request, author);
        
        // Validar espacio disponible antes de procesar archivos
        if (files != null && files.length > 0) {
            double totalFilesSize = calculateTotalFilesSize(files);
            validateUserStorageCapacity(author, totalFilesSize);
        }
        
        Memorial memorial = memorialRepository.findById(request.getMemorialId())
            .orElseThrow(() -> new RuntimeException("Memorial not found"));

        Memory memory = buildMemoryFromRequest(request, memorial, author);
        memory = memoryRepository.save(memory);

        List<File> savedFiles = new ArrayList<>();
        if (files != null && files.length > 0) {
            savedFiles = processFiles(files, memory);
            Double totalSpace = calculateTotalSpace(savedFiles);
            memory.setTotalUsedSpace(totalSpace);
            memory = memoryRepository.save(memory);
            
            // Actualizar el espacio usado del usuario
            updateUserUsedSpace(author, totalSpace);
        }

        return buildResponse(memory, savedFiles);
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

    private List<File> processFiles(MultipartFile[] files, Memory memory) {
        List<File> savedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            // Nueva estructura: user-{userId}/memorials/{memorialId}/memories/{memoryId}
            String folder = String.format("user-%s/memorials/%s/memories/%s", 
                memory.getAuthor().getIdUser(),
                memory.getMemorial().getIdMemorial(),
                memory.getIdMemory());
            
            StorageResult result = fileStorageService.uploadFile(file, folder);
            
            if (result.isSuccess()) {
                File fileEntity = new File();
                fileEntity.setFileName(result.getFileName());
                fileEntity.setOriginalFileName(file.getOriginalFilename());
                fileEntity.setMimeType(file.getContentType());
                fileEntity.setFileType(determineFileType(file.getContentType()));
                fileEntity.setFileUrl(result.getFileUrl());
                fileEntity.setFileSize(result.getFileSize());
                fileEntity.setStorageProvider("azure");
                fileEntity.setStoragePath(result.getStoragePath());
                fileEntity.setMemory(memory);
                
                savedFiles.add(fileRepository.save(fileEntity));
            }
        }
        
        return savedFiles;
    }

    private String determineFileType(String mimeType) {
        if (mimeType == null) return "unknown";
        
        if (mimeType.startsWith("image/")) return "image";
        if (mimeType.startsWith("video/")) return "video";
        if (mimeType.startsWith("audio/")) return "audio";
        if (mimeType.startsWith("text/") || mimeType.contains("document")) return "document";
        
        return "unknown";
    }

    private Double calculateTotalSpace(List<File> files) {
        return files.stream()
            .mapToDouble(File::getFileSize)
            .sum();
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
        response.setTotalUsedSpace(memory.getTotalUsedSpace());
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
        response.setFileUrl(file.getFileUrl());
        response.setFileSize(file.getFileSize());
        response.setUploadedDate(file.getUploadedDate());
        return response;
    }

    private double calculateTotalFilesSize(MultipartFile[] files) {
        double totalSize = 0;
        for (MultipartFile file : files) {
            totalSize += file.getSize();
        }
        return totalSize;
    }

    private void validateUserStorageCapacity(User user, double additionalSpace) {
        double currentUsedSpace = user.getUsedSpace() != null ? user.getUsedSpace() : 0.0;
        double totalCapacity = user.getTotalCapacity() != null ? user.getTotalCapacity() : 0.0;
        double newUsedSpace = currentUsedSpace + additionalSpace;

        if (newUsedSpace > totalCapacity) {
            double availableSpace = totalCapacity - currentUsedSpace;
            double requiredSpaceMB = additionalSpace / (1024 * 1024); // Convert bytes to MB
            double availableSpaceMB = availableSpace / (1024 * 1024); // Convert bytes to MB
            
            throw new InsufficientStorageException(
                String.format("Insufficient storage space. Required: %.2f MB, Available: %.2f MB. " +
                    "Please free up space or upgrade your storage plan.", 
                    requiredSpaceMB, availableSpaceMB)
            );
        }
    }

    private void updateUserUsedSpace(User user, double additionalSpace) {
        double currentUsedSpace = user.getUsedSpace() != null ? user.getUsedSpace() : 0.0;
        double newUsedSpace = currentUsedSpace + additionalSpace;
        user.setUsedSpace(newUsedSpace);
        userRepository.save(user);
    }
}