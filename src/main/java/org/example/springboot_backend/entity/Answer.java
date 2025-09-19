package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAnswer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @ManyToOne
    @JoinColumn(name = "id_question", nullable = false)
    private Question question;

    // getters and setters
    public Long getIdAnswer() { return idAnswer; }
    public void setIdAnswer(Long idAnswer) { this.idAnswer = idAnswer; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
}