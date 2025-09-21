package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.Memorial;

import java.util.List;
import java.util.UUID;

public interface IMemorialService {
    MemorialResponse createMemorial(Memorial memorial, User user);
    List<MemorialResponse> getMemorialsByUser(User user);
    MemorialResponse getMemorialById(UUID id);
    void deleteMemorial(UUID id);
}
