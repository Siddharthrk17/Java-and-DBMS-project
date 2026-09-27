package com.aeroponic.controller;

import com.aeroponic.model.*;
import com.aeroponic.service.AeroponicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aeroponics")
@CrossOrigin(origins = "*")
public class AeroponicController {

    private final AeroponicService aeroponicService;

    public AeroponicController(AeroponicService aeroponicService) {
        this.aeroponicService = aeroponicService;
    }

    /**
     * Use Case Step 1: Add aeroponic tower via POST /api/aeroponics/towers
     */
    @PostMapping("/towers")
    public ResponseEntity<AeroponicTower> createTower(@RequestBody AeroponicTower tower) {
        AeroponicTower saved = aeroponicService.addTower(tower);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/towers")
    public ResponseEntity<List<AeroponicTower>> getAllTowers() {
        return ResponseEntity.ok(aeroponicService.getAllTowers());
    }

    @GetMapping("/towers/{id}")
    public ResponseEntity<AeroponicTower> getTowerById(@PathVariable Integer id) {
        return aeroponicService.getTowerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Use Case Step 2:
     * Ingest root humidity via POST /api/aeroponics/telemetry;
     * log mist cycles and update crop yield inventory.
     */
    @PostMapping("/telemetry")
    public ResponseEntity<Map<String, Object>> ingestTelemetry(@RequestBody TelemetryRequest request) {
        Map<String, Object> result = aeroponicService.ingestTelemetry(
                request.towerId,
                request.rootHumidityPct,
                request.nutrientPpm
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/telemetry/latest")
    public ResponseEntity<List<TowerTelemetry>> getLatestTelemetry() {
        return ResponseEntity.ok(aeroponicService.getLatestTelemetry());
    }

    @GetMapping("/telemetry/history/{towerId}")
    public ResponseEntity<List<TowerTelemetry>> getTowerTelemetryHistory(@PathVariable Integer towerId) {
        return ResponseEntity.ok(aeroponicService.getTowerTelemetryHistory(towerId));
    }

    /**
     * Trigger mist injection cycle
     */
    @PostMapping("/towers/{id}/mist")
    public ResponseEntity<MistCycleLog> triggerMistCycle(@PathVariable Integer id,
                                                         @RequestBody(required = false) MistRequest request) {
        Integer duration = request != null ? request.durationSeconds : 30;
        BigDecimal nutrientMl = request != null ? request.nutrientMl : new BigDecimal("20.00");
        MistCycleLog cycle = aeroponicService.triggerManualMist(id, duration, nutrientMl);
        return ResponseEntity.ok(cycle);
    }

    @GetMapping("/mist-cycles")
    public ResponseEntity<List<MistCycleLog>> getRecentMistCycles() {
        return ResponseEntity.ok(aeroponicService.getRecentMistCycles());
    }

    /**
     * Record crop yield / harvest
     */
    @PostMapping("/harvests")
    public ResponseEntity<CropYield> recordHarvest(@RequestBody HarvestRequest request) {
        CropYield yield = aeroponicService.recordHarvest(
                request.towerId,
                request.productId,
                request.quantityKg,
                request.harvestDate != null ? request.harvestDate : LocalDate.now()
        );
        return ResponseEntity.ok(yield);
    }

    @GetMapping("/harvests")
    public ResponseEntity<List<CropYield>> getAllHarvests() {
        return ResponseEntity.ok(aeroponicService.getAllHarvests());
    }

    // Request DTOs
    public static class TelemetryRequest {
        public Integer towerId;
        public BigDecimal rootHumidityPct;
        public BigDecimal nutrientPpm;
    }

    public static class MistRequest {
        public Integer durationSeconds;
        public BigDecimal nutrientMl;
    }

    public static class HarvestRequest {
        public Integer towerId;
        public Integer productId;
        public BigDecimal quantityKg;
        public LocalDate harvestDate;
    }
}
