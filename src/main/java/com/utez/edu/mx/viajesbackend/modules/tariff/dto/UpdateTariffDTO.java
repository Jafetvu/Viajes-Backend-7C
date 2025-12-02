package com.utez.edu.mx.viajesbackend.modules.tariff.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) para recibir datos de actualización de tarifa.
 *
 * <p>Se utiliza para validar y recibir los datos necesarios cuando un
 * administrador actualiza la tarifa del sistema.</p>
 */
public class UpdateTariffDTO {

    @NotNull(message = "El valor de la tarifa es obligatorio")
    @Min(value = 1, message = "El valor de la tarifa debe ser mayor a 0")
    private Double tariffValue;

    @NotBlank(message = "El nombre del modificador es obligatorio")
    private String modifierName;

    @NotBlank(message = "La razón del cambio es obligatoria")
    @Size(min = 10, max = 500, message = "La razón debe tener entre 10 y 500 caracteres")
    private String changeReason;

    // Constructores

    public UpdateTariffDTO() {
    }

    public UpdateTariffDTO(Double tariffValue, String modifierName, String changeReason) {
        this.tariffValue = tariffValue;
        this.modifierName = modifierName;
        this.changeReason = changeReason;
    }

    // Getters y Setters

    public Double getTariffValue() {
        return tariffValue;
    }

    public void setTariffValue(Double tariffValue) {
        this.tariffValue = tariffValue;
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    @Override
    public String toString() {
        return "UpdateTariffDTO{" +
                "tariffValue=" + tariffValue +
                ", modifierName='" + modifierName + '\'' +
                ", changeReason='" + changeReason + '\'' +
                '}';
    }
}
