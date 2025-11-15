package com.utez.edu.mx.viajesbackend.websocket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.utez.edu.mx.viajesbackend.modules.notification.NotificationType;

import java.time.LocalDateTime;

/**
 * DTO for WebSocket messages about notifications.
 *
 * <p>Sent to users when a new notification is created, allowing real-time
 * notification delivery without polling.</p>
 */
public class NotificationMessage {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String body;

    @JsonFormat(pattern = "dd/MM/yyyy - HH:mm")
    private LocalDateTime createdAt;

    private boolean isRead;
    private Long tripId;

    public NotificationMessage() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }
}
