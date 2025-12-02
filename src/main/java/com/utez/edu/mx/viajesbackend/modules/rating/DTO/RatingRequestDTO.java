package com.utez.edu.mx.viajesbackend.modules.rating.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO utilizado para registrar una calificación. El mismo objeto sirve
 * tanto para calificaciones de clientes hacia conductores como para
 * calificaciones de conductores hacia clientes.
 */
public class RatingRequestDTO {

    @NotNull(message = "El identificador del viaje es obligatorio")
    private Long tripId;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer rating;

    private String comment;

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
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
}
