package com.aeroponic.repository;

import com.aeroponic.model.VendorBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorBillRepository extends JpaRepository<VendorBill, Integer> {
    Optional<VendorBill> findByBillNumber(String billNumber);
    List<VendorBill> findAllByOrderByBillDateDesc();
}
