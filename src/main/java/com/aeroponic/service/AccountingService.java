package com.aeroponic.service;

import com.aeroponic.model.*;
import com.aeroponic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AccountingService {

    private final ChartOfAccountRepository accountRepository;
    private final JournalEntryRepository entryRepository;
    private final JournalLineRepository lineRepository;

    public AccountingService(ChartOfAccountRepository accountRepository,
                             JournalEntryRepository entryRepository,
                             JournalLineRepository lineRepository) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
    }

    public List<ChartOfAccount> getAllAccounts() {
        return accountRepository.findAll();
    }

    public ChartOfAccount getAccountByCode(String code) {
        return accountRepository.findByAccountCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + code));
    }

    public List<JournalEntry> getAllJournalEntries() {
        return entryRepository.findAllByOrderByEntryDateDesc();
    }

    public List<JournalLine> getAllJournalLines() {
        return lineRepository.findAllWithDetails();
    }

    /**
     * Create balancing double-entry journal entry
     */
    @Transactional
    public JournalEntry recordDoubleEntry(JournalEntry.JournalType journalType,
                                          LocalDate date,
                                          String reference,
                                          String description,
                                          String debitAccountCode,
                                          String creditAccountCode,
                                          BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        ChartOfAccount debitAccount = getAccountByCode(debitAccountCode);
        ChartOfAccount creditAccount = getAccountByCode(creditAccountCode);

        JournalEntry entry = new JournalEntry(journalType, date != null ? date : LocalDate.now(), reference, description);
        entry = entryRepository.save(entry);

        JournalLine debitLine = new JournalLine(entry, debitAccount, amount, BigDecimal.ZERO);
        JournalLine creditLine = new JournalLine(entry, creditAccount, BigDecimal.ZERO, amount);

        lineRepository.save(debitLine);
        lineRepository.save(creditLine);

        return entry;
    }

    /**
     * Post Vendor Bill:
     * Dr: 4002 Nutrient Chemical Costs (Expense)
     * Cr: 2001 Mineral Supplier Payables (Liability)
     */
    @Transactional
    public JournalEntry postVendorBill(VendorBill bill) {
        return recordDoubleEntry(
                JournalEntry.JournalType.PURCHASE,
                bill.getBillDate(),
                bill.getBillNumber(),
                "Vendor Bill: " + bill.getVendor().getName(),
                "4002", // Nutrient Chemical Costs
                "2001", // Mineral Supplier Payables
                bill.getTotalAmount()
        );
    }

    /**
     * Pay Vendor Bill via Bank:
     * Dr: 2001 Mineral Supplier Payables (Liability reduction)
     * Cr: 1004 Bank (Asset reduction)
     */
    @Transactional
    public JournalEntry payVendorBill(VendorBill bill) {
        return recordDoubleEntry(
                JournalEntry.JournalType.BANK,
                LocalDate.now(),
                "PAY-" + bill.getBillNumber(),
                "Payment to Supplier: " + bill.getVendor().getName(),
                "2001", // Payables
                "1004", // Bank
                bill.getTotalAmount()
        );
    }

    /**
     * Post Customer Invoice:
     * Dr: 1005 Accounts Receivable (Asset increase)
     * Cr: 3001 Produce Sales Revenue (Income increase)
     */
    @Transactional
    public JournalEntry postCustomerInvoice(CustomerInvoice invoice) {
        return recordDoubleEntry(
                JournalEntry.JournalType.SALES,
                invoice.getInvoiceDate(),
                invoice.getInvoiceNumber(),
                "Customer Invoice: " + invoice.getCustomer().getName(),
                "1005", // Accounts Receivable
                "3001", // Produce Sales Revenue
                invoice.getTotalAmount()
        );
    }

    /**
     * Settle Customer Invoice via Bank Transfer:
     * Dr: 1004 Bank (Asset increase)
     * Cr: 1005 Accounts Receivable (Asset reduction)
     */
    @Transactional
    public JournalEntry payCustomerInvoice(CustomerInvoice invoice) {
        return recordDoubleEntry(
                JournalEntry.JournalType.BANK,
                LocalDate.now(),
                "REC-" + invoice.getInvoiceNumber(),
                "Receipt from Grocery Client: " + invoice.getCustomer().getName(),
                "1004", // Bank
                "1005", // Accounts Receivable
                invoice.getTotalAmount()
        );
    }
}
