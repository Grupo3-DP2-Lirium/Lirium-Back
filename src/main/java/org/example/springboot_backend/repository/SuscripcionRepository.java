package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
}
