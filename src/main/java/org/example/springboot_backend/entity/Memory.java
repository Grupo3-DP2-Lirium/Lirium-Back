package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.MemoryOriginType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import org.example.springboot_backend.enums.CategoriaEnum;
import org.example.springboot_backend.enums.MomentoEnum;

@Entity
@Table(name = "memories")
public class Memory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idMemory;

    @ManyToOne(optional = false)
    private Memorial memorial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoryOriginType type;

    private String associatedQuestion;

    @ManyToOne
    private Question question;

    @ManyToOne
    private Answer answer;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDate photoDate;
    private String location;
    private boolean visible;

    @ManyToOne(optional = false)
    private User author;

    @OneToMany(mappedBy = "memory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>();

    private Double totalUsedSpace; // Suma del tamaño de todos los archivos

    @ElementCollection
    private List<String> tags;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @ElementCollection(targetClass = CategoriaEnum.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "memory_categorias",
            joinColumns = @JoinColumn(name = "memory_id")
    )
    @Column(name = "categoria", length = 50, nullable = false)
    private List<CategoriaEnum> categorias = new ArrayList<>();

    @ElementCollection(targetClass = MomentoEnum.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "memory_momentos",
            joinColumns = @JoinColumn(name = "memory_id")
    )
    @Column(name = "momento", length = 50, nullable = false)
    private List<MomentoEnum> momentos = new ArrayList<>();

    @Column(name = "es_linea_tiempo")
    private Boolean esLineaTiempo = false; //no séw kjsjs


    // getters and setters
    public UUID getIdMemory() { return idMemory; }
    public void setIdMemory(UUID idMemory) { this.idMemory = idMemory; }
    public Memorial getMemorial() { return memorial; }
    public void setMemorial(Memorial memorial) { this.memorial = memorial; }
    public MemoryOriginType getType() { return type; }
    public void setType(MemoryOriginType type) { this.type = type; }
    public String getAssociatedQuestion() { return associatedQuestion; }
    public void setAssociatedQuestion(String associatedQuestion) { this.associatedQuestion = associatedQuestion; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public Answer getAnswer() { return answer; }
    public void setAnswer(Answer answer) { this.answer = answer; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getPhotoDate() { return photoDate; }
    public void setPhotoDate(LocalDate photoDate) { this.photoDate = photoDate; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public List<File> getFiles() { return files; }
    public void setFiles(List<File> files) { this.files = files; }
    public Double getTotalUsedSpace() { return totalUsedSpace; }
    public void setTotalUsedSpace(Double totalUsedSpace) { this.totalUsedSpace = totalUsedSpace; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<CategoriaEnum> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaEnum> categorias) { this.categorias = categorias; }

    public List<MomentoEnum> getMomentos() { return momentos; }
    public void setMomentos(List<MomentoEnum> momentos) { this.momentos = momentos; }

    // Métodos de utilidad para manejar archivos
    public void addFile(File file) {
        files.add(file);
        file.setMemory(this);
        updateTotalUsedSpace();
    }

    public void removeFile(File file) {
        files.remove(file);
        file.setMemory(null);
        updateTotalUsedSpace();
    }

    private void updateTotalUsedSpace() {
        this.totalUsedSpace = files.stream()
                .mapToDouble(File::getFileSize)
                .sum();
    }

    public boolean hasFiles() {
        return files != null && !files.isEmpty();
    }

    public int getFileCount() {
        return files != null ? files.size() : 0;
    }

    public Boolean getEsLineaTiempo() {
        return esLineaTiempo;
    }

    public void setEsLineaTiempo(Boolean esLineaTiempo) {
        this.esLineaTiempo = esLineaTiempo;
    }
}