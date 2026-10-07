# Kiểm tra dự án bằng Playwright CLI — 04/10/2026

**Kết luận lượt trước sửa:** giao diện và phần lớn tương tác đạt, nhưng phát hiện **1 lỗi nghiệp vụ ngày khám** và **1 lỗi tài nguyên nhỏ**; lượt này đạt 149/152. Cả hai lỗi đã được xử lý và nghiệm thu lại ở [báo cáo sau sửa](PLAYWRIGHT_FIX_ACCEPTANCE_2026-10-04.md), 154/154 CLI và 242/242 Java. Bằng chứng dưới đây được giữ nguyên làm lịch sử.

Chạy bằng **`@playwright/cli 0.1.22` chính thức của Microsoft**, lệnh `open` và `run-code --filename`; trình duyệt thật **Microsoft Edge 154.0.4258.53 / Chromium headless**. Không dùng CUA làm bằng chứng cho lượt CLI này. Tool/dependency, snapshot và log thô nằm trong `output/` bị ignore; không thêm npm dependency vào ứng dụng Java.

Source: working tree `codex/completion-runtime-ci`, nền `fa56618`, gồm các sửa ngày 03/10 chưa commit. Runtime chính: Docker SQL Server + Mailpit ở `127.0.0.1:8080`; ca có ghi dữ liệu: H2 riêng ở `127.0.0.1:18090`. Docker Desktop được khởi động lại trước kiểm tra vì các endpoint ban đầu không truy cập được.

## Kết quả

| Nhóm | Kết quả |
|---|---|
| Tổng assertion | **149/152 đạt**, 3 assertion không đạt cùng liên quan đến ngày khám tương lai |
| Ma trận render | **115/115 đạt**: 23 tổ hợp trang/vai trò × 5 cấu hình viewport/theme |
| Viewport/theme | 1440×900 sáng/tối; 768×1024 sáng; 390×844 sáng/tối. Theme thực tế `data-bs-theme` khớp cấu hình, không chỉ mô phỏng media query |
| Trang/vai trò | Public: home, danh sách/hồ sơ bác sĩ, login/register/forgot password. Patient: booking, appointments, history, profile. Staff: reception, walk-in, billing, profile. Doctor: desk, schedule, profile. Admin: dashboard, users, doctors, specialties, medicines, profile |
| Render và tài nguyên | HTTP 200, 1 H1 và main landmark, không tràn ngang cả trang, không có ảnh hỏng đang hiển thị hoặc input thiếu nhãn ở các mẫu; không request tài nguyên ngoài origin ở ma trận |
| JavaScript/network | **0 pageerror**, **0 HTTP error do page response listener ghi nhận**, **1 console error 404**. Kiểm tra riêng `/favicon.ico` trả 404; API probe này không thuộc bộ đếm HTTP của page listener |
| Auth và bàn phím | Đăng nhập 4 tài khoản seed, anonymous chuyển về login khi vào trang bảo vệ, patient/staff/doctor bị 403 ở admin, skip link nhận focus và chuyển vào main, đăng ký rỗng báo lỗi server |
| Booking và trạng thái lỗi | Tìm bác sĩ, theme lưu qua reload, fetch slot bị abort hiển thị lỗi/khóa submit, retry tải lại thành công, chọn giờ cập nhật selection/summary, lưu lịch và xem QR |
| Dark mode giờ khám | Tương phản mẫu nút giờ **16,30:1**, chiều cao **44,59 px**; không suy diễn một mẫu này thành chứng nhận WCAG toàn hệ thống |
| Mailpit và hủy lịch | Email đặt lịch nhận PNG inline QR đúng MIME; dialog hủy được đóng mà giữ lịch, xác nhận hủy chuyển lịch sang “Đã hủy”; ticket vẫn hiển thị ở print media |
| Offline | Điều hướng trang bệnh nhân khi ngắt mạng hiện “Chưa có kết nối mạng”; cache không có HTML authenticated/API; nối mạng lại tải được trang |
| Form khám, trong ca biên H2 | Thêm/xóa thuốc có live announcement và nhãn, 2 viên Paracetamol hiển thị **3 000 đ**, nút lưu mở review/confirmation, chọn “Kiểm tra lại” giữ input và không tạo hóa đơn |
| WebSocket | Doctor Desk kết nối và báo “Đang nhận cập nhật” |

[Tổng hợp JSON](evidence/playwright-2026-10-04/summary.json), [public](evidence/playwright-2026-10-04/public.json), [patient](evidence/playwright-2026-10-04/patient.json), [staff](evidence/playwright-2026-10-04/staff.json), [doctor](evidence/playwright-2026-10-04/doctor.json), [admin](evidence/playwright-2026-10-04/admin.json).

## Vấn đề đã phát hiện và tasklist đã xử lý

Các mục dưới đây đã hoàn tất trong yêu cầu sửa tiếp theo; xem bằng chứng sau sửa ở báo cáo liên kết đầu trang.

### PW-01 — P2: ngày tương lai vẫn được tiếp nhận và bắt đầu khám

Ngày kiểm tra **04/10/2026**, lịch khám **05/10/2026**:

1. Đặt lịch hợp lệ qua UI bằng patient seed trong H2 fixture.
2. Staff gửi `POST /staff/reception/check-in/{id}` bằng Playwright APIRequestContext với CSRF và session hợp lệ. Server trả 302; patient mở lại chi tiết thấy **“Đã tiếp nhận (Chờ khám)”** thay vì vẫn “Đã xác nhận”.
3. Doctor gửi `POST /doctor/start-examination/{id}` tương tự. Server trả 302; GET trang khám trả 200 và có `examinationForm` cho lịch ngày mai.
4. Qua **UI khám vãng lai**, chọn ngày mai, chọn slot, nhập dữ liệu giả rồi submit. UI báo **“Đã đăng ký khám trực tiếp thành công”** nhưng danh sách hôm nay vẫn 0 cuộc hẹn. Source `bookWalkInAppointment` gọi ngay `checkInAppointment`, nên nhánh này cũng tiếp nhận lịch tương lai. Việc cấp STT trong nhánh vãng lai là suy luận từ source; UI success không in số STT.

`AppointmentServiceImpl.checkInAppointment` hiện kiểm tra status rồi cấp queue sequence, chưa kiểm tra ngày hiện tại. `MedicalRecordServiceImpl.startExamination` kiểm tra doctor/status, chưa chặn ca tương lai. Hàng chờ UI chỉ hiển thị ngày hôm nay. Điều này không khớp mô hình “STT trong ngày / hàng chờ hôm nay” ở FR-REC-02 và FR-EXAM-01.

- [x] Kiểm tra ngày bằng business clock ở service trước khi cấp STT/check-in; lịch đặt tương lai vẫn giữ `CONFIRMED` đến ngày khám.
- [x] Chặn bắt đầu khám lịch khác ngày hiện tại tại service, kể cả request trực tiếp có đúng role/CSRF.
- [x] Form/endpoint khám vãng lai chỉ đăng ký và tiếp nhận trong ngày; hiển thị hướng dẫn dùng đặt lịch cho ngày tương lai.
- [x] Thêm regression cho ngày tương lai/ngày đã qua và xác minh không đổi status, cấp queue sequence, trừ kho, tạo bệnh án/hóa đơn khi bị từ chối. Chạy lại ca hôm nay để bảo đảm luồng hợp lệ vẫn hoạt động.

Bằng chứng: [tương tác và check-in](evidence/playwright-2026-10-04/interactions.json), [bắt đầu khám](evidence/playwright-2026-10-04/clinical.json), [walk-in](evidence/playwright-2026-10-04/walk-in.json), [UI báo success vãng lai nhưng hôm nay trống](evidence/playwright-2026-10-04/future-walk-in.png). Hai probe POST đầu là kiểm tra ranh giới server qua Playwright, không phải thao tác click hàng chờ UI hôm nay.

### PW-02 — P3: favicon 404

Một console error 404 được ghi ở nhóm staff. Tái hiện riêng GET `/favicon.ico` trả **404**, trong khi tài nguyên UI khác đã tải được. Lỗi console tự phát không xuất hiện ở lượt focused tiếp theo, nên không kết luận mọi lần điều hướng đều có console error này.

- [x] Cung cấp favicon hoặc khai báo icon local rõ ràng trong cả layout main/dashboard; chạy lại console và kiểm tra anonymous favicon trả 200.

Bằng chứng: [console nhóm staff](evidence/playwright-2026-10-04/staff.json) và [request favicon](evidence/playwright-2026-10-04/favicon-check.json).

## Phạm vi và giới hạn

Lượt này diễn ra sau giờ slot khám trong ngày, nên booking dùng ngày kế tiếp. Ca form khám được mở qua probe của lỗi PW-01, **không phải bằng chứng luồng khám hôm nay hợp lệ hoàn chỉnh**. Form xác nhận lưu đã hủy; **0 bệnh án được lưu, 0 hóa đơn được tạo, 0 lần thu tiền**. Thanh toán ở runtime SQL chỉ kiểm tra trang thu ngân hiện có, chưa thực hiện giao dịch hay luồng PDF từ một ca mới qua browser.

Các test tương tác chỉ ghi vào H2 disposable; lượt debug/final tạo 4 lịch thử, hủy 1, thao tác status của ca biên và tạo một tài khoản walk-in giả. H2 fixture được xóa sau khi thu bằng chứng; SQL Server, uploads và bản xem trước 18088 được giữ. OTP đăng ký tài khoản mới, camera thật, screen reader, thiết bị touch thật, Firefox/Safari, production SMTP/ngân hàng và tải đồng thời không được nghiệm thu lại bằng lượt CLI này. Kết quả Maven **218/218** ngày 03/10 là lịch sử của lượt trước, không phải test Maven vừa chạy ngày 04/10.

Yêu cầu kiểm tra ban đầu không sửa code nghiệp vụ. Yêu cầu sửa tiếp theo đã chốt tasklist; kết quả và source thay đổi được ghi riêng trong báo cáo sau sửa.

## Chạy lại

Mở Docker Desktop, chạy Compose demo theo README, cần Node/npm và Microsoft Edge đã cài. Bộ script chỉ dùng tài khoản seed trong demo:

```powershell
docker compose up -d
.\ops\run-playwright-check.ps1
# Bao gồm ghi dữ liệu thử, probe ngày khám và UI trong H2 riêng:
.\ops\run-playwright-check.ps1 -WithFixture
```

[Runner](../ops/run-playwright-check.ps1) tải CLI phiên bản cố định vào `output/playwright-tools`, tạo browser context riêng từng role, lưu JSON/ảnh vào `output/playwright-check`, đóng đúng session của nó và dọn fixture có label do helper tạo. `-ResetFixture` chỉ dùng khi muốn bỏ dữ liệu của fixture cũ có đúng label; helper từ chối thay container không thuộc fixture. Không dùng tài khoản/database production với runner này. Không ghi auth storage/cookie/OTP vào artifact công khai.

Runner trả lỗi khi có nhóm kiểm tra không đạt. Tại lượt ban đầu, các callback trực tiếp ghi nhận 3 assertion PW-01 không đạt và wrapper mới chỉ được kiểm tra cú pháp. Sau khi sửa, wrapper `-WithFixture` đã thực chạy thành công với bộ assertion điều chỉnh; xem báo cáo sau sửa.

Tham khảo lệnh và hành vi `run-code`: [Playwright CLI chính thức](https://github.com/microsoft/playwright-cli), [running-code](https://github.com/microsoft/playwright-cli/blob/main/skills/playwright-cli/references/running-code.md).
