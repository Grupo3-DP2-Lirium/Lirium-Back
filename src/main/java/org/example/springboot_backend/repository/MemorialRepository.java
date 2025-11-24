package org.example.springboot_backend.repository;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemorialRepository extends JpaRepository<Memorial, UUID> {
    List<Memorial> findByUser(User user);
    List<Memorial> findByUserAndIsJournalFalse(User user);
    List<Memorial> findByIsCollaborativeTrue();
    
    // Find memorials where the user is an active collaborator but NOT the owner
    @Query("SELECT m FROM Memorial m WHERE m.idMemorial IN " +
           "(SELECT c.memorial.idMemorial FROM Collaborator c WHERE c.user = :user AND c.isActive = true) " +
           "AND m.user != :user")
    List<Memorial> findMemorialsByCollaborator(@Param("user") User user);
    
    // Find user's personal reflection space (journal)
    Memorial findByUserAndIsJournalTrue(User user);
    Page<Memorial> findByUserAndIsJournalFalse(User user, Pageable pageable);

}
