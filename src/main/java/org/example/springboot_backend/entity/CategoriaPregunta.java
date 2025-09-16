package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CategoriaPregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoriaPregunta;

    @Column(nullable = false, unique = true)
    private String nombre;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pregunta> preguntas = new ArrayList<>();

    // getters and setters
    public Long getIdCategoriaPregunta() { return idCategoriaPregunta; }
    public void setIdCategoriaPregunta(Long idCategoriaPregunta) { this.idCategoriaPregunta = idCategoriaPregunta; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<Pregunta> getPreguntas() { return preguntas; }
    public void setPreguntas(List<Pregunta> preguntas) { this.preguntas = preguntas; }
}
