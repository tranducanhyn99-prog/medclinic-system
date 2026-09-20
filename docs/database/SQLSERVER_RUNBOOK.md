# SQL Server Production / QA Runbook

SQL Server là database chính của MedClinic ở phạm vi hiện tại. H2 chỉ dùng cho dev/test; MySQL là future roadmap.

## 1. Nguyên tắc schema

- Profile `sqlserver` dùng **Flyway** làm schema owner và `spring.jpa.hibernate.ddl-auto=validate`; Hibernate không tự sửa production schema.
- Migration hiện tại nằm tại `src/main/resources/db/migration/sqlserver/`:
  - `V1__baseline_schema.sql`: baseline 16 bảng và các constraint/index.
  - `V2__reference_roles.sql`: seed idempotent 4 role hệ thống.
  - `V3__restore_jdbc_session_state.sql`: khôi phục `NOCOUNT OFF` để connection pool/JDBC generated keys ổn định sau migration.
- V1/V2 được contract-test để luôn đồng bộ với các SQL đã review trong `docs/database/`; V3 là migration vận hành độc lập để chuẩn hóa session state.
- Production mặc định giữ `baseline-on-migrate=false`. Chỉ bật `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` cho lần adoption đầu tiên của database legacy đã được backup/pre-check và có đúng baseline 16 bảng.
- Không baseline schema dở dang/không rõ nguồn gốc. Luôn backup và chạy pre-check trước production.

## 2. Database mới

DBA chỉ cần tạo database rỗng và cấp quyền migration cho application/deployment principal:

```powershell
sqlcmd -S localhost -E -C -Q "CREATE DATABASE [medclinic_db];"
```

Khi app khởi động với profile `sqlserver`, Flyway tự tạo `flyway_schema_history`, chạy V1, V2 rồi V3; sau đó Hibernate `validate` mapping entity. Startup phải fail nếu migration hoặc schema validation không đạt.

Sau lần migrate đầu, có thể giảm quyền DDL của runtime principal theo chính sách môi trường nếu tổ chức tách deployment principal và application principal.

## 3. Database legacy 16 bảng

Trước khi bật Flyway cho database đã tồn tại:

1. Backup database và ghi nhận phiên bản triển khai hiện tại.
2. Xác nhận schema thực tế tương ứng baseline đã review và có đúng 16 bảng ứng dụng.
3. Xác nhận chưa có `flyway_schema_history` hoặc xử lý theo runbook migration đã được DBA phê duyệt.
4. Đặt `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` cho lần adoption đầu tiên rồi khởi động release với profile `sqlserver`.
5. Sau khi xác nhận `flyway_schema_history` đã có baseline version 1 cùng V2/V3 thành công, bỏ biến opt-in cho các lần deploy sau.

Với schema legacy hợp lệ, Flyway sẽ tạo history bằng **BASELINE version 1**, bỏ qua V1 và áp dụng V2, V3 trở đi. Hibernate `validate` chạy sau migration để chặn schema lệch entity.

Docker init job chỉ áp dụng guard `0`/`16` khi chưa có `flyway_schema_history`; nếu history đã tồn tại thì không hard-code số bảng để tránh chặn migration tương lai. Compose local/demo bật legacy baseline opt-in rõ ràng sau guard; production standalone vẫn mặc định tắt.

## 4. Biến môi trường application

Không ghi secret vào repository. Trước khi chạy profile `sqlserver`, cung cấp:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=medclinic_db;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=true"
$env:DB_USERNAME="<application-login>"
$env:DB_PASSWORD="<secret>"
$env:REMEMBER_ME_KEY="<random-secret>"
$env:MAIL_USERNAME="<smtp-user>"
$env:MAIL_PASSWORD="<smtp-secret>"
```

Khởi động:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=sqlserver"
```

## 5. Verification SQL

```sql
SELECT COUNT(*) AS application_table_count
FROM sys.tables
WHERE name <> 'flyway_schema_history';

SELECT installed_rank, version, description, type, success
FROM flyway_schema_history
ORDER BY installed_rank;

SELECT name, filter_definition
FROM sys.indexes
WHERE object_id = OBJECT_ID('appointments')
  AND name = 'uk_appointment_date_queue_sequence';
```

Kỳ vọng: 16 bảng ứng dụng; fresh DB có V1/V2/V3 kiểu `SQL`, còn legacy adoption có version 1 kiểu `BASELINE` và V2/V3 kiểu `SQL`; tất cả row history phải `success=1`.

## 6. SQL Server integration tests

Các test SQL Server chỉ chạy khi chủ động bật environment guard. Migration acceptance còn yêu cầu base URL local trỏ `medclinic_e2e`; test tự tạo/drop database cô lập `medclinic_flyway_it`.

```powershell
$env:RUN_SQLSERVER_IT="true"
# DB_URL / DB_USERNAME / DB_PASSWORD theo môi trường QA local
mvn "-Dtest=SqlServer*" test
```

Các kiểm chứng trọng yếu gồm:

- Fresh Flyway migration V1 → V2 → V3 + Hibernate validate.
- Legacy 16-table adoption: BASELINE v1 → V2 → V3 + Hibernate validate.
- Concurrent booking, medicine stock locking, invoice-code reservation và rollback.
- Release acceptance E2E Patient → Staff → Doctor → Billing/PDF.

Máy QA local hiện là SQL Server 17.0; Flyway 10.10.0 cảnh báo server này mới hơn mức hỗ trợ đã công bố (16.0). Test local vẫn PASS, nhưng deployment target của project vẫn là SQL Server 2019/2022.

## 7. Production handoff

- Backup + pre-check trước khi Flyway nhận quản lý database legacy.
- Review migration SQL/checksum và quyền DDL trước deployment.
- Provision admin theo `PRODUCTION_ADMIN_PROVISIONING.md` sau khi V2 đã tạo role hệ thống.
- Không dùng demo account/password từ profile `dev`.
- Ghi lại version Flyway, operator, thời điểm và kết quả smoke test trong biên bản triển khai.
