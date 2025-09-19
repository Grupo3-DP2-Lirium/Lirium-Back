package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.MemoryCreateRequest;
import org.example.springboot_backend.dto.MemoryResponse;
import org.example.springboot_backend.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface IMemoryService {
    MemoryResponse createMemory(MemoryCreateRequest request, MultipartFile[] files, User author);
}