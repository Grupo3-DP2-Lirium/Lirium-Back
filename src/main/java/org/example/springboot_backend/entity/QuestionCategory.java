package org.example.springboot_backend.entity;

import jakarta.persistence.*;

@Entity
public class QuestionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idQuestionCategory;

    @Column(nullable = false, unique = true)
    private String name;

    // getters and setters
    public Long getIdQuestionCategory() { return idQuestionCategory; }
    public void setIdQuestionCategory(Long idQuestionCategory) { this.idQuestionCategory = idQuestionCategory; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}