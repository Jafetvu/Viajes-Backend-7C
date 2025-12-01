package com.utez.edu.mx.viajesbackend.modules.trip.DTO;

import com.utez.edu.mx.viajesbackend.modules.trip.TripStatus;

/**
 * DTO de salida para un viaje. Simplifica la representación de la entidad
 * {@link com.utez.edu.mx.viajesbackend.modules.trip.Trip} al exponer
 * únicamente los campos relevantes para el cliente o conductor.
 */
public class TripDTO {

    private Long id;
    private String originAddress;
    private Double originLatitude;
    private Double originLongitude;
    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private Double fare;
    private TripStatus status;
    private String clientName;
    private String clientPhone;
    private String driverName;
    private String driverLicense;
    private Integer rating;

    public TripDTO() {}

    public TripDTO(Long id, String originAddress, Double originLatitude, Double originLongitude,
                   String destinationAddress, Double destinationLatitude, Double destinationLongitude,
                   Double fare, TripStatus status, String clientName, String clientPhone,
                   String driverName, String driverLicense, Integer rating) {
        this.id = id;
        this.originAddress = originAddress;
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.destinationAddress = destinationAddress;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.fare = fare;
        this.status = status;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.driverName = driverName;
        this.driverLicense = driverLicense;
        this.rating = rating;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverLicense() {
        return driverLicense;
    }

    public void setDriverLicense(String driverLicense) {
        this.driverLicense = driverLicense;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
