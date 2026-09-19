# Khởi tạo tài khoản quản trị production

`DataInitializer` chỉ chạy với profile `dev`. Profile `sqlserver` không tự tạo role, tài khoản mẫu, bác sĩ hoặc lịch hẹn. Với database sạch, phải áp dụng theo thứ tự:

1. `SQLSERVER_BASELINE_SCHEMA_2026-09-19.sql` để tạo schema.
2. `SQLSERVER_REFERENCE_DATA_2026-09-19.sql` để tạo idempotent 4 role bắt buộc: `ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_STAFF`, `ROLE_ADMIN`.
3. `SQLSERVER_ADMIN_PROVISIONING_TEMPLATE.sql` để tạo quản trị viên đầu tiên bằng BCrypt hash đã sinh ngoài script.

Không truyền mật khẩu thô vào file SQL hoặc source control. Biến `AdminPasswordHash` phải là BCrypt hash; username/email phải là giá trị production riêng và không trùng dữ liệu hiện có. Template tự dừng nếu thiếu `ROLE_ADMIN`, username/email đã tồn tại hoặc hash không có dạng BCrypt.

Sau provisioning, vận hành viên phải đăng nhập xác minh tài khoản, đổi/rotate bootstrap password qua luồng ứng dụng trước bàn giao và ghi lại ticket/người thực hiện/thời điểm. Schema hiện tại **không có** cờ `must_change_password`, vì vậy việc rotate password là bước deployment bắt buộc chứ không phải enforcement tự động ở lần đăng nhập đầu tiên.

Không dùng lại `admin`, `staff`, `patient`, `doctor` hoặc mật khẩu `123456` trong production. Secrets kết nối database, SMTP và `REMEMBER_ME_KEY` phải đi qua biến môi trường hoặc secret manager của môi trường triển khai; không ghi chúng vào `application.yml`.

Bằng chứng QA ngày 19/09/2026: baseline + reference data apply PASS trên SQL Server local sạch; reference data chạy lặp vẫn giữ đúng 4 role; admin provisioning template insert đúng `ROLE_ADMIN` và duplicate username bị chặn; JAR profile `sqlserver` start PASS với `ddl-auto=validate` và không chạy dev seed. Xem `SQLSERVER_PRODUCTION_READINESS_2026-09-19.md`.
