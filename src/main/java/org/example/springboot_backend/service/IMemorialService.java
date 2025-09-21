package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.MemorialRequest;
import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.User;
import org.springframework.web.multipart.MultipartFile;


public interface IMemorialService {
    MemorialResponse createMemorial(MemorialRequest memorial, MultipartFile file, User user);
}
