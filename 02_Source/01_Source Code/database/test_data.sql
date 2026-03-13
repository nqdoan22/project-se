-- ===========================================================================
-- SAMPLE / TEST DATA
-- All passwords are BCrypt hash of: Password@123
-- ===========================================================================

USE inventory_management;

-- ─── 1. Users ───────────────────────────────────────────────────────────────
INSERT INTO Users (user_id, username, email, password, role, is_active, last_login, created_date, modified_date) VALUES
('u0000001-0000-0000-0000-000000000001', 'admin',        'admin@ims.com',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2i', 'Admin',            TRUE, '2026-03-10 08:00:00', '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('u0000001-0000-0000-0000-000000000002', 'inv_manager',  'inv.manager@ims.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2i', 'InventoryManager', TRUE, '2026-03-10 07:30:00', '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('u0000001-0000-0000-0000-000000000003', 'qc_analyst',   'qc.analyst@ims.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2i', 'QualityControl',  TRUE, '2026-03-10 09:00:00', '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('u0000001-0000-0000-0000-000000000004', 'prod_operator','prod.op@ims.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2i', 'Production',      TRUE, '2026-03-09 08:00:00', '2026-01-01 09:00:00', '2026-01-01 09:00:00'),
('u0000001-0000-0000-0000-000000000005', 'viewer1',      'viewer1@ims.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2i', 'Viewer',          TRUE, NULL,                 '2026-01-01 09:00:00', '2026-01-01 09:00:00');

-- ─── 2. Materials ───────────────────────────────────────────────────────────
INSERT INTO Materials (material_id, part_number, material_name, material_type, storage_conditions, specification_document, created_date, modified_date) VALUES
('MAT-001', 'MAT-001', 'Vitamin D3 100,000 IU/g',              'API',                'Store 2–8°C, protect from light',     'SPEC-MAT-001', '2026-01-05 09:00:00', '2026-01-05 09:00:00'),
('MAT-002', 'MAT-002', 'Microcrystalline Cellulose PH102',      'Excipient',          'Store below 30°C, dry conditions',    'SPEC-MAT-002', '2026-01-05 09:10:00', '2026-01-05 09:10:00'),
('MAT-003', 'MAT-003', 'Ascorbic Acid USP Grade',               'Dietary Supplement', 'Store below 25°C, avoid moisture',    'SPEC-MAT-003', '2026-01-05 09:20:00', '2026-01-05 09:20:00'),
('MAT-004', 'MAT-004', 'HDPE Bottle 120mL White',               'Container',          'Ambient, dry storage',                'SPEC-MAT-004', '2026-01-05 09:30:00', '2026-01-05 09:30:00'),
('MAT-005', 'MAT-005', 'Child-Resistant Cap 38mm White',        'Closure',            'Ambient, dry storage',                'SPEC-MAT-005', '2026-01-05 09:40:00', '2026-01-05 09:40:00'),
('MAT-006', 'MAT-006', 'Purified Water WFI Grade',              'Process Chemical',   'Freshly prepared, <24h',              'SPEC-MAT-006', '2026-01-05 09:50:00', '2026-01-05 09:50:00'),
('MAT-007', 'MAT-007', 'Endotoxin Reference Standard BET',      'Testing Material',   'Store at -20°C',                      'SPEC-MAT-007', '2026-01-05 10:00:00', '2026-01-05 10:00:00'),
('MAT-008', 'MAT-008', 'Lactose Monohydrate SuperTab 30SD',     'Excipient',          'Store below 30°C, RH < 65%',          'SPEC-MAT-008', '2026-01-05 10:10:00', '2026-01-05 10:10:00'),
('MAT-009', 'MAT-009', 'Magnesium Stearate NF',                 'Excipient',          'Store below 25°C',                    'SPEC-MAT-009', '2026-01-05 10:20:00', '2026-01-05 10:20:00'),
('MAT-010', 'MAT-010', 'IMS Vitamin D3 Tablet 1000IU',         'Dietary Supplement', 'Store below 25°C, protect from light','SPEC-MAT-010', '2026-01-05 10:30:00', '2026-01-05 10:30:00');

-- ─── 3. LabelTemplates ──────────────────────────────────────────────────────
INSERT INTO LabelTemplates (template_id, template_name, label_type, template_content, width, height, created_date, modified_date) VALUES
('TPL-RM-01',  'Standard Raw Material Label',  'Raw Material',   '<div style="font-family:Arial;font-size:11pt"><b>{{materialName}}</b><br/>Part#: {{partNumber}}<br/>Lot: {{manufacturerLot}}<br/>Mfr: {{manufacturerName}}<br/>Qty: {{quantity}} {{unitOfMeasure}}<br/>Rcvd: {{receivedDate}}<br/>Exp: {{expirationDate}}<br/>Loc: {{storageLocation}}</div>', 10.00, 6.00, '2026-01-10 09:00:00', '2026-01-10 09:00:00'),
('TPL-SMP-01', 'QC Sample Label',              'Sample',         '<div style="font-family:Arial;font-size:10pt"><b>QC SAMPLE</b><br/>{{materialName}}<br/>Lot: {{manufacturerLot}}<br/>Sample Qty: {{quantity}} {{unitOfMeasure}}<br/>Parent Lot: {{parentLotId}}<br/>Exp: {{expirationDate}}<br/>Loc: {{storageLocation}}</div>', 8.00, 5.00, '2026-01-10 09:10:00', '2026-01-10 09:10:00'),
('TPL-FP-01',  'Finished Product Label',       'Finished Product','<div style="font-family:Arial;font-size:11pt"><b>{{productName}}</b><br/>Batch#: {{batchNumber}}<br/>Size: {{batchSize}} {{unitOfMeasure}}<br/>Mfg Date: {{manufactureDate}}<br/>Exp Date: {{expirationDate}}</div>', 12.00, 7.00, '2026-01-10 09:20:00', '2026-01-10 09:20:00'),
('TPL-STS-01', 'Status Change Label',          'Status',         '<div style="font-family:Arial;font-size:10pt"><b>STATUS: {{status}}</b><br/>Lot: {{lotId}}<br/>{{materialName}}<br/>Date: {{statusDate}}</div>', 8.00, 4.00, '2026-01-10 09:30:00', '2026-01-10 09:30:00'),
('TPL-API-01', 'API Material Label',           'API',            '<div style="font-family:Arial;font-size:11pt"><b>ACTIVE PHARMACEUTICAL INGREDIENT</b><br/>{{materialName}}<br/>Part#: {{partNumber}}<br/>Lot: {{manufacturerLot}}<br/>Exp: {{expirationDate}}</div>', 10.00, 6.00, '2026-01-10 09:40:00', '2026-01-10 09:40:00');

-- ─── 4. InventoryLots ───────────────────────────────────────────────────────
-- Parent lots first (no parent_lot_id), then child sample lot
INSERT INTO InventoryLots (lot_id, material_id, manufacturer_name, manufacturer_lot, supplier_name, received_date, expiration_date, in_use_expiration_date, status, quantity, unit_of_measure, storage_location, is_sample, parent_lot_id, po_number, receiving_form_id, created_date, modified_date) VALUES
-- LOT-001: Vitamin D3, Accepted — used in completed production batch
('lot00001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'MAT-001', 'DSM Nutritional Products', 'DSM-2026-0101', 'Global Pharma Supply', '2026-01-15', '2028-06-30', NULL,          'Accepted',   23.500, 'kg',  'Kho A / Kệ-1',   FALSE, NULL,                                '2026-PO-0001', 'RF-2026-0001', '2026-01-15 08:00:00', '2026-03-15 10:00:00'),
-- LOT-002: Vitamin D3, Quarantine — newly received, awaiting QC
('lot00002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'MAT-001', 'DSM Nutritional Products', 'DSM-2026-0215', 'Global Pharma Supply', '2026-02-15', '2028-12-31', NULL,          'Quarantine', 50.000, 'kg',  'Kho A / Kệ-2',   FALSE, NULL,                                '2026-PO-0012', 'RF-2026-0015', '2026-02-15 09:00:00', '2026-02-15 09:00:00'),
-- LOT-003: MCC PH102, Accepted — used in production
('lot00003-cccc-cccc-cccc-cccccccccccc', 'MAT-002', 'FMC BioPolymer',           'FMC-2026-0110', 'Chem Direct Ltd',     '2026-01-20', '2029-01-20', NULL,          'Accepted',  180.000, 'kg',  'Kho B / Kệ-3',   FALSE, NULL,                                '2026-PO-0003', 'RF-2026-0003', '2026-01-20 08:30:00', '2026-02-10 11:00:00'),
-- LOT-004: Ascorbic Acid, Quarantine — newly received
('lot00004-dddd-dddd-dddd-dddddddddddd', 'MAT-003', 'CSPC Pharmaceutical',     'CSPC-2026-031', 'Asia Chem Import',    '2026-03-05', '2028-03-05', NULL,          'Quarantine',  75.000, 'kg',  'Kho A / Kệ-4',   FALSE, NULL,                                '2026-PO-0020', 'RF-2026-0030', '2026-03-05 10:00:00', '2026-03-05 10:00:00'),
-- LOT-005: Ascorbic Acid, Rejected — failed microbial QC
('lot00005-eeee-eeee-eeee-eeeeeeeeeeee', 'MAT-003', 'CSPC Pharmaceutical',     'CSPC-2026-011', 'Asia Chem Import',    '2026-01-10', '2028-01-10', NULL,          'Rejected',   30.000, 'kg',  'Kho C / Cách ly', FALSE, NULL,                                '2026-PO-0009', 'RF-2026-0010', '2026-01-10 08:00:00', '2026-01-25 14:00:00'),
-- LOT-006: Lactose, Accepted
('lot00006-ffff-ffff-ffff-ffffffffffff', 'MAT-008', 'Meggle GmbH',             'MEG-2026-0122', 'Euro Excipients BV',  '2026-01-22', '2028-07-22', NULL,          'Accepted',  250.000, 'kg',  'Kho B / Kệ-5',   FALSE, NULL,                                '2026-PO-0005', 'RF-2026-0005', '2026-01-22 09:00:00', '2026-02-05 10:00:00'),
-- LOT-007: Magnesium Stearate, Accepted
('lot00007-gggg-gggg-gggg-gggggggggggg', 'MAT-009', 'Peter Greven GmbH',       'PG-2026-0118',  'Euro Excipients BV',  '2026-01-18', '2028-01-18', NULL,          'Accepted',   25.000, 'kg',  'Kho B / Kệ-6',   FALSE, NULL,                                '2026-PO-0006', 'RF-2026-0006', '2026-01-18 09:00:00', '2026-02-08 10:00:00'),
-- LOT-008: Vitamin D3, Depleted — fully used up
('lot00008-hhhh-hhhh-hhhh-hhhhhhhhhhhh', 'MAT-001', 'DSM Nutritional Products', 'DSM-2025-1105', 'Global Pharma Supply', '2025-11-05', '2027-11-05', NULL,         'Depleted',    0.000, 'kg',  'Kho A / Kệ-1',   FALSE, NULL,                                '2025-PO-0088', 'RF-2025-0088', '2025-11-05 08:00:00', '2026-02-20 16:00:00'),
-- LOT-009: Vitamin D3 sample (child of LOT-002) — Quarantine awaiting QC
('lot00009-iiii-iiii-iiii-iiiiiiiiiiii', 'MAT-001', 'DSM Nutritional Products', 'DSM-2026-0215', 'Global Pharma Supply', '2026-02-15', '2028-12-31', NULL,         'Quarantine',  0.500, 'kg',  'Phòng KN / Tủ-1', TRUE, 'lot00002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', NULL,          NULL,           '2026-02-16 10:00:00', '2026-02-16 10:00:00'),
-- LOT-010: HDPE Bottles, Accepted — near expiry for testing
('lot00010-jjjj-jjjj-jjjj-jjjjjjjjjjjj', 'MAT-004', 'RPC Packaging',          'RPC-2026-0201',  'Pack Solutions',      '2026-02-01', '2026-04-10', NULL,          'Accepted', 5000.000, 'pcs', 'Kho C / Kệ-1',   FALSE, NULL,                                '2026-PO-0010', 'RF-2026-0010', '2026-02-01 08:00:00', '2026-02-20 09:00:00');

-- ─── 5. InventoryTransactions ────────────────────────────────────────────────
INSERT INTO InventoryTransactions (transaction_id, lot_id, transaction_type, quantity, unit_of_measure, reference_id, notes, performed_by, transaction_date, created_date) VALUES
-- Receipts (initial stock-in for each lot)
('txn00001-0001-0001-0001-000000000001', 'lot00001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Receipt',    25.500, 'kg',  'RF-2026-0001', 'Initial receipt from DSM',             'inv_manager', '2026-01-15 08:30:00', '2026-01-15 08:30:00'),
('txn00001-0001-0001-0001-000000000002', 'lot00002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Receipt',    50.000, 'kg',  'RF-2026-0015', 'Initial receipt from DSM',             'inv_manager', '2026-02-15 09:30:00', '2026-02-15 09:30:00'),
('txn00001-0001-0001-0001-000000000003', 'lot00003-cccc-cccc-cccc-cccccccccccc', 'Receipt',   180.000, 'kg',  'RF-2026-0003', 'Initial receipt from FMC',             'inv_manager', '2026-01-20 09:00:00', '2026-01-20 09:00:00'),
('txn00001-0001-0001-0001-000000000004', 'lot00004-dddd-dddd-dddd-dddddddddddd', 'Receipt',    75.000, 'kg',  'RF-2026-0030', 'Initial receipt from CSPC',            'inv_manager', '2026-03-05 10:30:00', '2026-03-05 10:30:00'),
('txn00001-0001-0001-0001-000000000005', 'lot00005-eeee-eeee-eeee-eeeeeeeeeeee', 'Receipt',    30.000, 'kg',  'RF-2026-0010', 'Initial receipt from CSPC',            'inv_manager', '2026-01-10 08:30:00', '2026-01-10 08:30:00'),
('txn00001-0001-0001-0001-000000000006', 'lot00006-ffff-ffff-ffff-ffffffffffff', 'Receipt',   250.000, 'kg',  'RF-2026-0005', 'Initial receipt from Meggle',          'inv_manager', '2026-01-22 09:30:00', '2026-01-22 09:30:00'),
('txn00001-0001-0001-0001-000000000007', 'lot00007-gggg-gggg-gggg-gggggggggggg', 'Receipt',    25.000, 'kg',  'RF-2026-0006', 'Initial receipt from Peter Greven',    'inv_manager', '2026-01-18 09:30:00', '2026-01-18 09:30:00'),
('txn00001-0001-0001-0001-000000000008', 'lot00008-hhhh-hhhh-hhhh-hhhhhhhhhhhh', 'Receipt',    20.000, 'kg',  'RF-2025-0088', 'Initial receipt from DSM (Nov 2025)',  'inv_manager', '2025-11-05 08:30:00', '2025-11-05 08:30:00'),
('txn00001-0001-0001-0001-000000000009', 'lot00010-jjjj-jjjj-jjjj-jjjjjjjjjjjj','Receipt', 5000.000, 'pcs', 'RF-2026-0010', 'Initial receipt from RPC',             'inv_manager', '2026-02-01 08:30:00', '2026-02-01 08:30:00'),
-- Split: LOT-002 → LOT-009 (QC sample)
('txn00001-0001-0001-0001-000000000010', 'lot00002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Split',       -0.500, 'kg',  'lot00009-iiii-iiii-iiii-iiiiiiiiiiii', 'Split sample for QC testing',   'qc_analyst',  '2026-02-16 10:00:00', '2026-02-16 10:00:00'),
('txn00001-0001-0001-0001-000000000011', 'lot00009-iiii-iiii-iiii-iiiiiiiiiiii', 'Split',        0.500, 'kg',  'lot00002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Sample received from parent lot','qc_analyst',  '2026-02-16 10:00:00', '2026-02-16 10:00:00'),
-- QC status adjustment: LOT-005 moved to Rejected
('txn00001-0001-0001-0001-000000000012', 'lot00005-eeee-eeee-eeee-eeeeeeeeeeee', 'Adjustment',   0.000, 'kg',  NULL, 'Status changed to Rejected after microbial QC failure', 'system', '2026-01-25 14:00:00', '2026-01-25 14:00:00'),
-- QC status adjustment: LOT-001 moved to Accepted after QC pass
('txn00001-0001-0001-0001-000000000013', 'lot00001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Adjustment',   0.000, 'kg',  NULL, 'Status changed to Accepted after all QC tests passed', 'system', '2026-02-01 11:00:00', '2026-02-01 11:00:00'),
-- QC status adjustment: LOT-003 moved to Accepted
('txn00001-0001-0001-0001-000000000014', 'lot00003-cccc-cccc-cccc-cccccccccccc', 'Adjustment',   0.000, 'kg',  NULL, 'Status changed to Accepted after all QC tests passed', 'system', '2026-02-05 10:00:00', '2026-02-05 10:00:00'),
-- Usage: LOT-001 used in BATCH-2026-001
('txn00001-0001-0001-0001-000000000015', 'lot00001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Usage',       -1.500, 'kg',  'BATCH-2026-001', 'Used in production batch BATCH-2026-001', 'prod_operator', '2026-03-15 10:00:00', '2026-03-15 10:00:00'),
-- Usage: LOT-003 used in BATCH-2026-001
('txn00001-0001-0001-0001-000000000016', 'lot00003-cccc-cccc-cccc-cccccccccccc', 'Usage',       -5.000, 'kg',  'BATCH-2026-001', 'Used in production batch BATCH-2026-001', 'prod_operator', '2026-03-15 10:05:00', '2026-03-15 10:05:00'),
-- Usage: LOT-008 fully depleted
('txn00001-0001-0001-0001-000000000017', 'lot00008-hhhh-hhhh-hhhh-hhhhhhhhhhhh', 'Usage',      -20.000, 'kg',  'BATCH-2025-009', 'Fully consumed in production BATCH-2025-009', 'prod_operator', '2026-02-20 16:00:00', '2026-02-20 16:00:00'),
-- Transfer: LOT-006 moved to new location
('txn00001-0001-0001-0001-000000000018', 'lot00006-ffff-ffff-ffff-ffffffffffff', 'Transfer',     0.000, 'kg',  NULL, 'Relocated: Kho A / Kệ-2 → Kho B / Kệ-5 (temperature zone)', 'inv_manager', '2026-02-10 11:00:00', '2026-02-10 11:00:00');

-- ─── 6. ProductionBatches ────────────────────────────────────────────────────
INSERT INTO ProductionBatches (batch_id, product_id, batch_number, batch_size, unit_of_measure, manufacture_date, expiration_date, status, created_date, modified_date) VALUES
('batch001-1111-1111-1111-111111111111', 'MAT-010', 'BATCH-2026-001', 50.000, 'kg', '2026-03-15', '2028-03-15', 'Complete',    '2026-03-10 10:00:00', '2026-03-15 14:00:00'),
('batch002-2222-2222-2222-222222222222', 'MAT-010', 'BATCH-2026-002', 75.000, 'kg', '2026-03-20', '2028-03-20', 'In Progress', '2026-03-18 09:00:00', '2026-03-18 09:00:00'),
('batch003-3333-3333-3333-333333333333', 'MAT-010', 'BATCH-2026-003', 60.000, 'kg', '2026-04-05', '2028-04-05', 'Planned',     '2026-03-25 11:00:00', '2026-03-25 11:00:00');

-- ─── 7. BatchComponents ──────────────────────────────────────────────────────
INSERT INTO BatchComponents (component_id, batch_id, lot_id, planned_quantity, actual_quantity, unit_of_measure, addition_date, added_by, created_date, modified_date) VALUES
-- BATCH-2026-001 (Complete): Vitamin D3 + MCC
('comp0001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'batch001-1111-1111-1111-111111111111', 'lot00001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 1.500, 1.500, 'kg', '2026-03-15 10:00:00', 'prod_operator', '2026-03-10 10:30:00', '2026-03-15 10:00:00'),
('comp0002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'batch001-1111-1111-1111-111111111111', 'lot00003-cccc-cccc-cccc-cccccccccccc', 5.000, 5.000, 'kg', '2026-03-15 10:05:00', 'prod_operator', '2026-03-10 10:35:00', '2026-03-15 10:05:00'),
-- BATCH-2026-002 (In Progress): Lactose + Mg Stearate (actualQty not yet confirmed)
('comp0003-cccc-cccc-cccc-cccccccccccc', 'batch002-2222-2222-2222-222222222222', 'lot00006-ffff-ffff-ffff-ffffffffffff', 8.000, NULL, 'kg', NULL, 'prod_operator', '2026-03-18 09:30:00', '2026-03-18 09:30:00'),
('comp0004-dddd-dddd-dddd-dddddddddddd', 'batch002-2222-2222-2222-222222222222', 'lot00007-gggg-gggg-gggg-gggggggggggg', 0.500, NULL, 'kg', NULL, 'prod_operator', '2026-03-18 09:35:00', '2026-03-18 09:35:00');

-- ─── 8. QCTests ──────────────────────────────────────────────────────────────
INSERT INTO QCTests (test_id, lot_id, test_type, test_method, test_date, test_result, acceptance_criteria, result_status, performed_by, verified_by, created_date, modified_date) VALUES
-- LOT-001 (Vitamin D3): All passed → lot became Accepted
('test0001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'lot00001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Identity',  'HPLC-UV',        '2026-01-28', 'Conforms to specification',        'NLT 99.0% purity',     'Pass',    'qc_analyst', 'admin',       '2026-01-28 09:00:00', '2026-01-28 14:00:00'),
('test0002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'lot00001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Potency',   'UV-Spectroscopy','2026-01-29', '100,500 IU/g — within spec',       'NLT 95,000 IU/g',      'Pass',    'qc_analyst', 'admin',       '2026-01-29 10:00:00', '2026-01-29 15:00:00'),
('test0003-cccc-cccc-cccc-cccccccccccc', 'lot00001-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Microbial', 'USP <61>',       '2026-01-30', 'TAMC <100 CFU/g, TYMC <10 CFU/g', 'TAMC <1000, TYMC <100','Pass',    'qc_analyst', 'admin',       '2026-01-30 09:00:00', '2026-02-01 11:00:00'),
-- LOT-002 (Vitamin D3): QC in progress — Identity pending
('test0004-dddd-dddd-dddd-dddddddddddd', 'lot00002-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Identity',  'HPLC-UV',        '2026-03-01', 'Analysis in progress',            'NLT 99.0% purity',     'Pending', 'qc_analyst', NULL,          '2026-03-01 09:00:00', '2026-03-01 09:00:00'),
-- LOT-003 (MCC): All passed → lot became Accepted
('test0005-eeee-eeee-eeee-eeeeeeeeeeee', 'lot00003-cccc-cccc-cccc-cccccccccccc', 'Identity',  'IR Spectroscopy','2026-01-25', 'Conforms to reference spectrum',   'Must match ref.',       'Pass',    'qc_analyst', 'admin',       '2026-01-25 09:00:00', '2026-01-25 14:00:00'),
('test0006-ffff-ffff-ffff-ffffffffffff', 'lot00003-cccc-cccc-cccc-cccccccccccc', 'Physical',  'Particle Size',  '2026-01-26', 'D90: 180 μm — within spec',       'D90: 150–200 μm',      'Pass',    'qc_analyst', 'admin',       '2026-01-26 09:00:00', '2026-02-05 10:00:00'),
-- LOT-005 (Ascorbic Acid): Failed microbial → lot became Rejected
('test0007-gggg-gggg-gggg-gggggggggggg', 'lot00005-eeee-eeee-eeee-eeeeeeeeeeee', 'Identity',  'HPLC-UV',        '2026-01-18', 'Conforms to specification',        'NLT 99.0%',            'Pass',    'qc_analyst', 'admin',       '2026-01-18 09:00:00', '2026-01-18 14:00:00'),
('test0008-hhhh-hhhh-hhhh-hhhhhhhhhhhh', 'lot00005-eeee-eeee-eeee-eeeeeeeeeeee', 'Microbial', 'USP <62>',       '2026-01-20', 'E. coli detected — OOS result',    'Absent in 1g',         'Fail',    'qc_analyst', 'admin',       '2026-01-20 10:00:00', '2026-01-25 14:00:00'),
-- LOT-009 (Vitamin D3 sample): Growth promotion pending
('test0009-iiii-iiii-iiii-iiiiiiiiiiii', 'lot00009-iiii-iiii-iiii-iiiiiiiiiiii', 'Growth Promotion','EP 2.6.13','2026-03-01','Incubation in progress',          'Adequate growth required','Pending','qc_analyst', NULL,          '2026-03-01 11:00:00', '2026-03-01 11:00:00'),
-- LOT-006 (Lactose): All passed → Accepted
('test0010-jjjj-jjjj-jjjj-jjjjjjjjjjjj', 'lot00006-ffff-ffff-ffff-ffffffffffff','Identity',  'IR Spectroscopy','2026-01-28', 'Conforms to reference spectrum',  'Must match ref.',       'Pass',    'qc_analyst', 'admin',       '2026-01-28 09:30:00', '2026-02-05 10:00:00'),
('test0011-kkkk-kkkk-kkkk-kkkkkkkkkkkk', 'lot00006-ffff-ffff-ffff-ffffffffffff','Chemical',  'Loss on Drying', '2026-01-28', '0.3% w/w — within spec',         'NMT 0.5%',             'Pass',    'qc_analyst', 'admin',       '2026-01-28 10:00:00', '2026-02-05 10:00:00'),
-- LOT-007 (Mg Stearate): All passed → Accepted
('test0012-llll-llll-llll-llllllllllll', 'lot00007-gggg-gggg-gggg-gggggggggggg','Chemical',  'Titration',      '2026-01-22', 'Mg content: 4.2% — within spec', 'Mg 4.0–5.0%',          'Pass',    'qc_analyst', 'admin',       '2026-01-22 09:00:00', '2026-02-08 10:00:00');
