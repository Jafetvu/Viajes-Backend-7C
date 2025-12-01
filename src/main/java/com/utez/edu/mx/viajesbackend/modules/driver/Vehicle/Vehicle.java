package com.utez.edu.mx.viajesbackend.modules.driver.Vehicle;

import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfile;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false, unique = true)
    private String plate;

    @Column(nullable = false)
    private String color;

    @Column(name = "year_model")
    private Integer year;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DriverProfile getDriver() { return driver; }
    public void setDriver(DriverProfile driver) { this.driver = driver; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
