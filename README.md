# MedClinic — Quản lý và đặt lịch khám

Đồ án môn **Lập trình Web**: kết nối đặt lịch, tiếp nhận, khám, kê đơn và thu ngân trong một ứng dụng web với bốn vai trò: **bệnh nhân, bác sĩ, lễ tân và quản trị viên**.

```text
Chọn bác sĩ và giờ khám → Nhận phiếu hẹn QR → Tiếp nhận, cấp STT
→ Bác sĩ khám và kê đơn → Tạo hóa đơn → Thu ngân → Xem hồ sơ, tải PDF
```

Đây là ứng dụng học tập và demo. [Nghiệm thu sửa review](docs/PROJECT_REVIEW_FIX_ACCEPTANCE_2026-10-10.md) ghi kết quả xử lý chín vấn đề; [review ban đầu](docs/PROJECT_REVIEW_2026-10-10.md) giữ bằng chứng trước sửa. [Kịch bản thuyết trình](docs/PRESENTATION_SCRIPT_2026-10-10.md) dành cho một người trình bày trong khoảng 15 phút.

Đồ án do **một người thực hiện**. [Hồ sơ minh chứng bảo vệ](docs/DEFENSE_EVIDENCE_2026-10-12.md) đối chiếu rubric, chỉ vị trí code cho 8 nội dung mở rộng và hai ví dụ sửa/kiểm thử code có AI hỗ trợ.

## Giao diện

Xanh lam y khoa, nền trắng sạch, font Geist local và chế độ sáng/tối. Ảnh chụp ở lượt thiết kế ngày 09/10/2026.

![Trang chủ MedClinic](docs/assets/readme/home.png)

![Đăng nhập MedClinic](docs/assets/readme/login.png)

## Chức năng

| Vai trò | Chức năng |
| --- | --- |
| Bệnh nhân | Tìm bác sĩ theo chuyên khoa, mức phí và từ khóa; đặt/hủy lịch; xem phiếu hẹn QR, trạng thái, hồ sơ; tải đơn thuốc/hóa đơn PDF. |
| Lễ tân | Tra cứu lịch, quét QR bằng camera hoặc nhập mã; cấp STT; đăng ký vãng lai; thu tiền mặt; tạo chuyển khoản và đối soát khi đã cấu hình. |
| Bác sĩ | Xem hàng chờ và cập nhật WebSocket; bắt đầu khám; xem lịch sử bệnh nhân; ghi kết quả, kê thuốc, hoàn tất ca; xem lịch trực; xem lại bệnh án hoàn tất của mình, phân trang và tải đơn thuốc. |
| Quản trị viên | Quản lý chuyên khoa, bác sĩ, tài khoản, thuốc; nhập kho và xem lịch sử tồn kho; xem thống kê lịch hẹn/doanh thu. |

Backend khóa khung giờ để tránh đặt trùng, kiểm tra bác sĩ phụ trách và trạng thái ca khám. Khi hoàn tất khám, bệnh án, đơn thuốc, xuất kho và hóa đơn được ghi trong cùng transaction. Spring Security bảo vệ route; service và luồng đọc phiếu hẹn/PDF có thêm kiểm tra quyền.

**Thanh toán:** tiền mặt có sẵn. VietQR chỉ bật khi cấu hình đủ; tạo QR chưa xác nhận hóa đơn đã trả tiền. Callback kiểm tra HMAC và mã giao dịch. Ngân hàng thật chưa được xác nhận trong lượt review này.

**Google:** bệnh nhân có thể đăng ký/đăng nhập qua Google sau khi cấu hình OAuth Client, rồi xác nhận OTP email trước khi vào tài khoản. Email đã xác thực của bệnh nhân active được liên kết; tài khoản mới chỉ có quyền bệnh nhân. Nút mặc định ẩn khi chưa bật `GOOGLE_LOGIN_ENABLED`. Xem [hướng dẫn Google OAuth](docs/GOOGLE_LOGIN_SETUP.md) và [cấu hình Gmail gửi OTP thật](docs/GMAIL_OTP_SETUP.md).

## Công nghệ

| Phần | Công nghệ trong source |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.1, Spring MVC, Spring Security, Bean Validation |
| Giao diện | Thymeleaf Layout, Bootstrap, JavaScript, Chart.js, Geist; thư viện/font local |
| Dữ liệu | JPA/Hibernate; H2 cho dev/test; SQL Server và Flyway V1–V10 |
| Realtime, xuất file | WebSocket/STOMP/SockJS, ZXing QR, OpenPDF và font Unicode |
| Kiểm thử, vận hành | JUnit, Mockito, MockMvc, Node test runner, JaCoCo, Maven, Docker Compose, GitHub Actions |

Phiên bản backend nằm trong [pom.xml](pom.xml). Mô hình chính là **Controller → Service → Repository → Database**. Thymeleaf render HTML; chọn giờ và cập nhật hàng chờ sử dụng JavaScript/API.

## Chạy nhanh — H2

Cần **JDK 21** và **Maven** trong `PATH`; cần **Node.js 22** để chạy regression frontend. Tại thư mục chứa `pom.xml`:

```powershell
mvn spring-boot:run
```

Mở [localhost:8080](http://localhost:8080). Profile mặc định là `dev`. **Dừng ứng dụng sẽ mất dữ liệu H2 trong RAM**; lần chạy tiếp theo tạo lại seed.

| Tài khoản | Vai trò | Mật khẩu demo |
| --- | --- | --- |
| `patient` | Bệnh nhân | `123456` |
| `staff` | Lễ tân | `123456` |
| `doctor` | Bác sĩ | `123456` |
| `admin` | Quản trị viên | `123456` |

Seed trong `dev`/`demo` bỏ qua OTP khi dùng mật khẩu; Google luôn yêu cầu OTP. Nút điền nhanh chỉ hiện ở `dev`. `dev` không gửi email: đăng ký tài khoản mới vẫn cần OTP, nên demo đăng ký/magic link/reset mật khẩu bằng Mailpit bên dưới hoặc cấu hình Gmail SMTP.

H2 console: `/h2-console`, URL `jdbc:h2:mem:medclinic_db`, user `sa`, password trống. Chỉ dùng cấu hình này trên máy phát triển.

### Ngày khám khi demo

`dev`/`demo` cho tiếp nhận và khám lịch từ hôm nay đến **ngày +14**, giữ ngày hẹn và STT theo ngày. Có thể chọn ngày mai nếu giờ khám hôm nay đã qua. Khám vãng lai vẫn chỉ tiếp nhận hôm nay.

Muốn kiểm tra quy tắc đúng ngày:

```powershell
$env:DEMO_EARLY_CLINICAL_PROCESSING = 'false'
mvn spring-boot:run
```

Ngoài `dev`/`demo`, xử lý ca khám luôn yêu cầu hôm nay. Xem [chính sách demo](docs/CLINICAL_DEMO_BACKEND_2026-10-06.md).

## Demo SQL Server + email bằng Docker

Cần Docker Desktop với Linux containers:

```powershell
if (-not (Test-Path -LiteralPath .env)) {
    Copy-Item -LiteralPath .env.example -Destination .env
}
```

Giữ `.env` đang có nếu đã cấu hình. Điền `MSSQL_SA_PASSWORD` đủ mạnh và `REMEMBER_ME_KEY` ngẫu nhiên nếu còn trống, rồi chạy:

```powershell
docker compose up --build -d
```

| Dịch vụ | Địa chỉ |
| --- | --- |
| MedClinic | [localhost:8080](http://localhost:8080) |
| Mailpit — email demo | [localhost:8025](http://localhost:8025) |
| SQL Server | `localhost:14333` hoặc `SQLSERVER_HOST_PORT` đã cấu hình |

Compose dùng `sqlserver,demo`, các cổng host bind loopback; Flyway quản lý schema, Hibernate validate. Volume giữ database/avatar. `docker compose down` giữ volume; thêm `-v` xóa dữ liệu volume. Không dùng cấu hình demo với dữ liệu thật.

Docker Compose đọc `.env`; `mvn spring-boot:run` **không tự đọc `.env`**. Không commit file đã điền secret.

## Kiểm thử

```powershell
mvn --batch-mode --no-transfer-progress clean verify
node --test src/test/frontend/ui-regressions.cjs
node ops/check-doc-links.cjs
```

Kết quả ngày **12/10/2026** nằm trong [hồ sơ minh chứng](docs/DEFENSE_EVIDENCE_2026-10-12.md): 282 test Java thực thi đạt, 50 SQL test skipped trong suite mặc định; 4 test bệnh án chạy riêng trên SQL Server đạt, 26 regression frontend và 176 kiểm tra trình duyệt đạt. [Báo cáo nghiệm thu 10/10](docs/PROJECT_REVIEW_FIX_ACCEPTANCE_2026-10-10.md) giữ kết quả lịch sử. Test skipped không tính thành pass.

Gate JaCoCo: LINE ≥70%, BRANCH ≥50%. Test SQL Server cần environment guard và **database kiểm thử riêng, có thể hủy**, theo [SQL Server runbook](docs/database/SQLSERVER_RUNBOOK.md). Không trỏ test vào database cần giữ.

[CI](.github/workflows/ci.yml) chạy Maven, frontend regression và kiểm tra link. Local pass không có nghĩa các thay đổi chưa commit đã được CI trên GitHub kiểm tra.

## Cấu trúc

```text
src/main/java/com/medclinic/
  controller/       Nhận request, trả view/JSON/PDF
  service/          Nghiệp vụ và transaction
  repository/       Truy vấn, khóa dữ liệu
  entity/, dto/     Mô hình lưu trữ và dữ liệu form/API
  security/, config/, event/, validation/, util/
  generator/        Công cụ offline tái tạo DOCX/diagram
src/main/resources/
  application.yml   Nhiều profile trong cùng một file
  templates/        Thymeleaf theo vai trò
  static/           CSS, JS, ảnh, font và thư viện local
  db/migration/     Migration SQL Server V1–V10
src/test/           Test Java và frontend regression
docker/, deploy/    Database, backup/restore, mẫu reverse proxy
ops/                Script kiểm chứng và vận hành
docs/               Đặc tả, mô hình, báo cáo và kịch bản bảo vệ
```

`target`, `output`, `tmp`, backup, uploads, secret và bằng chứng thô là dữ liệu local/sinh tự động. [Báo cáo dọn repo](docs/REPO_CLEANUP_2026-10-10.md) ghi các file đã loại bỏ và cách khôi phục bản sao local.

## Tài liệu

- [Nghiệm thu sửa chín vấn đề](docs/PROJECT_REVIEW_FIX_ACCEPTANCE_2026-10-10.md), [review ban đầu](docs/PROJECT_REVIEW_2026-10-10.md).
- [Tasklist xử lý 9 vấn đề](docs/PROJECT_REVIEW_TASKLIST_2026-10-10.md): thứ tự ưu tiên, tiêu chí hoàn tất và kiểm thử.
- [Kịch bản thuyết trình một người](docs/PRESENTATION_SCRIPT_2026-10-10.md): lời thoại, thao tác demo, phản biện và phương án dự phòng.
- [Yêu cầu](docs/01_SRS_IEEE830.md), [UML](docs/02_UML_DESIGN.md), [dữ liệu](docs/03_DATABASE_DESIGN_3NF.md), [kiến trúc](docs/05_SYSTEM_ARCHITECTURE.md).
- [Hệ thống giao diện hiện tại](DESIGN.md); báo cáo UI theo ngày trong `docs` là lịch sử của phiên bản lúc kiểm tra.
- [SQL Server runbook](docs/database/SQLSERVER_RUNBOOK.md), [provision admin](docs/database/PRODUCTION_ADMIN_PROVISIONING.md), [chính sách bằng chứng](docs/evidence/README.md).

Triển khai ngoài local cần `sqlserver,prod`, HTTPS/SMTP/database credentials, `APP_WEBSITE` và remember-me key từ môi trường. Xem [.env.production.example](.env.production.example) và runbook. V7/V9 dừng nâng cấp nếu dữ liệu cũ có điện thoại trùng/sai hoặc giá lẻ; cần xử lý dữ liệu trước migration. Tích hợp thực tế vẫn cần nghiệm thu riêng.

## Giấy phép

[MIT License](LICENSE). Giữ license đi kèm font và thư viện trong thư mục nguồn. Dữ liệu seed chỉ phục vụ demo.
