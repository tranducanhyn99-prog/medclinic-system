# Kiểm thử MedClinic

Chạy tại gốc repo với JDK 21, Maven và Node.js 22. Source kiểm thử được giữ trong Git để chạy lại sau khi clone; log, ảnh và báo cáo sinh tự động giữ local.

## Bộ kiểm tra mặc định

```powershell
mvn --batch-mode --no-transfer-progress clean verify
node --test src/test/frontend/ui-regressions.cjs
node ops/check-doc-links.cjs
git diff --check
```

Maven chạy JUnit/Mockito/MockMvc, tích hợp H2, đóng gói JAR và gate JaCoCo **LINE ≥70%, BRANCH ≥50%**. Frontend regression kiểm tra JavaScript và hợp đồng giao diện. Kiểm tra link xác nhận đích local trong README/tài liệu tồn tại và không bị ignore.

Báo cáo Java: `target/surefire-reports`; coverage: `target/site/jacoco/index.html`; JAR: `target/medclinic-system-1.0.0.jar`. Đây là output build. [CI](../.github/workflows/ci.yml) chạy ba bộ chính và lưu artifact theo từng lượt.

## Test SQL Server

Các test `SqlServer*` có environment guard nên mặc định skipped. Cần SQL Server local và database kiểm thử riêng, có thể hủy, theo [runbook](database/SQLSERVER_RUNBOOK.md). Đọc guard của class vì tên database và biến môi trường có thể khác giữa các nhóm. Không trỏ test vào database đang sử dụng.

```powershell
$env:RUN_SQLSERVER_IT = 'true'
# Thiết lập DB_URL, DB_USERNAME, DB_PASSWORD cho đúng database test.
mvn "-Dtest=SqlServer*" test
```

Test H2 không thay thế xác nhận schema/migration, cạnh tranh và rollback trên SQL Server. **Executed = reported − skipped**; skipped không tính là pass.

## Trình duyệt và hiệu năng

[run-playwright-check.ps1](../ops/run-playwright-check.ps1) cần Windows, Edge và ứng dụng demo + Mailpit local theo README. Script cài Playwright CLI phiên bản cố định vào `output` nếu chưa có; `-WithFixture` tạo H2 riêng để chạy nghiệp vụ.

```powershell
./ops/run-playwright-check.ps1 -WithFixture
```

Kết quả ở `output/playwright-check`; phạm vi gồm public, bốn vai trò, kích thước màn hình, theme và luồng ca khám. Chỉ dùng môi trường demo. Thời gian MockMvc/HTML local không đại diện tải đồng thời hoặc hiển thị toàn trang.

## Kết quả đợt dọn repo 15/10/2026

Chạy trên Windows, JDK 21.0.12.1, Maven 3.9.16; `RUN_SQLSERVER_IT=false`. Kết quả từ source sau dọn:

| Kiểm tra | Kết quả | Phạm vi |
| --- | --- | --- |
| `mvn clean verify` | **BUILD SUCCESS**; 332 reported, **282 executed/pass**, 50 skipped; 0 failure/error | 93 class có báo cáo; SQL Server có guard không được tính là đã chạy. |
| JaCoCo | **88,67% line; 72,27% branch**, cả hai gate đạt | Báo cáo của chính lượt verify này. |
| Frontend regression | **26/26 pass** | Node test runner, không phải kiểm thử trình duyệt thật. |
| Link tài liệu | **77 đích local hợp lệ** | README và Markdown trong docs sau hợp nhất. |
| `git diff --check` | Đạt | Không phát hiện lỗi whitespace trong diff. |

Đã kiểm tra danh sách xuất bản: 555 file trước dọn, bỏ 150 file cũ, thêm 2 hướng dẫn, còn **407 file**. Không xóa source ứng dụng/test, migration, script vận hành, license hoặc 5 báo cáo DOCX. `.env`, backup và dữ liệu local vẫn giữ trên máy, được ignore. Manifest dọn file và log/test-summary của lượt chạy nằm local trong `output/repo-cleanup-2026-10-15`.

Đợt này tập trung vào nội dung GitHub và hướng dẫn thuyết trình. Google/SMTP/ngân hàng thật, camera, backup/restore, benchmark tải và GitHub Actions của commit xuất bản cần được xác nhận theo môi trường tương ứng.

## Bổ sung OTP trên SQL Server — 15/10/2026

Đã thêm hai lớp kế thừa bộ test hiện có, dùng Flyway V1–V10 và Hibernate validate. Cả hai yêu cầu `RUN_SQLSERVER_IT=true` cùng URL localhost trỏ đúng database test; không chạy với database ứng dụng.

| Bộ kiểm tra | Database riêng | Kết quả |
| --- | --- | --- |
| [SqlServerOtpAuthenticationIntegrationTest](../src/test/java/com/medclinic/SqlServerOtpAuthenticationIntegrationTest.java) | `medclinic_otp_it` | **5/5 thực thi đạt, 0 skipped**: đăng ký, magic link, vô hiệu mã cũ khi resend, ràng buộc phiên, giới hạn gửi lại. |
| [SqlServerGoogleCallbackProtocolIntegrationTest](../src/test/java/com/medclinic/SqlServerGoogleCallbackProtocolIntegrationTest.java) | `medclinic_google_otp_it` | **7/7 thực thi đạt, 0 skipped**: Google chờ OTP, sai phiên/mã, tài khoản khóa, lỗi phát hành challenge, nonce/chữ ký giả. |
| Bộ mặc định sau bổ sung | H2/MockMvc và unit test | **344 reported, 282 executed/pass, 62 SQL skipped**, 0 failure/error. Trong 62 bài skipped của lượt mặc định, 12 bài ở hai dòng trên đã chạy riêng trên SQL Server. |

Java test giả lập `EmailService` và nhà cung cấp Google để kiểm tra dữ liệu, giao thức và quyền trên SQL Server thật. Điều đó không chứng minh đã gửi Gmail hoặc đăng nhập Google thật. Phiên quay video dùng database thứ ba `medclinic_video_20261015`, SMTP thật gửi vào Mailpit local riêng; không gửi ra hộp thư bên ngoài.

Đọc trực tiếp database OTP sau lượt test xác nhận SQL Server `16.0.4295.3`, 10 migration thành công và 9 challenge có `code_hash` đúng dạng BCrypt 60 ký tự, `magic_token_hash` dài 64 ký tự. Kiểm tra tổng hợp không xuất mã, token hoặc giá trị hash ra log.

Phiên trình duyệt quay video đạt **67 mốc kiểm tra** trên SQL Server: đăng ký/OTP qua Mailpit, hồ sơ và mật khẩu, đặt/hủy lịch, quét QR mô phỏng, WebSocket ở phiên riêng, khám/kê đơn, tiền mặt/PDF, vãng lai, callback ngân hàng mô phỏng và quản trị. Cảnh ảnh đại diện kiểm tra từ chối MIME không khớp, sau đó bổ sung JPEG hợp lệ, tải lại trang và đọc đúng byte ảnh từ backend. Đây là kiểm tra của phiên quay, không cộng vào số JUnit ở bảng trên. SQL sau quay xác nhận ca chính `COMPLETED`, hóa đơn `PAID` 353.000 đồng và hai yêu cầu chuyển khoản mô phỏng `CONFIRMED`. Xem [kịch bản và lời thuyết minh](KICH_BAN_QUAY_VIDEO.md).

Log và báo cáo XML của lượt SQL được giữ local trong `output/video-demo-v2`; cách đặt biến môi trường xem [runbook](database/SQLSERVER_RUNBOOK.md). Chạy từng lớp với URL database tương ứng.
