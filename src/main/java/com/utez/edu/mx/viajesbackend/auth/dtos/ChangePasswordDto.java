package com.utez.edu.mx.viajesbackend.auth.dtos;

public record ChangePasswordDto(
    String currentPassword,
    String newPassword
) {}
