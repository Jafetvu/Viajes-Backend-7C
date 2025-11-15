package com.utez.edu.mx.viajesbackend.modules.trip;

/**
 * Enumeration with the different states that a trip can have.
 *
 * <p>Trips are requested by clients and go through different phases
 * while the driver accepts and performs the service. The values defined
 * here are used both in the database (stored as strings) and in the
 * business logic to validate valid transitions.</p>
 */
public enum TripStatus {
    /**
     * The trip has been requested by the client and no driver has been assigned yet.
     * This state reflects that the request is available for drivers to review and
     * decide if they accept or reject it. While in this state, the
     * {@link com.utez.edu.mx.viajesbackend.modules.trip.Trip#driver} field will be null.
     */
    REQUESTED,

    /**
     * The driver has accepted the trip request and is heading to the origin point.
     */
    ACCEPTED,

    /**
     * The trip is in progress, meaning the client has boarded the vehicle
     * and the driver is heading to the destination. Both parties must confirm
     * the start of the trip to transition to this state.
     */
    IN_PROGRESS,

    /**
     * The trip has been completed successfully. This state allows the client
     * to rate the service. Both driver and client must confirm completion.
     */
    COMPLETED,

    /**
     * The trip was cancelled, either by the client before starting or by
     * driver rejection. When cancelled, the driver is released and no revenue is generated.
     */
    CANCELLED
}
