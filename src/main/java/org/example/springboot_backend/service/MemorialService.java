package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.FileResponse;
import org.example.springboot_backend.dto.MemorialRequest;
import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;


@Service
@Transactional
public class MemorialService implements IMemorialService {

    @Autowired
    private MemorialRepository memorialRepository;

    @Autowired
    private StorageService storageService;

    // Create a new Memorial with optional profile photo
    @Override
    public MemorialResponse createMemorial(MemorialRequest request, MultipartFile profilePhoto, User user) {

        // Validate storage if photo exists
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            double photoSize = profilePhoto.getSize(); // in bytes
            storageService.validateUserStorageCapacity(user, photoSize);
        }

        // Build Memorial entity
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

        // Handle profile photo
        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            // Upload photo and get File
            File uploadedFile = storageService.processSingleFile(profilePhoto, memorial);

            // Link photo to memorial
            memorial.setProfilePhoto(uploadedFile);

            // Update user storage usage
            storageService.increaseUserUsedSpace(user, profilePhoto.getSize());

            // Save again with photo
            memorial = memorialRepository.save(memorial);
        }

        return buildResponse(memorial);
    }

    // List memorials for a user
    @Override
    public java.util.List<MemorialResponse> getMyMemorials(User user) {
        java.util.List<Memorial> memorials = memorialRepository.findByUser(user);
        return memorials.stream().map(this::buildResponse).toList();
    }

    // List memorials where user is a collaborator
    @Override
    public java.util.List<MemorialResponse> getCollaborativeMemorials(User user) {
        java.util.List<Memorial> memorials = memorialRepository.findMemorialsByCollaborator(user);
        return memorials.stream().map(this::buildResponse).toList();
    }

    private MemorialResponse buildResponse(Memorial memorial) {
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

        // Add profile photo if exists
        if (memorial.getProfilePhoto() != null) {
            File file = memorial.getProfilePhoto();
            response.setProfilePhoto(buildFileResponse(file));
        }

        return response;
    }

    // Original method
    /*private FileResponse buildFileResponse(File file) {
        FileResponse response = new FileResponse();
        response.setIdFile(file.getIdFile());
        response.setFileName(file.getFileName());
        response.setOriginalFileName(file.getOriginalFileName());
        response.setFileType(file.getFileType());
        response.setMimeType(file.getMimeType());
        response.setFileUrl(file.getFileUrl());
        response.setFileSize(file.getFileSize());
        response.setUploadedDate(file.getUploadedDate());
        return response;
    }*/

    private FileResponse buildFileResponse(File file) {
        FileResponse response = new FileResponse();
        response.setIdFile(file.getIdFile());
        response.setFileName(file.getFileName());
        response.setOriginalFileName(file.getOriginalFileName());
        response.setFileType(file.getFileType());
        response.setMimeType(file.getMimeType());
        response.setFileSize(file.getFileSize());
        response.setUploadedDate(file.getUploadedDate());

        return response;
    }


}
