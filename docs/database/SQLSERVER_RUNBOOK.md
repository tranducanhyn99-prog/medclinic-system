# SQL Server Production / QA Runbook

SQL Server là database chính của MedClinic ở phạm vi hiện tại. H2 chỉ dùng cho dev/test; MySQL là future roadmap.

## 1. Nguyên tắc schema

- Database mới: áp dụng `SQLSERVER_BASELINE_SCHEMA_2026-09-19.sql` trên database sạch.
- Database cũ: backup, chạy pre-check rồi áp dụng migration incremental trong `CORE_WORKFLOW_MIGRATION_2026-09-17.md`.
- Profile `sqlserver` dùng `spring.jpa.hibernate.ddl-auto=validate`; Hibernate không được tự sửa production schema.
- Migration/baseline do tài khoản DBA thực hiện; runtime application login chỉ cần quyền dữ liệu cần thiết.

## 2. Tạo database sạch và áp dụng baseline

Ví dụ với Windows Authentication của DBA:

```powershell
sqlcmd -S localhost -E -C -Q "CREATE DATABASE [medclinic_db];"
sqlcmd -S localhost -E -C -d medclinic_db -b -i docs\database\SQLSERVER_BASELINE_SCHEMA_2026-09-19.sql
sqlcmd -S localhost -E -C -d medclinic_db -b -i docs\database\SQLSERVER_REFERENCE_DATA_2026-09-19.sql
```

Baseline hiện tạo 16 bảng, 17 foreign key và filtered unique index `uk_appointment_date_queue_sequence`.
## 3. Biến môi trường application

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

Nếu schema lệch entity, startup phải fail ở bước Hibernate `validate` thay vì tự mutate database.

## 4. Verification SQL

```sql
SELECT COUNT(*) AS table_count FROM sys.tables;
SELECT COUNT(*) AS foreign_key_count FROM sys.foreign_keys;
SELECT name, filter_definition
FROM sys.indexes
WHERE object_id = OBJECT_ID('appointments')
  AND name = 'uk_appointment_date_queue_sequence';
```

Kỳ vọng hiện tại: `table_count=16`, `foreign_key_count=17`, filtered index có điều kiện `queue_sequence IS NOT NULL`.

## 5. SQL Server integration tests

Các test chỉ chạy khi chủ động bật environment guard:

```powershell
$env:RUN_SQLSERVER_IT="true"
mvn "-Dtest=SqlServerInvoiceCodeReservationIntegrationTest,SqlServerConcurrentBookingIntegrationTest,SqlServerMedicineStockConcurrencyIntegrationTest" test
```

T10, T11 và T15 đều đã PASS trên SQL Server local ngày 19/09/2026, gồm invoice-code retry, full Spring/JDBC stock-concurrency/rollback và concurrent booking. Các test SQL Server vẫn dùng environment guard; không hard-code credential.

## 6. Production handoff

- Backup + pre-check trước migration incremental.
- Provision admin theo `PRODUCTION_ADMIN_PROVISIONING.md`.
- Không dùng demo account/password từ profile `dev`.
- Ghi lại version baseline/migration, operator, thời điểm và kết quả smoke test trong biên bản triển khai.
