package com.utez.edu.mx.viajesbackend.modules.tariff.dto;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para transferir información de una tarifa.
 *
 * <p>Se utiliza para enviar datos de tarifa desde el backend hacia el cliente,
 * incluyendo toda la información de auditoría.</p>
 */
public class TariffDTO {

    private Long id;
    private Double tariffValue;
    private LocalDateTime modificationDate;
    private String modifierName;
    private String changeReason;

    // Constructores

    public TariffDTO() {
    }

    public TariffDTO(Long id, Double tariffValue, LocalDateTime modificationDate,
                     String modifierName, String changeReason) {
        this.id = id;
        this.tariffValue = tariffValue;
        this.modificationDate = modificationDate;
        this.modifierName = modifierName;
        this.changeReason = changeReason;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTariffValue() {
        return tariffValue;
    }

    public void setTariffValue(Double tariffValue) {
        this.tariffValue = tariffValue;
    }

    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
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
        return "TariffDTO{" +
                "id=" + id +
                ", tariffValue=" + tariffValue +
                ", modificationDate=" + modificationDate +
                ", modifierName='" + modifierName + '\'' +
                ", changeReason='" + changeReason + '\'' +
                '}';
    }
}
