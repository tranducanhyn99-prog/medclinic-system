# MedClinic source review and consolidated tasklist

Ngày chốt: **18/09/2026 (Asia/Saigon)**<br>
Branch: `codex/ui-design-documentation`; HEAD gốc: `f778779`<br>
Phạm vi: working tree chưa commit tại thời điểm đối chiếu. Đây là **nguồn tracking duy nhất**; `CHECKLISTNEW.md` không còn tồn tại và không được dùng song song.

## 1. Kết luận và bằng chứng

Các lỗi blocker của lượt review trước đã được sửa trong source hiện tại. Lệnh sau chạy xanh với H2 profile `dev`:

```text
mvn -o -B "-Dmaven.repo.local=C:/Users/DUCANHZZ/.m2/repository" test
```

Surefire hiện ghi **63 test case: 56 executed pass, 0 failure, 0 error và 7 environment-gated case được skip theo environment guard** (19/09/2026). `mvn verify` và `git diff --check` đều PASS.

Quy ước: `[x]` có source và bằng chứng kiểm thử phù hợp trong môi trường hiện có; `[~]` có implementation hữu ích nhưng thiếu test/QA hoặc giới hạn triển khai; `[ ]` chưa đáp ứng. `[x]` nghĩa là đã đáp ứng phạm vi hiện tại với bằng chứng phù hợp. SQL Server là database mục tiêu; MySQL là roadmap tương lai và không còn là điều kiện đóng task hiện tại. Browser/production QA vẫn được ghi riêng khi cần.

| Nhóm | [x] | [~] | [ ] |
|---|---:|---:|---:|
| T01–T22 | 22 | 0 | 0 |
| D01–D04 | 4 | 0 | 0 |
| R01–R14 | 14 đã xử lý trong source | 0 | 0 |

## 2. Task implementation

| Task | Pri | Trạng thái | Bằng chứng và phần còn lại |
|---|---:|---|---|
| T01 UI sửa chuyên khoa | P2 | [x] | `AdminController`, `SpecialtyServiceImpl`, `admin/specialties.html` có DTO, modal sửa và re-render khi validation lỗi. `AdminControllerFormRetentionTest.invalidSpecialtyRendersSameFormObjectAndReferenceData` kiểm tra controller giữ form và nạp lại dữ liệu tham chiếu. |
| T02 Quản lý bác sĩ hoàn chỉnh | P1 | [x] | `AdminDoctorCreateDto`, `DoctorService.registerDoctor/updateDoctor`, controller và `admin/doctors.html` tạo tài khoản BCrypt, license, cập nhật hồ sơ; `DoctorServiceTest` chạy xanh. |
| T03 UI sửa thuốc/tồn kho | P1 | [x] | DTO, hidden id/modal và `MedicineService.update` đã có validation/role guard. `AdminControllerFormRetentionTest` kiểm tra đúng nhánh update theo id và re-render khi form thuốc lỗi. |
| T04 Sửa link PDF bệnh sử | P1 | [x] | `patient/medical-history.html` truyền `rec.appointment.id`; `PdfExportControllerTest` chạy xanh. |
| T05 Khắc phục lazy loading | P1 | [x] | `MedicalRecordRepository`/`InvoiceRepository` dùng entity graph và service read-only transaction. `LazyLoadingEntityGraphIntegrationTest` truy cập các association record/invoice sau repository call, gồm patient, doctor user, prescription item và medicine. |
| T06 Queue numeric | P1 | [x] | `DailyQueueCounter`, `queueSequence`, unique `(appointment_date, queue_sequence)` và query sort theo sequence đã có. `DailyQueueCounterAllocator` dùng `REQUIRES_NEW` và retry unique collision; `DailyQueueCounterAllocatorTest` cùng test check-in chạy xanh. |
| T07 POST bắt đầu khám và concurrency | P1 | [x] | `POST /doctor/start-examination/{id}` chuyển `CHECKED_IN` sang `IN_PROGRESS` bằng optimistic lock; `StartExaminationIntegrationTest` kiểm tra CSRF và transition. |
| T08 Giữ dữ liệu form lỗi | P2 | [x] | Các form admin dùng DTO/`BindingResult` để giữ input. `AdminControllerFormRetentionTest` và `WorkflowFormErrorRoundTripTest` kiểm tra giữ form/reference data cho specialty, medicine và luồng booking/khám khi validation lỗi. |
| T09 Slot quá giờ và reason validation | P2 | [x] | `AppointmentServiceImpl` kiểm tra slot đã qua giờ, reason blank/length và dữ liệu appointment; `AppointmentServiceTest` chạy xanh. |
| T10 Retry collision mã | P2 | [x] | Booking code retry và `InvoiceCodeReservationCoordinator` chống trùng mã invoice có test unit/integration. `SqlServerInvoiceCodeReservationIntegrationTest` đã chạy trên SQL Server thật và PASS với unique violation 2627 được retry đúng. Đây là database mục tiêu của phiên bản hiện tại; MySQL được chuyển sang roadmap tương lai. |
| T11 Toàn vẹn kho | P1 | [x] | `Medicine.@Version`, pessimistic repository lock, aggregate quantity, active check và `PrescriptionItem.unitPriceAtTime` đã có. H2 concurrency test PASS. Trên SQL Server thật, `SqlServerMedicineStockConcurrencyIntegrationTest` PASS với stock=1 / hai transaction cạnh tranh, không oversell; `SqlServerMedicalRecordRollbackIntegrationTest` cũng PASS và xác nhận invoice failure rollback medical record, appointment và medicine stock. |
| T12 Validation tài khoản | P2 | [x] | Profile/password được validate, mật khẩu đổi có min length và ownership guard; `UserServiceTest` chạy xanh. |
| T13 Walk-in UC_013 | P2 | [x] | Reception nhận DOB/gender/address, tìm bệnh nhân theo dữ liệu liên hệ và dùng BCrypt; `WalkInSecurityIntegrationTest` xác minh walk-in mới bị disable và không dùng mật khẩu mặc định. |
| T14 Nghiệm thu PDF | P2 | [x] | `PdfExportServiceImpl` nạp font Unicode đóng gói, export kiểm soát ownership; `PdfUnicodeFontTest` và `PdfExportControllerTest` chạy xanh. Chưa có visual PDF QA với tài liệu dài. |
| T15 Concurrent booking test | P1 | [x] | `ConcurrentBookingIntegrationTest` kiểm tra cùng slot chỉ một success và hai slot khác nhau đều success. Cùng kịch bản đã chạy qua `SqlServerConcurrentBookingIntegrationTest` trên SQL Server thật và PASS. SQL Server là database mục tiêu; MySQL là roadmap tương lai. |
| T16 Email integration | P2 | [x] | Event after-commit nối booking/cancel với email; `AppointmentEmailIntegrationTest` xác minh hai thông báo. SMTP production chưa được gửi thật. |
| T17 Performance baseline | P2 | [x] | Baseline SQL Server cùng môi trường đã đủ PERF-01..04 và đều dưới ngưỡng 1,5 giây: `/` p95 68.986 ms, booking 76.550 ms, reception 52.658 ms, doctor desk 45.062 ms. Đây là local application baseline trên database target; production/CDN vẫn là acceptance riêng. |
| T18 Service authorization | P2 | [x] | `@EnableMethodSecurity` và guard trên doctor/medicine/specialty/user/medical-record service đã có. `ServiceAuthorizationIntegrationTest` xác nhận PATIENT bị từ chối ở năm service entry point nhạy cảm; các test nghiệp vụ hiện có chứng minh các luồng hợp lệ. |
| T19 Isolation | P2 | [x] | Booking, khám, payment, mã invoice và queue counter dùng transaction/lock/retry; rollback invoice được kiểm tra bằng integration test. Chưa kiểm chứng dialect/locking trên DB production. |
| T20 Responsive portal | P2 | [x] | Edge CDP QA ở CSS viewport 390×844 và 768×1024 đã PASS cho public pages và 4 portal đại diện: Patient Booking, Staff Reception, Doctor Desk, Admin Dashboard. Tất cả `overflowX=false`; mobile sidebar off-canvas không tạo horizontal scroll. `AuthenticatedPortalRenderIntegrationTest` tạo snapshot từ Thymeleaf output của từng role để browser QA không phụ thuộc credential/cookie automation. |
| T21 UI states/accessibility | P2 | [x] | Public và các portal đại diện đã qua Edge CDP responsive/AX audit; keyboard focus của mobile sidebar được quản lý khi mở/đóng; booking dynamic state có live region, loading/empty/selection semantics và `aria-pressed`; Reception/Doctor bỏ inline `onclick`; QR/ticket/payment/admin CRUD modal có accessible name; form control trong modal được liên kết label. Render audit trên 8 snapshot (`patient-booking`, `staff-reception`, `doctor-desk`, `admin-dashboard`, `admin-doctors`, `admin-specialties`, `admin-medicines`, `staff-billing`) cho **TOTAL_ISSUES=0** với duplicate ID, label, modal name, button/link name và image-alt checks. `LayoutTemplateContractTest` khóa các regression. Manual screen-reader vẫn là pre-release acceptance riêng, không còn là blocker implementation T21. |
| T22 Đồng bộ visual design | P3 | [x] | Đã review screenshot Edge CDP của public pages và 4 portal đại diện ở 390/768. Typography, accent, card/KPI, sidebar, form và chart nhất quán; mobile/desktop bố cục không bị cắt. Dashboard copy cũng được sửa từ “Bác Sĩ Trực Tuyến”/“Cập nhật thời gian thực” sang “Tổng Bác Sĩ”/“Dữ liệu tại thời điểm tải trang” để phản ánh đúng source. |

## 3. Task tài liệu

| Task | Pri | Trạng thái | Bằng chứng và phần còn lại |
|---|---:|---|---|
| D01 Chốt mâu thuẫn phạm vi | P2 | [x] | `docs/08_SCOPE_CONFLICT_DECISIONS.md` xác định source/test là bằng chứng, VNPay/WebSocket/audit trail là backlog. |
| D02 RTM tới test thực | P2 | [x] | `docs/04_RTM_MATRIX.md` phân biệt TC kế hoạch với test đã chạy. Evidence hiện tại: Surefire 63 test case, 56 executed pass, 7 environment-gated skipped, 34 test classes; mapping cần được duy trì khi test thay đổi. |
| D03 Schema và generated artifacts | P3 | [x] | `03_DATABASE_DESIGN_3NF.md`, SDD, migration và generator đã cập nhật. Năm DOCX tại `docs/generated/` qua OpenXML package/relationship check, được Microsoft Word COM mở read-only với `OpenAndRepair=false`, rồi `ExportAsFixedFormat` thành PDF tại `docs/generated/rendered/`: Use Case 20 trang/316,156 B, Khảo sát 5 trang/162,416 B, Mô hình hóa 9 trang/343,545 B, Thiết kế CSDL 14 trang/531,972 B, Xác định yêu cầu 6 trang/183,460 B. `pypdf.PdfReader` trích được lần lượt 25,829; 6,795; 12,134; 15,060; 9,212 ký tự; `pdfinfo` khớp số trang; `pdftoppm` đã tạo năm preview trang đầu trong `docs/generated/rendered/previews/`. |
| D04 Cập nhật audit | P2 | [x] | File này là audit source chốt, và `DE_XUAT_CAI_THIEN_MEDCLINIC.md` trỏ về nó thay vì khẳng định trạng thái implementation cũ. |

## 4. Regression của lượt review trước

| Regression | Trạng thái | Bằng chứng source/test |
|---|---|---|
| R01 Compile fail | Đã xử lý | `MedicalRecordService`/implementation khớp chữ ký; full Maven suite xanh. |
| R02 Đảo `appointmentId`/`doctorUserId` | Đã xử lý | `DoctorDeskController` gọi đúng thứ tự; test start examination pass. |
| R03 Khám và invoice không nguyên tử | Đã xử lý | `MedicalRecordRollbackIntegrationTest` chứng minh invoice failure rollback medical record, appointment và medicine stock. |
| R04 Walk-in plaintext password | Đã xử lý | Walk-in dùng BCrypt/random secret, bị disable; `WalkInSecurityIntegrationTest` chạy xanh. Seed chỉ chạy profile `dev`. |
| R05 Cancel không đặt lại slot được | Đã xử lý | Cancellation giải phóng/replacement slot theo mô hình mới; cần DB thật để kiểm tra migration lịch sử. |
| R06 Queue race/sort chuỗi | Đã xử lý | Numeric sequence/counter/constraint đã thêm; allocator `REQUIRES_NEW` retry unique collision và có unit test. |
| R07 Payment không guard/lock | Đã xử lý | Invoice repository lock và kiểm tra trạng thái; UI/service chỉ nhận CASH, VNPay chưa triển khai bị từ chối. |
| R08 Kê đơn không lock/giá lịch sử | Đã xử lý | Medicine version/pessimistic lock, aggregate stock và `unitPriceAtTime`; H2 concurrency test và full Spring/JDBC SQL Server stock-concurrency đều PASS, không oversell. |
| R09 JavaScript layout sai cấu trúc | Đã xử lý | Script đặt đúng thẻ; public/authenticated render tests PASS; Doctor/Reception inline click handlers đã được chuyển sang event listeners; responsive Edge CDP QA 390/768 PASS. |
| R10 Secret/CSRF mặc định yếu | Đã xử lý | `REMEMBER_ME_KEY` cấu hình qua environment; CSRF chỉ bỏ qua H2/ws; production provisioning được tài liệu hóa. |
| R11 Email booking/cancel không wired | Đã xử lý | After-commit event và integration test. |
| R12 Reception search không theo ngày | Đã xử lý | `searchForReception(keyword, LocalDate.now())` tại controller/service. |
| R13 PDF không Unicode | Đã xử lý | Bundled Unicode font và test `/FontFile2`. |
| R14 Test không chứng minh nghiệp vụ | Đã xử lý | Concurrent test chỉ chấp nhận `BusinessLogicException`; examination test kiểm tra endpoint, CSRF, state. |

## 5. Điều kiện và hạn chế trước nghiệm thu

- **SQL Server là database mục tiêu của phiên bản hiện tại.** Clean baseline 16 bảng apply PASS trên SQL Server 17.0.1000.7; Hibernate `ddl-auto=validate`, Unicode NVARCHAR, T10 invoice retry, T11 stock concurrency/rollback, T15 concurrent booking và local performance baseline đều đã verify PASS. MySQL thuộc roadmap tương lai.
- **Performance/UI:** T17 baseline trên SQL Server PASS PERF-01..04 với p95 < 100 ms trong local QA. Public + portal đại diện đã QA responsive/AX; render accessibility audit 8 snapshot hiện `TOTAL_ISSUES=0`. Manual screen-reader trên môi trường release vẫn là acceptance trước production, không phải task implementation còn mở.
- **Admin production:** reference-data roles và admin provisioning template đã được runtime-QA trên SQL Server disposable; duplicate guard PASS. Actual production admin identity/password rotation vẫn là bước vận hành khi deploy thật.
- **DOCX/diagram:** generator output vào `docs/generated/`; năm DOCX, bốn diagram và năm PDF/preview đã được nghiệm thu cấu trúc và render. Những artifact này vẫn cần được cập nhật nếu schema hoặc tài liệu nguồn thay đổi.

## 6. Phần còn lại trước production

Checklist implementation/documentation/regression hiện đã đóng hoàn toàn. Local SQL Server release smoke, reference roles và admin-provisioning template cũng đã QA PASS. Phần còn lại cần môi trường hoặc danh tính production thật:

1. Manual screen-reader smoke trên môi trường release đại diện.
2. Cấu hình SMTP thật và xác nhận một email outbound trên staging/production.
3. Wiring/rotation bằng secret manager của hạ tầng đích, thay cho QA environment variables.
4. Provision danh tính admin production thật, đăng nhập xác minh và rotate bootstrap password trước bàn giao.
5. DBA backup/change approval và deployment-window smoke trên production.
6. MySQL, VNPay/IPN, audit trail, refund/reschedule/WebSocket chỉ triển khai khi mở scope tương lai.

## 7. Direct continuation 19/09/2026

- Không dùng Codex; verification được thực hiện trực tiếp trên `DESKTOP-KGQUP06`.
- SQL Server local 17.0.1000.7 là database target; MySQL chuyển sang future roadmap.
- Baseline `SQLSERVER_BASELINE_SCHEMA_2026-09-19.sql` apply PASS trên DB QA sạch: 16 tables, filtered unique queue index, `notifications`, `password_reset_tokens`.
- SQL Server profile dùng `ddl-auto=validate` và nationalized character data; NVARCHAR Unicode equality smoke PASS.
- Hibernate schema validation qua JDBC Integrated Authentication PASS.
- T10 invoice-code retry, T11 stock-concurrency/rollback và T15 concurrent booking đều có SQL Server evidence PASS.
- T17 SQL Server local baseline PASS PERF-01..04; p95 lần lượt 68.986 / 76.550 / 52.658 / 45.062 ms.
- SQL Server reference-data script đã apply lặp PASS: đúng 4 mandatory roles, không duplicate.
- Admin provisioning template runtime-QA PASS trên DB disposable: insert active `ROLE_ADMIN` thành công; duplicate username bị chặn bằng error 51001.
- Packaged JAR profile `sqlserver` release smoke PASS trên port 18085: `/`, `/login`, `/register` HTTP 200; `/admin/dashboard` chưa đăng nhập redirect 302 về login; không chạy dev seed.
- Full default `mvn verify`: 63 tests, 56 executed pass, 7 environment-gated skipped, 0 failure/error; 34 test classes; `git diff --check` PASS.
