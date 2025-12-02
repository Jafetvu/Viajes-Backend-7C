package com.utez.edu.mx.viajesbackend.modules.admin.dto;

import java.util.List;
import java.util.Map;

// DTO for admin dashboard statistics
public class DashboardStatsDTO {

    private long totalUsers;
    private long totalClients;
    private long totalDrivers;
    private long activeDrivers;
    private long pendingDrivers;
    private long totalTrips;
    private long completedTrips;
    private long activeTrips;
    private long cancelledTrips;
    private double totalIncome;
    private List<Map<String, Object>> chartData;

    public DashboardStatsDTO() {}

    // Getters and setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(long totalClients) {
        this.totalClients = totalClients;
    }

    public long getTotalDrivers() {
        return totalDrivers;
    }

    public void setTotalDrivers(long totalDrivers) {
        this.totalDrivers = totalDrivers;
    }

    public long getActiveDrivers() {
        return activeDrivers;
    }

    public void setActiveDrivers(long activeDrivers) {
        this.activeDrivers = activeDrivers;
    }

    public long getPendingDrivers() {
        return pendingDrivers;
    }

    public void setPendingDrivers(long pendingDrivers) {
        this.pendingDrivers = pendingDrivers;
    }

    public long getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(long totalTrips) {
        this.totalTrips = totalTrips;
    }

    public long getCompletedTrips() {
        return completedTrips;
    }

    public void setCompletedTrips(long completedTrips) {
        this.completedTrips = completedTrips;
    }

    public long getActiveTrips() {
        return activeTrips;
    }

    public void setActiveTrips(long activeTrips) {
        this.activeTrips = activeTrips;
    }

    public long getCancelledTrips() {
        return cancelledTrips;
    }

    public void setCancelledTrips(long cancelledTrips) {
        this.cancelledTrips = cancelledTrips;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public List<Map<String, Object>> getChartData() {
        return chartData;
    }

    public void setChartData(List<Map<String, Object>> chartData) {
        this.chartData = chartData;
    }
}

