# SQL Server Production / QA Runbook

SQL Server là database chính của MedClinic ở phạm vi hiện tại. H2 chỉ dùng cho dev/test; MySQL là future roadmap.

## 1. Nguyên tắc schema

- Profile `sqlserver` dùng **Flyway** làm schema owner và `spring.jpa.hibernate.ddl-auto=validate`; Hibernate không tự sửa production schema.
- Migration hiện tại nằm tại `src/main/resources/db/migration/sqlserver/`:
  - `V1__baseline_schema.sql`: baseline 16 bảng và các constraint/index.
  - `V2__reference_roles.sql`: seed idempotent 4 role hệ thống.
  - `V3__restore_jdbc_session_state.sql`: khôi phục `NOCOUNT OFF` để connection pool/JDBC generated keys ổn định sau migration.
  - `V4__inventory_restock_and_low_stock.sql`: ngưỡng tồn kho và lịch sử nhập/xuất thuốc.
  - `V5__email_otp_and_profile_security.sql`: trạng thái xác thực email và OTP challenge.
  - `V6__bank_payments_audit_and_shared_rate_limits.sql`: chuyển khoản, audit và rate limit dùng chung.
  - `V7__unique_patient_phone.sql`: kiểm tra dữ liệu cũ, filtered unique index và constraint số điện thoại.
  - `V8__account_security_version.sql`: phiên bản bảo mật để chặn session cũ sau khóa/mở khóa.
  - `V9__integral_vnd_catalog_prices.sql`: kiểm tra giá thuốc/phí khám/giá nhập kho nguyên VND.
  - `V10__google_patient_identity.sql`: Google subject nullable, so sánh binary và unique index lọc non-null; giữ nguyên tài khoản cũ.
- V1/V2 được contract-test để luôn đồng bộ với các SQL đã review trong `docs/database/`; V3 là migration vận hành độc lập để chuẩn hóa session state.
- V4/V5 dùng `GO` tách các batch SQL Server sau khi tạo cột/bảng, trước khi khai báo constraint tham chiếu các đối tượng vừa tạo. Nếu môi trường đã áp dụng phiên bản V4/V5 cũ, phải đối chiếu Flyway checksum và lịch sử triển khai trước khi nâng cấp; không chạy `repair` hoặc sửa history một cách tự động.
- Production mặc định giữ `baseline-on-migrate=false`. Chỉ bật `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` cho lần adoption đầu tiên của database legacy đã được backup/pre-check và có đúng baseline 16 bảng.
- Không baseline schema dở dang/không rõ nguồn gốc. Luôn backup và chạy pre-check trước production.
- V7 dừng nếu điện thoại cũ trùng, rỗng hoặc sai định dạng; V9 dừng nếu giá catalog/nhập kho cũ âm hoặc có phần lẻ. Sửa dữ liệu theo quyết định nghiệp vụ rồi kiểm tra lại trước nâng cấp. Không tự gộp/xóa hồ sơ hoặc làm tròn hóa đơn lịch sử; không tự chạy `repair` để bỏ qua lỗi.

## 2. Database mới

DBA chỉ cần tạo database rỗng và cấp quyền migration cho application/deployment principal:

```powershell
sqlcmd -S localhost -E -C -Q "CREATE DATABASE [medclinic_db];"
```

Khi app khởi động với profile `sqlserver`, Flyway tự tạo `flyway_schema_history`, chạy V1–V10; sau đó Hibernate `validate` mapping entity. Startup phải fail nếu migration hoặc schema validation không đạt.

Sau lần migrate đầu, có thể giảm quyền DDL của runtime principal theo chính sách môi trường nếu tổ chức tách deployment principal và application principal.

## 3. Database legacy 16 bảng

Trước khi bật Flyway cho database đã tồn tại:

1. Backup database và ghi nhận phiên bản triển khai hiện tại.
2. Xác nhận schema thực tế tương ứng baseline đã review và có đúng 16 bảng ứng dụng.
3. Xác nhận chưa có `flyway_schema_history` hoặc xử lý theo runbook migration đã được DBA phê duyệt.
4. Đặt `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` cho lần adoption đầu tiên rồi khởi động release với profile `sqlserver`.
5. Sau khi xác nhận `flyway_schema_history` đã có baseline version 1 cùng V2–V10 thành công, bỏ biến opt-in cho các lần deploy sau.

Với schema legacy hợp lệ, Flyway sẽ tạo history bằng **BASELINE version 1**, bỏ qua V1 và áp dụng V2–V10. Hibernate `validate` chạy sau migration để chặn schema lệch entity.

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

Kỳ vọng: 21 bảng ứng dụng; fresh DB có V1–V10 kiểu `SQL`, còn legacy adoption có version 1 kiểu `BASELINE` và V2–V10 kiểu `SQL`; tất cả row history phải `success=1`.

## Backup và restore drill

Tạo backup nhất quán của SQL Server cùng uploads:

```powershell
.\ops\backup.ps1
```

Diễn tập restore vào database tạm, chạy `DBCC CHECKDB` rồi tự xóa database drill:

```powershell
$latest = Get-ChildItem .\backups -Recurse -Filter *.bak | Sort-Object LastWriteTime -Descending | Select-Object -First 1
.\ops\restore-drill.ps1 -BackupFile $latest.FullName
```

Không dùng restore drill để ghi đè `medclinic_db`. Restore production cần change approval, maintenance window và backup mới ngay trước thao tác.

## 6. SQL Server integration tests

Các test SQL Server chỉ chạy khi chủ động bật environment guard. Migration acceptance còn yêu cầu base URL local trỏ `medclinic_e2e`; test tự tạo/drop database cô lập `medclinic_flyway_it`.

```powershell
$env:RUN_SQLSERVER_IT="true"
# DB_URL / DB_USERNAME / DB_PASSWORD theo môi trường QA local
mvn "-Dtest=SqlServer*" test
```

Các kiểm chứng trọng yếu gồm:

- Fresh Flyway migration V1 → V10 + Hibernate validate.
- Legacy 16-table adoption: BASELINE v1 → V2–V10 + Hibernate validate.
- V7/V9 từ chối dữ liệu legacy không hợp lệ, giữ nguyên hồ sơ/giá trị cũ để xử lý có chủ đích.
- Concurrent booking, medicine stock locking, invoice-code reservation và rollback.
- Release acceptance E2E Patient → Staff → Doctor → Billing/PDF.

Lần nghiệm thu 23/09/2026 dùng container SQL Server 2022 (engine 16.0). Kiểm tra lại phiên bản engine và kết quả test ở từng môi trường triển khai.

Test điện thoại và thanh toán mới dùng database riêng `medclinic_phone_it` và `medclinic_payments_it`; đổi `DB_URL` tương ứng và chạy lần lượt `SqlServerPhoneUniquenessIntegrationTest`, `SqlServerBankPaymentConcurrencyIntegrationTest`. Cả hai từ chối URL không trỏ đúng database local dành cho test. Xem [hướng dẫn kiểm thử](../KIEM_THU.md).

Test Google dùng local `medclinic_google_it` với V1–V10 + Hibernate validate; chạy `SqlServerGoogleLoginIntegrationTest`. Client giả lập trong test không cần secret thật. Bật tính năng trên app theo [hướng dẫn Google OAuth](../GOOGLE_LOGIN_SETUP.md); bật flag mà thiếu credentials sẽ làm startup fail.

### Kiểm thử OTP và Google chờ OTP

Tạo database test riêng, dùng Flyway V1–V10 + Hibernate validate. Bật `RUN_SQLSERVER_IT=true`, đặt `DB_USERNAME`, `DB_PASSWORD`, `REMEMBER_ME_KEY` trong môi trường, rồi chạy từng lớp:

```powershell
$env:DB_URL = 'jdbc:sqlserver://localhost:14333;databaseName=medclinic_otp_it;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=true'
mvn '-Dtest=SqlServerOtpAuthenticationIntegrationTest' test
$env:DB_URL = 'jdbc:sqlserver://localhost:14333;databaseName=medclinic_google_otp_it;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=true'
mvn '-Dtest=SqlServerGoogleCallbackProtocolIntegrationTest' test
```

Đổi port theo SQL Server local. Guard của từng lớp chỉ chấp nhận localhost/127.0.0.1 và đúng tên database trên. Bộ này giả lập email/Google; kết quả và phạm vi xem [kiểm thử](../KIEM_THU.md).

## 7. Production handoff

- Backup + pre-check trước khi Flyway nhận quản lý database legacy.
- Review migration SQL/checksum và quyền DDL trước deployment.
- Sau khi Flyway áp dụng `V2__reference_roles.sql` tạo role hệ thống, khởi tạo tài khoản quản trị qua `SQLSERVER_ADMIN_PROVISIONING_TEMPLATE.sql` với BCrypt hash tạo riêng và bật `must_change_password = 1` (production không có tài khoản admin mặc định).
- Không dùng demo account/password từ profile `dev`.
- Ghi lại version Flyway, operator, thời điểm và kết quả smoke test trong biên bản triển khai.
