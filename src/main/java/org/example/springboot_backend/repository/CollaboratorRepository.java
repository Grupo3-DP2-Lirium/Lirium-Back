package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Collaborator;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {
    
    List<Collaborator> findByMemorialAndIsActiveTrue(Memorial memorial);
    
    List<Collaborator> findByUserAndIsActiveTrue(User user);
    
    List<Collaborator> findByMemorial(Memorial memorial);

    Optional<Collaborator> findByUserAndMemorialAndIsActiveTrue(User user, Memorial memorial);
    
    boolean existsByUserAndMemorialAndIsActiveTrue(User user, Memorial memorial);
}