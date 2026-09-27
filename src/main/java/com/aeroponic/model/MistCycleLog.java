package com.aeroponic.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mist_cycle_log")
public class MistCycleLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cycle_id")
    private Integer cycleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tower_id", nullable = false)
    private AeroponicTower tower;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds = 30;

    @Column(name = "nutrient_ml", precision = 8, scale = 2)
    private BigDecimal nutrientMl;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", length = 20)
    private TriggerSource triggeredBy = TriggerSource.AUTO;

    public enum TriggerSource {
        AUTO, MANUAL
    }

    public MistCycleLog() {}

    public MistCycleLog(AeroponicTower tower, Integer durationSeconds, BigDecimal nutrientMl, TriggerSource triggeredBy) {
        this.tower = tower;
        this.durationSeconds = durationSeconds;
        this.nutrientMl = nutrientMl;
        this.triggeredBy = triggeredBy;
        this.triggeredAt = LocalDateTime.now();
    }

    public Integer getCycleId() { return cycleId; }
    public void setCycleId(Integer cycleId) { this.cycleId = cycleId; }

    public AeroponicTower getTower() { return tower; }
    public void setTower(AeroponicTower tower) { this.tower = tower; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public BigDecimal getNutrientMl() { return nutrientMl; }
    public void setNutrientMl(BigDecimal nutrientMl) { this.nutrientMl = nutrientMl; }

    public TriggerSource getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(TriggerSource triggeredBy) { this.triggeredBy = triggeredBy; }
}
