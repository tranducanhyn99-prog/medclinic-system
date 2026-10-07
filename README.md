# MedClinic

[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-green)](https://spring.io/projects/spring-boot)
[![CI](https://github.com/tranducanhyn99-prog/medclinic-system/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/tranducanhyn99-prog/medclinic-system/actions/workflows/ci.yml)
[![MIT](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

Đồ án LTW/CNPM về quản lý phòng khám: đặt lịch, tiếp nhận bằng QR/webcam, khám và kê đơn, quản lý thuốc, thu ngân và xuất PDF. Hỗ trợ bốn vai trò Bệnh nhân, Bác sĩ, Lễ tân và Admin.

## Chức năng

- Đặt lịch theo bác sĩ/chuyên khoa và khung giờ; khóa slot khi đặt để tránh trùng lịch, hủy lịch trả slot.
- Check-in bằng QR, cấp STT, đăng ký vãng lai; hàng chờ bác sĩ cập nhật qua WebSocket sau transaction commit.
- Profile `dev`/`demo` cho phép tiếp nhận và khám ngay lịch đã đặt từ hôm nay đến 14 ngày tới để chạy xuyên suốt kịch bản bảo vệ. Lễ tân và hàng chờ hiển thị cùng khoảng ngày; giữ nguyên ngày hẹn và STT theo ngày. Đặt `DEMO_EARLY_CLINICAL_PROCESSING=false` để kiểm tra đúng ngày; ngoài `dev`/`demo` luôn yêu cầu khám hôm nay. Vãng lai vẫn chỉ tiếp nhận hôm nay.
- Khám, kê thuốc, rà soát và xác nhận lưu; kiểm tra tồn kho, tạo bệnh án/đơn thuốc/hóa đơn trong transaction.
- Thu tiền mặt hoặc tạo VietQR khi đã cấu hình. Callback HMAC/idempotency và đối soát thủ công; tạo QR không đồng nghĩa đã thanh toán.
- Đăng ký và đăng nhập OTP/magic link, hồ sơ/avatar, phân quyền và audit; tài khoản seed chỉ dành cho dev/demo.
- PDF hóa đơn có chi tiết khoản thu/thuốc; tiền VND dạng **353 000**, không có `.00`.
- Giao diện responsive, dark/light, điều khiển bàn phím và thông báo trạng thái. [16 task UX/UI đã nghiệm thu trong phạm vi local](docs/UX_UI_TASKLIST_2026-10-02.md).

## Giao diện

![Trang chủ MedClinic](docs/assets/readme/home.png)

<p>
  <img src="docs/assets/readme/doctors.png" alt="Danh sách bác sĩ" width="49%">
  <img src="docs/assets/readme/login.png" alt="Đăng nhập" width="49%">
</p>

## Công nghệ và cấu trúc

Java 21, Spring Boot 4.1.1 / Spring Security, Spring MVC + Thymeleaf, JPA/Hibernate, Flyway, SQL Server 2022 hoặc H2. Jackson 3, OpenPDF, ZXing, Bootstrap, Chart.js, JUnit/Mockito, JaCoCo và Node test runner.

```text
src/main/java/com/medclinic/   controller, service, security, repository, entity, DTO, validation
src/main/resources/          templates, static assets, application.yml, Flyway V1–V6
src/test/                    Java integration/unit/concurrency và frontend regression
.github/workflows/ci.yml     Maven + Node + kiểm tra link tài liệu
ops/                        backup, restore drill, mẫu nghiệm thu UX, kiểm tra tài liệu
docs/                       đặc tả, thiết kế, runbook, biên bản và bằng chứng đã chọn
```

## Chạy nhanh bằng H2

Cần JDK 21, Maven 3.6.3+; Node 22 để chạy regression frontend. H2 không cần cài database riêng.

```bash
git clone https://github.com/tranducanhyn99-prog/medclinic-system.git
cd medclinic-system
mvn spring-boot:run
```

Mở [localhost:8080](http://localhost:8080). Profile mặc định `dev` dùng database in-memory; dữ liệu mất khi ứng dụng dừng. H2 console ở `/h2-console`, JDBC URL `jdbc:h2:mem:medclinic_db`, user `sa`, password trống; chỉ dùng trong phát triển.

Tài khoản seed `admin`, `doctor`, `staff`, `patient` dùng mật khẩu **123456** và bỏ qua OTP trong dev/demo. Shortcut đăng nhập chỉ hiển thị ở dev. Profile `dev` chủ động không gửi email; tài khoản mới cần OTP nên không thể hoàn tất đăng ký chỉ bằng lệnh H2 này. Dùng **Docker + Mailpit** bên dưới để demo đăng ký, OTP/magic link, email đặt/hủy lịch và reset mật khẩu đầy đủ. Các tài khoản/mật khẩu mẫu không được dùng với dữ liệu thật.

## Chạy demo đầy đủ bằng Docker Compose + Mailpit

Cần Docker Desktop với Linux containers. Tạo file env riêng:

```powershell
Copy-Item .env.example .env
```

Điền `MSSQL_SA_PASSWORD` đủ mạnh và `REMEMBER_ME_KEY` ngẫu nhiên trong `.env`, rồi:

```bash
docker compose up --build -d
```

- Ứng dụng: [localhost:8080](http://localhost:8080).
- Mailpit: [localhost:8025](http://localhost:8025), nhận email DEMO local; dùng để xem OTP/magic link, email đặt lịch kèm QR, hủy lịch và reset mật khẩu.
- SQL Server: `localhost:14333`, có thể đổi qua `SQLSERVER_HOST_PORT`.
- Các cổng host chỉ bind localhost. Volume giữ database và avatar; `docker compose down` giữ volume, `down -v` xóa dữ liệu demo.

Compose dùng `sqlserver,demo`, chạy init guard rồi Flyway V1–V6; Hibernate chỉ validate schema. Bank payment mặc định tắt. Không dùng Compose demo làm cấu hình production.

Giao diện dùng thư viện/font local, không cần CDN khi demo sau khi đã build. Cài dependency hoặc build Docker lần đầu vẫn cần Internet. Ảnh VietQR tùy chọn vẫn dùng dịch vụ bên ngoài.

## SQL Server và production

SQL Server là database chính có migration; H2 phục vụ phát triển và kiểm thử local.

[SQL Server runbook](docs/database/SQLSERVER_RUNBOOK.md) mô tả fresh DB, legacy baseline opt-in, backup/restore và SQL integration tests. [Provision admin](docs/database/PRODUCTION_ADMIN_PROVISIONING.md) mô tả tạo tài khoản quản trị khi không dùng seed.

Production chạy `sqlserver,prod`, không chạy cùng dev/demo. Dùng [.env.production.example](.env.production.example) làm danh sách biến tham khảo, nạp secret qua môi trường/secret manager; Spring Boot không tự đọc file `.env`. Cần URL SQL Server TLS hợp lệ, DB credentials, remember-me key, SMTP, HTTPS và `APP_WEBSITE` đúng origin. Không commit env đã điền, mật khẩu, uploads, backup hoặc log thô.

Mật khẩu mới: ít nhất 8 ký tự, tối đa 72 byte UTF-8 nếu dùng BCrypt; chữ có dấu/emoji có thể đạt giới hạn sớm hơn. Giữ định dạng hash hiện hữu; không tự đổi mật khẩu người dùng.

## Kiểm thử và phạm vi nghiệm thu

Kiểm tra source sau refactor ponytail: Maven `clean verify` ngày 06/10/2026 đạt với **230 test chạy thành công, 26 test SQL Server skipped**, frontend **26/26**; JaCoCo LINE **85,67%**, BRANCH **69,30%**. Ngày 07/10/2026 đã chạy bổ sung nhóm `SqlServer*` trên SQL Server 2022: **32/32 test đạt**, gồm toàn bộ **26 integration test** từng bị skip, không có failure/error/skipped. Database test và database migration được tạo riêng rồi dọn sạch sau khi chạy.

[Chế độ demo ngày 06/10/2026](docs/CLINICAL_DEMO_BACKEND_2026-10-06.md) giữ bằng chứng lịch sử trước refactor. Luồng ngày mai/ngày thứ 14 đã kiểm chứng qua HTTP MockMvc/H2 đến thu tiền và PDF. Browser E2E chưa được chạy lại sau refactor; cần build lại image trước khi demo.

Mốc browser trước thay đổi demo: [nghiệm thu sửa PW-01/PW-02 ngày 04/10/2026](docs/PLAYWRIGHT_FIX_ACCEPTANCE_2026-10-04.md), **154/154 assertion Playwright CLI đạt**, gồm 115 lượt trang/viewport/theme. Bản ngày 04/10 chặn tiếp nhận/khám sai ngày và sửa favicon 404, chạy ở **cổng 8080**; preview H2 18088 dùng image cũ để giữ dữ liệu của phiên đang mở. [Lượt trước sửa](docs/PLAYWRIGHT_CHECK_2026-10-04.md) giữ bằng chứng lịch sử 149/152.

```bash
mvn clean verify
node --test src/test/frontend/ui-regressions.cjs
node ops/check-doc-links.cjs
```

JaCoCo gate: LINE ≥70%, BRANCH ≥50%. Maven mặc định bỏ qua các case SQL Server cần environment guard. Để chạy chúng, dùng **database test disposable riêng** theo runbook, đặt `RUN_SQLSERVER_IT=true` và DB credentials; không dùng `medclinic_db` chứa dữ liệu cần giữ. Test migration còn tạo/drop `medclinic_flyway_it`.

Lượt nghiệm thu SQL Server + browser trước refactor: [sửa lỗi ngày khám và favicon 04/10/2026](docs/PLAYWRIGHT_FIX_ACCEPTANCE_2026-10-04.md), **242/242 Java test** khi bật SQL Server guard và **25/25 frontend test**. [Nghiệm thu đồ án 03/10](docs/ACADEMIC_ACCEPTANCE_2026-10-03.md) giữ mốc 218 test trước đó. [Bàn giao GitHub](docs/GITHUB_RELEASE_2026-10-03.md) và các mốc cũ giữ kết quả lịch sử của source lúc đó. CI chạy Maven clean verify, Node regression và kiểm tra link trên push main/feature/codex hoặc PR vào main; SQL Server integration cần môi trường riêng nên không tự chạy trên hosted CI mặc định.

Nghiệm thu local đã có camera thật do người dùng xác nhận, SMTP Mailpit, backup/restore, workflow H2/SQL Server và 16 task UX/UI. Ngân hàng/SMTP nhà cung cấp thật, HTTPS production và mọi tổ hợp thiết bị/trình duyệt chưa được xác nhận. Không suy diễn local pass thành chứng nhận WCAG hoặc sẵn sàng triển khai y tế thực tế.

## Tài liệu

- [Đặc tả yêu cầu](docs/01_SRS_IEEE830.md), [UML](docs/02_UML_DESIGN.md), [thiết kế dữ liệu](docs/03_DATABASE_DESIGN_3NF.md), [kiến trúc](docs/05_SYSTEM_ARCHITECTURE.md).
- [Nghiệm thu đồ án bản cuối](docs/ACADEMIC_ACCEPTANCE_2026-10-03.md).
- [Kiểm tra Playwright CLI trước sửa](docs/PLAYWRIGHT_CHECK_2026-10-04.md), [nghiệm thu hai bản sửa](docs/PLAYWRIGHT_FIX_ACCEPTANCE_2026-10-04.md).
- [UX/UI tasklist](docs/UX_UI_TASKLIST_2026-10-02.md), [nghiệm thu UX-10](docs/UX10_ACCEPTANCE_2026-10-03.md).
- [Nghiệm thu backend](docs/BACKEND_ACCEPTANCE_2026-10-02.md), [đối chiếu backlog local](docs/BACKLOG_COMPLETION_2026-10-02.md).
- [7 mục chuẩn bị GitHub](docs/GITHUB_READINESS_TASKLIST_2026-10-03.md), [bàn giao source cuối](docs/GITHUB_RELEASE_2026-10-03.md).
- [Danh sách chuẩn bị staging, SMTP và sandbox ngân hàng](docs/INTEGRATION_PREPARATION_2026-10-03.md).
- [Chính sách bằng chứng công khai](docs/evidence/README.md). Tài liệu theo ngày giữ bối cảnh lịch sử; raw log, OTP/magic link và khung hình webcam cá nhân được giữ local.

## Đóng góp và giấy phép

Tạo nhánh riêng, chạy verification và mở PR với mô tả hành vi, phạm vi thay đổi và kết quả kiểm thử. Không gửi secret hoặc dữ liệu bệnh nhân thật trong issue/PR.

Dự án dùng [MIT License](LICENSE). Giữ license của font, Chart.js và page mascot trong thư mục nguồn tương ứng. Cảm ơn các dự án Spring, Bootstrap, OpenPDF, ZXing và các thư viện mã nguồn mở được sử dụng. Đây là đồ án môn học Lập Trình Web và Công Nghệ Phần Mềm.
