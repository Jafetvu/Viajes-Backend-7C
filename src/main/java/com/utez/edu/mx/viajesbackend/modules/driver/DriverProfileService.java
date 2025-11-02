package com.utez.edu.mx.viajesbackend.modules.driver;

import com.utez.edu.mx.viajesbackend.modules.driver.Documents.DriverDocument;
import com.utez.edu.mx.viajesbackend.modules.driver.Documents.DriverDocumentRepository;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverAvailability;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfile;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfileRepository;
import com.utez.edu.mx.viajesbackend.modules.driver.Vehicle.Vehicle;
import com.utez.edu.mx.viajesbackend.modules.driver.Vehicle.VehicleRepository;
import com.utez.edu.mx.viajesbackend.modules.user.User;
import com.utez.edu.mx.viajesbackend.modules.user.UserRepository;
import com.utez.edu.mx.viajesbackend.utils.CustomResponseEntity;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class DriverProfileService {

    private static final int ROLE_DRIVER_ID = 3;

    private final DriverProfileRepository driverProfileRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CustomResponseEntity customResponseEntity; // ajusta import/paquete

    public DriverProfileService(DriverProfileRepository driverProfileRepository,
                                VehicleRepository vehicleRepository,
                                DriverDocumentRepository documentRepository,
                                UserRepository userRepository,
                                CustomResponseEntity customResponseEntity) {
        this.driverProfileRepository = driverProfileRepository;
        this.vehicleRepository = vehicleRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.customResponseEntity = customResponseEntity;
    }

    /** Paso 2: crear perfil chofer para un usuario existente (User.status se maneja en UserService). */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> createDriverProfile(Long userId, String licenseNumber) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return customResponseEntity.get404Response();

        if (user.getRole() == null || user.getRole().getId() != ROLE_DRIVER_ID) {
            return customResponseEntity.get400Response("El usuario no tiene rol de chofer");
        }
        if (driverProfileRepository.existsByUserId(userId)) {
            return customResponseEntity.get400Response("El chofer ya tiene perfil");
        }
        if (driverProfileRepository.existsByLicenseNumber(licenseNumber)) {
            return customResponseEntity.get400Response("La licencia ya está registrada");
        }

        DriverProfile dp = new DriverProfile();
        dp.setUser(user);
        dp.setLicenseNumber(licenseNumber);

        driverProfileRepository.save(dp);
        return customResponseEntity.getOkResponse("Perfil de chofer creado", "ok", 200, null);
    }

    /** Agregar vehículo al perfil */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> addVehicle(Long driverProfileId, Vehicle payload) {
        DriverProfile dp = driverProfileRepository.findById(driverProfileId).orElse(null);
        if (dp == null) return customResponseEntity.get404Response();

        payload.setId(null);
        payload.setDriver(dp);
        vehicleRepository.save(payload);
        return customResponseEntity.getOkResponse("Vehículo agregado", "ok", 200, null);
    }

    /** Agregar documento al perfil */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> addDocument(Long driverProfileId, DriverDocument payload) {
        DriverProfile dp = driverProfileRepository.findById(driverProfileId).orElse(null);
        if (dp == null) return customResponseEntity.get404Response();

        payload.setId(null);
        payload.setDriver(dp);
        documentRepository.save(payload);
        return customResponseEntity.getOkResponse("Documento agregado", "ok", 200, null);
    }

    /** Aprobar chofer (User.status = true). Úsalo cuando ya validaste docs y vehículo. */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> approveDriver(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return customResponseEntity.get404Response();
        if (user.getRole() == null || user.getRole().getId() != ROLE_DRIVER_ID) {
            return customResponseEntity.get400Response("El usuario no tiene rol de chofer");
        }
        if (!driverProfileRepository.existsByUserId(userId)) {
            return customResponseEntity.get400Response("El chofer no tiene perfil");
        }

        user.setStatus(true);
        userRepository.save(user);
        return customResponseEntity.getOkResponse("Chofer aprobado", "ok", 200, null);
    }

    /** Suspender chofer (User.status = false). */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> suspendDriver(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return customResponseEntity.get404Response();
        if (user.getRole() == null || user.getRole().getId() != ROLE_DRIVER_ID) {
            return customResponseEntity.get400Response("El usuario no tiene rol de chofer");
        }

        user.setStatus(false);
        userRepository.save(user);
        return customResponseEntity.getOkResponse("Chofer suspendido", "ok", 200, null);
    }


    /** Cambiar disponibilidad del chofer a DISPONIBLE */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> setAvailable(Long driverId) {
        DriverProfile dp = driverProfileRepository.findById(driverId).orElse(null);
        if (dp == null) return customResponseEntity.get404Response();

        User user = dp.getUser();
        if (!canOperateAsDriver(user)) {
            return customResponseEntity.get400Response("El chofer no puede operar actualmente");
        }

        dp.setAvailability(DriverAvailability.DISPONIBLE);
        driverProfileRepository.save(dp);
        return customResponseEntity.getOkResponse(
                "El chofer ahora está DISPONIBLE",
                "ok", 200, null
        );
    }

    /** Cambiar disponibilidad del chofer a FUERA_DE_SERVICIO */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> setOffline(Long driverId) {
        DriverProfile dp = driverProfileRepository.findById(driverId).orElse(null);
        if (dp == null) return customResponseEntity.get404Response();

        User user = dp.getUser();
        if (!canOperateAsDriver(user)) {
            return customResponseEntity.get400Response("El chofer no puede operar actualmente");
        }

        dp.setAvailability(DriverAvailability.FUERA_DE_SERVICIO);
        driverProfileRepository.save(dp);
        return customResponseEntity.getOkResponse(
                "El chofer ahora está FUERA DE SERVICIO",
                "ok", 200, null
        );
    }


    /** Regla de negocio rápida: ¿puede operar como chofer? */
    public boolean canOperateAsDriver(User user) {
        return user != null
                && user.getRole() != null
                && user.getRole().getId() == ROLE_DRIVER_ID
                && user.isStatus(); // status true en User
    }
}
