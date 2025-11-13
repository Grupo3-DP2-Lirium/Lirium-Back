package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByIdUser(UUID idUser);
    long countByStatus(UserStatus status);
}
