package com.utez.edu.mx.viajesbackend.auth.modules;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotBlank;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    Optional<PasswordResetToken> findTopByUser_EmailOrderByExpiresAtDesc(@NotBlank String email);
}