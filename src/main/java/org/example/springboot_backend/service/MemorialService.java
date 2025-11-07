package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.FileResponse;
import org.example.springboot_backend.dto.MemorialRequest;
import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.repository.CollaboratorRepository;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.MemoryRepository;
import org.example.springboot_backend.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemorialService implements IMemorialService {
    
    @Autowired
    private MemorialRepository memorialRepository;
    
    @Autowired
    private StorageService storageService;
    
    @Autowired
    private MemoryRepository memoryRepository;
    
    @Autowired
    private MemoryService memoryService;
    
    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Override
    public MemorialResponse createMemorial(MemorialRequest request, MultipartFile profilePhoto, User user) {
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            double photoSize = profilePhoto.getSize();
            storageService.validateUserStorageCapacity(user, photoSize);
        }

        Memorial memorial = new Memorial();
        memorial.setName(request.getName());
        memorial.setNickname(request.getNickname());
        memorial.setBirthDate(request.getBirthDate());
        memorial.setGender(request.getGender());
        memorial.setDescription(request.getDescription());
        memorial.setRelationType(request.getRelationType());
        memorial.setCollaborative(request.isCollaborative());
        memorial.setJournal(request.isJournal());
        memorial.setCreatedDate(LocalDateTime.now());
        memorial.setUser(user);
        memorial = memorialRepository.save(memorial);

        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            File uploadedFile = storageService.processSingleFile(profilePhoto, memorial);
            memorial.setProfilePhoto(uploadedFile);
            storageService.increaseUserUsedSpace(user, profilePhoto.getSize());
            memorial = memorialRepository.save(memorial);
        }

        // ✅ Pasar el usuario actual para calcular isOwner
        return buildResponse(memorial, user);
    }

    @Override
    public List<MemorialResponse> getMyMemorials(User user) {
        List<Memorial> memorials = memorialRepository.findByUser(user);
        // ✅ Siempre son del usuario, así que isOwner = true
        return memorials.stream()
                .map(m -> buildResponse(m, user))
                .collect(Collectors.toList());
    }

    @Override
    public List<MemorialResponse> getCollaborativeMemorials(User user) {
        List<Memorial> memorials = memorialRepository.findMemorialsByCollaborator(user);
        // ✅ Calcular isOwner para cada memorial
        return memorials.stream()
                .map(m -> buildResponse(m, user))
                .collect(Collectors.toList());
    }

    @Override
    public MemorialResponse getMemorialById(String memorialId, User user) {
        try {
            java.util.UUID uuid = java.util.UUID.fromString(memorialId);
            Memorial memorial = memorialRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("Memorial not found with ID: " + memorialId));

            boolean isOwner = memorial.getUser().getIdUser().equals(user.getIdUser());
            boolean isCollaborator = collaboratorRepository
                    .existsByUserAndMemorialAndIsActiveTrue(user, memorial);

            if (!isOwner && !isCollaborator) {
                throw new RuntimeException("User does not have access to this memorial");
            }

            // ✅ Pasar el usuario para calcular isOwner
            return buildResponse(memorial, user);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid memorial ID format: " + memorialId);
        }
    }

    @Override
    public MemorialResponse updateMemorial(String memorialId, MemorialRequest request, 
                                          MultipartFile profilePhoto, User user) {
        try {
            java.util.UUID uuid = java.util.UUID.fromString(memorialId);
            Memorial memorial = memorialRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("Memorial not found with ID: " + memorialId));

            // ✅ Verificar si es dueño
            if (!memorial.getUser().getIdUser().equals(user.getIdUser())) {
                throw new RuntimeException("Only the owner can update this memorial");
            }

            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                double photoSize = profilePhoto.getSize();
                storageService.validateUserStorageCapacity(user, photoSize);
            }

            memorial.setName(request.getName());
            memorial.setNickname(request.getNickname());
            memorial.setBirthDate(request.getBirthDate());
            memorial.setGender(request.getGender());
            memorial.setDescription(request.getDescription());
            memorial.setRelationType(request.getRelationType());
            memorial.setCollaborative(request.isCollaborative());
            memorial.setJournal(request.isJournal());
            memorial.setUpdatedDate(LocalDateTime.now());

            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                if (memorial.getProfilePhoto() != null) {
                    File oldPhoto = memorial.getProfilePhoto();
                    double oldPhotoSize = oldPhoto.getFileSize() != null ? oldPhoto.getFileSize() : 0.0;
                    storageService.deleteFile(oldPhoto);
                    storageService.decreaseUserUsedSpace(user, oldPhotoSize);
                    memorial.setProfilePhoto(null);
                }
                
                File uploadedFile = storageService.processSingleFile(profilePhoto, memorial);
                memorial.setProfilePhoto(uploadedFile);
                storageService.increaseUserUsedSpace(user, profilePhoto.getSize());
            }

            memorial = memorialRepository.save(memorial);
            
            // ✅ Pasar el usuario para calcular isOwner
            return buildResponse(memorial, user);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid memorial ID format: " + memorialId);
        }
    }

    @Override
    public void deleteMemorial(String memorialId, User user) {
        try {
            System.out.println("Eliminando memorial: " + memorialId + " para usuario: " + user.getEmail());
            
            java.util.UUID uuid = java.util.UUID.fromString(memorialId);
            Memorial memorial = memorialRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("Memorial not found with ID: " + memorialId));

            // ✅ Solo el dueño puede eliminar
            if (!memorial.getUser().getIdUser().equals(user.getIdUser())) {
                throw new RuntimeException("User does not have permission to delete this memorial");
            }

            if (memorial.getUsedSpace() != null && memorial.getUsedSpace() > 0) {
                storageService.decreaseUserUsedSpace(user, memorial.getUsedSpace());
                System.out.println("Liberado espacio del usuario: " + (memorial.getUsedSpace() / (1024 * 1024)) + " MB");
            }

            if (memorial.getProfilePhoto() != null) {
                File profilePhoto = memorial.getProfilePhoto();
                storageService.deleteFile(profilePhoto);
                System.out.println("Foto de perfil eliminada: " + profilePhoto.getFileName());
            }

            List<Memory> associatedMemories = memoryRepository.findByMemorialIdMemorial(memorial.getIdMemorial());
            if (associatedMemories != null && !associatedMemories.isEmpty()) {
                for (Memory memory : associatedMemories) {
                    try {
                        memoryService.deleteMemory(memory.getIdMemory(), user);
                        System.out.println("✅ Memoria asociada eliminada: " + memory.getIdMemory());
                    } catch (Exception e) {
                        System.err.println("⚠️ Error eliminando memoria asociada " + memory.getIdMemory() + ": " + e.getMessage());
                    }
                }
            }

            memorialRepository.delete(memorial);
            System.out.println("Memorial eliminado exitosamente: " + memorialId);
            
        } catch (Exception e) {
            System.err.println("Error eliminando memorial: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting memorial: " + e.getMessage(), e);
        }
    }

    // ✅ NUEVO: Método helper que calcula isOwner
    private MemorialResponse buildResponse(Memorial memorial, User currentUser) {
        MemorialResponse response = new MemorialResponse();
        response.setIdMemorial(memorial.getIdMemorial());
        response.setName(memorial.getName());
        response.setNickname(memorial.getNickname());
        response.setBirthDate(memorial.getBirthDate());
        response.setGender(memorial.getGender());
        response.setDescription(memorial.getDescription());
        response.setRelationType(memorial.getRelationType());
        response.setCollaborative(memorial.isCollaborative());
        response.setJournal(memorial.isJournal());
        response.setCreatedDate(memorial.getCreatedDate());
        response.setUpdatedDate(memorial.getUpdatedDate());
        
        // ✅ CRÍTICO: Calcular isOwner comparando usuarios
        boolean isOwner = memorial.getUser().getIdUser().equals(currentUser.getIdUser());
        response.setIsOwner(isOwner);
        
        System.out.println("🔍 Memorial: " + memorial.getName() + 
                         " | Owner: " + memorial.getUser().getEmail() + 
                         " | Current: " + currentUser.getEmail() + 
                         " | isOwner: " + isOwner);

        if (memorial.getProfilePhoto() != null) {
            File file = memorial.getProfilePhoto();
            response.setProfilePhoto(buildFileResponse(file));
        }

        return response;
    }

    private FileResponse buildFileResponse(File file) {
        FileResponse response = new FileResponse();
        response.setIdFile(file.getIdFile());
        response.setFileName(file.getFileName());
        response.setOriginalFileName(file.getOriginalFileName());
        response.setFileType(file.getFileType());
        response.setMimeType(file.getMimeType());
        response.setFileUrl(file.getFileUrl());
        response.setFileSize(file.getFileSize() != null ? file.getFileSize() / (1024 * 1024) : 0.0);
        response.setUploadedDate(file.getUploadedDate());
        return response;
    }
}