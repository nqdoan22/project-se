-- Create Database
CREATE DATABASE IF NOT EXISTS inventory_management;
USE inventory_management;

-- 1. Users Table
CREATE TABLE Users (
    user_id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('Admin', 'InventoryManager', 'QualityControl', 'Production', 'Viewer') NOT NULL DEFAULT 'Viewer',
    is_active BOOLEAN DEFAULT TRUE,
    last_login DATETIME NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Materials Table
CREATE TABLE Materials (
    material_id VARCHAR(20) PRIMARY KEY,
    part_number VARCHAR(20) NOT NULL UNIQUE,
    material_name VARCHAR(100) NOT NULL,
    material_type ENUM('API', 'Excipient', 'Dietary Supplement', 'Container', 'Closure', 'Process Chemical', 'Testing Material') NOT NULL,
    storage_conditions VARCHAR(100) NULL,
    specification_document VARCHAR(50) NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. LabelTemplates Table
CREATE TABLE LabelTemplates (
    template_id VARCHAR(20) PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    label_type ENUM('Raw Material', 'Sample', 'Intermediate', 'Finished Product', 'API', 'Status') NOT NULL,
    template_content TEXT NOT NULL,
    width DECIMAL(5,2) NOT NULL,
    height DECIMAL(5,2) NOT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. InventoryLots Table
CREATE TABLE InventoryLots (
    lot_id VARCHAR(36) PRIMARY KEY,
    material_id VARCHAR(20) NOT NULL,
    manufacturer_name VARCHAR(100) NOT NULL,
    manufacturer_lot VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(100) NULL,
    received_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    in_use_expiration_date DATE NULL,
    status ENUM('Quarantine', 'Accepted', 'Rejected', 'Depleted') NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit_of_measure VARCHAR(10) NOT NULL,
    storage_location VARCHAR(50) NULL,
    is_sample BOOLEAN DEFAULT FALSE,
    parent_lot_id VARCHAR(36) NULL,
    po_number VARCHAR(30) NULL,
    receiving_form_id VARCHAR(50) NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_lots_material FOREIGN KEY (material_id) REFERENCES Materials(material_id),
    CONSTRAINT fk_lots_parent FOREIGN KEY (parent_lot_id) REFERENCES InventoryLots(lot_id)
);

-- 5. InventoryTransactions Table
CREATE TABLE InventoryTransactions (
    transaction_id VARCHAR(36) PRIMARY KEY,
    lot_id VARCHAR(36) NOT NULL,
    transaction_type ENUM('Receipt', 'Usage', 'Split', 'Transfer', 'Adjustment', 'Disposal') NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit_of_measure VARCHAR(10) NOT NULL,
    reference_id VARCHAR(50) NULL,
    notes TEXT NULL,
    performed_by VARCHAR(50) NOT NULL,
    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_txn_lot FOREIGN KEY (lot_id) REFERENCES InventoryLots(lot_id)
);

-- 6. ProductionBatches Table
CREATE TABLE ProductionBatches (
    batch_id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(20) NOT NULL,
    batch_number VARCHAR(50) NOT NULL UNIQUE,
    batch_size DECIMAL(10,3) NOT NULL,
    unit_of_measure VARCHAR(10) NOT NULL,
    manufacture_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    status ENUM('Planned', 'In Progress', 'Complete', 'Rejected') NOT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_batch_product FOREIGN KEY (product_id) REFERENCES Materials(material_id)
);

-- 7. BatchComponents Table
CREATE TABLE BatchComponents (
    component_id VARCHAR(36) PRIMARY KEY,
    batch_id VARCHAR(36) NOT NULL,
    lot_id VARCHAR(36) NOT NULL,
    planned_quantity DECIMAL(10,3) NOT NULL,
    actual_quantity DECIMAL(10,3) NULL,
    unit_of_measure VARCHAR(10) NOT NULL,
    addition_date DATETIME NULL,
    added_by VARCHAR(50) NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_comp_batch FOREIGN KEY (batch_id) REFERENCES ProductionBatches(batch_id),
    CONSTRAINT fk_comp_lot FOREIGN KEY (lot_id) REFERENCES InventoryLots(lot_id)
);

-- 8. QCTests Table
CREATE TABLE QCTests (
    test_id VARCHAR(36) PRIMARY KEY,
    lot_id VARCHAR(36) NOT NULL,
    test_type ENUM('Identity', 'Potency', 'Microbial', 'Growth Promotion', 'Physical', 'Chemical') NOT NULL,
    test_method VARCHAR(100) NOT NULL,
    test_date DATE NOT NULL,
    test_result VARCHAR(100) NOT NULL,
    acceptance_criteria VARCHAR(200) NULL,
    result_status ENUM('Pass', 'Fail', 'Pending') NOT NULL,
    performed_by VARCHAR(50) NOT NULL,
    verified_by VARCHAR(50) NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_qc_lot FOREIGN KEY (lot_id) REFERENCES InventoryLots(lot_id)
);