package com.utez.edu.mx.viajesbackend.modules.driver.Documents;

import com.utez.edu.mx.viajesbackend.modules.driver.Profile.DriverProfile;
import jakarta.persistence.*;

@Entity
@Table(name = "driver_document")
public class DriverDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)
    private DriverDocType type;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "original_name")
    private String originalName;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DriverProfile getDriver() { return driver; }
    public void setDriver(DriverProfile driver) { this.driver = driver; }

    public DriverDocType getType() { return type; }
    public void setType(DriverDocType type) { this.type = type; }

    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
}
