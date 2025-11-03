package com.utez.edu.mx.viajesbackend.modules.trip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfile;
import com.utez.edu.mx.viajesbackend.modules.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un viaje (solicitud de transporte) dentro del sistema.
 *
 * <p>El viaje relaciona a un cliente con un conductor y registra el punto de
 * origen, destino, tarifa y estado. A lo largo de su ciclo de vida el viaje
 * pasa por distintas etapas que se reflejan en el campo {@link #status}.</p>
 */
@Entity
@Table(name = "trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cliente que solicita el viaje. Es obligatorio, por lo que no se permite
     * nulo en la columna.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnoreProperties({"password"})
    private User client;

    /**
     * Conductor asignado. Puede ser nulo en la fase de solicitud si no se
     * ha asignado ningún conductor; será asignado cuando un conductor acepte el viaje.
     */
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private DriverProfile driver;

    /**
     * Punto de origen del viaje. Requerido.
     */
    @Column(nullable = false)
    private String origin;

    /**
     * Punto de destino del viaje. Requerido.
     */
    @Column(nullable = false)
    private String destination;

    /**
     * Tarifa total para el viaje. Esta cifra se calcula al solicitar el
     * viaje y puede ser una tarifa fija o calculada a partir de la distancia.
     */
    @Column(nullable = false)
    private Double fare;

    /**
     * Estado actual del viaje. No puede ser nulo y se almacena como cadena
     * utilizando la enumeración {@link TripStatus}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TripStatus status;

    /**
     * Tiempo de creación del viaje. Útil para ordenar cronológicamente.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Tiempo de última actualización del viaje. Permite conocer cuándo
     * cambió de estado por última vez.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Motivo de cancelación en caso de que el viaje se cancele. Es opcional.
     */
    @Column(name = "cancel_reason")
    private String cancelReason;

    /**
     * Calificación otorgada por el cliente al finalizar el viaje. Valor de 1 a 5.
     */
    @Column
    private Integer rating;

    /**
     * Comentario opcional asociado a la calificación.
     */
    @Column
    private String comment;

    /**
     * Indicador de si el conductor ha marcado el viaje como completado. Para que un
     * viaje cambie a estado {@link TripStatus#COMPLETADO} tanto el conductor como el
     * cliente deben confirmar la finalización. Este campo se inicializa en {@code false}.
     */
    @Column(name = "driver_completed")
    private boolean driverCompleted = false;

    /**
     * Indicador de si el cliente ha marcado el viaje como completado. Para que un
     * viaje cambie a estado {@link TripStatus#COMPLETADO} tanto el conductor como el
     * cliente deben confirmar la finalización. Este campo se inicializa en {@code false}.
     */
    @Column(name = "client_completed")
    private boolean clientCompleted = false;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public DriverProfile getDriver() {
        return driver;
    }

    public void setDriver(DriverProfile driver) {
        this.driver = driver;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getFare() {
        return fare;
    }

    public void setFare(Double fare) {
        this.fare = fare;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isDriverCompleted() {
        return driverCompleted;
    }

    public void setDriverCompleted(boolean driverCompleted) {
        this.driverCompleted = driverCompleted;
    }

    public boolean isClientCompleted() {
        return clientCompleted;
    }

    public void setClientCompleted(boolean clientCompleted) {
        this.clientCompleted = clientCompleted;
    }
}
