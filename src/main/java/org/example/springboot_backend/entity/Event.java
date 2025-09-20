package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvent;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column
    private String location;

    @ManyToOne
    @JoinColumn(name = "id_memory", nullable = false)
    private Memory memory;

    // getters and setters
    public Long getIdEvent() { return idEvent; }
    public void setIdEvent(Long idEvent) { this.idEvent = idEvent; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Memory getMemory() { return memory; }
    public void setMemory(Memory memory) { this.memory = memory; }
}