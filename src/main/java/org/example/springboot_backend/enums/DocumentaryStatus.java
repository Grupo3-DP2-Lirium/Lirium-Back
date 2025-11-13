package org.example.springboot_backend.enums;

public enum DocumentaryStatus {
    DRAFT,          // Borrador - recién creado o editado
    PROCESSING,     // En proceso de generación
    COMPLETED,      // Completado pero no publicado aún
    PUBLISHED,      // Publicado en el perfil
    FAILED,         // Falló el proceso
    CANCELLED       // Cancelado por el usuario
}