package com.utez.edu.mx.viajesbackend.modules.trip;

import com.utez.edu.mx.viajesbackend.modules.trip.DTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone los endpoints relacionados con los viajes. Atiende
 * solicitudes de clientes y conductores y delega la lógica en el
 * {@link TripService}.
 */
@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    /** Solicita un nuevo viaje. */
    @PostMapping
    public ResponseEntity<?> requestTrip(@Valid @RequestBody TripRequestDTO dto) {
        return tripService.requestTrip(dto);
    }

    /** Obtiene los detalles de un viaje. */
    @GetMapping("/{tripId}/details")
    public ResponseEntity<?> getTripDetails(@PathVariable Long tripId,
                                            @RequestParam Long clientId) {
        return tripService.getTripDetails(tripId, clientId);
    }

    /** Cancela un viaje no completado. */
    @PutMapping("/{tripId}/cancel")
    public ResponseEntity<?> cancelTrip(@PathVariable Long tripId,
                                        @RequestParam Long clientId,
                                        @RequestParam(required = false) String reason) {
        return tripService.cancelTrip(tripId, clientId, reason);
    }

    /** Califica un viaje completado. */
    @PostMapping("/rate")
    public ResponseEntity<?> rateTrip(@RequestParam Long clientId,
                                      @Valid @RequestBody TripRateDTO dto) {
        return tripService.rateTrip(clientId, dto);
    }

    /** Historial de viajes de un cliente. */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getClientTripHistory(@PathVariable Long clientId) {
        return tripService.getClientTripHistory(clientId);
    }

    /** Solicitudes de viaje asignadas a un conductor. */
    @GetMapping("/driver/{driverId}/assigned")
    public ResponseEntity<?> getAssignedTrips(@PathVariable Long driverId) {
        return tripService.getAssignedTrips(driverId);
    }

    /** Solicitudes de viaje disponibles (sin conductor) para que los conductores las vean. */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableTrips() {
        return tripService.getAvailableTrips();
    }

    /** Permite al conductor aceptar un viaje solicitado. */
    @PutMapping("/{tripId}/accept")
    public ResponseEntity<?> acceptTrip(@PathVariable Long tripId,
                                        @RequestParam Long driverId) {
        return tripService.acceptTrip(tripId, driverId);
    }

    /** Permite al conductor rechazar un viaje solicitado o aceptado. */
    @PutMapping("/{tripId}/reject")
    public ResponseEntity<?> rejectTrip(@PathVariable Long tripId,
                                        @RequestParam Long driverId) {
        return tripService.rejectTrip(tripId, driverId);
    }

    /** Actualiza el estado del viaje (flujo ACEPTADO → EN_CAMINO → EN_CURSO). */
    @PutMapping("/status")
    public ResponseEntity<?> updateTripStatus(@RequestParam Long driverId,
                                              @Valid @RequestBody TripStatusUpdateDTO dto) {
        return tripService.updateTripStatus(driverId, dto);
    }

    /** Historial de viajes e ingresos de un conductor. */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getDriverTripHistory(@PathVariable Long driverId) {
        return tripService.getDriverTripHistory(driverId);
    }

    /** Marca el viaje como completado por parte del conductor. */
    @PutMapping("/{tripId}/complete/driver")
    public ResponseEntity<?> completeTripByDriver(@PathVariable Long tripId,
                                                  @RequestParam Long driverId) {
        return tripService.completeTripByDriver(driverId, tripId);
    }

    /** Marca el viaje como completado por parte del cliente. */
    @PutMapping("/{tripId}/complete/client")
    public ResponseEntity<?> completeTripByClient(@PathVariable Long tripId,
                                                  @RequestParam Long clientId) {
        return tripService.completeTripByClient(clientId, tripId);
    }
}
