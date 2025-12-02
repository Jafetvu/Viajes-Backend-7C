package com.utez.edu.mx.viajesbackend.modules.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.utez.edu.mx.viajesbackend.modules.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a notification sent to a user.
 *
 * <p>Notifications are used to inform users about trip updates, system alerts,
 * and other important events. Each notification has a type, title, body, and
 * can be marked as read or unread.</p>
 */
@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who will receive this notification. Required.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "notifications"})
    private User user;

    /**
     * Type of notification (INFO, WARN, ERROR, OK).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NotificationType type;

    /**
     * Title of the notification. Required.
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * Body/message of the notification. Required.
     */
    @Column(nullable = false, length = 500)
    private String body;

    /**
     * Timestamp when the notification was created.
     */
    @Column(name = "created_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Indicates whether the notification has been read by the user.
     */
    @Column(nullable = false)
    private boolean isRead = false;

    /**
     * Timestamp when the notification was read by the user.
     */
    @Column(name = "read_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime readAt;

    /**
     * Optional reference to a trip if this notification is trip-related.
     */
    @Column(name = "trip_id")
    private Long tripId;

    // Lifecycle callback to set creation timestamp
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }
}
