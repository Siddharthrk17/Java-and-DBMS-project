package com.aeroponic.service;

import com.aeroponic.model.*;
import com.aeroponic.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final PurchaseOrderRepository poRepository;
    private final POLineRepository poLineRepository;
    private final VendorBillRepository billRepository;
    private final SalesOrderRepository soRepository;
    private final SOLineRepository soLineRepository;
    private final CustomerInvoiceRepository invoiceRepository;
    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final AccountingService accountingService;

    public TransactionService(PurchaseOrderRepository poRepository,
                              POLineRepository poLineRepository,
                              VendorBillRepository billRepository,
                              SalesOrderRepository soRepository,
                              SOLineRepository soLineRepository,
                              CustomerInvoiceRepository invoiceRepository,
                              ContactRepository contactRepository,
                              ProductRepository productRepository,
                              AccountingService accountingService) {
        this.poRepository = poRepository;
        this.poLineRepository = poLineRepository;
        this.billRepository = billRepository;
        this.soRepository = soRepository;
        this.soLineRepository = soLineRepository;
        this.invoiceRepository = invoiceRepository;
        this.contactRepository = contactRepository;
        this.productRepository = productRepository;
        this.accountingService = accountingService;
    }

    // --- PURCHASE ORDERS ---

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return poRepository.findAllByOrderByOrderDateDesc();
    }

    public List<VendorBill> getAllVendorBills() {
        return billRepository.findAllByOrderByBillDateDesc();
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(Integer vendorId, String notes, List<POLineDto> lineDtos) {
        Contact vendor = contactRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber("PO-" + System.currentTimeMillis() % 1000000);
        po.setVendor(vendor);
        po.setOrderDate(LocalDate.now());
        po.setStatus(PurchaseOrder.POStatus.CONFIRMED);
        po.setNotes(notes);

        BigDecimal total = BigDecimal.ZERO;
        po = poRepository.save(po);

        for (POLineDto dto : lineDtos) {
            Product product = productRepository.findById(dto.productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.productId));
            BigDecimal unitPrice = dto.unitPrice != null ? dto.unitPrice : product.getUnitPrice();
            POLine line = new POLine(po, product, dto.quantity, unitPrice);
            poLineRepository.save(line);
            total = total.add(dto.quantity.multiply(unitPrice));
        }

        po.setTotalAmount(total);
        return poRepository.save(po);
    }

    /**
     * Convert PO into Vendor Bill and post journal entry
     */
    @Transactional
    public VendorBill convertPoToVendorBill(Integer poId) {
        PurchaseOrder po = poRepository.findById(poId)
                .orElseThrow(() -> new IllegalArgumentException("PO not found: " + poId));

        VendorBill bill = new VendorBill();
        bill.setBillNumber("BILL-" + po.getPoNumber().replace("PO-", ""));
        bill.setPurchaseOrder(po);
        bill.setVendor(po.getVendor());
        bill.setBillDate(LocalDate.now());
        bill.setDueDate(LocalDate.now().plusDays(30));
        bill.setStatus(VendorBill.BillStatus.POSTED);
        bill.setTotalAmount(po.getTotalAmount());
        bill = billRepository.save(bill);

        po.setStatus(PurchaseOrder.POStatus.BILLED);
        poRepository.save(po);

        // Double-entry record
        accountingService.postVendorBill(bill);

        return bill;
    }

    /**
     * Issue payment via Bank for Vendor Bill
     */
    @Transactional
    public VendorBill payVendorBill(Integer billId) {
        VendorBill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + billId));

        bill.setStatus(VendorBill.BillStatus.PAID);
        bill = billRepository.save(bill);

        accountingService.payVendorBill(bill);
        return bill;
    }

    // --- SALES ORDERS ---

    public List<SalesOrder> getAllSalesOrders() {
        return soRepository.findAllByOrderByOrderDateDesc();
    }

    public List<CustomerInvoice> getAllCustomerInvoices() {
        return invoiceRepository.findAllByOrderByInvoiceDateDesc();
    }

    @Transactional
    public SalesOrder createSalesOrder(Integer customerId, List<SOLineDto> lineDtos) {
        Contact customer = contactRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        SalesOrder so = new SalesOrder();
        so.setSoNumber("SO-" + System.currentTimeMillis() % 1000000);
        so.setCustomer(customer);
        so.setOrderDate(LocalDate.now());
        so.setStatus(SalesOrder.SOStatus.CONFIRMED);

        BigDecimal total = BigDecimal.ZERO;
        so = soRepository.save(so);

        for (SOLineDto dto : lineDtos) {
            Product product = productRepository.findById(dto.productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.productId));
            BigDecimal unitPrice = dto.unitPrice != null ? dto.unitPrice : product.getUnitPrice();
            SOLine line = new SOLine(so, product, dto.quantityKg, unitPrice);
            soLineRepository.save(line);
            total = total.add(dto.quantityKg.multiply(unitPrice));
        }

        so.setTotalAmount(total);
        return soRepository.save(so);
    }

    /**
     * Convert delivered produce weight (kg) into Customer Invoice
     */
    @Transactional
    public CustomerInvoice convertSoToCustomerInvoice(Integer soId) {
        SalesOrder so = soRepository.findById(soId)
                .orElseThrow(() -> new IllegalArgumentException("SO not found: " + soId));

        CustomerInvoice invoice = new CustomerInvoice();
        invoice.setInvoiceNumber("INV-" + so.getSoNumber().replace("SO-", ""));
        invoice.setSalesOrder(so);
        invoice.setCustomer(so.getCustomer());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(15));
        invoice.setStatus(CustomerInvoice.InvoiceStatus.POSTED);
        invoice.setTotalAmount(so.getTotalAmount());
        invoice = invoiceRepository.save(invoice);

        so.setStatus(SalesOrder.SOStatus.INVOICED);
        soRepository.save(so);

        // Double-entry record
        accountingService.postCustomerInvoice(invoice);

        return invoice;
    }

    /**
     * Settle payment from buyer via Bank transfer
     */
    @Transactional
    public CustomerInvoice payCustomerInvoice(Integer invoiceId) {
        CustomerInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        invoice.setStatus(CustomerInvoice.InvoiceStatus.PAID);
        invoice = invoiceRepository.save(invoice);

        accountingService.payCustomerInvoice(invoice);
        return invoice;
    }

    // DTO helpers
    public static class POLineDto {
        public Integer productId;
        public BigDecimal quantity;
        public BigDecimal unitPrice;
    }

    public static class SOLineDto {
        public Integer productId;
        public BigDecimal quantityKg;
        public BigDecimal unitPrice;
    }
}
