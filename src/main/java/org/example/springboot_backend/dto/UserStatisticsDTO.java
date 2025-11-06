package org.example.springboot_backend.dto;

import java.time.LocalDateTime;

/**
 * DTO para mostrar estadísticas del usuario
 */
public class UserStatisticsDTO {
    private Integer memorialsCount;
    private Integer memoriesCount;
    private Integer totalFilesCount;
    private LocalDateTime lastActivityDate;
    private Long daysSinceLastActivity;
    private Double averageMemoriesPerMemorial;

    // Constructores
    public UserStatisticsDTO() {}

    public UserStatisticsDTO(Integer memorialsCount, Integer memoriesCount, Integer totalFilesCount,
                             LocalDateTime lastActivityDate, Long daysSinceLastActivity,
                             Double averageMemoriesPerMemorial) {
        this.memorialsCount = memorialsCount;
        this.memoriesCount = memoriesCount;
        this.totalFilesCount = totalFilesCount;
        this.lastActivityDate = lastActivityDate;
        this.daysSinceLastActivity = daysSinceLastActivity;
        this.averageMemoriesPerMemorial = averageMemoriesPerMemorial;
    }

    // Getters y Setters
    public Integer getMemorialsCount() { return memorialsCount; }
    public void setMemorialsCount(Integer memorialsCount) { this.memorialsCount = memorialsCount; }

    public Integer getMemoriesCount() { return memoriesCount; }
    public void setMemoriesCount(Integer memoriesCount) { this.memoriesCount = memoriesCount; }

    public Integer getTotalFilesCount() { return totalFilesCount; }
    public void setTotalFilesCount(Integer totalFilesCount) { this.totalFilesCount = totalFilesCount; }

    public LocalDateTime getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(LocalDateTime lastActivityDate) { this.lastActivityDate = lastActivityDate; }

    public Long getDaysSinceLastActivity() { return daysSinceLastActivity; }
    public void setDaysSinceLastActivity(Long daysSinceLastActivity) { this.daysSinceLastActivity = daysSinceLastActivity; }

    public Double getAverageMemoriesPerMemorial() { return averageMemoriesPerMemorial; }
    public void setAverageMemoriesPerMemorial(Double averageMemoriesPerMemorial) { this.averageMemoriesPerMemorial = averageMemoriesPerMemorial; }
}