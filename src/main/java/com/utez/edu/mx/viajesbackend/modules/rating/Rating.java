package com.utez.edu.mx.viajesbackend.modules.rating;

import com.utez.edu.mx.viajesbackend.modules.trip.Trip;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una calificación otorgada en el sistema.
 *
 * Las calificaciones se vinculan a un viaje concreto y relacionan a un
 * usuario calificador ({@code raterUser}) con un usuario calificado ({@code ratedUser}).
 *
 * Solo se permite una calificación por cada combinación de viaje y
 * usuario calificador, lo que se garantiza mediante una restricción de unicidad a
 * nivel de base de datos.
 */
@Entity
@Table(name = "rating",
        uniqueConstraints = @UniqueConstraint(columnNames = {"trip_id", "rater_user_id"}))
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Viaje al que corresponde la calificación.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    /**
     * Usuario que realiza la calificación (Pasajero o Conductor).
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "rater_user_id", nullable = false)
    private com.utez.edu.mx.viajesbackend.modules.user.User raterUser;

    /**
     * Usuario que recibe la calificación (Conductor o Pasajero).
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "rated_user_id", nullable = false)
    private com.utez.edu.mx.viajesbackend.modules.user.User ratedUser;

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

    public com.utez.edu.mx.viajesbackend.modules.user.User getRaterUser() {
        return raterUser;
    }

    public void setRaterUser(com.utez.edu.mx.viajesbackend.modules.user.User raterUser) {
        this.raterUser = raterUser;
    }

    public com.utez.edu.mx.viajesbackend.modules.user.User getRatedUser() {
        return ratedUser;
    }

    public void setRatedUser(com.utez.edu.mx.viajesbackend.modules.user.User ratedUser) {
        this.ratedUser = ratedUser;
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
