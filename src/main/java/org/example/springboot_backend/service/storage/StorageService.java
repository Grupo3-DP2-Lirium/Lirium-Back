package org.example.springboot_backend.service.storage;

import java.util.ArrayList;
import java.util.List;

import org.example.springboot_backend.entity.*;
import org.example.springboot_backend.exception.InsufficientStorageException;
import org.example.springboot_backend.repository.FileRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${app.storage.provider:local}")
    private String storageProvider;

    /**
     * Validates if the user has enough storage space for a new file
     * @param user The user who wants to upload the file
     * @param additionalSpace The size of the new file in bytes
     * @throws InsufficientStorageException if the user does not have enough space
     */

    public void validateUserStorageCapacity(User user, double additionalSpace) {
        double currentUsedSpace = user.getUsedSpace() != null ? user.getUsedSpace() : 0.0;
        double totalCapacity = user.getTotalCapacity() != null ? user.getTotalCapacity() : 0.0;
        double newUsedSpace = currentUsedSpace + additionalSpace;

        if (newUsedSpace > totalCapacity) {
            double availableSpace = totalCapacity - currentUsedSpace;
            double requiredSpaceMB = additionalSpace / (1024 * 1024); // convert bytes to MB
            double availableSpaceMB = availableSpace / (1024 * 1024); // convert bytes to MB

            throw new InsufficientStorageException(
                    String.format(
                            "Insufficient storage space. Required: %.2f MB, Available: %.2f MB. " +
                                    "Please free up space or upgrade your storage plan.",
                            requiredSpaceMB, availableSpaceMB
                    )
            );
        }
    }

    /**
     * Increases the user's used storage after uploading a file
     * @param user The user who uploaded the file
     * @param additionalSpace The size of the uploaded file in bytes
     */
    public void increaseUserUsedSpace(User user, double additionalSpace) {
        double currentUsedSpace = user.getUsedSpace() != null ? user.getUsedSpace() : 0.0;
        user.setUsedSpace(currentUsedSpace + additionalSpace);
        userRepository.save(user);
    }

    /**
     * Decreases the user's used storage after deleting a file
     * @param user The user who deleted the file
     * @param removedSpace The size of the deleted file in bytes
     */
    public void decreaseUserUsedSpace(User user, double removedSpace) {
        double currentUsedSpace = user.getUsedSpace() != null ? user.getUsedSpace() : 0.0;
        user.setUsedSpace(Math.max(0.0, currentUsedSpace - removedSpace));
        userRepository.save(user);
    }

    // Calculate the total size of an array of files in bytes.
    public double calculateTotalFilesSize(MultipartFile[] files) {
        double totalSize = 0;
        for (MultipartFile file : files) {
            totalSize += file.getSize();
        }
        return totalSize;
    }

    // Calculate the total size of a list of File entities in bytes.
    public double calculateTotalSpace(List<org.example.springboot_backend.entity.File> files) {
        return files.stream().mapToDouble(f -> f.getFileSize() != null ? f.getFileSize() : 0).sum();
    }

    // Upload multiple files and save them to the database for a Memory
    public List<File> processFiles(MultipartFile[] files, Memory memory) {
        List<File> savedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            // Build folder path: user-{userId}/memorials/{memorialId}/memories/{memoryId}
            String folder = String.format("user-%s/memorials/%s/memories/%s",
                    memory.getAuthor().getIdUser(),
                    memory.getMemorial().getIdMemorial(),
                    memory.getIdMemory());

            System.out.println("DEBUG - Processing file: " + file.getOriginalFilename());
            System.out.println("DEBUG - Folder path: " + folder);
            System.out.println("DEBUG - Memory ID: " + memory.getIdMemory());
            System.out.println("DEBUG - Memorial ID: " + memory.getMemorial().getIdMemorial());
            System.out.println("DEBUG - User ID: " + memory.getAuthor().getIdUser());

            StorageResult result = fileStorageService.uploadFile(file, folder);

            if (result.isSuccess()) {
                File fileEntity = new File();
                fileEntity.setFileName(result.getFileName());
                fileEntity.setOriginalFileName(file.getOriginalFilename());
                fileEntity.setMimeType(file.getContentType());
                fileEntity.setFileType(determineFileType(file.getContentType()));
                fileEntity.setFileUrl(result.getFileUrl());
                fileEntity.setFileSize(result.getFileSize());
                fileEntity.setStorageProvider("azure");
                fileEntity.setStoragePath(result.getStoragePath());
                fileEntity.setMemory(memory);

                savedFiles.add(fileRepository.save(fileEntity));
            }
        }

        return savedFiles;
    }

    // Upload a single file (profile photo) for a memorial and save it to the database
    public File processSingleFile(MultipartFile file, Memorial memorial) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("No file provided for upload");
        }

        // Build folder path: user-{userId}/memorials/{memorialId}/profile
        String folder = String.format("user-%s/memorials/%s/profile",
                memorial.getUser().getIdUser(),
                memorial.getIdMemorial());

        // Upload the file
        StorageResult result = fileStorageService.uploadFile(file, folder);

        if (!result.isSuccess()) {
            throw new RuntimeException("Failed to upload file: " + result.getErrorMessage());
        }

        // Create File entity and save
        File fileEntity = new File();
        fileEntity.setFileName(result.getFileName());
        fileEntity.setOriginalFileName(file.getOriginalFilename());
        fileEntity.setMimeType(file.getContentType());
        fileEntity.setFileType(determineFileType(file.getContentType()));
        fileEntity.setFileUrl(result.getFileUrl());
        fileEntity.setFileSize(result.getFileSize());
        fileEntity.setStorageProvider("azure");
        fileEntity.setStoragePath(result.getStoragePath());

        return fileRepository.save(fileEntity);
    }


    // Determine file type based on MIME type.
    public String determineFileType(String mimeType) {
        if (mimeType == null) return "unknown";

        if (mimeType.startsWith("image/")) return "image";
        if (mimeType.startsWith("video/")) return "video";
        if (mimeType.startsWith("audio/")) return "audio";
        if (mimeType.startsWith("text/") || mimeType.contains("document")) return "document";

        return "unknown";
    }
}