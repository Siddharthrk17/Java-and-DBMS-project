# 🌱 Aeroponic Farm Management System

**Autonomous Vertical Urban Aeroponic Farming & Nutrient Injection System**

A fully functional desktop CLI application that automates aeroponic crop tower monitoring, nutrient mist injection scheduling, harvest tracking, grocery chain billing, and double-entry farm accounting.

---

## Technology Stack

| Layer       | Technology                                  |
|-------------|---------------------------------------------|
| Language    | Java 17+ (Core Java, JDBC)                  |
| Database    | MySQL 8.x                                   |
| DB Driver   | MySQL Connector/J 8.3.0                     |
| Build       | Maven (single dependency: mysql-connector-j)|
| UI          | Console / CLI                               |
| ORM         | None — raw JDBC PreparedStatement only      |
| Frameworks  | None                                        |

---

## Project Structure

```
AeroponicFarm/
├── src/main/java/main/
│   ├── AeroponicApp.java              # Entry point / main menu
│   ├── db/
│   │   └── DBConnection.java          # MySQL JDBC singleton
│   ├── master/
│   │   ├── ContactMaster.java         # Customer & Vendor CRUD
│   │   ├── ProductMaster.java         # Product/Item CRUD
│   │   └── ChartOfAccounts.java       # CoA CRUD + balance queries
│   ├── telemetry/
│   │   ├── TowerTelemetry.java        # Ingest humidity/PPM data
│   │   └── MistCycleController.java   # Auto/manual misting + yield
│   ├── transactions/
│   │   ├── PurchaseOrder.java         # PO from nutrient supplier
│   │   ├── VendorBill.java            # PO → Vendor Bill → Payment
│   │   ├── SalesOrder.java            # Harvest supply order
│   │   ├── CustomerInvoice.java       # SO → Invoice → Payment
│   │   └── JournalEntry.java          # Double-entry ledger posting
│   ├── budget/
│   │   ├── AnalyticAccount.java       # Farm Zone analytic accounts
│   │   └── BudgetAllocation.java      # Budget vs actual tracking
│   └── reports/
│       ├── BalanceSheet.java          # Assets vs Liabilities
│       ├── ProfitAndLoss.java         # Revenue minus costs
│       └── BudgetReport.java          # Zone budget performance
├── src/main/resources/
│   └── db.properties                  # Database connection config
├── sql/
│   └── schema.sql                     # Full MySQL DDL + seed data
├── pom.xml                            # Maven build (mysql-connector only)
└── README.md
```

---

## Setup Instructions

### 1. Prerequisites

- **Java 17+** (JDK) installed and `java`/`javac` on PATH
- **MySQL 8.x** running locally
- **Maven 3.x** installed (or use `mvnw`)

### 2. Create the MySQL Database

```bash
# Connect to MySQL and create the database
mysql -u root -p -e "CREATE DATABASE aeroponic_farm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Load the schema (creates all 14 tables + seed data)
mysql -u root -p aeroponic_farm < sql/schema.sql
```

### 3. Configure Database Credentials

Edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/aeroponic_farm?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=yourpassword
```

### 4. Build with Maven

```bash
cd AeroponicFarm
mvn clean compile
```

### 5. Run the Application

```bash
mvn exec:java -Dexec.mainClass="main.AeroponicApp"
```

**Alternative — manual compilation:**

```bash
# Compile
javac -cp "lib/mysql-connector-j-8.3.0.jar" -d out/ src/main/java/main/**/*.java src/main/java/main/*.java

# Run (Linux/Mac)
java -cp "out/:lib/mysql-connector-j-8.3.0.jar:src/main/resources/" main.AeroponicApp

# Run (Windows)
java -cp "out\;lib\mysql-connector-j-8.3.0.jar;src\main\resources\" main.AeroponicApp
```

---

## Main Menu

```
═══════════════════════════════════════════════
  MAIN MENU
═══════════════════════════════════════════════
  1. Master Data
     1a. Manage Contacts (Customers/Vendors)
     1b. Manage Products
     1c. Manage Chart of Accounts
     1d. Register Aeroponic Tower

  2. Telemetry & Mist Control
     2a. Ingest Telemetry (Humidity + PPM)
     2b. Manual Mist Trigger
     2c. View Mist History
     2d. Log Crop Yield Harvest

  3. Purchasing
     3a. Create Purchase Order (Nutrients)
     3b. Convert PO → Vendor Bill
     3c. Pay Vendor Bill

  4. Sales
     4a. Create Sales Order (Supermarket)
     4b. Convert SO → Customer Invoice
     4c. Receive Customer Payment

  5. Accounting
     5a. View Journal Entries
     5b. View Account Balances

  6. Reports
     6a. Balance Sheet
     6b. Profit & Loss
     6c. Budget Performance Report

  0. Exit
═══════════════════════════════════════════════
```

---

## Business Rules

1. **Misting Rule**: If `root_humidity_pct < 65%` OR `nutrient_ppm < 800`, auto-trigger a mist cycle and log to `mist_cycle_log`.
2. **Double-Entry**: Every journal posting validates `SUM(debit) == SUM(credit)`. Rolls back on imbalance.
3. **PO → Bill Flow**: PO must be `CONFIRMED` → Bill `DRAFT` → `POSTED` → `PAID`. Each step enforced.
4. **SO → Invoice Flow**: SO must be `CONFIRMED` → Invoice `DRAFT` → `POSTED` → `PAID`. Each step enforced.
5. **Budget Variance**: Budget Report flags accounts where `actual > budgeted` with `*** OVER BUDGET`.
6. **Sequence Numbers**: Auto-generated with `PO-YYYY-NNNN`, `SO-YYYY-NNNN`, `INV-YYYY-NNNN`, `BILL-YYYY-NNNN`.
7. **JDBC Transactions**: Multi-step operations use `setAutoCommit(false)` / `commit()` / `rollback()`.

---

## Database Schema

14 tables organized into 4 groups:

| Group         | Tables                                                    |
|---------------|-----------------------------------------------------------|
| Master Data   | `contacts`, `products`, `chart_of_accounts`, `aeroponic_towers` |
| Telemetry     | `tower_telemetry`, `mist_cycle_log`, `crop_yield`         |
| Transactions  | `purchase_orders`, `po_lines`, `vendor_bills`, `sales_orders`, `so_lines`, `customer_invoices` |
| Accounting    | `journal_entries`, `journal_lines`, `analytic_accounts`, `budgets` |

---

## Seed Data

The `schema.sql` pre-loads:
- **2 Contacts**: FreshMart Superchain (Customer), GreenNutrient Supplies Ltd (Vendor)
- **3 Products**: Aeroponic Lettuce, Mineral Solution, Misting Nozzle
- **3 Towers**: TOWER-A1, TOWER-B2, TOWER-C3
- **3 Analytic Accounts**: ZONE-M1, ZONE-M2, ZONE-M3
- **11 GL Accounts**: Assets (1001-1005), Liabilities (2001-2002), Income (3001-3002), Expense (4001-4002)

---

## License

Internal use — Vertical Aeroponic Farm Management System.
