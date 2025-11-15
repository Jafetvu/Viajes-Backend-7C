package com.utez.edu.mx.viajesbackend.websocket;

import com.utez.edu.mx.viajesbackend.modules.notification.NotificationType;
import com.utez.edu.mx.viajesbackend.websocket.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket controller for handling notification-related messages.
 *
 * <p>This controller handles incoming WebSocket messages related to notifications
 * and can broadcast notifications to users.</p>
 */
@Controller
public class NotificationWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle notification read status updates from clients.
     * This allows clients to mark notifications as read via WebSocket.
     *
     * @param notificationId the notification ID
     * @param principal the authenticated user
     */
    @MessageMapping("/notifications/read")
    public void markNotificationAsRead(@Payload Long notificationId, Principal principal) {
        try {
            logger.info("User {} marked notification {} as read via WebSocket",
                    principal.getName(), notificationId);
            // The actual marking as read should be done via REST API
            // This is just for logging/tracking purposes
        } catch (Exception e) {
            logger.error("Error handling notification read status: {}", e.getMessage());
        }
    }

    /**
     * Send a notification to a specific user via WebSocket.
     * This method is called by NotificationService, not exposed via @MessageMapping.
     *
     * @param username the user's username
     * @param notification the notification message
     */
    public void sendNotificationToUser(String username, NotificationMessage notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/notifications",
                    notification
            );
            logger.info("Sent notification to user: {}", username);
        } catch (Exception e) {
            logger.error("Error sending notification to user: {}", e.getMessage());
        }
    }

    /**
     * Broadcast a system-wide notification to all connected users.
     * Useful for maintenance alerts or system-wide announcements.
     *
     * @param title notification title
     * @param body notification body
     * @param type notification type
     */
    public void broadcastSystemNotification(String title, String body, NotificationType type) {
        try {
            NotificationMessage message = new NotificationMessage();
            message.setTitle(title);
            message.setBody(body);
            message.setType(type);
            message.setRead(false);

            messagingTemplate.convertAndSend("/topic/notifications/system", message);
            logger.info("Broadcasted system notification: {}", title);
        } catch (Exception e) {
            logger.error("Error broadcasting system notification: {}", e.getMessage());
        }
    }
}
