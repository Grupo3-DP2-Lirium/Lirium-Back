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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
            // Decodifica %2F → /
            storagePath = URLDecoder.decode(storagePath, StandardCharsets.UTF_8);
            storagePath = storagePath.startsWith("/") ? storagePath.substring(1) : storagePath;

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(storagePath);

            boolean deleted = blobClient.deleteIfExists();
            System.out.println("Intentando eliminar: " + storagePath + " -> deleted: " + deleted);
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


    //PARA DOCUMENTALES
    /**
     * Descarga un archivo de Azure Blob Storage a una ruta local en disco
     * @param blobPath Ruta del blob en Azure (ej: "user-xxx/memorials/xxx/memories/xxx/file.jpg")
     * @param localPath Ruta local donde guardar el archivo
     */
    public void downloadBlobToFile(String blobPath, Path localPath) throws IOException {
        try {
            // Limpiar el path
            blobPath = blobPath.replace("\\", "/");
            blobPath = blobPath.startsWith("/") ? blobPath.substring(1) : blobPath;

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobPath);

            // Verificar si existe
            if (!blobClient.exists()) {
                throw new RuntimeException("Blob not found in Azure: " + blobPath);
            }

            // Crear directorios padre si no existen
            Files.createDirectories(localPath.getParent());

            // Descargar el archivo
            try (InputStream inputStream = blobClient.openInputStream();
                 FileOutputStream outputStream = new FileOutputStream(localPath.toFile())) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            System.out.println("✅ Downloaded from Azure: " + blobPath + " -> " + localPath);

        } catch (Exception e) {
            System.err.println("❌ Error downloading blob: " + blobPath + " - " + e.getMessage());
            throw new IOException("Failed to download blob from Azure: " + e.getMessage(), e);
        }
    }

    // SUBIR ARCHIVO LOCAL A AZURE ✨
    /**
     * Sube un archivo local a Azure Blob Storage
     * @param localFilePath Ruta del archivo local
     * @param blobPath Ruta destino en Azure (ej: "documentaries/memorial-id/documentary-id.mp4")
     * @return URL pública del archivo subido
     */
    public String uploadFileToBlob(Path localFilePath, String blobPath) throws IOException {
        try {
            if (!Files.exists(localFilePath)) {
                throw new IOException("Local file does not exist: " + localFilePath);
            }

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobPath);

            // Detectar MIME type
            String mimeType = Files.probeContentType(localFilePath);
            if (mimeType == null) {
                mimeType = "video/mp4"; // Default para videos
            }

            // Configurar headers
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(mimeType)
                    .setContentDisposition("inline; filename=\"" + localFilePath.getFileName().toString() + "\"");

            // Subir el archivo (sobrescribir si existe)
            blobClient.uploadFromFile(localFilePath.toString(), true);
            blobClient.setHttpHeaders(headers);

            String fileUrl = blobClient.getBlobUrl();

            System.out.println("✅ Uploaded to Azure: " + localFilePath + " -> " + fileUrl);

            return fileUrl;

        } catch (Exception e) {
            System.err.println("❌ Error uploading file to Azure: " + localFilePath + " - " + e.getMessage());
            throw new IOException("Failed to upload file to Azure: " + e.getMessage(), e);
        }
    }


}