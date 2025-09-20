package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idFile;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String fileType; // "image", "video", "audio", "document", "text"

    @Column(nullable = false)
    private String mimeType; // "image/jpeg", "video/mp4", "audio/mpeg", etc.

    @Column(nullable = false)
    private String fileUrl; // URL general donde está almacenado el archivo

    @Column(nullable = false)
    private Double fileSize; // Tamaño en MB

    @Column
    private String storageProvider; // "local", "s3", "cloudinary", etc. - para futuro uso

    @Column
    private String storagePath; // Ruta específica en el proveedor de almacenamiento

    @Column(nullable = false)
    private LocalDateTime uploadedDate;

    @Column
    private LocalDateTime updatedDate;

    // Relación opcional - puede asociarse a diferentes entidades
    @ManyToOne
    @JoinColumn(name = "id_memory")
    private Memory memory;

    // Constructor por defecto
    public File() {
        this.uploadedDate = LocalDateTime.now();
    }

    // Constructor con parámetros básicos
    public File(String fileName, String originalFileName, String fileType, String mimeType, String fileUrl, Double fileSize) {
        this();
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
    }

    // Getters and setters
    public UUID getIdFile() { return idFile; }
    public void setIdFile(UUID idFile) { this.idFile = idFile; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Double getFileSize() { return fileSize; }
    public void setFileSize(Double fileSize) { this.fileSize = fileSize; }

    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String storageProvider) { this.storageProvider = storageProvider; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public LocalDateTime getUploadedDate() { return uploadedDate; }
    public void setUploadedDate(LocalDateTime uploadedDate) { this.uploadedDate = uploadedDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public Memory getMemory() { return memory; }
    public void setMemory(Memory memory) { this.memory = memory; }

    // Métodos de utilidad
    public boolean isImage() {
        return "image".equalsIgnoreCase(this.fileType);
    }

    public boolean isVideo() {
        return "video".equalsIgnoreCase(this.fileType);
    }

    public boolean isAudio() {
        return "audio".equalsIgnoreCase(this.fileType);
    }

    public boolean isDocument() {
        return "document".equalsIgnoreCase(this.fileType);
    }
}