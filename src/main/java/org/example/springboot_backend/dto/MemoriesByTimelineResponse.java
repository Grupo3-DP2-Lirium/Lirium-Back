package org.example.springboot_backend.dto;

import java.util.List;
import java.util.Map;

public class MemoriesByTimelineResponse {
    private Map<String, Map<String, List<MemoryResponse>>> memoriesByTimeline; // year -> month -> memories
    private Map<String, Integer> countByYear;
    private Map<String, Map<String, Integer>> countByMonth; // year -> month -> count
    private int totalMemories;

    public MemoriesByTimelineResponse() {}

    public MemoriesByTimelineResponse(Map<String, Map<String, List<MemoryResponse>>> memoriesByTimeline,
                                    Map<String, Integer> countByYear,
                                    Map<String, Map<String, Integer>> countByMonth,
                                    int totalMemories) {
        this.memoriesByTimeline = memoriesByTimeline;
        this.countByYear = countByYear;
        this.countByMonth = countByMonth;
        this.totalMemories = totalMemories;
    }

    // Getters and setters
    public Map<String, Map<String, List<MemoryResponse>>> getMemoriesByTimeline() { 
        return memoriesByTimeline; 
    }
    public void setMemoriesByTimeline(Map<String, Map<String, List<MemoryResponse>>> memoriesByTimeline) { 
        this.memoriesByTimeline = memoriesByTimeline; 
    }

    public Map<String, Integer> getCountByYear() { return countByYear; }
    public void setCountByYear(Map<String, Integer> countByYear) { this.countByYear = countByYear; }

    public Map<String, Map<String, Integer>> getCountByMonth() { return countByMonth; }
    public void setCountByMonth(Map<String, Map<String, Integer>> countByMonth) { 
        this.countByMonth = countByMonth; 
    }

    public int getTotalMemories() { return totalMemories; }
    public void setTotalMemories(int totalMemories) { this.totalMemories = totalMemories; }
}