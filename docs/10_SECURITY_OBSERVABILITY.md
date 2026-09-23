# Security & Observability Baseline

Tài liệu này mô tả các control bảo mật và khả năng quan sát vận hành đã triển khai trong MedClinic. Phạm vi hiện tại tập trung vào web monolith Spring Boot chạy session-based authentication; đây không phải là bộ control thay thế WAF/SIEM hoặc gateway của môi trường production.

## 1. Session, cookie và response hardening

- Spring Security chống session fixation bằng `migrateSession` sau đăng nhập thành công.
- Session timeout mặc định: **30 phút**.
- `JSESSIONID`: `HttpOnly=true`, `SameSite=Lax`.
- Profile `sqlserver`: `Secure=true` mặc định cho session cookie; remember-me cookie dùng cùng policy.
- Docker local/demo chạy HTTP nên `.env` mặc định `SESSION_COOKIE_SECURE=false`; production HTTPS không được dùng opt-out này.
- Error response không expose message, binding error hoặc stack trace.
- Response thêm `Referrer-Policy: strict-origin-when-cross-origin`.
- `Permissions-Policy` tắt camera, microphone và geolocation vì MedClinic hiện không sử dụng các capability này.
- CSRF vẫn được bật cho browser workflow; chỉ H2 console, SockJS transport `/ws/**` và callback bank server-to-server `/api/payments/bank/callback` dùng ngoại lệ. Callback bank bắt buộc HMAC SHA-256.

## 2. Rate limiting cho endpoint nhạy cảm

`SensitiveEndpointRateLimitFilter` giới hạn request theo `remoteAddr + endpoint`:

| Endpoint POST | Mặc định | Biến môi trường |
| --- | ---: | --- |
| `/auth/login` | 20 request / 300 giây | `LOGIN_RATE_LIMIT_MAX` |
| `/auth/otp`, `/auth/otp/resend` | 5 request / 300 giây | `PASSWORD_RESET_RATE_LIMIT_MAX` |
| `/forgot-password` | 5 request / 300 giây | `PASSWORD_RESET_RATE_LIMIT_MAX` |
| `/reset-password` | 5 request / 300 giây | `PASSWORD_RESET_RATE_LIMIT_MAX` |
| `/api/payments/bank/callback` | 60 request / 300 giây | `PAYMENT_CALLBACK_RATE_LIMIT_MAX` |

Cửa sổ thời gian dùng `AUTH_RATE_LIMIT_WINDOW_SECONDS` (mặc định 300). Khi vượt ngưỡng, server trả **HTTP 429** và header `Retry-After`.

Profile `dev` dùng in-memory store. Profile `sqlserver` mặc định dùng `JdbcSqlServerRateLimitStore`: khóa bucket là SHA-256 của IP + endpoint, cập nhật trong transaction `SERIALIZABLE` với `UPDLOCK/HOLDLOCK`, nên nhiều instance cùng nhìn một counter. Ứng dụng vẫn dùng địa chỉ đã được Spring xử lý; chỉ bật forwarded headers sau reverse proxy do đơn vị triển khai kiểm soát.

## 3. Security audit

`SecurityAuditService` ghi log dạng key=value cho các event:

- `AUTH_SUCCESS`
- `AUTH_FAILURE`
- `LOGOUT`
- `RATE_LIMIT_REJECTED`

Log chỉ ghi hash rút gọn của principal/remote IP và reason đã sanitize; **không ghi password, email đầy đủ, remember-me key, OTP, reset/magic token, DB credential, bank callback secret hoặc SMTP secret**.

Audit nghiệp vụ bền vững nằm trong `business_audit_events`: thu tiền mặt, tạo yêu cầu VietQR, callback thành công/từ chối và đối soát thủ công. Bảng chỉ giữ mã sự kiện/thực thể và detail code, không lưu raw callback hay số tài khoản.

Đồng thời Micrometer ghi các metric custom, không dùng username làm tag để tránh high-cardinality:

```text
medclinic.security.authentication{outcome=success|failure|logout}
medclinic.security.rate_limit.rejected{endpoint=/login|/forgot-password|/reset-password|/api/payments/bank/callback}
medclinic.email.delivery{type=...,outcome=success|retry|failure}
medclinic.email.consecutive_failures
medclinic.retention.deleted{type=...}
```

## 4. Actuator access model

Project chỉ expose hai Actuator endpoint qua HTTP:

| Endpoint | Quyền | Dữ liệu |
| --- | --- | --- |
| `/actuator/health` | Anonymous | Chỉ `status`; không show component/details |
| `/actuator/metrics/**` | `ROLE_ADMIN` | JVM, HTTP và custom Micrometer metrics |

Mail health contributor bị tắt vì SMTP là integration tùy môi trường và lỗi SMTP không được phép làm core application health chuyển DOWN.

Ví dụ kiểm tra:

```bash
curl -i http://localhost:8080/actuator/health
```

Kỳ vọng unauthenticated: HTTP 200 và body tương đương `{"status":"UP"}`. `/actuator/metrics` phải redirect tới login nếu chưa xác thực; tài khoản ADMIN mới được đọc metrics.

## 5. Verification tự động

- `SensitiveEndpointRateLimitFilterTest`: kiểm ngưỡng 429, `Retry-After`, reset cửa sổ, bucket độc lập và giới hạn riêng cho callback thanh toán.
- `SqlServerSharedRateLimitIntegrationTest`: kiểm store production thật sự dùng SQL Server và cùng bucket bị chặn sau khi vượt ngưỡng.
- `SecurityAuditServiceTest`: kiểm custom Micrometer counters.
- `SecurityHardeningIntegrationTest`: kiểm security headers, public health, ADMIN-only metrics và audit login/logout.
- `SecurityObservabilityConfigurationTest`: khóa contract cho error detail, cookie policy, Actuator exposure và Docker local override.
- Full clean baseline ngày 23/09/2026: **147 test case; 138 executed PASS, 9 SQL environment-gated skipped, 0 failure, 0 error**.
- JaCoCo: **76,51% line / 59,61% branch**, vượt quality gate 70% / 50%.

## 6. Giới hạn và bước production bên ngoài source code

- Shared SQL rate limit bảo vệ ở tầng ứng dụng nhưng không thay WAF/gateway trước các đợt DDoS lớn.
- Chưa áp CSP enforcement cứng vì UI hiện còn inline script/style; bật CSP nghiêm ngặt trước khi refactor nonce/hash có thể làm vỡ giao diện. Referrer/Permissions policy đã được áp dụng an toàn trước.
- Production phải chạy HTTPS và giữ `SESSION_COOKIE_SECURE=true`.
- Secret manager, log aggregation/SIEM, alert rule và credential rotation phải được nối vào hạ tầng triển khai; source đã cung cấp metric và retention job để tích hợp.
- SMTP production, merchant onboarding MB, screen-reader release smoke và DBA deployment approval vẫn là external acceptance item.
