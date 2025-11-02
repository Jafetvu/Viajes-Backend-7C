package com.utez.edu.mx.viajesbackend.auth.modules;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.utez.edu.mx.viajesbackend.auth.dtos.AuthLoginDto;
import com.utez.edu.mx.viajesbackend.auth.dtos.ChangePasswordDto;
import com.utez.edu.mx.viajesbackend.auth.dtos.PasswordResetDto;
import com.utez.edu.mx.viajesbackend.auth.dtos.PasswordResetRequestDto;
import com.utez.edu.mx.viajesbackend.auth.dtos.VerifyCodeDto;
import com.utez.edu.mx.viajesbackend.modules.user.UserRepository;
import com.utez.edu.mx.viajesbackend.security.JWTUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthService service, JWTUtil jwtUtil, UserRepository userRepository) {
        this.service = service;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthLoginDto dto) {
        return service.login(dto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody PasswordResetRequestDto dto) {
        return service.requestPasswordReset(dto);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(
            @Valid @RequestBody VerifyCodeDto dto) {
        return service.verifyCode(dto);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody PasswordResetDto dto) {
        return service.resetPassword(dto);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordDto dto,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .getId();

        return service.changePassword(dto, userId);
    }
}
