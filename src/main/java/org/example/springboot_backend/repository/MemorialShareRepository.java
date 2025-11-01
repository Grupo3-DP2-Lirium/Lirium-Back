package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.MemorialShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemorialShareRepository extends JpaRepository<MemorialShare, UUID> {
    
    /**
     * Busca un memorial compartido por su slug único
     * @param slug el identificador único del enlace compartido
     * @return Optional con el MemorialShare si existe
     */
    Optional<MemorialShare> findBySlug(String slug);
}
