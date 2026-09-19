# Bàn giao trước khi merge — 19/09/2026

## Trạng thái repository

- Nhánh làm việc: `codex/ui-design-documentation`.
- HEAD gốc trước đợt khắc phục này: `f778779` (`origin/codex/ui-design-documentation` theo local refs tại thời điểm kiểm tra).
- Local `main`: `af11b4b` (`origin/main` theo local refs tại thời điểm kiểm tra).
- Thư mục `target/premerge-quarantine/` lưu các helper/artifact chỉ dùng cục bộ và tuyệt đối không đưa vào commit.

## Cổng kiểm chứng trước commit

- `mvn verify`: PASS.
- Surefire: **63 test case, 56 executed PASS, 7 environment-gated skipped, 0 failure, 0 error, 34 test class**.
- `git diff --check` và `git diff --cached --check`: PASS.
- Bằng chứng SQL Server về clean baseline, reference data, admin provisioning template và release smoke được lưu tại `docs/database/SQLSERVER_PRODUCTION_READINESS_2026-09-19.md`.
- Checklist implementation: T01–T22 = 22/22 `[x]`; D01–D04 = 4/4 `[x]`; R01–R14 đã xử lý.

## Phạm vi commit

Batch này chủ động gộp backend hardening, SQL Server production baseline, sửa workflow/concurrency, accessibility/responsive, regression test và tài liệu đồng bộ vì các thay đổi phụ thuộc lẫn nhau và đã được kiểm chứng cùng một lượt.

Các artifact DOCX/PDF/PNG trong `docs/generated/` được giữ lại vì là deliverable học thuật và khớp với trạng thái generator/tài liệu hiện tại.

## Phần vẫn cần nghiệm thu ngoài môi trường local

- Manual screen-reader smoke trên môi trường release.
- Cấu hình SMTP thật và xác minh ít nhất một email outbound.
- Wiring/rotation secret bằng secret manager của hạ tầng đích.
- Provision danh tính admin production thật và rotate bootstrap password trước bàn giao.
- DBA backup/change approval và deployment-window smoke.

MySQL, VNPay/IPN, audit trail, refund/reschedule và WebSocket vẫn là future scope cho đến khi phạm vi dự án được mở lại.

## Mô tả commit đề xuất

`feat: củng cố luồng nghiệp vụ và mức sẵn sàng SQL Server`

Phần mô tả commit nên nêu rõ: hoàn thiện tính nhất quán nghiệp vụ, kiểm soát concurrency/rollback, tăng cường bảo mật và accessibility, bổ sung baseline/runbook SQL Server cùng regression test, đồng thời đồng bộ tài liệu kỹ thuật với source hiện tại.

Sau khi commit, cần `fetch` remote mới nhất rồi mới quyết định merge hay rebase vào nhánh đích. Không dựa vào local refs cũ để kết luận tình trạng remote.
