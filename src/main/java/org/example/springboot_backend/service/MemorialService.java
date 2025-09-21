package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.MemorialRequest;
import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.MemorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemorialService implements IMemorialService {

    @Autowired
    private MemorialRepository memorialRepository;

    @Override
    public MemorialResponse createMemorial(MemorialRequest memorial, MultipartFile file, User author) {
        
        Memorial saved = memorialRepository.save(memorial);
        return buildResponse(saved);
    }
    
}
