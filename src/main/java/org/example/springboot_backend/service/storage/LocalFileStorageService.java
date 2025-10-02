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

    private final String baseFolder = System.getProperty("user.dir") + "/storage";

    @Override
    public StorageResult uploadFile(MultipartFile file, String folder) {
        try {
            // Validar que el archivo no esté vacío
            if (file == null || file.isEmpty()) {
                return StorageResult.error("File is null or empty");
            }

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                return StorageResult.error("Original filename is null");
            }

            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // RUTA ABSOLUTA - Para crear directorios y guardar archivo físico
            Path absoluteFolderPath = Paths.get(baseFolder).resolve(folder).normalize();
            Path absoluteFilePath = absoluteFolderPath.resolve(uniqueFileName);
            
            System.out.println("DEBUG LocalFileStorage - Base folder: " + baseFolder);
            System.out.println("DEBUG LocalFileStorage - Folder parameter: " + folder);
            System.out.println("DEBUG LocalFileStorage - Absolute folder path: " + absoluteFolderPath.toString());
            System.out.println("DEBUG LocalFileStorage - Absolute file path: " + absoluteFilePath.toString());
            
            // Crear directorios si no existen (usando ruta absoluta)
            Files.createDirectories(absoluteFolderPath);

            // Transferir el archivo (usando ruta absoluta)
            file.transferTo(absoluteFilePath.toFile());

            // RUTA RELATIVA - Para guardar en la base de datos
            String relativeStoragePath = folder + "/" + uniqueFileName;
            
            System.out.println("DEBUG LocalFileStorage - Relative storage path for DB: " + relativeStoragePath);

            // Construir la URL relativa
            String fileUrl = folder + "/" + uniqueFileName;
            Double fileSize = (double) file.getSize(); // Keep size in bytes

            // Devolver la ruta RELATIVA en storagePath para que se guarde en BD
            return new StorageResult(uniqueFileName, relativeStoragePath, fileUrl, fileSize);

        } catch (IOException e) {
            return StorageResult.error("Failed to upload file locally: " + e.getMessage());
        } catch (Exception e) {
            return StorageResult.error("Unexpected error during file upload: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String storagePath) {
        // Si storagePath es relativo, construir la ruta absoluta
        Path absolutePath;
        if (Paths.get(storagePath).isAbsolute()) {
            // Es una ruta absoluta (para retrocompatibilidad)
            absolutePath = Paths.get(storagePath);
        } else {
            // Es una ruta relativa, construir la absoluta
            absolutePath = Paths.get(baseFolder).resolve(storagePath);
        }
        
        File file = absolutePath.toFile();
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public String getFileUrl(String storagePath) {
        // Para almacenamiento local, la URL es la misma ruta relativa
        return storagePath;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Override 
    public ResponseEntity<Resource> downloadFile(String folder, String fileName) {
        try {
            // Construye la ruta absoluta para leer el archivo
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
