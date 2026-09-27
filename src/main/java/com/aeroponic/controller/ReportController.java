package com.aeroponic.controller;

import com.aeroponic.repository.*;
import com.aeroponic.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final AeroponicTowerRepository towerRepository;
    private final MistCycleLogRepository mistCycleLogRepository;
    private final CropYieldRepository cropYieldRepository;
    private final CustomerInvoiceRepository invoiceRepository;
    private final VendorBillRepository billRepository;

    public ReportController(ReportService reportService,
                            AeroponicTowerRepository towerRepository,
                            MistCycleLogRepository mistCycleLogRepository,
                            CropYieldRepository cropYieldRepository,
                            CustomerInvoiceRepository invoiceRepository,
                            VendorBillRepository billRepository) {
        this.reportService = reportService;
        this.towerRepository = towerRepository;
        this.mistCycleLogRepository = mistCycleLogRepository;
        this.cropYieldRepository = cropYieldRepository;
        this.invoiceRepository = invoiceRepository;
        this.billRepository = billRepository;
    }

    @GetMapping("/profit-loss")
    public ResponseEntity<Map<String, Object>> getProfitLoss() {
        return ResponseEntity.ok(reportService.getProfitAndLoss());
    }

    @GetMapping("/balance-sheet")
    public ResponseEntity<Map<String, Object>> getBalanceSheet() {
        return ResponseEntity.ok(reportService.getBalanceSheet());
    }

    @GetMapping("/budget")
    public ResponseEntity<List<Map<String, Object>>> getBudgetReport() {
        return ResponseEntity.ok(reportService.getBudgetReport());
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTowers", towerRepository.count());
        stats.put("activeTowers", towerRepository.findAll().stream().filter(t -> "ACTIVE".equals(t.getStatus().name())).count());
        stats.put("totalMistCycles", mistCycleLogRepository.count());
        stats.put("totalHarvestKg", cropYieldRepository.findAll().stream()
                .map(y -> y.getQuantityKg())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.put("totalInvoiced", invoiceRepository.findAll().stream()
                .map(i -> i.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.put("totalBilled", billRepository.findAll().stream()
                .map(b -> b.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        Map<String, Object> pl = reportService.getProfitAndLoss();
        stats.put("netProfit", pl.get("netProfit"));

        return ResponseEntity.ok(stats);
    }
}
