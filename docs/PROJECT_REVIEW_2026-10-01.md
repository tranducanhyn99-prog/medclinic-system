# Báo cáo kiểm tra dự án MedClinic — 01/10/2026

**Kết luận:** build sạch và bộ test hiện có đạt, nhưng kiểm tra bổ sung tìm thấy 10 vấn đề cần sửa. Năm vấn đề ưu tiên P1 ảnh hưởng thanh toán, khóa tài khoản, dữ liệu bệnh án, quét QR và thao tác lưu khám. Chưa đủ bằng chứng để nghiệm thu production.

## Phạm vi và bằng chứng

- Kiểm tra working tree hiện tại trên commit nền `8a9beba`, gồm 21 file tracked đã thay đổi và các asset/template chưa tracked. Không giới hạn review ở commit HEAD.
- Rà soát cấu trúc 135 file Java production, 61 file Java test, 30 template HTML; tập trung controller/service/security/repository/entity, giao diện và JavaScript, cấu hình Maven, Flyway V1–V6, Docker/Compose, CI, backup/restore và tài liệu.
- Môi trường: Windows, Java 21.0.12.1, Maven 3.9.16. Lệnh kiểm tra sạch:

```powershell
mvn -B -o '-Dmaven.repo.local=C:/Users/DUCANHZZ/.m2/repository' clean verify
```

| Kiểm tra | Kết quả |
|---|---|
| Build, package và coverage gate | BUILD SUCCESS |
| Surefire | 147 test / 61 class; 138 pass, 9 skipped, 0 failure, 0 error |
| JaCoCo LINE | 75,75% — 1546 covered / 2041 total; ngưỡng 70% |
| JaCoCo BRANCH | 57,27% — 374 covered / 653 total; ngưỡng 50% |
| Kiểm tra bổ sung backend | Chạy ứng dụng dev với H2 riêng `project_audit`, MockMvc và email mock; không gọi ngân hàng hay SMTP thật |
| Kiểm tra JavaScript | Chạy đoạn mã gốc của form khám và dashboard bằng Node VM với DOM tối giản |
| Docker / SQL Server runtime | Docker daemon không hoạt động; chưa chạy lại các test SQL Server, migration thật, backup hoặc restore |
| `git diff --check` | 38 cảnh báo whitespace/EOF trong 4 file; không làm build fail |

Bằng chứng tại [log build sạch — bằng chứng local, xem tóm tắt](evidence/README.md), [kết quả probe backend — bằng chứng local, xem tóm tắt](evidence/README.md), [kết quả probe JavaScript — bằng chứng local, xem tóm tắt](evidence/README.md) và [JaCoCo HTML — bằng chứng local, xem tóm tắt](evidence/README.md). Các file trong `target` bị xóa khi chạy `mvn clean`.

## Các vấn đề đã xác nhận

### 1. P1 — Mã chuyển khoản mới không qua được validation của callback

Vị trí: [BankTransferPaymentServiceImpl.java:181](../src/main/java/com/medclinic/service/impl/BankTransferPaymentServiceImpl.java), [BankTransferCallbackDto.java:11](../src/main/java/com/medclinic/dto/BankTransferCallbackDto.java).

`nextTransferCode()` hiện tạo `Thanh toan hoa don <6 ký tự>`, chứa dấu cách và chữ thường. DTO callback áp dụng `[A-Z0-9]{6,25}`. Vì vậy mã mà ứng dụng tự tạo luôn bị `@Valid` từ chối trước khi kiểm tra HMAC. Đây là regression trong thay đổi chưa commit; mã cũ dạng `MC...` phù hợp regex.

**Tái hiện:** tạo yêu cầu từ hóa đơn trên H2, gửi callback với đúng mã, số tiền và chữ ký HMAC. Kết quả `MethodArgumentNotValidException`; hóa đơn vẫn `UNPAID`. Handler lỗi chung còn trả HTTP 200 với HTML.

**Hướng sửa:** thống nhất định dạng mã giữa bên sinh, DTO và bên đối soát; nếu giữ nội dung chuyển khoản thân thiện, cập nhật contract/validation tương ứng và xử lý tương thích mã cũ. Thêm integration test đi qua HTTP controller với mã do `createInstruction()` sinh.

### 2. P1 — Tài khoản bị khóa vẫn đăng nhập qua challenge OTP đang còn hạn

Vị trí: [AuthVerificationServiceImpl.java:114](../src/main/java/com/medclinic/service/impl/AuthVerificationServiceImpl.java), [AuthController.java:291](../src/main/java/com/medclinic/controller/AuthController.java).

Sau bước xác thực mật khẩu, quản trị viên có thể khóa tài khoản trong khi người dùng đang chờ OTP. `consume()` không kiểm tra lại `isActive` đối với challenge LOGIN; `establishSession()` tạo authentication đã xác thực trực tiếp, bỏ qua kiểm tra trạng thái tài khoản của authentication provider.

**Tái hiện:** đăng nhập để nhận OTP → cập nhật `isActive=false` trong DB → gửi đúng OTP. Ứng dụng chuyển tới `/patient/appointments`; cùng session truy cập `/profile` được HTTP 200 dù DB vẫn ghi tài khoản không active.

**Hướng sửa:** kiểm tra trạng thái hiện tại khi consume LOGIN và trước khi lập session, áp dụng cho cả OTP và magic link; vô hiệu challenge liên quan khi khóa tài khoản. Phân biệt tài khoản chờ kích hoạt REGISTRATION với tài khoản bị quản trị viên khóa.

### 3. P1 — Lưu được bệnh án có triệu chứng và chẩn đoán rỗng

Vị trí: [DoctorDeskController.java:75](../src/main/java/com/medclinic/controller/DoctorDeskController.java), [MedicalRecordServiceImpl.java:70](../src/main/java/com/medclinic/service/impl/MedicalRecordServiceImpl.java).

Controller nhận `ExaminationFormDto` không có `@Valid`; DTO không có constraint cho các trường bắt buộc. Service gán nguyên giá trị vào entity, chỉ kiểm tra quyền, trạng thái cuộc hẹn và thuốc. `nullable=false` không ngăn chuỗi rỗng.

**Tái hiện:** gửi POST hợp lệ về quyền và CSRF, với `symptoms=""`, `diagnosis=""`. Ứng dụng lưu bệnh án, chuyển ca khám sang `COMPLETED` và tạo hóa đơn.

**Hướng sửa:** validation phía máy chủ cho triệu chứng/chẩn đoán, giới hạn độ dài và ngày tái khám; kiểm tra ở service để bảo vệ mọi caller. Dữ liệu không hợp lệ phải giữ ca khám và tồn kho nguyên trạng, trả lại form cùng lỗi cụ thể.

### 4. P1 — Header bảo mật vô hiệu camera của màn quét QR

Vị trí: [SecurityConfig.java:149](../src/main/java/com/medclinic/config/SecurityConfig.java), [reception.html:339](../src/main/resources/templates/staff/reception.html).

Mọi response có `Permissions-Policy: camera=(), microphone=(), geolocation=()`, trong khi tiếp nhận dùng `Html5QrcodeScanner` để lấy ảnh camera. Policy này cấm camera ngay cả khi người dùng muốn cấp quyền.

**Bằng chứng:** response `/staff/reception` thực tế chứa `camera=()`. Kiểm tra này xác nhận header; chưa thử thiết bị camera thật trong trình duyệt.

**Hướng sửa:** cho phép `camera=(self)` ở màn cần quét, giữ các quyền khác ở mức cần thiết; nghiệm thu lại quét trên localhost hoặc HTTPS.

### 5. P1 — Hủy hộp xác nhận khám làm nút lưu bị khóa

Vị trí: [dashboard.html:388](../src/main/resources/templates/layout/dashboard.html), [examine.html:325](../src/main/resources/templates/doctor/examine.html).

Form khám `preventDefault()` để chờ SweetAlert xác nhận. Listener chung của dashboard vẫn đặt `form.dataset.submitting=true` và disable nút submit. Chọn “Kiểm tra lại” không phục hồi trạng thái, khiến người dùng không thể lưu tiếp bằng nút đang hiển thị.

**Tái hiện bằng JavaScript gốc:** sau khi SweetAlert trả `{isConfirmed:false}`, event đã bị ngăn nhưng `BUTTON_DISABLED=true` và `FORM_SUBMITTING=true`. Đây là kiểm tra logic sự kiện bằng DOM tối giản, chưa phải browser E2E.

**Hướng sửa:** chỉ khóa form khi thật sự gửi; phối hợp listener xác nhận với listener chống gửi lặp và phục hồi nút khi hủy. Rà soát listener tương tự ở `layout/main.html`.

### 6. P2 — Các danh sách thuốc không đồng bộ gây exception thay vì lỗi form

Vị trí: [DoctorDeskController.java:82](../src/main/java/com/medclinic/controller/DoctorDeskController.java).

Controller lấy index theo `medicineIds` rồi truy cập `quantities`, `dosages`, `frequencies`, `instructions` mà không kiểm tra null hoặc kích thước. Request thiếu một danh sách gây NPE; danh sách ngắn hơn gây lỗi index.

**Tái hiện:** gửi `medicineIds=1` nhưng thiếu `quantities`. Kết quả `NullPointerException`, trang `error/error` với HTTP 200.

**Hướng sửa:** dùng danh sách DTO dòng thuốc với nested validation, hoặc kiểm tra tính đồng bộ trước khi dựng prescription; hiển thị lỗi và giữ dữ liệu người dùng đã nhập.

### 7. P2 — Bác sĩ bị khóa vẫn được hiển thị và nhận lịch mới

Vị trí: [AppointmentServiceImpl.java:49](../src/main/java/com/medclinic/service/impl/AppointmentServiceImpl.java), [DoctorServiceImpl.java:39](../src/main/java/com/medclinic/service/impl/DoctorServiceImpl.java).

Danh sách công khai lấy toàn bộ doctor profile; luồng booking không kiểm tra tài khoản bác sĩ hoặc chuyên khoa có active hay không. Khóa tài khoản bác sĩ không ngăn đặt lịch vào các slot còn sẵn.

**Tái hiện:** cập nhật bác sĩ seed sang `isActive=false`, bệnh nhân gửi POST đặt lịch cho slot ngày kế tiếp. Ứng dụng tạo cuộc hẹn và chuyển tới trang chi tiết.

**Hướng sửa:** quy định rõ khả năng nhận lịch khi bác sĩ/chuyên khoa ngừng hoạt động; lọc trên trang public/API và kiểm tra lại trong transaction booking để chặn request trực tiếp.

### 8. P2 — Handler lỗi trả sai HTTP status và tiết lộ exception nội bộ

Vị trí: [GlobalExceptionHandler.java:13](../src/main/java/com/medclinic/exception/GlobalExceptionHandler.java), [GlobalExceptionHandler.java:28](../src/main/java/com/medclinic/exception/GlobalExceptionHandler.java).

Handler render HTML nhưng không đặt 404/400/500, đồng thời đưa `ex.getMessage()` vào trang. Cấu hình `server.error.include-message=never` không che nội dung do controller advice tự render.

**Tái hiện:** `/doctors/999999` trả HTTP 200 thay vì 404; API slots với ngày `not-a-date` trả HTTP 200/HTML; payload callback sai validation cũng trả HTML và chi tiết validation thay vì lỗi JSON có status phù hợp.

**Hướng sửa:** đặt status đúng theo loại lỗi, giữ thông báo công khai an toàn, log exception đầy đủ ở server; có contract JSON riêng cho API. Bổ sung assertion về status, content type và body, tránh test chỉ kiểm tra tên view.

### 9. P2 — Redirect lỗi tin trực tiếp header Referer

Vị trí: [GlobalExceptionHandler.java:20](../src/main/java/com/medclinic/exception/GlobalExceptionHandler.java).

Khi có lỗi nghiệp vụ, handler dùng nguyên `Referer` làm đích redirect. Referer ngoài domain khiến ứng dụng chuyển người dùng ra website ngoài.

**Tái hiện:** bệnh nhân truy cập hóa đơn không thuộc mình, request có `Referer: https://example.test/audit`; response redirect đúng URL ngoài đó.

**Hướng sửa:** dùng đích điều hướng nội bộ đã biết hoặc chỉ cho phép path/URL cùng origin sau khi validate; có fallback an toàn.

### 10. P2 — Avatar bác sĩ upload không tải được trên trang công khai

Vị trí: [SecurityConfig.java:111](../src/main/java/com/medclinic/config/SecurityConfig.java), [UserContentController.java:18](../src/main/java/com/medclinic/controller/UserContentController.java), [doctors/list.html:76](../src/main/resources/templates/doctors/list.html).

Sau upload, URL avatar là `/user-content/avatars/...`. Trang chủ và danh sách/chi tiết bác sĩ công khai dùng URL này, nhưng security không cho anonymous truy cập route; request bị redirect tới login trước khi đọc file. Ảnh seed trong `/images` không bộc lộ lỗi.

**Bằng chứng:** anonymous GET một path đúng định dạng của route avatar trả 302 tới `/login`. Policy áp dụng trước bước tồn tại file.

**Hướng sửa:** tách rõ avatar bác sĩ công khai và nội dung bệnh nhân cần riêng tư; cấp quyền public cho loại ảnh phù hợp hoặc cung cấp route ảnh public riêng.

## Các điểm cần kiểm tra tiếp từ mã nguồn

Các mục dưới đây chưa được tái hiện bằng browser hoặc kiểm tra đồng thời trong lượt này; không tính vào 10 vấn đề đã xác nhận.

- `patient/booking.html` và `staff/walk-in.html` không hủy hoặc phân biệt response tải slot cũ. Khi đổi bác sĩ/ngày nhanh, response cũ có thể ghi đè danh sách mới. Booking service kiểm tra doctor/date nên có lớp bảo vệ dữ liệu, nhưng trải nghiệm có thể báo conflict sai.
- API slots lọc trạng thái AVAILABLE mà chưa lọc giờ đã qua; UI ghi nhận giới hạn 14 ngày nhưng service chưa kiểm tra trần ngày này.
- `PasswordResetToken` không có `@Version`, repository tìm token không khóa khi consume. Cần test hai request reset đồng thời với cùng token để kiểm tra cam kết dùng một lần. Chính sách độ dài mật khẩu cũng khác nhau: reset tối thiểu 6, đổi mật khẩu tối thiểu 8.
- Service worker precache `/`, trang có phần điều hướng theo session và CSRF. Cần kiểm tra cache giữa các tài khoản và offline fallback; ưu tiên một trang offline tĩnh không có nội dung theo người dùng.
- Dockerfile/Compose chưa cố định múi giờ nghiệp vụ, trong khi các service dùng `LocalDate.now()` / `LocalTime.now()`. Cần nghiệm thu nếu runtime dùng UTC, nhất là hàng chờ và giới hạn slot quanh thời điểm chuyển ngày Việt Nam.
- Profile dev dùng `DevelopmentEmailService` để bỏ qua gửi email, nên đăng ký/OTP người dùng mới không có email thật để lấy mã. Demo luồng này cần Compose/Mailpit theo README.

## Điểm đang hoạt động tốt và giới hạn nghiệm thu

Luồng booking có khóa slot và version; cấp số hàng chờ/mã hóa đơn có xử lý collision; kê đơn khóa thuốc theo thứ tự, ghi inventory transaction và tạo hóa đơn cùng transaction. Các test hiện có bao phủ tranh chấp H2, rollback bệnh án/tồn kho, CSRF, phân quyền URL, OTP, event sau commit, render template và PDF Unicode. Flyway/artifact tests và coverage gate đều đạt.

Baseline MVC nội bộ từ build sạch có p95: trang chủ 85,945 ms; booking 71,465 ms; tiếp nhận 43,586 ms; bàn khám 39,955 ms. Các số này không đo SQL Server runtime, tải đồng thời, mạng/CDN, rendering, animation hoặc camera trong trình duyệt.

Chín test SQL Server bị skip gồm hai test concurrent booking và các test migration, invoice reservation, rollback, stock concurrency, performance, release acceptance, shared rate limit. Chưa xác minh GitHub Actions cho working tree hiện tại; chưa kiểm tra lỗ hổng dependency theo cơ sở dữ liệu CVE hiện hành. Chưa nghiệm thu responsive và browser E2E cho giao diện mới.

README còn ghi mốc 139 test/59 class từ 23/09, trong khi nguồn hiện tại có 147 test/61 class. Cần cập nhật số liệu cùng môi trường chạy, tránh so trực tiếp coverage giữa lượt H2 và lượt có SQL Server.

## Thứ tự xử lý đề xuất

1. Sửa contract mã chuyển khoản/callback và kiểm tra lại trạng thái tài khoản khi consume OTP.
2. Bổ sung validation khám bệnh, sửa nút lưu sau khi hủy xác nhận và policy camera.
3. Sửa HTTP/error contract, redirect Referer, booking bác sĩ inactive và quyền avatar.
4. Thêm regression test cho các tình huống trên, chạy lại build sạch; bật SQL Server test trên database thử nghiệm riêng, nghiệm thu browser, camera và backup/restore.

Lượt review chỉ thêm báo cáo này và các file kiểm tra trong `target`; không sửa mã ứng dụng hoặc các thay đổi đang có của người dùng.
