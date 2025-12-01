package com.utez.edu.mx.viajesbackend.websocket.dto;

import com.utez.edu.mx.viajesbackend.modules.trip.TripStatus;

import java.time.LocalDateTime;

/**
 * DTO for WebSocket messages about trip status updates.
 */
public class TripUpdateMessage {

    private Long tripId;
    private TripStatus status;
    private Long clientId;
    private Long driverId;
    private String originAddress;
    private Double originLatitude;
    private Double originLongitude;
    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private Double fare;
    private boolean driverCompleted;
    private boolean clientCompleted;
    private boolean driverStarted;  // New field
    private boolean clientStarted;  // New field
    private LocalDateTime updatedAt;
    private String message;
    
    // Extra info for UI
    private String clientName;
    private String driverName;

    public TripUpdateMessage() {
    }

    // Getters and Setters
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

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
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

    public boolean isDriverStarted() {
        return driverStarted;
    }

    public void setDriverStarted(boolean driverStarted) {
        this.driverStarted = driverStarted;
    }

    public boolean isClientStarted() {
        return clientStarted;
    }

    public void setClientStarted(boolean clientStarted) {
        this.clientStarted = clientStarted;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
}