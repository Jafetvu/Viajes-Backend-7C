package com.utez.edu.mx.viajesbackend.modules.notification;

/**
 * Enumeration representing the type of notification.
 *
 * <p>Used to categorize notifications by their severity and purpose,
 * allowing the frontend to display them with appropriate styling and priority.</p>
 */
public enum NotificationType {
    /**
     * Informational notification - general information for the user.
     */
    INFO,

    /**
     * Warning notification - alerts about something that should be addressed soon or urgently.
     */
    WARN,

    /**
     * Error notification - notification about an error in a procedure or operation.
     */
    ERROR,

    /**
     * Success notification - notification that something completed successfully.
     */
    OK
}
