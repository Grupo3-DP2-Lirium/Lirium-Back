package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.GeneratedVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedVideoRepository extends JpaRepository<GeneratedVideo, Long> {
}