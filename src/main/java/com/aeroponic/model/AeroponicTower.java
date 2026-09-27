package com.aeroponic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aeroponic_towers")
public class AeroponicTower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tower_id")
    private Integer towerId;

    @Column(name = "tower_code", nullable = false, unique = true, length = 50)
    private String towerCode;

    @Column(length = 100)
    private String location;

    @Column(name = "crop_type", length = 100)
    private String cropType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TowerStatus status = TowerStatus.ACTIVE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TowerStatus {
        ACTIVE, IDLE, MAINTENANCE
    }

    public AeroponicTower() {}

    public AeroponicTower(String towerCode, String location, String cropType, TowerStatus status) {
        this.towerCode = towerCode;
        this.location = location;
        this.cropType = cropType;
        this.status = status;
    }

    public Integer getTowerId() { return towerId; }
    public void setTowerId(Integer towerId) { this.towerId = towerId; }

    public String getTowerCode() { return towerCode; }
    public void setTowerCode(String towerCode) { this.towerCode = towerCode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }

    public TowerStatus getStatus() { return status; }
    public void setStatus(TowerStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
