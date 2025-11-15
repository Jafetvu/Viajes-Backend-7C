package com.utez.edu.mx.viajesbackend.modules.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notification entity operations.
 *
 * <p>Provides database access methods for managing user notifications,
 * including queries for read/unread status and user-specific notifications.</p>
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific user, ordered by creation date descending.
     *
     * @param userId the ID of the user
     * @return list of notifications for the user
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = ?1 ORDER BY n.createdAt DESC")
    List<Notification> findByUserId(Long userId);

    /**
     * Find all unread notifications for a specific user.
     *
     * @param userId the ID of the user
     * @return list of unread notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = ?1 AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserId(Long userId);

    /**
     * Count unread notifications for a specific user.
     *
     * @param userId the ID of the user
     * @return count of unread notifications
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = ?1 AND n.isRead = false")
    Long countUnreadByUserId(Long userId);

    /**
     * Find all notifications related to a specific trip.
     *
     * @param tripId the ID of the trip
     * @return list of notifications related to the trip
     */
    List<Notification> findByTripId(Long tripId);
}
