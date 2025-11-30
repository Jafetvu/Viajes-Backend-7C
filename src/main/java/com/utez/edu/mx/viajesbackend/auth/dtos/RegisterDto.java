package com.utez.edu.mx.viajesbackend.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDto(
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @NotBlank(message = "El apellido paterno es obligatorio")
    String surname,

    String lastname,

    @NotBlank(message = "El nombre de usuario es obligatorio")
    String username,

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Formato de correo inválido")
    String email,

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    String phoneNumber,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password,

    boolean isDriver
) {}
