package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
}