package com.aeroponic.controller;

import com.aeroponic.model.AnalyticAccount;
import com.aeroponic.model.Budget;
import com.aeroponic.model.ChartOfAccount;
import com.aeroponic.repository.AnalyticAccountRepository;
import com.aeroponic.repository.BudgetRepository;
import com.aeroponic.repository.ChartOfAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BudgetController {

    private final AnalyticAccountRepository analyticRepository;
    private final BudgetRepository budgetRepository;
    private final ChartOfAccountRepository accountRepository;

    public BudgetController(AnalyticAccountRepository analyticRepository,
                            BudgetRepository budgetRepository,
                            ChartOfAccountRepository accountRepository) {
        this.analyticRepository = analyticRepository;
        this.budgetRepository = budgetRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/analytic-accounts")
    public ResponseEntity<List<AnalyticAccount>> getAnalyticAccounts() {
        return ResponseEntity.ok(analyticRepository.findAll());
    }

    @PostMapping("/analytic-accounts")
    public ResponseEntity<AnalyticAccount> createAnalyticAccount(@RequestBody AnalyticAccount account) {
        return ResponseEntity.ok(analyticRepository.save(account));
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<Budget>> getBudgets() {
        return ResponseEntity.ok(budgetRepository.findAll());
    }

    @PostMapping("/budgets")
    public ResponseEntity<Budget> createBudget(@RequestBody CreateBudgetRequest request) {
        AnalyticAccount zone = analyticRepository.findById(request.analyticId)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found: " + request.analyticId));
        ChartOfAccount account = accountRepository.findById(request.accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.accountId));

        Budget budget = new Budget(
                zone,
                account,
                request.periodStart,
                request.periodEnd,
                request.budgetedAmt
        );
        return ResponseEntity.ok(budgetRepository.save(budget));
    }

    public static class CreateBudgetRequest {
        public Integer analyticId;
        public Integer accountId;
        public LocalDate periodStart;
        public LocalDate periodEnd;
        public BigDecimal budgetedAmt;
    }
}
