package com.utez.edu.mx.viajesbackend.modules.tariff;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una tarifa del sistema.
 *
 * <p>La tarifa almacena el valor por viaje y mantiene un historial completo
 * de cambios con información de auditoría (quién modificó, cuándo y por qué).
 * Solo una tarifa puede estar activa a la vez (is_active = true).</p>
 */
@Entity
@Table(name = "tariff")
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Valor de la tarifa por viaje en pesos mexicanos (MXN).
     * Debe ser mayor a 0.
     */
    @NotNull(message = "El valor de la tarifa es obligatorio")
    @Min(value = 1, message = "El valor de la tarifa debe ser mayor a 0")
    @Column(name = "tariff_value", nullable = false)
    private Double tariffValue;

    /**
     * Fecha y hora en que se modificó la tarifa.
     */
    @NotNull(message = "La fecha de modificación es obligatoria")
    @Column(name = "modification_date", nullable = false)
    private LocalDateTime modificationDate;

    /**
     * Nombre completo de la persona que modificó la tarifa.
     */
    @NotBlank(message = "El nombre del modificador es obligatorio")
    @Column(name = "modifier_name", nullable = false)
    private String modifierName;

    /**
     * Razón o justificación del cambio de tarifa.
     * Debe tener entre 10 y 500 caracteres.
     */
    @NotBlank(message = "La razón del cambio es obligatoria")
    @Size(min = 10, max = 500, message = "La razón debe tener entre 10 y 500 caracteres")
    @Column(name = "change_reason", nullable = false, length = 500)
    private String changeReason;

    /**
     * Indica si esta tarifa es la activa actualmente.
     * Solo puede haber una tarifa activa a la vez.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Fecha de creación del registro.
     * Se establece automáticamente al crear la entidad.
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Callback ejecutado antes de persistir la entidad.
     * Establece automáticamente la fecha de creación.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructores

    public Tariff() {
    }

    public Tariff(Double tariffValue, LocalDateTime modificationDate, String modifierName, String changeReason) {
        this.tariffValue = tariffValue;
        this.modificationDate = modificationDate;
        this.modifierName = modifierName;
        this.changeReason = changeReason;
        this.isActive = true;
    }

    public Tariff(Long id, Double tariffValue, LocalDateTime modificationDate, String modifierName,
                  String changeReason, Boolean isActive) {
        this.id = id;
        this.tariffValue = tariffValue;
        this.modificationDate = modificationDate;
        this.modifierName = modifierName;
        this.changeReason = changeReason;
        this.isActive = isActive;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Tariff{" +
                "id=" + id +
                ", tariffValue=" + tariffValue +
                ", modificationDate=" + modificationDate +
                ", modifierName='" + modifierName + '\'' +
                ", changeReason='" + changeReason + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
