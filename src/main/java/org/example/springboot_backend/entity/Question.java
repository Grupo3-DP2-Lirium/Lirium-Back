package org.example.springboot_backend.entity;

import jakarta.persistence.*;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idQuestion;

    @Column(nullable = false, length = 500)
    private String text;

    // Si es una pregunta predefinida, tiene categoría; si es propia, la categoría es null y isOwn=true
    @ManyToOne
    private QuestionCategory category;

    @Column(nullable = false)
    private boolean isOwn = false;

    // autor opcional para propias
    @ManyToOne
    private User author;

    // getters and setters
    public Long getIdQuestion() { return idQuestion; }
    public void setIdQuestion(Long idQuestion) { this.idQuestion = idQuestion; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public QuestionCategory getCategory() { return category; }
    public void setCategory(QuestionCategory category) { this.category = category; }
    public boolean isOwn() { return isOwn; }
    public void setOwn(boolean own) { isOwn = own; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
}