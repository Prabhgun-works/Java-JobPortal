package com.jobportal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String SECRET = "A7F9k3LmPqR8sT2vXyZ5bC1dW6nH0uJ4rE8mK2pO7qV5xN9yQ3tS6wU8zR1bF0aD";
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private final long EXPIRATION_TIME = 86400000; // 1 day

    // ---------------- Generate token with User object ----------------
    public String generateToken(com.jobportal.model.User user) {
        Map<String, Object> claims = new HashMap<>();
        // Add role with Spring Security convention "ROLE_XXX"
        claims.put("roles", "ROLE_" + user.getRole().toUpperCase());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())   // email as subject
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // ---------------- Extract username/email ----------------
    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ---------------- Extract roles ----------------
    public String extractRoles(String token) {
        return (String) Jwts.parser().setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .get("roles");
    }

    // ---------------- Validate token without UserDetails ----------------
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ---------------- Validate token against UserDetails ----------------
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // ---------------- Check if token expired ----------------
    private boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parser().setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true; // consider invalid token as expired
        }
    }
}
