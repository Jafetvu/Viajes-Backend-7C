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

    @NotBlank(message = "La dirección de origen es obligatoria")
    private String originAddress;

    @NotNull(message = "La latitud de origen es obligatoria")
    private Double originLatitude;

    @NotNull(message = "La longitud de origen es obligatoria")
    private Double originLongitude;

    @NotBlank(message = "La dirección de destino es obligatoria")
    private String destinationAddress;

    @NotNull(message = "La latitud de destino es obligatoria")
    private Double destinationLatitude;

    @NotNull(message = "La longitud de destino es obligatoria")
    private Double destinationLongitude;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(String originAddress) {
        this.originAddress = originAddress;
    }

    public Double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(Double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public Double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(Double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(Double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(Double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }
}
