package com.utez.edu.mx.viajesbackend.modules.notification;

import com.utez.edu.mx.viajesbackend.security.UserDetailsImpl;
import com.utez.edu.mx.viajesbackend.websocket.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing notifications.
 *
 * <p>Provides endpoints for retrieving, marking as read, and deleting notifications.
 * All endpoints require authentication and users can only access their own notifications.</p>
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get all notifications for the authenticated user.
     *
     * @param authentication the authenticated user
     * @return list of notifications
     */
    @GetMapping
    public ResponseEntity<List<NotificationMessage>> getMyNotifications(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            List<NotificationMessage> notifications = notificationService.getNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error getting notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread notifications for the authenticated user.
     *
     * @param authentication the authenticated user
     * @return list of unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationMessage>> getUnreadNotifications(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            List<NotificationMessage> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Error getting unread notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get count of unread notifications for the authenticated user.
     *
     * @param authentication the authenticated user
     * @return count of unread notifications
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Long count = notificationService.getUnreadCount(userId);

            Map<String, Long> response = new HashMap<>();
            response.put("count", count);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting unread count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark a specific notification as read.
     *
     * @param notificationId the notification ID
     * @param authentication the authenticated user
     * @return updated notification
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            notificationService.markAsRead(notificationId, userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification marked as read");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark all notifications as read for the authenticated user.
     *
     * @param authentication the authenticated user
     * @return success message
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            notificationService.markAllAsRead(userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "All notifications marked as read");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a specific notification.
     *
     * @param notificationId the notification ID
     * @param authentication the authenticated user
     * @return success message
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long notificationId,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            notificationService.deleteNotification(notificationId, userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification deleted");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error deleting notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Error deleting notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Extract user ID from authentication object.
     *
     * @param authentication the authentication object
     * @return user ID
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}
