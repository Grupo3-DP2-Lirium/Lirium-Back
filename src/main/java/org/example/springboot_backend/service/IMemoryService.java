package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.MemoryCreateRequest;
import org.example.springboot_backend.dto.MemoryResponse;
import org.example.springboot_backend.entity.User;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IMemoryService {
    MemoryResponse createMemory(MemoryCreateRequest request, MultipartFile[] files, User author);
    Page<MemoryResponse> listByMemorial(UUID idMemory, int page, int size);
    List<MemoryResponse> listByAuthor(User author);
    MemoryResponse updateMemory(UUID memoryId, MemoryCreateRequest request, MultipartFile[] files, User author);
}