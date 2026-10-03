-- Acceptance-only restore; source is the isolated demo database.
SET NOCOUNT ON;
RESTORE DATABASE [medclinic_restore_content_20261002074600]
FROM DISK = N'/var/opt/mssql/backup/medclinic_20261002_074407.bak'
WITH MOVE N'medclinic_db' TO N'/var/opt/mssql/data/medclinic_restore_content_20261002074600.mdf',
     MOVE N'medclinic_db_log' TO N'/var/opt/mssql/data/medclinic_restore_content_20261002074600_log.ldf',
     RECOVERY;
DBCC CHECKDB ([medclinic_restore_content_20261002074600]) WITH NO_INFOMSGS;
IF (SELECT COUNT(*) FROM [medclinic_restore_content_20261002074600].dbo.appointments)
    <> (SELECT COUNT(*) FROM [medclinic_db].dbo.appointments)
    THROW 51000, 'Restored appointment count differs', 1;
IF (SELECT COUNT(*) FROM [medclinic_restore_content_20261002074600].dbo.medical_records)
    <> (SELECT COUNT(*) FROM [medclinic_db].dbo.medical_records)
    THROW 51000, 'Restored medical record count differs', 1;
IF (SELECT COUNT(*) FROM [medclinic_restore_content_20261002074600].dbo.prescriptions)
    <> (SELECT COUNT(*) FROM [medclinic_db].dbo.prescriptions)
    THROW 51000, 'Restored prescription count differs', 1;
IF (SELECT COUNT(*) FROM [medclinic_restore_content_20261002074600].dbo.prescription_items)
    <> (SELECT COUNT(*) FROM [medclinic_db].dbo.prescription_items)
    THROW 51000, 'Restored prescription item count differs', 1;
IF (SELECT COUNT(*) FROM [medclinic_restore_content_20261002074600].dbo.invoices)
    <> (SELECT COUNT(*) FROM [medclinic_db].dbo.invoices)
    THROW 51000, 'Restored invoice count differs', 1;
IF EXISTS (
    SELECT id,stock_quantity FROM [medclinic_db].dbo.medicines
    EXCEPT SELECT id,stock_quantity FROM [medclinic_restore_content_20261002074600].dbo.medicines
) OR EXISTS (
    SELECT id,stock_quantity FROM [medclinic_restore_content_20261002074600].dbo.medicines
    EXCEPT SELECT id,stock_quantity FROM [medclinic_db].dbo.medicines
) THROW 51000, 'Restored stock differs', 1;
IF NOT EXISTS (
    SELECT 1 FROM [medclinic_restore_content_20261002074600].dbo.appointments a
    JOIN [medclinic_restore_content_20261002074600].dbo.invoices i ON i.appointment_id=a.id
    WHERE a.booking_code='MED-261002-6EAF8B62' AND a.status='COMPLETED'
        AND i.invoice_code='INV-20261002-21F4935BB8624CEB'
        AND i.total_amount=353000 AND i.payment_method='CASH' AND i.payment_status='PAID'
        AND i.paid_at IS NOT NULL
) THROW 51000, 'Restored demo invoice differs', 1;
IF NOT EXISTS (
    SELECT 1 FROM [medclinic_restore_content_20261002074600].dbo.medicines
    WHERE id=1 AND stock_quantity=1998
) THROW 51000, 'Restored demo stock differs', 1;
SELECT 'PASS: appointments, medical records, prescriptions, prescription items, invoices and medicine stocks restored correctly' AS acceptance_result;
-- Only the exact temporary database created above is removed.
ALTER DATABASE [medclinic_restore_content_20261002074600] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE [medclinic_restore_content_20261002074600];
