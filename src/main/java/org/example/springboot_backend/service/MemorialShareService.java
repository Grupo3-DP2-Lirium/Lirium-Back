package org.example.springboot_backend.service;

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
    
    @Value("${app.share.base-url}")
    private String baseUrl;
    
    public MemorialShareService(MemorialShareRepository memorialShareRepository, 
                               MemorialRepository memorialRepository,
                               MemoryRepository memoryRepository) {
        this.memorialShareRepository = memorialShareRepository;
        this.memorialRepository = memorialRepository;
        this.memoryRepository = memoryRepository;
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
        dto.setCoverURL(memorial.getCoverURL());
        dto.setBirthDate(memorial.getBirthDate() != null ? memorial.getBirthDate().toString() : null);
        dto.setGender(memorial.getGender());
        
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
        return dto;
    }
}
