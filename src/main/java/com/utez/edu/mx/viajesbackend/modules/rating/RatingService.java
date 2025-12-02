package com.utez.edu.mx.viajesbackend.modules.rating;

import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfile;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfileRepository;
import com.utez.edu.mx.viajesbackend.modules.trip.TripStatus;
import com.utez.edu.mx.viajesbackend.modules.rating.DTO.RatingRequestDTO;
import com.utez.edu.mx.viajesbackend.modules.rating.DTO.RatingResponseDTO;
import com.utez.edu.mx.viajesbackend.modules.trip.Trip;
import com.utez.edu.mx.viajesbackend.modules.trip.TripRepository;
import com.utez.edu.mx.viajesbackend.modules.trip.TripStatus;
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
 * Servicio que encapsula la lógica para crear y consultar calificaciones.
 * Permite que un cliente califique a un conductor y viceversa, siempre
 * asegurando que el viaje se haya completado y que no existan duplicados.
 */
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final CustomResponseEntity customResponseEntity;

    public RatingService(RatingRepository ratingRepository,
                         TripRepository tripRepository,
                         UserRepository userRepository,
                         DriverProfileRepository driverProfileRepository,
                         CustomResponseEntity customResponseEntity) {
        this.ratingRepository = ratingRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.customResponseEntity = customResponseEntity;
    }

    /**
     * Permite al cliente calificar al conductor. Se verifica que el viaje exista,
     * pertenezca al cliente, esté completado y que no exista ya una calificación
     * del cliente para ese viaje.
     *
     * @param clientId identificador del cliente que califica
     * @param dto datos de la calificación
     * @return respuesta HTTP con el resultado
     */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> rateDriver(Long clientId, RatingRequestDTO dto) {
        Optional<Trip> maybe = tripRepository.findById(dto.getTripId());
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (!Objects.equals(trip.getClient().getId(), clientId)) {
            return customResponseEntity.get400Response("El viaje no pertenece al cliente");
        }
        if (trip.getStatus() != TripStatus.COMPLETED) {
            return customResponseEntity.get400Response("Solo se pueden calificar viajes completados");
        }
        // Verificar que el cliente no haya calificado previamente
        Optional<Rating> existing = ratingRepository.findByTripIdAndFromClient(trip.getId(), true);
        if (existing.isPresent()) {
            return customResponseEntity.get400Response("El viaje ya ha sido calificado por el cliente");
        }
        Rating rating = new Rating();
        rating.setTrip(trip);
        rating.setFromClient(true);
        rating.setRating(dto.getRating());
        rating.setComment(dto.getComment());
        rating.setCreatedAt(LocalDateTime.now());
        ratingRepository.save(rating);
        return customResponseEntity.getOkResponse("Calificación registrada", "ok", 200, null);
    }

    /**
     * Permite al conductor calificar al cliente. Se verifica que el viaje exista,
     * esté asignado al conductor correspondiente, esté completado y que no exista
     * ya una calificación del conductor para ese viaje.
     *
     * @param driverId identificador del conductor que califica
     * @param dto datos de la calificación
     * @return respuesta HTTP con el resultado
     */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> rateClient(Long driverId, RatingRequestDTO dto) {
        Optional<Trip> maybe = tripRepository.findById(dto.getTripId());
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();
        if (trip.getDriver() == null || !Objects.equals(trip.getDriver().getId(), driverId)) {
            return customResponseEntity.get400Response("El viaje no está asignado a este conductor");
        }
        if (trip.getStatus() != TripStatus.COMPLETED) {
            return customResponseEntity.get400Response("Solo se pueden calificar viajes completados");
        }
        // Verificar que el conductor no haya calificado previamente al cliente
        Optional<Rating> existing = ratingRepository.findByTripIdAndFromClient(trip.getId(), false);
        if (existing.isPresent()) {
            return customResponseEntity.get400Response("El viaje ya ha sido calificado por el conductor");
        }
        Rating rating = new Rating();
        rating.setTrip(trip);
        rating.setFromClient(false);
        rating.setRating(dto.getRating());
        rating.setComment(dto.getComment());
        rating.setCreatedAt(LocalDateTime.now());
        ratingRepository.save(rating);
        return customResponseEntity.getOkResponse("Calificación registrada", "ok", 200, null);
    }

    /**
     * Obtiene el promedio y la lista de calificaciones recibidas por un conductor.
     *
     * @param driverId identificador del conductor
     * @return respuesta HTTP con el promedio y el listado de calificaciones
     */
    @Transactional
    public ResponseEntity<?> getDriverRatings(Long driverId) {
        // Verificar que el conductor exista
        Optional<DriverProfile> driverOpt = driverProfileRepository.findById(driverId);
        if (driverOpt.isEmpty()) return customResponseEntity.get404Response();
        List<Rating> ratings = ratingRepository.findByTrip_Driver_IdAndFromClient(driverId, true);
        if (ratings.isEmpty()) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("average", null);
            resp.put("ratings", Collections.emptyList());
            return customResponseEntity.getOkResponse("No hay calificaciones registradas", "ok", 200, resp);
        }
        double sum = 0.0;
        List<RatingResponseDTO> out = new ArrayList<>();
        for (Rating r : ratings) {
            sum += (r.getRating() != null ? r.getRating() : 0);
            // Nombre del cliente que emite la calificación
            User rater = r.getTrip().getClient();
            String raterName = null;
            if (rater != null) {
                raterName = String.format("%s %s %s",
                        rater.getName(),
                        rater.getSurname(),
                        rater.getLastname() != null ? rater.getLastname() : "").trim();
            }
            RatingResponseDTO dtoOut = new RatingResponseDTO(
                    r.getId(),
                    r.getRating(),
                    r.getComment(),
                    r.getFromClient(),
                    raterName,
                    r.getCreatedAt()
            );
            out.add(dtoOut);
        }
        double avg = sum / ratings.size();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("average", avg);
        resp.put("ratings", out);
        return customResponseEntity.getOkResponse("Calificaciones del conductor", "ok", 200, resp);
    }

    /**
     * Obtiene el promedio y la lista de calificaciones recibidas por un cliente.
     *
     * @param clientId identificador del cliente
     * @return respuesta HTTP con el promedio y el listado de calificaciones
     */
    @Transactional
    public ResponseEntity<?> getClientRatings(Long clientId) {
        // Verificar que el cliente exista
        User client = userRepository.findById(clientId).orElse(null);
        if (client == null) return customResponseEntity.get404Response();
        List<Rating> ratings = ratingRepository.findByTrip_Client_IdAndFromClient(clientId, false);
        if (ratings.isEmpty()) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("average", null);
            resp.put("ratings", Collections.emptyList());
            return customResponseEntity.getOkResponse("No hay calificaciones registradas", "ok", 200, resp);
        }
        double sum = 0.0;
        List<RatingResponseDTO> out = new ArrayList<>();
        for (Rating r : ratings) {
            sum += (r.getRating() != null ? r.getRating() : 0);
            // Nombre del conductor que emite la calificación
            DriverProfile rater = r.getTrip().getDriver();
            String raterName = null;
            if (rater != null && rater.getUser() != null) {
                User u = rater.getUser();
                raterName = String.format("%s %s %s",
                        u.getName(),
                        u.getSurname(),
                        u.getLastname() != null ? u.getLastname() : "").trim();
            }
            RatingResponseDTO dtoOut = new RatingResponseDTO(
                    r.getId(),
                    r.getRating(),
                    r.getComment(),
                    r.getFromClient(),
                    raterName,
                    r.getCreatedAt()
            );
            out.add(dtoOut);
        }
        double avg = sum / ratings.size();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("average", avg);
        resp.put("ratings", out);
        return customResponseEntity.getOkResponse("Calificaciones del cliente", "ok", 200, resp);
    }
}
