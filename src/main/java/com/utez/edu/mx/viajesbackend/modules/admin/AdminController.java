package com.utez.edu.mx.viajesbackend.modules.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controller for admin dashboard endpoints
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Get dashboard statistics
    @GetMapping("/stats/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        return adminService.getDashboardStats();
    }
}

