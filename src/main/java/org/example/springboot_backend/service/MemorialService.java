package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.FileResponse;
import org.example.springboot_backend.dto.MemorialRequest;
import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.repository.CollaboratorRepository;
import org.example.springboot_backend.repository.InviteCodeRepository;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.MemorialShareRepository;
import org.example.springboot_backend.repository.MemoryRepository;
import org.example.springboot_backend.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private InviteCodeRepository inviteCodeRepository;

    @Autowired
    private MemorialShareRepository memorialShareRepository;

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
    public Page<MemorialResponse> getMyMemorials(User user, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Memorial> memorials =
                memorialRepository.findByUserAndIsJournalFalse(user, pageable);

        return memorials.map(m -> buildResponse(m, user));
    }


    @Override
    public List<MemorialResponse> getCollaborativeMemorials(User user) {
        List<Memorial> memorials = memorialRepository.findMemorialsByCollaborator(user);
        // Calcular isOwner para cada memorial
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

            List<MemorialShare> memorialShares = memorialShareRepository.findByMemorial(memorial);
            if (!memorialShares.isEmpty()) {
                System.out.println("🔗 Eliminando " + memorialShares.size() + " enlaces compartidos...");
                memorialShareRepository.deleteAll(memorialShares);
                System.out.println("✅ Enlaces compartidos eliminados");
            }

            // ✅ PASO 1: Eliminar todos los INVITE CODES asociados
            List<InviteCode> inviteCodes = inviteCodeRepository.findByMemorial(memorial);
            if (!inviteCodes.isEmpty()) {
                System.out.println("📋 Eliminando " + inviteCodes.size() + " códigos de invitación...");
                inviteCodeRepository.deleteAll(inviteCodes);
                System.out.println("✅ Códigos eliminados");
            }

            // ✅ PASO 2: Eliminar todos los COLABORADORES
            List<Collaborator> collaborators = collaboratorRepository.findByMemorial(memorial);
            if (!collaborators.isEmpty()) {
                System.out.println("👥 Eliminando " + collaborators.size() + " colaboradores...");
                collaboratorRepository.deleteAll(collaborators);
                System.out.println("✅ Colaboradores eliminados");
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
        
        if (!isOwner) {
        collaboratorRepository.findByUserAndMemorialAndIsActiveTrue(currentUser, memorial)
                .ifPresent(collaborator -> {
                    response.setCanEdit(collaborator.getCanEdit());
                });
            }
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