package com.aeroponic.controller;

import com.aeroponic.model.JournalEntry;
import com.aeroponic.model.JournalLine;
import com.aeroponic.service.AccountingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AccountingController {

    private final AccountingService accountingService;

    public AccountingController(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    @GetMapping("/journal-entries")
    public ResponseEntity<List<JournalEntry>> getJournalEntries() {
        return ResponseEntity.ok(accountingService.getAllJournalEntries());
    }

    @GetMapping("/journal-lines")
    public ResponseEntity<List<JournalLine>> getJournalLines() {
        return ResponseEntity.ok(accountingService.getAllJournalLines());
    }
}
