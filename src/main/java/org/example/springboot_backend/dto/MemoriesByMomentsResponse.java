package org.example.springboot_backend.dto;

import java.util.List;
import java.util.Map;

public class MemoriesByMomentsResponse {
    private Map<String, List<MemoryResponse>> memoriesByMoment;
    private Map<String, Integer> countByMoment;
    private List<String> availableMoments;
    private int totalMemories;

    public MemoriesByMomentsResponse() {}

    public MemoriesByMomentsResponse(Map<String, List<MemoryResponse>> memoriesByMoment,
                                   Map<String, Integer> countByMoment,
                                   List<String> availableMoments,
                                   int totalMemories) {
        this.memoriesByMoment = memoriesByMoment;
        this.countByMoment = countByMoment;
        this.availableMoments = availableMoments;
        this.totalMemories = totalMemories;
    }

    // Getters and setters
    public Map<String, List<MemoryResponse>> getMemoriesByMoment() { return memoriesByMoment; }
    public void setMemoriesByMoment(Map<String, List<MemoryResponse>> memoriesByMoment) { 
        this.memoriesByMoment = memoriesByMoment; 
    }

    public Map<String, Integer> getCountByMoment() { return countByMoment; }
    public void setCountByMoment(Map<String, Integer> countByMoment) { this.countByMoment = countByMoment; }

    public List<String> getAvailableMoments() { return availableMoments; }
    public void setAvailableMoments(List<String> availableMoments) { this.availableMoments = availableMoments; }

    public int getTotalMemories() { return totalMemories; }
    public void setTotalMemories(int totalMemories) { this.totalMemories = totalMemories; }
}