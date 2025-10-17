package org.example.springboot_backend.dto;

import java.util.List;
import java.util.Map;

public class MemoriesOrganizedResponse {
    private List<MemoryResponse> memories;
    private Map<String, Object> metadata;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private String filterType;
    private String sortBy;

    public MemoriesOrganizedResponse() {}

    public MemoriesOrganizedResponse(List<MemoryResponse> memories, Map<String, Object> metadata, 
                                   int totalElements, int totalPages, int currentPage, 
                                   String filterType, String sortBy) {
        this.memories = memories;
        this.metadata = metadata;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.filterType = filterType;
        this.sortBy = sortBy;
    }

    // Getters and setters
    public List<MemoryResponse> getMemories() { return memories; }
    public void setMemories(List<MemoryResponse> memories) { this.memories = memories; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public String getFilterType() { return filterType; }
    public void setFilterType(String filterType) { this.filterType = filterType; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
}