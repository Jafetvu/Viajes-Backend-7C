package com.utez.edu.mx.viajesbackend.modules.tariff;

import com.utez.edu.mx.viajesbackend.modules.tariff.dto.UpdateTariffDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de tarifas del sistema.
 *
 * <p>Expone endpoints para consultar y actualizar la tarifa activa.
 * La actualización requiere permisos de administrador.</p>
 */
@RestController
@RequestMapping("/api/tariff")
@CrossOrigin(origins = {"http://localhost:5173"})
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    /**
     * Obtiene la tarifa activa actual del sistema.
     *
     * <p>Este endpoint es público y no requiere autenticación, ya que
     * los clientes y conductores necesitan conocer la tarifa para
     * calcular el costo de los viajes.</p>
     *
     * @return ResponseEntity con la tarifa activa o mensaje de error
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentTariff() {
        return tariffService.getCurrentTariff();
    }

    /**
     * Actualiza la tarifa del sistema.
     *
     * <p>Solo usuarios con rol ADMIN pueden ejecutar esta operación.
     * Se requiere proporcionar el nuevo valor, el nombre del modificador
     * y la razón del cambio.</p>
     *
     * @param dto DTO con los datos de la nueva tarifa
     * @return ResponseEntity con la tarifa actualizada o mensaje de error
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTariff(@Valid @RequestBody UpdateTariffDTO dto) {
        return tariffService.updateTariff(dto);
    }
}
