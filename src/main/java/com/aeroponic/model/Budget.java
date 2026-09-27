package com.aeroponic.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Integer budgetId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "analytic_id", nullable = false)
    private AnalyticAccount analyticAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private ChartOfAccount account;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "budgeted_amt", nullable = false, precision = 14, scale = 2)
    private BigDecimal budgetedAmt;

    public Budget() {}

    public Budget(AnalyticAccount analyticAccount, ChartOfAccount account, LocalDate periodStart, LocalDate periodEnd, BigDecimal budgetedAmt) {
        this.analyticAccount = analyticAccount;
        this.account = account;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.budgetedAmt = budgetedAmt;
    }

    public Integer getBudgetId() { return budgetId; }
    public void setBudgetId(Integer budgetId) { this.budgetId = budgetId; }

    public AnalyticAccount getAnalyticAccount() { return analyticAccount; }
    public void setAnalyticAccount(AnalyticAccount analyticAccount) { this.analyticAccount = analyticAccount; }

    public ChartOfAccount getAccount() { return account; }
    public void setAccount(ChartOfAccount account) { this.account = account; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public BigDecimal getBudgetedAmt() { return budgetedAmt; }
    public void setBudgetedAmt(BigDecimal budgetedAmt) { this.budgetedAmt = budgetedAmt; }
}
