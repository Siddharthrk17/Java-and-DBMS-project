package com.aeroponic.repository;

import com.aeroponic.model.AeroponicTower;
import com.aeroponic.model.MistCycleLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MistCycleLogRepository extends JpaRepository<MistCycleLog, Integer> {
    List<MistCycleLog> findByTowerOrderByTriggeredAtDesc(AeroponicTower tower);
    List<MistCycleLog> findTop20ByOrderByTriggeredAtDesc();
}
