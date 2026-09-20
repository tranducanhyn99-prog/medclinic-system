# Core workflow schema migration

SQL Server là database production hiện tại. Từ 20/09/2026, schema production được quản lý bằng Flyway: database sạch chạy `V1__baseline_schema.sql` rồi `V2__reference_roles.sql`; database legacy 16 bảng chỉ được baseline ở version 1 khi operator bật opt-in sau backup/pre-check rồi chạy V2. Hibernate chỉ dùng `ddl-auto=validate` sau migration. File này được giữ làm **tài liệu lịch sử/pre-check cho schema cũ**, không phải đường deploy mặc định hiện tại. MySQL vẫn là roadmap tương lai.

## Pre-checks

```sql
-- Must be empty before adding the queue unique constraint.
SELECT appointment_date, queue_sequence, COUNT(*)
FROM appointments
WHERE queue_sequence IS NOT NULL
GROUP BY appointment_date, queue_sequence
HAVING COUNT(*) > 1;

-- Historical cancellations may reuse a slot after this migration. Existing active bookings must still be unique.
SELECT time_slot_id, COUNT(*)
FROM appointments
WHERE status <> 'CANCELLED'
GROUP BY time_slot_id
HAVING COUNT(*) > 1;
```

## MySQL 8+ (future compatibility; không phải acceptance gate hiện tại)

```sql
ALTER TABLE appointments DROP INDEX time_slot_id;
ALTER TABLE appointments ADD COLUMN queue_sequence BIGINT NULL;
ALTER TABLE appointments ADD CONSTRAINT uk_appointment_date_queue_sequence
    UNIQUE (appointment_date, queue_sequence);

ALTER TABLE medicines ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE prescription_items ADD COLUMN unit_price_at_time DECIMAL(12,2) NULL;
UPDATE prescription_items pi
JOIN medicines m ON m.id = pi.medicine_id
SET pi.unit_price_at_time = m.unit_price
WHERE pi.unit_price_at_time IS NULL;
ALTER TABLE prescription_items MODIFY unit_price_at_time DECIMAL(12,2) NOT NULL;

CREATE TABLE daily_queue_counters (
    id BIGINT NOT NULL AUTO_INCREMENT,
    queue_date DATE NOT NULL,
    next_sequence BIGINT NOT NULL,
    version BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_daily_queue_counter_date UNIQUE (queue_date)
);

CREATE TABLE invoice_code_reservations (
    invoice_code VARCHAR(30) NOT NULL PRIMARY KEY
);
INSERT IGNORE INTO invoice_code_reservations (invoice_code)
SELECT invoice_code FROM invoices;
```

If `SHOW INDEX FROM appointments` reports a different generated name for the one-to-one index, replace `time_slot_id` in the first statement with that index name.

## SQL Server 2019+ — incremental SQL lịch sử (không chạy trên schema Flyway hiện tại)

Filtered indexes require the standard SQL Server session options below.

```sql
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET QUOTED_IDENTIFIER ON;
SET NUMERIC_ROUNDABORT OFF;

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;

DECLARE @constraint sysname;
SELECT @constraint = kc.name
FROM sys.key_constraints kc
JOIN sys.index_columns ic ON ic.object_id = kc.parent_object_id AND ic.index_id = kc.unique_index_id
JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE kc.parent_object_id = OBJECT_ID('appointments') AND c.name = 'time_slot_id';
IF @constraint IS NOT NULL EXEC('ALTER TABLE appointments DROP CONSTRAINT ' + QUOTENAME(@constraint));

ALTER TABLE appointments ADD queue_sequence BIGINT NULL;
CREATE UNIQUE INDEX uk_appointment_date_queue_sequence
    ON appointments (appointment_date, queue_sequence)
    WHERE queue_sequence IS NOT NULL;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'uk_doctor_license_number' AND object_id = OBJECT_ID('doctor_profiles'))
    CREATE UNIQUE INDEX uk_doctor_license_number ON doctor_profiles (license_number) WHERE license_number IS NOT NULL;

ALTER TABLE medicines ADD version BIGINT NOT NULL CONSTRAINT df_medicines_version DEFAULT 0;
ALTER TABLE prescription_items ADD unit_price_at_time DECIMAL(12,2) NULL;
UPDATE pi SET unit_price_at_time = m.unit_price
FROM prescription_items pi JOIN medicines m ON m.id = pi.medicine_id
WHERE pi.unit_price_at_time IS NULL;
ALTER TABLE prescription_items ALTER COLUMN unit_price_at_time DECIMAL(12,2) NOT NULL;

CREATE TABLE daily_queue_counters (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    queue_date DATE NOT NULL,
    next_sequence BIGINT NOT NULL,
    version BIGINT NULL,
    CONSTRAINT uk_daily_queue_counter_date UNIQUE (queue_date)
);

CREATE TABLE invoice_code_reservations (
    invoice_code VARCHAR(30) NOT NULL PRIMARY KEY
);
INSERT INTO invoice_code_reservations (invoice_code)
SELECT i.invoice_code FROM invoices i
WHERE NOT EXISTS (SELECT 1 FROM invoice_code_reservations r WHERE r.invoice_code = i.invoice_code);
```

## Post-check

Đường deploy hiện tại phải theo `SQLSERVER_RUNBOOK.md`: Flyway migrate trước, Hibernate `validate` sau. `SqlServerFlywayMigrationIntegrationTest` đã kiểm cả fresh schema và legacy adoption trên database disposable riêng; các concurrency/E2E test SQL Server tiếp tục chạy qua environment guard. Phần SQL lịch sử phía trên chỉ dùng để đối chiếu/pre-check các schema rất cũ, không chạy chồng lên database đã ở Flyway version 2. Production deployment approval/SMTP/secrets vẫn là acceptance theo môi trường thật.
