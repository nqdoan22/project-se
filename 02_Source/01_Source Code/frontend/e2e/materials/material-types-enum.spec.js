import { test, expect } from '@playwright/test';

/**
 * Test to verify all 7 material type enum values are accepted by the backend
 * Enum values: API, EXCIPIENT, DIETARY_SUPPLEMENT, CONTAINER, CLOSURE, PROCESS_CHEMICAL, TESTING_MATERIAL
 */

const MATERIAL_TYPES = [
  'API',
  'EXCIPIENT',
  'DIETARY_SUPPLEMENT',
  'CONTAINER',
  'CLOSURE',
  'PROCESS_CHEMICAL',
  'TESTING_MATERIAL'
];

const DISPLAY_NAMES = {
  'API': 'API',
  'EXCIPIENT': 'Excipient',
  'DIETARY_SUPPLEMENT': 'Dietary Supplement',
  'CONTAINER': 'Container',
  'CLOSURE': 'Closure',
  'PROCESS_CHEMICAL': 'Process Chemical',
  'TESTING_MATERIAL': 'Testing Material'
};

let createdMaterialIds = [];

async function apiDelete(page, materialId) {
  if (!materialId) return;
  await page.evaluate(async (id) => {
    const { materialApi } = await import('/src/services/api.js');
    await materialApi.delete(id);
  }, materialId);
}

async function createMaterial(page, partNumber, materialType, materialName) {
  const res = await page.evaluate(async (data) => {
    const { materialApi } = await import('/src/services/api.js');
    const res = await materialApi.create(data);
    return res.data;
  }, { partNumber, materialType, materialName });
  return res;
}

test.describe('Material Type Enum Tests', () => {
  test.afterEach(async ({ page }) => {
    // Cleanup: delete all created materials
    for (const id of createdMaterialIds) {
      await apiDelete(page, id);
    }
    createdMaterialIds = [];
  });

  // Test each enum value individually
  MATERIAL_TYPES.forEach((type) => {
    test(`should accept and display material type: ${type} (${DISPLAY_NAMES[type]})`, async ({ page }) => {
      await page.goto('/');
      await expect(page.locator('h1')).toContainText('Vật tư (Materials)');

      // Create material via API
      const partNumber = `TEST-${type}-${Date.now()}`;
      const created = await createMaterial(page, partNumber, type, `Test ${DISPLAY_NAMES[type]}`);
      createdMaterialIds.push(created.materialId);

      expect(created.materialType).toBe(type);

      // Reload page to see the created material
      await page.goto('/materials');
      await page.waitForSelector('table tbody tr');

      // Verify the material appears in the list with correct display name
      const row = page.locator(`tr:has-text("${partNumber}")`);
      await expect(row).toBeVisible();
      await expect(row.locator('.type-badge')).toContainText(DISPLAY_NAMES[type]);
    });
  });

  test('should filter materials by all enum types', async ({ page }) => {
    await page.goto('/');

    // Create one material of each type
    const materials = {};
    for (const type of MATERIAL_TYPES) {
      const partNumber = `FILTER-${type}-${Date.now()}`;
      const created = await createMaterial(page, partNumber, type, `Test ${DISPLAY_NAMES[type]}`);
      materials[type] = created;
      createdMaterialIds.push(created.materialId);
      // Small delay between creations
      await page.waitForTimeout(100);
    }

    // Go to materials page
    await page.goto('/materials');
    await page.waitForSelector('.filter-chips');

    // Test filtering by each type
    for (const type of MATERIAL_TYPES) {
      const displayName = DISPLAY_NAMES[type];
      const filterButton = page.locator(`.filter-chips button:has-text("${displayName}")`);
      await expect(filterButton).toBeVisible();
      await filterButton.click();

      // Wait for filtered results
      await page.waitForTimeout(500);

      // The table should show only materials of this type
      const rows = page.locator('table tbody tr');
      const count = await rows.count();
      
      // Should have at least one row (the material we created for this type)
      expect(count).toBeGreaterThan(0);

      // All visible rows should have the correct type displayed
      const badges = page.locator('.type-badge');
      const badgeCount = await badges.count();
      for (let i = 0; i < badgeCount; i++) {
        const badge = badges.nth(i);
        await expect(badge).toContainText(displayName);
      }
    }
  });

  test('dropdown should display all enum values with correct display names', async ({ page }) => {
    await page.goto('/materials');
    await page.locator('#btn-add-material').click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();

    const dropdown = page.locator('#materialType');
    await expect(dropdown).toBeVisible();

    // Get all options
    const options = await dropdown.locator('option').all();

    // Should have 8 options: 1 empty + 7 enum values
    expect(options.length).toBe(8);

    // Verify each enum value is present
    for (const type of MATERIAL_TYPES) {
      const displayName = DISPLAY_NAMES[type];
      const option = dropdown.locator(`option[value="${type}"]`);
      await expect(option).toHaveText(displayName);
    }
  });
});
