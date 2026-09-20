# DOCKER RUNTIME ACCEPTANCE

**Dự án:** MedClinic - Phòng Khám Tư Nhân
**Ngày nghiệm thu:** 20/09/2026
**Mục tiêu:** xác nhận Docker Desktop/WSL2, SQL Server 2022 container, Flyway và application container chạy end-to-end trên máy QA local.

## 1. Runtime host

- Windows 11 Home Single Language.
- Hypervisor: active (`HypervisorPresent=True`).
- WSL: 2.7.13.0, kernel 6.18.33.2-2.
- Docker Engine: 29.8.0, Linux containers qua Docker Desktop.
- Docker Compose: v5.5.1.
- Host SQL port mặc định của Compose: `14333`; container vẫn dùng `1433`. Coexistence với `MSSQLSERVER` Windows trên host `1433` đã được xác nhận.
- `docker run --rm hello-world`: PASS.

Trong quá trình setup, Windows `VirtualMachinePlatform` từng ở trạng thái enabled nhưng thiếu payload `vmcompute`. Windows servicing/update được hoàn tất, `vmcompute`, `hns` và `WslService` sau đó hoạt động bình thường. Hai biến hệ thống `ProgramData` và `ALLUSERSPROFILE` cũng được khôi phục về `C:\ProgramData` để Docker Desktop backend tìm đúng data folder.

## 2. Fresh Compose acceptance

Acceptance được chạy từ volume sạch:

```bash
docker compose down -v --remove-orphans
docker compose up -d --build
```

Kết quả dependency chain:
- `medclinic-sqlserver`: `healthy`.
- `medclinic-sqlserver-init`: exit code `0`.
- `medclinic-app`: `healthy`.
- `medclinic-app` automatic `RestartCount=0` trên fresh startup sau fix.
- App healthcheck dùng `/actuator/health` và chỉ chấp nhận `status=UP`.

Flyway history trên `medclinic_db`:

- V1 `baseline schema` — `SQL`, success.
- V2 `reference roles` — `SQL`, success.
- V3 `restore jdbc session state` — `SQL`, success.

V3 được bổ sung sau khi fresh Docker startup phát hiện `SET NOCOUNT ON` của V2 có thể còn hiệu lực trên connection pool và làm JDBC generated-key insert báo `A result set was generated for update`. V3 trả session state về `NOCOUNT OFF`; fresh deployment sau fix không còn restart lần đầu.

## 3. Database/runtime invariants

Sau startup từ volume sạch:

- 16 application tables.
- 4 system roles.
- 9 demo users.
- 6 doctor profiles.
- 15 medicines.
- `/actuator/health`: HTTP 200, status `UP`.
- `/`: HTTP 200.
Security headers trên homepage đã được kiểm tra: `X-Content-Type-Options=nosniff`, `X-Frame-Options=SAMEORIGIN`, `Referrer-Policy=strict-origin-when-cross-origin` và `Permissions-Policy=camera=(), microphone=(), geolocation=()`.

## 4. Restart và persistence

- Restart riêng SQL Server container: quay lại `healthy`.
- Restart application container: quay lại `healthy`.
- `medclinic-app` không có automatic restart do lỗi startup (`RestartCount=0`).
- Flyway history và demo data vẫn giữ nguyên sau restart, xác nhận named volume hoạt động đúng.

## 5. Automated verification

Full Maven verify được chạy với toàn bộ SQL Server integration tests bật và trỏ vào SQL Server container local:

```text
128 test case
128 executed
0 skipped
0 failures
0 errors
55 test classes
JaCoCo: 82.55% line / 66.42% branch
```

Default verify không có external SQL Server vẫn giữ 8 test environment-gated như thiết kế. Docker acceptance không thay đổi quy tắc này.

## 6. Kết luận

Docker runtime acceptance cho môi trường local/demo được đánh dấu **PASS**. Compose hiện cung cấp SQL Server healthcheck, application healthcheck, database-init guard, Flyway V1/V2/V3 và persistent SQL Server volume. Production vẫn cần secret manager, TLS/reverse proxy, backup/change approval và hạ tầng SMTP theo runbook riêng.
