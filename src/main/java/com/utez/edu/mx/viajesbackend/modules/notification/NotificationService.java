package com.utez.edu.mx.viajesbackend.modules.notification;

import com.utez.edu.mx.viajesbackend.modules.user.User;
import com.utez.edu.mx.viajesbackend.modules.user.UserRepository;
import com.utez.edu.mx.viajesbackend.websocket.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing notifications.
 *
 * <p>Handles creation, retrieval, and updating of notifications,
 * as well as sending real-time notifications via WebSocket.</p>
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Create and send a notification to a user.
     *
     * @param userId user ID to send notification to
     * @param type notification type
     * @param title notification title
     * @param body notification body
     * @param tripId optional trip ID if trip-related
     * @return the created notification
     */
    @Transactional
    public Notification createAndSendNotification(
            Long userId,
            NotificationType type,
            String title,
            String body,
            Long tripId) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            logger.error("User not found with ID: {}", userId);
            throw new RuntimeException("User not found");
        }

        Notification notification = new Notification();
        notification.setUser(userOpt.get());
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setTripId(tripId);
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        logger.info("Notification created with ID: {} for user: {}", saved.getId(), userId);

        // Send via WebSocket
        sendNotificationViaWebSocket(saved);

        return saved;
    }

    /**
     * Send a notification to a user via WebSocket.
     *
     * @param notification the notification to send
     */
    private void sendNotificationViaWebSocket(Notification notification) {
        try {
            NotificationMessage message = convertToMessage(notification);
            messagingTemplate.convertAndSendToUser(
                    notification.getUser().getUsername(),
                    "/queue/notifications",
                    message
            );
            logger.info("Notification sent via WebSocket to user: {}", notification.getUser().getUsername());
        } catch (Exception e) {
            logger.error("Error sending notification via WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Get all notifications for a user.
     *
     * @param userId user ID
     * @return list of notifications
     */
    public List<NotificationMessage> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(this::convertToMessage)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a user.
     *
     * @param userId user ID
     * @return list of unread notifications
     */
    public List<NotificationMessage> getUnreadNotifications(Long userId) {
        return notificationRepository.findUnreadByUserId(userId)
                .stream()
                .map(this::convertToMessage)
                .collect(Collectors.toList());
    }

    /**
     * Get count of unread notifications for a user.
     *
     * @param userId user ID
     * @return count of unread notifications
     */
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * Mark a notification as read.
     *
     * @param notificationId notification ID
     * @param userId user ID (for security check)
     * @return updated notification
     */
    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            throw new RuntimeException("Notification not found");
        }

        Notification notification = notificationOpt.get();

        // Security check: ensure the notification belongs to the user
        if (notification.getUser().getId() != userId) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user.
     *
     * @param userId user ID
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
        });

        notificationRepository.saveAll(unreadNotifications);
        logger.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), userId);
    }

    /**
     * Delete a notification.
     *
     * @param notificationId notification ID
     * @param userId user ID (for security check)
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            throw new RuntimeException("Notification not found");
        }

        Notification notification = notificationOpt.get();

        // Security check
        if (notification.getUser().getId() != userId) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        notificationRepository.delete(notification);
        logger.info("Deleted notification with ID: {} for user: {}", notificationId, userId);
    }

    /**
     * Convert Notification entity to NotificationMessage DTO.
     *
     * @param notification the notification entity
     * @return notification message DTO
     */
    private NotificationMessage convertToMessage(Notification notification) {
        NotificationMessage message = new NotificationMessage();
        message.setId(notification.getId());
        message.setUserId(notification.getUser().getId());
        message.setType(notification.getType());
        message.setTitle(notification.getTitle());
        message.setBody(notification.getBody());
        message.setCreatedAt(notification.getCreatedAt());
        message.setRead(notification.isRead());
        message.setTripId(notification.getTripId());
        return message;
    }
}
