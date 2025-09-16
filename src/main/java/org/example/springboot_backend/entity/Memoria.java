package org.example.springboot_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Memoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMemoria;

    @ManyToOne(optional = false)
    private Memorial memorial;

    private String tipo; // texto, foto, audio, video

    @ManyToOne
    private Pregunta preguntaAsociada;

    @ManyToOne
    private Respuesta respuesta;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private String ubicacion;

    @ManyToOne
    private Usuario autor;

    private String contenidoUrl; // para video, foto, audio

    // getters and setters
    public Long getIdMemoria() { return idMemoria; }
    public void setIdMemoria(Long idMemoria) { this.idMemoria = idMemoria; }
    public Memorial getMemorial() { return memorial; }
    public void setMemorial(Memorial memorial) { this.memorial = memorial; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Pregunta getPreguntaAsociada() { return preguntaAsociada; }
    public void setPreguntaAsociada(Pregunta preguntaAsociada) { this.preguntaAsociada = preguntaAsociada; }
    public Respuesta getRespuesta() { return respuesta; }
    public void setRespuesta(Respuesta respuesta) { this.respuesta = respuesta; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }
    public String getContenidoUrl() { return contenidoUrl; }
    public void setContenidoUrl(String contenidoUrl) { this.contenidoUrl = contenidoUrl; }
}
