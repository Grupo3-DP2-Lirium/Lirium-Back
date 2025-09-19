package org.example.springboot_backend.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8081}")
    private String serverPort;

    @Override
    public StorageResult uploadFile(MultipartFile file, String folder) {
        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            Path uploadPath = Paths.get(uploadDir, folder);
            Files.createDirectories(uploadPath);
            
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);
            
            String storagePath = folder + "/" + uniqueFileName;
            String fileUrl = "http://localhost:" + serverPort + "/api/files/download/" + storagePath;
            Double fileSize = (double) file.getSize() / (1024 * 1024);
            
            return new StorageResult(uniqueFileName, storagePath, fileUrl, fileSize);
            
        } catch (IOException e) {
            return StorageResult.error("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String storagePath) {
        try {
            Path filePath = Paths.get(uploadDir, storagePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + storagePath, e);
        }
    }

    @Override
    public String getFileUrl(String storagePath) {
        return "http://localhost:" + serverPort + "/api/files/download/" + storagePath;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}