package com.utez.edu.mx.viajesbackend.modules.driver;

import com.utez.edu.mx.viajesbackend.modules.driver.Documents.DriverDocument;
import com.utez.edu.mx.viajesbackend.modules.driver.Documents.DriverDocumentRepository;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfile;
import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfileRepository;
import com.utez.edu.mx.viajesbackend.modules.driver.Vehicle.Vehicle;
import com.utez.edu.mx.viajesbackend.modules.driver.Vehicle.VehicleRepository;
import com.utez.edu.mx.viajesbackend.modules.user.User;
import com.utez.edu.mx.viajesbackend.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private static final int ROLE_DRIVER_ID = 3;

    private final DriverProfileRepository driverProfileRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DriverProfileService driverProfileService; // para usar tus servicios existentes

    public DriverController(DriverProfileRepository driverProfileRepository,
                            VehicleRepository vehicleRepository,
                            DriverDocumentRepository documentRepository,
                            UserRepository userRepository,
                            DriverProfileService driverProfileService) {
        this.driverProfileRepository = driverProfileRepository;
        this.vehicleRepository = vehicleRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.driverProfileService = driverProfileService;
    }

    /* ============================
       PASO 2: CREAR PERFIL CHOFER
       ============================ */
    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(
            @RequestParam Long userId,
            @RequestParam String licenseNumber
    ) {
        return driverProfileService.createDriverProfile(userId, licenseNumber);
    }

    /* ============================
       AGREGAR VEHÍCULO AL PERFIL
       ============================ */
    @PostMapping("/profile/{driverProfileId}/vehicles")
    public ResponseEntity<?> addVehicle(
            @PathVariable Long driverProfileId,
            @RequestBody Vehicle vehicle
    ) {
        return driverProfileService.addVehicle(driverProfileId, vehicle);
    }

    /* ============================
       AGREGAR DOCUMENTO AL PERFIL
       ============================ */
    @PostMapping("/profile/{driverProfileId}/documents")
    public ResponseEntity<?> addDocument(
            @PathVariable Long driverProfileId,
            @RequestBody DriverDocument document
    ) {
        return driverProfileService.addDocument(driverProfileId, document);
    }

    /* ===========================================
       VISTA ADMIN: LISTA DE CHOFERES PENDIENTES
       - rol.id = 3 y user.status = false
       =========================================== */
    @GetMapping("/admin/pending")
    @Transactional(Transactional.TxType.SUPPORTS)
    public ResponseEntity<?> listPendingDrivers() {
        List<User> pending = userRepository.findPendingDrivers();
        // devolvemos info básica del user y, si existe, su perfil (license)
        List<Map<String, Object>> out = new ArrayList<>();
        for (User u : pending) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userId", u.getId());
            row.put("name", u.getName());
            row.put("surname", u.getSurname());
            row.put("lastname", u.getLastname());
            row.put("email", u.getEmail());
            row.put("phoneNumber", u.getPhoneNumber());
            row.put("status", u.isStatus());
            // intenta encontrar perfil para mostrar licencia si ya existe
            driverProfileRepository.findByUserId(u.getId()).ifPresent(dp -> {
                row.put("driverProfileId", dp.getId());
                row.put("licenseNumber", dp.getLicenseNumber());
            });
            out.add(row);
        }
        return ResponseEntity.ok(out);
    }

    /* ====================================================
       VISTA ADMIN: DETALLE COMPLETO DEL CHOFER POR userId
       Incluye: User + DriverProfile + Vehicles + Documents
       ==================================================== */
    @GetMapping("/admin/{userId}/full")
    @Transactional(Transactional.TxType.REQUIRED) // mantenemos sesión abierta
    public ResponseEntity<?> getFullDriverInfo(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("Usuario no encontrado");
        if (user.getRole() == null || user.getRole().getId() != ROLE_DRIVER_ID) {
            return ResponseEntity.badRequest().body("El usuario no tiene rol de chofer");
        }

        Optional<DriverProfile> maybe = driverProfileRepository.findByUserId(userId);
        if (maybe.isEmpty()) {
            // puede estar recién registrado como driver sin perfil aún
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("user", toUserMap(user));
            resp.put("driverProfile", null);
            resp.put("vehicles", Collections.emptyList());
            resp.put("documents", Collections.emptyList());
            return ResponseEntity.ok(resp);
        }

        DriverProfile dp = maybe.get();
        // Inicializar colecciones perezosas dentro de la transacción
        dp.getVehicles().size();
        dp.getDocuments().size();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("user", toUserMap(user));
        resp.put("driverProfile", toDriverProfileMap(dp));
        resp.put("vehicles", toVehiclesList(dp.getVehicles()));
        resp.put("documents", toDocumentsList(dp.getDocuments()));
        return ResponseEntity.ok(resp);
    }

    /* ============================
       ADMIN: APROBAR / SUSPENDER
       ============================ */
    @PutMapping("/admin/{userId}/approve")
    public ResponseEntity<?> approve(@PathVariable Long userId) {
        return driverProfileService.approveDriver(userId);
    }

    @PutMapping("/admin/{userId}/suspend")
    public ResponseEntity<?> suspend(@PathVariable Long userId) {
        return driverProfileService.suspendDriver(userId);
    }

    /* ============================
       Helpers de armado de salida
       ============================ */
    private Map<String, Object> toUserMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("surname", u.getSurname());
        m.put("lastname", u.getLastname());
        m.put("email", u.getEmail());
        m.put("phoneNumber", u.getPhoneNumber());
        m.put("status", u.isStatus());
        m.put("roleId", u.getRole() != null ? u.getRole().getId() : null);
        m.put("roleName", u.getRole() != null ? u.getRole().getName() : null);
        return m;
    }

    private Map<String, Object> toDriverProfileMap(DriverProfile dp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("driverProfileId", dp.getId());
        m.put("licenseNumber", dp.getLicenseNumber());
        m.put("userId", dp.getUser() != null ? dp.getUser().getId() : null);
        return m;
    }

    private List<Map<String, Object>> toVehiclesList(List<Vehicle> list) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Vehicle v : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", v.getId());
            m.put("brand", v.getBrand());
            m.put("model", v.getModel());
            m.put("plate", v.getPlate());
            m.put("color", v.getColor());
            m.put("year", v.getYear());
            m.put("active", v.isActive());
            out.add(m);
        }
        return out;
    }

    private List<Map<String, Object>> toDocumentsList(List<DriverDocument> list) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (DriverDocument d : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("type", d.getType());
            m.put("storageKey", d.getStorageKey());
            m.put("mimeType", d.getMimeType());
            m.put("originalName", d.getOriginalName());
            out.add(m);
        }
        return out;
    }

    /** Cambiar chofer a disponible */
    @PutMapping("/{id}/available")
    public ResponseEntity<?> setDriverAvailable(@PathVariable Long id) {
        return driverProfileService.setAvailable(id);
    }

    /** Cambiar chofer a fuera de servicio */
    @PutMapping("/{id}/offline")
    public ResponseEntity<?> setDriverOffline(@PathVariable Long id) {
        return driverProfileService.setOffline(id);
    }
}

