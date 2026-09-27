package com.aeroponic.controller;

import com.aeroponic.model.CustomerInvoice;
import com.aeroponic.model.PurchaseOrder;
import com.aeroponic.model.SalesOrder;
import com.aeroponic.model.VendorBill;
import com.aeroponic.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // --- PURCHASE ORDERS ---
    @GetMapping("/purchase-orders")
    public ResponseEntity<List<PurchaseOrder>> getPurchaseOrders() {
        return ResponseEntity.ok(transactionService.getAllPurchaseOrders());
    }

    @PostMapping("/purchase-orders")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody CreatePoRequest request) {
        PurchaseOrder po = transactionService.createPurchaseOrder(
                request.vendorId,
                request.notes,
                request.lines
        );
        return ResponseEntity.ok(po);
    }

    @PostMapping("/purchase-orders/{id}/convert-to-bill")
    public ResponseEntity<VendorBill> convertPoToBill(@PathVariable Integer id) {
        return ResponseEntity.ok(transactionService.convertPoToVendorBill(id));
    }

    // --- VENDOR BILLS ---
    @GetMapping("/vendor-bills")
    public ResponseEntity<List<VendorBill>> getVendorBills() {
        return ResponseEntity.ok(transactionService.getAllVendorBills());
    }

    @PostMapping("/vendor-bills/{id}/pay")
    public ResponseEntity<VendorBill> payVendorBill(@PathVariable Integer id) {
        return ResponseEntity.ok(transactionService.payVendorBill(id));
    }

    // --- SALES ORDERS ---
    @GetMapping("/sales-orders")
    public ResponseEntity<List<SalesOrder>> getSalesOrders() {
        return ResponseEntity.ok(transactionService.getAllSalesOrders());
    }

    @PostMapping("/sales-orders")
    public ResponseEntity<SalesOrder> createSalesOrder(@RequestBody CreateSoRequest request) {
        SalesOrder so = transactionService.createSalesOrder(
                request.customerId,
                request.lines
        );
        return ResponseEntity.ok(so);
    }

    @PostMapping("/sales-orders/{id}/convert-to-invoice")
    public ResponseEntity<CustomerInvoice> convertSoToInvoice(@PathVariable Integer id) {
        return ResponseEntity.ok(transactionService.convertSoToCustomerInvoice(id));
    }

    // --- CUSTOMER INVOICES ---
    @GetMapping("/customer-invoices")
    public ResponseEntity<List<CustomerInvoice>> getCustomerInvoices() {
        return ResponseEntity.ok(transactionService.getAllCustomerInvoices());
    }

    @PostMapping("/customer-invoices/{id}/pay")
    public ResponseEntity<CustomerInvoice> payCustomerInvoice(@PathVariable Integer id) {
        return ResponseEntity.ok(transactionService.payCustomerInvoice(id));
    }

    // Request DTOs
    public static class CreatePoRequest {
        public Integer vendorId;
        public String notes;
        public List<TransactionService.POLineDto> lines;
    }

    public static class CreateSoRequest {
        public Integer customerId;
        public List<TransactionService.SOLineDto> lines;
    }
}
