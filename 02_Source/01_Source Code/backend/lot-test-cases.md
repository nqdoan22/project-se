# Unit Test Cases - Lot Service

## Overview
This document outlines comprehensive unit test cases for the Lot Service in the backend, focusing on business logic validation and error handling. Test cases cover success scenarios and business rule violations as defined in the requirements.

---

## Test Cases Table

| Test ID | Test Case Name | Test Values | Tested Function | Auto Testing Functions |
|---|---|---|---|---|
| 1 | **RECEIVE LOT - Success** | materialId: valid UUID, quantity: 100.0, unitOfMeasure: "kg", expirationDate: 2026-12-31, receivedDate: 2025-04-03 | `receiveLot()` | `receiveLot_success_createsQuarantineLotAndRecordsReceipt()` |
| 2 | **RECEIVE LOT - Material Not Found** | materialId: non-existent UUID, quantity: 100.0, unitOfMeasure: "kg", expirationDate: 2026-12-31 | `receiveLot()` | `receiveLot_materialNotFound_throwsResourceNotFoundException()` |
| 3 | **UPDATE LOT STATUS - Quarantine to Accepted - Success** | lotId: valid lot in Quarantine, status: Accepted, performedBy: "user123", notes: "Sample passed QC" | `updateLotStatus()` | `updateLotStatus_quarantineToAccepted_succeeds()` |
| 4 | **UPDATE LOT STATUS - Quarantine to Rejected - Success** | lotId: valid lot in Quarantine, status: Rejected, performedBy: "user123", notes: "Failed inspection" | `updateLotStatus()` | `updateLotStatus_quarantineToRejected_succeeds()` |
| 5 | **UPDATE LOT STATUS - Accepted to Depleted - Success** | lotId: valid lot in Accepted, status: Depleted, performedBy: "user123", notes: "All used" | `updateLotStatus()` | `updateLotStatus_acceptedToDepleted_succeeds()` |
| 6 | **UPDATE LOT STATUS - Rejected to Depleted - Success** | lotId: valid lot in Rejected, status: Depleted, performedBy: "user123", notes: "Disposal complete" | `updateLotStatus()` | `updateLotStatus_rejectedToDepleted_succeeds()` |
| 7 | **UPDATE LOT STATUS - Rejected to Quarantine - Success** | lotId: valid lot in Rejected, status: Quarantine, performedBy: "user123", notes: "Re-quarantine for retest" | `updateLotStatus()` | `updateLotStatus_rejectedToQuarantine_succeeds()` |
| 8 | **UPDATE LOT STATUS - Accepted to Quarantine - Success** | lotId: valid lot in Accepted, status: Quarantine | `updateLotStatus()` | `updateLotStatus_acceptedToQuarantine_throwsBusinessException()` |
| 9 | **UPDATE LOT STATUS - Lot Not Found** | lotId: non-existent UUID, status: Accepted | `updateLotStatus()` | `updateLotStatus_lotNotFound_throwsResourceNotFoundException()` |
| 10 | **UPDATE LOT STATUS - Invalid Transition Quarantine to Depleted** | lotId: valid lot in Quarantine, status: Depleted | `updateLotStatus()` | `updateLotStatus_quarantineToDepleted_throwsBusinessException()` |
| 11 | **UPDATE LOT STATUS - Invalid Transition Rejected to Accepted** | lotId: valid lot in Rejected, status: Accepted | `updateLotStatus()` | `updateLotStatus_rejectedToAccepted_throwsBusinessException()` |
| 12 | **UPDATE LOT STATUS - Invalid Transition Depleted to Accepted** | lotId: valid lot in Depleted, status: Accepted | `updateLotStatus()` | `updateLotStatus_depletedToAnything_throwsBusinessException()` |
| 13 | **UPDATE LOT STATUS - Invalid Transition Depleted to Quarantine** | lotId: valid lot in Depleted, status: Quarantine | `updateLotStatus()` | `updateLotStatus_depletedToAnything_throwsBusinessException()` |
| 14 | **UPDATE LOT STATUS - Invalid Transition Depleted to Rejected** | lotId: valid lot in Depleted, status: Rejected | `updateLotStatus()` | `updateLotStatus_depletedToAnything_throwsBusinessException()` |
| 15 | **UPDATE LOT STATUS - Invalid Transition Depleted to Depleted** | lotId: valid lot in Depleted, status: Depleted | `updateLotStatus()` | `updateLotStatus_quarantineToQuarantine_throwsBusinessException()` |
| 16 | **SPLIT LOT - Success with Accepted Status** | lotId: valid Accepted lot with quantity 1000.0, sampleQuantity: 100.0, storageLocation: "COLD_STORAGE_A", performedBy: "user123" | `splitLot()` | `splitLot_success_deductsParentQtyAndCreatesSampleLot()` |
| 17 | **SPLIT LOT - Success with Quarantine Status** | lotId: valid Quarantine lot with quantity 500.0, sampleQuantity: 50.0, storageLocation: "STORAGE_B", performedBy: "user123" | `splitLot()` | `testSplitLotSuccessQuarantine()` |
| 18 | **SPLIT LOT - Success with Rejected Status** | lotId: valid Rejected lot with quantity 750.0, sampleQuantity: 150.0, storageLocation: "STORAGE_C", performedBy: "user123" | `splitLot()` | `testSplitLotSuccessRejected()` |
| 19 | **SPLIT LOT - Success with Depleted Status** | lotId: valid Depleted lot with quantity 200.0, sampleQuantity: 100.0, storageLocation: "STORAGE_D", performedBy: "user123" | `splitLot()` | `testSplitLotSuccessDepleted()` |
| 20 | **SPLIT LOT - Lot Not Found** | lotId: non-existent UUID, sampleQuantity: 100.0 | `splitLot()` | `testSplitLotNotFound()` |
| 21 | **SPLIT LOT - Sample Quantity Exceeds Available** | lotId: valid Accepted lot with quantity 50.0, sampleQuantity: 100.0 | `splitLot()` | `splitLot_insufficientQuantity_throwsBusinessException()` |
| 22 | **SPLIT LOT - Sample Quantity Equal to Available** | lotId: valid Accepted lot with quantity 100.0, sampleQuantity: 100.0 | `splitLot()` | `testSplitLotQuantityEqualToAvailable()` |
| 23 | **ADJUST LOT - Increase Quantity with Accepted Status** | lotId: valid Accepted lot with quantity 500.0, adjustmentQuantity: 100.0, reason: "Recount correction", performedBy: "user123" | `adjustLot()` | `adjustLot_positiveAdjustment_increasesQuantity()` |
| 24 | **ADJUST LOT - Decrease Quantity with Accepted Status** | lotId: valid Accepted lot with quantity 500.0, adjustmentQuantity: -100.0, reason: "Spillage loss", performedBy: "user123" | `adjustLot()` | `adjustLot_negativeAdjustment_decreasesQuantity()` |
| 25 | **ADJUST LOT - Increase Quantity with Quarantine Status** | lotId: valid Quarantine lot with quantity 300.0, adjustmentQuantity: 50.0, reason: "Initial recount", performedBy: "user123" | `adjustLot()` | `testAdjustLotIncreaseQuarantine()` |
| 26 | **ADJUST LOT - Decrease Quantity with Quarantine Status** | lotId: valid Quarantine lot with quantity 300.0, adjustmentQuantity: -50.0, reason: "Damage loss", performedBy: "user123" | `adjustLot()` | `testAdjustLotDecreaseQuarantine()` |
| 27 | **ADJUST LOT - Increase Quantity with Rejected Status** | lotId: valid Rejected lot with quantity 200.0, adjustmentQuantity: 75.0, reason: "Recount correction", performedBy: "user123" | `adjustLot()` | `testAdjustLotIncreaseRejected()` |
| 28 | **ADJUST LOT - Decrease Quantity with Rejected Status** | lotId: valid Rejected lot with quantity 200.0, adjustmentQuantity: -75.0, reason: "Disposal loss", performedBy: "user123" | `adjustLot()` | `testAdjustLotDecreaseRejected()` |
| 29 | **ADJUST LOT - Attempt Increase with Depleted Status - Blocked** | lotId: valid Depleted lot with quantity 0.0, adjustmentQuantity: 100.0, reason: "Recount" | `adjustLot()` | `testAdjustLotIncreaseDepletedBlocked()` |
| 30 | **ADJUST LOT - Attempt Decrease with Depleted Status - Blocked** | lotId: valid Depleted lot with quantity 0.0, adjustmentQuantity: -50.0, reason: "Removal" | `adjustLot()` | `testAdjustLotDecreaseDepletedBlocked()` |
| 31 | **ADJUST LOT - Lot Not Found** | lotId: non-existent UUID, adjustmentQuantity: 50.0, reason: "test" | `adjustLot()` | `testAdjustLotNotFound()` |
| 32 | **ADJUST LOT - Adjustment Results in Negative Quantity** | lotId: valid Accepted lot with quantity 50.0, adjustmentQuantity: -100.0, reason: "Test removal" | `adjustLot()` | `adjustLot_wouldResultInNegativeQuantity_throwsBusinessException()` |
| 33 | **ADJUST LOT - Adjustment to Zero Quantity** | lotId: valid Accepted lot with quantity 100.0, adjustmentQuantity: -100.0, reason: "Complete removal", performedBy: "user123" | `adjustLot()` | `testAdjustLotToZeroQuantity()` |
| 34 | **TRANSFER LOT - Success** | lotId: valid lot in "STORAGE_A", newStorageLocation: "STORAGE_B", performedBy: "user123", notes: "Moved to new shelf" | `transferLot()` | `transferLot_success_updatesLocationAndRecordsTransaction()` |
| 35 | **TRANSFER LOT - Lot Not Found** | lotId: non-existent UUID, newStorageLocation: "STORAGE_B" | `transferLot()` | `testTransferLotNotFound()` |
| 36 | **TRANSFER LOT - Transfer to Same Location** | lotId: valid lot in "STORAGE_A", newStorageLocation: "STORAGE_A", performedBy: "user123" | `transferLot()` | `testTransferLotSameLocation()` |
| 37 | **DISPOSE LOT - Partial Disposal with Accepted Status** | lotId: valid Accepted lot with quantity 1000.0, disposalQuantity: 300.0, reason: "Disposal per protocol", performedBy: "user123" | `disposeLot()` | `disposeLot_partialDisposal_reducesQuantityWithoutDepleting()` |
| 38 | **DISPOSE LOT - Complete Disposal with Accepted Status** | lotId: valid Accepted lot with quantity 300.0, disposalQuantity: 300.0, reason: "Complete disposal", performedBy: "user123" | `disposeLot()` | `disposeLot_fullDisposal_marksDepletedAndRecordsNegativeTransaction()` |
| 39 | **DISPOSE LOT - Partial Disposal with Quarantine Status** | lotId: valid Quarantine lot with quantity 500.0, disposalQuantity: 100.0, reason: "Failed QC disposal", performedBy: "user123" | `disposeLot()` | `testDisposeLotPartialQuarantine()` |
| 40 | **DISPOSE LOT - Complete Disposal with Quarantine Status** | lotId: valid Quarantine lot with quantity 200.0, disposalQuantity: 200.0, reason: "Complete disposal", performedBy: "user123" | `disposeLot()` | `testDisposeLotCompleteQuarantine()` |
| 41 | **DISPOSE LOT - Partial Disposal with Rejected Status** | lotId: valid Rejected lot with quantity 600.0, disposalQuantity: 150.0, reason: "Rejected lot disposal", performedBy: "user123" | `disposeLot()` | `testDisposeLotPartialRejected()` |
| 42 | **DISPOSE LOT - Complete Disposal with Rejected Status** | lotId: valid Rejected lot with quantity 250.0, disposalQuantity: 250.0, reason: "Complete disposal", performedBy: "user123" | `disposeLot()` | `testDisposeLotCompleteRejected()` |
| 43 | **DISPOSE LOT - Attempt Disposal from Depleted Status - Blocked** | lotId: valid Depleted lot with quantity 0.0, disposalQuantity: 100.0, reason: "Cannot dispose" | `disposeLot()` | `testDisposeLotDepletedBlocked()` |
| 44 | **DISPOSE LOT - Lot Not Found** | lotId: non-existent UUID, disposalQuantity: 100.0, reason: "test" | `disposeLot()` | `testDisposeLotNotFound()` |
| 45 | **DISPOSE LOT - Disposal Quantity Exceeds Available** | lotId: valid Accepted lot with quantity 100.0, disposalQuantity: 200.0, reason: "Over disposal attempt" | `disposeLot()` | `disposeLot_insufficientQuantity_throwsBusinessException()` |
| 46 | **RECEIVE LOT - Valid Request with All Optional Fields** | All required fields + manufacturerName, manufacturerLot, supplierName, inUseExpirationDate, poNumber, receivingFormId | `receiveLot()` | `testReceiveLotWithOptionalFields()` |

---

## Test Case Categories

### 1. Receive Lot Operations
- **Success Path**: Valid material exists, all required fields populated
- **Business Logic Failures**: Material does not exist
- **Request Validation**: Valid request object structure

### 2. Status Update Operations

### 2. Status Update Operations
- **Success Paths**: All valid state transitions
  - Quarantine → Accepted
  - Quarantine → Rejected
  - Accepted → Depleted
  - Rejected → Depleted
  - Rejected → Quarantineitions
  - Depleted status cannot transition to any other state

### 3. Split Lot Operations
- **Success Paths**: Valid lot with sufficient quantity at any status
  - Quarantine status split
  - Accepted status split
  - Rejected status split
  - Depleted status split
- **Business Logic Failures**:
  - Lot does not exist
  - Split quantity exceeds available quantity

### 4. Adjust Lot Operations
- **Success Paths**: Quantity adjustment at applicable statuses
  - Increase quantity (Quarantine, Accepted, Rejected)
  - Decrease quantity (Quarantine, Accepted, Rejected)
- **Business Logic Failures**:
  - Lot does not exist
  - Adjustment results in negative quantity
  - Attempt to adjust Depleted lot (blocked)

### 5. Transfer Lot Operations
- **Success Path**: Valid lot transferred to new or same location
- **Business Logic Failures**: Lot does not exist

### 6. Dispose Lot Operations
- **Success Paths**: Disposal at applicable statuses
  - Partial disposal (Quarantine, Accepted, Rejected - quantity remains)
  - Complete disposal (Quarantine, Accepted, Rejected - quantity becomes zero, status changes to Depleted)
- **Business Logic Failures**:
  - Lot does not exist
  - Disposal quantity exceeds available quantity
  - Attempt to dispose from Depleted lot (blocked)

---

## State Transition Rules Tested

```
Quarantine ←→ Accepted ✓
Quarantine ←→ Rejected ✓
Accepted → Depleted ✓
Rejected → Depleted ✓
Rejected → Quarantine ✓
Depleted ← (no transitions allowed) ✗
```

---

## Notes

- All test cases assume request DTOs are structurally valid (Bean Validation annotations are respected)
- Business logic validation focuses on entity state, quantity constraints, and mandatory resource existence
- **Replicated test cases add status coverage for operations**:
  - **Split operations**: Should succeed with all lot statuses (Quarantine, Accepted, Rejected, Depleted)
  - **Adjust operations**: Should succeed with Quarantine, Accepted, and Rejected statuses; must be blocked for Depleted status
  - **Dispose operations**: Should succeed with Quarantine, Accepted, and Rejected statuses; must be blocked for Depleted status
- **Depleted lot behavior**:
  - Depleted lots represent fully consumed inventory with no remaining quantity
  - Operations requiring quantity manipulation (adjust, dispose) must not be permitted on Depleted lots to maintain data integrity
  - Status transitions to/from Depleted are governed by the status transition rules and may not be reversible
- Each success test case should verify the expected state changes, database persistence, and transaction recording
- Each failure test case should verify the appropriate exception type (ResourceNotFoundException, BusinessException) and error message clarity
- Test cases should use realistic test data values that reflect actual system usage scenarios
- All test methods have been implemented and verified
