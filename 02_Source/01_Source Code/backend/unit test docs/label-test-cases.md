# Unit Test Cases - Label Service

## Overview
This document outlines comprehensive unit test cases for the Label Service, covering label template management and dynamic label generation. Test cases validate template CRUD operations, placeholder rendering, and label generation from inventory and production data.

---

## Test Cases Table

| Test ID | Test Case Name | Test Values | Tested Function | Auto Testing Functions |
|---|---|---|---|---|
| 101 | **LIST LABEL TEMPLATES - No Filter** | filter: null | `listTemplates()` | `listTemplates_noFilter_returnsAllTemplates()` |
| 102 | **LIST LABEL TEMPLATES - Filter by Type** | type: "FINISHED_PRODUCT" | `listTemplates()` | `listTemplates_byType_returnTemplatesOfType()` |
| 103 | **LIST LABEL TEMPLATES - Filter by Type Empty** | type: "RAW_MATERIAL" (no templates) | `listTemplates()` | `listTemplates_byTypeEmpty_returnsEmptyList()` |
| 104 | **GET LABEL TEMPLATE BY ID - Success** | templateId: valid UUID, name: "Standard Label" | `getTemplateById()` | `getTemplateById_found_returnsTemplate()` |
| 105 | **GET LABEL TEMPLATE BY ID - Not Found** | templateId: non-existent UUID | `getTemplateById()` | `getTemplateById_notFound_throwsResourceNotFoundException()` |
| 106 | **CREATE LABEL TEMPLATE - Success** | name: "Standard Label", type: "FINISHED_PRODUCT", width: 100, height: 50 | `createTemplate()` | `createTemplate_success_savesAndReturnsTemplate()` |
| 107 | **CREATE LABEL TEMPLATE - Duplicate Template ID** | templateId: "EXISTING" (already in use) | `createTemplate()` | `createTemplate_duplicateId_throwsConflictException()` |
| 108 | **UPDATE LABEL TEMPLATE - Success** | templateId: valid UUID, newName: "Updated Label" | `updateTemplate()` | `updateTemplate_success_updatesAndSaves()` |
| 109 | **UPDATE LABEL TEMPLATE - Not Found** | templateId: non-existent UUID | `updateTemplate()` | `updateTemplate_notFound_throwsResourceNotFoundException()` |
| 110 | **DELETE LABEL TEMPLATE - Success** | templateId: valid UUID | `deleteTemplate()` | `deleteTemplate_success_deletesTemplate()` |
| 111 | **DELETE LABEL TEMPLATE - Not Found** | templateId: non-existent UUID | `deleteTemplate()` | `deleteTemplate_notFound_throwsResourceNotFoundException()` |
| 112 | **GENERATE LABEL FROM LOT - Replace Placeholders** | templateId: valid UUID (content: "Lot: {{lotId}}, Material: {{materialName}}, Exp: {{expirationDate}}"), source: LOT "lot1" | `generateLabel()` | `generateLabel_fromLot_replacesPlaceholders()` |
| 113 | **GENERATE LABEL FROM BATCH - Replace Placeholders** | templateId: valid UUID (content: "Batch: {{batchNumber}}, Product: {{productName}}, Size: {{batchSize}}"), source: BATCH "batch1" | `generateLabel()` | `generateLabel_fromBatch_replacesPlaceholders()` |
| 114 | **GENERATE LABEL FROM LOT - Lot Not Found** | templateId: valid UUID, source: LOT "invalid" | `generateLabel()` | `generateLabel_lotNotFound_throwsResourceNotFoundException()` |
| 115 | **GENERATE LABEL FROM BATCH - Batch Not Found** | templateId: valid UUID, source: BATCH "invalid" | `generateLabel()` | `generateLabel_batchNotFound_throwsResourceNotFoundException()` |
| 116 | **GENERATE LABEL - Template Not Found** | templateId: non-existent UUID | `generateLabel()` | `generateLabel_templateNotFound_throwsResourceNotFoundException()` |
| 117 | **GENERATE LABEL - Unsupported Source Type** | templateId: valid UUID, sourceType: null (invalid) | `generateLabel()` | `generateLabel_unsupportedSourceType_throwsBusinessException()` |
| 118 | **GENERATE LABEL - Null Values Handled Gracefully** | source: LOT with null storage location | `generateLabel()` | `generateLabel_withNullValues_handlesGracefully()` |

---

## Test Case Categories

### 1. List Label Templates Operations
- **No Filter**: Retrieve all label templates in system
- **Type Filter**: Filter templates by label type (FINISHED_PRODUCT, RAW_MATERIAL, etc.)
- **Empty Results**: Returns empty list when no templates match filter
- **Pagination**: Applied to all list operations

### 2. Get Label Template by ID Operations
- **Success Path**: Valid template ID exists
- **Resource Failures**: Template does not exist
- **Data Retrieval**: Template includes name, type, dimensions, content

### 3. Create Label Template Operations
- **Success Path**: Unique template ID, all required fields provided
- **Template Fields**: Name, type, width, height, content (markup with placeholders)
- **Uniqueness Constraint**: Template ID must be unique
- **Business Logic Failures**: Template ID already exists (ConflictException)
- **Data Persistence**: Template saved with all fields

### 4. Update Label Template Operations
- **Success Path**: Valid template ID exists, updated field values
- **Field Updates**: Supports updating name, type, dimensions, content
- **Resource Failures**: Template does not exist
- **Data Persistence**: Changes saved to database

### 5. Delete Label Template Operations
- **Success Path**: Valid template ID exists
- **Resource Failures**: Template does not exist
- **Data Cleanup**: Template removed from system

### 6. Generate Label Operations
- **Placeholder Substitution**: Template content contains {{placeholder}} markers
- **Lot Source**: Generate label from inventory lot data
  - Available placeholders: {{lotId}}, {{materialName}}, {{expirationDate}}, {{storageLocation}}, etc.
- **Batch Source**: Generate label from production batch data
  - Available placeholders: {{batchNumber}}, {{productName}}, {{batchSize}}, {{mfgDate}}, {{expDate}}, etc.
- **Null Handling**: Gracefully handles missing/null data fields
- **Resource Failures**: 
  - Template does not exist
  - Lot does not exist (when source is LOT)
  - Batch does not exist (when source is BATCH)
- **Business Logic Failures**: Unsupported/null source type
- **Output**: Rendered label content as string with all placeholders replaced

---

## Business Rules Tested

**Template Management:**
- Template IDs must be unique (business key)
- Templates store HTML/markup content with placeholder markers
- Template types categorize labels by product category
- Template dimensions define physical label size

**Label Generation:**
- Placeholders in template content replaced with actual data values
- Data sourced from either inventory lots or production batches
- All referenced resources (template, lot, batch) must exist
- Null/missing field values do not break rendering (handled gracefully)

**Supported Placeholder Variables:**

| Placeholder | Data Source | Description |
|---|---|---|
| {{lotId}} | Lot | Unique inventory lot identifier |
| {{materialName}} | Lot.Material | Name of material in lot |
| {{expirationDate}} | Lot | Lot expiration date |
| {{storageLocation}} | Lot | Current storage location |
| {{batchNumber}} | Batch | Production batch number |
| {{productName}} | Batch.Product | Product/material name |
| {{batchSize}} | Batch | Batch size quantity |
| {{mfgDate}} | Batch | Manufacturing date |
| {{expDate}} | Batch | Expiration date |

---

## Test Data Patterns

- **Template IDs**: "t1", "EXISTING" (for conflict testing), "invalid" (for not-found)
- **Template Names**: "Standard Label", "Updated Label", descriptive names
- **Template Types**: FINISHED_PRODUCT, RAW_MATERIAL, INTERMEDIATE (enumerated)
- **Dimensions**: Width: 100, Height: 50 (in millimeters or arbitrary units)
- **Content**: HTML/markup with {{placeholder}} markers
- **Lot IDs**: "lot1", "invalid" (for not-found testing)
- **Batch IDs**: "batch1", "invalid" (for not-found testing)

---

## Template Content Examples

**Lot Label Template:**
```
Lot ID: {{lotId}}
Material: {{materialName}}
Expiration: {{expirationDate}}
Storage: {{storageLocation}}
```

**Batch Label Template:**
```
Batch Number: {{batchNumber}}
Product: {{productName}}
Batch Size: {{batchSize}} kg
Manufactured: {{mfgDate}}
Expires: {{expDate}}
```

---

## Integration Points

**Inventory Lot Service:**
- Lot data sourced when generating labels from lots
- Lot fields (ID, material, expiration, location) used for placeholders

**Production Batch Service:**
- Batch data sourced when generating labels from batches
- Batch fields (number, product, size, dates) used for placeholders

**Material/Product Service:**
- Material names retrieved and used in label rendering
- Product names retrieved and used in label rendering

---

## Error Handling

**Resource Not Found Exceptions:**
- Template does not exist
- Lot does not exist (when sourcing from lot)
- Batch does not exist (when sourcing from batch)

**Business Exceptions:**
- Invalid/null source type provided
- Template content markup errors (if validated)

**Graceful Degradation:**
- Null field values in lot/batch: Rendered as empty string or placeholder unchanged
- Missing optional fields: Omitted from output without error

---

## Notes

- All test cases assume request DTOs are structurally valid (Bean Validation annotations respected)
- Template content is stored as-is (HTML/markup) without validation
- Placeholder markers must follow {{name}} format (case-sensitive)
- Label generation is stateless and repeatable (same template + data = same output)
- Template deletion does not cascade (templates can be deleted independently)
- Each test verifies appropriate exception types (ResourceNotFoundException, ConflictException, BusinessException)
