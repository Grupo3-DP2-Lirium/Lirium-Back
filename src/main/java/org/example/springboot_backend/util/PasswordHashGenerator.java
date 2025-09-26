package org.example.springboot_backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generar hash para "rodrigo"
        String password = "rodrigo";
        String hash = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // Verificar que el hash es correcto
        boolean matches = encoder.matches(password, hash);
        System.out.println("Hash matches: " + matches);
    }
}