package org.example.springboot_backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.UserStatus;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserStatusFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public UserStatusFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Solo validar si hay un usuario autenticado
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            
            String email = authentication.getName();
            System.out.println("🔍 UserStatusFilter: Validando usuario: " + email);
            
            // Buscar el usuario en la base de datos
            User user = userRepository.findByEmail(email).orElse(null);
            
            // Si el usuario está suspendido, retornar 403
            if (user != null && user.getStatus() == UserStatus.SUSPENDED) {
                System.out.println("🚫 UserStatusFilter: Usuario " + email + " está SUSPENDIDO");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"ACCOUNT_SUSPENDED\", \"message\": \"Tu cuenta ha sido suspendida. Contacta al administrador.\"}");
                response.getWriter().flush();
                return;
            } else if (user != null) {
                System.out.println("✅ UserStatusFilter: Usuario " + email + " está ACTIVO");
            }
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // No aplicar el filtro a endpoints públicos
        String path = request.getRequestURI();
        boolean shouldSkip = path.startsWith("/api/auth/") || 
               path.startsWith("/api/public/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/");
        
        if (shouldSkip) {
            System.out.println("⏭️ UserStatusFilter: Saltando validación para: " + path);
        }
        
        return shouldSkip;
    }
}
