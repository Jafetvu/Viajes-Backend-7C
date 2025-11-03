package com.utez.edu.mx.viajesbackend.modules.trip;

import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverAvailability;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfile;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfileRepository;
import com.utez.edu.mx.viajesbackend.modules.trip.DTO.*;
import com.utez.edu.mx.viajesbackend.modules.user.User;
import com.utez.edu.mx.viajesbackend.modules.user.UserRepository;
import com.utez.edu.mx.viajesbackend.utils.CustomResponseEntity;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio principal para gestionar la lógica de negocio relacionada con los viajes.
 * Permite a los clientes solicitar viajes, a los conductores aceptar o rechazar
 * solicitudes, actualizar el estado del viaje y consultar historiales tanto
 * de clientes como de conductores. Todas las respuestas se generan a través
 * del utilitario {@link CustomResponseEntity} para mantener un formato uniforme.
 */
@Service
public class TripService {

    private static final int ROLE_DRIVER_ID = 3;

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final CustomResponseEntity customResponseEntity;

    public TripService(TripRepository tripRepository,
                       UserRepository userRepository,
                       DriverProfileRepository driverProfileRepository,
                       CustomResponseEntity customResponseEntity) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.customResponseEntity = customResponseEntity;
    }

    /**
     * Crea una nueva solicitud de viaje para un cliente. No asigna automáticamente
     * un conductor; registra la solicitud con estado {@link TripStatus#SOLICITADO}
     * para que los conductores la acepten o rechacen.
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
        trip.setStatus(TripStatus.SOLICITADO);
        LocalDateTime now = LocalDateTime.now();
        trip.setCreatedAt(now);
        trip.setUpdatedAt(now);
        tripRepository.save(trip);

        TripDTO responseDto = convertToDTO(trip);
        return customResponseEntity.getOkResponse("Viaje solicitado correctamente", "ok", 200, responseDto);
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
        if (trip.getStatus() == TripStatus.COMPLETADO) {
            return customResponseEntity.get400Response("No se puede cancelar un viaje completado");
        }
        trip.setStatus(TripStatus.CANCELADO);
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
        if (trip.getStatus() != TripStatus.COMPLETADO) {
            return customResponseEntity.get400Response("Solo se pueden calificar viajes completados");
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
            if (t.getStatus() != TripStatus.CANCELADO && t.getStatus() != TripStatus.COMPLETADO) {
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
            if (t.getStatus() == TripStatus.SOLICITADO) {
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
        if (trip.getStatus() != TripStatus.SOLICITADO || trip.getDriver() != null) {
            return customResponseEntity.get400Response("El viaje no está disponible para ser aceptado");
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
        trip.setStatus(TripStatus.ACEPTADO);
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        driver.setAvailability(DriverAvailability.EN_VIAJE);
        driverProfileRepository.save(driver);
        return customResponseEntity.getOkResponse("Viaje aceptado", "ok", 200, null);
    }

    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> rejectTrip(Long tripId, Long driverId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (trip.getStatus() == TripStatus.SOLICITADO && trip.getDriver() == null) {
            trip.setStatus(TripStatus.CANCELADO);
            trip.setUpdatedAt(LocalDateTime.now());
            tripRepository.save(trip);
            return customResponseEntity.getOkResponse("Viaje rechazado y cancelado", "ok", 200, null);
        }
        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), driverId)) {
            return customResponseEntity.get400Response("El viaje no está asignado a este conductor");
        }
        if (trip.getStatus() != TripStatus.ACEPTADO) {
            return customResponseEntity.get400Response("Solo se pueden rechazar viajes aceptados que aún no hayan iniciado");
        }
        trip.setStatus(TripStatus.CANCELADO);
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        DriverProfile driver = trip.getDriver();
        driver.setAvailability(DriverAvailability.DISPONIBLE);
        driverProfileRepository.save(driver);
        return customResponseEntity.getOkResponse("Viaje rechazado", "ok", 200, null);
    }

    /**
     * Actualiza el estado del viaje en el flujo: ACEPTADO → EN_CAMINO → EN_CURSO.
     * La transición a COMPLETADO se gestiona por separado (ambos usuarios deben confirmar).
     */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> updateTripStatus(Long driverId, TripStatusUpdateDTO dto) {
        Optional<Trip> maybe = tripRepository.findById(dto.getTripId());
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), driverId)) {
            return customResponseEntity.get400Response("El viaje no está asignado a este conductor");
        }
        TripStatus current = trip.getStatus();
        TripStatus newStatus = dto.getStatus();
        if (!isValidTransition(current, newStatus)) {
            return customResponseEntity.get400Response("Transición de estado no permitida");
        }
        trip.setStatus(newStatus);
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        return customResponseEntity.getOkResponse("Estado del viaje actualizado", "ok", 200, null);
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
            if (t.getStatus() == TripStatus.COMPLETADO) {
                totalIncome += (t.getFare() != null ? t.getFare() : 0.0);
            }
        }
        Map<String,Object> resp = new LinkedHashMap<>();
        resp.put("trips", out);
        resp.put("totalIncome", totalIncome);
        return customResponseEntity.getOkResponse("Historial de viajes e ingresos", "ok", 200, resp);
    }

    /**
     * Permite al conductor marcar un viaje como completado. Sólo cuando el cliente
     * también lo marque como completado el estado cambia a COMPLETADO y se libera el conductor.
     */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> completeTripByDriver(Long driverId, Long tripId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), driverId)) {
            return customResponseEntity.get400Response("El viaje no está asignado a este conductor");
        }
        trip.setDriverCompleted(true);
        boolean bothCompleted = trip.isDriverCompleted() && trip.isClientCompleted();
        if (bothCompleted) {
            trip.setStatus(TripStatus.COMPLETADO);
            DriverProfile driver = trip.getDriver();
            driver.setAvailability(DriverAvailability.DISPONIBLE);
            driverProfileRepository.save(driver);
        } else {
            trip.setStatus(TripStatus.EN_CURSO);
        }
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        String msg = bothCompleted ?
                "Viaje completado por ambas partes" :
                "El conductor ha marcado el viaje como completado. Esperando confirmación del cliente";
        return customResponseEntity.getOkResponse(msg, "ok", 200, null);
    }

    /**
     * Permite al cliente marcar un viaje como completado. Sólo cuando el conductor
     * también lo marque como completado el estado cambia a COMPLETADO.
     */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> completeTripByClient(Long clientId, Long tripId) {
        Optional<Trip> maybe = tripRepository.findById(tripId);
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (!Objects.equals(trip.getClient().getId(), clientId)) {
            return customResponseEntity.get400Response("El viaje no pertenece a este cliente");
        }
        trip.setClientCompleted(true);
        boolean bothCompleted = trip.isDriverCompleted() && trip.isClientCompleted();
        if (bothCompleted) {
            trip.setStatus(TripStatus.COMPLETADO);
            DriverProfile driver = trip.getDriver();
            if (driver != null) {
                driver.setAvailability(DriverAvailability.DISPONIBLE);
                driverProfileRepository.save(driver);
            }
        } else {
            trip.setStatus(TripStatus.EN_CURSO);
        }
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);
        String msg = bothCompleted ?
                "Viaje completado por ambas partes" :
                "El cliente ha marcado el viaje como completado. Esperando confirmación del conductor";
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
     * Valida la transición de estados en el flujo ACEPTADO → EN_CAMINO → EN_CURSO.
     */
    private boolean isValidTransition(TripStatus current, TripStatus next) {
        if (current == TripStatus.ACEPTADO && next == TripStatus.EN_CAMINO) return true;
        if (current == TripStatus.EN_CAMINO && next == TripStatus.EN_CURSO) return true;
        // La transición a COMPLETADO se gestiona con confirmaciones
        return false;
    }
}
