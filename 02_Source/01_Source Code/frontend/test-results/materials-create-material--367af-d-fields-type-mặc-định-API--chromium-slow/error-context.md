# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: materials/create-material.spec.js >> Create Material — Real Backend (dev profile) >> T2 — tạo material chỉ với required fields (type mặc định = API)
- Location: e2e/materials/create-material.spec.js:83:3

# Error details

```
Error: expect(locator).toContainText(expected) failed

Locator: locator('td.td-mono')
Expected substring: "MAT-MIN-1775206694951-975"
Error: strict mode violation: locator('td.td-mono') resolved to 20 elements:
    1) <td class="td-mono">MAT-001</td> aka getByRole('cell', { name: 'MAT-001' })
    2) <td class="td-mono">MAT-002</td> aka getByRole('cell', { name: 'MAT-002' })
    3) <td class="td-mono">MAT-003</td> aka getByRole('cell', { name: 'MAT-003' })
    4) <td class="td-mono">MAT-004</td> aka getByRole('cell', { name: 'MAT-004' })
    5) <td class="td-mono">MAT-005</td> aka getByRole('cell', { name: 'MAT-005' })
    6) <td class="td-mono">MAT-006</td> aka getByRole('cell', { name: 'MAT-006' })
    7) <td class="td-mono">MAT-007</td> aka getByRole('cell', { name: 'MAT-007' })
    8) <td class="td-mono">MAT-008</td> aka getByRole('cell', { name: 'MAT-008' })
    9) <td class="td-mono">MAT-009</td> aka getByRole('cell', { name: 'MAT-009' })
    10) <td class="td-mono">MAT-010</td> aka getByRole('cell', { name: 'MAT-010' })
    ...

Call log:
  - Expect "toContainText" with timeout 5000ms
  - waiting for locator('td.td-mono')

```

# Page snapshot

```yaml
- generic [ref=e3]:
  - complementary [ref=e4]:
    - generic [ref=e5]:
      - heading "IMS" [level=2] [ref=e6]
      - text: Inventory System
    - navigation [ref=e7]:
      - generic [ref=e8]:
        - generic [ref=e9]: Material Management
        - link "🧪 Vật tư (Materials)" [ref=e10] [cursor=pointer]:
          - /url: /materials
          - generic [ref=e11]: 🧪
          - text: Vật tư (Materials)
        - link "📦 Tồn kho (Lots)" [ref=e12] [cursor=pointer]:
          - /url: /lots
          - generic [ref=e13]: 📦
          - text: Tồn kho (Lots)
        - link "📋 Giao dịch (Transactions)" [ref=e14] [cursor=pointer]:
          - /url: /transactions
          - generic [ref=e15]: 📋
          - text: Giao dịch (Transactions)
      - generic [ref=e16]:
        - generic [ref=e17]: Quality Control
        - link "🔬 Kiểm nghiệm (QC Tests)" [ref=e18] [cursor=pointer]:
          - /url: /qctests
          - generic [ref=e19]: 🔬
          - text: Kiểm nghiệm (QC Tests)
      - generic [ref=e20]:
        - generic [ref=e21]: Production
        - link "⚗️ Lô sản xuất (Batches)" [ref=e22] [cursor=pointer]:
          - /url: /batches
          - generic [ref=e23]: ⚗️
          - text: Lô sản xuất (Batches)
        - link "📦 Nguyên liệu (Components)" [ref=e24] [cursor=pointer]:
          - /url: /components
          - generic [ref=e25]: 📦
          - text: Nguyên liệu (Components)
      - generic [ref=e26]:
        - generic [ref=e27]: Labels & Reports
        - link "🏷️ Nhãn (Labels)" [ref=e28] [cursor=pointer]:
          - /url: /labels
          - generic [ref=e29]: 🏷️
          - text: Nhãn (Labels)
        - link "📊 Báo cáo (Reports)" [ref=e30] [cursor=pointer]:
          - /url: /dashboard
          - generic [ref=e31]: 📊
          - text: Báo cáo (Reports)
      - generic [ref=e32]:
        - generic [ref=e33]: Administration
        - link "👥 Người dùng (Users)" [ref=e34] [cursor=pointer]:
          - /url: /users
          - generic [ref=e35]: 👥
          - text: Người dùng (Users)
    - generic [ref=e37]: Not connected
  - main [ref=e38]:
    - generic [ref=e39]:
      - generic [ref=e40]:
        - heading "🧪 Vật tư (Materials)" [level=1] [ref=e41]
        - paragraph [ref=e42]: Quản lý danh mục nguyên vật liệu và sản phẩm
      - button "＋ Thêm vật tư" [ref=e43] [cursor=pointer]
    - generic [ref=e45]:
      - generic [ref=e46]:
        - textbox "🔍 Tìm theo tên vật tư..." [ref=e47]
        - generic [ref=e48]:
          - button "Tất cả" [ref=e49] [cursor=pointer]
          - button "API" [ref=e50] [cursor=pointer]
          - button "Excipient" [ref=e51] [cursor=pointer]
          - button "Packaging" [ref=e52] [cursor=pointer]
          - button "Product" [ref=e53] [cursor=pointer]
          - button "Other" [ref=e54] [cursor=pointer]
      - table [ref=e56]:
        - rowgroup [ref=e57]:
          - row "Part Number Tên vật tư Loại Điều kiện bảo quản Ngày tạo Hành động" [ref=e58]:
            - columnheader "Part Number" [ref=e59]
            - columnheader "Tên vật tư" [ref=e60]
            - columnheader "Loại" [ref=e61]
            - columnheader "Điều kiện bảo quản" [ref=e62]
            - columnheader "Ngày tạo" [ref=e63]
            - columnheader "Hành động" [ref=e64]
        - rowgroup [ref=e65]:
          - row "MAT-001 Vitamin D3 API Store 2–8°C, protect from light 5/1/2026 ✏ Sửa 🗑" [ref=e66]:
            - cell "MAT-001" [ref=e67]
            - cell "Vitamin D3" [ref=e68]
            - cell "API" [ref=e69]
            - cell "Store 2–8°C, protect from light" [ref=e70]
            - cell "5/1/2026" [ref=e71]
            - cell "✏ Sửa 🗑" [ref=e72]:
              - generic [ref=e73]:
                - button "✏ Sửa" [ref=e74] [cursor=pointer]
                - button "🗑" [ref=e75] [cursor=pointer]
          - row "MAT-002 Microcrystalline Cellulose PH102 EXCIPIENT Store below 30°C, dry conditions 5/1/2026 ✏ Sửa 🗑" [ref=e76]:
            - cell "MAT-002" [ref=e77]
            - cell "Microcrystalline Cellulose PH102" [ref=e78]
            - cell "EXCIPIENT" [ref=e79]
            - cell "Store below 30°C, dry conditions" [ref=e80]
            - cell "5/1/2026" [ref=e81]
            - cell "✏ Sửa 🗑" [ref=e82]:
              - generic [ref=e83]:
                - button "✏ Sửa" [ref=e84] [cursor=pointer]
                - button "🗑" [ref=e85] [cursor=pointer]
          - row "MAT-003 Ascorbic Acid USP Grade DIETARY_SUPPLEMENT Store below 25°C, avoid moisture 5/1/2026 ✏ Sửa 🗑" [ref=e86]:
            - cell "MAT-003" [ref=e87]
            - cell "Ascorbic Acid USP Grade" [ref=e88]
            - cell "DIETARY_SUPPLEMENT" [ref=e89]
            - cell "Store below 25°C, avoid moisture" [ref=e90]
            - cell "5/1/2026" [ref=e91]
            - cell "✏ Sửa 🗑" [ref=e92]:
              - generic [ref=e93]:
                - button "✏ Sửa" [ref=e94] [cursor=pointer]
                - button "🗑" [ref=e95] [cursor=pointer]
          - row "MAT-004 HDPE Bottle 120mL White CONTAINER Ambient, dry storage 5/1/2026 ✏ Sửa 🗑" [ref=e96]:
            - cell "MAT-004" [ref=e97]
            - cell "HDPE Bottle 120mL White" [ref=e98]
            - cell "CONTAINER" [ref=e99]
            - cell "Ambient, dry storage" [ref=e100]
            - cell "5/1/2026" [ref=e101]
            - cell "✏ Sửa 🗑" [ref=e102]:
              - generic [ref=e103]:
                - button "✏ Sửa" [ref=e104] [cursor=pointer]
                - button "🗑" [ref=e105] [cursor=pointer]
          - row "MAT-005 Child-Resistant Cap 38mm White CLOSURE Ambient, dry storage 5/1/2026 ✏ Sửa 🗑" [ref=e106]:
            - cell "MAT-005" [ref=e107]
            - cell "Child-Resistant Cap 38mm White" [ref=e108]
            - cell "CLOSURE" [ref=e109]
            - cell "Ambient, dry storage" [ref=e110]
            - cell "5/1/2026" [ref=e111]
            - cell "✏ Sửa 🗑" [ref=e112]:
              - generic [ref=e113]:
                - button "✏ Sửa" [ref=e114] [cursor=pointer]
                - button "🗑" [ref=e115] [cursor=pointer]
          - row "MAT-006 Purified Water WFI Grade PROCESS_CHEMICAL Freshly prepared, <24h 5/1/2026 ✏ Sửa 🗑" [ref=e116]:
            - cell "MAT-006" [ref=e117]
            - cell "Purified Water WFI Grade" [ref=e118]
            - cell "PROCESS_CHEMICAL" [ref=e119]
            - cell "Freshly prepared, <24h" [ref=e120]
            - cell "5/1/2026" [ref=e121]
            - cell "✏ Sửa 🗑" [ref=e122]:
              - generic [ref=e123]:
                - button "✏ Sửa" [ref=e124] [cursor=pointer]
                - button "🗑" [ref=e125] [cursor=pointer]
          - row "MAT-007 Endotoxin Reference Standard BET TESTING_MATERIAL Store at -20°C 5/1/2026 ✏ Sửa 🗑" [ref=e126]:
            - cell "MAT-007" [ref=e127]
            - cell "Endotoxin Reference Standard BET" [ref=e128]
            - cell "TESTING_MATERIAL" [ref=e129]
            - cell "Store at -20°C" [ref=e130]
            - cell "5/1/2026" [ref=e131]
            - cell "✏ Sửa 🗑" [ref=e132]:
              - generic [ref=e133]:
                - button "✏ Sửa" [ref=e134] [cursor=pointer]
                - button "🗑" [ref=e135] [cursor=pointer]
          - row "MAT-008 Lactose Monohydrate SuperTab 30SD EXCIPIENT Store below 30°C, RH < 65% 5/1/2026 ✏ Sửa 🗑" [ref=e136]:
            - cell "MAT-008" [ref=e137]
            - cell "Lactose Monohydrate SuperTab 30SD" [ref=e138]
            - cell "EXCIPIENT" [ref=e139]
            - cell "Store below 30°C, RH < 65%" [ref=e140]
            - cell "5/1/2026" [ref=e141]
            - cell "✏ Sửa 🗑" [ref=e142]:
              - generic [ref=e143]:
                - button "✏ Sửa" [ref=e144] [cursor=pointer]
                - button "🗑" [ref=e145] [cursor=pointer]
          - row "MAT-009 Magnesium Stearate NF EXCIPIENT Store below 25°C 5/1/2026 ✏ Sửa 🗑" [ref=e146]:
            - cell "MAT-009" [ref=e147]
            - cell "Magnesium Stearate NF" [ref=e148]
            - cell "EXCIPIENT" [ref=e149]
            - cell "Store below 25°C" [ref=e150]
            - cell "5/1/2026" [ref=e151]
            - cell "✏ Sửa 🗑" [ref=e152]:
              - generic [ref=e153]:
                - button "✏ Sửa" [ref=e154] [cursor=pointer]
                - button "🗑" [ref=e155] [cursor=pointer]
          - row "MAT-010 IMS Vitamin D3 Tablet 1000IU DIETARY_SUPPLEMENT Store below 25°C, protect from light 5/1/2026 ✏ Sửa 🗑" [ref=e156]:
            - cell "MAT-010" [ref=e157]
            - cell "IMS Vitamin D3 Tablet 1000IU" [ref=e158]
            - cell "DIETARY_SUPPLEMENT" [ref=e159]
            - cell "Store below 25°C, protect from light" [ref=e160]
            - cell "5/1/2026" [ref=e161]
            - cell "✏ Sửa 🗑" [ref=e162]:
              - generic [ref=e163]:
                - button "✏ Sửa" [ref=e164] [cursor=pointer]
                - button "🗑" [ref=e165] [cursor=pointer]
          - row "a a API a 13/3/2026 ✏ Sửa 🗑" [ref=e166]:
            - cell "a" [ref=e167]
            - cell "a" [ref=e168]
            - cell "API" [ref=e169]
            - cell "a" [ref=e170]
            - cell "13/3/2026" [ref=e171]
            - cell "✏ Sửa 🗑" [ref=e172]:
              - generic [ref=e173]:
                - button "✏ Sửa" [ref=e174] [cursor=pointer]
                - button "🗑" [ref=e175] [cursor=pointer]
          - row "brick brick a API sang 13/3/2026 ✏ Sửa 🗑" [ref=e176]:
            - cell "brick" [ref=e177]
            - cell "brick a" [ref=e178]
            - cell "API" [ref=e179]
            - cell "sang" [ref=e180]
            - cell "13/3/2026" [ref=e181]
            - cell "✏ Sửa 🗑" [ref=e182]:
              - generic [ref=e183]:
                - button "✏ Sửa" [ref=e184] [cursor=pointer]
                - button "🗑" [ref=e185] [cursor=pointer]
          - row "MAT-011 Thuốc M011 API 10 độ C 13/3/2026 ✏ Sửa 🗑" [ref=e186]:
            - cell "MAT-011" [ref=e187]
            - cell "Thuốc M011" [ref=e188]
            - cell "API" [ref=e189]
            - cell "10 độ C" [ref=e190]
            - cell "13/3/2026" [ref=e191]
            - cell "✏ Sửa 🗑" [ref=e192]:
              - generic [ref=e193]:
                - button "✏ Sửa" [ref=e194] [cursor=pointer]
                - button "🗑" [ref=e195] [cursor=pointer]
          - row "mat-100 Thuốc M100 API 10 độc 13/3/2026 ✏ Sửa 🗑" [ref=e196]:
            - cell "mat-100" [ref=e197]
            - cell "Thuốc M100" [ref=e198]
            - cell "API" [ref=e199]
            - cell "10 độc" [ref=e200]
            - cell "13/3/2026" [ref=e201]
            - cell "✏ Sửa 🗑" [ref=e202]:
              - generic [ref=e203]:
                - button "✏ Sửa" [ref=e204] [cursor=pointer]
                - button "🗑" [ref=e205] [cursor=pointer]
          - row "EXP1 Example API API Storage conditions example 13/3/2026 ✏ Sửa 🗑" [ref=e206]:
            - cell "EXP1" [ref=e207]
            - cell "Example API" [ref=e208]
            - cell "API" [ref=e209]
            - cell "Storage conditions example" [ref=e210]
            - cell "13/3/2026" [ref=e211]
            - cell "✏ Sửa 🗑" [ref=e212]:
              - generic [ref=e213]:
                - button "✏ Sửa" [ref=e214] [cursor=pointer]
                - button "🗑" [ref=e215] [cursor=pointer]
          - row "MAT-MIN-1775203331784-338 E2E Minimal Material API — 3/4/2026 ✏ Sửa 🗑" [ref=e216]:
            - cell "MAT-MIN-1775203331784-338" [ref=e217]
            - cell "E2E Minimal Material" [ref=e218]
            - cell "API" [ref=e219]
            - cell "—" [ref=e220]
            - cell "3/4/2026" [ref=e221]
            - cell "✏ Sửa 🗑" [ref=e222]:
              - generic [ref=e223]:
                - button "✏ Sửa" [ref=e224] [cursor=pointer]
                - button "🗑" [ref=e225] [cursor=pointer]
          - row "MAT-MIN-1775203337631-495 E2E Minimal Material API — 3/4/2026 ✏ Sửa 🗑" [ref=e226]:
            - cell "MAT-MIN-1775203337631-495" [ref=e227]
            - cell "E2E Minimal Material" [ref=e228]
            - cell "API" [ref=e229]
            - cell "—" [ref=e230]
            - cell "3/4/2026" [ref=e231]
            - cell "✏ Sửa 🗑" [ref=e232]:
              - generic [ref=e233]:
                - button "✏ Sửa" [ref=e234] [cursor=pointer]
                - button "🗑" [ref=e235] [cursor=pointer]
          - row "MAT-MIN-1775203417948-267 E2E Minimal Material API — 3/4/2026 ✏ Sửa 🗑" [ref=e236]:
            - cell "MAT-MIN-1775203417948-267" [ref=e237]
            - cell "E2E Minimal Material" [ref=e238]
            - cell "API" [ref=e239]
            - cell "—" [ref=e240]
            - cell "3/4/2026" [ref=e241]
            - cell "✏ Sửa 🗑" [ref=e242]:
              - generic [ref=e243]:
                - button "✏ Sửa" [ref=e244] [cursor=pointer]
                - button "🗑" [ref=e245] [cursor=pointer]
          - row "MAT-MIN-1775203500491-967 E2E Minimal Material API — 3/4/2026 ✏ Sửa 🗑" [ref=e246]:
            - cell "MAT-MIN-1775203500491-967" [ref=e247]
            - cell "E2E Minimal Material" [ref=e248]
            - cell "API" [ref=e249]
            - cell "—" [ref=e250]
            - cell "3/4/2026" [ref=e251]
            - cell "✏ Sửa 🗑" [ref=e252]:
              - generic [ref=e253]:
                - button "✏ Sửa" [ref=e254] [cursor=pointer]
                - button "🗑" [ref=e255] [cursor=pointer]
          - row "MAT-MIN-1775203948512-663 E2E Minimal Material API — 3/4/2026 ✏ Sửa 🗑" [ref=e256]:
            - cell "MAT-MIN-1775203948512-663" [ref=e257]
            - cell "E2E Minimal Material" [ref=e258]
            - cell "API" [ref=e259]
            - cell "—" [ref=e260]
            - cell "3/4/2026" [ref=e261]
            - cell "✏ Sửa 🗑" [ref=e262]:
              - generic [ref=e263]:
                - button "✏ Sửa" [ref=e264] [cursor=pointer]
                - button "🗑" [ref=e265] [cursor=pointer]
```

# Test source

```ts
  1   | import { test, expect } from '../fixtures/auth.js';
  2   | 
  3   | /**
  4   |  * Gọi materialApi.create() trực tiếp qua browser context (không mock).
  5   |  * Dev backend không cần token (DevSecurityConfig permitAll).
  6   |  */
  7   | async function apiCreate(page, body) {
  8   |   return page.evaluate(async (data) => {
  9   |     const { materialApi } = await import('/src/services/api.js');
  10  |     const res = await materialApi.create(data);
  11  |     return res.data;
  12  |   }, body);
  13  | }
  14  | 
  15  | /**
  16  |  * Gọi materialApi.delete() để dọn dẹp data test sau mỗi case.
  17  |  */
  18  | async function apiDelete(page, materialId) {
  19  |   if (!materialId) return;
  20  |   await page.evaluate(async (id) => {
  21  |     const { materialApi } = await import('/src/services/api.js');
  22  |     await materialApi.delete(id);
  23  |   }, materialId);
  24  | }
  25  | 
  26  | /**
  27  |  * Lấy materialId từ API theo partNumber (dùng sau khi tạo qua UI).
  28  |  */
  29  | async function getMaterialIdByPartNumber(page, partNumber) {
  30  |   return page.evaluate(async (pn) => {
  31  |     const { materialApi } = await import('/src/services/api.js');
  32  |     const res = await materialApi.getAll({ keyword: pn });
  33  |     return res.data.content?.[0]?.materialId ?? null;
  34  |   }, partNumber);
  35  | }
  36  | 
  37  | /** Tạo part number unique để tránh conflict giữa các lần chạy */
  38  | function uniquePartNumber(prefix = 'MAT-E2E') {
  39  |   return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
  40  | }
  41  | 
  42  | test.describe('Create Material — Real Backend (dev profile)', () => {
  43  |   let createdMaterialId = null;
  44  | 
  45  |   test.beforeEach(async ({ page }) => {
  46  |     createdMaterialId = null;
  47  |     await page.goto('/materials');
  48  |     await page.waitForSelector('.empty-state, table', { timeout: 15_000 });
  49  |   });
  50  | 
  51  |   test.afterEach(async ({ page }) => {
  52  |     await apiDelete(page, createdMaterialId);
  53  |     createdMaterialId = null;
  54  |   });
  55  | 
  56  |   test('T1 — tạo material với đầy đủ tất cả fields', async ({ page }) => {
  57  |     const partNumber = uniquePartNumber();
  58  | 
  59  |     await page.locator('#btn-add-material').click();
  60  |     await expect(page.locator('[role="dialog"]')).toBeVisible();
  61  | 
  62  |     await page.locator('#partNumber').fill(partNumber);
  63  |     await page.locator('#materialType').selectOption('Excipient');
  64  |     await page.locator('#materialName').fill('E2E Full Fields Material');
  65  |     await page.locator('#storageConditions').fill('Store at 2-8°C');
  66  |     await page.locator('#specificationDocument').fill('https://docs.example.com/spec-e2e');
  67  | 
  68  |     await page.locator('#submitMaterial').click();
  69  | 
  70  |     // Modal phải đóng
  71  |     await expect(page.locator('[role="dialog"]')).not.toBeVisible();
  72  | 
  73  |     // Thông báo thành công từ backend thật
  74  |     await expect(page.locator('.alert-success')).toContainText('Đã thêm vật tư mới');
  75  | 
  76  |     // Material xuất hiện trong bảng (data từ DB thật)
  77  |     await expect(page.locator('td.td-mono')).toContainText(partNumber);
  78  |     await expect(page.locator('td.td-primary')).toContainText('E2E Full Fields Material');
  79  | 
  80  |     createdMaterialId = await getMaterialIdByPartNumber(page, partNumber);
  81  |   });
  82  | 
  83  |   test('T2 — tạo material chỉ với required fields (type mặc định = API)', async ({ page }) => {
  84  |     const partNumber = uniquePartNumber('MAT-MIN');
  85  | 
  86  |     await page.locator('#btn-add-material').click();
  87  | 
  88  |     await page.locator('#partNumber').fill(partNumber);
  89  |     await page.locator('#materialName').fill('E2E Minimal Material');
  90  |     // materialType mặc định là "API", không chọn gì
  91  | 
  92  |     await page.locator('#submitMaterial').click();
  93  | 
  94  |     await expect(page.locator('[role="dialog"]')).not.toBeVisible();
  95  |     await expect(page.locator('.alert-success')).toContainText('Đã thêm vật tư mới');
> 96  |     await expect(page.locator('td.td-mono')).toContainText(partNumber);
      |                                              ^ Error: expect(locator).toContainText(expected) failed
  97  | 
  98  |     // Xác nhận type badge là "API"
  99  |     const row = page.locator('tr', { has: page.locator(`td.td-mono:text("${partNumber}")`) });
  100 |     await expect(row.locator('.type-badge')).toContainText('API');
  101 | 
  102 |     createdMaterialId = await getMaterialIdByPartNumber(page, partNumber);
  103 |   });
  104 | 
  105 |   test('T3 — validation: required fields trống không thể submit', async ({ page }) => {
  106 |     await page.locator('#btn-add-material').click();
  107 |     await expect(page.locator('[role="dialog"]')).toBeVisible();
  108 | 
  109 |     // Click submit khi tất cả trống — HTML5 required chặn, không gọi API
  110 |     await page.locator('#submitMaterial').click();
  111 |     await expect(page.locator('[role="dialog"]')).toBeVisible();
  112 | 
  113 |     // Fill partNumber, để materialName trống — vẫn bị chặn
  114 |     await page.locator('#partNumber').fill('MAT-SHOULD-NOT-SAVE');
  115 |     await page.locator('#submitMaterial').click();
  116 |     await expect(page.locator('[role="dialog"]')).toBeVisible();
  117 | 
  118 |     // Đóng modal — không có gì được lưu xuống DB
  119 |     await page.locator('[role="dialog"] .btn-outline').click();
  120 |     await expect(page.locator('[role="dialog"]')).not.toBeVisible();
  121 |   });
  122 | 
  123 |   test('T4 — trùng part number: backend trả 409, hiện lỗi trong modal', async ({ page }) => {
  124 |     const partNumber = uniquePartNumber('MAT-DUP');
  125 | 
  126 |     // Tạo sẵn 1 material qua API trực tiếp (không qua UI)
  127 |     const existing = await apiCreate(page, {
  128 |       partNumber,
  129 |       materialName: 'Pre-existing Material',
  130 |       materialType: 'API',
  131 |     });
  132 |     createdMaterialId = existing.materialId;
  133 | 
  134 |     // Reload để bảng hiển thị material vừa tạo
  135 |     await page.reload();
  136 |     await page.waitForSelector('table', { timeout: 10_000 });
  137 | 
  138 |     // Thử tạo lại qua UI với cùng partNumber
  139 |     await page.locator('#btn-add-material').click();
  140 |     await page.locator('#partNumber').fill(partNumber);
  141 |     await page.locator('#materialName').fill('Duplicate Attempt');
  142 |     await page.locator('#submitMaterial').click();
  143 | 
  144 |     // Backend trả 409 → lỗi hiện trong modal (real error message từ server)
  145 |     await expect(page.locator('[role="dialog"] .alert-error')).toBeVisible({ timeout: 10_000 });
  146 | 
  147 |     // Modal vẫn mở để user sửa lại
  148 |     await expect(page.locator('[role="dialog"]')).toBeVisible();
  149 |   });
  150 | 
  151 |   test('T5 — cancel: đóng modal không lưu data', async ({ page }) => {
  152 |     const partNumber = uniquePartNumber('MAT-CANCEL');
  153 | 
  154 |     await page.locator('#btn-add-material').click();
  155 |     await expect(page.locator('[role="dialog"]')).toBeVisible();
  156 | 
  157 |     await page.locator('#partNumber').fill(partNumber);
  158 |     await page.locator('#materialName').fill('Should Not Be Saved');
  159 | 
  160 |     // Click Huỷ
  161 |     await page.locator('[role="dialog"] .btn-outline').click();
  162 |     await expect(page.locator('[role="dialog"]')).not.toBeVisible();
  163 | 
  164 |     // Xác nhận qua API thật: material không tồn tại trong DB
  165 |     const found = await page.evaluate(async (pn) => {
  166 |       const { materialApi } = await import('/src/services/api.js');
  167 |       const res = await materialApi.getAll({ keyword: pn });
  168 |       return res.data.content ?? [];
  169 |     }, partNumber);
  170 |     expect(found).toHaveLength(0);
  171 |   });
  172 | });
  173 | 
```