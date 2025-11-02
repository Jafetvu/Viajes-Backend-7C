package com.utez.edu.mx.viajesbackend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Base64;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

@Service
public class KeyService {

    @Value("${secret.key}")
    private String JWT_KEY;

    public Key getSigningKey() {
        if (JWT_KEY == null || JWT_KEY.isEmpty()) {
            throw new IllegalStateException("JWT primary key not configured");
        }
        try {
            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(JWT_KEY);
            } catch (IllegalArgumentException e) {
                decoded = JWT_KEY.getBytes();
            }
            if (decoded.length < 32) {
                byte[] padded = new byte[32];
                System.arraycopy(decoded, 0, padded, 0, Math.min(decoded.length, 32));
                decoded = padded;
            }
            return Keys.hmacShaKeyFor(decoded);
        } catch (Exception e) {
            throw new IllegalStateException("Error generating JWT key: " + e.getMessage(), e);
        }
    }

    public Key getKey() {
        return getSigningKey();
    }

}
