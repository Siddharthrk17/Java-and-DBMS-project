-- =====================================================
-- AEROPONIC FARM MANAGEMENT SYSTEM — MySQL Schema
-- Engine: InnoDB | Charset: utf8mb4
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS budgets;
DROP TABLE IF EXISTS analytic_accounts;
DROP TABLE IF EXISTS journal_lines;
DROP TABLE IF EXISTS journal_entries;
DROP TABLE IF EXISTS customer_invoices;
DROP TABLE IF EXISTS so_lines;
DROP TABLE IF EXISTS sales_orders;
DROP TABLE IF EXISTS vendor_bills;
DROP TABLE IF EXISTS po_lines;
DROP TABLE IF EXISTS purchase_orders;
DROP TABLE IF EXISTS crop_yield;
DROP TABLE IF EXISTS mist_cycle_log;
DROP TABLE IF EXISTS tower_telemetry;
DROP TABLE IF EXISTS aeroponic_towers;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS contacts;
DROP TABLE IF EXISTS chart_of_accounts;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 1. Contacts (Customers & Vendors)
-- =====================================================
CREATE TABLE contacts (
    contact_id    INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    type          ENUM('CUSTOMER','VENDOR') NOT NULL,
    email         VARCHAR(150),
    phone         VARCHAR(30),
    address       TEXT,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_contacts_type ON contacts(type);

-- =====================================================
-- 2. Products / Items
-- =====================================================
CREATE TABLE products (
    product_id    INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    category      ENUM('GOODS','SERVICE') DEFAULT 'GOODS',
    uom           VARCHAR(20) NOT NULL,
    unit_price    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- 3. Chart of Accounts
-- =====================================================
CREATE TABLE chart_of_accounts (
    account_id    INT AUTO_INCREMENT PRIMARY KEY,
    account_code  VARCHAR(20) UNIQUE NOT NULL,
    account_name  VARCHAR(200) NOT NULL,
    account_type  ENUM('ASSET','LIABILITY','INCOME','EXPENSE','EQUITY') NOT NULL,
    parent_id     INT DEFAULT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES chart_of_accounts(account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_coa_type ON chart_of_accounts(account_type);

-- =====================================================
-- 4. Aeroponic Towers
-- =====================================================
CREATE TABLE aeroponic_towers (
    tower_id      INT AUTO_INCREMENT PRIMARY KEY,
    tower_code    VARCHAR(50) UNIQUE NOT NULL,
    location      VARCHAR(100),
    crop_type     VARCHAR(100),
    status        ENUM('ACTIVE','IDLE','MAINTENANCE') DEFAULT 'ACTIVE',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- 5. Tower Telemetry (root humidity %, nutrient PPM)
-- =====================================================
CREATE TABLE tower_telemetry (
    telemetry_id      INT AUTO_INCREMENT PRIMARY KEY,
    tower_id          INT NOT NULL,
    root_humidity_pct DECIMAL(5,2),
    nutrient_ppm      DECIMAL(8,2),
    recorded_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tower_id) REFERENCES aeroponic_towers(tower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_telemetry_tower ON tower_telemetry(tower_id, recorded_at DESC);

-- =====================================================
-- 6. Mist Cycle Log
-- =====================================================
CREATE TABLE mist_cycle_log (
    cycle_id          INT AUTO_INCREMENT PRIMARY KEY,
    tower_id          INT NOT NULL,
    triggered_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    duration_seconds  INT DEFAULT 30,
    nutrient_ml       DECIMAL(8,2),
    triggered_by      ENUM('AUTO','MANUAL') DEFAULT 'AUTO',
    FOREIGN KEY (tower_id) REFERENCES aeroponic_towers(tower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_mist_tower ON mist_cycle_log(tower_id, triggered_at DESC);

-- =====================================================
-- 7. Crop Yield Inventory
-- =====================================================
CREATE TABLE crop_yield (
    yield_id     INT AUTO_INCREMENT PRIMARY KEY,
    tower_id     INT NOT NULL,
    product_id   INT NOT NULL,
    harvest_date DATE NOT NULL,
    quantity_kg  DECIMAL(10,3) NOT NULL,
    FOREIGN KEY (tower_id)   REFERENCES aeroponic_towers(tower_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_yield_tower ON crop_yield(tower_id, harvest_date DESC);

-- =====================================================
-- 8. Purchase Orders
-- =====================================================
CREATE TABLE purchase_orders (
    po_id         INT AUTO_INCREMENT PRIMARY KEY,
    po_number     VARCHAR(50) UNIQUE NOT NULL,
    vendor_id     INT NOT NULL,
    order_date    DATE NOT NULL,
    status        ENUM('DRAFT','CONFIRMED','BILLED','CANCELLED') DEFAULT 'DRAFT',
    total_amount  DECIMAL(14,2) DEFAULT 0.00,
    notes         TEXT,
    FOREIGN KEY (vendor_id) REFERENCES contacts(contact_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE po_lines (
    line_id      INT AUTO_INCREMENT PRIMARY KEY,
    po_id        INT NOT NULL,
    product_id   INT NOT NULL,
    quantity     DECIMAL(12,3) NOT NULL,
    unit_price   DECIMAL(12,2) NOT NULL,
    line_total   DECIMAL(14,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    FOREIGN KEY (po_id)      REFERENCES purchase_orders(po_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- 9. Vendor Bills
-- =====================================================
CREATE TABLE vendor_bills (
    bill_id       INT AUTO_INCREMENT PRIMARY KEY,
    bill_number   VARCHAR(50) UNIQUE NOT NULL,
    po_id         INT,
    vendor_id     INT NOT NULL,
    bill_date     DATE NOT NULL,
    due_date      DATE,
    status        ENUM('DRAFT','POSTED','PAID','CANCELLED') DEFAULT 'DRAFT',
    total_amount  DECIMAL(14,2) DEFAULT 0.00,
    FOREIGN KEY (po_id)      REFERENCES purchase_orders(po_id),
    FOREIGN KEY (vendor_id)  REFERENCES contacts(contact_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- 10. Sales Orders
-- =====================================================
CREATE TABLE sales_orders (
    so_id         INT AUTO_INCREMENT PRIMARY KEY,
    so_number     VARCHAR(50) UNIQUE NOT NULL,
    customer_id   INT NOT NULL,
    order_date    DATE NOT NULL,
    status        ENUM('DRAFT','CONFIRMED','INVOICED','CANCELLED') DEFAULT 'DRAFT',
    total_amount  DECIMAL(14,2) DEFAULT 0.00,
    FOREIGN KEY (customer_id) REFERENCES contacts(contact_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE so_lines (
    line_id      INT AUTO_INCREMENT PRIMARY KEY,
    so_id        INT NOT NULL,
    product_id   INT NOT NULL,
    quantity_kg  DECIMAL(12,3) NOT NULL,
    unit_price   DECIMAL(12,2) NOT NULL,
    line_total   DECIMAL(14,2) GENERATED ALWAYS AS (quantity_kg * unit_price) STORED,
    FOREIGN KEY (so_id)      REFERENCES sales_orders(so_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- 11. Customer Invoices
-- =====================================================
CREATE TABLE customer_invoices (
    invoice_id     INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    so_id          INT,
    customer_id    INT NOT NULL,
    invoice_date   DATE NOT NULL,
    due_date       DATE,
    status         ENUM('DRAFT','POSTED','PAID','CANCELLED') DEFAULT 'DRAFT',
    total_amount   DECIMAL(14,2) DEFAULT 0.00,
    FOREIGN KEY (so_id)       REFERENCES sales_orders(so_id),
    FOREIGN KEY (customer_id) REFERENCES contacts(contact_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- 12. Journal Entries (double-entry)
-- =====================================================
CREATE TABLE journal_entries (
    entry_id      INT AUTO_INCREMENT PRIMARY KEY,
    journal_type  ENUM('SALES','PURCHASE','CASH','BANK','GENERAL') NOT NULL,
    entry_date    DATE NOT NULL,
    reference     VARCHAR(100),
    description   TEXT,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE journal_lines (
    line_id      INT AUTO_INCREMENT PRIMARY KEY,
    entry_id     INT NOT NULL,
    account_id   INT NOT NULL,
    debit        DECIMAL(14,2) DEFAULT 0.00,
    credit       DECIMAL(14,2) DEFAULT 0.00,
    FOREIGN KEY (entry_id)   REFERENCES journal_entries(entry_id),
    FOREIGN KEY (account_id) REFERENCES chart_of_accounts(account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_jl_entry   ON journal_lines(entry_id);
CREATE INDEX idx_jl_account ON journal_lines(account_id);

-- =====================================================
-- 13. Analytic Accounts (Farm Zones)
-- =====================================================
CREATE TABLE analytic_accounts (
    analytic_id   INT AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(30) UNIQUE NOT NULL,
    name          VARCHAR(200) NOT NULL,
    tower_id      INT,
    FOREIGN KEY (tower_id) REFERENCES aeroponic_towers(tower_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- 14. Budget Allocations
-- =====================================================
CREATE TABLE budgets (
    budget_id     INT AUTO_INCREMENT PRIMARY KEY,
    analytic_id   INT NOT NULL,
    account_id    INT NOT NULL,
    period_start  DATE NOT NULL,
    period_end    DATE NOT NULL,
    budgeted_amt  DECIMAL(14,2) NOT NULL,
    FOREIGN KEY (analytic_id) REFERENCES analytic_accounts(analytic_id),
    FOREIGN KEY (account_id)  REFERENCES chart_of_accounts(account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_budget_analytic ON budgets(analytic_id, period_start, period_end);

-- =====================================================
-- SEED DATA — Chart of Accounts
-- =====================================================
INSERT INTO chart_of_accounts (account_code, account_name, account_type) VALUES
('1001', 'Aeroponic Tower Infrastructure', 'ASSET'),
('1002', 'LED Lighting Assets',           'ASSET'),
('1003', 'Cash',                          'ASSET'),
('1004', 'Bank',                          'ASSET'),
('1005', 'Accounts Receivable',           'ASSET'),
('2001', 'Mineral Supplier Payables',     'LIABILITY'),
('2002', 'Power Utility Creditors',       'LIABILITY'),
('3001', 'Produce Sales Revenue',         'INCOME'),
('3002', 'Farm Tour Revenue',             'INCOME'),
('4001', 'LED Electricity Expense',       'EXPENSE'),
('4002', 'Nutrient Chemical Costs',       'EXPENSE');

-- =====================================================
-- SEED DATA — Contacts
-- =====================================================
INSERT INTO contacts (name, type, email, phone) VALUES
('FreshMart Superchain',       'CUSTOMER', 'orders@freshmart.com',      '9876543210'),
('GreenNutrient Supplies Ltd', 'VENDOR',   'supply@greennutrient.com',  '8765432109');

-- =====================================================
-- SEED DATA — Products
-- =====================================================
INSERT INTO products (name, category, uom, unit_price) VALUES
('Organic Aeroponic Lettuce',    'GOODS', 'KG',    250.00),
('Concentrated Mineral Solution','GOODS', 'LITRE', 180.00),
('Misting Nozzle Assembly',      'GOODS', 'UNIT',  850.00);

-- =====================================================
-- SEED DATA — Aeroponic Towers
-- =====================================================
INSERT INTO aeroponic_towers (tower_code, location, crop_type) VALUES
('TOWER-A1', 'Module 1 - North Wing', 'Butterhead Lettuce'),
('TOWER-B2', 'Module 2 - South Wing', 'Rocket Arugula'),
('TOWER-C3', 'Module 3 - East Block', 'Spinach');

-- =====================================================
-- SEED DATA — Analytic Accounts
-- =====================================================
INSERT INTO analytic_accounts (code, name, tower_id) VALUES
('ZONE-M1', 'Aeroponic Module 1', 1),
('ZONE-M2', 'Aeroponic Module 2', 2),
('ZONE-M3', 'Aeroponic Module 3', 3);
