package org.example.springboot_backend.service.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
}
