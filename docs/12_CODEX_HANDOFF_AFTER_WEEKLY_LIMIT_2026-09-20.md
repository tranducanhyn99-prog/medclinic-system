# CODEX HANDOFF — Công việc hoàn thành sau khi Codex hết weekly limit

**Dự án:** MedClinic - Phòng Khám Tư Nhân
**Ngày tạo:** 20/09/2026
**Cập nhật gần nhất:** 23/09/2026
**Mục đích:** Đây là file handoff cho Codex. Hãy đọc file này trước khi tiếp tục bất kỳ task backend/database/security/Docker nào.

## 0. Source of truth hiện tại

- Repository: `https://github.com/tranducanhyn99-prog/medclinic-system.git`
- Branch chính trên remote vẫn là `main`, hiện ở `9cf7520` (commit tạo file handoff này).
- Branch assistant tiếp tục làm việc sau handoff: `feature/frontend-design-system`.
- HEAD hiện tại của branch tiếp tục: `085464fca801929394fdd4f139a0bc33a30a93a2`; `origin/feature/frontend-design-system` cùng commit.
- Functional baseline ban đầu được handoff: `016b1e0d17f8b54dce07802a9070139ece2d5aae`.
- Working tree trước khi cập nhật tài liệu này: clean; sau lần sửa này chỉ file handoff này được modified nếu chưa commit.
- Mốc trước khi assistant tiếp quản trực tiếp từ Codex: `2f38cef feat: bổ sung hàng chờ bác sĩ realtime bằng WebSocket`.
- Không merge `local/batch3` vào `main`.
- Các thay đổi sau `9cf7520` chưa nằm trên `origin/main`; không được giả định `main` đã có frontend/inventory/OTP nếu chưa merge hoặc PR.

Các commit hoàn thành sau khi Codex chạm weekly limit:

| Commit | Nội dung |
| --- | --- |
| `e496737` | `test: bổ sung nghiệm thu E2E trên SQL Server` |
| `3a4bda8` | `feat: quản lý schema SQL Server bằng Flyway` |
| `1487555` | `feat: tăng cường bảo mật và khả năng quan sát` |
| `016b1e0` | `fix: hoàn tất Docker runtime và ổn định Flyway` |

**Không làm lại các task dưới đây nếu không có regression hoặc yêu cầu mới.**
## 1. Task 1 — Release Acceptance / E2E SQL Server — DONE

Commit: `e496737 test: bổ sung nghiệm thu E2E trên SQL Server`

Đã bổ sung:

- `AsyncConfig` với executor riêng cho tác vụ async: core 4, max 10, queue 100, prefix `medclinic-async-`.
- `SqlServerReleaseAcceptanceEndToEndIntegrationTest` chạy luồng nghiệp vụ thật trên SQL Server.
- Tài liệu `docs/09_RELEASE_ACCEPTANCE.md`.

Luồng E2E đã kiểm chứng:

`Patient booking -> Staff check-in -> Queue -> Doctor examination -> Medical record -> Prescription -> Stock deduction -> Complete visit -> Invoice -> CASH payment -> PAID -> History -> PDF`.

Các invariant quan trọng được query trực tiếp từ SQL Server: trạng thái appointment/time-slot, queue sequence, medical record/prescription, tồn kho, snapshot giá thuốc, invoice total/payment status và PDF header.

Realtime STOMP chỉ mock ở transport boundary; transaction, repository, SQL Server và event `AFTER_COMMIT` vẫn chạy thật.

Task này đã PASS trước khi chuyển sang Flyway/production-readiness.
## 2. Task 2 — Flyway / SQL Server Production Readiness — DONE

Commit: `3a4bda8 feat: quản lý schema SQL Server bằng Flyway`

Quyết định kiến trúc đã chốt:

- **SQL Server là database chính hiện tại/production.**
- H2 chỉ dùng dev/test.
- MySQL chỉ là roadmap tương lai, không phải target hiện tại.
- Flyway là schema owner cho profile `sqlserver`.
- Hibernate production dùng `ddl-auto=validate`, không tự tạo/sửa schema.

Dependencies đã thêm: `flyway-core` và `flyway-sqlserver` (Flyway 10.10.0 theo Spring Boot 3.3.5).

Migration ban đầu:

- `V1__baseline_schema.sql`: baseline 16 application tables + constraints/indexes.
- `V2__reference_roles.sql`: seed idempotent 4 role hệ thống.

Fresh DB: Flyway chạy V1 -> V2 -> Hibernate validate.
Legacy 16-table DB: chỉ sau backup/pre-check mới bật one-time `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true`; Flyway ghi BASELINE version 1 rồi chạy migration sau V1.

Production/default `baseline-on-migrate=false`. Không tự baseline schema dở dang hoặc không rõ nguồn gốc.
Các test/tài liệu chính của Task 2:

- `SqlServerFlywayMigrationIntegrationTest`: fresh + legacy adoption trên DB disposable `medclinic_flyway_it`.
- `SqlServerFlywayArtifactsTest`: giữ migration và reviewed SQL đồng bộ, kiểm config Flyway/Docker init contract.
- `docker/sqlserver/init-db.sh`: chỉ tạo/guard database; không tự chạy baseline SQL trực tiếp.
- `docs/database/SQLSERVER_RUNBOOK.md`.
- `docs/database/SQLSERVER_PRODUCTION_READINESS_2026-09-19.md`.
- `docs/database/PRODUCTION_ADMIN_PROVISIONING.md`.

Lưu ý môi trường QA local từng dùng SQL Server 17.0.1000.7 (Developer). Flyway 10.10.0 cảnh báo SQL Server 17 mới hơn mức support được công bố (16.0). Đây chỉ là cảnh báo local QA; deployment target của project vẫn là SQL Server 2019/2022.

## 3. Task 3 — Security + Observability — DONE

Commit: `1487555 feat: tăng cường bảo mật và khả năng quan sát`

Security hardening đã thêm:

- Session fixation protection bằng `migrateSession`.
- Session timeout 30 phút.
- `JSESSIONID`: `HttpOnly`, `SameSite=Lax`; profile SQL Server production mặc định `Secure=true`.
- Remember-me cookie dùng cùng Secure policy.
- Production không expose error message/binding details/stacktrace.
- Security headers gồm `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Permissions-Policy`.
- CSRF/RBAC/BCrypt hiện có được giữ nguyên, không bị nới lỏng.
Rate limiting đã thêm cho endpoint nhạy cảm:

- `POST /login`: mặc định 20 request / 300 giây.
- `POST /forgot-password` và `POST /reset-password`: mặc định 5 request / 300 giây.
- Bucket theo remote IP + endpoint.
- Vượt ngưỡng trả HTTP `429` + `Retry-After`.
- Có thể override qua env: `LOGIN_RATE_LIMIT_MAX`, `PASSWORD_RESET_RATE_LIMIT_MAX`, `AUTH_RATE_LIMIT_WINDOW_SECONDS`.
- Hiện là in-memory per JVM; nếu scale multi-instance thì chuyển enforcement lên gateway/WAF/shared store như Redis.

Security audit/metrics:

- Event: `AUTH_SUCCESS`, `AUTH_FAILURE`, `LOGOUT`, `RATE_LIMIT_REJECTED`.
- Log dạng key=value, sanitize CR/LF/TAB, không log password/reset token/secret.
- Custom Micrometer counters: `medclinic.security.authentication{outcome=...}` và `medclinic.security.rate_limit.rejected{endpoint=...}`.

Observability:

- Thêm Spring Boot Actuator + Micrometer.
- Chỉ expose `health` và `metrics`.
- `/actuator/health`: anonymous được phép nhưng chỉ trả status.
- `/actuator/metrics/**`: yêu cầu `ROLE_ADMIN`.
- Không expose `env`, `configprops` hoặc endpoint nhạy cảm khác.

Tài liệu chính: `docs/10_SECURITY_OBSERVABILITY.md`.
SRS/RTM đã được đồng bộ từ 12 -> 15 NFR, thêm:

- `NFR-SEC-06`: session/cookie + response hardening.
- `NFR-SEC-07`: rate limiting + security audit.
- `NFR-OPS-01`: health/metrics có kiểm soát truy cập.

## 4. Docker Runtime Acceptance + Flyway V3 — DONE

Commit: `016b1e0 fix: hoàn tất Docker runtime và ổn định Flyway`

Docker Desktop/WSL2 đã được sửa và nghiệm thu runtime thật. Các vấn đề host đã xử lý:

- Hypervisor đã được nạp thành công.
- `hypervisorlaunchtype=Auto`.
- `VirtualMachinePlatform`, `vmcompute`, `hns`, `WslService` hoạt động bình thường.
- Khôi phục biến hệ thống `ProgramData` và `ALLUSERSPROFILE` về `C:\ProgramData` để Docker Desktop backend chạy đúng.
- `docker run --rm hello-world`: PASS.

Runtime versions lúc nghiệm thu:

- Docker Engine 29.8.0.
- Docker Compose v5.5.1.
- WSL 2.7.13.0, kernel 6.18.33.2-2.
- Linux containers qua Docker Desktop.
Compose hiện được thiết kế để chạy song song với SQL Server cài trên Windows:

- Windows `MSSQLSERVER`: host port `1433`.
- Docker SQL Server 2022: host `${SQLSERVER_HOST_PORT:-14333}` -> container `1433`.
- MedClinic app: host `8080`.
- App bên trong Compose vẫn kết nối `sqlserver:1433`.
- `.env.example` có `SQLSERVER_HOST_PORT=14333`.

Health/runtime acceptance đã PASS:

- `medclinic-sqlserver`: `healthy`.
- `medclinic-sqlserver-init`: exit code `0`.
- `medclinic-app`: `healthy`.
- `medclinic-app RestartCount=0` trên fresh startup sau fix.
- `/actuator/health`: HTTP 200, `status=UP`.
- `/`: HTTP 200 và security headers đúng.
- Named volume giữ dữ liệu đúng sau restart/down-up không xóa volume.

Tài liệu chi tiết: `docs/11_DOCKER_RUNTIME_ACCEPTANCE.md`.

### Flyway V3 — không được xóa hoặc sửa lịch sử V1/V2

Fresh Docker startup từng phát hiện lỗi JDBC: `A result set was generated for update` khi demo seed insert `users` bằng IDENTITY.

Root cause: V2 dùng `SET NOCOUNT ON`; trạng thái session có thể còn tồn tại trên pooled connection và ảnh hưởng JDBC generated-key insert.
Fix đúng nguyên tắc migration bất biến:

- **Không sửa V2 đã phát hành.**
- Thêm `V3__restore_jdbc_session_state.sql`.
- V3 thực hiện `SET NOCOUNT OFF;` để trả JDBC session về trạng thái an toàn.

Fresh Docker deployment sau V3:

- V1 `baseline schema`: success.
- V2 `reference roles`: success.
- V3 `restore jdbc session state`: success.
- 16 application tables.
- 4 roles.
- 9 demo users.
- 6 doctor profiles.
- 15 medicines.
- App không còn crash/restart lần đầu.

Ngoài ra `SqlServerPortalPerformanceBaselineIntegrationTest` được sửa để test tự quản lý schema không kích hoạt Flyway sai ngữ cảnh (`spring.flyway.enabled=false`).

## 5. Verification cuối cùng

Default verify không yêu cầu external SQL Server vẫn giữ các test SQL environment-gated như thiết kế.

Docker/SQL-enabled final verification đã chạy trên SQL Server container local và PASS:

```text
128 test case
128 executed
0 skipped
0 failures
0 errors
55 test classes
JaCoCo: 82.55% line / 66.42% branch
```
Fresh clone đúng commit `016b1e0` cũng tái lập kết quả:

```text
FRESH_TESTS=128
EXECUTED=128
SKIPPED=0
FAILURES=0
ERRORS=0
CLASSES=55
LINE=82.55%
BRANCH=66.42%
JAR=true
Git status=clean
Compose config=PASS
Docker image build=PASS
```

Runtime image cuối cũng được build từ fresh clone của chính commit `016b1e0`, sau đó force-recreate `medclinic-app` và xác nhận:

```text
medclinic-app = healthy
RestartCount = 0
medclinic-sqlserver = healthy
medclinic-sqlserver-init = Exited (0)
/actuator/health = UP
application error matches = 0
```

Không được tuyên bố GitHub Actions xanh nếu chưa trực tiếp quan sát workflow remote; local/fresh-clone evidence ở trên là bằng chứng đã xác minh.

## 6. Các tài liệu nên đọc nếu cần đào sâu

- `docs/09_RELEASE_ACCEPTANCE.md` — SQL Server E2E acceptance.
- `docs/10_SECURITY_OBSERVABILITY.md` — security + observability.
- `docs/11_DOCKER_RUNTIME_ACCEPTANCE.md` — Docker runtime acceptance.
- `docs/database/SQLSERVER_RUNBOOK.md` — production/QA runbook.
- `docs/database/SQLSERVER_PRODUCTION_READINESS_2026-09-19.md` — production-readiness evidence.
- `docs/03_DATABASE_DESIGN_3NF.md` — database design + Flyway strategy.
- `docs/05_SYSTEM_ARCHITECTURE.md` — architecture hiện tại.
- `docs/04_RTM_MATRIX.md` — traceability matrix.
- `README.md` — setup, Docker, SQL Server, security và quality summary.

## 7. Guardrails cho Codex khi tiếp tục

1. Giữ **SQL Server** là database chính; không chuyển project sang MySQL ở giai đoạn hiện tại.
2. H2 chỉ dùng dev/test; MySQL là future roadmap.
3. Không sửa nội dung migration đã phát hành `V1`, `V2`, `V3`, `V4`, `V5`; migration mới phải tạo version tiếp theo.
4. Không bật `baseline-on-migrate=true` mặc định cho production. Chỉ one-time opt-in với legacy schema đã backup/pre-check.
5. Không disable CSRF hoặc nới RBAC để "fix nhanh" test/UI.
6. Không expose thêm Actuator endpoint nhạy cảm (`env`, `configprops`, v.v.) nếu không có yêu cầu rõ ràng.
7. Không commit `.env`, password, remember-me key hoặc secret local.
8. Docker SQL Server host port mặc định là `14333`, không đổi lại `1433` nếu không có lý do vì Windows SQL Server đang dùng `1433`.
9. Nếu chỉnh Docker/database/security, phải chạy regression tương ứng trước khi commit.
10. Giữ commit message tiếng Việt, lịch sử commit tuần tự và `git diff --check` sạch.

## 8. Trạng thái roadmap

```text
DONE  Task 1 — Release Acceptance / SQL Server E2E
DONE  Task 2 — Flyway / Database Production Readiness
DONE  Task 3 — Security + Observability
DONE  Docker runtime acceptance
NEXT  Task 4 — Defense / Demo Package
```

Không cần quay lại Task 1-3 hoặc Docker trừ khi có bug/regression mới.
## 9. Quick resume commands cho Codex

Kiểm tra source-of-truth:

```powershell
cd C:\Users\DUCANHZZ\Downloads\LTW\PRJ
git fetch origin
git status --short
git rev-parse HEAD
git rev-parse origin/main
```

Docker local/demo:

```powershell
Copy-Item .env.example .env   # chỉ nếu .env chưa tồn tại
# Điền MSSQL_SA_PASSWORD và REMEMBER_ME_KEY local, không commit file này.
docker compose up -d --build
docker compose ps
```

Health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Default test:

```powershell
mvn clean verify
```

SQL Server integration tests được environment-gated; xem `docs/11_DOCKER_RUNTIME_ACCEPTANCE.md` và `docs/database/SQLSERVER_RUNBOOK.md` trước khi bật.

## 10. Next action đề xuất

Tiếp tục **Task 4 — Defense / Demo Package**: chuẩn bị luồng demo, dữ liệu/tài khoản demo, checklist trình bày, bằng chứng architecture/security/database/Docker, và kịch bản fallback nếu môi trường demo gặp sự cố.

Trước khi bắt đầu Task 4, hãy kiểm tra đang ở `feature/frontend-design-system` và HEAD chứa `085464f` (hoặc commit hậu nhiệm đã merge đầy đủ các thay đổi này).

## 11. Cập nhật bổ sung sau khi file handoff được tạo

Mốc tạo file handoff: `9cf7520` (`20/09/2026 15:26 +07:00`). Từ mốc này đến HEAD hiện tại `085464f`, assistant đã tiếp tục dự án trực tiếp bằng Desktop Commander trên branch `feature/frontend-design-system`.

### 11.1. Frontend / Design System — DONE

Chuỗi commit:

- `6b766a4 feat: chuẩn hóa design system và giao diện frontend`
- `a15538a feat: tinh gọn bảng quản trị và responsive`
- `a4bc9f7 feat: redesign auth and profile experience`
- `9872820 feat: refine doctor and staff operational ui`
- `ab50925 feat: polish patient ui and mobile responsiveness`

Đã hoàn thành:

- Tạo và áp dụng `medclinic-design-system.css` làm design system dùng chung.
- Đồng bộ layout public/dashboard, trang chủ, danh sách bác sĩ, đặt lịch và dashboard Admin.
- Tinh gọn bảng quản trị người dùng, bác sĩ, chuyên khoa và thuốc; cải thiện responsive.
- Redesign login/register/forgot-password/reset-password/profile.
- Cải thiện UI nghiệp vụ Doctor: desk, examination, schedule.
- Cải thiện UI nghiệp vụ Staff: reception, walk-in, billing.
- Hoàn thiện các màn Patient: danh sách/chi tiết lịch hẹn, bệnh sử, doctor detail và mobile responsiveness.

### 11.2. Regression fix sau frontend — DONE

- `9d8975c`: sửa regression ở trang đặt lịch để việc chọn bác sĩ/khung giờ hoạt động lại đúng.
- `7b974fd`: bổ sung `doctor.specialty` và `timeSlot` vào `AppointmentRepository` entity graph; thêm integration assertion để trang khám render đầy đủ form khám, bảng thuốc và nút thao tác.
- `0152d59`: bổ sung `appointment.doctor.specialty` vào các `InvoiceRepository` entity graph; thêm regression test bảo đảm màn billing render đầy đủ dòng hóa đơn, chuyên khoa, trạng thái và nút thu tiền mà không dính lazy-loading.

### 11.3. Quản lý kho thuốc: cảnh báo tồn thấp + nhập bổ sung — DONE

Commit: `97c359e feat: add low-stock alerts and medicine restocking`

Đã bổ sung:

- `Medicine.reorderLevel` với ngưỡng nhập lại riêng cho từng thuốc; mặc định nghiệp vụ là 50.
- Phân loại tồn kho: hết hàng, sắp hết/cần nhập, bình thường.
- Dashboard Admin hiển thị cảnh báo thuốc tồn thấp.
- Admin không chỉnh trực tiếp `stockQuantity` như field thông thường; dùng luồng **Nhập thêm**.
- Thêm `InventoryTransaction` và `InventoryTransactionType` để audit biến động kho.
- `RESTOCK`: lưu tồn trước/sau, số lượng nhập, giá nhập tùy chọn, ghi chú và người thực hiện.
- `DISPENSE`: phát sinh khi bác sĩ hoàn tất đơn thuốc; transaction kho rollback cùng bệnh án/hóa đơn nếu nghiệp vụ thất bại.
- Giữ pessimistic locking/chống oversell khi nhiều giao dịch cùng trừ tồn.
- Flyway mới: `V4__inventory_restock_and_low_stock.sql`.
- Tài liệu mới: `docs/13_INVENTORY_RESTOCK_WORKFLOW_2026-09-22.md`.
- SRS/UML/Database Design/RTM đã đồng bộ theo nghiệp vụ kho mới.

### 11.4. Email OTP / Magic Link + Profile editable — DONE

Commit: `085464f feat: add email OTP authentication and editable profiles`

Luồng xác thực mới:

- Đăng ký tài khoản Patient mới không active ngay; phải xác thực email trước.
- Đăng nhập tài khoản thường: đúng username/password mới phát hành challenge OTP, chưa tạo authenticated session ngay.
- OTP là mã 6 số, mặc định hiệu lực 300 giây (5 phút).
- OTP được lưu dạng BCrypt hash; magic token lưu SHA-256 hash.
- Mỗi challenge chỉ dùng một lần; resend sẽ consume challenge cũ và tạo challenge mới.
- Giới hạn 5 lần nhập OTP sai trước khi bắt buộc gửi mã mới.
- Email chứa cả OTP và magic-link `/auth/verify-email?token=...`; bấm link hợp lệ sẽ xác thực và đăng nhập.
- URL gốc của link lấy từ `APP_WEBSITE`; SMTP dùng `MAIL_USERNAME` / `MAIL_PASSWORD`, không hard-code secret.
- Tài khoản seed/test có `otpBypass=true` để phục vụ demo/test mà không chặn luồng phát triển.
- Rate limiting đã được nối với endpoint login/OTP nhạy cảm và regression test đã cập nhật.

Profile:

- Cho phép sửa họ tên, số điện thoại, giới tính, ngày sinh và địa chỉ.
- Cho phép upload/thay avatar; file runtime nằm trong `uploads/` và đã được `.gitignore`.
- Username/email vẫn là định danh không sửa trực tiếp trong form profile.
- Có luồng đổi mật khẩu hiện hữu và giữ kiểm tra mật khẩu cũ/xác nhận mật khẩu mới.
- Flyway mới: `V5__email_otp_and_profile_security.sql`.
- Thêm `auth_verification_challenges`, `users.email_verified`, `users.otp_bypass`.
- SRS/UML/Database Design/RTM đã đồng bộ.

### 11.5. Migration history hiện tại

Không sửa migration đã phát hành:

```text
V1  baseline schema
V2  reference roles
V3  restore JDBC session state
V4  inventory restock and low-stock
V5  email OTP and profile security
```

SQL Server migration integration hiện kiểm tra **18 application tables** và cả hai schema mới `inventory_transactions`, `auth_verification_challenges`.

### 11.6. Verification mới nhất sau toàn bộ phần tiếp tục

Kết quả từ report hiện có sau `mvn verify` cuối:

```text
58 test classes
136 test cases
128 executed
8 environment-gated skipped
0 failures
0 errors
JaCoCo: 81.39% line / 63.65% branch
```

Ngoài ra:

- Targeted OTP/Profile regression: PASS.
- Email OTP content test: PASS (kiểm tra OTP, thời hạn và magic-link trong email).
- Rate-limit regression: PASS.
- `git diff --check`: PASS trước checkpoint `085464f`.
- Audit UTF-8/control characters: sạch.
- Không có `MAIL_USERNAME`, `MAIL_PASSWORD` hoặc credential mail thật bị hard-code.
- Checkpoint `085464f` đã push lên `origin/feature/frontend-design-system`.

### 11.7. Trạng thái Git cần Codex biết

Tại thời điểm cập nhật tài liệu này:

```text
current branch: feature/frontend-design-system
HEAD:           085464fca801929394fdd4f139a0bc33a30a93a2
origin/feature/frontend-design-system: 085464fca801929394fdd4f139a0bc33a30a93a2
origin/main:    9cf75200942c0c498a91bb914b42a518890dbcea
```

Như vậy các thay đổi frontend + inventory + OTP/profile **đã push lên feature branch nhưng chưa nằm trên `origin/main`**. Không được giả định `main` đã chứa các thay đổi này nếu chưa merge/PR.

## 12. Roadmap tiếp theo sau bản cập nhật này

```text
DONE  Task 1 — Release Acceptance / SQL Server E2E
DONE  Task 2 — Flyway / Database Production Readiness
DONE  Task 3 — Security + Observability
DONE  Docker runtime acceptance
DONE  Frontend design system + responsive refinement
DONE  Regression fixes cho booking / examination / billing
DONE  Inventory restock + low-stock alerts (Flyway V4)
DONE  Email OTP / Magic Link + editable profile (Flyway V5)
NEXT  Task 4 — Defense / Demo Package
```

Nếu Codex tiếp tục từ đây, hãy checkout/pull `feature/frontend-design-system`, xác nhận HEAD chứa `085464f`, đọc lại SRS/RTM cùng `docs/13_INVENTORY_RESTOCK_WORKFLOW_2026-09-22.md`, rồi mới lập task mới. Không quay lại sửa V1-V5 hoặc làm lại các phần DONE nếu không có regression/yêu cầu mới.
