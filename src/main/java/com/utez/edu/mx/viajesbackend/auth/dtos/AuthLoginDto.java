package com.utez.edu.mx.viajesbackend.auth.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * Dto para el login de usuario
 * Contiene el username y password en texto plano,
 * ambos son obligatorios
 */
public record AuthLoginDto(
    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    String username,

    @NotBlank(message = "La contraseña no puede estar vacía")
    String password
) { }
