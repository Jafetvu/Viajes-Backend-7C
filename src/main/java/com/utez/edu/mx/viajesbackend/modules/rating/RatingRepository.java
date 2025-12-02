package com.utez.edu.mx.viajesbackend.modules.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Rating}. Proporciona métodos para
 * consultar calificaciones asociadas a un viaje y filtrar por el origen
 * (cliente o conductor), así como para obtener todas las calificaciones
 * recibidas por un conductor o un cliente.
 */
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Busca una calificación específica de un viaje según si proviene del cliente.
     *
     * @param tripId identificador del viaje
     * @param fromClient indica si la calificación proviene del cliente
     * @return calificación correspondiente, o vacío si no existe
     */
    Optional<Rating> findByTripIdAndFromClient(Long tripId, Boolean fromClient);

    /**
     * Devuelve las calificaciones otorgadas por clientes hacia un conductor.
     *
     * @param driverId identificador del conductor
     * @param fromClient debe ser {@code true} para indicar que provienen de clientes
     * @return lista de calificaciones recibidas por el conductor
     */
    List<Rating> findByTrip_Driver_IdAndFromClient(Long driverId, Boolean fromClient);

    /**
     * Devuelve las calificaciones otorgadas por conductores hacia un cliente.
     *
     * @param clientId identificador del cliente
     * @param fromClient debe ser {@code false} para indicar que provienen de conductores
     * @return lista de calificaciones recibidas por el cliente
     */
    List<Rating> findByTrip_Client_IdAndFromClient(Long clientId, Boolean fromClient);
}
