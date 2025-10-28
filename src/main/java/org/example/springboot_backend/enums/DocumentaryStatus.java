package org.example.springboot_backend.enums;

public enum DocumentaryStatus {
    PENDING,        // Recién creado, esperando procesamiento
    PROCESSING,     // En proceso de generación
    COMPLETED,      // Completado exitosamente
    FAILED,         // Falló el proceso
    CANCELLED       // Cancelado por el usuario
}