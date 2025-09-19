package org.example.springboot_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Memoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMemoria;

    @ManyToOne(optional = false)
    private Memorial memorial;

    private String tipo; // texto, foto, audio, video

    private String preguntaAsociada;

    @ManyToOne
    private Pregunta pregunta;

    @ManyToOne
    private Respuesta respuesta;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private LocalDate fechaFoto;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private String ubicacion;
    private boolean visible;

    @ManyToOne
    private Usuario autor;

    private String s3keyURL; // para video, foto, audio
    private Double espacioUsado;
    
    @ElementCollection
    private List<String> tags;

    // getters and setters
    public Long getIdMemoria() { return idMemoria; }
    public void setIdMemoria(Long idMemoria) { this.idMemoria = idMemoria; }
    public Memorial getMemorial() { return memorial; }
    public void setMemorial(Memorial memorial) { this.memorial = memorial; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getPreguntaAsociada() { return preguntaAsociada; }
    public void setPreguntaAsociada(String preguntaAsociada) { this.preguntaAsociada = preguntaAsociada; }
    public Pregunta getPregunta() { return pregunta; }
    public void setPregunta(Pregunta pregunta) { this.pregunta = pregunta; }
    public Respuesta getRespuesta() { return respuesta; }
    public void setRespuesta(Respuesta respuesta) { this.respuesta = respuesta; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaFoto() { return fechaFoto; }
    public void setFechaFoto(LocalDate fechaFoto) { this.fechaFoto = fechaFoto; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }
    public String getS3keyURL() { return s3keyURL; }
    public void setS3keyURL(String s3keyURL) { this.s3keyURL = s3keyURL; }
    public Double getEspacioUsado() { return espacioUsado; }
    public void setEspacioUsado(Double espacioUsado) { this.espacioUsado = espacioUsado; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
