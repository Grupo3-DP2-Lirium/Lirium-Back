package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.*;
import org.example.springboot_backend.entity.Role;
import org.example.springboot_backend.entity.Subscription;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtiene una página de usuarios con filtros aplicados
     */
    public Page<UserListDTO> getUsersWithFilters(UserSearchFiltersDTO filters) {
        Pageable pageable = createPageable(filters);
        
        LocalDateTime lastSessionFrom = filters.getLastSessionFrom() != null ? 
            filters.getLastSessionFrom().atStartOfDay() : null;
        LocalDateTime lastSessionTo = filters.getLastSessionTo() != null ? 
            filters.getLastSessionTo().atTime(23, 59, 59) : null;
        
        Page<User> userPage = userRepository.findUsersWithFilters(
            filters.getSearch(),
            filters.getStatus(),
            filters.getRole(),
            filters.getCreatedDateFrom(),
            filters.getCreatedDateTo(),
            lastSessionFrom,
            lastSessionTo,
            filters.getUsedSpaceMin(),
            filters.getUsedSpaceMax(),
            pageable
        );
        
        return userPage.map(this::convertToUserListDTO);
    }

    /**
     * Obtiene el detalle completo de un usuario por su ID
     */
    public Optional<UserDetailDTO> getUserDetail(UUID userId) {
        Optional<User> userOpt = userRepository.findByIdUser(userId);
        
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        
        User user = userOpt.get();
        UserDetailDTO detailDTO = convertToUserDetailDTO(user);
        
        return Optional.of(detailDTO);
    }

    /**
     * Convierte un User entity a UserListDTO
     */
    private UserListDTO convertToUserListDTO(User user) {
        // Obtener estadísticas básicas
        Long memorialsCount = userRepository.countMemorialsByUserId(user.getIdUser());
        Long memoriesCount = userRepository.countMemoriesByUserId(user.getIdUser());
        
        return new UserListDTO(
            user.getIdUser(),
            user.getFirstName(),
            user.getFirstLastName(),
            user.getSecondLastName(),
            user.getEmail(),
            null, // profilePhotoUrl - se puede implementar después
            user.getStatus(),
            getPrimaryRoleName(user),
            getActivePlanName(user),
            user.getCreatedDate(),
            user.getLastSessionDate(),
            user.getUsedSpace(),
            user.getTotalCapacity(),
            memorialsCount.intValue(),
            memoriesCount.intValue()
        );
    }

    /**
     * Convierte un User entity a UserDetailDTO
     */
    private UserDetailDTO convertToUserDetailDTO(User user) {
        // Obtener estadísticas del usuario
        UserStatisticsDTO statistics = getUserStatistics(user);
        
        // Obtener información de suscripción activa
        ActiveSubscriptionDTO activeSubscription = getActiveSubscription(user);
        
        // Obtener lista de roles
        List<String> roles = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList());
        
        return new UserDetailDTO(
            user.getIdUser(),
            user.getFirstName(),
            user.getFirstLastName(),
            user.getSecondLastName(),
            user.getEmail(),
            null, // profilePhotoUrl - se puede implementar después
            user.getStatus(),
            roles,
            getActivePlanName(user),
            user.getCreatedDate(),
            user.getUpdatedDate(),
            user.getLastSessionDate(),
            user.getUsedSpace(),
            user.getTotalCapacity(),
            statistics,
            activeSubscription
        );
    }

    /**
     * Obtiene las estadísticas del usuario
     */
    private UserStatisticsDTO getUserStatistics(User user) {
        Long memorialsCount = userRepository.countMemorialsByUserId(user.getIdUser());
        Long memoriesCount = userRepository.countMemoriesByUserId(user.getIdUser());
        Long totalFilesCount = userRepository.countFilesByUserId(user.getIdUser());
        LocalDateTime lastActivity = userRepository.findLastActivityByUserId(user.getIdUser());
        
        Long daysSinceLastActivity = null;
        if (lastActivity != null) {
            daysSinceLastActivity = ChronoUnit.DAYS.between(lastActivity.toLocalDate(), LocalDate.now());
        }
        
        Double averageMemoriesPerMemorial = 0.0;
        if (memorialsCount > 0) {
            averageMemoriesPerMemorial = memoriesCount.doubleValue() / memorialsCount.doubleValue();
        }
        
        return new UserStatisticsDTO(
            memorialsCount.intValue(),
            memoriesCount.intValue(),
            totalFilesCount.intValue(),
            lastActivity,
            daysSinceLastActivity,
            averageMemoriesPerMemorial
        );
    }

    /**
     * Obtiene información de la suscripción activa del usuario
     */
    private ActiveSubscriptionDTO getActiveSubscription(User user) {
        if (user.getSubscriptions() == null || user.getSubscriptions().isEmpty()) {
            return new ActiveSubscriptionDTO(
                "Free Plan", "FREE", null, null, true, 0.0, "ACTIVE", null
            );
        }
        
        // Buscar la suscripción más reciente y activa
        Optional<Subscription> activeSubscription = user.getSubscriptions().stream()
            .filter(s -> s.getEndDate() == null || s.getEndDate().isAfter(LocalDateTime.now()))
            .max((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()));
        
        if (activeSubscription.isEmpty()) {
            return new ActiveSubscriptionDTO(
                "Free Plan", "FREE", null, null, true, 0.0, "ACTIVE", null
            );
        }
        
        Subscription subscription = activeSubscription.get();
        Long daysRemaining = null;
        
        if (subscription.getEndDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());
        }
        
        // Convertir LocalDateTime a LocalDate para el DTO
        LocalDate startDate = subscription.getStartDate() != null ? subscription.getStartDate().toLocalDate() : null;
        LocalDate endDate = subscription.getEndDate() != null ? subscription.getEndDate().toLocalDate() : null;
        
        return new ActiveSubscriptionDTO(
            subscription.getPlan() != null ? subscription.getPlan().getName() : "Unknown Plan",
            subscription.getPlan() != null ? subscription.getPlan().getName() : "UNKNOWN",
            startDate,
            endDate,
            subscription.getEndDate() == null || subscription.getEndDate().isAfter(LocalDateTime.now()),
            subscription.getPlan() != null ? subscription.getPlan().getPrice() : 0.0,
            subscription.getEndDate() == null || subscription.getEndDate().isAfter(LocalDateTime.now()) ? "ACTIVE" : "EXPIRED",
            daysRemaining
        );
    }

    /**
     * Obtiene el rol principal del usuario (prioriza ADMIN > PREMIUM > USER)
     */
    private String getPrimaryRoleName(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return "USER";
        }
        
        List<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList());
        
        if (roleNames.contains("ADMIN")) return "ADMIN";
        if (roleNames.contains("PREMIUM")) return "PREMIUM";
        return roleNames.get(0);
    }

    /**
     * Obtiene el nombre del plan activo del usuario
     */
    private String getActivePlanName(User user) {
        if (user.getSubscriptions() == null || user.getSubscriptions().isEmpty()) {
            return "Free Plan";
        }
        
        Optional<Subscription> activeSubscription = user.getSubscriptions().stream()
            .filter(s -> s.getEndDate() == null || s.getEndDate().isAfter(LocalDateTime.now()))
            .max((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()));
        
        return activeSubscription
            .map(s -> s.getPlan() != null ? s.getPlan().getName() : "Free Plan")
            .orElse("Free Plan");
    }

    /**
     * Crea el objeto Pageable para la paginación y ordenamiento
     */
    private Pageable createPageable(UserSearchFiltersDTO filters) {
        Sort.Direction direction = "desc".equalsIgnoreCase(filters.getSortDirection()) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        Sort sort = Sort.by(direction, filters.getSortBy());
        
        return PageRequest.of(filters.getPage(), filters.getSize(), sort);
    }

    /**
     * Obtiene todos los usuarios (sin filtros ni paginación) - para casos específicos
     */
    public List<UserListDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(this::convertToUserListDTO)
            .collect(Collectors.toList());
    }

    /**
     * Cuenta el total de usuarios en el sistema
     */
    public long getTotalUsersCount() {
        return userRepository.count();
    }
}