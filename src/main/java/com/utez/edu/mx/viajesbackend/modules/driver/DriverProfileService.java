package com.utez.edu.mx.viajesbackend.modules.driver;

import com.utez.edu.mx.viajesbackend.modules.driver.Documents.DriverDocument;
import com.utez.edu.mx.viajesbackend.modules.driver.Documents.DriverDocumentRepository;
import com.utez.edu.mx.viajesbackend.modules.driver.Documents.DriverDocType;
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
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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

        // Return driver profile ID for subsequent calls
        Map<String, Object> data = new HashMap<>();
        data.put("driverProfileId", dp.getId());
        return customResponseEntity.getOkResponse("Perfil de chofer creado", "ok", 200, data);
    }

    /** Agregar vehículo al perfil */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> addVehicle(Long driverProfileId, Vehicle payload) {
        DriverProfile dp = driverProfileRepository.findById(driverProfileId).orElse(null);
        if (dp == null) return customResponseEntity.get404Response();

        // Validate required fields
        if (payload.getBrand() == null || payload.getBrand().trim().isEmpty()) {
            return customResponseEntity.get400Response("La marca del vehículo es obligatoria");
        }
        if (payload.getModel() == null || payload.getModel().trim().isEmpty()) {
            return customResponseEntity.get400Response("El modelo del vehículo es obligatorio");
        }
        if (payload.getPlate() == null || payload.getPlate().trim().isEmpty()) {
            return customResponseEntity.get400Response("La placa del vehículo es obligatoria");
        }
        if (payload.getColor() == null || payload.getColor().trim().isEmpty()) {
            return customResponseEntity.get400Response("El color del vehículo es obligatorio");
        }
        if (payload.getYear() == null || payload.getYear() < 1900 || payload.getYear() > 2030) {
            return customResponseEntity.get400Response("El año del vehículo es inválido");
        }

        // Check plate uniqueness
        if (vehicleRepository.existsByPlate(payload.getPlate())) {
            return customResponseEntity.get400Response("La placa ya está registrada");
        }

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

    /** Agregar documento con archivo (upload) */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> uploadDocument(Long driverProfileId, MultipartFile file, String documentType) {
        // Validate driver profile exists
        DriverProfile dp = driverProfileRepository.findById(driverProfileId).orElse(null);
        if (dp == null) return customResponseEntity.get404Response();

        // Validate file not empty
        if (file.isEmpty()) {
            return customResponseEntity.get400Response("El archivo está vacío");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return customResponseEntity.get400Response("El archivo excede 5MB");
        }

        // Validate MIME type (PDF only)
        if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
            return customResponseEntity.get400Response("Solo se permiten archivos PDF");
        }

        // Parse document type
        DriverDocType docType;
        try {
            docType = DriverDocType.valueOf(documentType);
        } catch (IllegalArgumentException e) {
            return customResponseEntity.get400Response("Tipo de documento inválido");
        }

        try {
            DriverDocument document = new DriverDocument();
            document.setDriver(dp);
            document.setType(docType);
            document.setMimeType(file.getContentType());
            document.setOriginalName(file.getOriginalFilename());
            document.setStorageKey("BLOB_" + System.currentTimeMillis());
            document.setFileData(file.getBytes()); // Store as BLOB

            documentRepository.save(document);
            return customResponseEntity.getOkResponse("Documento subido exitosamente", "ok", 200, null);
        } catch (Exception e) {
            return customResponseEntity.get400Response("Error al procesar el archivo: " + e.getMessage());
        }
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

    /** Actualizar número de licencia */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> updateLicense(Long driverProfileId, String licenseNumber) {
        DriverProfile dp = driverProfileRepository.findById(driverProfileId).orElse(null);
        if (dp == null) return customResponseEntity.get404Response();

        // Check if license is unique (excluding current profile)
        if (driverProfileRepository.existsByLicenseNumber(licenseNumber) && !dp.getLicenseNumber().equals(licenseNumber)) {
            return customResponseEntity.get400Response("La licencia ya está registrada en otro perfil");
        }

        dp.setLicenseNumber(licenseNumber);
        driverProfileRepository.save(dp);
        return customResponseEntity.getOkResponse("Licencia actualizada", "ok", 200, null);
    }

    /** Actualizar vehículo */
    @Transactional(rollbackOn = {SQLException.class, Exception.class})
    public ResponseEntity<?> updateVehicle(Long vehicleId, Vehicle payload) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null) return customResponseEntity.get404Response();

        // Validate required fields
        if (payload.getBrand() == null || payload.getBrand().trim().isEmpty()) {
            return customResponseEntity.get400Response("La marca del vehículo es obligatoria");
        }
        if (payload.getModel() == null || payload.getModel().trim().isEmpty()) {
            return customResponseEntity.get400Response("El modelo del vehículo es obligatorio");
        }
        if (payload.getPlate() == null || payload.getPlate().trim().isEmpty()) {
            return customResponseEntity.get400Response("La placa del vehículo es obligatoria");
        }
        if (payload.getColor() == null || payload.getColor().trim().isEmpty()) {
            return customResponseEntity.get400Response("El color del vehículo es obligatorio");
        }
        if (payload.getYear() == null || payload.getYear() < 1900 || payload.getYear() > 2030) {
            return customResponseEntity.get400Response("El año del vehículo es inválido");
        }

        // Check plate uniqueness (excluding current vehicle)
        if (vehicleRepository.existsByPlate(payload.getPlate()) && !vehicle.getPlate().equals(payload.getPlate())) {
            return customResponseEntity.get400Response("La placa ya está registrada");
        }

        vehicle.setBrand(payload.getBrand());
        vehicle.setModel(payload.getModel());
        vehicle.setPlate(payload.getPlate());
        vehicle.setColor(payload.getColor());
        vehicle.setYear(payload.getYear());
        
        vehicleRepository.save(vehicle);
        return customResponseEntity.getOkResponse("Vehículo actualizado", "ok", 200, null);
    }

    public DriverDocument getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }
}
