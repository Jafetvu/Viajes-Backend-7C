package com.utez.edu.mx.viajesbackend.modules.driver.Documents;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverDocumentRepository extends JpaRepository<DriverDocument, Long> {
    List<DriverDocument> findByDriverId(Long driverId);
}

