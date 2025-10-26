package com.utez.edu.mx.viajesbackend.modules.driver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.utez.edu.mx.viajesbackend.modules.user.User;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "driver_profile",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id"}),
                @UniqueConstraint(columnNames = {"license_number"})
        })
public class DriverProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"password"})
    private User user;

    @Column(name="license_number", nullable=false, length=30)
    private String licenseNumber;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehicle> vehicles = new ArrayList<>();

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DriverDocument> documents = new ArrayList<>();

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public List<Vehicle> getVehicles() { return vehicles; }
    public void setVehicles(List<Vehicle> vehicles) { this.vehicles = vehicles; }

    public List<DriverDocument> getDocuments() { return documents; }
    public void setDocuments(List<DriverDocument> documents) { this.documents = documents; }
}