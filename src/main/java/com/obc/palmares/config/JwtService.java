package com.obc.palmares.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service 
public class JwtService {
    
    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret, 
                      @Value ("${app.jwt.expiration-minutes:120}") long minutes  ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = minutes * 60_000;
    }
    public String generer(Long compteId, String username, Long personneId) {
        return Jwts.builder()
            .subject(username)
            .claim("compteId", compteId)
            .claim("personneId", personneId)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact();
    }

    public io.jsonwebtoken.Claims verifier(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload();
    }
    
}
