# Kịch bản demo Lập trình Web — MedClinic

**Ngày chốt:** 23/09/2026<br>
**Thời lượng mục tiêu:** 10–15 phút<br>
**Trọng tâm:** trải nghiệm web, MVC, form/validation, session, phân quyền, AJAX, WebSocket, responsive và tích hợp dịch vụ web

## 1. Mục tiêu trình bày

Demo theo góc nhìn một ứng dụng web hoàn chỉnh. Người xem cần thấy rõ:

- Giao diện server-side bằng Thymeleaf, dùng chung layout và design system responsive.
- Luồng đăng nhập, session và điều hướng theo bốn vai trò.
- Form có validation, CSRF và thông báo lỗi/thành công rõ ràng.
- Dữ liệu động qua AJAX khi chọn bác sĩ, ngày và khung giờ.
- Cập nhật hàng chờ theo thời gian thực bằng WebSocket/STOMP.
- Upload avatar, email OTP/magic link qua Mailpit và xuất PDF từ trình duyệt.
- Cùng một ứng dụng hoạt động tốt trên desktop, tablet và mobile.

SQL Server, Flyway, transaction và locking vẫn là nền tảng của hệ thống, nhưng chỉ nêu ngắn khi giải thích vì sao thao tác web an toàn. Không dành phần lớn thời gian để trình bày schema hoặc quy trình vận hành.

## 2. Chuẩn bị trước khi demo

```powershell
docker compose up -d
docker compose ps -a
```

Chuẩn bị trình duyệt:

- Mở `http://localhost:8080` ở cửa sổ chính.
- Mở Mailpit `http://localhost:8025` ở tab riêng nếu trình diễn đăng ký/OTP.
- Chuẩn bị bốn cửa sổ hoặc browser profile tách session cho Patient, Staff, Doctor và Admin.
- Giữ DevTools đóng trong luồng chính để giao diện dễ nhìn; chỉ mở Network/Responsive Mode khi giảng viên hỏi về request AJAX hoặc responsive.
- Dùng ngày hiện tại hoặc một ngày trong 14 ngày tiếp theo và chọn **PGS.TS Nguyễn Văn Hùng** để tài khoản `doctor` nhận đúng hàng chờ.

Tài khoản demo dùng mật khẩu `123456`, chỉ dành cho profile `dev`/`demo`:

| Vai trò | Username | Màn hình web chính |
|---|---|---|
| Patient | `patient` | Đặt lịch, lịch hẹn, QR, bệnh sử |
| Staff | `staff` | Tìm kiếm/check-in, walk-in, thu ngân |
| Doctor | `doctor` | Hàng chờ, form khám, kê đơn động |
| Admin | `admin` | Dashboard, bảng quản trị, kho thuốc |

## 3. Kịch bản chính 10–15 phút

### 0:00–1:30 — Trang chủ và responsive web

1. Mở trang chủ và giới thiệu navbar, tìm bác sĩ, CTA đặt lịch và các khối nội dung.
2. Thu hẹp cửa sổ hoặc bật Responsive Mode ở khoảng 390 px để cho thấy navbar chuyển thành menu mobile, form tìm kiếm và CTA tự xếp lại, không tràn ngang.
3. Nói ngắn gọn: giao diện dùng Thymeleaf Layout, Bootstrap và `medclinic-design-system.css`; cùng một template thích ứng desktop/mobile.

Điểm web cần nhấn mạnh: semantic HTML, label/ARIA, keyboard focus, responsive breakpoint và tái sử dụng layout.

### 1:30–3:00 — Đăng nhập, session và phân quyền

1. Mở trang đăng nhập; thử bấm **Đăng nhập** khi để trống để trình diễn validation bắt buộc của trình duyệt.
2. Dùng nút điền nhanh tài khoản `patient`, sau đó đăng nhập.
3. Cho xem navbar đã đổi theo trạng thái session và chuyển đúng Patient Portal.
4. Nếu cần minh họa RBAC, nhập trực tiếp `/admin/dashboard` khi đang là Patient: server phải từ chối hoặc điều hướng theo cấu hình bảo mật.

Điểm web cần nhấn mạnh: form `POST`, CSRF token, Spring Security session, BCrypt và route authorization phía server; ẩn menu chỉ là hỗ trợ UX, bảo vệ thật nằm ở backend.

### 3:00–5:30 — Form đặt lịch động bằng AJAX

1. Vào **Đặt lịch khám**.
2. Chọn **Chuyên Khoa Tim Mạch**; danh sách bác sĩ được lọc theo lựa chọn.
3. Chọn **PGS.TS Nguyễn Văn Hùng** và ngày khám.
4. Chỉ ra vùng khung giờ đang được cập nhật mà không reload toàn trang. Trình duyệt gọi API public lấy slot còn trống rồi JavaScript render các nút giờ.
5. Chọn một slot, nhập lý do `Đau đầu, hồi hộp, cần kiểm tra huyết áp`, bấm xác nhận.
6. Trang kết quả hiển thị mã `MED-...`, QR và trạng thái lịch; ghi lại mã để dùng ở bước Staff.

Điểm web cần nhấn mạnh: thao tác kết hợp server-rendered form với AJAX; validation phía client giúp phản hồi nhanh, server vẫn kiểm tra lại dữ liệu và quyền trước khi lưu.

### 5:30–7:30 — Tìm kiếm, filter và check-in ở Staff Portal

1. Chuyển sang session `staff` → **Tiếp nhận & Check-in**.
2. Tìm bằng mã `MED-...`; cho thấy query/filter cập nhật danh sách kết quả.
3. Bấm **Check-in**, nhận số `STT-xx` và thông báo thành công.
4. Chuyển nhanh sang cửa sổ Doctor đã mở sẵn; hàng chờ nhận thay đổi theo WebSocket/STOMP hoặc bấm refresh nếu đường truyền realtime bị gián đoạn.

Điểm web cần nhấn mạnh: tìm kiếm bằng request có tham số, kết quả Thymeleaf server-rendered, thao tác thay đổi trạng thái dùng `POST` + CSRF, WebSocket bổ sung cập nhật realtime.

### 7:30–10:00 — Form khám và kê đơn động

1. Trong session `doctor`, mở bệnh nhân vừa check-in và bắt đầu khám.
2. Form khám hiển thị thông tin bệnh nhân và tiền sử ở cột trái; phần nhập liệu ở cột phải.
3. Nhập:
   - Triệu chứng: `Huyết áp 145/90 mmHg, hồi hộp nhẹ`.
   - Chẩn đoán: `Theo dõi tăng huyết áp độ 1`.
   - Thuốc: chọn một thuốc còn tồn, số lượng 5, giữ liều dùng mẫu.
   - Lời dặn: `Theo dõi huyết áp tại nhà, tái khám nếu triệu chứng tăng`.
4. Bấm **Thêm thuốc** để minh họa JavaScript thêm/xóa dòng form và tính tổng tiền ngay trên trang; sau đó giữ một dòng hợp lệ.
5. Bấm hoàn tất, cho xem SweetAlert xác nhận trước khi submit và thông báo sau khi server xử lý.

Điểm web cần nhấn mạnh: DOM động, input arrays, validation `required/min`, confirm dialog và cơ chế Post/Redirect/Get tránh submit lại khi refresh.

### 10:00–11:30 — Thu ngân và PDF trên web

1. Quay lại session `staff` → **Thu ngân & Viện phí**.
2. Mở hóa đơn mới; giao diện hiển thị phí khám, tiền thuốc và tổng tiền.
3. Chọn một trong hai luồng: xác nhận tiền mặt, hoặc bấm **Tạo VietQR**, quét QR MB Bank và chỉ ra trạng thái đang chờ callback/đối soát; sau khi `PAID`, mở hóa đơn PDF trong trình duyệt.

Điểm web cần nhấn mạnh: QR được tạo theo số tiền/nội dung riêng, JavaScript poll trạng thái, callback HMAC chạy server-to-server và endpoint PDF trả file trực tiếp cho browser. Nếu chưa có tài khoản/callback thật, giữ `BANK_PAYMENT_ENABLED=false` và demo tiền mặt.

### 11:30–13:00 — Đăng ký, OTP và upload avatar

Phần này có thể làm trước Admin nếu giảng viên quan tâm xác thực web:

1. Mở cửa sổ chưa đăng nhập → `/register`, tạo tài khoản với email và số điện thoại thử nghiệm duy nhất.
2. Mở Mailpit, lấy OTP hoặc bấm magic link; quay lại ứng dụng trong đúng luồng xác thực.
3. Vào Profile, chỉ ra form `multipart/form-data` và upload avatar JPG/PNG/WebP.
4. Nói rõ resend OTP gắn với session đang giữ challenge; tài khoản seed chỉ bypass OTP trong môi trường demo.

Điểm web cần nhấn mạnh: email integration local, token/link, session continuity, multipart upload và phục vụ file avatar qua endpoint riêng.

### 13:00–15:00 — Admin dashboard và kết luận web

1. Đăng nhập `admin`; cho xem dashboard desktop với KPI/Chart.js.
2. Mở **Kho thuốc** hoặc **Tài khoản** để trình diễn bảng responsive, filter/action và modal/form quản trị.
3. Thu hẹp cửa sổ để cho thấy sidebar đổi thành drawer mobile và card/bảng vẫn sử dụng được.
4. Kết luận: một codebase phục vụ public site và bốn portal, có MVC, security, AJAX, realtime, email, file upload, PDF và responsive UI.

Nếu còn 20–30 giây mới nêu bằng chứng: `139/139` test PASS khi bật SQL Server integration; UI đã kiểm tra tại 1440, 768 và 390 px.

## 4. Câu hỏi web thường gặp

| Câu hỏi | Trả lời ngắn |
|---|---|
| Vì sao dùng Thymeleaf thay vì SPA? | Phù hợp phạm vi môn học và Spring MVC; server quản lý route, session, validation và render HTML, còn JavaScript xử lý phần tương tác động cần thiết. |
| Phần nào dùng AJAX? | API lấy slot còn trống theo bác sĩ/ngày; trang không cần reload toàn bộ khi người dùng đổi lựa chọn. |
| Phần nào realtime? | Hàng chờ bác sĩ dùng WebSocket/STOMP; check-in và trạng thái khám phát cập nhật sau khi thao tác thành công. |
| Chống CSRF thế nào? | Spring Security phát token; các form thay đổi dữ liệu gửi token và server xác minh. |
| Phân quyền chỉ dựa vào menu? | Không. URL và service được bảo vệ phía server theo role; menu chỉ hiển thị tác vụ phù hợp. |
| Validation nằm ở đâu? | HTML5/JavaScript phản hồi nhanh; DTO/service/server kiểm tra lại vì client có thể bị bỏ qua. |
| Upload avatar an toàn thế nào? | Giới hạn 5 MB, chỉ nhận MIME JPG/PNG/WebP, tên file UUID, chuẩn hóa đường dẫn và chạy container bằng user không phải root. |
| Responsive được kiểm tra ra sao? | Đã chạy thật ở 1440×900, 768×900 và 390×844; không có horizontal overflow ở các màn chính. |

## 5. Checklist trước giờ bảo vệ

- [ ] App mở được tại `http://localhost:8080`; Mailpit mở được nếu trình diễn OTP.
- [ ] Bốn tài khoản demo đăng nhập được và mỗi vai trò dùng session riêng.
- [ ] Patient chưa giữ slot dự định chọn; bác sĩ Nguyễn Văn Hùng còn slot trong 14 ngày.
- [ ] Trình duyệt zoom 100%, không bật extension che giao diện, autofill không ghi đè dữ liệu demo.
- [ ] Đã thử desktop và mobile; menu mobile đóng/mở được bằng bàn phím.
- [ ] Đã chuẩn bị một mã lịch hẹn dự phòng hoặc biết cách tạo nhanh bằng walk-in.
- [ ] Không chạy `docker compose down -v` ngay trước giờ demo.
- [ ] Có sẵn [biên bản UI acceptance](17_P1_REVIEW_AND_UI_ACCEPTANCE_2026-09-23.md) nếu luồng live gặp sự cố.

## 6. Kịch bản dự phòng

| Sự cố | Cách tiếp tục theo hướng web |
|---|---|
| Mailpit/OTP không hoạt động | Dùng tài khoản seed, sau đó mở template OTP và kết quả test để giải thích luồng web; không để SMTP chiếm thời gian demo chính. |
| AJAX không tải slot | Chọn bác sĩ/ngày khác, kiểm tra app còn `UP`; chuyển sang màn lịch hẹn đã có để tiếp tục trình diễn portal. |
| WebSocket không cập nhật ngay | Bấm **Làm mới danh sách**; giải thích WebSocket là lớp cập nhật realtime, request HTTP thông thường vẫn là fallback. |
| Không có lịch hẹn phù hợp | Tạo walk-in bằng Staff hoặc đặt một slot mới; dùng cùng bác sĩ `doctor`. |
| Dữ liệu demo đã bẩn | Chọn slot/ngày khác. Không sửa trực tiếp SQL trong lúc demo web. |
| Mạng Internet mất | Ứng dụng và dữ liệu chạy local, nhưng Bootstrap/Icons/Google Fonts/SweetAlert/Chart.js đang dùng CDN. Dùng cache trình duyệt hoặc ảnh UI acceptance; backlog nên bundle asset local. |
| Docker Desktop tái lỗi socket | Dùng ảnh và biên bản UI/runtime để tiếp tục thuyết trình; không biến phần sửa Docker thành nội dung chính của bài Lập trình Web. |

## 7. Tài liệu hỗ trợ

- [UI/UX Design](06_UI_UX_DESIGN.md)
- [P1 UI acceptance](17_P1_REVIEW_AND_UI_ACCEPTANCE_2026-09-23.md)
- [Security & observability](10_SECURITY_OBSERVABILITY.md)
- [P0 runtime acceptance](15_P0_RUNTIME_ACCEPTANCE_2026-09-23.md)
- [Release acceptance E2E](09_RELEASE_ACCEPTANCE.md)

Kịch bản chính có thay đổi dữ liệu demo local. Sau buổi trình bày, giữ volume để đối chiếu hoặc backup trước khi chủ động reset.
