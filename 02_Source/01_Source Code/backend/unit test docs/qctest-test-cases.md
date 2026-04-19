# Unit Test Cases - QC Test Service

## Overview
This document outlines comprehensive unit test cases for the QC Test Service, covering quality control test operations including test creation, result tracking, lot-specific testing, and QC summary reporting. Test cases validate test lifecycle management and QC result aggregation.

---

## Test Cases Table

| Test ID | Test Case Name | Test Values | Tested Function | Auto Testing Functions |
|---|---|---|---|---|
| 76 | **GET TESTS FOR LOT - Success with Multiple Tests** | lotId: valid UUID with 2 tests (Pass, Pending) | `getTestsForLot()` | `getTestsForLot_returnsMappedResponsesOrderedByDate()` |
| 77 | **GET TESTS FOR LOT - No Tests Found** | lotId: valid UUID with no tests | `getTestsForLot()` | `getTestsForLot_noTests_returnsEmptyList()` |
| 78 | **GET SUMMARY - Counts Pass, Fail, Pending** | lotId: valid UUID, tests: 4 (2 Pass, 1 Fail, 1 Pending) | `getSummary()` | `getSummary_correctlyCounts_passFailPendingTests()` |
| 79 | **GET SUMMARY - No Tests Returns All Zeros** | lotId: valid UUID with no tests | `getSummary()` | `getSummary_noTests_returnsAllCountsZero()` |
| 80 | **GET SUMMARY - Lot Not Found** | lotId: non-existent UUID | `getSummary()` | `getSummary_lotNotFound_throwsResourceNotFoundException()` |
| 81 | **GET TEST BY ID - Success** | testId: valid UUID, status: "Pass" | `getTestById()` | `getTestById_found_returnsResponse()` |
| 82 | **GET TEST BY ID - Not Found** | testId: non-existent UUID | `getTestById()` | `getTestById_notFound_throwsResourceNotFoundException()` |
| 83 | **CREATE TEST - Success with All Fields** | lotId: valid UUID, type: "IDENTITY", method: "HPLC-UV", result: "Conforms", status: "Pass", performer: "qc_analyst" | `createTest()` | `createTest_success_savesTestWithProvidedFields()` |
| 84 | **CREATE TEST - Uses Caller Username When PerformedBy Null** | lotId: valid UUID, type: "MICROBIAL", method: "USP <61>", performedBy: null, caller: "jwt_user" | `createTest()` | `createTest_usesCallerUsernameWhenPerformedByIsNull()` |
| 85 | **CREATE TEST - Lot Not Found** | lotId: non-existent UUID, type: "IDENTITY" | `createTest()` | `createTest_lotNotFound_throwsResourceNotFoundException()` |
| 86 | **UPDATE TEST - Same Lot, Updates Status** | testId: valid UUID (Pending on lot1), newType: "POTENCY", newStatus: "Pass", newResult: "100,500 IU/g" | `updateTest()` | `updateTest_sameLot_updatesFieldsWithoutReloadingLot()` |
| 87 | **UPDATE TEST - Different Lot, Reloads New Lot** | testId: valid UUID (lot1), newLotId: lot2 (different lot) | `updateTest()` | `updateTest_differentLot_reloadsNewLot()` |
| 88 | **UPDATE TEST - Test Not Found** | testId: non-existent UUID | `updateTest()` | `updateTest_notFound_throwsResourceNotFoundException()` |

---

## Test Case Categories

### 1. Get Tests for Lot Operations
- **Success Path**: Lot exists with one or more tests
- **No Tests**: Lot exists but has no associated tests
- **Ordering**: Tests returned in date order (earliest to latest)
- **Data Mapping**: Test entity mapped to response DTO

### 2. Get Summary Operations
- **Aggregation**: Counts tests by status (Pass, Fail, Pending)
- **Empty Result Handling**: Returns all zero counts when no tests
- **Resource Validation**: Lot must exist in system
- **Statistics**: Summary includes total test count and status breakdown

### 3. Get Test by ID Operations
- **Success Path**: Valid test ID exists
- **Resource Failures**: Test does not exist
- **Data Retrieval**: Test information includes type, method, result, status

### 4. Create Test Operations
- **Success Path**: Valid lot ID, all required test fields provided
- **Performer Tracking**: Records which analyst performed test
- **Default Performer**: Uses JWT caller username if no performer specified
- **Resource Failures**: Lot does not exist
- **Test Types**: Supports various test types (IDENTITY, MICROBIAL, POTENCY, etc.)
- **Test Methods**: Method/procedure used recorded (HPLC-UV, USP standards, etc.)
- **Results**: Result data recorded (Conforms, quantity, etc.)
- **Status**: Test status set (Pass, Fail, Pending)

### 5. Update Test Operations
- **Success Path**: Valid test ID, updated field values
- **Lot Changes**: When test moved to different lot, new lot reloaded
- **Same Lot Updates**: Status/result/type changes without lot reload
- **Resource Failures**: Test does not exist
- **Field Updates**: Supports updating test type, method, result, status, lot assignment

---

## Business Rules Tested

**Test Creation & Tracking:**
- Tests must be associated with a valid inventory lot
- Each test records the QC analyst/performer who conducted it
- If performer not specified, system defaults to JWT authenticated user
- Test type and method are recorded for traceability

**Test Result Recording:**
- Test can have multiple statuses (Pass, Fail, Pending)
- Result data is recorded (e.g., quantity measured, conformance statement)
- Tests are linked to specific lot identifiers

**Test Summary & Reporting:**
- Summary aggregates all tests for a lot by status
- Pass rate and test counts calculated from summary data
- Supports historical tracking of test progression

**Test Updates:**
- Tests can be reassigned to different lots
- Status updates reflect test progression through completion
- Method/result updates support test reprocessing scenarios

---

## Test Data Patterns

- **Lot IDs**: "lot1", "lot2", "invalid" (for not-found scenarios)
- **Test Types**: IDENTITY, MICROBIAL, POTENCY (enumerated test types)
- **Test Methods**: "HPLC-UV", "USP <61>" (standardized procedure references)
- **Results**: "Conforms", "100,500 IU/g", "Pass" (quantitative/qualitative)
- **Statuses**: Pass, Fail, Pending (test completion states)
- **Performers**: "qc_analyst", "jwt_user" (authenticated user references)

---

## Summary Statistics

**Per-Lot Test Counts:**
- Single test per lot: Treated normally
- Multiple tests per lot: All counted in summary aggregation
- Zero tests: Summary returns all counts = 0

**Test Result Distribution:**
- Example: 4 tests → 2 Pass, 1 Fail, 1 Pending
- Aggregation by status drives pass/fail rates
- Pending tests not counted as pass or fail until resolution

---

## Integration Points

**Inventory Lot Service:**
- Lot must exist when creating test
- Lot service validates lot ID presence

**User Service:**
- Performer defaults to authenticated user (JWT sub)
- User records linked through performer field

**Reporting Service:**
- Test data feeds QC Report aggregations
- Summary statistics used in dashboard reporting

---

## Notes

- All test cases assume request DTOs are structurally valid (Bean Validation annotations respected)
- Tests are immutable once created; updates create new test records or modify in-place based on business rules
- Lot reassignment (changing test.lot) may impact lot-level statistics retroactively
- Performer tracking enables audit trail of QC operations
- Pagination is not explicitly tested but supported on list operations
- Each test verifies appropriate exception types (ResourceNotFoundException, BusinessException)
