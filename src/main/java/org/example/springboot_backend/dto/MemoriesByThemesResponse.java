package org.example.springboot_backend.dto;

import java.util.List;
import java.util.Map;

public class MemoriesByThemesResponse {
    private Map<String, List<MemoryResponse>> memoriesByTheme;
    private Map<String, Integer> countByTheme;
    private List<String> availableThemes;
    private int totalMemories;

    public MemoriesByThemesResponse() {}

    public MemoriesByThemesResponse(Map<String, List<MemoryResponse>> memoriesByTheme,
                                  Map<String, Integer> countByTheme,
                                  List<String> availableThemes,
                                  int totalMemories) {
        this.memoriesByTheme = memoriesByTheme;
        this.countByTheme = countByTheme;
        this.availableThemes = availableThemes;
        this.totalMemories = totalMemories;
    }

    // Getters and setters
    public Map<String, List<MemoryResponse>> getMemoriesByTheme() { return memoriesByTheme; }
    public void setMemoriesByTheme(Map<String, List<MemoryResponse>> memoriesByTheme) { 
        this.memoriesByTheme = memoriesByTheme; 
    }

    public Map<String, Integer> getCountByTheme() { return countByTheme; }
    public void setCountByTheme(Map<String, Integer> countByTheme) { this.countByTheme = countByTheme; }

    public List<String> getAvailableThemes() { return availableThemes; }
    public void setAvailableThemes(List<String> availableThemes) { this.availableThemes = availableThemes; }

    public int getTotalMemories() { return totalMemories; }
    public void setTotalMemories(int totalMemories) { this.totalMemories = totalMemories; }
}