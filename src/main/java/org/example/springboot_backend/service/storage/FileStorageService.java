package org.example.springboot_backend.service.storage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StorageResult uploadFile(MultipartFile file, String folder);
    void deleteFile(String storagePath);
    String getFileUrl(String storagePath);
    ResponseEntity<org.springframework.core.io.Resource> downloadFile(String folder, String fileName);
}