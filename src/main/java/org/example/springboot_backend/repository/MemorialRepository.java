package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface MemorialRepository extends JpaRepository<Memorial, UUID> {
    List<Memorial> findByUser(User user);
    List<Memorial> findByIsCollaborativeTrue();
}
