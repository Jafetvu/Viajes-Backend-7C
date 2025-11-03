package com.utez.edu.mx.viajesbackend.modules.trip.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO utilizado para solicitar un viaje. Incluye la información básica
 * proporcionada por el cliente al momento de crear una solicitud.
 */
public class TripRequestDTO {

    @NotNull(message = "El identificador del cliente es obligatorio")
    private Long clientId;

    @NotBlank(message = "El origen es obligatorio")
    private String origin;

    @NotBlank(message = "El destino es obligatorio")
    private String destination;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
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
}
