package org.example.springboot_backend.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.springboot_backend.dto.RegisterUserDTO;
import org.example.springboot_backend.dto.UserResponseDTO;
import org.example.springboot_backend.entity.Role;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.UserStatus;
import org.example.springboot_backend.exception.EmailAlreadyExistsException;
import org.example.springboot_backend.exception.RoleNotFoundException;
import org.example.springboot_backend.repository.RoleRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO registerUser(RegisterUserDTO registerUserDTO) {
        // Check if email already exists
        if (userRepository.findByEmail(registerUserDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists: " + registerUserDTO.getEmail());
        }

        // Fetch the USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException("Role not found: USER"));

        // Create new User entity
        User newUser = new User();
        newUser.setFirstName(registerUserDTO.getFirstName());
        newUser.setFirstLastName(registerUserDTO.getFirstLastName());
        newUser.setSecondLastName(registerUserDTO.getSecondLastName());
        newUser.setEmail(registerUserDTO.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(registerUserDTO.getPassword()));
        newUser.setCreatedDate(LocalDate.now());
        newUser.setUpdatedDate(LocalDate.now());
        newUser.setStatus(UserStatus.ACTIVE);
        
        // Set roles
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);

        // Save the user to the database
        User savedUser = userRepository.save(newUser);

        // Convert to UserResponseDTO and return
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setIdUser(savedUser.getIdUser());
        responseDTO.setEmail(savedUser.getEmail());
        responseDTO.setFirstName(savedUser.getFirstName());
        responseDTO.setFirstLastName(savedUser.getFirstLastName());
        responseDTO.setSecondLastName(savedUser.getSecondLastName());
        responseDTO.setCreatedDate(savedUser.getCreatedDate());
        responseDTO.setStatus(savedUser.getStatus());
        responseDTO.setRoles(savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        
        return responseDTO;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> createUser(String email, String password, 
                                                          String firstName, String firstLastName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if email already exists
            if (userRepository.findByEmail(email).isPresent()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "Email already exists: " + email);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Fetch the USER role
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RoleNotFoundException("Role not found: USER"));

            // Create new User entity
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPasswordHash(passwordEncoder.encode(password));
            newUser.setFirstName(firstName);
            newUser.setFirstLastName(firstLastName);
            newUser.setCreatedDate(LocalDate.now());
            newUser.setUpdatedDate(LocalDate.now());
            newUser.setStatus(UserStatus.ACTIVE);
            
            // Set roles
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            newUser.setRoles(roles);

            // Save the user to the database
            User savedUser = userRepository.save(newUser);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "User created successfully");
            response.put("user", Map.of(
                "id", savedUser.getIdUser().toString(),
                "email", savedUser.getEmail(),
                "firstName", savedUser.getFirstName(),
                "firstLastName", savedUser.getFirstLastName(),
                "roles", savedUser.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()),
                "status", savedUser.getStatus().toString(),
                "createdDate", savedUser.getCreatedDate().toString()
            ));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> createTestUser() {
        return createUser(
            "test@example.com", 
            "password123", 
            "Test", 
            "User"
        );
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("No user exists with email " + email));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
