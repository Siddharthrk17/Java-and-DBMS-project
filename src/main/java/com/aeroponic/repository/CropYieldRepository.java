package com.aeroponic.repository;

import com.aeroponic.model.CropYield;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropYieldRepository extends JpaRepository<CropYield, Integer> {
    List<CropYield> findAllByOrderByHarvestDateDesc();
}
