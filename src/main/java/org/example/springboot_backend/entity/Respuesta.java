package org.example.springboot_backend.entity;

import jakarta.persistence.*;

@Entity
public class Respuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRespuesta;

    @ManyToOne(optional = false)
    private Pregunta pregunta;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    // getters and setters
    public Long getIdRespuesta() { return idRespuesta; }
    public void setIdRespuesta(Long idRespuesta) { this.idRespuesta = idRespuesta; }
    public Pregunta getPregunta() { return pregunta; }
    public void setPregunta(Pregunta pregunta) { this.pregunta = pregunta; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}
