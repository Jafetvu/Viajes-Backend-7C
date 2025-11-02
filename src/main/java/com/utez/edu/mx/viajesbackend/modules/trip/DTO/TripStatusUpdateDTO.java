package com.utez.edu.mx.viajesbackend.modules.trip.DTO;

import com.utez.edu.mx.viajesbackend.modules.trip.TripStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar el estado de un viaje por parte del conductor.
 */
public class TripStatusUpdateDTO {

    @NotNull(message = "El identificador del viaje es obligatorio")
    private Long tripId;

    @NotNull(message = "El nuevo estado es obligatorio")
    private TripStatus status;

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }
}
