package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.LoginRequest;
import org.example.springboot_backend.dto.LoginResponse;
import org.example.springboot_backend.dto.RegisterUserDTO;
import org.example.springboot_backend.dto.UserResponseDTO;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.Role;
import org.example.springboot_backend.entity.Subscription;
import org.example.springboot_backend.enums.UserStatus;
import org.example.springboot_backend.exception.UserNotFoundException;
import org.example.springboot_backend.exception.InvalidCredentialsException;
import org.example.springboot_backend.exception.EmailAlreadyExistsException;
import org.example.springboot_backend.exception.RoleNotFoundException;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.repository.PlanRepository;
import org.example.springboot_backend.repository.RoleRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;
    private final PlanRepository planRepository;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, 
                      JwtService jwtService, AuthenticationManager authenticationManager,
                      PasswordEncoder passwordEncoder, SubscriptionService subscriptionService, PlanRepository planRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
        this.planRepository = planRepository;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password. Please check your credentials and try again.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User with email '" + request.getEmail() + "' not found"));

        String token = jwtService.generateToken(user.getEmail());

        System.out.println("JWT GENERADO: " + token);
        
        // Get the first role name (assuming user has at least one role)
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");
        
        Subscription activeSub = subscriptionService.getActiveSubscription(user);

        // Default values for users without plan (Free)
        String planName = "DESCUBRE_LIRIUM";
        List<String> permissions = List.of();

        if (activeSub != null && activeSub.getPlan() != null) {

            System.out.println("Suscripción activa encontrada: " + activeSub.getIdSubscription());
            System.out.println("Plan del usuario: " + activeSub.getPlan().getName());

            planName = activeSub.getPlan().getName();
            permissions = subscriptionService.getPlanPermissions(activeSub.getPlan().getIdPlan());

            System.out.println("Permisos del plan: " + permissions);
        } else {
            // Usuario sin suscripción → plan FREE
            Plan freePlan = planRepository.findByName("DESCUBRE_REMORY")
                    .orElseThrow(() -> new RuntimeException("Plan FREE no encontrado"));
            planName = freePlan.getName();
            permissions = subscriptionService.getPlanPermissions(freePlan.getIdPlan());
            System.out.println("Usuario sin suscripción activa. Se asigna plan FREE");
        }

        System.out.println("Plan final enviado al cliente: " + planName);

        return new LoginResponse(token, user.getEmail(), roleName, planName, permissions);
    }

    public UserResponseDTO registerUser(RegisterUserDTO registerRequest) {
        // Verificar si el email ya existe
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email '" + registerRequest.getEmail() + "' is already registered");
        }

        // Buscar el rol USER
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException(
                    "System configuration error: 'USER' role does not exist in the database. " +
                    "Please contact the system administrator to resolve this issue."
                ));

        // Crear nuevo usuario
        User newUser = new User();
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setFirstLastName(registerRequest.getFirstLastName());
        newUser.setSecondLastName(registerRequest.getSecondLastName());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        
        // Configuración por defecto
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setUsedSpace(0.0);
        newUser.setTotalCapacity(10240.0 * 1024 * 1024); // 10 GB en bytes (10240 MB * 1024 * 1024)
        newUser.setCreatedDate(LocalDate.now());
        newUser.setUpdatedDate(LocalDate.now());
        
        // Asignar rol USER
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);

        // Guardar usuario
        User savedUser = userRepository.save(newUser);

        // Convertir a DTO de respuesta
        return convertToUserResponseDTO(savedUser);
    }

    private UserResponseDTO convertToUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setIdUser(user.getIdUser());
        dto.setFirstName(user.getFirstName());
        dto.setFirstLastName(user.getFirstLastName());
        dto.setSecondLastName(user.getSecondLastName());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setUsedSpace(user.getUsedSpace() != null ? user.getUsedSpace() / (1024 * 1024) : 0.0); // Convert bytes to MB
        dto.setTotalCapacity(user.getTotalCapacity() != null ? user.getTotalCapacity() / (1024 * 1024) : 0.0); // Convert bytes to MB
        dto.setCreatedDate(user.getCreatedDate());
        dto.setUpdatedDate(user.getUpdatedDate());
        dto.setLastSessionDate(user.getLastSessionDate());
        
        // Convertir roles a Set<String>
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoles(roleNames);
        
        return dto;
    }
}