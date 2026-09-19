-- MedClinic SQL Server reference data
-- Idempotent seed for mandatory application roles.
-- Safe to run repeatedly after the baseline schema is present.

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRANSACTION;

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = N'ROLE_PATIENT')
    INSERT INTO roles(name, description)
    VALUES (N'ROLE_PATIENT', N'Bệnh nhân sử dụng cổng đặt lịch và hồ sơ cá nhân');

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = N'ROLE_DOCTOR')
    INSERT INTO roles(name, description)
    VALUES (N'ROLE_DOCTOR', N'Bác sĩ khám bệnh, lập bệnh án và kê đơn');

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = N'ROLE_STAFF')
    INSERT INTO roles(name, description)
    VALUES (N'ROLE_STAFF', N'Lễ tân tiếp nhận và thu ngân');

IF NOT EXISTS (SELECT 1 FROM roles WHERE name = N'ROLE_ADMIN')
    INSERT INTO roles(name, description)
    VALUES (N'ROLE_ADMIN', N'Quản trị viên hệ thống');

COMMIT TRANSACTION;
