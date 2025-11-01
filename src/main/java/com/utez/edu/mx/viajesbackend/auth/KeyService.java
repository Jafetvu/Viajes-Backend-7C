package com.utez.edu.mx.viajesbackend.auth;

import org.springframework.beans.factory.annotation.Value;
import java.util.Base64;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

public class KeyService {

    @Value("${JWT_KEY}")
    private String JWT_KEY;

    public Key getSigningKey() {
        if (JWT_KEY == null) {
            throw new IllegalStateException("JWT  primary key not configured");
        }
        byte[] decoded = Base64.getDecoder().decode(JWT_KEY);
        return Keys.hmacShaKeyFor(decoded);
    }

    public Key getKey() {
        return getSigningKey();
    }

}
