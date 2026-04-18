# Unit Test Cases - Inventory Transaction Service

## Overview
This document outlines comprehensive unit test cases for the Inventory Transaction Service, covering transaction logging and retrieval operations. Test cases validate transaction recording for all inventory operations (receipts, usage, adjustments, transfers, disposals) and transaction history tracking.

---

## Test Cases Table

| Test ID | Test Case Name | Test Values | Tested Function | Auto Testing Functions |
|---|---|---|---|---|
| 89 | **LIST TRANSACTIONS - No Lot Filter** | lotId: null | `listTransactions()` | `listTransactions_noLotId_callsFindAll()` |
| 90 | **LIST TRANSACTIONS - Blank Lot ID Treated as No Filter** | lotId: "   " (spaces) | `listTransactions()` | `listTransactions_blankLotId_callsFindAll()` |
| 91 | **LIST TRANSACTIONS - Filter by Lot ID** | lotId: valid UUID (exists), with 3 transactions | `listTransactions()` | `listTransactions_withValidLotId_returnsTransactionsForLot()` |
| 92 | **LIST TRANSACTIONS - Lot Not Found** | lotId: non-existent UUID | `listTransactions()` | `listTransactions_lotNotFound_throwsResourceNotFoundException()` |
| 93 | **LIST TRANSACTIONS - Pagination Applied** | lotId: valid UUID with 3 transactions | `listTransactions()` | `listTransactions_pagination_appliesPagination()` |
| 94 | **GET TRANSACTION BY ID - Success** | transactionId: valid UUID, quantity: 50 kg | `getTransactionById()` | `getTransactionById_found_returnsTransactionResponse()` |
| 95 | **GET TRANSACTION BY ID - Not Found** | transactionId: non-existent UUID | `getTransactionById()` | `getTransactionById_notFound_throwsResourceNotFoundException()` |
| 96 | **CREATE TRANSACTION - Success** | lotId: valid UUID, type: "Receipt", quantity: 50.0 kg, reference: "REF-001" | `createTransaction()` | `createTransaction_success_savesAndReturnsTransaction()` |
| 97 | **CREATE TRANSACTION - Lot Not Found** | lotId: non-existent UUID, type: "Receipt", quantity: 50.0 | `createTransaction()` | `createTransaction_lotNotFound_throwsResourceNotFoundException()` |
| 98 | **CREATE TRANSACTION - Invalid Transaction Type** | lotId: valid UUID, type: "InvalidType" | `createTransaction()` | `createTransaction_invalidTransactionType_throwsBusinessException()` |
| 99 | **CREATE TRANSACTION - Custom Date Used** | lotId: valid UUID, customDate: "2025-01-15 10:30" | `createTransaction()` | `createTransaction_withCustomDate_usesProvidedDate()` |
| 100 | **CREATE TRANSACTION - Null Date Uses Current Time** | lotId: valid UUID, transactionDate: null | `createTransaction()` | `createTransaction_nullDate_usesCurrentTime()` |

---

## Test Case Categories

### 1. List Transactions Operations
- **No Lot Filter**: Retrieve all transactions in system with pagination
- **Lot Filter**: Retrieve transactions for specific lot (by lot ID)
- **Input Validation**: Blank lot IDs treated as no filter
- **Ordering**: Transactions ordered by date (descending - most recent first)
- **Pagination**: Applied to all list operations

### 2. Get Transaction by ID Operations
- **Success Path**: Valid transaction ID exists in system
- **Resource Failures**: Transaction does not exist
- **Data Retrieval**: Transaction includes lot reference, type, quantity, timestamp

### 3. Create Transaction Operations
- **Success Path**: Valid lot ID exists, valid transaction type, quantity provided
- **Transaction Types**: Receipt, Usage, Adjustment, Transfer, Disposal (enumerated types)
- **Reference Tracking**: Optional reference field (e.g., order number, batch ID)
- **Quantity Tracking**: Records transaction quantity (positive/negative)
- **Date Handling**: 
  - Custom date: Uses provided transaction date
  - Null date: Automatically uses current timestamp
- **Resource Failures**: Lot does not exist
- **Business Logic Failures**: Invalid transaction type specified
- **Data Persistence**: Transaction saved with all fields and timestamp

---

## Business Rules Tested

**Transaction Recording:**
- All inventory operations trigger transaction creation
- Transactions are immutable once created (audit trail)
- Each transaction records lot, operation type, quantity, timestamp

**Lot Association:**
- Transactions must reference an existing lot
- Lot deletion may cascade or preserve transaction history (business rule dependent)
- Transactions enable lot history/traceability

**Transaction Types:**
- **Receipt**: Positive quantity inflow (new inventory received)
- **Usage**: Negative quantity (consumed in production)
- **Adjustment**: Positive/negative (recount corrections, damage loss)
- **Transfer**: Zero-quantity or reference-based (lot relocation)
- **Disposal**: Negative quantity (destruction, expiration)

**Date/Timestamp Management:**
- Transactions dated when created (defaults to current time)
- Custom dates support backdated transaction recording
- Enables historical record correction scenarios

---

## Test Data Patterns

- **Lot IDs**: "lot1", "invalid", null/"   " (for filtering tests)
- **Transaction Types**: Receipt, Usage, Adjustment, Transfer, Disposal (enumerated)
- **Quantities**: 50.0, -50.0, 0.0 kg (positive inflow, negative usage, zero transfers)
- **References**: "REF-001", "BATCH-001", optional (links to related operations)
- **Dates**: Custom dates like "2025-01-15 10:30", null (uses current time)

---

## Transaction Type Specifications

| Type | Quantity | Purpose | Example |
|------|----------|---------|---------|
| **Receipt** | Positive | Incoming inventory | Received shipment +100 kg |
| **Usage** | Negative | Production consumption | Batch component usage -2.0 kg |
| **Adjustment** | +/- | Recount/damage/correction | Spillage loss -10 kg, Recount +5 kg |
| **Transfer** | Typically 0 or reference | Storage relocation | Moved from Storage_A to Storage_B |
| **Disposal** | Negative | Destruction/expiration | Expired lot disposed -50 kg |

---

## Integration Points

**Inventory Lot Service:**
- Lot must exist when creating transaction
- Transaction quantity affects lot available quantity

**Production Batch Service:**
- Component confirmation creates Usage transaction
- Reference field may link to batch ID

**Reports Service:**
- Transaction history feeds lot trace reports
- Aggregated for inventory snapshots

---

## Filtering & Pagination

**List by Lot:**
- Blank/null lot ID → returns all transactions
- Valid lot ID → returns transactions for that lot only
- Lot not found → throws ResourceNotFoundException (validates lot exists)

**Ordering:**
- Transactions for a lot ordered by date (descending)
- Most recent transactions retrieved first

**Pagination:**
- Page size and number applied to all list operations
- Returns subset of results per pagination parameters

---

## Notes

- All test cases assume request DTOs are structurally valid (Bean Validation annotations respected)
- Transactions are immutable audit records (cannot be modified after creation)
- Transaction quantity can be positive (inflow), negative (outflow), or zero (reference)
- Date defaults to current system time if not provided
- Custom dates support historical/backdated transaction recording
- Lot filtering validates lot existence to prevent orphaned transactions
- Each test verifies appropriate exception types (ResourceNotFoundException, BusinessException)
