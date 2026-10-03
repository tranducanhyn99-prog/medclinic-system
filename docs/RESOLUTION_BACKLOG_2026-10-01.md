# Danh sách vấn đề cần giải quyết — 01/10/2026

Nguồn: [báo cáo kiểm tra dự án](PROJECT_REVIEW_2026-10-01.md). Giao diện đã xử lý theo [báo cáo Impeccable](IMPECCABLE_FIXES_2026-10-01.md). **Cập nhật cuối 02/10/2026:** toàn bộ 22 mục có ID dưới đây đã đối chiếu và nghiệm thu trong phạm vi local; **194/194 tests, 0 skipped, coverage 84,15% LINE / 67,31% BRANCH**, JS 9/9 PASS. Lượt rà soát cuối còn sửa giới hạn độ dài đơn thuốc, binding dấu phẩy và lỗi tràn tổng số lượng. Xem [đối chiếu từng yêu cầu và bằng chứng cuối](BACKLOG_COMPLETION_2026-10-02.md); [phiên camera/SMTP/backup](BACKEND_ACCEPTANCE_2026-10-02.md) giữ bằng chứng nghiệm thu trước đó. Ngân hàng/SMTP nhà cung cấp thật, HTTPS production và CI cho mã hiện tại chưa được nghiệm thu.

## 1. P1 — Sửa trước

| ID | Việc cần làm | Điều kiện hoàn thành |
|---|---|---|
| P1-01 | **Đã nghiệm thu HTTP H2/SQL Server.** Đồng bộ mã chuyển khoản và validation callback | Callback HTTP dùng mã do ứng dụng sinh, đúng HMAC/số tiền, xác nhận hóa đơn; retry không ghi nhận thanh toán hai lần; tương thích mã cũ |
| P1-02 | **Đã nghiệm thu.** Kiểm tra lại trạng thái tài khoản khi hoàn tất OTP/magic link | Tài khoản bị khóa trong thời gian chờ xác thực không tạo được session; luồng kích hoạt đăng ký vẫn hoạt động |
| P1-03 | **Đã nghiệm thu HTTP và browser.** Validation bệnh án phía máy chủ | Triệu chứng/chẩn đoán rỗng bị từ chối; ca khám, hóa đơn và tồn kho không bị thay đổi khi input lỗi |
| P1-04 | **Đã nghiệm thu webcam thật trên localhost, người dùng xác nhận.** Permissions-Policy cho camera tại màn tiếp nhận | Tự động quét QR, tìm đúng lịch hẹn và check-in; policy giữ phạm vi cần thiết. HTTPS production chưa kiểm tra |
| P1-05 | **Đã xong.** Sửa khóa nút lưu sau khi hủy xác nhận khám | Chọn “Kiểm tra lại”, chỉnh form và lưu tiếp được; vẫn chặn gửi lặp khi request thực sự được gửi |

## 2. P2 — Sửa tiếp

| ID | Việc cần làm | Điều kiện hoàn thành |
|---|---|---|
| P2-01 | **Đã nghiệm thu lại H2/SQL Server và browser.** Dòng thuốc thiếu/lệch dữ liệu | Không NPE hoặc lỗi index; trả lỗi form rõ ràng, giữ dữ liệu nhập; hướng dẫn tùy chọn trống và dấu phẩy lưu nguyên văn; giới hạn 100/100/255 ký tự đúng schema, tối đa 100 dòng; số lượng tràn bị chặn trước xuất kho |
| P2-02 | **Đã nghiệm thu.** Ngăn đặt lịch bác sĩ/chuyên khoa ngừng hoạt động | Danh sách public và API áp dụng trạng thái; POST trực tiếp cũng bị chặn |
| P2-03 | **Đã nghiệm thu.** HTTP status, API error contract và thông báo lỗi | 400/403/404/500 phù hợp; API trả JSON; không hiển thị exception nội bộ |
| P2-04 | **Đã nghiệm thu.** Giới hạn redirect lỗi | Referer ngoài origin không được dùng làm đích redirect; có fallback nội bộ |
| P2-05 | **Đã xong.** Sửa quyền avatar bác sĩ công khai | Khách chưa đăng nhập thấy ảnh bác sĩ đã upload; quy định quyền riêng tư avatar bệnh nhân rõ ràng |

## 3. Kiểm tra bổ sung — Đã nghiệm thu local

- [x] CHECK-01: Response slot cũ không ghi đè lựa chọn bác sĩ/ngày mới khi tải bất đồng bộ. **Đã sửa abort/sequence và regression đảo thứ tự response**; xem UI-12 trong [Impeccable audit](IMPECCABLE_AUDIT_2026-10-01.md).
- [x] CHECK-02: Loại slot đã qua và thực thi giới hạn đặt lịch 14 ngày ở máy chủ; tests business clock/API/service đạt.
- [x] CHECK-03: Token reset mật khẩu chỉ consume một lần khi hai request đồng thời trên H2/SQL Server; đăng ký/đổi/reset thống nhất 8–72 ký tự.
- [x] CHECK-04: Service worker không dùng lại HTML theo session/CSRF giữa các tài khoản; offline fallback phù hợp.
- [x] CHECK-05: Docker TZ và Java business clock Việt Nam; kiểm tra chuyển ngày bằng clock cố định, runtime Docker +07.
- [x] CHECK-06: Browser đăng ký/OTP/magic link qua SMTP Mailpit đạt trên môi trường sqlserver,demo; dev dùng luồng email phát triển và không đại diện cho nghiệm thu SMTP.

## 4. Kiểm chứng và tài liệu

- [x] VERIFY-01: `mvn clean verify` hoàn tất 09:06:56 +07 ngày 02/10: **194 tests / 67 classes**, 0 failures/errors/skipped; LINE **84,15%**, BRANCH **67,31%**; JS 9/9 PASS. [Log/JSON cuối](evidence/backlog-final-2026-10-02/verification-summary.json).
- [x] VERIFY-02: 9 SQL Server cases trước đây bị skip và **16 acceptance cases SQL Server** đã chạy đạt trên database riêng `medclinic_e2e`; không dùng database browser `medclinic_db` cho JUnit.
- [x] VERIFY-03: Login/OTP mobile 390px, booking, tiếp nhận/QR webcam thật, khám và thu tiền DEMO/PDF đã nghiệm thu; không bao gồm thanh toán ngân hàng thật.
- [x] VERIFY-04: COPY_ONLY/CHECKSUM/VERIFYONLY, restore database tạm, DBCC CHECKDB, đối chiếu dữ liệu và uploads PNG fixture đạt.
- [x] DOC-01: Cập nhật README bằng số test/coverage và môi trường chạy thực tế.
- [x] DOC-02: `git diff --check` không còn lỗi whitespace/EOF; thông báo LF/CRLF là quy tắc line ending của Git.

## Trạng thái cài Impeccable

- Repo: [pbakaus/impeccable](https://github.com/pbakaus/impeccable).
- Skill Codex cài cho tài khoản tại `C:/Users/DUCANHZZ/.agents/skills/impeccable`.
- Installer chính thức đã đồng bộ về bản phát hành ổn định: skill **4.3.1**, Windows engine **0.1.5**. Bản development từ main được thay bằng bản release trong bước hoàn tất cài đặt.
- Engine đi kèm ở `scripts/bin/windows-x64/impeccable.exe`; kiểm tra launcher sau khi cài trả `impeccable-engine 0.1.5`.
- Hook dành cho dự án được installer tạo tại [`.codex/hooks.json` — bằng chứng local, xem tóm tắt](evidence/README.md), dùng đường dẫn skill của tài khoản. JSON hợp lệ; có PostToolUse và Stop. Chưa xác minh native hook tự chạy trong phiên chat hiện tại; nếu Codex yêu cầu chấp thuận hook khi nạp lại dự án, cần hoàn tất bước đó để bật kiểm tra tự động.
- Dùng `$impeccable` từ lượt tiếp theo; `audit` kiểm tra chất lượng giao diện, `critique` đánh giá UX, `polish` hoàn thiện giao diện.
- Chưa chạy `init`, audit hoặc sửa giao diện trong yêu cầu cài đặt này.

## Kết quả Impeccable audit sau khi cài

[Báo cáo audit 01/10/2026](IMPECCABLE_AUDIT_2026-10-01.md): Implementation Integrity FAIL; 8/20; 19 nhóm vấn đề (1 P0, 7 P1, 11 P2). Detector trả 73 cảnh báo, đã phân loại theo ngữ cảnh Thymeleaf và browser. Đây là kết quả trước khi sửa. Xem [kết quả xử lý 19 nhóm UI](IMPECCABLE_FIXES_2026-10-01.md): build sạch 153 test (144 PASS, 9 SQL Server skipped), 9 regression JS PASS, coverage 76,18% LINE / 58,08% BRANCH; camera thật và nghiệm thu production còn mở.

Đối chiếu mục trùng: UI-01 ↔ P1-05; UI-02 ↔ P1-04; UI-03 ↔ P2-05; UI-12 ↔ CHECK-01; UI-17 ↔ CHECK-04. Các UI-04–11, UI-13–16 và UI-18–19 là phần bổ sung vào backlog chất lượng giao diện. Phạm vi browser và những kiểm tra chưa hoàn tất được ghi rõ trong báo cáo audit.

Kết quả 153 tests/camera còn mở ở đoạn audit phía trên là lịch sử đợt UI ngày 01/10. Đợt [nghiệm thu backend 02/10](BACKEND_ACCEPTANCE_2026-10-02.md) đạt 182/182 PASS, webcam thật đã xác nhận, các mục local đã hoàn tất. Sau đó [mẫu hóa đơn PDF đã làm lại](INVOICE_PDF_UPDATE_2026-10-02.md): tiền dạng `353 000`, ngày giờ dễ đọc, chi tiết từng thuốc và khối ký xác nhận; **186/186 tests PASS**, coverage **83,88% LINE / 64,86% BRANCH**. Nghiệm thu tích hợp nhà cung cấp thật/production vẫn là công việc riêng.

Lượt [đối chiếu backlog cuối](BACKLOG_COMPLETION_2026-10-02.md) nâng kết quả lên **194/194 PASS** sau 4 regression mới chạy trên cả H2 và SQL Server. Browser ca DEMO `MED-261002-526A4F99` xác nhận lỗi không thay đổi database, sửa số lượng rồi lưu lại thành công với dấu phẩy giữ nguyên. Ứng dụng 18087 đã cập nhật; không còn mục local nào trong bảng/checklist trên chưa xử lý.
