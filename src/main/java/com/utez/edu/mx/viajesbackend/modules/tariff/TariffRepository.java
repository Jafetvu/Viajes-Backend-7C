package com.utez.edu.mx.viajesbackend.modules.tariff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Tariff.
 *
 * <p>Proporciona métodos para acceder y manipular tarifas en la base de datos,
 * incluyendo consultas personalizadas para obtener la tarifa activa y
 * desactivar todas las tarifas existentes.</p>
 */
@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    /**
     * Busca la tarifa activa actual.
     *
     * @return Optional conteniendo la tarifa activa, o vacío si no existe ninguna
     */
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true ORDER BY t.modificationDate DESC")
    Optional<Tariff> findActiveTariff();

    /**
     * Desactiva todas las tarifas existentes.
     * Este método debe ejecutarse en un contexto transaccional.
     *
     * @return número de tarifas desactivadas
     */
    @Modifying
    @Query("UPDATE Tariff t SET t.isActive = false WHERE t.isActive = true")
    int deactivateAllTariffs();
}
