package com.aeroponic.repository;

import com.aeroponic.model.SOLine;
import com.aeroponic.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SOLineRepository extends JpaRepository<SOLine, Integer> {
    List<SOLine> findBySalesOrder(SalesOrder salesOrder);
}
