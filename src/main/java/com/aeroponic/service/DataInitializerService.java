package com.aeroponic.service;

import com.aeroponic.model.*;
import com.aeroponic.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializerService implements CommandLineRunner {

    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final ChartOfAccountRepository accountRepository;
    private final AeroponicTowerRepository towerRepository;
    private final TowerTelemetryRepository telemetryRepository;
    private final MistCycleLogRepository mistCycleLogRepository;
    private final CropYieldRepository cropYieldRepository;
    private final AnalyticAccountRepository analyticRepository;
    private final BudgetRepository budgetRepository;
    private final PurchaseOrderRepository poRepository;
    private final POLineRepository poLineRepository;
    private final VendorBillRepository billRepository;
    private final SalesOrderRepository soRepository;
    private final SOLineRepository soLineRepository;
    private final CustomerInvoiceRepository invoiceRepository;
    private final JournalEntryRepository entryRepository;
    private final JournalLineRepository lineRepository;

    public DataInitializerService(ContactRepository contactRepository,
                                  ProductRepository productRepository,
                                  ChartOfAccountRepository accountRepository,
                                  AeroponicTowerRepository towerRepository,
                                  TowerTelemetryRepository telemetryRepository,
                                  MistCycleLogRepository mistCycleLogRepository,
                                  CropYieldRepository cropYieldRepository,
                                  AnalyticAccountRepository analyticRepository,
                                  BudgetRepository budgetRepository,
                                  PurchaseOrderRepository poRepository,
                                  POLineRepository poLineRepository,
                                  VendorBillRepository billRepository,
                                  SalesOrderRepository soRepository,
                                  SOLineRepository soLineRepository,
                                  CustomerInvoiceRepository invoiceRepository,
                                  JournalEntryRepository entryRepository,
                                  JournalLineRepository lineRepository) {
        this.contactRepository = contactRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
        this.towerRepository = towerRepository;
        this.telemetryRepository = telemetryRepository;
        this.mistCycleLogRepository = mistCycleLogRepository;
        this.cropYieldRepository = cropYieldRepository;
        this.analyticRepository = analyticRepository;
        this.budgetRepository = budgetRepository;
        this.poRepository = poRepository;
        this.poLineRepository = poLineRepository;
        this.billRepository = billRepository;
        this.soRepository = soRepository;
        this.soLineRepository = soLineRepository;
        this.invoiceRepository = invoiceRepository;
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Seed Chart of Accounts if empty
        if (accountRepository.count() == 0) {
            accountRepository.save(new ChartOfAccount("1001", "Aeroponic Tower Infrastructure", ChartOfAccount.AccountType.ASSET));
            accountRepository.save(new ChartOfAccount("1002", "LED Lighting Assets", ChartOfAccount.AccountType.ASSET));
            accountRepository.save(new ChartOfAccount("1003", "Cash", ChartOfAccount.AccountType.ASSET));
            accountRepository.save(new ChartOfAccount("1004", "Bank", ChartOfAccount.AccountType.ASSET));
            accountRepository.save(new ChartOfAccount("1005", "Accounts Receivable", ChartOfAccount.AccountType.ASSET));
            accountRepository.save(new ChartOfAccount("2001", "Mineral Supplier Payables", ChartOfAccount.AccountType.LIABILITY));
            accountRepository.save(new ChartOfAccount("2002", "Power Utility Creditors", ChartOfAccount.AccountType.LIABILITY));
            accountRepository.save(new ChartOfAccount("3001", "Produce Sales Revenue", ChartOfAccount.AccountType.INCOME));
            accountRepository.save(new ChartOfAccount("3002", "Farm Tour Revenue", ChartOfAccount.AccountType.INCOME));
            accountRepository.save(new ChartOfAccount("4001", "LED Electricity Expense", ChartOfAccount.AccountType.EXPENSE));
            accountRepository.save(new ChartOfAccount("4002", "Nutrient Chemical Costs", ChartOfAccount.AccountType.EXPENSE));
        }

        // 2. Contacts
        if (contactRepository.count() == 0) {
            contactRepository.save(new Contact("Whole Foods Market", Contact.ContactType.CUSTOMER, "orders@wholefoods.com", "+1-555-0101", "123 Grocery Blvd, New York, NY"));
            contactRepository.save(new Contact("Greenhouse Nutrients Inc.", Contact.ContactType.VENDOR, "sales@greenhousenutrients.com", "+1-555-0202", "45 Industrial Pkwy, Newark, NJ"));
            contactRepository.save(new Contact("Metro Supermarket Chain", Contact.ContactType.CUSTOMER, "procurement@metrosuper.com", "+1-555-0303", "789 Retail Ave, Brooklyn, NY"));
            contactRepository.save(new Contact("Advanced AgroTech Liquid Minerals", Contact.ContactType.VENDOR, "orders@agrotechminerals.com", "+1-555-0404", "101 Science Park, Boston, MA"));
            contactRepository.save(new Contact("Farm Fresh Organic Co-op", Contact.ContactType.CUSTOMER, "buyer@farmfresh.org", "+1-555-0505", "220 Green Way, Queens, NY"));
        }

        // 3. Products
        if (productRepository.count() == 0) {
            productRepository.save(new Product("Organic Aeroponic Lettuce", Product.ProductCategory.GOODS, "KG", new BigDecimal("450.00")));
            productRepository.save(new Product("Romaine Crisp Lettuce", Product.ProductCategory.GOODS, "KG", new BigDecimal("420.00")));
            productRepository.save(new Product("Aromatic Sweet Basil", Product.ProductCategory.GOODS, "KG", new BigDecimal("1200.00")));
            productRepository.save(new Product("Rocket Wild Arugula", Product.ProductCategory.GOODS, "KG", new BigDecimal("850.00")));
            productRepository.save(new Product("Concentrated Mineral Solution (Blend A)", Product.ProductCategory.GOODS, "LITRE", new BigDecimal("1500.00")));
            productRepository.save(new Product("Concentrated Mineral Solution (Blend B)", Product.ProductCategory.GOODS, "LITRE", new BigDecimal("1450.00")));
            productRepository.save(new Product("pH Balancing Solution (Down)", Product.ProductCategory.GOODS, "LITRE", new BigDecimal("800.00")));
            productRepository.save(new Product("Misting Nozzle Assembly v2", Product.ProductCategory.GOODS, "UNIT", new BigDecimal("950.00")));
        }

        // 4. Aeroponic Towers
        if (towerRepository.count() == 0) {
            towerRepository.save(new AeroponicTower("TWR-A01", "Module 1 - North Wing", "Butterhead Lettuce", AeroponicTower.TowerStatus.ACTIVE));
            towerRepository.save(new AeroponicTower("TWR-A02", "Module 1 - North Wing", "Butterhead Lettuce", AeroponicTower.TowerStatus.ACTIVE));
            towerRepository.save(new AeroponicTower("TWR-A03", "Module 1 - North Wing", "Sweet Basil", AeroponicTower.TowerStatus.ACTIVE));
            towerRepository.save(new AeroponicTower("TWR-B01", "Module 2 - South Wing", "Rocket Wild Arugula", AeroponicTower.TowerStatus.ACTIVE));
            towerRepository.save(new AeroponicTower("TWR-B02", "Module 2 - South Wing", "Romaine Crisp Lettuce", AeroponicTower.TowerStatus.MAINTENANCE));
            towerRepository.save(new AeroponicTower("TWR-C01", "Module 3 - East Block", "Butterhead Lettuce", AeroponicTower.TowerStatus.ACTIVE));
            towerRepository.save(new AeroponicTower("TWR-C02", "Module 3 - East Block", "Sweet Basil", AeroponicTower.TowerStatus.ACTIVE));
            towerRepository.save(new AeroponicTower("TWR-D01", "Module 4 - West Block", "Romaine Crisp Lettuce", AeroponicTower.TowerStatus.IDLE));
        }

        // 5. Telemetry
        if (telemetryRepository.count() == 0) {
            var towers = towerRepository.findAll();
            for (var t : towers) {
                telemetryRepository.save(new TowerTelemetry(t, new BigDecimal("74.5"), new BigDecimal("860.0"), LocalDateTime.now().minusHours(2)));
                telemetryRepository.save(new TowerTelemetry(t, new BigDecimal("68.2"), new BigDecimal("820.0"), LocalDateTime.now().minusHours(1)));
                telemetryRepository.save(new TowerTelemetry(t, new BigDecimal("82.0"), new BigDecimal("890.0"), LocalDateTime.now()));
            }
        }

        // 6. Mist Cycle Logs
        if (mistCycleLogRepository.count() == 0) {
            var t1 = towerRepository.findByTowerCode("TWR-A01").orElse(null);
            var t2 = towerRepository.findByTowerCode("TWR-B01").orElse(null);
            if (t1 != null) {
                mistCycleLogRepository.save(new MistCycleLog(t1, 30, new BigDecimal("15.00"), MistCycleLog.TriggerSource.AUTO));
                mistCycleLogRepository.save(new MistCycleLog(t1, 45, new BigDecimal("22.50"), MistCycleLog.TriggerSource.MANUAL));
            }
            if (t2 != null) {
                mistCycleLogRepository.save(new MistCycleLog(t2, 30, new BigDecimal("15.00"), MistCycleLog.TriggerSource.AUTO));
            }
        }

        // 7. Crop Yield Harvests
        if (cropYieldRepository.count() == 0) {
            var t1 = towerRepository.findByTowerCode("TWR-A01").orElse(null);
            var t3 = towerRepository.findByTowerCode("TWR-A03").orElse(null);
            var pLettuce = productRepository.findAll().stream().filter(p -> p.getName().contains("Lettuce")).findFirst().orElse(null);
            var pBasil = productRepository.findAll().stream().filter(p -> p.getName().contains("Basil")).findFirst().orElse(null);

            if (t1 != null && pLettuce != null) {
                cropYieldRepository.save(new CropYield(t1, pLettuce, LocalDate.now().minusDays(5), new BigDecimal("45.500")));
                cropYieldRepository.save(new CropYield(t1, pLettuce, LocalDate.now().minusDays(1), new BigDecimal("52.200")));
            }
            if (t3 != null && pBasil != null) {
                cropYieldRepository.save(new CropYield(t3, pBasil, LocalDate.now().minusDays(3), new BigDecimal("18.750")));
            }
        }

        // 8. Analytic Accounts (Vertical Farm Zones)
        if (analyticRepository.count() == 0) {
            var t1 = towerRepository.findByTowerCode("TWR-A01").orElse(null);
            var t2 = towerRepository.findByTowerCode("TWR-B01").orElse(null);
            var t3 = towerRepository.findByTowerCode("TWR-C01").orElse(null);
            analyticRepository.save(new AnalyticAccount("ZONE-M1", "Aeroponic Module 1 - North Wing", t1));
            analyticRepository.save(new AnalyticAccount("ZONE-M2", "Aeroponic Module 2 - South Wing", t2));
            analyticRepository.save(new AnalyticAccount("ZONE-M3", "Aeroponic Module 3 - East Block", t3));
        }

        // 9. Budgets
        if (budgetRepository.count() == 0) {
            var zones = analyticRepository.findAll();
            var expPower = accountRepository.findByAccountCode("4001").orElse(null);
            var expNutrient = accountRepository.findByAccountCode("4002").orElse(null);
            for (var z : zones) {
                if (expPower != null) {
                    budgetRepository.save(new Budget(z, expPower, LocalDate.now().withDayOfMonth(1), LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1), new BigDecimal("12000.00")));
                }
                if (expNutrient != null) {
                    budgetRepository.save(new Budget(z, expNutrient, LocalDate.now().withDayOfMonth(1), LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1), new BigDecimal("18500.00")));
                }
            }
        }

        // 10. Sample Purchase Order & Vendor Bill & Double Entry
        if (poRepository.count() == 0) {
            var vendor = contactRepository.findByType(Contact.ContactType.VENDOR).stream().findFirst().orElse(null);
            var nutrientProd = productRepository.findAll().stream().filter(p -> p.getName().contains("Mineral Solution")).findFirst().orElse(null);

            if (vendor != null && nutrientProd != null) {
                PurchaseOrder po = new PurchaseOrder();
                po.setPoNumber("PO-2026-1001");
                po.setVendor(vendor);
                po.setOrderDate(LocalDate.now().minusDays(10));
                po.setStatus(PurchaseOrder.POStatus.BILLED);
                po.setNotes("Monthly supply of concentrated mineral liquid nutrients");
                po.setTotalAmount(new BigDecimal("14750.00"));
                po = poRepository.save(po);

                POLine line = new POLine(po, nutrientProd, new BigDecimal("10.000"), new BigDecimal("1475.00"));
                poLineRepository.save(line);

                VendorBill bill = new VendorBill();
                bill.setBillNumber("BILL-2026-1001");
                bill.setPurchaseOrder(po);
                bill.setVendor(vendor);
                bill.setBillDate(LocalDate.now().minusDays(9));
                bill.setDueDate(LocalDate.now().plusDays(21));
                bill.setStatus(VendorBill.BillStatus.POSTED);
                bill.setTotalAmount(new BigDecimal("14750.00"));
                billRepository.save(bill);

                // Double Entry
                var entry = new JournalEntry(JournalEntry.JournalType.PURCHASE, LocalDate.now().minusDays(9), "BILL-2026-1001", "Nutrient Liquid Purchase");
                entry = entryRepository.save(entry);
                var accExpense = accountRepository.findByAccountCode("4002").orElse(null);
                var accPayable = accountRepository.findByAccountCode("2001").orElse(null);
                if (accExpense != null && accPayable != null) {
                    lineRepository.save(new JournalLine(entry, accExpense, new BigDecimal("14750.00"), BigDecimal.ZERO));
                    lineRepository.save(new JournalLine(entry, accPayable, BigDecimal.ZERO, new BigDecimal("14750.00")));
                }
            }
        }

        // 11. Sample Sales Order & Customer Invoice & Double Entry
        if (soRepository.count() == 0) {
            var customer = contactRepository.findByType(Contact.ContactType.CUSTOMER).stream().findFirst().orElse(null);
            var lettuceProd = productRepository.findAll().stream().filter(p -> p.getName().contains("Lettuce")).findFirst().orElse(null);

            if (customer != null && lettuceProd != null) {
                SalesOrder so = new SalesOrder();
                so.setSoNumber("SO-2026-2001");
                so.setCustomer(customer);
                so.setOrderDate(LocalDate.now().minusDays(4));
                so.setStatus(SalesOrder.SOStatus.INVOICED);
                so.setTotalAmount(new BigDecimal("9000.00"));
                so = soRepository.save(so);

                SOLine line = new SOLine(so, lettuceProd, new BigDecimal("20.000"), new BigDecimal("450.00"));
                soLineRepository.save(line);

                CustomerInvoice inv = new CustomerInvoice();
                inv.setInvoiceNumber("INV-2026-2001");
                inv.setSalesOrder(so);
                inv.setCustomer(customer);
                inv.setInvoiceDate(LocalDate.now().minusDays(3));
                inv.setDueDate(LocalDate.now().plusDays(27));
                inv.setStatus(CustomerInvoice.InvoiceStatus.POSTED);
                inv.setTotalAmount(new BigDecimal("9000.00"));
                invoiceRepository.save(inv);

                // Double Entry
                var entry = new JournalEntry(JournalEntry.JournalType.SALES, LocalDate.now().minusDays(3), "INV-2026-2001", "Produce Delivery to Supermarket");
                entry = entryRepository.save(entry);
                var accAR = accountRepository.findByAccountCode("1005").orElse(null);
                var accSales = accountRepository.findByAccountCode("3001").orElse(null);
                if (accAR != null && accSales != null) {
                    lineRepository.save(new JournalLine(entry, accAR, new BigDecimal("9000.00"), BigDecimal.ZERO));
                    lineRepository.save(new JournalLine(entry, accSales, BigDecimal.ZERO, new BigDecimal("9000.00")));
                }
            }
        }
    }
}
