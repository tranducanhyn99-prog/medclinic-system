# UX-10 — nghiệm thu lưu ca khám qua browser

Ngày: 03/10/2026. Trạng thái: **Đạt UX-10 trong phạm vi nghiệm thu; tasklist UX/UI 16/16.**

## Môi trường

Preview H2 cũ ở port 18088 đã dừng. Đã khởi động lại ứng dụng từ source hiện tại, profile dev, bind 127.0.0.1:18088, database mới `jdbc:h2:mem:ux10_20261003`. Hàng đợi hôm nay được quan sát trực tiếp là 0 ca. SMTP trỏ localhost port 9; bank payment tắt; H2 console tắt. Không chạy trên SQL Server 18087.

Log khởi động: `output/ux10-preview-2026-10-03.log`. Người dùng đã xác nhận cụ thể: **“Đồng ý tạo và lưu ca DEMO này”**. Đã thực hiện toàn bộ thao tác ghi qua browser trong H2 này.

## Dữ liệu giả được duyệt và đã lưu

| Trường | Giá trị giả |
|---|---|
| Họ tên | NGHIỆM THU UX-10 DEMO |
| Số điện thoại | 0900000010 — dùng làm fixture, không liên hệ |
| Bác sĩ | Nguyễn Văn Hùng, Tim mạch |
| Ngày khám | 03/10/2026, 09:00–09:30, STT-01 |
| Lý do, triệu chứng, chẩn đoán, lời dặn | DEMO nghiệm thu UX-10 |
| Thuốc | 2 viên Paracetamol 500mg, 1 500 đ/viên |
| Tổng hóa đơn | Khám 350 000 đ + thuốc 3 000 đ = 353 000 đ |
| Hóa đơn | UNPAID; không xác nhận thu tiền |

Mã hẹn: **MED-261003-BA755DE1**, appointment ID **1** (URL browser). Mã hóa đơn: **INV-20261003-9B8C805FA3174E62**, tạo lúc 08:51.

## Kết quả quan sát trực tiếp

1. Đăng ký vãng lai tạo đúng một cuộc hẹn CHECKED_IN. Tài khoản bác sĩ thấy ca chờ khám; Bắt đầu khám mở `/doctor/examine/1`.
2. Bấm lưu khi để trống trường bắt buộc: browser giữ ở form và focus `symptoms`; triệu chứng/chẩn đoán đều invalid. Chưa mở xác nhận hay lưu.
3. Điền nội dung DEMO đã duyệt; thêm một dòng Paracetamol, số lượng 2. Bản rà soát hiển thị đúng người, triệu chứng/chẩn đoán/lời dặn, thuốc, liều/tần suất và tiền **3 000 đ**.
4. Mở xác nhận, nhấn Enter ở **Kiểm tra lại**: vẫn ở URL form, giữ cả ba trường DEMO và số lượng 2; review vẫn mở; focus về `btnSubmitExamine`. Hiển thị “Đã chỉnh sửa · chưa lưu”.
5. Nhấn Enter ở nút lưu rồi Enter ở **Đồng ý lưu bệnh án**, chỉ xác nhận lưu một lần. Redirect bàn khám, thông báo **“Đã lưu kết quả khám thành công”**; tổng một ca, hoàn tất một ca, không còn ca chờ/đang khám. Lọc Đã hoàn tất thấy đúng mã hẹn với nhãn **“Bệnh án đã lưu”**.
6. Đọc lại tiền sử: đúng **một** bệnh án ngày 03/10/2026, chẩn đoán/triệu chứng/lời dặn DEMO; đơn thuốc **Paracetamol 500mg (Hasan) (2 Viên) - 1 viên**. Không submit lại form khi đọc tiền sử.
7. Billing: đúng **một** hóa đơn, chờ thanh toán **1**, đã quyết toán **0**. Đúng người và bác sĩ; tiền khám **350 000 đ**, thuốc **3 000 đ**, tổng **353 000 đ**, trạng thái **Chưa thanh toán**. Không mở Thu Tiền hoặc xác nhận thanh toán.

## Bằng chứng và kiểm thử

Thư mục `docs/evidence/ux10-acceptance-2026-10-03/`:

- `prepared-walk-in.png`: màn chuẩn bị trước quyền ghi.
- `walk-in-created.png`: tạo ca thành công, đã tiếp nhận.
- `clinical-review.png`: nội dung rà soát đầy đủ.
- `save-confirmation.png`: xác nhận đúng người/tổng thuốc.
- `cancel-keeps-draft.png`: hủy giữ dữ liệu.
- `save-success-completed.png`: thông báo lưu và ca hoàn tất.
- `persisted-history.png`: bệnh án và đơn thuốc đọc lại.
- `invoice-unpaid.png`: một hóa đơn 353 000 đ chưa thanh toán.

Kiểm thử tự động liên quan chạy lại trên source hiện tại: **Node 5/5 pass** (4 examination guard + 1 modal keyboard boundary). Các lần kiểm tra mobile, theme, AT/zoom/touch và kiểm thử Maven trước đó được ghi trong `IMPECCABLE_FINAL_SIX_2026-10-02.md`; không chạy lại toàn bộ hoặc suy diễn thành độ phủ mọi thiết bị trong lượt này.

## Giới hạn và lịch sử quyền ghi

Automatic approval review ở lượt trước chặn lưu vì thiếu quyền ghi dữ liệu y tế cụ thể. Lượt này ca cũ không còn trong H2; người dùng đã duyệt tạo và lưu duy nhất fixture mới cùng dữ liệu và đích nêu trên, nên bước chặn UX-10 đã được giải quyết.

Đối chiếu dữ liệu bằng giao diện; không truy vấn DB để xác nhận tồn kho hoặc ID bệnh án/đơn thuốc. H2 in-memory mất dữ liệu khi preview dừng. Không chạy nghiệm thu này trên SQL Server 18087, không thực hiện giao dịch thật. Tại thời điểm nghiệm thu, truy cập lại URL khám đã hoàn tất vẫn render form trống; không gửi form đó. Luồng bàn khám đã hoàn tất chỉ hiển thị “Bệnh án đã lưu”.

Follow-up GH-06 trong [bàn giao GitHub](GITHUB_RELEASE_2026-10-03.md) đã xử lý URL ca COMPLETED bằng redirect về bàn khám và thông báo bệnh án đã lưu, sau khi kiểm tra quyền bác sĩ. URL cũ không còn mở form tạo bệnh án mới. Đây là kết quả source sau lượt nghiệm thu ở trên.
