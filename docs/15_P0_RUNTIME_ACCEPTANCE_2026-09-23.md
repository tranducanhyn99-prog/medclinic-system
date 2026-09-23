# Nghiệm thu P0 trên Docker và SQL Server — 23/09/2026

## Môi trường và kết quả

- Windows, Docker Desktop 4.91.0, Docker Engine client/server 29.8.0; Compose với SQL Server 2022 (engine 16.0), Mailpit `v1.31.1` và Java 21.
- `docker compose config --quiet`: PASS. `docker compose up -d --build --remove-orphans`: build thành công. `docker compose ps -a`: app, SQL Server, Mailpit `healthy`; init job kết thúc mã 0.
- `GET http://127.0.0.1:8080/actuator/health`: `UP`; `GET http://127.0.0.1:8025/livez`: HTTP 200.
- SQL Server hiện có được nâng từ Flyway v3 lên v5: cả V4 và V5 `success=1`, 19 bảng vật lý gồm 18 bảng ứng dụng và `flyway_schema_history`. Migration test cũng xác nhận đường DB mới V1–V5 và adoption legacy BASELINE v1 → V2–V5.
- `mvn verify` mặc định: 139 test case, 131 chạy PASS, 8 SQL Server test bị environment guard bỏ qua. `RUN_SQLSERVER_IT=true` trên DB local riêng `medclinic_e2e`: 139/139 PASS, 0 skipped/failure/error. JaCoCo 82,12% line / 64,62% branch. SQL migration test tạo rồi xóa `medclinic_flyway_it`.
- Smoke HTTP trên Compose: đăng ký bệnh nhân mới → nhận email ở Mailpit → resend đổi challenge → OTP mới đăng nhập → upload avatar → thay avatar; tất cả PASS. App chạy bằng UID 10001 (`medclinic`) và ghi được `/app/uploads/avatars`. Sau `docker compose down` rồi `up -d` (không dùng `-v`), file avatar thử nghiệm còn nguyên, profile trong DB và Flyway v5 còn nguyên. Các tài khoản/email/ảnh thử nghiệm đã được dọn sau kiểm tra.

## Lỗi phát hiện và sửa trong lần nghiệm thu

1. Docker Desktop không khởi động vì Windows không truy cập được `sailor-ingest.sock` khi đổi tên thành `.stale`; sau đó gặp `docker-secrets-engine/engine.sock`. Dừng toàn bộ tiến trình Docker, đổi tên hai thư mục runtime lỗi để Docker tạo lại, rồi mở Docker Desktop một lần. Bản sao runtime cũ được giữ tại `%LOCALAPPDATA%\Docker\run.broken-20260923-141940`, `run.broken-20260923-142505` và `%LOCALAPPDATA%\docker-secrets-engine.broken-20260923-142147`. Không reset factory, không xóa volume/image.
2. Khi engine hoạt động, app fail ở V4 với `Invalid column name 'reorder_level'`. SQL Server biên dịch constraint tham chiếu cột vừa tạo trong cùng batch. Thêm `GO` sau lệnh tạo cột/bảng ở V4 và sau tạo bảng ở V5. Trước khi sửa, DB chỉ có history V1–V3; sau khi sửa, V4/V5 apply thành công.
3. Thử smoke nhiều lần với cùng số điện thoại tạo trùng fixture và khiến `findByPhone` trả nhiều dòng khi cập nhật profile. Thử lại bằng số điện thoại riêng cho mỗi tài khoản; upload và thay avatar PASS. Đây là vấn đề dữ liệu fixture của smoke test; ứng dụng hiện chưa áp đặt duy nhất số điện thoại ở bước đăng ký, nên cần xử lý trong backlog nếu nghiệp vụ yêu cầu duy nhất.

## Lưu ý khi triển khai

- V4/V5 đã đổi nội dung để sửa batch SQL Server, nên checksum Flyway cũng đổi. Với DB đã chạy thành công bản V4/V5 trước đó, phải đối chiếu history/checksum và kế hoạch migration riêng trước khi triển khai; không `repair` history tự động. DB local lần này chưa từng áp dụng V4/V5 trước khi sửa.
- Mailpit là hộp thư demo chạy local, không gửi email ra Internet; tin nhắn trong Mailpit không được cấu hình volume bền vững và sẽ mất sau `compose down`. Ứng dụng chỉ giữ file avatar và SQL data qua named volume. Đổi `SPRING_PROFILES_ACTIVE` và SMTP theo môi trường trước khi dùng dữ liệu thật.
- Các bài test chạy ở local; chưa có kết quả CI remote cho thay đổi P0 này.

## Lệnh nghiệm thu chính

```powershell
docker compose config --quiet
docker compose up -d --build --remove-orphans
docker compose ps -a
# Đặt DB_URL tới localhost:14333/medclinic_e2e và DB_USERNAME/DB_PASSWORD theo .env local.
$env:RUN_SQLSERVER_IT="true"
mvn verify
docker compose down
docker compose up -d
```

Chi tiết cấu hình và kiểm tra schema: [SQL Server runbook](database/SQLSERVER_RUNBOOK.md). Các kết quả ở trên là quan sát tại máy local ngày 23/09/2026, không phải cam kết về môi trường khác.
