package com.utez.edu.mx.viajesbackend.security;

import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.utez.edu.mx.viajesbackend.auth.KeyService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JWTUtil {

    private final KeyService keyService;

    public JWTUtil(KeyService keyService) {
        this.keyService = keyService;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(keyService.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            return resolver.apply(claims);
        } catch (JwtException e) {
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(keyService.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                return resolver.apply(claims);
            } catch (JwtException err) {
                throw err;
            }
        }
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
        .setHeaderParam("kid", "primary")
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000L*60*60*10))
        .signWith(keyService.getKey(), SignatureAlgorithm.HS256)
        .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (JwtException ex) {
            return false;
        }
    }
}
