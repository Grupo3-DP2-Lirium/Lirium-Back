package org.example.springboot_backend.dto;

import java.util.List;
import java.util.Map;

public class MemoriesByTypeResponse {
    private Map<String, List<MemoryResponse>> memoriesByType;
    private Map<String, Integer> countByType;
    private int totalMemories;

    public MemoriesByTypeResponse() {}

    public MemoriesByTypeResponse(Map<String, List<MemoryResponse>> memoriesByType,
                                  Map<String, Integer> countByType, int totalMemories) {
        this.memoriesByType = memoriesByType;
        this.countByType = countByType;
        this.totalMemories = totalMemories;
    }

    // Getters and setters
    public Map<String, List<MemoryResponse>> getMemoriesByType() { return memoriesByType; }
    public void setMemoriesByType(Map<String, List<MemoryResponse>> memoriesByType) {
        this.memoriesByType = memoriesByType;
    }

    public Map<String, Integer> getCountByType() { return countByType; }
    public void setCountByType(Map<String, Integer> countByType) { this.countByType = countByType; }

    public int getTotalMemories() { return totalMemories; }
    public void setTotalMemories(int totalMemories) { this.totalMemories = totalMemories; }
}