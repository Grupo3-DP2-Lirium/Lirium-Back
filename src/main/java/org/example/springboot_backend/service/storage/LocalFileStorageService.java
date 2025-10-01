package org.example.springboot_backend.service.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final String baseFolder = "D:\\DP2";

    @Override
    public StorageResult uploadFile(MultipartFile file, String folder) {
        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path folderPath = Paths.get(baseFolder, folder);
            Files.createDirectories(folderPath);

            Path filePath = folderPath.resolve(uniqueFileName);
            file.transferTo(filePath.toFile());

            String fileUrl = folder + "/" + uniqueFileName;
            Double fileSize = (double) file.getSize() / (1024 * 1024);

            return new StorageResult(uniqueFileName, filePath.toString(), fileUrl, fileSize);

        } catch (IOException e) {
            return StorageResult.error("Failed to upload file locally: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String storagePath) {
        File file = new File(storagePath);
        if (file.exists()) file.delete();
    }

    @Override
    public String getFileUrl(String storagePath) {
        return storagePath;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String folder, String fileName) {
        try {
            // Construye la ruta de forma segura
            Path filePath = Paths.get(baseFolder).resolve(folder).resolve(fileName).normalize();

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] data = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(data);

            // Detecta el tipo MIME
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
