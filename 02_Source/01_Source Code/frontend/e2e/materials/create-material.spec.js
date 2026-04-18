import { test, expect } from '../fixtures/auth.js';

/**
 * Gọi materialApi.create() trực tiếp qua browser context (không mock).
 * Dev backend không cần token (DevSecurityConfig permitAll).
 */
async function apiCreate(page, body) {
  return page.evaluate(async (data) => {
    const { materialApi } = await import('/src/services/api.js');
    const res = await materialApi.create(data);
    return res.data;
  }, body);
}

/**
 * Gọi materialApi.delete() để dọn dẹp data test sau mỗi case.
 */
async function apiDelete(page, materialId) {
  if (!materialId) return;
  await page.evaluate(async (id) => {
    const { materialApi } = await import('/src/services/api.js');
    await materialApi.delete(id);
  }, materialId);
}

/**
 * Lấy materialId từ API theo partNumber (dùng sau khi tạo qua UI).
 */
async function getMaterialIdByPartNumber(page, partNumber) {
  return page.evaluate(async (pn) => {
    const { materialApi } = await import('/src/services/api.js');
    const res = await materialApi.getAll({ keyword: pn });
    return res.data.content?.[0]?.materialId ?? null;
  }, partNumber);
}

/** Tạo part number unique để tránh conflict giữa các lần chạy */
function uniquePartNumber(prefix = 'MAT-E2E') {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
}

test.describe('Create Material — Real Backend (dev profile)', () => {
  let createdMaterialId = null;

  test.beforeEach(async ({ page }) => {
    createdMaterialId = null;
    await page.goto('/materials');
    await page.waitForSelector('.empty-state, table', { timeout: 15_000 });
  });

  test.afterEach(async ({ page }) => {
    await apiDelete(page, createdMaterialId);
    createdMaterialId = null;
  });

  test('T1 — tạo material với đầy đủ tất cả fields', async ({ page }) => {
    const partNumber = uniquePartNumber();

    await page.locator('#btn-add-material').click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();

    await page.locator('#partNumber').fill(partNumber);
    await page.locator('#materialType').selectOption('EXCIPIENT');
    await page.locator('#materialName').fill('E2E Full Fields Material');
    await page.locator('#storageConditions').fill('Store at 2-8°C');
    await page.locator('#specificationDocument').fill('https://docs.example.com/spec-e2e');

    await page.locator('#submitMaterial').click();

    // Modal phải đóng
    await expect(page.locator('[role="dialog"]')).not.toBeVisible();

    // Thông báo thành công từ backend thật
    await expect(page.locator('.alert-success')).toContainText('Đã thêm vật tư mới');

    // Material xuất hiện trong bảng (data từ DB thật)
    await expect(page.locator('td.td-mono')).toContainText(partNumber);
    await expect(page.locator('td.td-primary')).toContainText('E2E Full Fields Material');

    createdMaterialId = await getMaterialIdByPartNumber(page, partNumber);
  });

  test('T2 — tạo material chỉ với required fields (type mặc định = API)', async ({ page }) => {
    const partNumber = uniquePartNumber('MAT-MIN');

    await page.locator('#btn-add-material').click();

    await page.locator('#partNumber').fill(partNumber);
    await page.locator('#materialName').fill('E2E Minimal Material');
    // materialType mặc định là "API", không chọn gì

    await page.locator('#submitMaterial').click();

    await expect(page.locator('[role="dialog"]')).not.toBeVisible();
    await expect(page.locator('.alert-success')).toContainText('Đã thêm vật tư mới');
    await expect(page.locator('td.td-mono')).toContainText(partNumber);

    // Xác nhận type badge là "API"
    const row = page.locator('tr', { has: page.locator(`td.td-mono:text("${partNumber}")`) });
    await expect(row.locator('.type-badge')).toContainText('API');

    createdMaterialId = await getMaterialIdByPartNumber(page, partNumber);
  });

  test('T3 — validation: required fields trống không thể submit', async ({ page }) => {
    await page.locator('#btn-add-material').click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();

    // Click submit khi tất cả trống — HTML5 required chặn, không gọi API
    await page.locator('#submitMaterial').click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();

    // Fill partNumber, để materialName trống — vẫn bị chặn
    await page.locator('#partNumber').fill('MAT-SHOULD-NOT-SAVE');
    await page.locator('#submitMaterial').click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();

    // Đóng modal — không có gì được lưu xuống DB
    await page.locator('[role="dialog"] .btn-outline').click();
    await expect(page.locator('[role="dialog"]')).not.toBeVisible();
  });

  test('T4 — trùng part number: backend trả 409, hiện lỗi trong modal', async ({ page }) => {
    const partNumber = uniquePartNumber('MAT-DUP');

    // Tạo sẵn 1 material qua API trực tiếp (không qua UI)
    const existing = await apiCreate(page, {
      partNumber,
      materialName: 'Pre-existing Material',
      materialType: 'API',
    });
    createdMaterialId = existing.materialId;

    // Reload để bảng hiển thị material vừa tạo
    await page.reload();
    await page.waitForSelector('table', { timeout: 10_000 });

    // Thử tạo lại qua UI với cùng partNumber
    await page.locator('#btn-add-material').click();
    await page.locator('#partNumber').fill(partNumber);
    await page.locator('#materialName').fill('Duplicate Attempt');
    await page.locator('#submitMaterial').click();

    // Backend trả 409 → lỗi hiện trong modal (real error message từ server)
    await expect(page.locator('[role="dialog"] .alert-error')).toBeVisible({ timeout: 10_000 });

    // Modal vẫn mở để user sửa lại
    await expect(page.locator('[role="dialog"]')).toBeVisible();
  });

  test('T5 — cancel: đóng modal không lưu data', async ({ page }) => {
    const partNumber = uniquePartNumber('MAT-CANCEL');

    await page.locator('#btn-add-material').click();
    await expect(page.locator('[role="dialog"]')).toBeVisible();

    await page.locator('#partNumber').fill(partNumber);
    await page.locator('#materialName').fill('Should Not Be Saved');

    // Click Huỷ
    await page.locator('[role="dialog"] .btn-outline').click();
    await expect(page.locator('[role="dialog"]')).not.toBeVisible();

    // Xác nhận qua API thật: material không tồn tại trong DB
    const found = await page.evaluate(async (pn) => {
      const { materialApi } = await import('/src/services/api.js');
      const res = await materialApi.getAll({ keyword: pn });
      return res.data.content ?? [];
    }, partNumber);
    expect(found).toHaveLength(0);
  });
});
