package com.utez.edu.mx.viajesbackend.modules.rating;

import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfile;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfileRepository;
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
     * Permite al cliente calificar al conductor.
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

        // Rater: Cliente (Usuario)
        User rater = trip.getClient();
        // Rated: Conductor (Usuario asociado al perfil)
        if (trip.getDriver() == null || trip.getDriver().getUser() == null) {
            return customResponseEntity.get400Response("El viaje no tiene conductor asignado válido");
        }
        User rated = trip.getDriver().getUser();

        // Verificar que el cliente no haya calificado previamente este viaje
        Optional<Rating> existing = ratingRepository.findByTripIdAndRaterUser_Id(trip.getId(), rater.getId());
        if (existing.isPresent()) {
            return customResponseEntity.get400Response("El viaje ya ha sido calificado por el cliente");
        }

        Rating rating = new Rating();
        rating.setTrip(trip);
        rating.setRaterUser(rater);
        rating.setRatedUser(rated);
        rating.setRating(dto.getRating());
        rating.setComment(dto.getComment());
        rating.setCreatedAt(LocalDateTime.now());
        ratingRepository.save(rating);
        return customResponseEntity.getOkResponse("Calificación registrada", "ok", 200, null);
    }

    /**
     * Permite al conductor calificar al cliente.
     *
     * @param driverId identificador del conductor (DriverProfile ID) que califica
     * @param dto datos de la calificación
     * @return respuesta HTTP con el resultado
     */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> rateClient(Long driverId, RatingRequestDTO dto) {
        Optional<Trip> maybe = tripRepository.findById(dto.getTripId());
        if (maybe.isEmpty()) return customResponseEntity.get404Response();
        Trip trip = maybe.get();

        if (trip.getDriver() == null) {
            return customResponseEntity.get400Response("El viaje no tiene conductor asignado");
        }

        boolean isProfileId = Objects.equals(trip.getDriver().getId(), driverId);
        boolean isUserId = trip.getDriver().getUser() != null && Objects.equals(trip.getDriver().getUser().getId(), driverId);

        if (!isProfileId && !isUserId) {
            return customResponseEntity.get400Response("El viaje no está asignado a este conductor");
        }
        if (trip.getStatus() != TripStatus.COMPLETED) {
            return customResponseEntity.get400Response("Solo se pueden calificar viajes completados");
        }
        
        // Rater: Conductor (Usuario)
        if (trip.getDriver().getUser() == null) {
            return customResponseEntity.get400Response("El conductor no tiene un usuario válido asociado");
        }
        User rater = trip.getDriver().getUser();
        // Rated: Cliente (Usuario)
        User rated = trip.getClient();

        // Verificar que el conductor no haya calificado previamente al cliente
        Optional<Rating> existing = ratingRepository.findByTripIdAndRaterUser_Id(trip.getId(), rater.getId());
        if (existing.isPresent()) {
            return customResponseEntity.get400Response("El viaje ya ha sido calificado por el conductor");
        }

        Rating rating = new Rating();
        rating.setTrip(trip);
        rating.setRaterUser(rater);
        rating.setRatedUser(rated);
        rating.setRating(dto.getRating());
        rating.setComment(dto.getComment());
        rating.setCreatedAt(LocalDateTime.now());
        ratingRepository.save(rating);
        return customResponseEntity.getOkResponse("Calificación registrada", "ok", 200, null);
    }

    /**
     * Obtiene el promedio y la lista de calificaciones recibidas por un conductor.
     *
     * @param driverId identificador del perfil del conductor
     * @return respuesta HTTP con el promedio y el listado de calificaciones
     */
    @Transactional
    public ResponseEntity<?> getDriverRatings(Long driverId) {
        Optional<DriverProfile> driverOpt = driverProfileRepository.findById(driverId);
        if (driverOpt.isEmpty()) return customResponseEntity.get404Response();
        
        User driverUser = driverOpt.get().getUser();
        if (driverUser == null) return customResponseEntity.get404Response();

        return getRatingsForUser(driverUser, "Calificaciones del conductor");
    }

    /**
     * Obtiene el promedio y la lista de calificaciones recibidas por un cliente.
     *
     * @param clientId identificador del cliente (User ID)
     * @return respuesta HTTP con el promedio y el listado de calificaciones
     */
    @Transactional
    public ResponseEntity<?> getClientRatings(Long clientId) {
        Optional<User> clientOpt = userRepository.findById(clientId);
        if (clientOpt.isEmpty()) return customResponseEntity.get404Response();

        return getRatingsForUser(clientOpt.get(), "Calificaciones del cliente");
    }

    /**
     * Método auxiliar para obtener calificaciones de un usuario genérico.
     */
    private ResponseEntity<?> getRatingsForUser(User user, String message) {
        List<Rating> ratings = ratingRepository.findByRatedUser_Id(user.getId());

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
            
            User rater = r.getRaterUser();
            String raterName = "Desconocido";
            if (rater != null) {
                raterName = String.format("%s %s %s",
                        rater.getName(),
                        rater.getSurname(),
                        rater.getLastname() != null ? rater.getLastname() : "").trim();
            }

            RatingResponseDTO dtoOut = new RatingResponseDTO(
                    r.getId(),
                    r.getTrip().getId(),
                    r.getRating(),
                    r.getComment(),
                    raterName,
                    r.getCreatedAt()
            );
            out.add(dtoOut);
        }
        double avg = sum / ratings.size();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("average", avg);
        resp.put("ratings", out);
        return customResponseEntity.getOkResponse(message, "ok", 200, resp);
    }

    /**
     * Obtiene las calificaciones asociadas a un viaje específico.
     *
     * @param tripId identificador del viaje
     * @return lista de calificaciones del viaje
     */
    @Transactional
    public ResponseEntity<?> getRatingsByTrip(Long tripId) {
        List<Rating> ratings = ratingRepository.findByTrip_Id(tripId);
        List<RatingResponseDTO> out = new ArrayList<>();
        
        for (Rating r : ratings) {
            User rater = r.getRaterUser();
            String raterName = "Desconocido";
            if (rater != null) {
                raterName = String.format("%s %s %s",
                        rater.getName(),
                        rater.getSurname(),
                        rater.getLastname() != null ? rater.getLastname() : "").trim();
            }

            out.add(new RatingResponseDTO(
                    r.getId(),
                    r.getTrip().getId(),
                    r.getRating(),
                    r.getComment(),
                    raterName,
                    r.getCreatedAt()
            ));
        }
        return customResponseEntity.getOkResponse("Calificaciones del viaje", "ok", 200, out);
    }
}