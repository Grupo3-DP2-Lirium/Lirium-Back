package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.LoginRequest;
import org.example.springboot_backend.dto.LoginResponse;
import org.example.springboot_backend.dto.RegisterUserDTO;
import org.example.springboot_backend.dto.UserResponseDTO;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.Role;
import org.example.springboot_backend.enums.UserStatus;
import org.example.springboot_backend.exception.UserNotFoundException;
import org.example.springboot_backend.exception.InvalidCredentialsException;
import org.example.springboot_backend.exception.EmailAlreadyExistsException;
import org.example.springboot_backend.exception.RoleNotFoundException;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.repository.RoleRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, 
                      JwtService jwtService, AuthenticationManager authenticationManager,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
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
        
        // Get the first role name (assuming user has at least one role)
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");
        
        return new LoginResponse(token, user.getEmail(), roleName);
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
        newUser.setTotalCapacity(10240.0); // 10 GB en MB
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
        dto.setUsedSpace(user.getUsedSpace());
        dto.setTotalCapacity(user.getTotalCapacity());
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