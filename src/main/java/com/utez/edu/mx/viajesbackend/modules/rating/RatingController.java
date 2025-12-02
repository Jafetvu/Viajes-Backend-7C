package com.utez.edu.mx.viajesbackend.modules.rating;

import com.utez.edu.mx.viajesbackend.modules.rating.DTO.RatingRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para exponer los endpoints de calificaciones.
 * Separamos las acciones de clientes y conductores para mayor claridad.
 */
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * Endpoint para que un cliente califique a un conductor.
     *
     * @param clientId identificador del cliente (en query param)
     * @param dto cuerpo con datos de la calificación
     * @return respuesta de negocio
     */
    @PostMapping("/driver")
    public ResponseEntity<?> rateDriver(@RequestParam Long clientId,
                                        @Valid @RequestBody RatingRequestDTO dto) {
        return ratingService.rateDriver(clientId, dto);
    }

    /**
     * Endpoint para que un conductor califique a un cliente.
     *
     * @param driverId identificador del conductor (en query param)
     * @param dto cuerpo con datos de la calificación
     * @return respuesta de negocio
     */
    @PostMapping("/client")
    public ResponseEntity<?> rateClient(@RequestParam Long driverId,
                                        @Valid @RequestBody RatingRequestDTO dto) {
        return ratingService.rateClient(driverId, dto);
    }

    /**
     * Obtiene todas las calificaciones recibidas por un conductor, junto con
     * el promedio global.
     *
     * @param driverId identificador del conductor
     * @return promedio y lista de calificaciones
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getDriverRatings(@PathVariable Long driverId) {
        return ratingService.getDriverRatings(driverId);
    }

    /**
     * Obtiene todas las calificaciones recibidas por un cliente, junto con
     * el promedio global.
     *
     * @param clientId identificador del cliente
     * @return promedio y lista de calificaciones
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getClientRatings(@PathVariable Long clientId) {
        return ratingService.getClientRatings(clientId);
    }

    /**
     * Obtiene las calificaciones asociadas a un viaje específico.
     *
     * @param tripId identificador del viaje
     * @return lista de calificaciones
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<?> getRatingsByTrip(@PathVariable Long tripId) {
        return ratingService.getRatingsByTrip(tripId);
    }
}
