package org.example.springboot_backend.enums;

public enum InviteCodeStatusEnum {
    ACTIVE,    // Código activo y disponible
    EXPIRED,   // Código expirado (más de 24h)
    REVOKED,   // Código revocado manualmente por el dueño
    USED       // Código alcanzó su límite de usos
}