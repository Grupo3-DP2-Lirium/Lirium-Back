package org.example.springboot_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.example.springboot_backend.service.SubscriptionValidationService;
import org.example.springboot_backend.service.UserService;
import org.example.springboot_backend.entity.User;

@Component
public class SubscriptionInterceptor implements HandlerInterceptor {

    @Autowired
    private SubscriptionValidationService subscriptionValidationService;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // rutas excluidas
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/") || uri.startsWith("/public/")) {
            return true;
        }

        String email = (String) request.getAttribute("userEmail");

        if (email == null) {
            return true;
        }

        User user = userService.getByEmail(email);

        boolean ok = subscriptionValidationService.hasActiveSubscription(user);

        if (!ok) {
            response.setStatus(402);
            response.getWriter().write("Subscription expired");
            return false;
        }

        return true;
    }
}
