package org.example.springboot_backend.dto;

public class ShareLinkResponse {
    
    private String url;
    private String slug;
    
    // Constructors
    public ShareLinkResponse() {
    }
    
    public ShareLinkResponse(String url, String slug) {
        this.url = url;
        this.slug = slug;
    }
    
    // Getters and Setters
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
}
