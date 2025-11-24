package org.example.springboot_backend.service;

import java.util.List;

import org.example.springboot_backend.dto.MemorialRequest;
import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;


public interface IMemorialService {
    MemorialResponse createMemorial(MemorialRequest memorial, MultipartFile file, User user);
    Page<MemorialResponse> getMyMemorials(User user, int page, int size);
    List<MemorialResponse> getCollaborativeMemorials(User user);
    MemorialResponse getMemorialById(String memorialId, User user);
    MemorialResponse updateMemorial(String memorialId, MemorialRequest request, MultipartFile file, User user);
    void deleteMemorial(String memorialId, User user);
}
