package com.predictorama.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final String jwtSecret;
    private final Integer expiryDays;


    public JwtService(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiry-days}") Integer expiryDays) {
        this.jwtSecret = jwtSecret;
        this.expiryDays = expiryDays;
    }

    public String generateToken(UUID userId, boolean needsOnboarding){
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));

        return Jwts.builder()
                .subject(userId.toString())
                .claim("needsOnboarding", needsOnboarding)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryDays * 86_400_000L))
                .signWith(key)
                .compact();
    };

    public Claims validateToken(String token){
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));

        return Jwts.parser().verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
