# MA TRẬN TRUY VẾT YÊU CẦU PHẦN MỀM (REQUIREMENTS TRACEABILITY MATRIX - RTM)
**Dự án:** MedClinic - Phòng Khám Tư Nhân  
**Mục đích:** Đảm bảo 100% yêu cầu chức năng (FR) trong SRS được ánh xạ tới Sơ đồ Use Case, Bảng CSDL, Lớp Service/Controller và Kế hoạch kiểm thử.  

**Mốc lịch sử kiểm chứng (23/09/2026):** Cột `Test Case ID` là định danh kế hoạch, không tự khẳng định test tự động đã tồn tại. Full `mvn verify` có **139 test case trên 59 test class: 131 PASS, 8 SQL Server environment-gated skipped** ở cấu hình mặc định; bật `RUN_SQLSERVER_IT=true` trên DB local riêng có **139/139 PASS, 0 skipped/failure/error**. JaCoCo đạt **82,12% line / 64,62% branch**, vượt quality gate 70% line / 50% branch. Coverage gồm service/controller/security, WebSocket/STOMP hàng chờ `AFTER_COMMIT`, rollback suppression, SockJS endpoint security, public/authenticated render, concurrent booking/stock, email after-commit, PDF Unicode, SQL Server integration, acceptance E2E (`SqlServerReleaseAcceptanceEndToEndIntegrationTest`), rate limiting/audit/Actuator (`SecurityHardeningIntegrationTest`) và accessibility contracts. Biên bản runtime: [15_P0_RUNTIME_ACCEPTANCE_2026-09-23.md](15_P0_RUNTIME_ACCEPTANCE_2026-09-23.md).

**Cập nhật 03/10/2026:** Cột kiểm thử bên dưới dùng tên class/method có trong repo, thay các mã kế hoạch `TC-*`. Service unit test, portal render và E2E có phạm vi khác nhau; một dòng được ánh xạ không tự chứng minh mọi nhánh nghiệp vụ đã được nghiệm thu qua browser. Kết quả bản cuối xem [nghiệm thu đồ án](ACADEMIC_ACCEPTANCE_2026-10-03.md).

---

| Mã Yêu Cầu (SRS) | Mô Tả Yêu Cầu Chức Năng | Use Case | Bảng CSDL Ánh Xạ | Lớp Xử Lý Chính | Test thực tế |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **FR-AUTH-01** | Đăng ký bệnh nhân ở trạng thái chờ xác thực email | UC1 | `users`, `roles`, `auth_verification_challenges` | `UserService.registerPatient()`, `AuthVerificationService`, `AuthController` | `OtpAuthenticationIntegrationTest` |
| **FR-AUTH-02** | Mật khẩu + OTP trước khi tạo session; seed account bypass OTP khi dùng mật khẩu; Google luôn OTP | UC1 | `users`, `roles`, `auth_verification_challenges` | `AuthenticationManager`, `AuthVerificationService`, `AuthController`, `SecurityConfig` | `OtpAuthenticationIntegrationTest`, `SecurityHardeningIntegrationTest` |
| **FR-AUTH-03** | Chỉnh Profile, giới tính/ngày sinh, avatar và đổi mật khẩu | UC1 | `users` | `UserService`, `AvatarStorageService`, `AuthController` | `ProfileManagementIntegrationTest`, `AvatarStorageServiceImplTest`, `UserServiceExtendedTest` |
| **FR-AUTH-04** | Quản trị viên Khóa / Mở tài khoản | UC16 | `users` | `UserService.toggleUserStatus()`, `AdminController` | `UserServiceExtendedTest.togglesUserStatus` |
| **FR-AUTH-05** | Khôi phục mật khẩu an toàn | UC1 | `users`, `password_reset_tokens` | `PasswordResetService`, `AuthController` | `PasswordResetServiceTest` |
| **FR-AUTH-07** | Google OIDC cho bệnh nhân; bắt buộc OTP trước session, định danh sub và email đã xác thực | UC1 | `users.google_subject`, `auth_verification_challenges` | `GoogleLoginConfig`, `GoogleAccountService`, `AuthController` | `GoogleLoginIntegrationTest`, `GoogleCallbackProtocolIntegrationTest`, `GoogleLoginConfigurationTest` |
| **FR-AUTH-06** | OTP 6 số + magic link 5 phút; resend gắn với session, giới hạn theo tài khoản và vô hiệu mã cũ | UC1 | `auth_verification_challenges`, `users` | `AuthVerificationService`, `EmailServiceImpl`, `AuthController` | `OtpAuthenticationIntegrationTest` |
| **FR-SPEC-01** | Quản lý Chuyên khoa (CRUD) | UC13 | `specialties` | `SpecialtyService`, `AdminController` | `SpecialtyServiceTest` |
| **FR-DOC-01** | Quản lý Hồ sơ Bác sĩ | UC14 | `doctor_profiles` | `DoctorService`, `AdminController` | `DoctorServiceTest, DoctorServiceExtendedTest` |
| **FR-DOC-02** | Tìm kiếm & Lọc Bác sĩ | UC2 | `doctor_profiles`, `specialties` | `DoctorService.filterDoctors()`, `PublicDoctorController` | `PublicDoctorControllerTest, DoctorServiceExtendedTest.filtersBySpecialtyFeeAndKeyword` |
| **FR-DOC-03** | Xem lịch trực cá nhân | UC14 | `doctor_schedules`, `time_slots` | `ScheduleService.findByDoctorAndDateRange()`, `DoctorDeskController` | `ScheduleServiceTest, DeepUiStateIntegrationTest.scheduleUsesVietnameseDaysAndTextualSlotLegend` |
| **FR-BOOK-01** | Xem danh sách Slot trống (AJAX) | UC3 | `time_slots` | `ApiController.getDoctorSlots()`, `ScheduleService.getAvailableSlots()` | `ApiControllerTest.mapsAvailableSlotsToPublicDto` |
| **FR-BOOK-02** | Đặt lịch khám trực tuyến | UC3 | `appointments`, `time_slots` | `AppointmentService.bookAppointment()` | `AppointmentServiceTest.testBookAppointmentSuccess, SqlServerReleaseAcceptanceEndToEndIntegrationTest` |
| **FR-BOOK-03** | Chống Double Booking | UC3 | `time_slots.version` | `TimeSlotRepository.findByIdForUpdate()`, `@Version`, `AppointmentService` | `ConcurrentBookingIntegrationTest`, `SqlServerConcurrentBookingIntegrationTest` |
| **FR-BOOK-04** | Sinh mã QR & Mã đặt lịch | UC3 | `appointments.booking_code` | `QrCodeUtil.generateQrCodeBase64()` | `QrCodeUtilTest, EmailServiceImplTest.sendsBookingConfirmationEmail` |
| **FR-BOOK-05** | Gửi Email xác nhận bất đồng bộ sau commit | UC3 | - | `AppointmentEmailListener`, `EmailService` | `AppointmentEmailIntegrationTest` |
| **FR-BOOK-06** | Bệnh nhân Hủy lịch hẹn | UC4 | `appointments`, `time_slots` | `AppointmentService.cancelAppointment()` | `AppointmentServiceTest.testCancelAppointmentSuccess, AppointmentEmailIntegrationTest` |
| **FR-REC-01** | Tra cứu tiếp nhận lịch hẹn | UC6 | `appointments` | `StaffReceptionController.receptionDesk()` | `AuthenticatedPortalRenderIntegrationTest, BackendAcceptanceIntegrationTest` |
| **FR-REC-02** | Check-in & Cấp số thứ tự (STT), hỗ trợ ngày tương lai trong demo | UC7 | `appointments.queue_number`, `appointments.queue_sequence`, `daily_queue_counters` | `AppointmentService.checkInAppointment()`, `ClinicalDatePolicy`, `DailyQueueCounterAllocator` | `AppointmentServiceTest.testCheckInAppointmentSuccess, DailyQueueCounterAllocatorTest, ClinicalDateBoundaryTest, ClinicalDateBoundaryIntegrationTest, ClinicalDemoPolicyTest, ClinicalDemoWorkflowIntegrationTest` |
| **FR-REC-03** | Đăng ký Bệnh nhân vãng lai | UC8 | `users`, `appointments` | `AppointmentService.bookWalkInAppointment()` | `BackendAcceptanceIntegrationTest, WalkInSecurityIntegrationTest, ClinicalDateBoundaryTest, ClinicalDateBoundaryIntegrationTest` |
| **FR-EXAM-01** | Bác sĩ xem hàng chờ khám, gồm ngày tương lai khi bật demo | UC10 | `appointments` | `DoctorDeskController.doctorDesk()`, `AppointmentService` | `AppointmentServiceTest.testFindTodayQueueByDoctor, AuthenticatedPortalRenderIntegrationTest, ClinicalDemoWorkflowIntegrationTest` |
| **FR-EXAM-02** | Chẩn đoán & Ghi bệnh án điện tử | UC11 | `medical_records` | `MedicalRecordService.saveMedicalRecordAndPrescription()`| `StartExaminationIntegrationTest, ClinicalDateBoundaryTest, ClinicalDateBoundaryIntegrationTest, SqlServerReleaseAcceptanceEndToEndIntegrationTest, MedicalRecordRollbackIntegrationTest, LazyLoadingEntityGraphIntegrationTest, SqlServerLazyLoadingEntityGraphIntegrationTest` |
| **FR-EXAM-03** | Kê đơn thuốc & Trừ tồn kho | UC12 | `prescriptions`, `medicines` | `MedicalRecordService`, `MedicineRepository` | `MedicineStockConcurrencyIntegrationTest, SqlServerReleaseAcceptanceEndToEndIntegrationTest` |
| **FR-EXAM-04** | Xuất Đơn thuốc ra file PDF | UC5 | `medical_records`, `prescriptions` | `PdfExportService.exportPrescriptionPdf()` | `PdfExportControllerTest.passesRequesterToPrescriptionService, PrescriptionPdfPresentationTest, SqlServerReleaseAcceptanceEndToEndIntegrationTest` |
| **FR-EXAM-06** | Bác sĩ xem lại bệnh án hoàn tất do mình phụ trách; phân trang 25, chỉ đọc | UC18 | `medical_records`, `appointments`, `prescriptions`, `prescription_items` | `DoctorDeskController.completedRecords()`, `MedicalRecordService.getCompletedRecordForDoctor()` | `DoctorCompletedRecordIntegrationTest` |
| **FR-EXAM-05** | Hàng chờ bác sĩ realtime sau commit | UC10 | `appointments` | `DoctorQueueChangedEvent`, `DoctorQueueWebSocketListener`, `WebSocketConfig`, Doctor Desk STOMP client | `DoctorQueueWebSocketListenerTest`, `DoctorQueueWebSocketAfterCommitIntegrationTest`, `MedicalRecordQueueEventTest` |
| **FR-BILL-01** | Tự động sinh Hóa đơn viện phí | UC9 | `invoices` | `InvoiceService.createInvoiceForAppointment()` | `InvoiceServiceTest, InvoiceCodeReservationIntegrationTest` |
| **FR-BILL-02** | Thu tiền mặt hoặc tạo VietQR chuyển khoản theo đúng số tiền/nội dung hóa đơn | UC9 | `invoices`, `bank_transfer_payments` | `InvoiceService`, `BankTransferPaymentService`, Staff billing controllers | `InvoiceServiceTest`, `BankTransferPaymentServiceTest` |
| **FR-BILL-03** | Xuất Hóa đơn ra file PDF | UC9 | `invoices` | `PdfExportService.exportInvoicePdf()` | `InvoicePdfPresentationTest, PdfExportControllerTest.allowsInvoiceDownloadForStaff` |
| **FR-BILL-04** | Callback HMAC/idempotency hoặc đối soát thủ công có mã giao dịch; chỉ khi khớp mới cập nhật `PAID` | UC9 | `bank_transfer_payments`, `business_audit_events`, `invoices` | `BankPaymentCallbackController`, `BankTransferPaymentService`, `BusinessAuditService` | `BankTransferPaymentServiceTest` |
| **FR-ADMIN-01**| Dashboard thống kê KPI & Doanh thu| UC17 | `invoices`, `appointments` | `DashboardService.getDashboardStatistics()` | `AuthenticatedPortalRenderIntegrationTest` |
| **FR-ADMIN-02**| Biểu đồ phân bổ ca khám & tỷ lệ trạng thái | UC17 | `appointments`, `invoices` | `DashboardService`, `AdminController` | `AuthenticatedPortalRenderIntegrationTest, LayoutTemplateContractTest` |
| **FR-ADMIN-03**| Quản lý danh mục thuốc, giá bán và ngưỡng nhập lại | UC15 | `medicines` | `MedicineService.updateCatalog()`, `AdminController` | `MedicineServiceTest`, `AuthenticatedPortalRenderIntegrationTest` |
| **FR-ADMIN-04**| Cảnh báo thuốc sắp hết/hết hàng theo `reorder_level` | UC15 | `medicines.reorder_level`, `medicines.stock_quantity` | `MedicineRepository.countLowStock()`, `AdminController` | `MedicineServiceTest`, portal render contract |
| **FR-ADMIN-05**| Nhập bổ sung và audit mọi biến động kho | UC15, UC12 | `inventory_transactions`, `medicines` | `MedicineService.restock()`, `MedicalRecordService` | `MedicineServiceTest`, `MedicineStockConcurrencyIntegrationTest` |


## Truy vết NFR Security & Observability

| Mã NFR | Mục tiêu | Implementation chính | Kiểm thử / bằng chứng |
| :--- | :--- | :--- | :--- |
| **NFR-SEC-06** | Session/cookie và response hardening | `SecurityConfig`, `application.yml` | `SecurityHardeningIntegrationTest`, `SecurityObservabilityConfigurationTest` |
| **NFR-SEC-07** | Shared rate limiting + security audit không lưu IP/principal thô | `SensitiveEndpointRateLimitFilter`, `JdbcSqlServerRateLimitStore`, `SecurityAuditService` | `SensitiveEndpointRateLimitFilterTest`, `SecurityAuditServiceTest`, SQL runtime acceptance |
| **NFR-SEC-08** | Callback thanh toán ký HMAC, idempotent, exact amount/reference | `BankPaymentCallbackController`, `HmacSignatureVerifier`, `BankTransferPaymentService` | `BankTransferPaymentServiceTest` |
| **NFR-OPS-01/02** | Health/metrics, email retry, backup/restore và retention | Actuator, `EmailDeliveryMonitor`, `SecurityDataRetentionJob`, `ops/*.ps1` | `EmailServiceImplTest`, restore drill, runtime smoke `/actuator/health` |
