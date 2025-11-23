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
        long start = System.currentTimeMillis();
        System.out.println("====== LOGIN START ======");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password. Please check your credentials and try again.");
        }
        System.out.println("Tiempo despues de AUTH: " + (System.currentTimeMillis() - start) + "ms");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User with email '" + request.getEmail() + "' not found"));
        System.out.println("Tiempo despues de BUSCAR USER: " + (System.currentTimeMillis() - start) + "ms");

        String token = jwtService.generateToken(user.getEmail());
        System.out.println("Tiempo despues de GENERAR TOKEN: " + (System.currentTimeMillis() - start) + "ms");

        String fullName = user.getFullName();
        String name = user.getFirstName();

        System.out.println("JWT GENERADO: " + token);
        
        // Get the first role name (assuming user has at least one role)
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");
        
        long end = System.currentTimeMillis();
        System.out.println("====== LOGIN END (" + (end - start) + "ms) ======");
        LoginResponse response = new LoginResponse(
            token,
            user.getEmail(),
            fullName,
            name,
            roleName,
            user.getUsedSpace(),
            user.getTotalCapacity()
        );

        // Imprimir todo lo que se está enviando
        System.out.println("LOGIN RESPONSE => " +
            "token: " + response.getToken() + ", " +
            "email: " + response.getEmail() + ", " +
            "fullName: " + response.getFullName() + ", " +
            "name: " + response.getName() + ", " +
            "role: " + response.getRole() + ", " +
            "usedSpace: " + response.getUsedSpace() + ", " +
            "totalCapacity: " + response.getTotalCapacity()
        );

        return response;
            
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
        //newUser.setTotalCapacity(10240 * 1024 * 1024); // 10 GB en bytes (10240 MB * 1024 * 1024)
        newUser.setTotalCapacity(15.0 * 1024 * 1024 * 1024);
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