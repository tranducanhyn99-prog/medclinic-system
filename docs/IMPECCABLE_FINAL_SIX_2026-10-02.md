# MedClinic — chốt 6 task UX/UI còn lại

Ngày: 02/10/2026. Phạm vi: UX-07, UX-10, UX-12, UX-13, UX-15, UX-16; dùng Impeccable harden và giữ hệ cyan/teal/slate hiện tại.

## Kết quả

Đã chốt 5 task trong phạm vi nghiệm thu UI: UX-07/12/13/15/16. Tổng tasklist: **15/16**. UX-10 đã triển khai và đạt kiểm thử tự động, nhưng giữ mở bước lưu ca DEMO qua browser ở H2 18088. Không coi kiểm thử fixture hoặc MockMvc là lần lưu trên ứng dụng đang chạy.

Người dùng đã trả lời **“Đã ổn”** cho hướng dẫn kiểm tra zoom 200%, Narrator/NVDA, giảm chuyển động và touch. Đây là xác nhận của người dùng trên mẫu nghiệm thu; không có tên thiết bị, phiên bản browser hoặc bản ghi screen reader. Không suy diễn đây là chứng nhận WCAG hoặc độ phủ mọi thiết bị.

## Thay đổi

- Form khám: lỗi validation theo trường, error summary có liên kết, giữ dữ liệu trả về, focus lỗi server; cảnh báo rời trang sau lỗi. Thêm thuốc đưa focus đến select; xóa đưa focus đến dòng bên cạnh hoặc nút Thêm; thông báo qua live region. Giới hạn 100 dòng và không cho chọn thuốc hết kho mới.
- Guard lưu khám: một hộp xác nhận mỗi lần, hủy trả focus, kiểm tra lại validity trước submit, khôi phục trạng thái chưa lưu khi quay lại bằng BFcache và khi dispatch thất bại. Bản rà soát coi chuỗi chỉ có dấu cách là chưa nhập.
- Modal: thêm xử lý Tab/Shift+Tab ở biên để focus không đi vào chrome của browser. Giữ Escape của Bootstrap và focus trả về trigger. Xác nhận trực tiếp modal sửa thuốc; unit test kiểm tra bỏ qua phần tử disabled/ẩn. Modal nhập kho đã kiểm tra nhãn và Escape; dùng cùng guard.
- OTP: dùng deadline theo thời gian thực thay cho giảm từng giây; cập nhật khi quay lại tab/trang, hiện thông báo hết hạn và vô hiệu nút xác nhận. Backend vẫn quyết định hiệu lực cuối cùng.
- Chuyển khoản: QR hết hạn được ẩn và ngừng polling; vẫn giữ đối soát thủ công cho EXPIRED vì backend cho phép giao dịch đến muộn. CONFIRMED/REJECTED ẩn đối soát. Không tự xác nhận thanh toán. Thông báo hết hạn hướng dẫn kiểm tra tiền đã chuyển trước khi tạo mã mới.
- Lịch bác sĩ: từng khung giờ có biểu tượng và nhãn trạng thái đọc được; không phụ thuộc riêng màu sắc.
- Reduced motion: tăng specificity để rule chung thắng inline CSS của homepage, dừng pulse và hover transform trang trí.
- Tương phản: liên kết trong alert dùng màu của alert và gạch chân; làm đậm gradient CTA cam/teal và hotline teal.

## Tương phản và browser

| Cặp màu | Trước | Sau |
|---|---:|---:|
| Liên kết trên alert-danger light | 4,01:1 | 10,22:1 |
| Chữ trắng ở đầu sáng nhất của gradient hotline cam | 3,56:1 | 5,18:1 |
| Chữ trắng ở đầu sáng nhất của gradient CTA teal | 3,77:1 | 5,47:1 |
| Placeholder slate trên trắng | — | 7,58:1 |

Đã kiểm tra bổ sung desktop 1440×900 và mobile 390×844: form lỗi, thêm/xóa thuốc, rà soát, bốn trạng thái chuyển khoản và QR lỗi tải. Menu/theme/đối soát đạt vùng chạm 44px trong mẫu đã đo. Modal sửa thuốc giữ vòng focus sau sửa; Escape trả focus đúng.

Lượt xác nhận cuối có 4 mẫu DOM không báo lỗi trong phạm vi detector: homepage light desktop, bank EXPIRED dark desktop, examination error light desktop và dark mobile. Đã đọc computed colors của CTA gradient và placeholder riêng vì detector không đo gradient. Các số đo ngay giữa chuyển theme được giữ trong JSON và ghi rõ là trạng thái chuyển tiếp, không dùng làm kết luận màu ổn định. Đây không phải audit WCAG toàn diện. Độ phủ các role/route trước đó nằm trong báo cáo deep acceptance.

Bằng chứng: `docs/evidence/impeccable-final-six-2026-10-02/checks.json`, `examination-errors-desktop-light.jpg`, `examination-mobile-dark.jpg`, `bank-expired-desktop.jpg`. Viewport đã trả về mặc định sau kiểm tra.

## Kiểm thử

- Node frontend: **24/24 pass**. Các test mới kiểm tra guard lưu/hủy/revalidate/back navigation, lỗi dispatch, OTP qua suspension và focus biên modal.
- Maven toàn bộ: **205 test, 180 pass, 25 skip, 0 failure/error**. Log: `output/impeccable-final-six-confirmation.log`.
- Sau lượt sửa cuối: BackendAcceptanceIntegrationTest, DeepUiStateIntegrationTest, LayoutTemplateContractTest: **30/30 pass**. Log: `output/impeccable-final-six-targeted.log`.
- `git diff --check` đạt; Node syntax kiểm tra cho tooling và regression script.
- Lần Maven đầu có một assertion kiểm tra text HTML thô không tính entity `&agrave;` của th:field. Đã đổi assertion sang text giải mã entity; nội dung nhập được giữ đúng. Không thay code sản phẩm để che lỗi test.

## Mẫu nghiệm thu an toàn

`ops/ux-review-server.cjs` dựng mẫu từ HTML render của test H2 riêng, phục vụ trên port 18089. Năm mẫu gồm lỗi form khám và PENDING/EXPIRED/CONFIRMED/REJECTED. QR PENDING cố ý lỗi để xác nhận fallback. Mẫu không nối database, không proxy backend, không có API ghi; bỏ CSRF và chặn submit tại browser. Server chỉ GET/HEAD, đường dẫn file được giới hạn; POST thử trả 405, `.env` trả 404, trang hướng dẫn trả 200.

Mẫu được mở trong LAN cho điện thoại cùng Wi-Fi: `http://192.168.1.30:18089/`, máy tính `http://127.0.0.1:18089/`. Không đổi firewall hay mở ứng dụng khám ra LAN. Có thể dựng lại bằng `node ops/ux-review-server.cjs build`, chạy bằng `node ops/ux-review-server.cjs serve`. Các trang mẫu cần kết nối để tải Bootstrap CDN đang dùng trong dự án.

## Còn mở

UX-10: lần lưu qua browser cho ca DEMO duy nhất ở `http://127.0.0.1:18088/doctor/examine/1` chưa thực hiện. Automatic approval review ở lượt trước đã chặn thao tác lưu vì có thể ghi dữ liệu y tế, cần xác nhận riêng về dữ liệu và đích. Câu trả lời “Đã ổn” chỉ trả lời nghiệm thu mẫu zoom/AT/motion/touch, không được dùng làm quyền ghi ca khám.

Kiểm thử Maven đã chứng minh ca fake trong test tạo bệnh án/đơn thuốc, trừ tồn kho, chuyển COMPLETED và tạo hóa đơn UNPAID; không thay thế bằng chứng UI lưu trên preview. Ứng dụng SQL Server 18087 không được deploy/restart trong lượt này. Provider ngân hàng thật và giao dịch thật thuộc nghiệm thu tích hợp thanh toán, chưa chạy và không dùng để chặn việc chốt UI mocked states của UX-13.

## Cập nhật tiếp nối — 03/10/2026

Đã giải quyết bước còn mở UX-10 sau quyền ghi cụ thể của người dùng. Preview H2 mới ở localhost 18088: tạo duy nhất ca NGHIỆM THU UX-10 DEMO, kiểm tra validation/hủy giữ dữ liệu/trả focus, rồi lưu qua xác nhận bằng bàn phím một lần. Ca hoàn tất, tiền sử có đúng bệnh án/đơn 2 viên Paracetamol; billing có một hóa đơn 353 000 đ chưa thanh toán. Tổng tasklist hiện **16/16 trong phạm vi nghiệm thu**. Báo cáo và ảnh: `UX10_ACCEPTANCE_2026-10-03.md`. Những nội dung “còn mở” phía trên là trạng thái lịch sử 02/10.
