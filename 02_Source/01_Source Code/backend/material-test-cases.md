# Unit Test Cases - Material Service

## Overview
This document outlines comprehensive unit test cases for the Material Service in the backend, focusing on CRUD operations and business logic validation. Test cases cover all material management operations including filtering, duplicate detection, and inventory constraints.

---

## Test Cases Table

| Test ID | Test Case Name | Test Values | Tested Function | Auto Testing Functions |
|---|---|---|---|---|
| 47 | **LIST MATERIALS - No Filter** | keyword: null, type: null | `listMaterials()` | `listMaterials_noFilter_callsFindAll()` |
| 48 | **LIST MATERIALS - Filter by Keyword** | keyword: "vitamin", type: null | `listMaterials()` | `listMaterials_keywordOnly_callsFindByName()` |
| 49 | **LIST MATERIALS - Filter by Type** | keyword: null, type: "API" | `listMaterials()` | `listMaterials_typeOnly_callsFindByType()` |
| 50 | **LIST MATERIALS - Filter by Keyword and Type** | keyword: "vit", type: "API" | `listMaterials()` | `listMaterials_keywordAndType_callsCombinedQuery()` |
| 51 | **LIST MATERIALS - Blank Keyword Treated as No Keyword** | keyword: "   " (spaces), type: "API" | `listMaterials()` | `listMaterials_blankKeyword_treatedAsNoKeyword()` |
| 52 | **GET MATERIAL - Success** | materialId: valid UUID (exists) | `getMaterialById()` | `getMaterialById_found_returnsMaterialResponse()` |
| 53 | **GET MATERIAL - Not Found** | materialId: non-existent UUID | `getMaterialById()` | `getMaterialById_notFound_throwsResourceNotFoundException()` |
| 54 | **CREATE MATERIAL - Success** | partNumber: "PN-002", name: "Vitamin D3", type: "API" | `createMaterial()` | `createMaterial_success_savesAndReturnsMaterial()` |
| 55 | **CREATE MATERIAL - Duplicate Part Number** | partNumber: "EXISTING" (already in use) | `createMaterial()` | `createMaterial_duplicatePartNumber_throwsBusinessException()` |
| 56 | **UPDATE MATERIAL - Success** | materialId: valid UUID, newName: "Updated Vitamin D3", newType: "EXCIPIENT" | `updateMaterial()` | `updateMaterial_success_updatesFieldsAndSaves()` |
| 57 | **UPDATE MATERIAL - Not Found** | materialId: non-existent UUID | `updateMaterial()` | `updateMaterial_notFound_throwsResourceNotFoundException()` |
| 58 | **UPDATE MATERIAL - Part Number Taken by Another Material** | materialId: valid UUID, partNumber: "TAKEN" (used by different material) | `updateMaterial()` | `updateMaterial_partNumberTakenByAnother_throwsBusinessException()` |
| 59 | **DELETE MATERIAL - Success** | materialId: valid UUID with no existing inventory lots | `deleteMaterial()` | `deleteMaterial_success_callsDeleteById()` |
| 60 | **DELETE MATERIAL - Not Found** | materialId: non-existent UUID | `deleteMaterial()` | `deleteMaterial_notFound_throwsResourceNotFoundException()` |
| 61 | **DELETE MATERIAL - With Existing Inventory Lots** | materialId: valid UUID with existing lots in system | `deleteMaterial()` | `deleteMaterial_withExistingLots_throwsBusinessException()` |
| 62 | **DELETE MATERIAL - Verification of Cascade Prevention** | materialId: valid UUID, existingLots: true | `deleteMaterial()` | `deleteMaterial_withExistingLots_throwsBusinessException()` |

---

## Test Case Categories

### 1. List Materials Operations
- **No Filter**: Retrieve all materials with pagination
- **Keyword Filter Only**: Find materials by name/keyword, ignoring type
- **Type Filter Only**: Find materials by material type (API, Excipient, etc.)
- **Combined Filters**: Find materials by both keyword and type
- **Input Validation**: Blank keywords treated as no filter

### 2. Get Material Operations
- **Success Path**: Valid material ID exists in system
- **Business Logic Failures**: Material does not exist

### 3. Create Material Operations
- **Success Path**: All required fields provided, part number unique
- **Business Logic Failures**: Duplicate part number already exists
- **Data Persistence**: Material saved with all provided fields

### 4. Update Material Operations
- **Success Path**: Valid material ID, unique new part number
- **Resource Failures**: Material does not exist
- **Business Logic Failures**: New part number already in use by another material
- **Field Updates**: All material fields can be updated (name, type, part number)

### 5. Delete Material Operations
- **Success Path**: Material exists and has no associated inventory lots
- **Resource Failures**: Material does not exist
- **Business Logic Failures**: Material has active inventory lots (cascade prevention)
- **Data Integrity**: Cannot delete material with existing lot references

---

## Business Rules Tested

**Material Creation & Updates:**
- Part numbers must be unique across all materials
- Material types are fixed categories (API, Excipient, etc.)
- Material names can be empty but part numbers are mandatory

**Material Deletion:**
- Cannot delete material that has existing inventory lots
- Prevents orphaned lot references and data integrity issues

**Filtering:**
- Keyword and type filters can be used independently or together
- Blank keywords (whitespace) are treated as null/no filter
- Pagination is applied to all list operations

---

## Test Data Patterns

- **Part Numbers**: "PN-001", "PN-002", "EXISTING", "TAKEN" (used for duplicate detection)
- **Material Names**: "Vitamin D3", "Updated Vitamin D3", generic descriptive names
- **Material Types**: API, EXCIPIENT, and other defined material type enumerations
- **IDs**: UUIDs for materials; non-existent represented as "x" or "missing"

---

## Notes

- All test cases assume request DTOs are structurally valid (Bean Validation annotations respected)
- Business logic focuses on uniqueness constraints, entity relationships, and cascade prevention
- Material filtering uses case-insensitive keyword matching
- Deletion is prevented by checking associated inventory lot count
- Each test verifies appropriate exception types (ResourceNotFoundException, BusinessException) with meaningful error messages
