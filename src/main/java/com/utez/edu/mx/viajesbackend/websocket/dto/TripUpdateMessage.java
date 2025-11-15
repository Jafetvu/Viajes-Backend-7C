package com.utez.edu.mx.viajesbackend.websocket.dto;

import com.utez.edu.mx.viajesbackend.modules.trip.TripStatus;

import java.time.LocalDateTime;

/**
 * DTO for WebSocket messages about trip status updates.
 *
 * <p>Sent to clients when a trip's status changes, allowing real-time
 * updates for both drivers and passengers.</p>
 */
public class TripUpdateMessage {

    private Long tripId;
    private TripStatus status;
    private Long clientId;
    private Long driverId;
    private String origin;
    private String destination;
    private Double fare;
    private boolean driverCompleted;
    private boolean clientCompleted;
    private LocalDateTime updatedAt;
    private String message;

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
}
