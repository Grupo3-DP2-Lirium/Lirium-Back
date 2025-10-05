package org.example.springboot_backend.service.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "azure")
public class AzureBlobStorageService implements FileStorageService {

    private final BlobServiceClient blobServiceClient;
    private final String containerName;

    public AzureBlobStorageService(
            @Value("${azure.storage.connection-string:}") String connectionString,
            @Value("${azure.storage.container-name:lirium-files}") String containerName) {
        this.containerName = containerName;
        
        if (connectionString == null || connectionString.trim().isEmpty()) {
            throw new IllegalArgumentException("Azure Storage connection string is required when using Azure storage provider");
        }
        
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Override
    public StorageResult uploadFile(MultipartFile file, String folder) {
        try {
            // Generar nombre único para el archivo
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // Construir la ruta completa del blob
            String blobPath = folder + "/" + uniqueFileName;
            
            // Obtener el container client
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            // Crear blob client
            BlobClient blobClient = containerClient.getBlobClient(blobPath);
            
            // Configurar headers HTTP para el blob
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType())
                    .setContentDisposition("inline; filename=\"" + originalFileName + "\"");
            
            // Subir el archivo
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);
            
            // Construir URL pública del archivo
            String fileUrl = blobClient.getBlobUrl();
            
            // Mantener tamaño en bytes
            Double fileSize = (double) file.getSize(); // Keep size in bytes
            
            return new StorageResult(uniqueFileName, blobPath, fileUrl, fileSize);
            
        } catch (IOException e) {
            return StorageResult.error("Failed to upload file to Azure Blob Storage: " + e.getMessage());
        } catch (Exception e) {
            return StorageResult.error("Unexpected error during file upload: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String storagePath) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(storagePath);
            blobClient.deleteIfExists();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from Azure Blob Storage: " + storagePath, e);
        }
    }

    @Override
    public String getFileUrl(String storagePath) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(storagePath);
        return blobClient.getBlobUrl();
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String folder, String fileName) {
        try {
            // Construir la ruta del blob
            String blobPath = folder + "/" + fileName;
            
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobPath);
            
            if (!blobClient.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // Descargar el contenido del blob
            byte[] data = blobClient.downloadContent().toBytes();
            ByteArrayResource resource = new ByteArrayResource(data);
            
            // Obtener propiedades del blob para el tipo MIME
            String mimeType = blobClient.getProperties().getContentType();
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}