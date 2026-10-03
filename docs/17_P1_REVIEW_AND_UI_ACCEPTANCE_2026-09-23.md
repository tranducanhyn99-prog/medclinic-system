# P1 Review và UI Acceptance — 23/09/2026

## 1. Review nhánh tích hợp

- Nhánh: `feature/frontend-design-system`, HEAD trước checkpoint P1 là `085464f`.
- Sau `git fetch --prune origin`: `origin/main` ở `9cf7520`; feature đi trước main 10 commit và không bị main đi trước. `origin/feature/frontend-design-system` trùng HEAD trước các thay đổi P0/P1 local.
- Diff feature so với main gồm 85 file: design system/responsive UI, regression booking/examination/billing, inventory V4, OTP/profile V5 và test tương ứng.
- Không thấy credential SMTP/SQL thật trong file tracked; Compose chỉ đọc secret từ `.env`. Demo credential `123456` được giới hạn trong profile `dev`/`demo` và tài liệu ghi rõ không dùng production.
- Migration V4/V5 đã được sửa batch separator `GO` sau khi lỗi được tái hiện trên SQL Server 2022. Cần kiểm tra checksum ở môi trường từng áp dụng bản V4/V5 cũ; local DB trước sửa mới ở v3.
- `git diff --check` cho working tree P0/P1: PASS. Hai Markdown hard break cũ trong tài liệu inventory được đổi sang `<br>` để diff từ `origin/main` không còn trailing whitespace.

## 2. Verification

- `mvn verify` mặc định: 139 test case, 131 PASS, 8 SQL Server environment-gated skipped, 0 failure/error.
- `RUN_SQLSERVER_IT=true` trên `medclinic_e2e`: 139/139 PASS, không skip/failure/error.
- JaCoCo: 82,12% line / 64,62% branch; vượt gate 70% / 50%.
- Docker Engine 29.8.0; app, SQL Server 2022 và Mailpit healthy; Flyway V1–V5 success; 18 bảng ứng dụng.
- Smoke HTTP: đăng ký → Mailpit → resend → OTP → upload/thay avatar; PASS. SQL data và avatar giữ nguyên sau `compose down`/`up` không xóa volume.

## 3. UI acceptance thực tế

Kiểm tra trên Codex in-app browser với viewport thật 1440×900, 768×900 và 390×844:

| Màn hình | 1440 | 768 | 390 | Kết quả |
|---|---:|---:|---:|---|
| Trang chủ/public navigation | ✓ | ✓ | ✓ | Không tràn ngang; navbar đổi sang menu mobile; CTA và search không bị cắt. |
| Login | ✓ | — | ✓ | Label/autocomplete/required đầy đủ; submit rỗng giữ trang và focus field sai đầu tiên; tab order đi qua các control. |
| Patient appointments/empty state | — | — | ✓ | Empty state và CTA rõ; không tràn ngang. |
| Patient booking | — | — | ✓ | Select/date/textarea/submit rộng 302 px trong viewport 390; required fields hiện diện. |
| Admin dashboard | ✓ | ✓ | ✓ | KPI/card responsive; sidebar desktop và drawer mobile hoạt động; không tràn ngang. |
| Doctor desk | ✓ | — | — | KPI/hàng chờ/empty state render đầy đủ; không tràn ngang. |
| Staff reception | ✓ | — | — | Search, filter, CTA walk-in/billing và empty state render đầy đủ. |
| Staff billing | ✓ | — | — | KPI và empty state hóa đơn render đầy đủ. |

Không ghi nhận console warning/error trong phiên kiểm tra. Luồng nghiệp vụ đặt lịch → check-in → khám/kê đơn → hóa đơn/thanh toán đã được thực thi trong `SqlServerReleaseAcceptanceEndToEndIntegrationTest`; phiên UI này không ghi thêm bệnh án giả vào database sau khi xác nhận các màn và control cần thiết.

## 4. Giới hạn và rủi ro còn lại

- UI Admin ở 768 px vẫn giữ sidebar rộng khoảng 248 px. Nội dung không tràn ngang nhưng vùng làm việc hẹp; đây là lựa chọn tablet hiện tại, chưa phải lỗi chặn demo.
- `users.phone` chưa có unique constraint. Đăng ký nhiều tài khoản với cùng số điện thoại có thể khiến lookup trả nhiều kết quả khi sửa profile. Demo phải dùng số điện thoại duy nhất; nên xử lý bằng validation và migration mới ở backlog trước khi dùng dữ liệu thật.
- CDN của Bootstrap Icons/Bootstrap có thể làm giao diện thiếu style/icon khi demo offline hoàn toàn và cache chưa có. Nên bundle asset local nếu buổi bảo vệ không bảo đảm Internet.
- CI GitHub cần được quan sát trên pull request; kết quả local không thay cho CI remote.

## 5. Kết luận P1

Nhánh đủ điều kiện tạo pull request sau checkpoint P0/P1. Merge chỉ nên thực hiện khi CI của PR xanh và diff cuối không có thay đổi ngoài phạm vi đã review. Bộ demo tại [16_DEFENSE_DEMO_PACKAGE_2026-09-23.md](16_DEFENSE_DEMO_PACKAGE_2026-09-23.md) ưu tiên đúng phạm vi môn Lập trình Web: UI responsive, Spring MVC/Thymeleaf, form/validation, session/RBAC, AJAX, WebSocket, email, upload và PDF; phần database/transaction được chuyển thành nội dung giải thích hỗ trợ.
