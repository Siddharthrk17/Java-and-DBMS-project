package com.aeroponic.repository;

import com.aeroponic.model.ChartOfAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChartOfAccountRepository extends JpaRepository<ChartOfAccount, Integer> {
    Optional<ChartOfAccount> findByAccountCode(String accountCode);
    List<ChartOfAccount> findByAccountType(ChartOfAccount.AccountType accountType);
}
