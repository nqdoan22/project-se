# Unit Test Cases - Report Service

## Overview
This document outlines comprehensive unit test cases for the Report Service, covering business intelligence and analytics reporting. Test cases validate report generation including dashboard aggregations, near-expiry tracking, lot traceability, quality control summaries, and inventory snapshots.

---

## Test Cases Table

| Test ID | Test Case Name | Test Values | Tested Function | Auto Testing Functions |
|---|---|---|---|---|
| 119 | **DASHBOARD REPORT - Aggregated Data** | materials: 10, lots: 3 (1 Accepted, 1 Quarantine, 1 Rejected), batches: 1 IN_PROGRESS, QC: 2 (1 Pass, 1 Fail) | `getDashboard()` | `getDashboard_returnsAggregatedData()` |
| 120 | **DASHBOARD REPORT - Status Counts** | materials: 5, lots: 3 (2 Accepted, 1 Rejected) | `getDashboard()` | `getDashboard_countsByStatus()` |
| 121 | **NEAR EXPIRY REPORT - 30 Day Range** | days: 30 | `getNearExpiry()` | `getNearExpiry_returnsItemsWithinDays()` |
| 122 | **NEAR EXPIRY REPORT - 60 Day Range** | days: 60 | `getNearExpiry()` | `getNearExpiry_withDifferentDayRange()` |
| 123 | **LOT TRACE REPORT - Complete Trace with Transactions** | lotId: valid UUID, transactions: 1 (Receipt), QC tests: 1 (Pass) | `getLotTrace()` | `getLotTrace_found_returnsCompleteTrace()` |
| 124 | **LOT TRACE REPORT - Lot Not Found** | lotId: non-existent UUID | `getLotTrace()` | `getLotTrace_notFound_throwsResourceNotFoundException()` |
| 125 | **LOT TRACE REPORT - Includes Batch Usage** | lotId: valid UUID, transaction: Usage type, batchRef: "batch1" | `getLotTrace()` | `getLotTrace_withBatchUsages_includesUsageTransactions()` |
| 126 | **QC REPORT - Summary Statistics** | dateRange: last 30 days, tests: 4 (2 Pass, 1 Fail, 1 Pending) | `getQCReport()` | `getQCReport_calculatesSummaryStats()` |
| 127 | **QC REPORT - Pass Rate Calculation** | dateRange: last 30 days, tests: 4 (2 Pass, 2 Fail), expectedPassRate: 50.0% | `getQCReport()` | `getQCReport_calculatesSummaryStats()` |
| 128 | **QC REPORT - Empty Results** | dateRange: last 30 days, tests: 0 | `getQCReport()` | `getQCReport_emptyResults_passRateZero()` |
| 129 | **QC REPORT - Group by Material** | dateRange: last 30 days, tests: 1 for material "mat1" | `getQCReport()` | `getQCReport_groupsByMaterial()` |
| 130 | **INVENTORY SNAPSHOT - No Filter** | status: null, type: null | `getInventorySnapshot()` | `getInventorySnapshot_noFilter_returnsAllLots()` |
| 131 | **INVENTORY SNAPSHOT - Filter by Lot Status** | status: "Accepted" | `getInventorySnapshot()` | `getInventorySnapshot_filterByStatus()` |
| 132 | **INVENTORY SNAPSHOT - Filter by Material Type** | type: "API" | `getInventorySnapshot()` | `getInventorySnapshot_filterByMaterialType()` |
| 133 | **INVENTORY SNAPSHOT - Calculate Total Available Quantity** | material: "mat1", lots: 2 (100 kg + 200 kg), expectedTotal: 300 kg | `getInventorySnapshot()` | `getInventorySnapshot_calculatesTotalAvailable()` |
| 134 | **INVENTORY SNAPSHOT - Empty Lots** | no lots in system | `getInventorySnapshot()` | `getInventorySnapshot_emptyLots_returnsEmpty()` |

---

## Test Case Categories

### 1. Dashboard Report Operations
- **Data Aggregation**: Summarizes key business metrics at a glance
- **Metrics Included**:
  - Total materials in system
  - Total active/managed lots by status (Accepted, Quarantine, Rejected)
  - Near-expiry lot count
  - Active/in-progress production batches
  - Failed QC tests in last 30 days
- **Status Breakdown**: Counts lots grouped by status (byStatus map)
- **Real-time**: Reflects current system state

### 2. Near Expiry Report Operations
- **Configurable Timeframe**: Accepts day range parameter (30, 60, etc.)
- **Expiration Logic**: Identifies lots expiring within specified days from current date
- **Results**: Lists lots approaching expiration dates
- **Use Case**: Alerts for inventory management (usage priority, disposal planning)

### 3. Lot Trace Report Operations
- **Complete History**: Full lot lifecycle including:
  - Lot details (ID, material, quantity, status, location)
  - All transactions (Receipt, Usage, Adjustment, Transfer, Disposal)
  - Quality control test results
  - Associated batch usages (when component of production batch)
- **Success Path**: Valid lot ID exists
- **Resource Failures**: Lot does not exist
- **Batch References**: Includes batch ID when lot was used as component
- **Traceability**: Supports product traceability (lot → batch → finished product)

### 4. QC Report Operations
- **Date Range Filtering**: Configurable date range (typically last 30 days)
- **Summary Statistics**:
  - Total tests performed
  - Passed test count
  - Failed test count
  - Pending test count
  - Pass rate percentage (passed / total)
- **Aggregation by Material**: Tests grouped by material tested
- **Empty Result Handling**: Pass rate = 0.0 when no tests
- **Use Case**: Quality metrics, trend analysis, compliance reporting

### 5. Inventory Snapshot Operations
- **Status Filter**: Optional filter by lot status (Accepted, Quarantine, Rejected, Depleted)
- **Material Type Filter**: Optional filter by material type (API, Excipient, etc.)
- **Aggregation**: Sums quantities for same material across multiple lots
- **Total Available**: Calculates total available quantity per material
- **No Filter**: Returns complete inventory when filters null/empty
- **Empty Results**: Returns empty list when no lots match criteria
- **Use Case**: Stock level monitoring, procurement planning, inventory valuation

---

## Report Data Specifications

### Dashboard Metrics

| Metric | Description | Calculation |
|--------|-------------|-------------|
| **totalMaterials** | Count of all materials in system | COUNT(material) |
| **totalActiveLots** | Count of non-depleted lots | COUNT(lot WHERE status != Depleted) |
| **nearExpiryLots** | Lots expiring within 30 days | COUNT(lot WHERE expirationDate <= today+30) |
| **activeBatches** | In-progress/non-completed batches | COUNT(batch WHERE status IN [PLANNED, IN_PROGRESS]) |
| **failedQCLast30Days** | QC tests with Failed status | COUNT(qctest WHERE status=Failed AND createdDate >= today-30) |
| **byStatus** | Lot count breakdown | {Accepted: #, Quarantine: #, Rejected: #} |

### QC Report Metrics

| Metric | Description | Calculation |
|--------|-------------|-------------|
| **totalTests** | All tests in date range | COUNT(qctest) |
| **passedTests** | Tests with Pass status | COUNT(qctest WHERE status=Pass) |
| **failedTests** | Tests with Fail status | COUNT(qctest WHERE status=Fail) |
| **pendingTests** | Tests with Pending status | COUNT(qctest WHERE status=Pending) |
| **passRate** | % of tests that passed | (passedTests / totalTests) * 100 |
| **byMaterial** | Test counts grouped | {material1: {passed, failed, pending}, ...} |

### Inventory Snapshot Metrics

| Metric | Description | Calculation |
|--------|-------------|-------------|
| **lotId** | Unique lot identifier | From lot record |
| **materialName** | Material being tracked | From material record |
| **currentStatus** | Lot status | From lot record |
| **totalAvailable** | Sum of quantities | SUM(lot.quantity WHERE material=X) |
| **unitOfMeasure** | Standard UOM | From lot record |

---

## Test Data Patterns

- **Date Ranges**: Last 30 days, last 60 days (relative to current date)
- **Lot Statuses**: Accepted, Quarantine, Rejected, Depleted (enumerated)
- **Material Types**: API, EXCIPIENT, and other defined types
- **Quantities**: 100 kg, 200 kg, 300 kg (aggregate quantities)
- **Material IDs**: "mat1", "mat2" (simple identifiers in tests)
- **Batch IDs**: "batch1", "BATCH-001" (production batch references)

---

## Integration Points

**Inventory Lot Service:**
- Lot data sourced for all reports
- Status transitions tracked in lot lifecycle

**Production Batch Service:**
- Batch data included in lot trace (usage references)
- Batch status affects active batch counts in dashboard

**Quality Control Service:**
- QC test results feed QC Report
- Test dates and status drive pass/fail metrics

**Inventory Transaction Service:**
- Transaction history supports lot trace reporting
- Receipt/usage transactions provide movement records

**Material Service:**
- Material counts and types used in dashboard
- Material groupings in inventory snapshot

---

## Performance Considerations

**Aggregations:**
- Dashboard aggregations computed fresh on each request (or cached)
- Large inventories may require pagination on snapshot results
- Date range filtering optimizes QC report queries

**Filtering:**
- Null/empty filters return complete results
- Status and type filters reduce result sets

---

## Notes

- All test cases assume appropriate access controls validated separately
- Reports are read-only (no data modification)
- Date ranges are inclusive of start and end dates
- Pass rate calculated as (passed / total) * 100, handles zero division
- Near-expiry threshold configurable (tests use 30 and 60 day examples)
- Lot trace includes all related transactions and QC tests for complete history
- Each test verifies appropriate exception types (ResourceNotFoundException)
- Reports reflect real-time system state unless explicitly cached
