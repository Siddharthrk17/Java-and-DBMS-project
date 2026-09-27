package com.aeroponic.repository;

import com.aeroponic.model.AnalyticAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalyticAccountRepository extends JpaRepository<AnalyticAccount, Integer> {
    Optional<AnalyticAccount> findByCode(String code);
}
