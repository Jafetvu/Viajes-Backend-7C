package com.utez.edu.mx.viajesbackend.modules.admin;

import com.utez.edu.mx.viajesbackend.modules.admin.dto.DashboardStatsDTO;
import com.utez.edu.mx.viajesbackend.modules.trip.TripRepository;
import com.utez.edu.mx.viajesbackend.modules.trip.TripStatus;
import com.utez.edu.mx.viajesbackend.modules.user.UserRepository;
import com.utez.edu.mx.viajesbackend.utils.CustomResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Service for admin dashboard operations
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final CustomResponseEntity customResponseEntity;

    // Role IDs from DataInitializer
    private static final int ROLE_CLIENT = 2;
    private static final int ROLE_DRIVER = 3;

    public AdminService(UserRepository userRepository,
                        TripRepository tripRepository,
                        CustomResponseEntity customResponseEntity) {
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.customResponseEntity = customResponseEntity;
    }

    // Get dashboard statistics with real data
    @Transactional(readOnly = true)
    public ResponseEntity<?> getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // User statistics
        long totalUsers = userRepository.count();
        long totalClients = userRepository.countByRoleId(ROLE_CLIENT);
        long totalDrivers = userRepository.countByRoleId(ROLE_DRIVER);
        long activeDrivers = userRepository.countByRoleIdAndStatus(ROLE_DRIVER, true);
        long pendingDrivers = userRepository.countByRoleIdAndStatus(ROLE_DRIVER, false);

        stats.setTotalUsers(totalUsers);
        stats.setTotalClients(totalClients);
        stats.setTotalDrivers(totalDrivers);
        stats.setActiveDrivers(activeDrivers);
        stats.setPendingDrivers(pendingDrivers);

        // Trip statistics
        long totalTrips = tripRepository.count();
        long completedTrips = tripRepository.countByStatus(TripStatus.COMPLETED);
        long activeTrips = tripRepository.countByStatusIn(
            List.of(TripStatus.REQUESTED, TripStatus.ACCEPTED, TripStatus.IN_PROGRESS)
        );
        long cancelledTrips = tripRepository.countByStatus(TripStatus.CANCELLED);

        stats.setTotalTrips(totalTrips);
        stats.setCompletedTrips(completedTrips);
        stats.setActiveTrips(activeTrips);
        stats.setCancelledTrips(cancelledTrips);

        // Total income from completed trips
        Double totalIncome = tripRepository.sumFareByStatus(TripStatus.COMPLETED);
        stats.setTotalIncome(totalIncome != null ? totalIncome : 0.0);

        // Chart data for pie chart
        List<Map<String, Object>> chartData = new ArrayList<>();
        
        Map<String, Object> clientsData = new LinkedHashMap<>();
        clientsData.put("id", "Clientes");
        clientsData.put("label", "Clientes");
        clientsData.put("value", totalClients);
        chartData.add(clientsData);

        Map<String, Object> driversData = new LinkedHashMap<>();
        driversData.put("id", "Conductores");
        driversData.put("label", "Conductores");
        driversData.put("value", totalDrivers);
        chartData.add(driversData);

        Map<String, Object> completedData = new LinkedHashMap<>();
        completedData.put("id", "Viajes Completados");
        completedData.put("label", "Viajes Completados");
        completedData.put("value", completedTrips);
        chartData.add(completedData);

        Map<String, Object> activeData = new LinkedHashMap<>();
        activeData.put("id", "Viajes Activos");
        activeData.put("label", "Viajes Activos");
        activeData.put("value", activeTrips);
        chartData.add(activeData);

        stats.setChartData(chartData);

        return customResponseEntity.getOkResponse("Dashboard stats retrieved", "ok", 200, stats);
    }
}

