-- =====================================================
-- MIGRACIÓN: Sistema de Tarifa Única Global
-- Fecha: 2025-12-02
-- Descripción: Crea la tabla tariff para almacenar
--              tarifas del sistema con historial
-- =====================================================

-- Crear tabla tariff
CREATE TABLE tariff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tariff_value DOUBLE NOT NULL COMMENT 'Valor de la tarifa en MXN',
    modification_date DATETIME NOT NULL COMMENT 'Fecha y hora de modificación',
    modifier_name VARCHAR(255) NOT NULL COMMENT 'Nombre completo del modificador',
    change_reason VARCHAR(500) NOT NULL COMMENT 'Razón o justificación del cambio',
    is_active BOOLEAN DEFAULT TRUE NOT NULL COMMENT 'Indica si es la tarifa activa',
    created_at DATETIME NOT NULL COMMENT 'Fecha de creación del registro',
    INDEX idx_active (is_active, modification_date DESC) COMMENT 'Índice para búsquedas de tarifa activa'
) COMMENT='Tabla de tarifas del sistema con historial de cambios';

-- Insertar tarifa inicial
INSERT INTO tariff (tariff_value, modification_date, modifier_name, change_reason, is_active, created_at)
VALUES (15.00, NOW(), 'Sistema', 'Tarifa inicial del sistema', true, NOW());

-- Verificar que se creó correctamente
SELECT * FROM tariff;

-- =====================================================
-- FIN DE LA MIGRACIÓN
-- =====================================================
