# NGHIỆP VỤ KHO THUỐC: CẢNH BÁO TỒN THẤP & NHẬP BỔ SUNG

**Dự án:** MedClinic<br>
**Ngày cập nhật:** 22/09/2026<br>
**Phạm vi:** Admin kho thuốc, bác sĩ kê đơn, audit nhập/xuất kho.

## 1. Mục tiêu nghiệp vụ

Hệ thống phải quản lý tồn kho theo biến động thực tế thay vì cho phép sửa số lượng tồn như một trường dữ liệu thông thường. Mỗi thuốc có ngưỡng nhập lại riêng (`reorderLevel`) để Admin biết khi nào cần bổ sung.

## 2. Quy tắc trạng thái tồn kho

- `stockQuantity = 0`: **Hết hàng**.
- `0 < stockQuantity <= reorderLevel`: **Sắp hết / cần nhập thêm**.
- `stockQuantity > reorderLevel`: **Bình thường**.
- `reorderLevel` mặc định là `50`, nhưng Admin được cấu hình riêng cho từng thuốc.
- Chỉ thuốc đang hoạt động mới được tính vào số cảnh báo tồn thấp trên Dashboard.

## 3. Luồng nhập bổ sung

1. Admin mở `Kho thuốc & dược phẩm`.
2. Chọn **Nhập thêm** ở thuốc cần bổ sung.
3. Nhập số lượng > 0; giá nhập và ghi chú là tùy chọn.
4. Hệ thống khóa bản ghi thuốc, tính tồn mới và lưu trong cùng transaction.
5. Hệ thống tạo một `InventoryTransaction` loại `RESTOCK` chứa tồn trước/sau và Admin thực hiện.

## 4. Luồng xuất kho theo đơn thuốc

1. Bác sĩ hoàn tất ca khám và kê đơn.
2. Service khóa từng thuốc theo ID để chống oversell đồng thời.
3. Nếu tồn không đủ, toàn bộ ca hoàn tất bị từ chối và transaction rollback.
4. Nếu đủ, hệ thống trừ tồn và tạo `InventoryTransaction` loại `DISPENSE` với `quantityChange` âm.
5. Giao dịch xuất kho tham chiếu `APPOINTMENT` và lưu bác sĩ là người thực hiện.
6. Nếu lưu bệnh án/hóa đơn thất bại, cả tồn kho và transaction kho phải rollback cùng nhau.

## 5. Nhật ký và audit

Mỗi giao dịch kho lưu: thuốc, loại giao dịch, số lượng biến động, tồn trước, tồn sau, giá nhập nếu có, ghi chú, tham chiếu nghiệp vụ, người thực hiện và thời điểm. Nhật ký không dùng để tính lại tồn hiện tại; `medicines.stock_quantity` là số tồn hiện hành, còn transaction là bằng chứng audit.

## 6. Quyền hạn

- `ROLE_ADMIN`: cấu hình thuốc/ngưỡng, nhập bổ sung, xem lịch sử kho.
- `ROLE_DOCTOR`: chỉ phát sinh xuất kho gián tiếp khi hoàn tất đơn thuốc hợp lệ.
- `ROLE_STAFF` và `ROLE_PATIENT`: không được thay đổi tồn kho.

## 7. Acceptance criteria

- Admin không cần sửa trực tiếp `stockQuantity` trong form chỉnh thông tin thuốc.
- Nhập 100 đơn vị từ tồn 20 phải cho tồn sau bằng 120 và ghi transaction `+100`.
- Kê 5 đơn vị từ tồn 120 phải cho tồn sau bằng 115 và ghi transaction `-5`.
- Thuốc dưới ngưỡng phải xuất hiện cảnh báo ở Dashboard và Kho thuốc.
- Giao dịch đồng thời không được làm tồn âm hoặc mất cập nhật.
- SQL Server production phải áp dụng schema qua Flyway `V4__inventory_restock_and_low_stock.sql`.
