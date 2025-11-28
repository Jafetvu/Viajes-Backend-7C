package com.utez.edu.mx.viajesbackend.modules.trip;

import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverAvailability;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfile;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfileRepository;
import com.utez.edu.mx.viajesbackend.modules.notification.NotificationService;
import com.utez.edu.mx.viajesbackend.modules.notification.NotificationType;
import com.utez.edu.mx.viajesbackend.modules.trip.DTO.*;
import com.utez.edu.mx.viajesbackend.modules.user.User;
import com.utez.edu.mx.viajesbackend.modules.user.UserRepository;
import com.utez.edu.mx.viajesbackend.utils.CustomResponseEntity;
import com.utez.edu.mx.viajesbackend.websocket.TripWebSocketController;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Main service for managing business logic related to trips.
 * Allows clients to request trips, drivers to accept or reject requests,
 * update trip status and query history for both clients and drivers.
 * All responses are generated through {@link CustomResponseEntity} for uniform formatting.
 * Integrates WebSocket notifications for real-time updates.
 */
@Service
public class TripService {

    private static final Logger logger = LoggerFactory.getLogger(TripService.class);

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final CustomResponseEntity customResponseEntity;
    private final TripWebSocketController tripWebSocketController;
    private final NotificationService notificationService;

    public TripService(TripRepository tripRepository,
                       UserRepository userRepository,
                       DriverProfileRepository driverProfileRepository,
                       CustomResponseEntity customResponseEntity,
                       TripWebSocketController tripWebSocketController,
                       NotificationService notificationService) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.customResponseEntity = customResponseEntity;
        this.tripWebSocketController = tripWebSocketController;
        this.notificationService = notificationService;
    }

    /**
     * Creates a new trip request for a client. Does not automatically assign
     * a driver; registers the request with status {@link TripStatus#REQUESTED}
     * so drivers can accept or reject it.
     */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> requestTrip(TripRequestDTO dto) {
        Optional<User> client = userRepository.findById(dto.getClientId());
        if (client.isEmpty()) {
            return customResponseEntity.get404Response();
        }
        double fare = calculateFare(dto.getOrigin(), dto.getDestination());

        Trip trip = new Trip();
        trip.setClient(client.orElse(null));
        trip.setOrigin(dto.getOrigin());
        trip.setDestination(dto.getDestination());
        trip.setFare(fare);
        trip.setStatus(TripStatus.REQUESTED);
        LocalDateTime now = LocalDateTime.now();
        trip.setCreatedAt(now);
        trip.setUpdatedAt(now);
        Trip savedTrip = tripRepository.save(trip);

        // Send WebSocket notification to all drivers
        tripWebSocketController.broadcastNewTripToDrivers(savedTrip);
        logger.info("New trip requested by client ID: {}, broadcasting to drivers", dto.getClientId());

        // Send notification to client
        notificationService.createAndSendNotification(
                client.get().getId(),
                NotificationType.OK,
                "Trip Requested",
                "Your trip from " + dto.getOrigin() + " to " + dto.getDestination() + " has been requested successfully.",
                savedTrip.getId()
        );

        TripDTO responseDto = convertToDTO(savedTrip);
        return customResponseEntity.getOkResponse("Trip requested successfully", "ok", 200, responseDto);
    }

    @Transactional
    public ResponseEntity<?> getTripDetails(Long tripId, Long clientId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (!Objects.equals(trip.getClient().getId(), clientId)) {
            return customResponseEntity.get400Response("El viaje no pertenece al cliente");
        }
        return customResponseEntity.getOkResponse("Detalle del viaje", "ok", 200, convertToDTO(trip));
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> cancelTrip(Long tripId, Long clientId, String reason) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (!Objects.equals(trip.getClient().getId(), clientId)) {
            return customResponseEntity.get400Response("El viaje no pertenece al cliente");
        }
        if (trip.getStatus() == TripStatus.COMPLETED) {
            return customResponseEntity.get400Response("Cannot cancel a completed trip");
        }
        trip.setStatus(TripStatus.CANCELLED);
        trip.setCancelReason(reason);
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        DriverProfile driver = trip.getDriver();
        if (driver != null) {
            driver.setAvailability(DriverAvailability.DISPONIBLE);
            driverProfileRepository.save(driver);
        }
        return customResponseEntity.getOkResponse("Viaje cancelado", "ok", 200, null);
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> rateTrip(Long clientId, TripRateDTO dto) {
        Optional<Trip> maybe = tripRepository.findById(dto.getTripId());
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (!Objects.equals(trip.getClient().getId(), clientId)) {
            return customResponseEntity.get400Response("El viaje no pertenece al cliente");
        }
        if (trip.getStatus() != TripStatus.COMPLETED) {
            return customResponseEntity.get400Response("Only completed trips can be rated");
        }
        if (trip.getRating() != null) {
            return customResponseEntity.get400Response("El viaje ya ha sido calificado previamente");
        }
        trip.setRating(dto.getRating());
        trip.setComment(dto.getComment());
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        return customResponseEntity.getOkResponse("Calificación registrada", "ok", 200, null);
    }

    @Transactional
    public ResponseEntity<?> getClientTripHistory(Long clientId) {
        List<Trip> trips = tripRepository.findByClientId(clientId);
        if (trips.isEmpty()) {
            return customResponseEntity.getOkResponse("No hay viajes registrados", "ok", 200, Collections.emptyList());
        }
        trips.sort(Comparator.comparing(Trip::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());
        List<TripDTO> out = new ArrayList<>();
        for (Trip t : trips) {
            out.add(convertToDTO(t));
        }
        return customResponseEntity.getOkResponse("Historial de viajes", "ok", 200, out);
    }

    @Transactional
    public ResponseEntity<?> getAssignedTrips(Long driverId) {
        List<Trip> trips = tripRepository.findByDriverId(driverId);
        List<TripDTO> out = new ArrayList<>();
        for (Trip t : trips) {
            if (t.getStatus() != TripStatus.CANCELLED && t.getStatus() != TripStatus.COMPLETED) {
                out.add(convertToDTO(t));
            }
        }
        return customResponseEntity.getOkResponse("Solicitudes asignadas", "ok", 200, out);
    }

    @Transactional
    public ResponseEntity<?> getAvailableTrips() {
        List<Trip> trips = tripRepository.findAll();
        List<TripDTO> out = new ArrayList<>();
        for (Trip t : trips) {
            if (t.getStatus() == TripStatus.REQUESTED) {
                out.add(convertToDTO(t));
            }
        }
        return customResponseEntity.getOkResponse("Solicitudes disponibles", "ok", 200, out);
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> acceptTrip(Long tripId, Long driverId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (trip.getStatus() != TripStatus.REQUESTED || trip.getDriver() != null) {
            return customResponseEntity.get400Response("Trip is not available to be accepted");
        }
        Optional<DriverProfile> driverOpt = driverProfileRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            return customResponseEntity.get404Response();
        }
        DriverProfile driver = driverOpt.get();
        if (driver.getAvailability() != DriverAvailability.DISPONIBLE) {
            return customResponseEntity.get400Response("El conductor no está disponible para aceptar viajes");
        }
        trip.setDriver(driver);
        trip.setStatus(TripStatus.ACCEPTED);
        trip.setUpdatedAt(LocalDateTime.now());
        Trip savedTrip = tripRepository.save(trip);
        driver.setAvailability(DriverAvailability.EN_VIAJE);
        driverProfileRepository.save(driver);

        // Send WebSocket update to client
        tripWebSocketController.sendTripUpdateToClient(
                trip.getClient().getUsername(),
                savedTrip,
                "Your trip has been accepted by a driver"
        );

        // Send notification to client
        notificationService.createAndSendNotification(
                trip.getClient().getId(),
                NotificationType.OK,
                "Trip Accepted",
                "A driver has accepted your trip request!",
                savedTrip.getId()
        );

        return customResponseEntity.getOkResponse("Trip accepted", "ok", 200, null);
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> rejectTrip(Long tripId, Long driverId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (trip.getStatus() == TripStatus.REQUESTED && trip.getDriver() == null) {
            trip.setStatus(TripStatus.CANCELLED);
            trip.setUpdatedAt(LocalDateTime.now());
            tripRepository.save(trip);
            return customResponseEntity.getOkResponse("Trip rejected and cancelled", "ok", 200, null);
        }
        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), driverId)) {
            return customResponseEntity.get400Response("Trip is not assigned to this driver");
        }
        if (trip.getStatus() != TripStatus.ACCEPTED) {
            return customResponseEntity.get400Response("Only accepted trips that haven't started can be rejected");
        }
        trip.setStatus(TripStatus.CANCELLED);
        trip.setUpdatedAt(LocalDateTime.now());
        Trip savedTrip = tripRepository.save(trip);
        DriverProfile driver = trip.getDriver();
        driver.setAvailability(DriverAvailability.DISPONIBLE);
        driverProfileRepository.save(driver);

        // Send WebSocket update to client
        tripWebSocketController.sendTripUpdateToClient(
                trip.getClient().getUsername(),
                savedTrip,
                "Your trip has been cancelled by the driver"
        );

        // Send notification to client
        notificationService.createAndSendNotification(
                trip.getClient().getId(),
                NotificationType.WARN,
                "Trip Cancelled",
                "The driver has cancelled your trip request.",
                savedTrip.getId()
        );

        return customResponseEntity.getOkResponse("Trip rejected", "ok", 200, null);
    }

    /**
     * Updates the trip status in the flow: ACCEPTED → IN_PROGRESS.
     * The transition to COMPLETED is managed separately (both users must confirm).
     */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> updateTripStatus(Long driverId, TripStatusUpdateDTO dto) {
        Optional<Trip> maybe = tripRepository.findById(dto.getTripId());
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), driverId)) {
            return customResponseEntity.get400Response("Trip is not assigned to this driver");
        }
        TripStatus current = trip.getStatus();
        TripStatus newStatus = dto.getStatus();
        if (!isValidTransition(current, newStatus)) {
            return customResponseEntity.get400Response("State transition not allowed");
        }
        trip.setStatus(newStatus);
        trip.setUpdatedAt(LocalDateTime.now());
        Trip savedTrip = tripRepository.save(trip);

        // Send WebSocket update to both parties
        tripWebSocketController.sendTripUpdateToBoth(savedTrip, "Trip status updated to " + newStatus);

        // Send notifications
        notificationService.createAndSendNotification(
                trip.getClient().getId(),
                NotificationType.INFO,
                "Trip Status Updated",
                "Your trip status has been updated to: " + newStatus,
                savedTrip.getId()
        );

        if (trip.getDriver() != null) {
            notificationService.createAndSendNotification(
                    trip.getDriver().getUser().getId(),
                    NotificationType.INFO,
                    "Trip Status Updated",
                    "Trip status updated to: " + newStatus,
                    savedTrip.getId()
            );
        }

        return customResponseEntity.getOkResponse("Trip status updated", "ok", 200, null);
    }

    @Transactional
    public ResponseEntity<?> getDriverTripHistory(Long driverId) {
        List<Trip> trips = tripRepository.findByDriverId(driverId);
        if (trips.isEmpty()) {
            Map<String,Object> resp = new LinkedHashMap<>();
            resp.put("trips", Collections.emptyList());
            resp.put("totalIncome", 0.0);
            return customResponseEntity.getOkResponse("No hay viajes registrados", "ok", 200, resp);
        }
        double totalIncome = 0.0;
        List<TripDTO> out = new ArrayList<>();
        for (Trip t : trips) {
            out.add(convertToDTO(t));
            if (t.getStatus() == TripStatus.COMPLETED) {
                totalIncome += (t.getFare() != null ? t.getFare() : 0.0);
            }
        }
        Map<String,Object> resp = new LinkedHashMap<>();
        resp.put("trips", out);
        resp.put("totalIncome", totalIncome);
        return customResponseEntity.getOkResponse("Historial de viajes e ingresos", "ok", 200, resp);
    }

    /**
     * Allows the driver to mark a trip as completed. Only when the client
     * also marks it as completed does the status change to COMPLETED and the driver is released.
     */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> completeTripByDriver(Long driverId, Long tripId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), driverId)) {
            return customResponseEntity.get400Response("Trip is not assigned to this driver");
        }
        trip.setDriverCompleted(true);
        boolean bothCompleted = trip.isDriverCompleted() && trip.isClientCompleted();
        if (bothCompleted) {
            trip.setStatus(TripStatus.COMPLETED);
            DriverProfile driver = trip.getDriver();
            driver.setAvailability(DriverAvailability.DISPONIBLE);
            driverProfileRepository.save(driver);
        } else {
            trip.setStatus(TripStatus.IN_PROGRESS);
        }
        trip.setUpdatedAt(LocalDateTime.now());
        Trip savedTrip = tripRepository.save(trip);

        // Send WebSocket update to both parties
        tripWebSocketController.sendTripUpdateToBoth(savedTrip,
                bothCompleted ? "Trip completed!" : "Driver marked trip as completed");

        // Send notifications
        if (bothCompleted) {
            notificationService.createAndSendNotification(
                    trip.getClient().getId(),
                    NotificationType.OK,
                    "Trip Completed",
                    "Your trip has been completed successfully!",
                    savedTrip.getId()
            );
            notificationService.createAndSendNotification(
                    trip.getDriver().getUser().getId(),
                    NotificationType.OK,
                    "Trip Completed",
                    "Trip completed successfully!",
                    savedTrip.getId()
            );
        } else {
            notificationService.createAndSendNotification(
                    trip.getClient().getId(),
                    NotificationType.INFO,
                    "Driver Completed Trip",
                    "The driver has marked the trip as completed. Please confirm.",
                    savedTrip.getId()
            );
        }

        String msg = bothCompleted ?
                "Trip completed by both parties" :
                "Driver has marked the trip as completed. Waiting for client confirmation";
        return customResponseEntity.getOkResponse(msg, "ok", 200, null);
    }

    /**
     * Allows the client to mark a trip as completed. Only when the driver
     * also marks it as completed does the status change to COMPLETED.
     */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> completeTripByClient(Long clientId, Long tripId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (!Objects.equals(trip.getClient().getId(), clientId)) {
            return customResponseEntity.get400Response("Trip does not belong to this client");
        }
        trip.setClientCompleted(true);
        boolean bothCompleted = trip.isDriverCompleted() && trip.isClientCompleted();
        if (bothCompleted) {
            trip.setStatus(TripStatus.COMPLETED);
            DriverProfile driver = trip.getDriver();
            if (driver != null) {
                driver.setAvailability(DriverAvailability.DISPONIBLE);
                driverProfileRepository.save(driver);
            }
        } else {
            trip.setStatus(TripStatus.IN_PROGRESS);
        }
        trip.setUpdatedAt(LocalDateTime.now());
        Trip savedTrip = tripRepository.save(trip);

        // Send WebSocket update to both parties
        tripWebSocketController.sendTripUpdateToBoth(savedTrip,
                bothCompleted ? "Trip completed!" : "Client marked trip as completed");

        // Send notifications
        if (bothCompleted) {
            notificationService.createAndSendNotification(
                    trip.getClient().getId(),
                    NotificationType.OK,
                    "Trip Completed",
                    "Your trip has been completed successfully!",
                    savedTrip.getId()
            );
            if (trip.getDriver() != null) {
                notificationService.createAndSendNotification(
                        trip.getDriver().getUser().getId(),
                        NotificationType.OK,
                        "Trip Completed",
                        "Trip completed successfully!",
                        savedTrip.getId()
                );
            }
        } else {
            if (trip.getDriver() != null) {
                notificationService.createAndSendNotification(
                        trip.getDriver().getUser().getId(),
                        NotificationType.INFO,
                        "Client Completed Trip",
                        "The client has marked the trip as completed. Please confirm.",
                        savedTrip.getId()
                );
            }
        }

        String msg = bothCompleted ?
                "Trip completed by both parties" :
                "Client has marked the trip as completed. Waiting for driver confirmation";
        return customResponseEntity.getOkResponse(msg, "ok", 200, null);
    }

    /* =================== MÉTODOS AUXILIARES =================== */

    private double calculateFare(String origin, String destination) {
        return 50.0;
    }

    private TripDTO convertToDTO(Trip trip) {
        String clientName = null;
        String clientPhone = null;
        String driverName = null;
        String driverLicense = null;
        if (trip.getClient() != null) {
            clientName = String.format("%s %s %s",
                    trip.getClient().getName(),
                    trip.getClient().getSurname(),
                    trip.getClient().getLastname() != null ? trip.getClient().getLastname() : "").trim();
            clientPhone = trip.getClient().getPhoneNumber();
        }
        if (trip.getDriver() != null && trip.getDriver().getUser() != null) {
            User driverUser = trip.getDriver().getUser();
            driverName = String.format("%s %s %s",
                    driverUser.getName(),
                    driverUser.getSurname(),
                    driverUser.getLastname() != null ? driverUser.getLastname() : "").trim();
            driverLicense = trip.getDriver().getLicenseNumber();
        }
        return new TripDTO(
                trip.getId(),
                trip.getOrigin(),
                trip.getDestination(),
                trip.getFare(),
                trip.getStatus(),
                clientName,
                clientPhone,
                driverName,
                driverLicense,
                trip.getRating()
        );
    }

    /**
     * Validates state transitions in the flow ACCEPTED → IN_PROGRESS.
     * Simplified flow: drivers can start trip directly after accepting.
     */
    private boolean isValidTransition(TripStatus current, TripStatus next) {
        // Allow ACCEPTED → IN_PROGRESS when both parties confirm start
        if (current == TripStatus.ACCEPTED && next == TripStatus.IN_PROGRESS) return true;
        // Transition to COMPLETED is managed with confirmations
        return false;
    }
}
