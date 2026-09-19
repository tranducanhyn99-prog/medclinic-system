-- MedClinic SQL Server production-admin provisioning template.
-- Run with sqlcmd variables; AdminPasswordHash MUST already be BCrypt.
-- Never place a plaintext password in this script or source control.
-- Example variable names: AdminUsername, AdminEmail, AdminFullName,
-- AdminPhone, AdminPasswordHash.

SET NOCOUNT ON;
SET XACT_ABORT ON;

DECLARE @roleId BIGINT;
SELECT @roleId = id FROM roles WHERE name = N'ROLE_ADMIN';

IF @roleId IS NULL
    THROW 51000, 'ROLE_ADMIN is missing. Apply SQLSERVER_REFERENCE_DATA_2026-09-19.sql first.', 1;

IF EXISTS (SELECT 1 FROM users WHERE username = N'$(AdminUsername)')
    THROW 51001, 'Admin username already exists.', 1;

IF EXISTS (SELECT 1 FROM users WHERE email = N'$(AdminEmail)')
    THROW 51002, 'Admin email already exists.', 1;

IF N'$(AdminPasswordHash)' NOT LIKE N'$2%$%'
    THROW 51003, 'AdminPasswordHash does not look like a BCrypt hash.', 1;

BEGIN TRANSACTION;
INSERT INTO users (
    username, password, full_name, email, phone,
    role_id, is_active, created_at, updated_at
)
VALUES (
    N'$(AdminUsername)', N'$(AdminPasswordHash)', N'$(AdminFullName)',
    N'$(AdminEmail)', NULLIF(N'$(AdminPhone)', N''),
    @roleId, 1, SYSDATETIME(), SYSDATETIME()
);

DECLARE @newAdminId BIGINT = SCOPE_IDENTITY();

COMMIT TRANSACTION;

SELECT u.id, u.username, u.email, u.full_name, u.is_active, r.name AS role_name
FROM users u
JOIN roles r ON r.id = u.role_id
WHERE u.id = @newAdminId;

-- Operational handoff requirement:
-- verify login, rotate the bootstrap password through the application,
-- then record the provisioning ticket/operator/time. The current schema does
-- not contain a must-change-password flag, so rotation is a deployment step.
