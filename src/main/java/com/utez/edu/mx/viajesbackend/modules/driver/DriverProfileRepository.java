package com.utez.edu.mx.viajesbackend.modules.driver;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {
    Optional<DriverProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    boolean existsByLicenseNumber(String licenseNumber);

    DriverProfile findById(long id);
}

