package org.example.springboot_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Memory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMemory;

    @ManyToOne(optional = false)
    private Memorial memorial;

    private String type; // texto, foto, audio, video

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

    private String s3keyURL;
    private Double usedSpace;

    @ElementCollection
    private List<String> tags;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // getters and setters
    public Long getIdMemory() { return idMemory; }
    public void setIdMemory(Long idMemory) { this.idMemory = idMemory; }
    public Memorial getMemorial() { return memorial; }
    public void setMemorial(Memorial memorial) { this.memorial = memorial; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
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
    public String getS3keyURL() { return s3keyURL; }
    public void setS3keyURL(String s3keyURL) { this.s3keyURL = s3keyURL; }
    public Double getUsedSpace() { return usedSpace; }
    public void setUsedSpace(Double usedSpace) { this.usedSpace = usedSpace; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}