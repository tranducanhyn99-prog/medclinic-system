# SQL Server production-readiness evidence — 19/09/2026

## Scope verified locally

- Target server: local Microsoft SQL Server 17.0.1000.7.
- Clean schema baseline: `SQLSERVER_BASELINE_SCHEMA_2026-09-19.sql`.
- Mandatory reference data: `SQLSERVER_REFERENCE_DATA_2026-09-19.sql`.
- First-admin template: `SQLSERVER_ADMIN_PROVISIONING_TEMPLATE.sql`.
- Application artifact: `target/medclinic-system-1.0.0.jar` with profile `sqlserver`.

## Database evidence

A disposable database was recreated from zero. Baseline apply returned success. Reference-data script was applied twice; the final state contained exactly four role rows, one each for `ROLE_ADMIN`, `ROLE_DOCTOR`, `ROLE_PATIENT`, and `ROLE_STAFF`.

The admin provisioning template was executed with QA-only values and a non-production test hash. The inserted account was active and joined to `ROLE_ADMIN`. A second execution with the same username failed with SQL Server error 51001 (`Admin username already exists`), confirming the duplicate guard. The disposable database and QA helper files were removed after verification.

## Application release smoke

The packaged JAR was started against a clean/reference-data SQL Server database using Windows Integrated Authentication, `SPRING_PROFILES_ACTIVE=sqlserver`, `ddl-auto=validate`, and a QA-only remember-me key. Port 18084 was already occupied, so the smoke run used the verified-free port 18085 instead.

Observed results:

- Spring profile: `sqlserver`.
- JPA `EntityManagerFactory` initialized successfully.
- `MedClinicApplication` started successfully.
- `GET /` → HTTP 200.
- `GET /login` → HTTP 200.
- `GET /register` → HTTP 200.
- `GET /admin/dashboard` unauthenticated → HTTP 302 to `/login`.
- Database after startup: 4 roles, no unexpected dev-seed users.
- Application log contained no dev `DataInitializer` seed execution.

## Still external to local acceptance

The following are intentionally **not** marked as production-complete by this local QA:

- Real production/staging SMTP credentials and an actual outbound email delivery check.
- Secret-manager wiring/rotation policy in the chosen hosting environment.
- Manual screen-reader smoke on the release environment.
- Final production DBA backup/change approval and deployment-window smoke.
- Actual production admin identity and bootstrap-password rotation/handoff.

These require real deployment credentials, infrastructure, or human accessibility testing and must not be simulated as completed evidence.
