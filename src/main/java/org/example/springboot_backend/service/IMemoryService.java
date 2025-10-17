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
    
    // Nuevos métodos para visualizar recuerdos organizados
    MemoriesOrganizedResponse getMemoriesOrganized(UUID memorialId, String filterType, String sortBy, String sortOrder, int page, int size, User user);
    MemoriesByTypeResponse getMemoriesByType(UUID memorialId, User user);
    MemoriesByTimelineResponse getMemoriesByTimeline(UUID memorialId, String year, String month, User user);
    MemoriesByThemesResponse getMemoriesByThemes(UUID memorialId, User user);
    MemoriesByMomentsResponse getMemoriesByMoments(UUID memorialId, User user);
}