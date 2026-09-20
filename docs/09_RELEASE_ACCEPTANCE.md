# RELEASE ACCEPTANCE - SQL SERVER
**Dự án:** MedClinic - Phòng Khám Tư Nhân
**Ngày nghiệm thu:** 20/09/2026
**Mục tiêu:** Kiểm chứng một hành trình khám ngoại trú hoàn chỉnh trên Microsoft SQL Server thật, xuyên qua các service/transaction chính thay vì chỉ kiểm thử từng module độc lập.

## 1. Kịch bản E2E-01: Complete outpatient visit

Test tự động: `SqlServerReleaseAcceptanceEndToEndIntegrationTest`.

Luồng được thực thi tuần tự:
1. Lấy bệnh nhân, bác sĩ và slot khả dụng từ dữ liệu seed.
2. Bệnh nhân đặt lịch; slot chuyển `AVAILABLE -> BOOKED`.
3. Lễ tân check-in; lịch hẹn chuyển `CONFIRMED -> CHECKED_IN` và được cấp `queue_sequence`/`STT-XX`.
4. Hệ thống phát cập nhật hàng chờ realtime sau commit.
5. Bác sĩ bắt đầu khám; trạng thái chuyển `CHECKED_IN -> IN_PROGRESS`.
6. Bác sĩ lưu bệnh án và kê thuốc; tồn kho bị trừ trong transaction.
7. Ca khám chuyển `IN_PROGRESS -> COMPLETED` và phát cập nhật realtime sau commit.
8. Hệ thống tự tạo hóa đơn `UNPAID` gồm phí khám + phí thuốc.
9. Lễ tân thu tiền mặt; hóa đơn chuyển `UNPAID -> PAID`.
10. Lịch sử bệnh án được truy vấn lại và đơn thuốc/hóa đơn PDF được sinh thành công.

## 2. Invariant được kiểm chứng trực tiếp trong SQL Server

Sau khi hoàn tất flow, test truy vấn database và xác nhận:
- `appointments.status = COMPLETED`.
- `time_slots.status = BOOKED`.
- `queue_sequence` tồn tại và không trùng trong cùng ngày khám.
- Mỗi appointment có đúng một `medical_records`.
- Bệnh án có đúng một `prescriptions` và prescription item tương ứng.
- `medicines.stock_quantity` giảm đúng số lượng kê.
- `prescription_items.unit_price_at_time` giữ đúng snapshot đơn giá tại thời điểm kê.
- `invoices.payment_status = PAID` sau khi thu tiền mặt.
- Tổng hóa đơn bằng phí khám cộng tổng giá thuốc.
- Hai file PDF bắt đầu bằng header `%PDF` và có dữ liệu thực.

## 3. Realtime và xử lý bất đồng bộ

Trong cùng acceptance flow, `SimpMessagingTemplate` được thay bằng test double ở ranh giới transport để xác minh ba lần dispatch tới user destination của bác sĩ: check-in, bắt đầu khám và hoàn tất khám. Business transaction, event `AFTER_COMMIT`, repository và SQL Server vẫn chạy thật.

Email booking tiếp tục chạy sau commit bằng executor riêng `taskExecutor` (`core=4`, `max=10`, `queue=100`, prefix `medclinic-async-`). Cấu hình này tránh xung đột với các executor nội bộ do WebSocket/STOMP tạo ra.

## 4. Cách chạy

Test chỉ chạy khi có `RUN_SQLSERVER_IT=true`; vì vậy CI mặc định không phụ thuộc SQL Server bên ngoài.

Ví dụ với SQL Server Authentication trên một database test riêng:

```powershell
$env:RUN_SQLSERVER_IT="true"
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=medclinic_e2e;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=true;"
$env:DB_USERNAME="<test-user>"
$env:DB_PASSWORD="<local-secret>"
mvn -Dtest=SqlServerReleaseAcceptanceEndToEndIntegrationTest test
```

Không trỏ test này vào production database vì profile acceptance dùng `spring.jpa.hibernate.ddl-auto=create-drop`.

## 5. Kết quả nghiệm thu 20/09/2026

- SQL Server: 17.0.1000.7 Developer Edition.
- Kết nối acceptance local: Windows Integrated Authentication, database `medclinic_e2e`.
- `SqlServerReleaseAcceptanceEndToEndIntegrationTest`: **1/1 PASS**.
- Full `mvn verify`: **128 test case / 55 test class; 120 executed PASS, 8 environment-gated skipped, 0 failure, 0 error**.
- JaCoCo: **78,52% line / 64,22% branch**, vượt gate 70% line / 50% branch.
- `git diff --check`: PASS.

## 6. Docker runtime acceptance

Docker Desktop/WSL2 đã được nghiệm thu thật trên máy QA local. Fresh `docker compose down -v` + `docker compose up -d --build` PASS; SQL Server và application đều `healthy`, init job exit 0, application fresh startup có `RestartCount=0`, Flyway V1/V2/V3 success và named volume giữ dữ liệu qua restart.

Full verify khi bật toàn bộ SQL Server integration tests: **128/128 executed PASS, 0 skipped, 0 failure, 0 error**; JaCoCo **82,55% line / 66,42% branch**. Chi tiết bằng chứng runtime xem `docs/11_DOCKER_RUNTIME_ACCEPTANCE.md`.
