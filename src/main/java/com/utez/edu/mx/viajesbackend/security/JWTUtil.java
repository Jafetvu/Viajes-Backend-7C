package com.utez.edu.mx.viajesbackend.security;

import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.utez.edu.mx.viajesbackend.auth.KeyService;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfileRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JWTUtil {

    private final KeyService keyService;
    private final DriverProfileRepository driverProfileRepository;

    public JWTUtil(KeyService keyService, DriverProfileRepository driverProfileRepository) {
        this.keyService = keyService;
        this.driverProfileRepository = driverProfileRepository;
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
            throw e;
        }
    }

    public String generateToken(UserDetails userDetails) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        if (userDetails instanceof UserDetailsImpl) {
            UserDetailsImpl impl = (UserDetailsImpl) userDetails;
            claims.put("id", impl.getId());
            claims.put("name", impl.getName());
            claims.put("paternalSurname", impl.getPaternalSurname());
            claims.put("maternalSurname", impl.getMaternalSurname());
            claims.put("email", impl.getEmail());
            claims.put("phone", impl.getPhone());
            claims.put("status", impl.isEnabled());
            if (impl.getCreatedAt() != null) {
                claims.put("createdAt", impl.getCreatedAt().toString());
            }
            
            // Add driverProfileId if applicable
            boolean isDriver = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CONDUCTOR"));
            
            if (isDriver) {
                driverProfileRepository.findByUserId(impl.getId())
                        .ifPresent(profile -> claims.put("driverProfileId", profile.getId()));
            }
        }
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.joining(",")));

        return Jwts.builder()
        .setClaims(claims)
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
