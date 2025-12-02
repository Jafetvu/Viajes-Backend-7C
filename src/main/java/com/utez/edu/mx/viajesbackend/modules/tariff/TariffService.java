package com.utez.edu.mx.viajesbackend.modules.tariff;

import com.utez.edu.mx.viajesbackend.modules.tariff.dto.TariffDTO;
import com.utez.edu.mx.viajesbackend.modules.tariff.dto.UpdateTariffDTO;
import com.utez.edu.mx.viajesbackend.utils.CustomResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio para la gestión de tarifas del sistema.
 *
 * <p>Proporciona la lógica de negocio para obtener y actualizar la tarifa
 * activa del sistema, manteniendo un historial completo de cambios.</p>
 */
@Service
public class TariffService {

    private final TariffRepository tariffRepository;
    private final CustomResponseEntity customResponseEntity;

    public TariffService(TariffRepository tariffRepository,
                         CustomResponseEntity customResponseEntity) {
        this.tariffRepository = tariffRepository;
        this.customResponseEntity = customResponseEntity;
    }

    /**
     * Obtiene la tarifa activa actual del sistema.
     *
     * @return ResponseEntity con la tarifa activa o mensaje de error
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCurrentTariff() {
        try {
            Optional<Tariff> tariffOpt = tariffRepository.findActiveTariff();

            if (tariffOpt.isEmpty()) {
                return customResponseEntity.get404Response();
            }

            TariffDTO tariffDTO = transformToDTO(tariffOpt.get());
            return customResponseEntity.getOkResponse(
                "Tarifa actual obtenida correctamente",
                "ok",
                200,
                tariffDTO
            );
        } catch (Exception e) {
            e.printStackTrace();
            return customResponseEntity.get400Response("Error al obtener la tarifa actual");
        }
    }

    /**
     * Actualiza la tarifa del sistema.
     *
     * <p>Desactiva la tarifa actual y crea una nueva tarifa activa con los datos
     * proporcionados. Todo el proceso se ejecuta dentro de una transacción para
     * garantizar la consistencia de datos.</p>
     *
     * @param dto DTO con los datos de la nueva tarifa
     * @return ResponseEntity con la nueva tarifa creada o mensaje de error
     */
    @Transactional(rollbackFor = {SQLException.class, Exception.class})
    public ResponseEntity<?> updateTariff(UpdateTariffDTO dto) {
        try {
            // 1. Desactivar todas las tarifas actuales
            tariffRepository.deactivateAllTariffs();

            // 2. Crear nueva tarifa activa
            Tariff newTariff = new Tariff();
            newTariff.setTariffValue(dto.getTariffValue());
            newTariff.setModificationDate(LocalDateTime.now());
            newTariff.setModifierName(dto.getModifierName());
            newTariff.setChangeReason(dto.getChangeReason());
            newTariff.setIsActive(true);

            // 3. Guardar en base de datos
            Tariff savedTariff = tariffRepository.save(newTariff);

            // 4. Convertir a DTO y devolver respuesta
            TariffDTO result = transformToDTO(savedTariff);
            return customResponseEntity.getOkResponse(
                "Tarifa actualizada correctamente",
                "ok",
                200,
                result
            );
        } catch (Exception e) {
            e.printStackTrace();
            return customResponseEntity.get400Response("Error al actualizar la tarifa: " + e.getMessage());
        }
    }

    /**
     * Transforma una entidad Tariff a un TariffDTO.
     *
     * @param tariff entidad a transformar
     * @return DTO con los datos de la tarifa
     */
    private TariffDTO transformToDTO(Tariff tariff) {
        return new TariffDTO(
            tariff.getId(),
            tariff.getTariffValue(),
            tariff.getModificationDate(),
            tariff.getModifierName(),
            tariff.getChangeReason()
        );
    }
}
