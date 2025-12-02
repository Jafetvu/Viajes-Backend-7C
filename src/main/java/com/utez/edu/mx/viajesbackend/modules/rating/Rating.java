package com.utez.edu.mx.viajesbackend.modules.rating;

import com.utez.edu.mx.viajesbackend.modules.trip.Trip;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una calificación otorgada en el sistema.
 *
 * Las calificaciones se vinculan a un viaje concreto y pueden provenir
 * tanto del cliente hacia el conductor como del conductor hacia el cliente.
 * Se distingue el origen de la calificación a través del campo
 * {@code fromClient}. Un valor {@code true} indica que la calificación
 * fue otorgada por el cliente al conductor; {@code false} indica que
 * proviene del conductor hacia el cliente.
 *
 * Solo se permite una calificación por cada combinación de viaje y
 * origen, lo que se garantiza mediante una restricción de unicidad a
 * nivel de base de datos.
 */
@Entity
@Table(name = "rating",
        uniqueConstraints = @UniqueConstraint(columnNames = {"trip_id", "from_client"}))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Viaje al que corresponde la calificación. No puede ser nulo.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    /**
     * Indica si la calificación proviene del cliente. Un valor {@code true}
     * significa que el cliente calificó al conductor; {@code false}
     * significa que el conductor calificó al cliente.
     */
    @Column(name = "from_client", nullable = false)
    private Boolean fromClient;

    /**
     * Valor de la calificación entre 1 y 5.
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * Comentario opcional que acompaña a la calificación.
     */
    @Column
    private String comment;

    /**
     * Fecha y hora en que se creó la calificación.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Boolean getFromClient() {
        return fromClient;
    }

    public void setFromClient(Boolean fromClient) {
        this.fromClient = fromClient;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
