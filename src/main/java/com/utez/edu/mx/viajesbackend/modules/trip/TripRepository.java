package com.utez.edu.mx.viajesbackend.modules.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Trip}. Permite operaciones CRUD
 * básicas y consultas específicas por cliente o conductor.
 */
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * Devuelve todos los viajes solicitados por un cliente concreto.
     *
     * @param clientId identificador del usuario que solicitó el viaje
     * @return lista de viajes del cliente
     */
    List<Trip> findByClientId(Long clientId);

    /**
     * Devuelve todos los viajes que ha atendido un determinado conductor.
     *
     * @param driverId identificador del perfil de conductor
     * @return lista de viajes atendidos por el conductor
     */
    List<Trip> findByDriverId(Long driverId);

    // Count trips by status
    long countByStatus(TripStatus status);

    // Count trips by multiple statuses
    long countByStatusIn(List<TripStatus> statuses);

    // Sum fare for trips with specific status
    @Query("SELECT SUM(t.fare) FROM Trip t WHERE t.status = :status")
    Double sumFareByStatus(@Param("status") TripStatus status);
}
