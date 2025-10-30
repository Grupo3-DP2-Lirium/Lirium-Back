package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.FileResponse;
import org.example.springboot_backend.dto.PublicMemorialDto;
import org.example.springboot_backend.dto.PublicMemoryDto;
import org.example.springboot_backend.dto.ShareLinkResponse;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.MemorialShare;
import org.example.springboot_backend.entity.Memory;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.MemorialShareRepository;
import org.example.springboot_backend.repository.MemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemorialShareService {
    
    private final MemorialShareRepository memorialShareRepository;
    private final MemorialRepository memorialRepository;
    private final MemoryRepository memoryRepository;
    private final DefaultBackgroundService defaultBackgroundService;
    
    @Value("${app.share.base-url}")
    private String baseUrl;
    
    public MemorialShareService(MemorialShareRepository memorialShareRepository, 
                               MemorialRepository memorialRepository,
                               MemoryRepository memoryRepository,
                               DefaultBackgroundService defaultBackgroundService) {
        this.memorialShareRepository = memorialShareRepository;
        this.memorialRepository = memorialRepository;
        this.memoryRepository = memoryRepository;
        this.defaultBackgroundService = defaultBackgroundService;
    }
    
    /**
     * Crea un enlace compartible para un memorial
     * @param memorialId ID del memorial a compartir
     * @return ShareLinkResponse con la URL completa del enlace
     */
    @Transactional
    public ShareLinkResponse createShareLink(UUID memorialId) {
        // Buscar el memorial
        Memorial memorial = memorialRepository.findById(memorialId)
            .orElseThrow(() -> new RuntimeException("Memorial no encontrado con ID: " + memorialId));
        
        // Crear el nuevo share
        MemorialShare share = new MemorialShare(memorial);
        MemorialShare savedShare = memorialShareRepository.save(share);
        
        // Construir la URL completa
        String url = baseUrl + "/m/" + savedShare.getSlug();
        
        return new ShareLinkResponse(url, savedShare.getSlug());
    }
    
    /**
     * Obtiene la información pública de un memorial usando su slug
     * @param slug el identificador único del enlace compartido
     * @return PublicMemorialDto con la información pública del memorial y sus memorias
     */
    @Transactional(readOnly = true)
    public PublicMemorialDto getPublicMemorialBySlug(String slug) {
        // Buscar el share por slug
        MemorialShare share = memorialShareRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Enlace compartido no encontrado: " + slug));
        
        Memorial memorial = share.getMemorial();
        
        // Mapear a DTO público
        PublicMemorialDto dto = new PublicMemorialDto();
        dto.setIdMemorial(memorial.getIdMemorial());
        dto.setName(memorial.getName());
        dto.setNickname(memorial.getNickname());
        dto.setDescription(memorial.getDescription());
        dto.setBirthDate(memorial.getBirthDate() != null ? memorial.getBirthDate().toString() : null);
        dto.setGender(memorial.getGender());
        dto.setRelationType(memorial.getRelationType());
        
        // Configurar profilePhoto URL si existe
        if (memorial.getProfilePhoto() != null) {
            dto.setProfilePhotoUrl(memorial.getProfilePhoto().getFileUrl());
        }
        
        // Configurar background URL: usar coverURL si existe, sino usar fondo por defecto
        String backgroundUrl = memorial.getCoverURL();
        if (backgroundUrl == null || backgroundUrl.trim().isEmpty()) {
            backgroundUrl = defaultBackgroundService.getBackgroundForMemorial(memorial.getIdMemorial());
        }
        dto.setBackgroundUrl(backgroundUrl);
        
        // Obtener y mapear las memorias asociadas al memorial (solo visibles)
        List<Memory> memories = memoryRepository.findByMemorial_IdMemorialOrderByCreatedDateDesc(memorial.getIdMemorial());
        List<PublicMemoryDto> publicMemories = memories.stream()
            .filter(Memory::isVisible) // Solo memorias visibles
            .map(this::mapToPublicMemoryDto)
            .collect(Collectors.toList());
        dto.setMemories(publicMemories);
        
        return dto;
    }
    
    /**
     * Mapea una entidad Memory a PublicMemoryDto
     */
    private PublicMemoryDto mapToPublicMemoryDto(Memory memory) {
        PublicMemoryDto dto = new PublicMemoryDto();
        dto.setIdMemory(memory.getIdMemory());
        dto.setTitle(memory.getTitle());
        dto.setDescription(memory.getDescription());
        dto.setPhotoDate(memory.getPhotoDate() != null ? memory.getPhotoDate().toString() : null);
        dto.setLocation(memory.getLocation());
        dto.setCreatedDate(memory.getCreatedDate() != null ? memory.getCreatedDate().toString() : null);
        dto.setType(memory.getType() != null ? memory.getType().toString() : null);
        
        // Mapear archivos asociados si existen
        if (memory.getFiles() != null && !memory.getFiles().isEmpty()) {
            List<FileResponse> fileResponses = memory.getFiles().stream()
                .map(this::buildFileResponse)
                .collect(Collectors.toList());
            dto.setFiles(fileResponses);
        }
        
        return dto;
    }
    
    /**
     * Construye un FileResponse a partir de un File
     */
    private FileResponse buildFileResponse(org.example.springboot_backend.entity.File file) {
        FileResponse response = new FileResponse();
        response.setIdFile(file.getIdFile());
        response.setFileName(file.getFileName());
        response.setOriginalFileName(file.getOriginalFileName());
        response.setFileUrl(file.getFileUrl());
        response.setFileSize(file.getFileSize());
        response.setMimeType(file.getMimeType());
        response.setFileType(file.getFileType() != null ? file.getFileType().toString() : null);
        return response;
    }
}
