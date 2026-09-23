# P2 — Production Readiness và thanh toán ngân hàng

**Ngày thực hiện:** 23/09/2026

**Phạm vi:** T09, T10, T11

**Database:** SQL Server + Flyway V1–V6, 21 bảng ứng dụng

## 1. Kết quả triển khai

### T09 — Vận hành dữ liệu

- `ops/backup.ps1` tạo SQL Server `COPY_ONLY` backup có checksum/`RESTORE VERIFYONLY`, sao chép uploads và ghi hash SHA-256.
- `ops/restore-drill.ps1` restore file `.bak` vào database tạm, chạy `DBCC CHECKDB`, kiểm tra số bảng tối thiểu rồi xóa database diễn tập.
- Profile production phải là `sqlserver,prod`; `ProductionProfileGuard` từ chối chạy `prod` cùng `dev`/`demo`, do đó không seed tài khoản `123456`.
- [`.env.production.example`](../.env.production.example) chỉ chứa placeholder; secret thật phải ở secret manager hoặc cấu hình deployment bên ngoài Git.
- [`deploy/nginx/medclinic.conf.example`](../deploy/nginx/medclinic.conf.example) cung cấp redirect HTTPS, TLS 1.2/1.3, forwarded headers và WebSocket proxy. App dùng secure session cookie trong profile `prod`.
- SQL data và uploads phải được backup cùng một recovery point. Không dùng `docker compose down -v` trong vận hành.

Chạy backup và restore drill:

```powershell
.\ops\backup.ps1
$latest = Get-ChildItem .\backups -Recurse -Filter *.bak | Sort-Object LastWriteTime -Descending | Select-Object -First 1
.\ops\restore-drill.ps1 -BackupFile $latest.FullName
```

Chính sách tối thiểu:

| Dữ liệu | Retention đề xuất | Cách xử lý |
|---|---:|---|
| Backup hằng ngày | 30 ngày | Mã hóa, lưu ngoài host chạy app, kiểm tra checksum |
| Backup tuần/tháng | 12 tuần / 12 tháng | Immutable/offsite nếu dùng dữ liệu thật |
| OTP, magic link, reset token hết hạn | 7 ngày | `SecurityDataRetentionJob` xóa tự động |
| Rate-limit bucket | 1 ngày | Xóa tự động |
| Business/payment audit | 1.825 ngày (5 năm) | Xóa tự động theo cấu hình; production phải đối chiếu yêu cầu kế toán/pháp lý trước khi giảm |
| Hồ sơ bệnh án/hóa đơn | Theo chính sách pháp lý của đơn vị vận hành | Không tự động xóa; yêu cầu duyệt và audit riêng |
| Avatar | Theo vòng đời tài khoản | File cũ bị xóa sau thay ảnh; backup/restore cùng database |

### T10 — Bảo vệ và giám sát

- `JdbcSqlServerRateLimitStore` dùng transaction `SERIALIZABLE` cùng `UPDLOCK/HOLDLOCK`; tất cả app instance dùng chung bảng `security_rate_limit_buckets`.
- Callback thanh toán có bucket riêng, mặc định 60 request/5 phút/IP; cấu hình bằng `PAYMENT_CALLBACK_RATE_LIMIT_MAX`.
- Bucket key là SHA-256 của IP + endpoint. Security log chỉ ghi hash principal/IP.
- Email thử lại tối đa 3 lần với backoff hữu hạn. Metrics:
  - `medclinic.email.delivery{type,outcome}`
  - `medclinic.email.consecutive_failures`
  - `medclinic.security.rate_limit.rejected{endpoint}`
  - `medclinic.retention.deleted{type}`
- Cảnh báo hạ tầng đề xuất:
  - `medclinic.email.consecutive_failures >= 3` trong 10 phút;
  - rate-limit rejection tăng đột biến;
  - health khác `UP`, container restart hoặc DB connection failure;
  - payment callback `REJECTED` liên tiếp hoặc callback không đến trước khi request hết hạn.
- OTP/reset/magic token chỉ lưu hash. Log email dùng recipient hash; audit payment không lưu raw callback, số tài khoản hay secret.

### T11 — VietQR tới tài khoản ngân hàng, ví dụ MB Bank

Luồng đã triển khai:

1. Lễ tân bấm **Tạo VietQR** trên hóa đơn `UNPAID`.
2. Server tạo `bank_transfer_payments` ở trạng thái `PENDING`, amount snapshot và transfer code duy nhất.
3. Trang web hiển thị VietQR có bank id, số tài khoản, số tiền và nội dung. JavaScript poll trạng thái, không tự đánh dấu đã trả tiền.
4. Callback `POST /api/payments/bank/callback` gửi JSON và header `X-MedClinic-Signature`.
5. Server kiểm tra HMAC constant-time, event id, bank transaction id, transfer code, amount, expiry và trạng thái hóa đơn trong transaction có lock.
6. Chỉ khi hợp lệ, payment thành `CONFIRMED`, invoice thành `PAID/BANK_TRANSFER` và audit event được ghi.
7. Nếu nhà cung cấp callback chưa sẵn sàng, lễ tân có thể nhập mã giao dịch sau khi kiểm tra tài khoản ngân hàng. Mã giao dịch vẫn phải duy nhất và được audit.

Canonical payload để ký HMAC SHA-256:

```text
eventId|transactionId|transferCode|amount|occurredAt
```

`amount` bỏ số 0 thập phân không cần thiết; `occurredAt` dùng ISO-8601 hoặc chuỗi rỗng. Không log hoặc gửi callback secret xuống browser.

Biến môi trường:

```text
BANK_PAYMENT_ENABLED=true
BANK_PAYMENT_BANK_ID=970422
BANK_PAYMENT_BANK_NAME=MB Bank
BANK_PAYMENT_ACCOUNT_NO=<tai-khoan-thu-huong>
BANK_PAYMENT_ACCOUNT_NAME=<TEN CHU TAI KHOAN>
BANK_PAYMENT_CALLBACK_SECRET=<random-secret-toi-thieu-32-ky-tu>
BANK_PAYMENT_EXPIRY_MINUTES=30
PAYMENT_CALLBACK_RATE_LIMIT_MAX=60
```

MB có cổng developer và sản phẩm VietQR Paygate/MMS, nhưng API/callback trực tiếp chỉ dành cho đối tác đã đăng ký. Vì project chưa có merchant credential thật, implementation hiện tại dùng VietQR tới tài khoản cấu hình và callback HMAC trung gian. Khi được MB cấp contract chính thức, viết adapter ánh xạ checksum/payload MB vào `BankTransferPaymentService`; không thay đổi invariant đối soát của domain.

Tài liệu tham chiếu:

- [MB for Developers](https://developer.mbbank.com.vn/)
- [VietQR Quick Link](https://vietqr.io/danh-sach-api/link-tao-ma-nhanh/)
- [VietQR generate API](https://www.vietqr.io/danh-sach-api/link-tao-ma-nhanh/api-tao-ma-qr/)

## 2. Migration V6

V6 thêm ba bảng:

- `bank_transfer_payments`
- `business_audit_events`
- `security_rate_limit_buckets`

Đồng thời constraint `invoices.payment_method` cho phép `BANK_TRANSFER`. `VNPAY` chỉ được giữ để đọc dữ liệu legacy; UI và service không tạo giao dịch VNPAY mới.

## 3. Điều kiện bật với dữ liệu thật

- Có tài khoản doanh nghiệp/merchant và văn bản cho phép thu hộ; không dùng tài khoản cá nhân của thành viên dự án.
- Callback phải đi qua HTTPS public, secret riêng cho từng môi trường, có rotation và allowlist/gateway nếu nhà cung cấp hỗ trợ.
- Chạy full test, Flyway acceptance, backup và restore drill trước deploy.
- Dùng login SQL riêng có quyền tối thiểu; không dùng `sa`.
- SMTP production đã xác minh outbound delivery và alert failure.
- Gắn persistent storage/object storage cho uploads và đưa cả DB + uploads vào kế hoạch khôi phục.
- Chạy performance acceptance trong chính môi trường triển khai; baseline H2/MockMvc không đại diện production.

## 4. Giới hạn thực tế

- Quick Link VietQR tạo QR thanh toán nhưng không tự chứng minh tiền đã về. Bằng chứng thanh toán đến từ callback provider/MB hoặc đối soát thủ công.
- Chưa thể nghiệm thu callback MB thật nếu chưa có merchant credential và endpoint sandbox/production được MB cấp.
- Nginx file là mẫu cấu hình; chứng thư, DNS, WAF, SIEM và secret manager thuộc môi trường triển khai.
- Retention hồ sơ y tế cần quyết định pháp lý của đơn vị vận hành trước khi tự động hóa.

## 5. Bằng chứng nghiệm thu ngày 23/09/2026

- `mvn clean verify`: PASS, 147 test cases; 138 chạy, 9 SQL environment-gated bỏ qua, 0 lỗi; JaCoCo 76,51% line / 59,61% branch, vượt gate 70% / 50%.
- `RUN_SQLSERVER_IT=true mvn test`: PASS bộ 145 test trước khi bổ sung hai test rate limit cuối; 0 skipped. Hai test bổ sung cho callback rate limit và `SqlServerSharedRateLimitIntegrationTest` đều PASS riêng, trong đó test bucket chạy trực tiếp trên SQL Server.
- Flyway acceptance: database mới chạy V1–V6; schema legacy baseline V1 nâng V2–V6; cả hai có 21 bảng ứng dụng.
- Docker Compose: `medclinic-app`, SQL Server 2022 và Mailpit đều healthy; app log xác nhận Hibernate schema validation và migration V6 thành công.
- Backup sau V6: `backups/20260923_215941`; `RESTORE VERIFYONLY` PASS. Restore drill vào database tạm `medclinic_restore_drill_20260923215943`, `DBCC CHECKDB` và kiểm tra bảng PASS; database tạm đã được xóa.
- Payment service tests xác nhận QR không tự đánh dấu hóa đơn đã trả, callback sai chữ ký/sai số tiền bị từ chối, callback hợp lệ idempotent và đối soát thủ công được audit.

Callback MB thật chưa nằm trong bằng chứng trên vì project chưa có merchant credential và endpoint sandbox do MB cấp. Cấu hình mặc định giữ `BANK_PAYMENT_ENABLED=false` để tránh hiển thị QR giả trước khi có tài khoản thụ hưởng và secret thật.
