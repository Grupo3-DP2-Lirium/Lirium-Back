package org.example.springboot_backend.service;
import org.example.springboot_backend.controller.SubscriptionResponse;
import org.example.springboot_backend.dto.LoginRequest;
import org.example.springboot_backend.dto.LoginResponse;
import org.example.springboot_backend.dto.RegisterUserDTO;
import org.example.springboot_backend.dto.UserExtraStorageResponse;
import org.example.springboot_backend.dto.UserResponseDTO;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.UserExtraStorage;
import org.example.springboot_backend.entity.ExtraStoragePlan;
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
    private final ExtraStorageService extraStorageService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, 
                      JwtService jwtService, AuthenticationManager authenticationManager,
                      PasswordEncoder passwordEncoder, SubscriptionService subscriptionService, PlanRepository planRepository,
                      ExtraStorageService extraStorageService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
        this.planRepository = planRepository;
        this.extraStorageService = extraStorageService;
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

        String fullName = user.getFullName();
        String name = user.getFirstName();

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

        SubscriptionResponse subscriptionResponse = new SubscriptionResponse();

        if (activeSub != null) {
            subscriptionResponse = new SubscriptionResponse();
            subscriptionResponse.setSubscriptionId(activeSub.getIdSubscription());
            subscriptionResponse.setStatus(activeSub.getStatus());
            subscriptionResponse.setFrequency(activeSub.getFrequency());
            subscriptionResponse.setStartDate(activeSub.getStartDate());
            subscriptionResponse.setEndDate(activeSub.getEndDate());
            subscriptionResponse.setPaymentMethod(activeSub.getCurrentPaymentMethod());
            
            // Datos del plan
            subscriptionResponse.setPlanId(activeSub.getPlan().getIdPlan());
            subscriptionResponse.setPlanName(activeSub.getPlan().getName());
            subscriptionResponse.setPlanDescription(activeSub.getPlan().getDescription());
            subscriptionResponse.setPlanPrice(activeSub.getPlan().getPrice());
            subscriptionResponse.setPlanCurrency(activeSub.getPlan().getCurrency());
            subscriptionResponse.setStorageLimitGb(activeSub.getPlan().getStorageLimitGb());
            subscriptionResponse.setMaxFiles(activeSub.getPlan().getMaxFiles());
            subscriptionResponse.setMaxCollaborations(activeSub.getPlan().getMaxCollaborations());
            subscriptionResponse.setMaxDocumentariesPerMonth(activeSub.getPlan().getMaxDocumentariesPerMonth());

        }

        if (activeSub != null && activeSub.getPlan() != null) {
            Plan plan = activeSub.getPlan(); // Solo aquí, dentro del if
            System.out.println("Suscripción activa encontrada: " + activeSub.getIdSubscription());
            System.out.println("Plan del usuario: " + plan.getName());

            // Llenar SubscriptionResponse
            subscriptionResponse.setPlanId(plan.getIdPlan());
            subscriptionResponse.setPlanName(plan.getName());
            subscriptionResponse.setPlanDescription(plan.getDescription());
            subscriptionResponse.setPlanPrice(plan.getPrice());
            subscriptionResponse.setPlanCurrency(plan.getCurrency());
            subscriptionResponse.setStorageLimitGb(plan.getStorageLimitGb());
            subscriptionResponse.setMaxFiles(plan.getMaxFiles());
            subscriptionResponse.setMaxCollaborations(plan.getMaxCollaborations());
            subscriptionResponse.setMaxDocumentariesPerMonth(plan.getMaxDocumentariesPerMonth());

            permissions = subscriptionService.getPlanPermissions(plan.getIdPlan());
            System.out.println("Permisos del plan: " + permissions);

        } else {
            // Usuario sin suscripción → plan FREE
            Plan freePlan = planRepository.findByName("DESCUBRE_LIRIUM")
                    .orElseThrow(() -> new RuntimeException("Plan DESCUBRE_LIRIUM no encontrado"));

            System.out.println("Usuario sin suscripción activa. Se asigna plan FREE");

            // Llenar SubscriptionResponse con datos del plan FREE
            subscriptionResponse.setPlanId(freePlan.getIdPlan());
            subscriptionResponse.setPlanName(freePlan.getName());
            subscriptionResponse.setPlanDescription(freePlan.getDescription());
            subscriptionResponse.setPlanPrice(freePlan.getPrice());
            subscriptionResponse.setPlanCurrency(freePlan.getCurrency());
            subscriptionResponse.setStorageLimitGb(freePlan.getStorageLimitGb());
            subscriptionResponse.setMaxFiles(freePlan.getMaxFiles());
            subscriptionResponse.setMaxCollaborations(freePlan.getMaxCollaborations());
            subscriptionResponse.setMaxDocumentariesPerMonth(freePlan.getMaxDocumentariesPerMonth());

            permissions = subscriptionService.getPlanPermissions(freePlan.getIdPlan());
        }

        System.out.println("Plan final enviado al cliente: " + subscriptionResponse.getPlanName());


        System.out.println("Plan final enviado al cliente: " + planName);
        System.out.println("Nombre del cliente: " + fullName);
        
         // --- Extra Storage ---
        List<UserExtraStorage> extraStorages = extraStorageService.getActiveExtraStorageSubscriptions(user);
        List<UserExtraStorageResponse> extraStorageDTOs = extraStorages.stream()
                .map(us -> new UserExtraStorageResponse(
                        us.getPlan().getName(),
                        us.getPlan().getAdditionalStorageGb(),
                        us.getStatus().name(),
                        us.getStartDate().toLocalDate()
                ))
                .toList();
        
        // Imprimir los detalles de las suscripciones de almacenamiento extra
        System.out.println("Suscripciones de almacenamiento extra activas:");
        for (UserExtraStorageResponse dto : extraStorageDTOs) {
            System.out.println("Plan: " + dto.getPlanName() +
                            ", Almacenamiento adicional: " + dto.getAdditionalStorageGb() + "GB" +
                            ", Estado: " + dto.getStatus());
        }

        return new LoginResponse(token, user.getEmail(), fullName, name, roleName, subscriptionResponse, permissions, extraStorageDTOs);
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