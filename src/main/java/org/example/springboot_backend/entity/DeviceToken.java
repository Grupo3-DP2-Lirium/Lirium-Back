package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_tokens")
public class DeviceToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false, unique = true, length = 500)
    private String fcmToken;
    
    @Column(nullable = false)
    private String deviceType; // "android" o "ios"
    
    @Column
    private String deviceId;
    
    @Column(nullable = false)
    private LocalDateTime createdDate;
    
    @Column(nullable = false)
    private LocalDateTime lastUsedDate;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastUsedDate() { return lastUsedDate; }
    public void setLastUsedDate(LocalDateTime lastUsedDate) { this.lastUsedDate = lastUsedDate; }
}