package com.aeroponic.repository;

import com.aeroponic.model.AnalyticAccount;
import com.aeroponic.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    List<Budget> findByAnalyticAccount(AnalyticAccount analyticAccount);
}
