package com.aeroponic.repository;

import com.aeroponic.model.POLine;
import com.aeroponic.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface POLineRepository extends JpaRepository<POLine, Integer> {
    List<POLine> findByPurchaseOrder(PurchaseOrder purchaseOrder);
}
