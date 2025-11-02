package com.utez.edu.mx.viajesbackend.auth.modules;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.utez.edu.mx.viajesbackend.auth.dtos.AuthLoginDto;
import com.utez.edu.mx.viajesbackend.auth.dtos.ChangePasswordDto;
import com.utez.edu.mx.viajesbackend.auth.dtos.PasswordResetDto;
import com.utez.edu.mx.viajesbackend.auth.dtos.PasswordResetRequestDto;
import com.utez.edu.mx.viajesbackend.auth.dtos.VerifyCodeDto;
import com.utez.edu.mx.viajesbackend.modules.user.User;
import com.utez.edu.mx.viajesbackend.modules.user.UserRepository;
import com.utez.edu.mx.viajesbackend.security.JWTUtil;
import com.utez.edu.mx.viajesbackend.security.UserDetailsImpl;

import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final AttemptService attemptService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final SecureRandom random = new SecureRandom();

    public AuthService(
            AuthenticationManager authManager,
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            JavaMailSender mailSender,
            AttemptService attemptService,
            BCryptPasswordEncoder passwordEncoder,
            JWTUtil jwtUtil) {
        this.authManager = authManager;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.attemptService = attemptService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public ResponseEntity<?> login(AuthLoginDto dto) {
        String username = dto.username().toLowerCase().trim();

        if (attemptService.isLoginBlocked(username)) {
            return ResponseEntity.status(423)
                    .body(Map.of(
                            "statusCode", 423,
                            "message", "Cuenta bloqueada temporalmente. Intenta más tarde."));
        }

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            attemptService.loginFailed(username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "statusCode", HttpStatus.NOT_FOUND.value(),
                    "message", "Usuario o contraseña incorrectos"));
        }
        User user = optionalUser.get();

        if (!user.isStatus()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "statusCode", HttpStatus.FORBIDDEN.value(),
                            "message", "Cuenta deshabilitada. Contacta al administrador."));
        }

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            attemptService.loginFailed(username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "statusCode", HttpStatus.UNAUTHORIZED.value(),
                            "message", "Usuario o contraseña incorrectos"));
        }

        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, dto.password()));
            attemptService.loginSucceeded(username);
        } catch (BadCredentialsException ex) {
            attemptService.loginFailed(username);
            throw ex;
        }

        UserDetailsImpl userdetails = new UserDetailsImpl(user);
        String jwt = jwtUtil.generateToken(userdetails);

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("paternalSurname", user.getSurname());
        userMap.put("maternalSurname", user.getLastname());
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUsername());
        userMap.put("role", user.getRole());
        userMap.put("phone", user.getPhoneNumber());
        userMap.put("status", user.isStatus());
        payload.put("statusCode", HttpStatus.OK.value());
        payload.put("token", jwt);
        payload.put("user", userMap);

        return ResponseEntity.ok(payload);
    }

    @Transactional
    public ResponseEntity<?> requestPasswordReset(PasswordResetRequestDto dto) {
        String email = dto.email().toLowerCase().trim();

        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok("Si el correo existe, hemos enviado un código de verificación.");
        }
        User user = userOpt.get();

        int codeInt = 100_000 + random.nextInt(900_000);
        String code = String.valueOf(codeInt);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setUser(user);
        prt.setTokenHash(passwordEncoder.encode(code));
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        prt.setUsed(false);
        tokenRepository.save(prt);

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(user.getEmail());
            mail.setSubject("Código de verificación - Viajes");
            mail.setText("Hola " + user.getName() + ",\n\n" +
                    "Has solicitado un código de verificación para restablecer tu contraseña.\n\n" +
                    "Tu código de verificación es: " + code + "\n\n" +
                    "Este código expira en 15 minutos.\n\n" +
                    "Si no solicitaste este código, ignora este mensaje.\n\n" +
                    "Saludos,\nEquipo Viajes");
            mailSender.send(mail);
        } catch (Exception e) {
            // Log del error pero no expongas información sensible al cliente
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error al enviar el correo. Por favor, intenta más tarde.");
        }

        return ResponseEntity.ok("Si el correo existe, hemos enviado un código de verificación.");
    }

    @Transactional
    public ResponseEntity<?> verifyCode(VerifyCodeDto dto) {
        String email = dto.email().toLowerCase().trim();

        if (attemptService.isCodeBlocked(email)) {
            return ResponseEntity.status(423)
                    .body(Map.of(
                            "statusCode", 423,
                            "message", "Demasiados intentos fallidos. Intenta más tarde."));
        }

        Optional<PasswordResetToken> prtOpt = tokenRepository
                .findTopByUser_EmailOrderByExpiresAtDesc(email);

        if (prtOpt.isEmpty()) {
            attemptService.codeFailed(email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Código inválido");
        }
        PasswordResetToken prt = prtOpt.get();

        boolean expired = prt.getExpiresAt().isBefore(LocalDateTime.now());
        boolean wrongUser = !prt.getUser().getEmail().equals(email);
        boolean wrongCode = !passwordEncoder.matches(dto.code(), prt.getTokenHash());

        if (prt.isUsed() || expired || wrongUser || wrongCode) {
            attemptService.codeFailed(email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Código inválido o expirado");
        }

        attemptService.codeSucceeded(email);
        prt.setUsed(true);
        tokenRepository.save(prt);

        return ResponseEntity.ok("Código verificado con éxito");
    }

    @Transactional
    public ResponseEntity<?> resetPassword(PasswordResetDto dto) {
        String email = dto.email().toLowerCase().trim();

        ResponseEntity<?> verification = verifyCode(
                new VerifyCodeDto(email, dto.code()));
        if (!verification.getStatusCode().is2xxSuccessful()) {
            return verification;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @Transactional
    public ResponseEntity<?> changePassword(ChangePasswordDto dto, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Contraseña actual incorrecta"));
        }

        if (dto.newPassword() == null || dto.newPassword().length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "La nueva contraseña debe tener al menos 8 caracteres"));
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }
}
