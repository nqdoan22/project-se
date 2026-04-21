# 03 - User Guide

## 1. Muc tieu

Huong dan nguoi dung cuoi su dung he thong IMS theo luong nghiep vu chinh:

- Dang nhap va truy cap chuc nang.
- Quan ly materials va lot.
- Nhap ket qua QC.
- Tao batch va xac nhan usage.
- In nhan va xem bao cao truy xuat.

## 2. Doi tuong su dung

- Admin
- Manager
- QC
- Operator

## 3. Dang nhap he thong

1. Mo trang frontend IMS.
2. He thong chuyen sang Keycloak login.
3. Nhap tai khoan va mat khau duoc cap.
4. Sau khi dang nhap thanh cong, nguoi dung duoc dieu huong den dashboard.

## 4. Huong dan theo luong nghiep vu

## 4.1 Tao Material

1. Vao menu Materials.
2. Chon tao moi.
3. Dien thong tin part number, name, type.
4. Luu va kiem tra material xuat hien trong danh sach.

Luu y:

- part number phai duy nhat.

## 4.2 Nhap kho lot

1. Vao menu Inventory Lots.
2. Chon Receive Lot.
3. Chon material va nhap quantity, UOM, ngay nhan, han su dung.
4. Submit.

Ket qua mong doi:

- Lot tao moi o trang thai Quarantine.
- Co transaction Receipt duoc ghi nhan.

## 4.3 Nhap QC test va duyet lot

1. Vao menu QC Tests.
2. Chon lot can kiem nghiem.
3. Nhap test type, method, ket qua.
4. Luu ban ghi test.
5. Thuc hien evaluate lot.

Ket qua mong doi:

- All pass: lot Accepted.
- Co fail: lot Rejected.

## 4.4 Tao sample lot

1. Tu trang chi tiet lot, chon Tao Sample.
2. Nhap sample quantity.
3. Xac nhan.

Ket qua mong doi:

- Tao lot con voi is_sample=true.
- Co lien ket parent_lot_id.

## 4.5 Tao production batch

1. Vao menu Production Batches.
2. Chon tao moi.
3. Nhap product, batch number, batch size.
4. Luu.

Ket qua mong doi:

- Batch tao moi o trang thai Planned.

## 4.6 Them lot vao batch va xac nhan usage

1. Mo chi tiet batch.
2. Them lot vao components.
3. Nhap planned quantity.
4. Khi su dung thuc te, xac nhan actual usage.

Ket qua mong doi:

- Tao Usage transaction am.
- Ton lot giam tuong ung.

## 4.7 Hoan tat batch va in nhan

1. Kiem tra batch da du dieu kien.
2. Chuyen trang thai sang Complete.
3. Chon in nhan Finished Product.

## 4.8 Xem bao cao truy xuat

1. Vao menu Reports.
2. Chon traceability theo lot hoac batch.
3. Xem timeline transaction, QC, va usage.

## 5. Cac loi thuong gap va cach xu ly

| Van de                    | Nguyen nhan pho bien                  | Cach xu ly                         |
| ------------------------- | ------------------------------------- | ---------------------------------- |
| Khong dang nhap duoc      | Sai tai khoan hoac role chua duoc gan | Lien he Admin de reset va gan role |
| Khong usage duoc lot      | Lot chua Accepted hoac khong du ton   | Kiem tra QC status va quantity     |
| Khong complete duoc batch | Chua xac nhan usage du dieu kien      | Kiem tra tat ca components         |
| Khong in duoc nhan        | Chua co template hoac loi cau hinh in | Kiem tra Label Template            |

## 6. Luu y bao mat

- Khong chia se token dang nhap.
- Dang xuat sau khi su dung xong tren may dung chung.
- Bao ngay cho Admin neu phat hien truy cap bat thuong.

## 7. Video huong dan su dung

- Link video user guide:
  - https://www.youtube.com/watch?v=IMS_USER_GUIDE_VIDEO_ID
