package com.aeroponic.service;

import com.aeroponic.model.*;
import com.aeroponic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AeroponicService {

    private final AeroponicTowerRepository towerRepository;
    private final TowerTelemetryRepository telemetryRepository;
    private final MistCycleLogRepository mistCycleLogRepository;
    private final CropYieldRepository cropYieldRepository;
    private final ProductRepository productRepository;

    public AeroponicService(AeroponicTowerRepository towerRepository,
                            TowerTelemetryRepository telemetryRepository,
                            MistCycleLogRepository mistCycleLogRepository,
                            CropYieldRepository cropYieldRepository,
                            ProductRepository productRepository) {
        this.towerRepository = towerRepository;
        this.telemetryRepository = telemetryRepository;
        this.mistCycleLogRepository = mistCycleLogRepository;
        this.cropYieldRepository = cropYieldRepository;
        this.productRepository = productRepository;
    }

    public List<AeroponicTower> getAllTowers() {
        return towerRepository.findAll();
    }

    public Optional<AeroponicTower> getTowerById(Integer id) {
        return towerRepository.findById(id);
    }

    @Transactional
    public AeroponicTower addTower(AeroponicTower tower) {
        return towerRepository.save(tower);
    }

    public List<TowerTelemetry> getLatestTelemetry() {
        return telemetryRepository.findLatestTelemetryForAllTowers();
    }

    public List<TowerTelemetry> getTowerTelemetryHistory(Integer towerId) {
        AeroponicTower tower = towerRepository.findById(towerId)
                .orElseThrow(() -> new IllegalArgumentException("Tower not found: " + towerId));
        return telemetryRepository.findByTowerOrderByRecordedAtDesc(tower);
    }

    public List<MistCycleLog> getRecentMistCycles() {
        return mistCycleLogRepository.findTop20ByOrderByTriggeredAtDesc();
    }

    /**
     * Key Use-Case Step 2:
     * Ingest root humidity & nutrient PPM telemetry via POST /api/aeroponics/telemetry;
     * Auto-triggers misting cycle if humidity < 70% or PPM < 800.
     */
    @Transactional
    public Map<String, Object> ingestTelemetry(Integer towerId, BigDecimal rootHumidityPct, BigDecimal nutrientPpm) {
        AeroponicTower tower = towerRepository.findById(towerId)
                .orElseThrow(() -> new IllegalArgumentException("Tower not found: " + towerId));

        TowerTelemetry telemetry = new TowerTelemetry(tower, rootHumidityPct, nutrientPpm, LocalDateTime.now());
        telemetry = telemetryRepository.save(telemetry);

        Map<String, Object> response = new HashMap<>();
        response.put("telemetryId", telemetry.getTelemetryId());
        response.put("towerId", tower.getTowerId());
        response.put("towerCode", tower.getTowerCode());
        response.put("humidityPct", rootHumidityPct);
        response.put("nutrientPpm", nutrientPpm);
        response.put("recordedAt", telemetry.getRecordedAt());

        boolean mistTriggered = false;
        String reason = "Normal root-zone environmental state";

        // Misting rule: Auto-trigger mist nozzles if humidity < 70.0% OR PPM < 800.0
        BigDecimal humidityThreshold = new BigDecimal("70.0");
        BigDecimal ppmThreshold = new BigDecimal("800.0");

        if (rootHumidityPct != null && rootHumidityPct.compareTo(humidityThreshold) < 0) {
            mistTriggered = true;
            reason = "Root humidity (" + rootHumidityPct + "%) below threshold (" + humidityThreshold + "%)";
        } else if (nutrientPpm != null && nutrientPpm.compareTo(ppmThreshold) < 0) {
            mistTriggered = true;
            reason = "Nutrient concentration (" + nutrientPpm + " PPM) below threshold (" + ppmThreshold + " PPM)";
        }

        if (mistTriggered) {
            MistCycleLog cycle = new MistCycleLog(tower, 30, new BigDecimal("15.00"), MistCycleLog.TriggerSource.AUTO);
            cycle = mistCycleLogRepository.save(cycle);
            response.put("mistCycleTriggered", true);
            response.put("mistCycleId", cycle.getCycleId());
            response.put("mistDurationSeconds", cycle.getDurationSeconds());
            response.put("nutrientMlInjected", cycle.getNutrientMl());
            response.put("triggerReason", reason);
        } else {
            response.put("mistCycleTriggered", false);
            response.put("triggerReason", reason);
        }

        return response;
    }

    @Transactional
    public MistCycleLog triggerManualMist(Integer towerId, Integer durationSeconds, BigDecimal nutrientMl) {
        AeroponicTower tower = towerRepository.findById(towerId)
                .orElseThrow(() -> new IllegalArgumentException("Tower not found: " + towerId));

        int duration = durationSeconds != null ? durationSeconds : 30;
        BigDecimal ml = nutrientMl != null ? nutrientMl : new BigDecimal("20.00");

        MistCycleLog cycle = new MistCycleLog(tower, duration, ml, MistCycleLog.TriggerSource.MANUAL);
        return mistCycleLogRepository.save(cycle);
    }

    @Transactional
    public CropYield recordHarvest(Integer towerId, Integer productId, BigDecimal quantityKg, LocalDate harvestDate) {
        AeroponicTower tower = towerRepository.findById(towerId)
                .orElseThrow(() -> new IllegalArgumentException("Tower not found: " + towerId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        LocalDate date = harvestDate != null ? harvestDate : LocalDate.now();
        CropYield cropYield = new CropYield(tower, product, date, quantityKg);
        return cropYieldRepository.save(cropYield);
    }

    public List<CropYield> getAllHarvests() {
        return cropYieldRepository.findAllByOrderByHarvestDateDesc();
    }
}
