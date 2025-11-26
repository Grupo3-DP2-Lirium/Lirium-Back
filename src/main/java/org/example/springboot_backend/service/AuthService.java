package org.example.springboot_backend.service;
import org.example.springboot_backend.dto.FileResponse;
import org.example.springboot_backend.dto.LoginRequest;
import org.example.springboot_backend.dto.LoginResponse;
import org.example.springboot_backend.dto.RegisterUserDTO;
import org.example.springboot_backend.dto.UserResponseDTO;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.File;
import org.example.springboot_backend.entity.Role;
import org.example.springboot_backend.enums.UserStatus;
import org.example.springboot_backend.exception.UserNotFoundException;
import org.example.springboot_backend.exception.InvalidCredentialsException;
import org.example.springboot_backend.exception.EmailAlreadyExistsException;
import org.example.springboot_backend.exception.RoleNotFoundException;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.storage.StorageService;
import org.example.springboot_backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private StorageService storageService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, 
                      JwtService jwtService, AuthenticationManager authenticationManager,
                      PasswordEncoder passwordEncoder,
                     StorageService storageService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
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
        
        FileResponse profilePhotoResponse = toFileResponse(user.getProfilePhoto());

        LoginResponse response = new LoginResponse(
            token,
            user.getEmail(),
            fullName,
            name,
            roleName,
            user.getUsedSpace(),
            user.getTotalCapacity(),
            user.getDocumentariesPurchased(),
            user.getDocumentariesAvailable(),
            profilePhotoResponse
        );

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

    private FileResponse toFileResponse(File file) {
        if (file == null) {
            return null;
        }

        FileResponse response = new FileResponse();
        response.setIdFile(file.getIdFile());
        response.setFileName(file.getFileName());
        response.setOriginalFileName(file.getOriginalFileName());
        response.setFileType(file.getFileType());
        response.setMimeType(file.getMimeType());
        response.setFileUrl(file.getFileUrl());
        response.setFileSize(file.getFileSize());
        response.setUploadedDate(file.getUploadedDate());

        return response;
    }

    public UserResponseDTO registerUser(RegisterUserDTO registerRequest, MultipartFile profilePhoto) {

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException("USER role not found"));

        User newUser = new User();
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setFirstLastName(registerRequest.getFirstLastName());
        newUser.setSecondLastName(registerRequest.getSecondLastName());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setUsedSpace(0.0);
        newUser.setTotalCapacity(15.0 * 1024 * 1024 * 1024);
        newUser.setCreatedDate(LocalDate.now());
        newUser.setUpdatedDate(LocalDate.now());

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);

        User saved = userRepository.save(newUser);

        // --- profile photo
        if (profilePhoto != null && !profilePhoto.isEmpty()) {

            storageService.validateUserStorageCapacity(saved, profilePhoto.getSize());

            File fileUploaded = storageService.processSingleFileProfile(profilePhoto, saved);

            saved.setProfilePhoto(fileUploaded);

            storageService.increaseUserUsedSpace(saved, profilePhoto.getSize());

            saved = userRepository.save(saved);
        }

        return convertToUserResponseDTO(saved);
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