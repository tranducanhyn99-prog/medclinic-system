# Chuẩn bị môi trường và nghiệm thu tích hợp MedClinic

**Phạm vi:** hướng phát triển triển khai thật, không phải điều kiện hoàn thành đồ án. Docker + Mailpit và callback mô phỏng là môi trường nghiệm thu môn học.

Ngày: 03/10/2026. Người dùng xác nhận có staging, SMTP và sandbox ngân hàng; thông tin kết nối cụ thể và nghiệm thu các dịch vụ đó chưa được xác minh.

Tài liệu này liệt kê đầu vào cần có để tiếp tục các task tích hợp. Đối chiếu với [application.yml](../src/main/resources/application.yml), [mẫu production](../.env.production.example), [SQL Server runbook](database/SQLSERVER_RUNBOOK.md) và [bảo mật/vận hành](10_SECURITY_OBSERVABILITY.md). Không điền secret vào tài liệu này hoặc commit file env đã điền.

## 1. Staging — triển khai và kiểm tra toàn bộ luồng

| Cần chuẩn bị | Mục đích / yêu cầu |
|---|---|
| URL HTTPS staging và tên nền tảng/host | Truy cập ứng dụng, xác minh DNS, TLS, cookie và đường dẫn trong email. |
| Cách triển khai và truy cập được phép | Nêu Docker, JAR hoặc pipeline; cách truy cập console/SSH/deployment và nơi xem log. Credential lưu riêng. Runtime của dự án dùng Java 21. |
| SQL Server staging riêng | Có host, port, tên database, TLS certificate/trust chain và tài khoản ứng dụng. Tách dữ liệu nghiệm thu khỏi dữ liệu thật. |
| Quyền migration và tài khoản ứng dụng | Xác định ai chạy Flyway V1–V6; tài khoản ứng dụng không dùng `sa`. Kiểm tra quyền migration và quyền vận hành theo [SQL Server runbook](database/SQLSERVER_RUNBOOK.md). |
| Reverse proxy | HTTPS, forwarded headers đúng origin, WebSocket; app chỉ nhận forwarded headers từ proxy được tin cậy. Có [mẫu Nginx](../deploy/nginx/medclinic.conf.example). |
| Lưu trữ avatar bền vững | Đường dẫn mount/volume, quyền ghi, khả năng giữ file sau restart/redeploy. Hiện code lưu avatar trên filesystem. |
| Secret/config store | Nêu tên secret manager, cấu hình deployment hoặc đường dẫn file riêng; cách nạp vào process/container. Spring Boot không tự đọc `.env`. |
| Tài khoản nghiệm thu bốn vai trò | Admin, bác sĩ, lễ tân, bệnh nhân với dữ liệu giả. Production không seed tài khoản demo; [hướng dẫn provision admin](database/PRODUCTION_ADMIN_PROVISIONING.md). |
| Backup và giám sát | Nơi lưu DB + uploads, lịch backup, thời gian khôi phục mong muốn, nơi nhận cảnh báo. Restore drill dùng database tạm riêng. |
| Phạm vi cho phép kiểm tra | Quyền restart/redeploy, tạo dữ liệu giả, gửi email thử, chạy tải và tạo/xóa database diễn tập; khung giờ phù hợp. |

Biến cấu hình cần chuẩn bị:

```text
SPRING_PROFILES_ACTIVE=sqlserver,prod
DB_URL=<JDBC SQL Server với encrypt=true;trustServerCertificate=false>
DB_USERNAME=<tài khoản ứng dụng riêng>
DB_PASSWORD=<secret>
REMEMBER_ME_KEY=<secret ngẫu nhiên riêng cho môi trường>
APP_WEBSITE=<origin HTTPS staging>
SESSION_COOKIE_SECURE=true
AUTH_RATE_LIMIT_STORE=jdbc
AVATAR_STORAGE_DIR=<thư mục persistent đã mount>
```

Không chạy `prod` cùng `dev`/`demo`. Compose hiện tại là cấu hình demo; cần deployment staging riêng. Kiểm tra health/probes theo phân quyền hiện có, không mở metrics công khai chỉ để nghiệm thu.

Điều kiện chốt: migration và health đạt; đăng nhập/phân quyền bốn vai trò đạt; secure cookie/proxy/WebSocket đúng; avatar tồn tại sau redeploy; backup DB + uploads và restore drill có bằng chứng.

## 2. SMTP — OTP, magic link, đặt/hủy lịch và khôi phục mật khẩu

| Cần chuẩn bị | Mục đích / yêu cầu |
|---|---|
| Tên nhà cung cấp, tài liệu SMTP | Xác định cách xác thực và chính sách gửi. |
| Host, port, chế độ TLS | Repo mặc định port 587 + STARTTLS. Nếu nhà cung cấp dùng cấu hình khác, cần điều chỉnh theo tài liệu của họ. |
| Tài khoản SMTP và vị trí secret | Username và password/app-password/credential được lưu ngoài Git. Nếu chỉ có API gửi email, cần adapter thay cho SMTP hiện tại. |
| Địa chỉ `From` được xác minh | Địa chỉ/tên người gửi; nếu gửi bằng domain riêng, cung cấp trạng thái xác minh domain và cấu hình DNS mà nhà cung cấp yêu cầu. |
| Hộp thư được phép nhận thử | Một hoặc vài địa chỉ do bạn chỉ định; nêu có cho phép gửi thử các loại email trong bảng dưới không. |
| Hạn mức, dashboard delivery/bounce | Tránh vượt quota; đối chiếu kết quả SMTP với email thực nhận và cảnh báo lỗi gửi. |
| URL staging | `APP_WEBSITE` phải đúng để magic link/reset link mở được từ hộp thư. |

Biến hiện có:

```text
SPRING_MAIL_HOST=<host SMTP>
SPRING_MAIL_PORT=<port nhà cung cấp>
MAIL_USERNAME=<username>
MAIL_PASSWORD=<secret>
APP_WEBSITE=<origin HTTPS staging>
```

**Cập nhật code:** `EmailServiceImpl` đã dùng `MAIL_FROM` (mặc định `no-reply@medclinic.test` cho demo). Khi triển khai thật, đặt sender được nhà cung cấp chấp nhận; `app.email` vẫn chỉ là thông tin liên hệ. Kết nối SMTP thành công chưa chứng minh email tới inbox.

| Chức năng | Nghiệm thu |
|---|---|
| OTP đăng ký/đăng nhập | Email tới hộp thư, mã dùng đúng một lần, hết hạn bị từ chối. |
| Magic link | Link HTTPS đúng staging; dùng một lần, hết hạn bị từ chối. |
| Đặt lịch | Email chứa đúng mã lịch, bác sĩ, ngày và khung giờ của ca giả. |
| Hủy lịch | Email xác nhận đúng ca giả đã hủy. |
| Khôi phục mật khẩu | Link đúng staging, dùng một lần và hết hạn bị từ chối; chỉ đổi mật khẩu tài khoản thử được cho phép. |
| Lỗi gửi | Retry có giới hạn và metrics/log ghi nhận; không đưa OTP/link/credential vào bằng chứng công khai. |

## 3. Ngân hàng — VietQR và xác nhận thanh toán sandbox

| Cần chuẩn bị | Mục đích / yêu cầu |
|---|---|
| Tên ngân hàng/cổng thanh toán và tài liệu | Cần đúng sản phẩm sandbox, endpoint, quy trình tạo giao dịch và webhook. |
| Tài khoản sandbox/merchant | Mã merchant và thông tin tài khoản thụ hưởng thử; xác nhận giao dịch là mô phỏng, không thu tiền thật. |
| Cách tạo giao dịch thử | API, dashboard hoặc công cụ giả lập callback chính thức; mẫu payload đã loại bỏ secret. |
| Chữ ký webhook | Thuật toán, chuỗi được ký, header, thời gian/timestamp, retry và event/transaction ID. Credential/API key/chứng thư lưu ngoài Git. |
| URL callback public HTTPS | Nhà cung cấp phải gọi tới được staging; đăng ký URL/allowlist nếu hợp đồng yêu cầu. |
| Đối soát | Dashboard hoặc API cho biết giao dịch thật sự thành công; quy tắc tham chiếu hóa đơn và đơn vị tiền tệ VND. |

Biến hiện có cho luồng VietQR:

```text
BANK_PAYMENT_ENABLED=false
BANK_PAYMENT_BANK_ID=<mã ngân hàng>
BANK_PAYMENT_BANK_NAME=<tên ngân hàng>
BANK_PAYMENT_ACCOUNT_NO=<tài khoản thụ hưởng thử>
BANK_PAYMENT_ACCOUNT_NAME=<tên chủ tài khoản>
BANK_PAYMENT_QR_TEMPLATE=compact2
BANK_PAYMENT_CALLBACK_SECRET=<secret HMAC riêng, ít nhất 32 ký tự>
BANK_PAYMENT_EXPIRY_MINUTES=30
```

Giữ tắt cho tới khi cấu hình và đường tích hợp được xác minh; chỉ bật trên môi trường nghiệm thu được phép.

**Hiện trạng code:** tạo ảnh QR qua `img.vietqr.io`; nhận `POST /api/payments/bank/callback` với header `X-MedClinic-Signature` và các trường `eventId`, `transactionId`, `transferCode`, `amount`, `occurredAt`. Chữ ký HMAC SHA-256 dựa trên chuỗi canonical riêng của MedClinic. Repo chưa có adapter nhà cung cấp cụ thể hay biến endpoint/API key cho từng nhà cung cấp. Secret HMAC này không mặc nhiên là credential của ngân hàng.

**Phần cần bổ sung:** đối chiếu contract nhà cung cấp; nếu khác, viết adapter xác minh chữ ký gốc, ánh xạ payload và xử lý callback theo contract trước khi gọi luồng đối soát của ứng dụng. Không chỉ đổi tên header hoặc ký lại một payload chưa được xác thực.

Điều kiện chốt: QR không tự đánh dấu hóa đơn đã trả; callback hợp lệ/số tiền đúng xác nhận đúng một hóa đơn; gửi lại callback không ghi nhận hai lần; sai chữ ký, sai tiền, sai tham chiếu, giao dịch trùng và hết hạn được xử lý đúng. Kiểm tra cả callback tới muộn và đối soát thủ công bằng tài khoản lễ tân thử.

## 4. Hiệu năng và vận hành — nghiệm thu trên staging

| Cần chuẩn bị | Mục đích / yêu cầu |
|---|---|
| Cấu hình staging | CPU/RAM, số instance, DB, mạng, connection pool và vị trí chạy công cụ tải để đọc kết quả đúng bối cảnh. |
| Dữ liệu giả đại diện | Số bệnh nhân, lịch hẹn, thuốc, bệnh án/hóa đơn; ngày khám và tài khoản thử. Không tải dữ liệu bệnh nhân thật vào fixture. |
| Mức tải và mục tiêu | Số người dùng đồng thời, tỷ lệ các thao tác, thời lượng và mục tiêu p95/tỷ lệ lỗi cho từng luồng. Đối chiếu [kế hoạch hiệu năng](performance/TEST_PLAN.md). |
| Quyền và giới hạn chạy tải | Khung giờ, giới hạn request; chỉ thực hiện thao tác ghi trên dữ liệu thử được phép. SMTP/webhook dưới tải phải dùng sink/sandbox phù hợp hạn mức. |
| Quan sát hệ thống | Quyền xem CPU/RAM, DB connections/locks, lỗi, health và email/payment metrics. |

Các luồng cần đo: đặt lịch, tiếp nhận, bàn khám/lưu ca giả, thu ngân/callback sandbox, xuất PDF và gửi email. Kết quả H2 local không thay thế nghiệm thu staging; ghi rõ dữ liệu, tải, môi trường, p95, tỷ lệ lỗi và tài nguyên.

## 5. Camera, responsive và accessibility — thiết bị thật

Cần webcam + quyền camera, QR của lịch hẹn thử, desktop và điện thoại; ghi tên thiết bị, hệ điều hành, trình duyệt/phiên bản. Kiểm tra light/dark, touch, bàn phím, zoom 200%, giảm chuyển động và Narrator/NVDA. Camera staging cần secure context HTTPS hoặc localhost.

Đã có xác nhận webcam quét tự động local và phản hồi nghiệm thu UX trước đây; chưa suy ra mọi thiết bị/trình duyệt đều đạt. Lần nghiệm thu staging cần ghi cụ thể tổ hợp đã thử và lỗi nếu có.

## 6. Mẫu trả lời tối thiểu

Bạn có thể trả lời phần thông tin không nhạy cảm theo mẫu này, để trống phần chưa biết:

```text
Staging: URL / nền tảng / cách deploy và truy cập được phép
SQL Server: host / port / database staging / cách nạp credential
Uploads và backup: nơi mount / nơi lưu / quyền restore vào DB tạm
SMTP: nhà cung cấp / host / port / TLS / sender đã xác minh
Email nhận thử: địa chỉ được phép nhận / các loại email được phép thử
Ngân hàng: nhà cung cấp + sản phẩm / link tài liệu sandbox
Sandbox: cách tạo giao dịch mô phỏng / nơi cấu hình webhook
Secret: tên secret store hoặc đường dẫn cấu hình / cách nạp; không gửi giá trị
Phạm vi test: tài khoản/dữ liệu giả được phép / restart, ghi dữ liệu, tải / khung giờ
Thiết bị: hệ điều hành / trình duyệt + phiên bản / webcam, điện thoại, screen reader
```

Thông tin tối thiểu để bắt đầu tích hợp: URL/cách truy cập staging, tài liệu SMTP + sender + người nhận thử, tài liệu ngân hàng + quyền dùng sandbox, và vị trí/cách truy cập secret. Khi có những đầu vào này mới xác định adapter/config cụ thể và thực hiện nghiệm thu dịch vụ thật.
