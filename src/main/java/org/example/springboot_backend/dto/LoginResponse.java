package org.example.springboot_backend.dto;

import java.util.List;

public class LoginResponse {
    private String token;
    private String email;
    private String role;
    private String plan;
    private List<String> permissions;

    public LoginResponse(String token, String email, String role, String plan, List<String> permissions) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.plan = plan;
        this.permissions = permissions;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}