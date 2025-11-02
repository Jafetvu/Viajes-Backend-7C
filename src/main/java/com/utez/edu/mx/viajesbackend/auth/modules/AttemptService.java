package com.utez.edu.mx.viajesbackend.auth.modules;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Contador de memoria de intentos de login fallidos
 * funciona para login y podría para verificar códigos OTP
 */
@Component
public class AttemptService {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int MAX_CODE_ATTEMPTS = 5;

    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Integer> codeAttempts = new ConcurrentHashMap<>();

    public void loginSucceeded(String email) {
        loginAttempts.remove(email);
    }

    public void loginFailed(String email) {
        loginAttempts.merge(email, 1, (existing, value) -> {
            if (existing == null) return value;
            return existing + value;
        });
    }

    public boolean isLoginBlocked(String email) {
        return loginAttempts.getOrDefault(email, 0) >= MAX_LOGIN_ATTEMPTS;
    }

    public void codeSucceeded(String email) {
        codeAttempts.remove(email);
    }

    public void codeFailed(String email) {
        codeAttempts.merge(email, 1, (existing, value) -> {
            if (existing == null) return value;
            return existing + value;
        });
    }

    public boolean isCodeBlocked(String email) {
        return codeAttempts.getOrDefault(email, 0) >= MAX_CODE_ATTEMPTS;
    }
}