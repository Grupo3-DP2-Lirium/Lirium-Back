package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
}