package com.aeroponic.repository;

import com.aeroponic.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Integer> {
    Optional<SalesOrder> findBySoNumber(String soNumber);
    List<SalesOrder> findAllByOrderByOrderDateDesc();
}
