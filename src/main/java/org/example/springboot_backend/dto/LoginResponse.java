package org.example.springboot_backend.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String fullName;
    private String name;
    private String role;
    private Double usedSpace;
    private Double totalCapacity;  
    private Integer documentariesPurchased;
    private Integer documentariesAvailable;

    public LoginResponse(String token, String email, String fullname, 
                    String name, String role, Double usedSpace, Double totalCapacity,
                    Integer documentariesPurchased, Integer documentariesAvailable) {
        this.token = token;
        this.email = email;
        this.fullName = fullname;
        this.name = name;
        this.role = role;
        this.usedSpace = usedSpace;
        this.totalCapacity = totalCapacity;
        this.documentariesPurchased = documentariesPurchased;
        this.documentariesAvailable = documentariesAvailable;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Double getUsedSpace() { return usedSpace; }
    public void setUsedSpace(Double usedSpace) { this.usedSpace = usedSpace; }
    public Double getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Double totalCapacity) { this.totalCapacity = totalCapacity; }
    public Integer getDocumentariesPurchased() { return documentariesPurchased; }
    public void setDocumentariesPurchased(Integer documentariesPurchased) { this.documentariesPurchased = documentariesPurchased; }
    public Integer getDocumentariesAvailable() { return documentariesAvailable; }
    public void setDocumentariesAvailable(Integer documentariesAvailable) { this.documentariesAvailable = documentariesAvailable; }
}
