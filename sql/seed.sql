-- Clear existing telemetry and transaction data to avoid foreign key conflicts
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE journal_lines;
TRUNCATE TABLE journal_entries;
TRUNCATE TABLE so_lines;
TRUNCATE TABLE sales_orders;
TRUNCATE TABLE customer_invoices;
TRUNCATE TABLE po_lines;
TRUNCATE TABLE purchase_orders;
TRUNCATE TABLE vendor_bills;
TRUNCATE TABLE mist_cycle_log;
TRUNCATE TABLE tower_telemetry;
TRUNCATE TABLE crop_yield;
TRUNCATE TABLE budgets;
TRUNCATE TABLE analytic_accounts;
TRUNCATE TABLE aeroponic_towers;
TRUNCATE TABLE products;
TRUNCATE TABLE contacts;
TRUNCATE TABLE chart_of_accounts;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- SEED DATA — Chart of Accounts
-- =====================================================
INSERT INTO chart_of_accounts (account_id, account_code, account_name, account_type) VALUES
(1, '1001', 'Aeroponic Tower Infrastructure', 'ASSET'),
(2, '1002', 'LED Lighting Assets',           'ASSET'),
(3, '1003', 'Cash',                          'ASSET'),
(4, '1004', 'Bank',                          'ASSET'),
(5, '1005', 'Accounts Receivable',           'ASSET'),
(6, '2001', 'Mineral Supplier Payables',     'LIABILITY'),
(7, '2002', 'Power Utility Creditors',       'LIABILITY'),
(8, '3001', 'Produce Sales Revenue',         'INCOME'),
(9, '3002', 'Farm Tour Revenue',             'INCOME'),
(10, '4001', 'LED Electricity Expense',       'EXPENSE'),
(11, '4002', 'Nutrient Chemical Costs',       'EXPENSE');

-- 1. Contacts
INSERT INTO contacts (name, type, email, phone, address) VALUES
('Whole Foods Market', 'CUSTOMER', 'orders@wholefoods.com', '555-0101', '123 Grocery Blvd, NY'),
('Greenhouse Nutrients Inc.', 'VENDOR', 'sales@greenhousenutrients.com', '555-0202', '45 Industrial Pkwy, NJ'),
('Metro Restaurants Group', 'CUSTOMER', 'supply@metrorest.com', '555-0303', '789 Dining Ave, NY'),
('Advanced AgroTech', 'VENDOR', 'support@agrotech.com', '555-0404', '101 Tech Valley, CA'),
('Farm Fresh Co-op', 'CUSTOMER', 'buyer@farmfresh.org', '555-0505', '222 Market St, NY');

-- 2. Products
INSERT INTO products (name, category, uom, unit_price) VALUES
('Butterhead Lettuce', 'GOODS', 'KG', 450.00),
('Romaine Lettuce', 'GOODS', 'KG', 420.00),
('Sweet Basil', 'GOODS', 'KG', 1200.00),
('Rocket Arugula', 'GOODS', 'KG', 850.00),
('Cherry Tomatoes', 'GOODS', 'KG', 550.00),
('Hydroponic Nutrient Blend A', 'GOODS', 'LITRE', 1500.00),
('Hydroponic Nutrient Blend B', 'GOODS', 'LITRE', 1450.00),
('pH Down Solution', 'GOODS', 'LITRE', 800.00),
('Misting Nozzle v2', 'GOODS', 'UNIT', 950.00);

-- 3. Towers
INSERT INTO aeroponic_towers (tower_code, location, crop_type, status) VALUES
('TWR-A01', 'Zone A - North', 'Butterhead Lettuce', 'ACTIVE'),
('TWR-A02', 'Zone A - North', 'Butterhead Lettuce', 'ACTIVE'),
('TWR-A03', 'Zone A - North', 'Sweet Basil', 'ACTIVE'),
('TWR-B01', 'Zone B - East', 'Rocket Arugula', 'ACTIVE'),
('TWR-B02', 'Zone B - East', 'Cherry Tomatoes', 'MAINTENANCE'),
('TWR-C01', 'Zone C - South', 'Romaine Lettuce', 'ACTIVE'),
('TWR-C02', 'Zone C - South', 'Romaine Lettuce', 'ACTIVE'),
('TWR-D01', 'Zone D - West', 'Sweet Basil', 'IDLE');

-- 4. Telemetry (Historical data for the dashboard)
INSERT INTO tower_telemetry (tower_id, root_humidity_pct, nutrient_ppm, recorded_at) VALUES
(1, 72.5, 850, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 68.2, 830, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 64.1, 790, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(1, 85.0, 900, NOW()),
(2, 75.0, 880, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 71.0, 860, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 69.5, 840, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(2, 68.0, 820, NOW()),
(3, 62.0, 750, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(3, 80.0, 880, NOW()),
(4, 70.1, 850, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(4, 69.8, 845, NOW()),
(6, 66.5, 810, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(6, 63.2, 795, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(6, 82.0, 890, NOW());

-- 5. Mist Cycle Logs
INSERT INTO mist_cycle_log (tower_id, triggered_by, duration_seconds, nutrient_ml, triggered_at) VALUES
(1, 'AUTO', 30, 15.0, DATE_SUB(NOW(), INTERVAL 29 MINUTE)),
(3, 'AUTO', 30, 15.0, DATE_SUB(NOW(), INTERVAL 59 MINUTE)),
(6, 'AUTO', 30, 15.0, DATE_SUB(NOW(), INTERVAL 59 MINUTE)),
(2, 'MANUAL', 45, 22.5, DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- 6. Analytic Accounts
INSERT INTO analytic_accounts (code, name, tower_id) VALUES
('ZONE-A', 'North Wing Production', NULL),
('ZONE-B', 'East Wing Production', NULL),
('TWR-A01-CST', 'Tower A01 Costs', 1),
('TWR-B01-CST', 'Tower B01 Costs', 4);

-- 7. Purchase Orders & Bills (Historical)
-- PO 1 (Nutrients)
INSERT INTO purchase_orders (po_id, po_number, vendor_id, order_date, status, total_amount) VALUES 
(1, 'PO-2026-1001', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), 'BILLED', 14750.00);

INSERT INTO po_lines (po_id, product_id, quantity, unit_price) VALUES
(1, 6, 5.0, 1500.00),
(1, 7, 5.0, 1450.00);

INSERT INTO vendor_bills (bill_number, vendor_id, po_id, bill_date, due_date, status, total_amount) VALUES
('BILL-2026-1001', 2, 1, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_ADD(NOW(), INTERVAL 21 DAY), 'POSTED', 14750.00);

-- PO 2 (Equipment)
INSERT INTO purchase_orders (po_id, po_number, vendor_id, order_date, status, total_amount) VALUES 
(2, 'PO-2026-1002', 4, DATE_SUB(NOW(), INTERVAL 2 DAY), 'CONFIRMED', 9500.00);

INSERT INTO po_lines (po_id, product_id, quantity, unit_price) VALUES
(2, 9, 10.0, 950.00);

-- 8. Sales Orders & Invoices (Historical)
-- SO 1
INSERT INTO sales_orders (so_id, so_number, customer_id, order_date, status, total_amount) VALUES
(1, 'SO-2026-2001', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 'INVOICED', 9000.00);

INSERT INTO so_lines (so_id, product_id, quantity_kg, unit_price) VALUES
(1, 1, 10.0, 450.00),
(1, 3, 3.75, 1200.00);

INSERT INTO customer_invoices (invoice_number, customer_id, so_id, invoice_date, due_date, status, total_amount) VALUES
('INV-2026-2001', 1, 1, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 26 DAY), 'POSTED', 9000.00);

-- SO 2
INSERT INTO sales_orders (so_id, so_number, customer_id, order_date, status, total_amount) VALUES
(2, 'SO-2026-2002', 3, DATE_SUB(NOW(), INTERVAL 1 DAY), 'CONFIRMED', 4250.00);

INSERT INTO so_lines (so_id, product_id, quantity_kg, unit_price) VALUES
(2, 4, 5.0, 850.00);

-- 9. Journal Entries (Balanced double-entry accounting for the posted bill and invoice)

-- Journal for Bill 1001
INSERT INTO journal_entries (entry_id, journal_type, entry_date, reference, description) VALUES
(1, 'PURCHASE', DATE_SUB(NOW(), INTERVAL 9 DAY), 'BILL-2026-1001', 'Nutrient Purchase');

INSERT INTO journal_lines (entry_id, account_id, debit, credit) VALUES
(1, 11, 14750.00, 0.00), -- 4002 Nutrient Chemical Costs
(1, 6, 0.00, 14750.00); -- 2001 Mineral Supplier Payables

-- Journal for Invoice 2001
INSERT INTO journal_entries (entry_id, journal_type, entry_date, reference, description) VALUES
(2, 'SALES', DATE_SUB(NOW(), INTERVAL 4 DAY), 'INV-2026-2001', 'Produce Sale to Whole Foods');

INSERT INTO journal_lines (entry_id, account_id, debit, credit) VALUES
(2, 5, 9000.00, 0.00), -- 1005 Accounts Receivable
(2, 8, 0.00, 9000.00); -- 3001 Produce Sales Revenue
