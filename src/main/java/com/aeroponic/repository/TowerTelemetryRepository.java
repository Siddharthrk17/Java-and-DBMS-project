package com.aeroponic.repository;

import com.aeroponic.model.AeroponicTower;
import com.aeroponic.model.TowerTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TowerTelemetryRepository extends JpaRepository<TowerTelemetry, Integer> {
    List<TowerTelemetry> findByTowerOrderByRecordedAtDesc(AeroponicTower tower);

    @Query("SELECT t FROM TowerTelemetry t WHERE t.recordedAt IN (SELECT MAX(t2.recordedAt) FROM TowerTelemetry t2 GROUP BY t2.tower)")
    List<TowerTelemetry> findLatestTelemetryForAllTowers();

    Optional<TowerTelemetry> findFirstByTowerOrderByRecordedAtDesc(AeroponicTower tower);
}
