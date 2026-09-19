# 📋 BÁO CÁO ĐỀ XUẤT CẢI THIỆN DỰ ÁN MEDCLINIC

> Báo cáo là đề xuất cải tiến lịch sử. Trạng thái implementation và kiểm chứng hiện tại được đối chiếu tại `08_SOURCE_REVIEW_TASKLIST_2026-09-17.md`; quyết định phạm vi và bằng chứng nằm tại `08_SCOPE_CONFLICT_DECISIONS.md`.

---

## MỤC LỤC

1. [Tổng quan đánh giá hiện trạng](#1-tổng-quan-đánh-giá-hiện-trạng)
2. [Nhóm 1: Mã nguồn — Khoảng cách giữa Thiết kế và Triển khai](#nhóm-1-mã-nguồn--khoảng-cách-giữa-thiết-kế-và-triển-khai)
3. [Nhóm 2: Tài liệu kỹ thuật — Lỗ hổng và Mâu thuẫn chéo](#nhóm-2-tài-liệu-kỹ-thuật--lỗ-hổng-và-mâu-thuẫn-chéo)
4. [Nhóm 3: Kiểm thử — Test Coverage gần như bằng 0](#nhóm-3-kiểm-thử--test-coverage-gần-như-bằng-0)
5. [Nhóm 4: Bảo mật và Xử lý lỗi](#nhóm-4-bảo-mật-và-xử-lý-lỗi)
6. [Nhóm 5: Trải nghiệm người dùng (UX) và Frontend](#nhóm-5-trải-nghiệm-người-dùng-ux-và-frontend)
7. [Nhóm 6: Cơ sở dữ liệu và Nghiệp vụ](#nhóm-6-cơ-sở-dữ-liệu-và-nghiệp-vụ)
8. [Nhóm 7: DevOps, CI/CD và Triển khai](#nhóm-7-devops-cicd-và-triển-khai)
9. [Ma trận ưu tiên tổng hợp](#ma-trận-ưu-tiên-tổng-hợp)

---

## 1. Tổng quan đánh giá hiện trạng

### Điểm mạnh đã có ✅
- Thiết kế CSDL đạt chuẩn 3NF với chứng minh học thuật rõ ràng (13 bảng, 14 FK).
- Cơ chế chống Double Booking (Optimistic Lock `@Version` + Unique Constraint) được thiết kế nhất quán xuyên suốt cả 5 tài liệu.
- Bộ SRS IEEE 830 liệt kê đầy đủ 24 FR + 10 NFR với mã hóa rõ ràng.
- Sơ đồ UML (Class, Sequence, Activity) sát kiến trúc Spring Boot thực tế.
- Kiến trúc phân tầng Layered Architecture + MVC được mô tả chi tiết.
- Bộ generator Java tự động tạo 5 file DOCX + 4 biểu đồ PNG nhúng vào Word.

### Vấn đề cốt lõi cần giải quyết ⚠️

| Khía cạnh | Hiện trạng | Mức độ |
|:---|:---|:---:|
| **Mã nguồn triển khai** | Chỉ có 2/~15 Controller, 1/~8 Service, 0 trang nghiệp vụ | 🔴 Nghiêm trọng |
| **Kiểm thử** | 1 file test duy nhất, 0 integration test | 🔴 Nghiêm trọng |
| **Giao diện Thymeleaf** | Chỉ có 8 template (login, register, home, layout), thiếu ~25 trang | 🔴 Nghiêm trọng |
| **Tài liệu mâu thuẫn chéo** | 6 điểm không nhất quán giữa các tài liệu | 🟡 Trung bình |
| **Bảo mật nâng cao** | Thiếu rate limiting, input validation, XSS protection | 🟡 Trung bình |
| **DevOps** | Không có Dockerfile, CI/CD, hoặc deployment guide | 🟡 Trung bình |

---

## Nhóm 1: Mã nguồn — Khoảng cách giữa Thiết kế và Triển khai

> [!CAUTION]
> Đây là vấn đề nghiêm trọng nhất. Tài liệu thiết kế rất đầy đủ nhưng mã nguồn triển khai mới chỉ hoàn thành khoảng **15-20%** so với đặc tả.

### 1.1. Các Controller còn thiếu (Ưu tiên: 🔴 Cao)

Hiện tại chỉ có 2 Controller: [AuthController.java](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/src/main/java/com/medclinic/controller/AuthController.java) và [HomeController.java](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/src/main/java/com/medclinic/controller/HomeController.java).

Theo RTM ([04_RTM_MATRIX.md](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs/04_RTM_MATRIX.md)), cần bổ sung tối thiểu:

| Controller cần tạo | Phân hệ | FR ánh xạ | Trạng thái |
|:---|:---|:---|:---:|
| `PatientAppointmentController` | Đặt lịch khám | FR-BOOK-01 → FR-BOOK-06 | ❌ Chưa có |
| `PatientDashboardController` | Dashboard bệnh nhân | FR-AUTH-03 | ❌ Chưa có |
| `DoctorDeskController` | Bàn khám bác sĩ | FR-EXAM-01 → FR-EXAM-04 | ❌ Chưa có |
| `DoctorScheduleController` | Lịch trực bác sĩ | FR-DOC-03 | ❌ Chưa có |
| `StaffReceptionController` | Quầy tiếp nhận | FR-REC-01 → FR-REC-03 | ❌ Chưa có |
| `StaffBillingController` | Thu viện phí | FR-BILL-01 → FR-BILL-03 | ❌ Chưa có |
| `AdminController` | Quản trị hệ thống | FR-SPEC-01, FR-DOC-01, FR-ADMIN-01→03 | ❌ Chưa có |
| `ApiController` (REST) | API slot trống | FR-BOOK-01 (AJAX) | ❌ Chưa có |
| `PdfExportController` | Xuất PDF | FR-EXAM-04, FR-BILL-03 | ❌ Chưa có |

### 1.2. Các Service còn thiếu (Ưu tiên: 🔴 Cao)

Hiện tại chỉ có [UserService.java](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/src/main/java/com/medclinic/service/UserService.java) và [UserServiceImpl.java](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/src/main/java/com/medclinic/service/impl/UserServiceImpl.java).

| Service cần tạo | Trách nhiệm chính |
|:---|:---|
| `AppointmentService` | Đặt lịch, hủy lịch, check-in, cấp STT, walk-in |
| `ScheduleService` | Tạo/quản lý ca trực, sinh time_slots tự động 30 phút |
| `MedicalRecordService` | Ghi bệnh án, kê đơn thuốc, trừ kho dược |
| `InvoiceService` | Sinh hóa đơn tự động, xử lý thanh toán |
| `SpecialtyService` | CRUD chuyên khoa |
| `DoctorService` | CRUD hồ sơ bác sĩ, lọc/tìm kiếm |
| `MedicineService` | CRUD thuốc, quản lý tồn kho |
| `EmailService` | Gửi email xác nhận (`@Async`) |
| `PdfExportService` | Xuất đơn thuốc PDF, hóa đơn PDF |
| `DashboardService` | Thống kê KPI, doanh thu cho Admin |
| `QrCodeUtil` | Sinh mã QR Base64 bằng ZXing |

### 1.3. Các trang Thymeleaf còn thiếu (Ưu tiên: 🔴 Cao)

Hiện chỉ có 8 template HTML. Cần bổ sung tối thiểu:

**Khu vực Bệnh nhân (`/patient/`):**
- `patient/dashboard.html` — Trang chủ bệnh nhân
- `patient/booking.html` — Form đặt lịch khám (chọn bác sĩ, ngày, slot)
- `patient/appointment-detail.html` — Chi tiết lịch hẹn + mã QR
- `patient/appointments.html` — Danh sách lịch hẹn (tab: sắp tới / đã khám / đã hủy)
- `patient/medical-history.html` — Lịch sử bệnh án

**Khu vực Bác sĩ (`/doctor/`):**
- `doctor/desk.html` — Bàn khám (hàng chờ STT)
- `doctor/examine.html` — Form khám bệnh, chẩn đoán, kê đơn thuốc
- `doctor/schedule.html` — Xem lịch trực cá nhân

**Khu vực Lễ tân (`/staff/`):**
- `staff/reception.html` — Quầy tiếp nhận (tra cứu + check-in)
- `staff/walk-in.html` — Đăng ký vãng lai
- `staff/billing.html` — Danh sách hóa đơn chờ thanh toán

**Khu vực Admin (`/admin/`):**
- `admin/dashboard.html` — Dashboard KPI + biểu đồ Chart.js
- `admin/specialties.html` — Quản lý chuyên khoa
- `admin/doctors.html` — Quản lý bác sĩ
- `admin/medicines.html` — Quản lý kho thuốc
- `admin/users.html` — Quản lý tài khoản

---

## Nhóm 2: Tài liệu kỹ thuật — Lỗ hổng và Mâu thuẫn chéo

### 2.1. WebSocket "mồ côi" (Ưu tiên: 🟡 Trung bình)

**Vấn đề:** Trong [01_SRS_IEEE830.md](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs/01_SRS_IEEE830.md) mục 2.1 có đề cập *"WebSocket Client"* tại Presentation Layer, nhưng:
- Không có bất kỳ FR nào sử dụng WebSocket.
- Không có đề cập trong [05_SYSTEM_ARCHITECTURE.md](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs/05_SYSTEM_ARCHITECTURE.md).
- Không có code triển khai.

**Đề xuất:** Hoặc (a) **xóa** thuật ngữ "WebSocket Client" khỏi SRS mục 2.1, hoặc (b) **bổ sung FR mới** cho tính năng gọi số bệnh nhân realtime trên màn hình phòng chờ (rất ấn tượng nếu demo được).

### 2.2. Actor `GUEST` biến mất khỏi Use Case Diagram (Ưu tiên: 🟡 Trung bình)

**Vấn đề:** SRS bảng 2.2 liệt kê 5 Actor gồm `GUEST`, nhưng sơ đồ Use Case tổng quan trong [02_UML_DESIGN.md](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs/02_UML_DESIGN.md) mục 1.1 chỉ có 4 Actor (Patient, Staff, Doctor, Admin).

**Đề xuất:** Thêm Actor `Guest((Khách))` vào sơ đồ Mermaid với liên kết tới UC1 (Đăng ký) và UC2 (Tra cứu Bác sĩ).

### 2.3. `FR-ADMIN-02` bị bỏ sót trong RTM (Ưu tiên: 🟡 Trung bình)

**Vấn đề:** Trong [04_RTM_MATRIX.md](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs/04_RTM_MATRIX.md), danh sách nhảy từ `FR-ADMIN-01` thẳng sang `FR-ADMIN-03`. Yêu cầu `FR-ADMIN-02` *(Biểu đồ Chart.js: phân bổ ca khám theo chuyên khoa)* bị khuyết.

**Đề xuất:** Thêm dòng:
```
| FR-ADMIN-02 | Biểu đồ phân bổ ca khám & tỷ lệ trạng thái | UC17 | appointments, invoices | DashboardService, AdminController | TC-STAT-02 |
```

### 2.4. Bảng `notifications` không nhất quán giữa các tài liệu (Ưu tiên: 🟡 Trung bình)

**Vấn đề:**
- ✅ ERD trong [03_DATABASE_DESIGN_3NF.md](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs/03_DATABASE_DESIGN_3NF.md) có `NOTIFICATIONS`.
- ✅ Entity [Notification.java](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/src/main/java/com/medclinic/entity/Notification.java) tồn tại.
- ✅ Repository [NotificationRepository.java](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/src/main/java/com/medclinic/repository/NotificationRepository.java) tồn tại.
- ❌ **Thiếu** trong Data Dictionary (mục 3 của 03_DATABASE_DESIGN_3NF.md).
- ❌ **Thiếu** trong Class Diagram (02_UML_DESIGN.md).
- ❌ **Thiếu** trong SRS — không có FR nào cho Notification.
- ❌ **Thiếu** trong RTM — không có ánh xạ Test Case.

**Đề xuất:** Bổ sung mục 3.12 `notifications` vào Data Dictionary, thêm class `Notification` vào Class Diagram, thêm FR-NOTIF-01 vào SRS, và ánh xạ trong RTM.

### 2.5. Thiếu đặc tả tích hợp VNPay (Ưu tiên: 🟢 Thấp)

**Vấn đề:** FR-BILL-02 đề cập *"VNPay sandbox"* nhưng không có đặc tả kỹ thuật nào cho:
- Luồng redirect tới cổng thanh toán VNPay.
- Xử lý callback IPN (Instant Payment Notification).
- Kiểm tra chữ ký điện tử HMAC-SHA512.
- Xử lý timeout / thanh toán thất bại.

**Đề xuất:** Bổ sung mục phụ trong SRS hoặc tạo tài liệu `06_VNPAY_INTEGRATION.md` riêng.

### 2.6. Bảng `prescriptions` & `prescription_items` thiếu format chuẩn (Ưu tiên: 🟢 Thấp)

**Vấn đề:** Mục 3.10 trong [03_DATABASE_DESIGN_3NF.md](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs/03_DATABASE_DESIGN_3NF.md) viết vắn tắt bằng gạch đầu dòng, khác hoàn toàn format bảng Markdown của 9 bảng còn lại.

**Đề xuất:** Tách thành 2 bảng Markdown riêng biệt với đầy đủ cột: Tên Cột, Kiểu Dữ Liệu, Ràng Buộc, Ý Nghĩa.

---

## Nhóm 3: Kiểm thử — Test Coverage gần như bằng 0

> [!WARNING]
> RTM liệt kê các Test Case ID như kế hoạch. Working tree hiện có thêm các test unit/controller và hai lớp integration `ConcurrentBookingIntegrationTest`/`StartExaminationIntegrationTest`; chúng chưa được xem là bằng chứng nghiệm thu cho đến khi build và lần chạy phù hợp thành công. Vì vậy không dùng số lượng file test để suy ra coverage hay chất lượng luồng nghiệp vụ.

### 3.1. Bổ sung Unit Test cho các Service (Ưu tiên: 🔴 Cao)

| File test cần tạo | Service được test | Kịch bản quan trọng |
|:---|:---|:---|
| `AppointmentServiceTest.java` | `AppointmentService` | Đặt lịch thành công, Double Booking bị chặn, hủy lịch trả slot |
| `MedicalRecordServiceTest.java` | `MedicalRecordService` | Ghi bệnh án, kê đơn trừ kho, kho không đủ |
| `InvoiceServiceTest.java` | `InvoiceService` | Sinh hóa đơn tự động, thanh toán CASH, VNPay |
| `ScheduleServiceTest.java` | `ScheduleService` | Tạo ca trực, sinh 8 slot/ca, trùng lịch bị chặn |

### 3.2. Bổ sung Integration Test với H2 (Ưu tiên: 🟡 Trung bình)

```java
// Ý tưởng test đồng thời chống Double Booking
@SpringBootTest
@Transactional
class ConcurrencyBookingIntegrationTest {
    // Mô phỏng 2 luồng cùng đặt 1 slot → 1 thành công, 1 ném exception
}
```

### 3.3. Tạo file Test Plan (Ưu tiên: 🟡 Trung bình)

Tạo tài liệu `06_TEST_PLAN.md` ánh xạ 24 TC-ID trong RTM thành các kịch bản test cụ thể (Given-When-Then) với dữ liệu test mẫu.

---

## Nhóm 4: Bảo mật và Xử lý lỗi

### 4.1. Thiếu Input Validation trên DTO (Ưu tiên: 🔴 Cao)

**Vấn đề:** [UserRegisterDto.java](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/src/main/java/com/medclinic/dto/UserRegisterDto.java) hiện là DTO duy nhất. Cần kiểm tra xem đã có annotation `@Valid`, `@NotBlank`, `@Email`, `@Size` chưa.

**Đề xuất:** Bổ sung Jakarta Bean Validation cho mọi DTO đầu vào:

```java
public class BookingRequestDto {
    @NotNull(message = "Bác sĩ không được để trống")
    private Long doctorId;
    
    @NotNull @Future(message = "Ngày khám phải là ngày trong tương lai")
    private LocalDate appointmentDate;
    
    @NotNull(message = "Vui lòng chọn khung giờ")
    private Long timeSlotId;
    
    @NotBlank @Size(max = 500)
    private String reason;
}
```

### 4.2. Thiếu Rate Limiting cho API (Ưu tiên: 🟡 Trung bình)

**Vấn đề:** Endpoint AJAX lấy slot trống (`GET /api/public/doctors/{id}/slots`) công khai, có thể bị DDoS/brute-force.

**Đề xuất:** Sử dụng Bucket4j hoặc Spring Cloud Gateway rate limiter.

### 4.3. Thiếu Global Exception Handler cho lỗi nghiệp vụ (Ưu tiên: 🟡 Trung bình)

**Vấn đề:** Có [GlobalExceptionHandler.java](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/src/main/java/com/medclinic/exception/GlobalExceptionHandler.java) nhưng cần kiểm tra đã xử lý đầy đủ:
- `OptimisticLockingFailureException` → Thông báo "Khung giờ vừa bị đặt"
- `SlotAlreadyBookedException` → Custom exception
- `InsufficientStockException` → Thuốc không đủ tồn kho
- `DataIntegrityViolationException` → Trùng unique constraint

### 4.4. Bổ sung CORS Configuration cho REST API (Ưu tiên: 🟢 Thấp)

Nếu sau này mở rộng thành SPA hoặc mobile app, cần cấu hình CORS trong `SecurityConfig`.

---

## Nhóm 5: Trải nghiệm người dùng (UX) và Frontend

### 5.1. Thiếu trang Dashboard riêng cho từng vai trò (Ưu tiên: 🔴 Cao)

**Vấn đề:** [SecurityConfig.java](file:///c:/Users/DUCANHZZ/Downloads/LTW/PRJ/src/main/java/com/medclinic/config/SecurityConfig.java) chuyển hướng đăng nhập theo vai trò, nhưng chưa có trang dashboard tương ứng.

**Đề xuất:**
- `/patient/dashboard` — Lịch hẹn sắp tới, đơn thuốc gần nhất, nút đặt lịch nhanh.
- `/doctor/desk` — Hàng chờ khám hôm nay, số bệnh nhân đã check-in.
- `/staff/reception` — Ô tìm kiếm lịch hẹn, nút check-in nhanh.
- `/admin/dashboard` — 4 KPI card + 2 biểu đồ Chart.js.

### 5.2. Cải thiện luồng đặt lịch với UX hiện đại (Ưu tiên: 🟡 Trung bình)

**Đề xuất:**
- Wizard 3 bước: *Chọn chuyên khoa/Bác sĩ → Chọn ngày & slot → Xác nhận*
- Slot trống hiển thị dạng lưới (grid) với badge màu (xanh = trống, đỏ = đã đặt).
- Animation loading khi AJAX tải slot.
- Countdown timer 5 phút "giữ chỗ tạm" để tránh mất slot khi đang điền form.

### 5.3. Responsive cho thiết bị di động (Ưu tiên: 🟡 Trung bình)

**Trạng thái cập nhật:** Layout public và portal đã có breakpoint chung, menu sidebar mobile với overlay/ESC và hỗ trợ focus/giảm chuyển động. Cần smoke test các viewport bên dưới trước khi tuyên bố đạt NFR-USAB-01.

**Đề xuất:**
- Test trên viewport 375px (iPhone SE), 390px (iPhone 14), 768px (iPad).
- Sidebar dashboard collapse thành hamburger menu trên mobile.
- Bảng dữ liệu sử dụng horizontal scroll hoặc card layout trên mobile.

### 5.4. Thêm tính năng thông báo realtime (Ưu tiên: 🟢 Thấp)

Đề xuất nâng cao: Sử dụng WebSocket/SSE để push thông báo realtime:
- Bệnh nhân: *"Đến lượt bạn khám! Vui lòng vào phòng khám số 3."*
- Bác sĩ: *"Bệnh nhân STT-05 đã check-in."*
- Lễ tân: *"Ca khám #MED-260901-A1B2 đã hoàn tất, hóa đơn chờ thu."*

---

## Nhóm 6: Cơ sở dữ liệu và Nghiệp vụ

### 6.1. Thiếu Price Snapshot cho đơn thuốc (Ưu tiên: 🟡 Trung bình)

**Vấn đề:** Theo thiết kế 3NF, bảng `prescription_items` không lưu giá thuốc (`unit_price`). Nếu giá thuốc trong `medicines` thay đổi theo thời gian, tính lại hóa đơn cũ sẽ bị sai.

**Đề xuất:** Thêm cột `unit_price_at_time DECIMAL(12,2)` vào `prescription_items` để snapshot giá tại thời điểm kê đơn. Điều này vẫn đạt 3NF vì là giá trị lịch sử, không phải dư thừa.

### 6.2. Mô hình 1 User — 1 Role hạn chế (Ưu tiên: 🟢 Thấp)

**Vấn đề:** `role_id` FK trực tiếp trong bảng `users` chỉ cho phép 1 vai trò / tài khoản.

**Đề xuất (tùy quy mô):**
- **Giữ nguyên** nếu đồ án không yêu cầu kiêm nhiệm (phù hợp cho đồ án môn học).
- Hoặc chuyển sang bảng trung gian `user_roles(user_id, role_id)` nếu muốn linh hoạt (1 tài khoản vừa là Doctor vừa là Admin).

### 6.3. Thiếu trạng thái `NO_SHOW` cho lịch hẹn (Ưu tiên: 🟢 Thấp)

**Vấn đề:** Data Dictionary mục 3.7 liệt kê trạng thái bao gồm `NO_SHOW`, nhưng SRS và RTM không đề cập quy trình xử lý khi bệnh nhân không đến khám.

**Đề xuất:** Bổ sung FR hoặc ghi chú rằng hệ thống tự động chuyển `CONFIRMED` → `NO_SHOW` nếu quá giờ hẹn mà chưa check-in (batch job hàng ngày).

### 6.4. Thiếu Audit Trail / Lịch sử thay đổi (Ưu tiên: 🟢 Thấp)

**Đề xuất:** Sử dụng Hibernate Envers hoặc bảng `audit_logs` để ghi lại:
- Ai đã thay đổi trạng thái lịch hẹn (CONFIRMED → CANCELLED → ...)
- Lịch sử điều chỉnh giá thuốc
- Lịch sử khóa/mở tài khoản

---

## Nhóm 7: DevOps, CI/CD và Triển khai

### 7.1. Bổ sung Dockerfile (Ưu tiên: 🟡 Trung bình)

```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=mysql"]
```

### 7.2. Bổ sung `docker-compose.yml` (Ưu tiên: 🟡 Trung bình)

```yaml
services:
  app:
    build: .
    ports: ["8080:8080"]
    depends_on: [db]
    environment:
      SPRING_PROFILES_ACTIVE: mysql
  db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: medclinic
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
```

### 7.3. Bổ sung tài liệu triển khai `DEPLOYMENT.md` (Ưu tiên: 🟢 Thấp)

Hướng dẫn:
- Cách chạy local với H2 (`mvn spring-boot:run`)
- Cách chạy với MySQL qua Docker Compose
- Cách seed data demo (20 bác sĩ, 6 chuyên khoa, 50 loại thuốc)
- Tài khoản demo cho từng vai trò

### 7.4. GitHub Actions CI Pipeline (Ưu tiên: 🟢 Thấp)

```yaml
# .github/workflows/ci.yml
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17' }
      - run: mvn verify
```

---

## Ma trận ưu tiên tổng hợp

| # | Hạng mục cải thiện | Ưu tiên | Nỗ lực | Tác động |
|:---:|:---|:---:|:---:|:---:|
| 1 | Triển khai các Controller + Service còn thiếu | 🔴 Cao | Lớn | Rất cao |
| 2 | Tạo ~25 template Thymeleaf cho 4 vai trò | 🔴 Cao | Lớn | Rất cao |
| 3 | Bổ sung Unit Test cho nghiệp vụ cốt lõi | 🔴 Cao | Vừa | Cao |
| 4 | Input Validation trên mọi DTO | 🔴 Cao | Nhỏ | Cao |
| 5 | Sửa RTM thiếu `FR-ADMIN-02` | 🟡 TB | Rất nhỏ | Trung bình |
| 6 | Xóa/bổ sung WebSocket trong SRS | 🟡 TB | Rất nhỏ | Trung bình |
| 7 | Chuẩn hóa Notifications xuyên suốt tài liệu | 🟡 TB | Nhỏ | Trung bình |
| 8 | Thêm `unit_price_at_time` vào `prescription_items` | 🟡 TB | Nhỏ | Trung bình |
| 9 | Tạo bảng Data Dictionary cho `prescriptions` + `prescription_items` | 🟡 TB | Nhỏ | Trung bình |
| 10 | Bổ sung Dockerfile + docker-compose.yml | 🟡 TB | Nhỏ | Trung bình |
| 11 | Integration Test chống Double Booking | 🟡 TB | Vừa | Cao |
| 12 | Đặc tả tích hợp VNPay | 🟢 Thấp | Vừa | Trung bình |
| 13 | Actor `GUEST` trong Use Case Diagram | 🟢 Thấp | Rất nhỏ | Thấp |
| 14 | Rate Limiting cho API | 🟢 Thấp | Nhỏ | Trung bình |
| 15 | Realtime notification (WebSocket/SSE) | 🟢 Thấp | Lớn | Cao (nếu demo) |

---

> [!TIP]
> **Chiến lược triển khai khuyến nghị:** Hoàn thành nhóm 🔴 trước (Controller, Service, Template, Test, Validation) — đây là phần "xương sống" để có một ứng dụng chạy được end-to-end. Sau đó sửa tài liệu 🟡 để đảm bảo tính nhất quán khi nộp đồ án. Cuối cùng bổ sung 🟢 nếu còn thời gian.
