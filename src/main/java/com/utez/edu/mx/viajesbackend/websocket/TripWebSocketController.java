package com.utez.edu.mx.viajesbackend.websocket;

import com.utez.edu.mx.viajesbackend.modules.trip.Trip;
import com.utez.edu.mx.viajesbackend.websocket.dto.TripUpdateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for broadcasting trip updates.
 *
 * <p>This controller provides methods to send trip status updates to clients
 * via WebSocket, allowing real-time communication between drivers and passengers.</p>
 */
@Controller
public class TripWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(TripWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;

    public TripWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast trip update to all drivers.
     * Used when a new trip is requested so all drivers can see it.
     *
     * @param trip the trip that was updated
     */
    public void broadcastNewTripToDrivers(Trip trip) {
        try {
            TripUpdateMessage message = convertTripToMessage(trip);
            message.setMessage("New trip request available");

            // Send to all drivers subscribed to /topic/trips
            messagingTemplate.convertAndSend("/topic/trips/new", message);
            logger.info("Broadcasted new trip {} to all drivers", trip.getId());
        } catch (Exception e) {
            logger.error("Error broadcasting new trip to drivers: {}", e.getMessage());
        }
    }

    /**
     * Send trip update to a specific driver.
     *
     * @param driverUsername the driver's username
     * @param trip the trip that was updated
     * @param message custom message to send
     */
    public void sendTripUpdateToDriver(String driverUsername, Trip trip, String message) {
        try {
            TripUpdateMessage updateMessage = convertTripToMessage(trip);
            updateMessage.setMessage(message);

            messagingTemplate.convertAndSendToUser(
                    driverUsername,
                    "/queue/trips",
                    updateMessage
            );
            logger.info("Sent trip update to driver: {}", driverUsername);
        } catch (Exception e) {
            logger.error("Error sending trip update to driver: {}", e.getMessage());
        }
    }

    /**
     * Send trip update to a specific client.
     *
     * @param clientUsername the client's username
     * @param trip the trip that was updated
     * @param message custom message to send
     */
    public void sendTripUpdateToClient(String clientUsername, Trip trip, String message) {
        try {
            TripUpdateMessage updateMessage = convertTripToMessage(trip);
            updateMessage.setMessage(message);

            messagingTemplate.convertAndSendToUser(
                    clientUsername,
                    "/queue/trips",
                    updateMessage
            );
            logger.info("Sent trip update to client: {}", clientUsername);
        } catch (Exception e) {
            logger.error("Error sending trip update to client: {}", e.getMessage());
        }
    }

    /**
     * Send trip update to both driver and client.
     *
     * @param trip the trip that was updated
     * @param message custom message to send
     */
    public void sendTripUpdateToBoth(Trip trip, String message) {
        if (trip.getDriver() != null && trip.getDriver().getUser() != null) {
            sendTripUpdateToDriver(trip.getDriver().getUser().getUsername(), trip, message);
        }
        if (trip.getClient() != null) {
            sendTripUpdateToClient(trip.getClient().getUsername(), trip, message);
        }
    }

    /**
     * Convert Trip entity to TripUpdateMessage DTO.
     *
     * @param trip the trip entity
     * @return trip update message DTO
     */
    private TripUpdateMessage convertTripToMessage(Trip trip) {
        TripUpdateMessage message = new TripUpdateMessage();
        message.setTripId(trip.getId());
        message.setStatus(trip.getStatus());
        message.setClientId(trip.getClient().getId());
        message.setDriverId(trip.getDriver() != null ? trip.getDriver().getId() : null);
        message.setOrigin(trip.getOrigin());
        message.setDestination(trip.getDestination());
        message.setFare(trip.getFare());
        message.setDriverCompleted(trip.isDriverCompleted());
        message.setClientCompleted(trip.isClientCompleted());
        message.setUpdatedAt(trip.getUpdatedAt());
        return message;
    }
}
