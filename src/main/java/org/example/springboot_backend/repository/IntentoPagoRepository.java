package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.IntentoPago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentoPagoRepository extends JpaRepository<IntentoPago, Long> {
}
