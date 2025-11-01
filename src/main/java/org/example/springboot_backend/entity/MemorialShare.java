package org.example.springboot_backend.entity;

import jakarta.persistence.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "memorial_shares", indexes = {
    @Index(name = "idx_slug", columnList = "slug", unique = true)
})
public class MemorialShare {
    
    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "memorial_id", nullable = false)
    private Memorial memorial;
    
    @Column(nullable = false, unique = true, length = 10)
    private String slug;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug();
        }
    }
    
    private String generateSlug() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return sb.toString();
    }
    
    // Constructors
    public MemorialShare() {
    }
    
    public MemorialShare(Memorial memorial) {
        this.memorial = memorial;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Memorial getMemorial() {
        return memorial;
    }
    
    public void setMemorial(Memorial memorial) {
        this.memorial = memorial;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
