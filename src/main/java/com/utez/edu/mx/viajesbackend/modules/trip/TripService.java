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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Main service for managing business logic related to trips.
 */
@Service
public class TripService {

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

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> requestTrip(TripRequestDTO dto) {
        Optional<User> client = userRepository.findById(dto.getClientId());
        if (client.isEmpty()) {
            return customResponseEntity.get404Response();
        }
        // Pass coordinates to fare calculation if needed, or just address
        double fare = calculateFare(dto.getOriginAddress(), dto.getDestinationAddress());

        Trip trip = new Trip();
        trip.setClient(client.orElse(null));
        trip.setOriginAddress(dto.getOriginAddress());
        trip.setOriginLatitude(dto.getOriginLatitude());
        trip.setOriginLongitude(dto.getOriginLongitude());
        
        trip.setDestinationAddress(dto.getDestinationAddress());
        trip.setDestinationLatitude(dto.getDestinationLatitude());
        trip.setDestinationLongitude(dto.getDestinationLongitude());
        
        trip.setFare(fare);
        trip.setStatus(TripStatus.REQUESTED);
        LocalDateTime now = LocalDateTime.now();
        trip.setCreatedAt(now);
        trip.setUpdatedAt(now);
        Trip savedTrip = tripRepository.save(trip);

        tripWebSocketController.broadcastNewTripToDrivers(savedTrip);
        
        notificationService.createAndSendNotification(
                client.get().getId(),
                NotificationType.OK,
                "Trip Requested",
                "Your trip has been requested.",
                savedTrip.getId()
        );

        return customResponseEntity.getOkResponse("Trip requested", "ok", 200, convertToDTO(savedTrip));
    }

    @Transactional
    public ResponseEntity<?> getTripDetails(Long tripId, Long clientId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (!Objects.equals(trip.getClient().getId(), clientId)) {
            return customResponseEntity.get400Response("Unauthorized");
        }
        return customResponseEntity.getOkResponse("Detalle del viaje", "ok", 200, convertToDTO(trip));
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> cancelTrip(Long tripId, Long clientId, String reason) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (!Objects.equals(trip.getClient().getId(), clientId)) {
            return customResponseEntity.get400Response("Unauthorized");
        }
        if (trip.getStatus() == TripStatus.COMPLETED) {
            return customResponseEntity.get400Response("Cannot cancel completed trip");
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
            return customResponseEntity.get400Response("Unauthorized");
        }
        if (trip.getStatus() != TripStatus.COMPLETED) {
            return customResponseEntity.get400Response("Trip not completed");
        }
        trip.setRating(dto.getRating());
        trip.setComment(dto.getComment());
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        return customResponseEntity.getOkResponse("Rated", "ok", 200, null);
    }

    @Transactional
    public ResponseEntity<?> getClientTripHistory(Long clientId) {
        List<Trip> trips = tripRepository.findByClientId(clientId);
        List<TripDTO> out = new ArrayList<>();
        for (Trip t : trips) {
            out.add(convertToDTO(t));
        }
        return customResponseEntity.getOkResponse("History", "ok", 200, out);
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
        return customResponseEntity.getOkResponse("Assigned trips", "ok", 200, out);
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
        return customResponseEntity.getOkResponse("Available trips", "ok", 200, out);
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> acceptTrip(Long tripId, Long driverId, Long userId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (trip.getStatus() != TripStatus.REQUESTED || trip.getDriver() != null) {
            return customResponseEntity.get400Response("Unavailable");
        }

        DriverProfile driver = null;
        if (driverId != null) {
            driver = driverProfileRepository.findById(driverId).orElse(null);
        }
        if (driver == null && userId != null) {
            driver = driverProfileRepository.findByUserId(userId).orElse(null);
            if (driver == null) {
                // Auto-create profile if not exists
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent() && "CONDUCTOR".equalsIgnoreCase(userOpt.get().getRole().getName())) {
                    DriverProfile newProfile = new DriverProfile();
                    newProfile.setUser(userOpt.get());
                    newProfile.setLicenseNumber("LIC-" + System.currentTimeMillis());
                    newProfile.setAvailability(DriverAvailability.DISPONIBLE);
                    driver = driverProfileRepository.save(newProfile);
                }
            }
        }

        if (driver == null) return customResponseEntity.get404Response();
        if (driver.getAvailability() != DriverAvailability.DISPONIBLE) {
            return customResponseEntity.get400Response("Driver not available");
        }

        trip.setDriver(driver);
        trip.setStatus(TripStatus.ACCEPTED);
        trip.setUpdatedAt(LocalDateTime.now());
        Trip savedTrip = tripRepository.save(trip);
        driver.setAvailability(DriverAvailability.EN_VIAJE);
        driverProfileRepository.save(driver);

        tripWebSocketController.sendTripUpdateToClient(trip.getClient().getUsername(), savedTrip, "Trip accepted");
        notificationService.createAndSendNotification(trip.getClient().getId(), NotificationType.OK, "Trip Accepted", "Driver assigned", savedTrip.getId());

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
             return customResponseEntity.getOkResponse("Trip cancelled", "ok", 200, null);
        }

        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), driverId)) {
            return customResponseEntity.get400Response("Unauthorized");
        }
        trip.setStatus(TripStatus.CANCELLED);
        trip.setUpdatedAt(LocalDateTime.now());
        Trip savedTrip = tripRepository.save(trip);
        trip.getDriver().setAvailability(DriverAvailability.DISPONIBLE);
        driverProfileRepository.save(trip.getDriver());

        tripWebSocketController.sendTripUpdateToClient(trip.getClient().getUsername(), savedTrip, "Trip rejected");
        notificationService.createAndSendNotification(trip.getClient().getId(), NotificationType.WARN, "Trip Cancelled", "Driver cancelled", savedTrip.getId());

        return customResponseEntity.getOkResponse("Trip rejected", "ok", 200, null);
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> notifyArrival(Long tripId, Long driverId, Long userId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();

        Long effectiveDriverId = driverId;
        if (effectiveDriverId == null && userId != null) {
            Optional<DriverProfile> dp = driverProfileRepository.findByUserId(userId);
            if (dp.isPresent()) effectiveDriverId = dp.get().getId();
        }

        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), effectiveDriverId)) {
             return customResponseEntity.get400Response("Unauthorized");
        }
        
        // Send notification/WS update with a specific message or custom field
        // We reuse TripUpdateMessage but the 'message' field will carry the event
        tripWebSocketController.sendTripUpdateToClient(trip.getClient().getUsername(), trip, "DRIVER_ARRIVED");
        notificationService.createAndSendNotification(
                trip.getClient().getId(),
                NotificationType.INFO,
                "Driver Arrived",
                "Your driver has arrived at the pickup location.",
                trip.getId()
        );
        
        return customResponseEntity.getOkResponse("Arrival notified", "ok", 200, null);
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> notifyDropoff(Long tripId, Long driverId, Long userId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();

        Long effectiveDriverId = driverId;
        if (effectiveDriverId == null && userId != null) {
            Optional<DriverProfile> dp = driverProfileRepository.findByUserId(userId);
            if (dp.isPresent()) effectiveDriverId = dp.get().getId();
        }

        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), effectiveDriverId)) {
            return customResponseEntity.get400Response("Unauthorized");
        }

        // Send notification/WS update
        tripWebSocketController.sendTripUpdateToClient(trip.getClient().getUsername(), trip, "DROPOFF_ARRIVED");
        // Also send to driver so their UI can update if needed (optional but good for consistency)
        tripWebSocketController.sendTripUpdateToDriver(trip.getDriver().getUser().getUsername(), trip, "DROPOFF_ARRIVED");

        notificationService.createAndSendNotification(
                trip.getClient().getId(),
                NotificationType.INFO,
                "Arrived at Destination",
                "Driver has arrived at the destination.",
                trip.getId()
        );

        return customResponseEntity.getOkResponse("Dropoff arrival notified", "ok", 200, null);
    }

    // --- START TRIP LOGIC (DUAL CONFIRMATION) ---

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> startTripByDriver(Long driverId, Long userId, Long tripId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();

        Long effectiveDriverId = driverId;
        if (effectiveDriverId == null && userId != null) {
            DriverProfile dp = driverProfileRepository.findByUserId(userId).orElse(null);
            if (dp != null) effectiveDriverId = dp.getId();
        }

        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), effectiveDriverId)) {
            return customResponseEntity.get400Response("Unauthorized");
        }

        trip.setDriverStarted(true);
        return checkAndStartTrip(trip);
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> startTripByClient(Long clientId, Long tripId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();

        if (!Objects.equals(trip.getClient().getId(), clientId)) {
            return customResponseEntity.get400Response("Unauthorized");
        }

        trip.setClientStarted(true);
        return checkAndStartTrip(trip);
    }

    private ResponseEntity<?> checkAndStartTrip(Trip trip) {
        boolean bothStarted = trip.isDriverStarted() && trip.isClientStarted();
        
        if (bothStarted) {
            trip.setStatus(TripStatus.IN_PROGRESS);
            trip.setUpdatedAt(LocalDateTime.now());
            
            // Notify both
            tripWebSocketController.sendTripUpdateToBoth(trip, "Trip Started");
            notificationService.createAndSendNotification(trip.getClient().getId(), NotificationType.INFO, "Trip Started", "Trip is now in progress", trip.getId());
            notificationService.createAndSendNotification(trip.getDriver().getUser().getId(), NotificationType.INFO, "Trip Started", "Trip is now in progress", trip.getId());
        } else {
            // Notify the other party that one has confirmed
            tripWebSocketController.sendTripUpdateToBoth(trip, "Waiting for start confirmation");
        }
        
        tripRepository.save(trip);
        
        String msg = bothStarted ? "Trip started" : "Confirmation received. Waiting for other party.";
        return customResponseEntity.getOkResponse(msg, "ok", 200, null);
    }

    // Legacy update method (mapped to start by driver for simplicity if needed, but deprecated)
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> updateTripStatus(Long driverId, Long userId, TripStatusUpdateDTO dto) {
        if (dto.getStatus() == TripStatus.IN_PROGRESS) {
            return startTripByDriver(driverId, userId, dto.getTripId());
        }
        return customResponseEntity.get400Response("Use specific endpoints");
    }

    // --- COMPLETE TRIP LOGIC (DUAL CONFIRMATION) ---

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> completeTripByDriver(Long driverId, Long userId, Long tripId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();

        Long effectiveDriverId = driverId;
        if (effectiveDriverId == null && userId != null) {
            DriverProfile dp = driverProfileRepository.findByUserId(userId).orElse(null);
            if (dp != null) effectiveDriverId = dp.getId();
        }

        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), effectiveDriverId)) {
            return customResponseEntity.get400Response("Unauthorized");
        }

        trip.setDriverCompleted(true);
        return checkAndCompleteTrip(trip);
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> completeTripByClient(Long clientId, Long tripId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();

        if (!Objects.equals(trip.getClient().getId(), clientId)) {
            return customResponseEntity.get400Response("Unauthorized");
        }

        trip.setClientCompleted(true);
        return checkAndCompleteTrip(trip);
    }

    private ResponseEntity<?> checkAndCompleteTrip(Trip trip) {
        boolean bothCompleted = trip.isDriverCompleted() && trip.isClientCompleted();

        if (bothCompleted) {
            trip.setStatus(TripStatus.COMPLETED);
            trip.setUpdatedAt(LocalDateTime.now());
            
            // Free driver
            DriverProfile driver = trip.getDriver();
            driver.setAvailability(DriverAvailability.DISPONIBLE);
            driverProfileRepository.save(driver);

            // Notify both
            tripWebSocketController.sendTripUpdateToBoth(trip, "Trip Completed");
            notificationService.createAndSendNotification(trip.getClient().getId(), NotificationType.OK, "Trip Completed", "Trip completed successfully", trip.getId());
            notificationService.createAndSendNotification(trip.getDriver().getUser().getId(), NotificationType.OK, "Trip Completed", "Trip completed successfully", trip.getId());
        } else {
            tripWebSocketController.sendTripUpdateToBoth(trip, "Waiting for completion confirmation");
        }

        tripRepository.save(trip);
        
        String msg = bothCompleted ? "Trip completed" : "Confirmation received. Waiting for other party.";
        return customResponseEntity.getOkResponse(msg, "ok", 200, null);
    }

    @Transactional
    public ResponseEntity<?> getDriverTripHistory(Long driverId) {
        List<Trip> trips = tripRepository.findByDriverId(driverId);
        List<TripDTO> out = new ArrayList<>();
        double income = 0;
        for(Trip t : trips) {
            out.add(convertToDTO(t));
            if(t.getStatus() == TripStatus.COMPLETED) income += t.getFare();
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("trips", out);
        resp.put("totalIncome", income);
        return customResponseEntity.getOkResponse("History", "ok", 200, resp);
    }

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
                trip.getOriginAddress(),
                trip.getOriginLatitude(),
                trip.getOriginLongitude(),
                trip.getDestinationAddress(),
                trip.getDestinationLatitude(),
                trip.getDestinationLongitude(),
                trip.getFare(),
                trip.getStatus(),
                clientName,
                clientPhone,
                driverName,
                driverLicense,
                trip.getRating(),
                trip.getCreatedAt()
        );
    }

}