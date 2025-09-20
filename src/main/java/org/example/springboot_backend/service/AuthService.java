package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.LoginRequest;
import org.example.springboot_backend.dto.LoginResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.Role;
import org.example.springboot_backend.exception.UserNotFoundException;
import org.example.springboot_backend.exception.InvalidCredentialsException;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, JwtService jwtService, 
                      AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
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
}