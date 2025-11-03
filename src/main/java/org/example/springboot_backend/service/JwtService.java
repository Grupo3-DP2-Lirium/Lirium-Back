package org.example.springboot_backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.springboot_backend.exception.InvalidResetTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret:mySecretKey}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String username) {
        return buildToken(username, jwtExpiration, null);
    }

    /**
     * Genera un token JWT especial para reseteo de contraseña
     * 
     * @param email Email del usuario
     * @param expirationMinutes Tiempo de expiración en minutos
     * @return Token JWT con claim "type": "password_reset"
     */
    public String generateResetToken(String email, int expirationMinutes) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "password_reset");
        long expirationMillis = expirationMinutes * 60 * 1000L;
        return buildToken(email, expirationMillis, claims);
    }

    private String buildToken(String username, long expiration, Map<String, Object> extraClaims) {
        var builder = Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey());
        
        if (extraClaims != null && !extraClaims.isEmpty()) {
            builder.claims(extraClaims);
        }
        
        return builder.compact();
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Valida un token de reseteo de contraseña
     * Verifica que sea del tipo "password_reset" y no esté expirado
     * 
     * @param token Token JWT a validar
     * @throws InvalidResetTokenException si el token es inválido o no es de tipo reset
     */
    public void validateResetToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            
            // Verificar que sea un token de reseteo
            String type = claims.get("type", String.class);
            if (!"password_reset".equals(type)) {
                throw new InvalidResetTokenException("Token inválido: no es un token de reseteo de contraseña");
            }
            
            // Verificar expiración
            if (isTokenExpired(token)) {
                throw new InvalidResetTokenException("Token expirado");
            }
            
        } catch (Exception e) {
            if (e instanceof InvalidResetTokenException) {
                throw e;
            }
            throw new InvalidResetTokenException("Token inválido o mal formado");
        }
    }

    /**
     * Extrae el email del token de reseteo
     * 
     * @param token Token JWT
     * @return Email del usuario
     */
    public String extractEmailFromResetToken(String token) {
        validateResetToken(token);
        return extractUsername(token);
    }
}