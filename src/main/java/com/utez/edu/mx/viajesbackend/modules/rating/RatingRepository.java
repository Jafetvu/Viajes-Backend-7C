package com.utez.edu.mx.viajesbackend.modules.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Rating}.
 */
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Busca una calificación específica de un viaje realizada por un usuario concreto.
     *
     * @param tripId identificador del viaje
     * @param raterUserId identificador del usuario que califica
     * @return calificación correspondiente, o vacío si no existe
     */
    Optional<Rating> findByTripIdAndRaterUser_Id(Long tripId, Long raterUserId);

    /**
     * Devuelve las calificaciones recibidas por un usuario.
     *
     * @param ratedUserId identificador del usuario calificado
     * @return lista de calificaciones recibidas
     */
    List<Rating> findByRatedUser_Id(Long ratedUserId);

    /**
     * Busca todas las calificaciones asociadas a un viaje.
     *
     * @param tripId identificador del viaje
     * @return lista de calificaciones del viaje
     */
    List<Rating> findByTrip_Id(Long tripId);
}