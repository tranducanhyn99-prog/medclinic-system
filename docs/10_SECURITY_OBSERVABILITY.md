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
- CSRF vẫn được bật cho browser workflow; chỉ H2 console và SockJS transport `/ws/**` dùng ngoại lệ hiện có.

## 2. Rate limiting cho endpoint nhạy cảm

`SensitiveEndpointRateLimitFilter` giới hạn request theo `remoteAddr + endpoint` trong từng JVM:

| Endpoint POST | Mặc định | Biến môi trường |
| --- | ---: | --- |
| `/login` | 20 request / 300 giây | `LOGIN_RATE_LIMIT_MAX` |
| `/forgot-password` | 5 request / 300 giây | `PASSWORD_RESET_RATE_LIMIT_MAX` |
| `/reset-password` | 5 request / 300 giây | `PASSWORD_RESET_RATE_LIMIT_MAX` |

Cửa sổ thời gian dùng `AUTH_RATE_LIMIT_WINDOW_SECONDS` (mặc định 300). Khi vượt ngưỡng, server trả **HTTP 429** và header `Retry-After`.

Rate limiter hiện là baseline cho deployment một instance. Nó cố ý dùng `request.getRemoteAddr()` thay vì tin trực tiếp `X-Forwarded-For`. Khi deploy sau reverse proxy, chỉ cấu hình forwarded-header trust khi proxy đó được kiểm soát. Nếu scale nhiều instance, cần chuyển enforcement sang gateway/WAF hoặc shared store như Redis để tránh mỗi JVM có một bucket riêng.

## 3. Security audit

`SecurityAuditService` ghi log dạng key=value cho các event:

- `AUTH_SUCCESS`
- `AUTH_FAILURE`
- `LOGOUT`
- `RATE_LIMIT_REJECTED`

Log có principal/remote IP/reason phù hợp nhưng **không ghi password, remember-me key, reset token, DB credential hoặc SMTP secret**. Principal được sanitize CR/LF/TAB trước khi ghi để giảm log-injection.

Đồng thời Micrometer ghi hai metric custom, không dùng username làm tag để tránh high-cardinality:

```text
medclinic.security.authentication{outcome=success|failure|logout}
medclinic.security.rate_limit.rejected{endpoint=/login|/forgot-password|/reset-password}
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

- `SensitiveEndpointRateLimitFilterTest`: kiểm ngưỡng 429, `Retry-After`, reset cửa sổ và bucket độc lập.
- `SecurityAuditServiceTest`: kiểm custom Micrometer counters.
- `SecurityHardeningIntegrationTest`: kiểm security headers, public health, ADMIN-only metrics và audit login/logout.
- `SecurityObservabilityConfigurationTest`: khóa contract cho error detail, cookie policy, Actuator exposure và Docker local override.
- Full clean baseline ngày 20/09/2026: **128 test case / 55 test class; 120 executed PASS, 8 environment-gated skipped, 0 failure, 0 error**.
- JaCoCo: **78,52% line / 64,22% branch**, vượt quality gate 70% / 50%.

## 6. Giới hạn và bước production bên ngoài source code

- Rate limit in-memory không thay WAF/gateway và không đồng bộ giữa nhiều instance.
- Chưa áp CSP enforcement cứng vì UI hiện còn inline script/style; bật CSP nghiêm ngặt trước khi refactor nonce/hash có thể làm vỡ giao diện. Referrer/Permissions policy đã được áp dụng an toàn trước.
- Production phải chạy HTTPS và giữ `SESSION_COOKIE_SECURE=true`.
- Secret manager, log aggregation/SIEM, alert rule, retention policy và credential rotation thuộc hạ tầng deployment thực tế.
- SMTP production, screen-reader release smoke và DBA deployment approval vẫn là external acceptance item như tài liệu production-readiness đã nêu.
