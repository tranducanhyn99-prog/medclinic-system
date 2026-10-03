# Cập nhật hóa đơn PDF — 02/10/2026

Đã thay mẫu PDF sơ sài bằng hóa đơn A4 có thông tin phòng khám, mã hóa đơn/ngày lập/trạng thái, bệnh nhân và lịch khám, bảng kê phí khám và từng thuốc, tổng tiền bằng số/chữ, phương thức/thời gian thanh toán, ô ký và số trang.

- Tiền nguyên hiển thị **350 000**, **3 000**, **353 000**, không có `.00`. Số có phần thập phân thực sự khác 0 giữ nguyên giá trị và dùng dấu phẩy, ví dụ `1 234,5`; chỉ thay cách trình bày, không làm tròn hay sửa dữ liệu.
- Ngày giờ dùng `dd/MM/yyyy HH:mm`; ngày khám/ngày sinh dùng `dd/MM/yyyy`.
- Chi tiết thuốc dùng số lượng và **đơn giá tại thời điểm kê đơn**. Với hóa đơn cũ không có chi tiết hoặc tổng chi tiết khác phí đã lưu, hiển thị phí thuốc tổng hợp theo hóa đơn, không tự lấy giá thuốc hiện tại.
- Hóa đơn chưa thanh toán/hoàn tiền giữ đúng trạng thái; không tạo chữ ký, mã số thuế hoặc thông tin thanh toán không có trong dữ liệu.
- Hóa đơn dài lặp lại tiêu đề bảng; tổng tiền và khối xác nhận được giữ cùng nhau; footer có mã hóa đơn và số trang.
- Thông tin cơ sở lấy từ `app.name`, `app.address`, `app.hotline`, `app.email`. Thông tin bệnh nhân chưa có hiển thị dấu `-`.

## Kiểm chứng

`clean verify` sau đổi mẫu và `verify` sau chỉnh độ rộng cột đều thành công. Lượt cuối: **186 tests / 67 classes**, **0 failures/errors/skipped**, bao gồm SQL Server riêng. JaCoCo cuối **83,88% LINE / 64,86% BRANCH**, gate 70%/50% đạt.

4 regression tests mới bảo vệ định dạng số/tiền bằng chữ, dữ liệu giá lịch sử và ngày giờ, hóa đơn cũ/chưa thanh toán, phân trang và quyền truy cập. Tests PDF/controller/font hiện có vẫn đạt. Đã render và kiểm tra trực quan hóa đơn thực một trang, fixture chưa thanh toán và cả ba trang của fixture 45 thuốc; chỉnh cột ĐVT để chữ “Lượt” không bị tách dòng.

- [Verification summary](evidence/invoice-pdf-2026-10-02/verification-summary.json)
- [Lượt clean verify — bằng chứng local, xem tóm tắt](evidence/README.md)
- [Lượt verify cuối — bằng chứng local, xem tóm tắt](evidence/README.md)
- [Docker build cuối — bằng chứng local, xem tóm tắt](evidence/README.md)
- [Text hóa đơn tải từ browser](evidence/invoice-pdf-2026-10-02/hoa-don-text.txt)
- [PDF mẫu mới — bằng chứng local, xem tóm tắt](evidence/README.md)

Mẫu được tải từ `/export/invoice/1` qua phiên lễ tân trong ứng dụng SQL Server ở **http://127.0.0.1:18087**. Mã DEMO `INV-20261002-21F4935BB8624CEB`, tổng tiền **353 000 VND**, PAID/CASH. Image cuối: `sha256:1e02be645985b51b0f5a2ce4667e66fc14a9d594801f83fabce50ed49f963348`.

Chỉ cập nhật container ứng dụng nghiệm thu; giữ database/volume và tổng số 3 container đang dùng. Nút **In Hóa Đơn** xuất mẫu mới; file PDF đã tải trước đó không tự thay đổi.
