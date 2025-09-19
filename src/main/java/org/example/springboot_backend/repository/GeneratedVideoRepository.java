package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.GeneratedVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GeneratedVideoRepository extends JpaRepository<GeneratedVideo, UUID> {
}