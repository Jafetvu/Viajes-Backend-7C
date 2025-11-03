package com.utez.edu.mx.viajesbackend.modules.trip;

/**
 * Enumeración con los distintos estados que puede tener un viaje.
 *
 * <p>Los viajes son solicitados por los clientes y atraviesan
 * diferentes fases mientras el conductor acepta y realiza el servicio.
 * Los valores aquí definidos se utilizan tanto en la base de datos
 * (almacenados como cadenas) como en la lógica de negocio para
 * validar transiciones válidas.</p>
 */
public enum TripStatus {
    /**
     * El viaje ha sido solicitado por el cliente y aún no hay un conductor asignado.
     * Este estado refleja que la solicitud está disponible para que los conductores
     * la revisen y decidan si la aceptan o la rechazan. Mientras se encuentre en
     * este estado, el campo {@link com.utez.edu.mx.viajesbackend.modules.trip.Trip#driver}
     * será nulo.
     */
    SOLICITADO,
    /**
     * El viaje ha sido solicitado por el cliente y un conductor ha sido asignado
     * automáticamente por el sistema. El conductor aún no confirma.
     */
    ASIGNADO,
    /**
     * El conductor ha aceptado la solicitud de viaje y se dirige al punto de origen.
     */
    ACEPTADO,
    /**
     * El conductor está en camino para recoger al cliente. Solo el conductor puede
     * establecer este estado cuando ya se dirige al punto de origen.
     */
    EN_CAMINO,
    /**
     * El viaje se encuentra en curso, es decir, el cliente ya abordó el vehículo
     * y el conductor va hacia el destino.
     */
    EN_CURSO,
    /**
     * El viaje ha finalizado de forma satisfactoria. Este estado permite al cliente
     * calificar el servicio y contabiliza los ingresos del conductor.
     */
    COMPLETADO,
    /**
     * El viaje fue cancelado, ya sea por el cliente antes de comenzar o por un rechazo
     * del conductor. Al cancelarse se libera al conductor y no genera ingresos.
     */
    CANCELADO
}
