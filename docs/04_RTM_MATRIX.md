# MA TRẬN TRUY VẾT YÊU CẦU PHẦN MỀM (REQUIREMENTS TRACEABILITY MATRIX - RTM)
**Dự án:** MedClinic - Phòng Khám Tư Nhân  
**Mục đích:** Đảm bảo 100% yêu cầu chức năng (FR) trong SRS được ánh xạ tới Sơ đồ Use Case, Bảng CSDL, Lớp Service/Controller và Kế hoạch kiểm thử.  

---

| Mã Yêu Cầu (SRS) | Mô Tả Yêu Cầu Chức Năng | Use Case | Bảng CSDL Ánh Xạ | Lớp Xử Lý Chính | Test Case ID |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **FR-AUTH-01** | Đăng ký tài khoản Bệnh nhân | UC1 | `users`, `roles` | `UserService.registerPatient()`, `AuthController` | `TC-AUTH-01` |
| **FR-AUTH-02** | Đăng nhập & RBAC Security | UC1 | `users`, `roles` | `CustomUserDetailsService`, `SecurityConfig` | `TC-AUTH-02` |
| **FR-AUTH-03** | Quản lý Profile & Đổi mật khẩu | UC1 | `users` | `UserService.updateUserProfile()`, `AuthController` | `TC-AUTH-03` |
| **FR-AUTH-04** | Quản trị viên Khóa / Mở tài khoản | UC16 | `users` | `UserService.toggleUserStatus()`, `AdminController` | `TC-AUTH-04` |
| **FR-SPEC-01** | Quản lý Chuyên khoa (CRUD) | UC13 | `specialties` | `SpecialtyService`, `AdminController` | `TC-SPEC-01` |
| **FR-DOC-01** | Quản lý Hồ sơ Bác sĩ | UC14 | `doctor_profiles` | `DoctorService`, `AdminController` | `TC-DOC-01` |
| **FR-DOC-02** | Tìm kiếm & Lọc Bác sĩ | UC2 | `doctor_profiles`, `specialties` | `DoctorService.filterDoctors()`, `HomeController` | `TC-DOC-02` |
| **FR-DOC-03** | Tự động sinh Ca & Slot 30 phút | UC14 | `doctor_schedules`, `time_slots` | `ScheduleService.createOrGetSchedule()` | `TC-SCH-01` |
| **FR-BOOK-01** | Xem danh sách Slot trống (AJAX) | UC3 | `time_slots` | `ApiController.getAvailableSlots()` | `TC-BOOK-01` |
| **FR-BOOK-02** | Đặt lịch khám trực tuyến | UC3 | `appointments`, `time_slots` | `AppointmentService.bookAppointment()` | `TC-BOOK-02` |
| **FR-BOOK-03** | Chống Double Booking (Optimistic Lock) | UC3 | `time_slots.version` | `@Version`, `AppointmentService` | `TC-CONCURRENCY-01`|
| **FR-BOOK-04** | Sinh mã QR & Mã đặt lịch | UC3 | `appointments.booking_code` | `QrCodeUtil.generateQrCodeBase64()` | `TC-QR-01` |
| **FR-BOOK-05** | Gửi Email xác nhận bất đồng bộ | UC3 | - | `EmailService.sendBookingConfirmationEmail()` | `TC-MAIL-01` |
| **FR-BOOK-06** | Bệnh nhân Hủy lịch hẹn | UC4 | `appointments`, `time_slots` | `AppointmentService.cancelAppointment()` | `TC-BOOK-03` |
| **FR-REC-01** | Tra cứu tiếp nhận lịch hẹn | UC6 | `appointments` | `StaffReceptionController.receptionDesk()` | `TC-REC-01` |
| **FR-REC-02** | Check-in & Cấp số thứ tự (STT) | UC7 | `appointments.queue_number` | `AppointmentService.checkInAppointment()` | `TC-REC-02` |
| **FR-REC-03** | Đăng ký Bệnh nhân vãng lai | UC8 | `users`, `appointments` | `AppointmentService.bookWalkInAppointment()` | `TC-REC-03` |
| **FR-EXAM-01** | Bác sĩ xem hàng chờ khám | UC10 | `appointments` | `DoctorDeskController.doctorDesk()` | `TC-EXAM-01` |
| **FR-EXAM-02** | Chẩn đoán & Ghi bệnh án điện tử | UC11 | `medical_records` | `MedicalRecordService.saveMedicalRecordAndPrescription()`| `TC-EXAM-02` |
| **FR-EXAM-03** | Kê đơn thuốc & Trừ tồn kho | UC12 | `prescriptions`, `medicines` | `MedicalRecordService`, `MedicineRepository` | `TC-EXAM-03` |
| **FR-EXAM-04** | Xuất Đơn thuốc ra file PDF | UC5 | `medical_records`, `prescriptions` | `PdfExportService.exportPrescriptionPdf()` | `TC-PDF-01` |
| **FR-BILL-01** | Tự động sinh Hóa đơn viện phí | UC9 | `invoices` | `InvoiceService.createInvoiceForAppointment()` | `TC-BILL-01` |
| **FR-BILL-02** | Thu viện phí (Tiền mặt / VNPay) | UC9 | `invoices` | `InvoiceService.processPayment()` | `TC-BILL-02` |
| **FR-BILL-03** | Xuất Hóa đơn ra file PDF | UC9 | `invoices` | `PdfExportService.exportInvoicePdf()` | `TC-PDF-02` |
| **FR-ADMIN-01**| Dashboard thống kê KPI & Doanh thu| UC17 | `invoices`, `appointments` | `DashboardService.getDashboardStatistics()` | `TC-STAT-01` |
| **FR-ADMIN-03**| Quản lý Kho thuốc & Dược phẩm | UC15 | `medicines` | `MedicineService`, `AdminController` | `TC-MED-01` |
