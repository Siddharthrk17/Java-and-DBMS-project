package com.aeroponic.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tower_telemetry")
public class TowerTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "telemetry_id")
    private Integer telemetryId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tower_id", nullable = false)
    private AeroponicTower tower;

    @Column(name = "root_humidity_pct", precision = 5, scale = 2)
    private BigDecimal rootHumidityPct;

    @Column(name = "nutrient_ppm", precision = 8, scale = 2)
    private BigDecimal nutrientPpm;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    public TowerTelemetry() {}

    public TowerTelemetry(AeroponicTower tower, BigDecimal rootHumidityPct, BigDecimal nutrientPpm, LocalDateTime recordedAt) {
        this.tower = tower;
        this.rootHumidityPct = rootHumidityPct;
        this.nutrientPpm = nutrientPpm;
        this.recordedAt = recordedAt != null ? recordedAt : LocalDateTime.now();
    }

    public Integer getTelemetryId() { return telemetryId; }
    public void setTelemetryId(Integer telemetryId) { this.telemetryId = telemetryId; }

    public AeroponicTower getTower() { return tower; }
    public void setTower(AeroponicTower tower) { this.tower = tower; }

    public BigDecimal getRootHumidityPct() { return rootHumidityPct; }
    public void setRootHumidityPct(BigDecimal rootHumidityPct) { this.rootHumidityPct = rootHumidityPct; }

    public BigDecimal getNutrientPpm() { return nutrientPpm; }
    public void setNutrientPpm(BigDecimal nutrientPpm) { this.nutrientPpm = nutrientPpm; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
