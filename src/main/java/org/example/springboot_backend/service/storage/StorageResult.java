package org.example.springboot_backend.service.storage;

public class StorageResult {
    private String fileName;
    private String storagePath;
    private String fileUrl;
    private Double fileSize;
    private boolean success;
    private String errorMessage;

    public StorageResult() {}

    public StorageResult(String fileName, String storagePath, String fileUrl, Double fileSize) {
        this.fileName = fileName;
        this.storagePath = storagePath;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.success = true;
    }

    public static StorageResult error(String errorMessage) {
        StorageResult result = new StorageResult();
        result.success = false;
        result.errorMessage = errorMessage;
        return result;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Double getFileSize() {
        return fileSize;
    }

    public void setFileSize(Double fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}