package com.aeroponic.repository;

import com.aeroponic.model.AeroponicTower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AeroponicTowerRepository extends JpaRepository<AeroponicTower, Integer> {
    Optional<AeroponicTower> findByTowerCode(String towerCode);
}
