package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.*;
import org.example.springboot_backend.entity.User;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IMemoryService {
    MemoryResponse createMemory(MemoryCreateRequest request, MultipartFile[] files, User author);
    Page<MemoryResponse> listByMemorial(UUID idMemory, int page, int size);
    List<MemoryResponse> listByAuthor(User author);
    MemoryResponse updateMemory(UUID memoryId, MemoryCreateRequest request, MultipartFile[] files, List<FileDeleteRequest> filesToDelete, User author);

    MemoriesByTypeResponse getMemoriesByType(UUID memorialId, User user);
    java.util.Map<String, java.util.Map<String, java.util.List<MemoryLiteResponse>>>
    listGroupedByCategoryAndType(UUID memorialId, int page, int size);
    java.util.Map<String, java.util.Map<String, java.util.List<MemoryLiteResponse>>>
    listGroupedByMomentsAndType(UUID memorialId, int page, int size);
    List<MemoryResponse> findTimelineMemories(UUID memorialId, int page, int size);
}