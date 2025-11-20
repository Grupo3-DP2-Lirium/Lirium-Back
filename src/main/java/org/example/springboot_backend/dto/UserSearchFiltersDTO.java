package org.example.springboot_backend.dto;

import org.example.springboot_backend.enums.UserStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO para los filtros de búsqueda y filtrado de usuarios
 */
public class UserSearchFiltersDTO {
    private String search; // Búsqueda por nombre o email
    private UserStatus status; // Filtro por estado
    private String role; // Filtro por rol
    private String planType; // Filtro por tipo de plan
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdDateFrom; // Filtro por fecha de registro desde
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdDateTo; // Filtro por fecha de registro hasta
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lastSessionFrom; // Filtro por última sesión desde
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate lastSessionTo; // Filtro por última sesión hasta
    
    private Double usedSpaceMin; // Filtro por espacio usado mínimo (en MB)
    private Double usedSpaceMax; // Filtro por espacio usado máximo (en MB)
    
    // Parámetros de paginación
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "firstName"; // Campo por el cual ordenar
    private String sortDirection = "asc"; // Dirección del ordenamiento (asc/desc)

    // Constructores
    public UserSearchFiltersDTO() {}

    // Getters y Setters
    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public LocalDate getCreatedDateFrom() { return createdDateFrom; }
    public void setCreatedDateFrom(LocalDate createdDateFrom) { this.createdDateFrom = createdDateFrom; }

    public LocalDate getCreatedDateTo() { return createdDateTo; }
    public void setCreatedDateTo(LocalDate createdDateTo) { this.createdDateTo = createdDateTo; }

    public LocalDate getLastSessionFrom() { return lastSessionFrom; }
    public void setLastSessionFrom(LocalDate lastSessionFrom) { this.lastSessionFrom = lastSessionFrom; }

    public LocalDate getLastSessionTo() { return lastSessionTo; }
    public void setLastSessionTo(LocalDate lastSessionTo) { this.lastSessionTo = lastSessionTo; }

    public Double getUsedSpaceMin() { return usedSpaceMin; }
    public void setUsedSpaceMin(Double usedSpaceMin) { this.usedSpaceMin = usedSpaceMin; }

    public Double getUsedSpaceMax() { return usedSpaceMax; }
    public void setUsedSpaceMax(Double usedSpaceMax) { this.usedSpaceMax = usedSpaceMax; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page != null ? page : 0; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size != null && size > 0 ? size : 10; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { 
        this.sortBy = sortBy != null && !sortBy.trim().isEmpty() ? sortBy : "firstName"; 
    }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { 
        this.sortDirection = "desc".equalsIgnoreCase(sortDirection) ? "desc" : "asc"; 
    }

    // Métodos de utilidad
    public boolean hasSearch() {
        return search != null && !search.trim().isEmpty();
    }

    public boolean hasFilters() {
        return status != null || role != null || planType != null || 
               createdDateFrom != null || createdDateTo != null ||
               lastSessionFrom != null || lastSessionTo != null ||
               usedSpaceMin != null || usedSpaceMax != null;
    }
}