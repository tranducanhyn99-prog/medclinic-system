# Khởi tạo tài khoản quản trị production

`DataInitializer` chỉ chạy với profile `dev`/`demo`; profile `sqlserver` thuần không tự tạo tài khoản mẫu, bác sĩ hoặc lịch hẹn.

Với database mới, quy trình production là:

1. DBA tạo database rỗng và cấu hình principal/secret kết nối.
2. Khởi động release với profile `sqlserver`: Flyway chạy `V1__baseline_schema.sql` rồi `V2__reference_roles.sql`; Hibernate `validate` sau migration.
3. Chạy `SQLSERVER_ADMIN_PROVISIONING_TEMPLATE.sql` để tạo quản trị viên đầu tiên bằng BCrypt hash đã sinh ngoài script.

Với database legacy 16 bảng, phải backup + pre-check trước. Production mặc định không tự baseline; chỉ bật `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` cho lần adoption đầu tiên để Flyway ghi baseline version 1 và áp dụng V2 trở đi, sau đó bỏ opt-in. Không baseline schema dở dang hoặc không rõ nguồn gốc.

Không truyền mật khẩu thô vào file SQL hoặc source control. Biến `AdminPasswordHash` phải là BCrypt hash; username/email phải là giá trị production riêng và không trùng dữ liệu hiện có. Template tự dừng nếu thiếu `ROLE_ADMIN`, username/email đã tồn tại hoặc hash không có dạng BCrypt.

Sau provisioning, vận hành viên phải đăng nhập xác minh tài khoản, đổi/rotate bootstrap password qua luồng ứng dụng trước bàn giao và ghi lại ticket/người thực hiện/thời điểm. Schema hiện tại **không có** cờ `must_change_password`, vì vậy việc rotate password là bước deployment bắt buộc chứ không phải enforcement tự động ở lần đăng nhập đầu tiên.

Không dùng lại `admin`, `staff`, `patient`, `doctor` hoặc mật khẩu `123456` trong production. Secrets kết nối database, SMTP và `REMEMBER_ME_KEY` phải đi qua biến môi trường hoặc secret manager; không ghi chúng vào `application.yml`.

`SQLSERVER_BASELINE_SCHEMA_2026-09-19.sql` và `SQLSERVER_REFERENCE_DATA_2026-09-19.sql` trong `docs/database/` vẫn là bản review/audit; contract test bảo đảm chúng đồng bộ với Flyway V1/V2. Xem `SQLSERVER_RUNBOOK.md` và `SQLSERVER_PRODUCTION_READINESS_2026-09-19.md`.
