# Work Progress — Closed 2026-09-18

## Closure status

The earlier pause has been resolved. Keep the current working tree; do not reset or discard changes.

The canonical status source is `docs/08_SOURCE_REVIEW_TASKLIST_2026-09-17.md`. `CHECKLISTNEW.md` is no longer used.

## Verified state

- Full Maven suite (19/09 direct continuation): **63 total, 56 executed pass, 0 failures, 0 errors, 7 environment-gated skipped; 34 test classes**.
- `git diff --check`: passed at the last completed audit.
- Checklist counts: **T01–T22: 22 done, 0 partial, 0 open**; **D01–D04: 4 done, 0 partial, 0 open**.
- All R01–R14 regressions have source-level fixes; R03, R04, and R06 have additional tests.
- Clean local public-page p95: `/` 39.851 ms, `/login` 23.483 ms, `/register` 23.942 ms.
- Authenticated MockMvc portal p95: booking 97.846 ms, reception 73.677 ms, doctor desk 41.434 ms.

## Last completed implementation work

- Atomic examination, prescription, stock, and invoice workflow with rollback coverage.
- Invoice-code reservation uses a separate `REQUIRES_NEW` writer and collision retry.
- Daily queue counter uses a separate `REQUIRES_NEW` writer and first-row collision retry.
- Booking/cancellation email is sent after commit and is fail-safe.
- Walk-in accounts are inactive and do not expose/default to password `123456`.
- Admin doctor creation, validation, service authorization, CSRF hardening, Unicode PDF font bundling, and production-safe dev seeding.
- Responsive/accessibility layout contracts and authenticated performance baseline tests.
- Database design/architecture/generators updated for the current schema.
- Form error round-trips, lazy entity graphs, and method-level authorization now have integration/controller coverage.

## D03 closure evidence

All five generated DOCX files now open read-only in Microsoft Word COM with `OpenAndRepair=false` and export successfully with `ExportAsFixedFormat` to `docs/generated/rendered`. The rendered PDFs have 20, 5, 9, 14 and 6 pages respectively; `pypdf.PdfReader` extracted non-empty text from each; `pdftoppm` produced the five first-page previews in `docs/generated/rendered/previews`. OpenXML package and relationship checks also pass. D03 is therefore complete.

## Known external verification limits

- SQL Server is the current/production database target. Clean baseline apply, Hibernate schema validation, T10/T11/T15 SQL Server integration verification and SQL Server performance baseline all pass. MySQL is deferred to future scope.
- Public pages plus representative Patient/Staff/Doctor/Admin portal snapshots have been verified with Edge CDP at 390×844 and 768×1024. Expanded rendered-HTML accessibility audit covers 8 portal pages with `TOTAL_ISSUES=0`; manual screen-reader smoke remains a pre-release acceptance step rather than an open implementation task.
- SQL Server first-admin provisioning template and mandatory role seed were runtime-QA on a disposable database; actual production identity/password rotation remains an operational deployment step.
- Do not run the untracked `apply_ui_fixes.ps1`; it was identified as an unsafe/broken bulk-edit helper.

## Direct continuation 19/09/2026

- T11 full Spring/JDBC medicine-stock concurrency on real SQL Server passed using Windows Integrated Authentication; no oversell.
- SQL Server baseline generated from current JPA metadata was applied to a clean QA database: 16 tables, 17 foreign keys, filtered queue unique index; Hibernate `ddl-auto=validate` reached successful `EntityManagerFactory` initialization.
- T10 invoice-code collision/retry verified on real local SQL Server: PASS.
- T15 concurrent booking verified on real local SQL Server: PASS.
- Added H2 application-level stock oversell concurrency coverage plus SQL Server DB-level `UPDLOCK/HOLDLOCK` harness: one updater, second transaction reads stock 0, final stock 0.
- Public + representative authenticated portal browser QA with Edge CDP at 390×844 and 768×1024 passed without horizontal overflow; accessibility audit is clean after semantic fixes.
- SQL Server profile properties now use environment variables instead of checked-in placeholder credentials.
- Added idempotent SQL Server reference-data script for all four mandatory roles; repeated apply leaves exactly one row per role.
- Admin provisioning template runtime-QA passed: active `ROLE_ADMIN` insert succeeds and duplicate username is rejected with SQL Server error 51001.
- Packaged JAR release smoke with profile `sqlserver` passed on local SQL Server: `/`, `/login`, `/register` = 200; unauthenticated `/admin/dashboard` = 302 to login; no dev seed executed.
