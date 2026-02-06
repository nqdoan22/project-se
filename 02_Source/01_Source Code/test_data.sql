USE inventory_management;

-- 1. Users (Passwords are dummy bcrypt-style hashes)
INSERT INTO Users (user_id, username, email, password, role, last_login) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'jdoe', 'jdoe@example.com', '$2b$10$n9ko8a21u.xyz.examplehash', 'InventoryManager', '2025-01-29 14:30:00'),
('b2c3d4e5-f6a7-8901-bcde-f234567890ab', 'asmith', 'asmith@example.com', '$2b$10$m0lp9b32v.abc.examplehash', 'Admin', '2025-01-30 09:00:00'),
('c3d4e5f6-a7b8-9012-cdef-34567890abcd', 'qc_tech1', 'qc1@example.com', '$2b$10$p1qr0c43w.def.examplehash', 'QualityControl', '2025-01-29 10:15:00');

-- 2. Materials
INSERT INTO Materials (material_id, part_number, material_name, material_type, storage_conditions, specification_document) VALUES
('MAT-001', 'PART-10001', 'Vitamin D3 100K', 'API', '2-8°C, dry', 'SPEC-API-001'),
('MAT-002', 'PART-20002', 'Microcrystalline Cellulose', 'Excipient', 'Ambient, dry', 'SPEC-EXC-005'),
('PROD-001', 'PART-90001', 'Vitamin D Tablet 5000IU', 'Dietary Supplement', 'Ambient', 'SPEC-FP-100');

-- 3. LabelTemplates
INSERT INTO LabelTemplates (template_id, template_name, label_type, template_content, width, height) VALUES
('TPL-RM-01', 'Raw Material 2x1', 'Raw Material', 'Raw Material\nLot: {{lot_id}}\nMaterial: {{material_name}}', 2.00, 1.00),
('TPL-FP-01', 'Finished Product 4x3', 'Finished Product', 'Product: {{material_name}}\nBatch: {{batch_number}}\nExp: {{expiration_date}}', 4.00, 3.00);

-- 4. InventoryLots
INSERT INTO InventoryLots (lot_id, material_id, manufacturer_name, manufacturer_lot, supplier_name, received_date, expiration_date, status, quantity, unit_of_measure, storage_location, po_number) VALUES
('lot-uuid-001', 'MAT-001', 'Acme Pharma', 'MFR-2025-001', 'Acme Supply', '2025-01-10', '2026-01-10', 'Accepted', 25.500, 'kg', 'WH-A-12', 'PO-12345'),
('lot-uuid-002', 'MAT-002', 'Bulk Fillers Inc', 'BFI-9988', 'Direct', '2025-01-12', '2027-01-12', 'Accepted', 500.000, 'kg', 'WH-B-01', 'PO-12346');

-- 5. InventoryTransactions
INSERT INTO InventoryTransactions (transaction_id, lot_id, transaction_type, quantity, unit_of_measure, reference_id, performed_by, notes) VALUES
('txn-uuid-001', 'lot-uuid-001', 'Receipt', 25.500, 'kg', NULL, 'jdoe', 'Initial receipt from PO-12345'),
('txn-uuid-002', 'lot-uuid-001', 'Usage', -2.000, 'kg', 'PB-2025-0001', 'jdoe', 'Consumed in production batch PB-2025-0001');

-- 6. ProductionBatches
INSERT INTO ProductionBatches (batch_id, product_id, batch_number, batch_size, unit_of_measure, manufacture_date, expiration_date, status) VALUES
('batch-uuid-001', 'PROD-001', 'PB-2025-0001', 1000.000, 'units', '2025-01-20', '2026-01-20', 'Complete');

-- 7. BatchComponents
INSERT INTO BatchComponents (component_id, batch_id, lot_id, planned_quantity, actual_quantity, unit_of_measure, addition_date, added_by) VALUES
('comp-uuid-001', 'batch-uuid-001', 'lot-uuid-001', 2.000, 2.000, 'kg', '2025-01-20 09:00:00', 'jdoe'),
('comp-uuid-002', 'batch-uuid-001', 'lot-uuid-002', 10.000, 10.150, 'kg', '2025-01-20 09:30:00', 'jdoe');

-- 8. QCTests
INSERT INTO QCTests (test_id, lot_id, test_type, test_method, test_date, test_result, acceptance_criteria, result_status, performed_by, verified_by) VALUES
('test-uuid-001', 'lot-uuid-001', 'Identity', 'HPLC-IDENT-01', '2025-01-12', 'Match', 'Must match standard', 'Pass', 'qc_tech1', 'asmith');