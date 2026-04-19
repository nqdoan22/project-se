# Unit Test Cases - Production Batch Service

## Overview
This document outlines comprehensive unit test cases for the Production Batch Service, covering batch lifecycle management from creation through component tracking and status transitions. Test cases validate batch creation, status updates, component management, and associated inventory operations.

---

## Test Cases Table

| Test ID | Test Case Name | Test Values | Tested Function | Auto Testing Functions |
|---|---|---|---|---|
| 63 | **LIST BATCHES - No Filter** | status: null, productId: null | `listBatches()` | `listBatches_noFilter_callsFindAll()` |
| 64 | **LIST BATCHES - Filter by Status** | status: "PLANNED", productId: null | `listBatches()` | `listBatches_statusFilter_callsFindByStatus()` |
| 65 | **LIST BATCHES - Filter by Status and Product** | status: "IN_PROGRESS", productId: "prod1" | `listBatches()` | `listBatches_statusAndProductFilter_callsCombinedQuery()` |
| 66 | **CREATE BATCH - Success with PLANNED Status** | batchNumber: "BATCH-2026-001", size: 50.0 kg, productId: "prod1", mfgDate: now, expDate: now+2yrs | `createBatch()` | `createBatch_success_createsBatchWithPlannedStatus()` |
| 67 | **CREATE BATCH - Duplicate Batch Number** | batchNumber: "DUPLICATE" (already in use) | `createBatch()` | `createBatch_duplicateBatchNumber_throwsBusinessException()` |
| 68 | **CREATE BATCH - Product Not Found** | batchNumber: "BATCH-NEW", productId: "missing" (non-existent) | `createBatch()` | `createBatch_productNotFound_throwsResourceNotFoundException()` |
| 69 | **UPDATE BATCH STATUS - PLANNED to IN_PROGRESS** | batchId: valid UUID, currentStatus: "PLANNED", newStatus: "IN_PROGRESS" | `updateBatchStatus()` | `updateBatchStatus_plannedToInProgress_succeeds()` |
| 70 | **UPDATE BATCH STATUS - Batch Not Found** | batchId: non-existent UUID | `updateBatchStatus()` | `updateBatchStatus_batchNotFound_throwsResourceNotFoundException()` |
| 71 | **ADD COMPONENT - Success** | batchId: valid UUID, lotId: valid UUID, plannedQty: 2.0 kg, addedBy: "prod_operator" | `addComponent()` | `addComponent_success_savesComponentWithBatchAndLot()` |
| 72 | **ADD COMPONENT - Lot Not Found** | batchId: valid UUID, lotId: non-existent UUID | `addComponent()` | `addComponent_lotNotFound_throwsResourceNotFoundException()` |
| 73 | **ADD COMPONENT - Batch Not Found** | batchId: non-existent UUID, lotId: valid UUID | `addComponent()` | `addComponent_batchNotFound_throwsResourceNotFoundException()` |
| 74 | **CONFIRM COMPONENT - Success with Actual Quantity** | componentId: valid UUID, actualQty: 1.9 kg, performedBy: "prod_operator" | `confirmComponent()` | `confirmComponent_success_setsActualQtyAndRecordsUsageTransaction()` |
| 75 | **CONFIRM COMPONENT - Component Not Found** | componentId: non-existent UUID | `confirmComponent()` | `confirmComponent_notFound_throwsResourceNotFoundException()` |

---

## Test Case Categories

### 1. List Batches Operations
- **No Filter**: Retrieve all production batches with pagination
- **Status Filter**: Find batches by status (PLANNED, IN_PROGRESS, COMPLETED, CANCELLED)
- **Combined Filters**: Filter by status and product material simultaneously
- **Pagination**: Applied to all list operations

### 2. Create Batch Operations
- **Success Path**: Valid batch number (unique), valid product/material exists
- **Initial Status**: All new batches created with PLANNED status
- **Business Logic Failures**:
  - Duplicate batch number already exists
  - Referenced product/material does not exist
- **Data Persistence**: Batch saved with manufacturing and expiration dates

### 3. Update Batch Status Operations
- **Success Path**: Valid batch ID, valid status transition
- **Supported Transitions**: PLANNED → IN_PROGRESS, and other defined state machine transitions
- **Resource Failures**: Batch does not exist
- **Audit Trail**: Status changes are tracked with timestamps

### 4. Add Component Operations
- **Success Path**: Valid batch and lot IDs exist
- **Component Data**: Records planned quantity for lot used in batch
- **Resource Failures**:
  - Batch does not exist
  - Lot does not exist
- **Data Tracking**: Component records link batch to inventory lot with planned quantities

### 5. Confirm Component Operations
- **Success Path**: Valid component ID exists, actual quantity provided
- **Inventory Impact**: Records Usage transaction with negative quantity adjustment
- **Audit Trail**: Tracks performer/operator who confirmed component usage
- **Resource Failures**: Component does not exist
- **Quantity Handling**: Supports fractional quantities (e.g., 1.9 kg vs planned 2.0 kg)

---

## Business Rules Tested

**Batch Creation & Uniqueness:**
- Batch numbers must be unique across all production batches
- New batches always start in PLANNED status
- Manufacturing and expiration dates are tracked
- Referenced product/material must exist in system

**Batch Lifecycle:**
- Batches progress through defined status states
- Status transitions follow business rules (not all transitions valid)
- Historical tracking of status changes

**Component Management:**
- Components track which inventory lots are used in which batches
- Planned quantities are recorded at component creation
- Actual quantities recorded when component is confirmed
- Actual quantity can differ from planned (waste, overage tracking)

**Inventory Integration:**
- Confirming components triggers inventory transactions (Usage type)
- Quantity deductions applied to source lot when component confirmed
- Transaction records performer/operator responsible for usage

---

## Test Data Patterns

- **Batch Numbers**: "BATCH-2026-001", "BATCH-NEW", "DUPLICATE" (for conflict testing)
- **Product IDs**: "prod1", "missing", "invalid" (for not-found testing)
- **Quantities**: 50.0 kg, 2.0 kg, 1.9 kg, 100.0 kg (demonstrating planned vs actual)
- **Statuses**: PLANNED, IN_PROGRESS, COMPLETED, CANCELLED (defined state machine)
- **Dates**: Manufacturing date = now, Expiration date = now + 2 years

---

## Integration Points

**Inventory Lot Service:**
- Lot must exist when adding as component
- Lot status/quantity validated indirectly through lot service

**Inventory Transaction Service:**
- Component confirmation creates Usage transaction
- Negative quantity reflects consumption from lot

**Material/Product Service:**
- Product/material must exist at batch creation time
- Product information included in batch tracking

---

## Notes

- All test cases assume request DTOs are structurally valid (Bean Validation annotations respected)
- Batch status transitions are governed by a defined state machine
- Component creation does not immediately deduct inventory (deduction occurs at confirmation)
- Planned vs actual quantities support waste/overage tracking
- Each test verifies appropriate exception types (ResourceNotFoundException, BusinessException)
- Audit fields (createdBy, updatedAt) maintained automatically for all batch and component operations
