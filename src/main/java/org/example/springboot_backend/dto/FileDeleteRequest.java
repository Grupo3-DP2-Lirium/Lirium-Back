package org.example.springboot_backend.dto;

public class FileDeleteRequest {
    private String id;
    private String path; // path para Azure

    // getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}
