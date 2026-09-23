# SQL Server production-readiness evidence — cập nhật 20/09/2026

## Scope verified locally

- Deployment target: Microsoft SQL Server 2019/2022; máy QA local hiện chạy SQL Server 17.0.1000.7.
- Schema management: Flyway 10.10.0 (Spring Boot managed) + `flyway-sqlserver`.
- Migration V1: `src/main/resources/db/migration/sqlserver/V1__baseline_schema.sql`.
- Migration V2: `src/main/resources/db/migration/sqlserver/V2__reference_roles.sql`.
- Migration V3: `src/main/resources/db/migration/sqlserver/V3__restore_jdbc_session_state.sql` (khôi phục `NOCOUNT OFF` cho JDBC session).
- Hibernate production mode: `ddl-auto=validate`, chạy sau Flyway.
- First-admin template: `SQLSERVER_ADMIN_PROVISIONING_TEMPLATE.sql`.
- Application artifact: `target/medclinic-system-1.0.0.jar` với profile `sqlserver`.

## Flyway migration evidence

`SqlServerFlywayMigrationIntegrationTest` chạy trên database disposable riêng `medclinic_flyway_it`, chỉ được kích hoạt khi base URL là local `medclinic_e2e`.

Hai đường migration đã PASS:

1. **Fresh database:** Flyway tạo `flyway_schema_history`, chạy V1, V2 rồi V3; history kết thúc ở version 3 và Hibernate `validate` startup thành công.
2. **Legacy adoption:** test dựng đúng baseline 16 bảng nhưng không có history; production-safe default giữ baseline OFF, còn test bật opt-in riêng cho bước adoption để Flyway ghi `BASELINE` version 1, chạy V2, V3 và Hibernate `validate` startup thành công.

Test drop `medclinic_flyway_it` sau khi hoàn tất. `SqlServerFlywayArtifactsTest` còn xác minh V1/V2 luôn đồng bộ nội dung với các SQL review/audit trong `docs/database/`.

Máy QA SQL Server 17.0 làm Flyway 10.10.0 phát cảnh báo server mới hơn mức hỗ trợ đã công bố (16.0). Acceptance local vẫn PASS; điều này **không** được hiểu là chứng nhận chính thức SQL Server 17 cho Flyway 10.10. Deployment target của project vẫn là SQL Server 2019/2022.

## Database and admin provisioning evidence

Baseline schema trước khi chuyển sang Flyway đã được recreate từ zero và kiểm chứng 16 bảng/constraint/index. Reference-data script chạy lặp vẫn giữ đúng bốn role: `ROLE_ADMIN`, `ROLE_DOCTOR`, `ROLE_PATIENT`, `ROLE_STAFF`.

Admin provisioning template đã được kiểm thử với QA-only values và non-production hash. Tài khoản được join đúng `ROLE_ADMIN`; chạy lại cùng username bị chặn bằng SQL Server error 51001 (`Admin username already exists`).

Các SQL này hiện được giữ làm bản review/audit, còn runtime migration dùng Flyway V1/V2/V3. Contract test ngăn hai bản drift khỏi nhau.

## Application release smoke đã xác nhận trước đó

Packaged JAR profile `sqlserver` đã startup thành công với Windows Integrated Authentication và `ddl-auto=validate` trên local SQL Server. Các endpoint public `/`, `/login`, `/register` trả HTTP 200; `/admin/dashboard` unauthenticated redirect tới `/login`; không có dev seed trong profile `sqlserver`.

Sau khi đưa Flyway vào, migration integration test startup cùng `MedClinicApplication` sau migration và vượt Hibernate `validate` cho cả fresh/legacy schema.

## Docker / deployment behavior

`docker/sqlserver/init-db.sh` chỉ tạo `medclinic_db` nếu chưa tồn tại. Khi chưa có `flyway_schema_history`, script chỉ chấp nhận schema rỗng hoặc legacy 16 bảng; khi history đã tồn tại, script không hard-code số bảng để Flyway có thể quản lý các version tương lai. Script không tự apply baseline/reference SQL; app/Flyway chịu trách nhiệm versioning schema.

Docker runtime đã được nghiệm thu trên Docker Desktop/WSL2: `hello-world` PASS; fresh `docker compose down -v` + `docker compose up -d --build` PASS; SQL Server và application đều `healthy`, init job exit 0, application `RestartCount=0`, Flyway V1/V2/V3 success và volume persistence PASS sau restart container. Compose mặc định publish SQL Server ở host `14333` để có thể chạy song song với `MSSQLSERVER` Windows trên `1433`.

## Still external to local acceptance

Các mục sau **chưa** được đánh dấu production-complete:

- SMTP production/staging thật và outbound delivery smoke.
- Secret-manager wiring/rotation policy của hosting environment.
- Manual screen-reader release smoke.
- Final DBA backup/change approval và deployment-window smoke. Repository có `ops/backup.ps1` và `ops/restore-drill.ps1`; kết quả từng lần chạy phải được lưu ngoài Git.
- Với thanh toán ngân hàng: merchant onboarding/credential thật, HTTPS callback, secret rotation và đối soát kế toán theo `18_P2_PRODUCTION_READINESS_AND_BANK_PAYMENT_2026-09-23.md`.
- Production admin identity thật và bootstrap-password rotation/handoff.

Các mục này cần credential, hạ tầng hoặc human verification thật và không được giả lập thành bằng chứng đã hoàn thành.
