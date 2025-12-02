package com.utez.edu.mx.viajesbackend.modules.rating.DTO;

import java.time.LocalDateTime;

/**
 * DTO para exponer la información de una calificación en la API.
 * Incluye los datos básicos junto con el nombre del usuario que la emitió.
 */
public class RatingResponseDTO {

    private Long id;
    private Long tripId;
    private Integer rating;
    private String comment;
    private String raterName;
    private LocalDateTime createdAt;

    public RatingResponseDTO() {}

    public RatingResponseDTO(Long id, Long tripId, Integer rating, String comment,
                             String raterName, LocalDateTime createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.rating = rating;
        this.comment = comment;
        this.raterName = raterName;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getRaterName() {
        return raterName;
    }

    public void setRaterName(String raterName) {
        this.raterName = raterName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}