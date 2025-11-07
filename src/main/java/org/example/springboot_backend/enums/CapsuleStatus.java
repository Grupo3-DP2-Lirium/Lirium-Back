package org.example.springboot_backend.enums;

public enum CapsuleStatus {
    DRAFT,          // Borrador - recién creado
    PROCESSING,     // En proceso de generación
    COMPLETED,      // Completado, listo para ver
    PUBLISHED,      // Publicado en el memorial
    FAILED,         // Falló el proceso
    CANCELLED       // Cancelado por el usuario
}