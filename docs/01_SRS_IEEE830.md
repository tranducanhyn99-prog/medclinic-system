# ĐẶC TẢ YÊU CẦU PHẦN MỀM (SOFTWARE REQUIREMENTS SPECIFICATION - SRS)
**Tên dự án:** Hệ thống Quản lý và Đặt lịch khám Phòng khám Tư nhân (MedClinic)  
**Tiêu chuẩn áp dụng:** IEEE Std 830-1998  
**Học phần:** Lập trình Web & Công nghệ Phần mềm  
**Phiên bản:** 1.1 (Đồng bộ implementation - 19/09/2026)<br>

---

## 1. GIỚI THIỆU (INTRODUCTION)

### 1.1. Mục đích tài liệu (Purpose)
Tài liệu này xác định và đặc tả toàn bộ các yêu cầu chức năng (Functional Requirements - FR), yêu cầu phi chức năng (Non-Functional Requirements - NFR), cùng các ràng buộc thiết kế cho dự án **Hệ thống Quản lý và Đặt lịch khám Phòng khám Tư nhân (MedClinic)**. Tài liệu là căn cứ pháp lý và kỹ thuật cho việc thiết kế kiến trúc, xây dựng cơ sở dữ liệu, phát triển mã nguồn, kiểm thử và nghiệm thu đồ án.

### 1.2. Phạm vi sản phẩm (Scope)
MedClinic là giải pháp chuyển đổi số toàn diện cho phòng khám đa khoa tư nhân, giải quyết bài toán quản lý quy trình khám chữa bệnh từ khâu tiếp cận người bệnh đến quản lý chuyên môn và thu ngân:
- **Người bệnh (Patient):** Tra cứu chuyên khoa, tìm kiếm bác sĩ, đặt lịch khám theo khung giờ trực tuyến, nhận mã QR check-in qua email, theo dõi lịch sử khám và đơn thuốc điện tử.
- **Bác sĩ (Doctor):** Quản lý lịch khám cá nhân, tiếp nhận bệnh nhân theo hàng chờ số thứ tự, chẩn đoán bệnh án điện tử, kê đơn thuốc với kho dược phẩm tích hợp, tra cứu tiền sử bệnh nhân.
- **Lễ tân/Y tá (Staff):** Quét mã QR/tra cứu mã để check-in tiếp nhận, cấp số thứ tự khám (Queue number), tiếp nhận bệnh nhân vãng lai (walk-in), lập hóa đơn viện phí và thu ngân.
- **Quản trị viên (Admin):** Quản lý hồ sơ bác sĩ, danh mục chuyên khoa, danh mục thuốc, tài khoản người dùng, xem báo cáo doanh thu & biểu đồ thống kê theo dữ liệu giao dịch hiện có.

### 1.3. Định nghĩa, Từ viết tắt (Definitions & Acronyms)
| Thuật ngữ | Diễn giải |
| :--- | :--- |
| **SRS** | Software Requirements Specification (Đặc tả yêu cầu phần mềm) |
| **RBAC** | Role-Based Access Control (Kiểm soát truy cập dựa trên vai trò) |
| **TimeSlot (Slot)** | Khung giờ khám cố định của bác sĩ trong ca làm việc (ví dụ: 08:00 - 08:30) |
| **Double Booking** | Tình trạng 2 bệnh nhân đặt trùng cùng một khung giờ của cùng một bác sĩ |
| **RTM** | Requirements Traceability Matrix (Ma trận truy vết yêu cầu phần mềm) |
| **3NF** | Third Normal Form (Dạng chuẩn hóa dữ liệu cấp 3) |
| **Optimistic Lock** | Khóa lạc quan sử dụng phiên bản (`@Version`) kiểm soát truy cập đồng thời |

---

## 2. MÔ TẢ TỔNG QUAN (OVERALL DESCRIPTION)

### 2.1. Phối cảnh sản phẩm (Product Perspective)
Hệ thống được phát triển theo mô hình kiến trúc phân tầng chuẩn (Layered Architecture):
- **Tầng Trình diễn (Presentation Layer):** Thymeleaf Server-Side Rendering + Bootstrap 5.3 + AJAX REST cho tải dữ liệu động và Spring WebSocket/STOMP cho cập nhật hàng chờ bác sĩ theo thời gian thực.
- **Tầng Bảo mật & Nghiệp vụ (Security & Business Service Layer):** Spring Security 7 RBAC, Spring Data JPA Services, Concurrency Manager, Async Email Engine, OpenPDF & ZXing Utilities.
- **Tầng Dữ liệu (Data Layer):** Relational Database (H2 Database cho Development/Test / Microsoft SQL Server cho hệ thống hiện tại và Production) đạt chuẩn 3NF. MySQL được giữ ở roadmap tương lai, không thuộc tiêu chí nghiệm thu phiên bản hiện tại.

### 2.2. Danh mục tác nhân trong hệ thống (User Classes and Roles)
| Tác nhân (Actor) | Vai trò (Role) | Mô tả trách nhiệm & Quyền hạn |
| :--- | :--- | :--- |
| **Khách vãng lai** | `GUEST` | Xem thông tin phòng khám, tra cứu chuyên khoa, tìm kiếm bác sĩ, đăng ký tài khoản bệnh nhân, đăng nhập. |
| **Bệnh nhân** | `ROLE_PATIENT` | Đặt lịch khám theo slot, quản lý lịch hẹn, hủy lịch, xem bệnh án & đơn thuốc PDF. |
| **Bác sĩ** | `ROLE_DOCTOR` | Quản lý lịch trực, xem hàng chờ bệnh nhân, chẩn đoán, kê đơn thuốc, xem tiền sử bệnh án. |
| **Lễ tân / Tiếp nhận** | `ROLE_STAFF` | Quét QR check-in, cấp số thứ tự, đăng ký bệnh nhân vãng lai tại quầy, lập hóa đơn, thu viện phí. |
| **Quản trị viên** | `ROLE_ADMIN` | Toàn quyền quản trị danh mục, chuyên khoa, bác sĩ, thuốc, tài khoản và dashboard doanh thu. |

---

## 3. YÊU CẦU CHỨC NĂNG CHI TIẾT (FUNCTIONAL REQUIREMENTS)

### 3.1. Phân hệ Xác thực & Phân quyền (Authentication & Authorization)
- **FR-AUTH-01 (Đăng ký tài khoản):** Khách hàng đăng ký tài khoản bệnh nhân với Họ tên, Tên đăng nhập, Mật khẩu, Email, SĐT, Ngày sinh, Giới tính, Địa chỉ. Tài khoản mới ở trạng thái chưa kích hoạt cho đến khi xác thực email thành công.
- **FR-AUTH-02 (Đăng nhập & RBAC):** Hệ thống kiểm tra tên đăng nhập/email + mật khẩu bằng Spring Security 7 và BCrypt; tài khoản thường phải hoàn tất OTP email trước khi tạo session đăng nhập. Sau xác thực, hệ thống chuyển hướng đúng portal theo vai trò. Seed/test account được đánh dấu `otp_bypass` và không yêu cầu OTP.
- **FR-AUTH-03 (Quản lý Hồ sơ cá nhân):** Người dùng có thể cập nhật Họ tên, SĐT, Giới tính, Ngày sinh, Địa chỉ, thay đổi ảnh đại diện JPG/PNG/WebP tối đa 5 MB và đổi mật khẩu. Username/email là định danh tài khoản và không sửa trực tiếp tại form Profile.
- **FR-AUTH-04 (Quản lý Tài khoản người dùng):** Quản trị viên có thể xem danh sách tài khoản, tìm kiếm theo từ khóa và thực hiện khóa / mở khóa tài khoản người dùng.
- **FR-AUTH-05 (Khôi phục mật khẩu):** Người dùng yêu cầu đặt lại mật khẩu qua email. Hệ thống chỉ lưu SHA-256 hash của token, token hết hạn sau 24 giờ, chỉ dùng một lần và phản hồi theo cách không tiết lộ email có tồn tại hay không.
- **FR-AUTH-06 (OTP email & Magic Link):** Với đăng ký và đăng nhập tài khoản thường, hệ thống phát OTP 6 số và magic link qua email, cùng hết hạn sau 5 phút và chỉ dùng một lần. Chỉ lưu BCrypt hash OTP và SHA-256 hash magic token; tối đa 5 lần nhập sai. Người dùng có thể gửi lại OTP trong đúng phiên đang giữ challenge, giới hạn 3 challenge cho mỗi tài khoản/loại trong cửa sổ hiệu lực; challenge cũ bị vô hiệu hóa sau khi resend thành công. Bấm nút xác nhận trong email sẽ tự xác thực, tạo session và chuyển về portal với thông báo đăng nhập thành công.

### 3.2. Phân hệ Quản lý Chuyên khoa & Hồ sơ Bác sĩ (Doctor & Specialty Management)
- **FR-SPEC-01 (Quản lý Chuyên khoa):** Quản trị viên có thể thêm mới, sửa, bật/tắt trạng thái hoạt động của chuyên khoa (Tim mạch, Răng hàm mặt, Da liễu, Nhi khoa, Tai mũi họng, Cơ xương khớp).
- **FR-DOC-01 (Quản lý Bác sĩ):** Quản trị viên tạo và chỉnh sửa hồ sơ bác sĩ gồm tài khoản đăng nhập, số giấy phép hành nghề, học hàm/học vị, số năm kinh nghiệm, giá khám tư vấn, chuyên khoa phụ trách và tiểu sử công tác.
- **FR-DOC-02 (Tra cứu & Lọc Bác sĩ):** Khách/Bệnh nhân có thể tìm kiếm, lọc bác sĩ theo chuyên khoa, mức phí khám tối đa, hoặc từ khóa tên bác sĩ.
- **FR-DOC-03 (Xem Lịch trực cá nhân):** Bác sĩ xem lịch phân ca và TimeSlot đã lưu trong khoảng 14 ngày tới. Dữ liệu mẫu ở profile `dev` có thể được seed tự động; UI Admin quản trị lịch trực chưa thuộc phạm vi hiện hành.

### 3.3. Phân hệ Đặt lịch & Kiểm soát Đồng thời (Appointment Booking & Concurrency Control)
- **FR-BOOK-01 (Xem Slot trống):** Bệnh nhân chọn Bác sĩ và Ngày khám, hệ thống sử dụng AJAX để tải danh sách các khung giờ còn khả dụng (`AVAILABLE`).
- **FR-BOOK-02 (Đặt lịch khám):** Bệnh nhân chọn khung giờ, nhập lý do khám/triệu chứng và gửi yêu cầu đặt lịch.
- **FR-BOOK-03 (Bảo vệ Concurrency Lock):** Hệ thống khóa bản ghi TimeSlot khi ghi (`findByIdForUpdate`), kiểm tra trạng thái `AVAILABLE` và duy trì `@Version` để phát hiện cập nhật cạnh tranh. Transaction và kiểm tra trạng thái đảm bảo một slot không được xác nhận cho hai lịch hẹn đồng thời.
- **FR-BOOK-04 (Mã Đặt lịch & Mã QR):** Hệ thống tự động sinh Mã lịch hẹn duy nhất (format: `MED-YYMMDD-XXXX`) và tạo chuỗi Base64 mã QR check-in.
- **FR-BOOK-05 (Gửi Email sau commit):** Booking/hủy lịch phát sự kiện trong transaction; `AppointmentEmailListener` xử lý bất đồng bộ (`@Async`) ở pha `AFTER_COMMIT`, vì vậy lỗi email không rollback dữ liệu nghiệp vụ đã commit.
- **FR-BOOK-06 (Hủy lịch hẹn):** Bệnh nhân có thể hủy lịch hẹn trước giờ khám. Hệ thống tự động giải phóng TimeSlot về trạng thái `AVAILABLE` và gửi email thông báo hủy.

### 3.4. Phân hệ Quầy Tiếp nhận & Check-in (Reception & Check-in Desk)
- **FR-REC-01 (Tra cứu tiếp nhận):** Nhân viên lễ tân có thể tra cứu lịch hẹn theo Mã đặt lịch, Số điện thoại hoặc Họ tên bệnh nhân.
- **FR-REC-02 (Check-in & Cấp số thứ tự):** Lễ tân bấm Check-in tiếp nhận, hệ thống tự động sinh Số thứ tự khám trong ngày (format: `STT-01`, `STT-02`...) và chuyển trạng thái lịch hẹn sang `CHECKED_IN`. Sau khi transaction commit, hệ thống phát sự kiện cập nhật hàng chờ tới đúng tài khoản bác sĩ phụ trách.
- **FR-REC-03 (Tiếp nhận Bệnh nhân Vãng lai):** Lễ tân có thể tạo hồ sơ và đặt lịch trực tiếp tại quầy cho bệnh nhân chưa đăng ký online.

### 3.5. Phân hệ Khám bệnh & Kê đơn thuốc (Doctor Examination Desk)
- **FR-EXAM-01 (Hàng chờ khám):** Bác sĩ xem danh sách bệnh nhân đã check-in trong ngày theo thứ tự số STT tiếp nhận.
- **FR-EXAM-02 (Bệnh án điện tử):** Bác sĩ xem tiền sử các lần khám trước của bệnh nhân, nhập triệu chứng lâm sàng quan sát được, chẩn đoán y khoa, lời dặn và hẹn ngày tái khám (`MedicalRecord`).
- **FR-EXAM-03 (Kê đơn thuốc động & Quản lý tồn kho):** Bác sĩ kê đơn thuốc: chọn thuốc từ kho dược, nhập số lượng, liều dùng, cách dùng. Hệ thống tự động kiểm tra và trừ số lượng tồn kho dược phẩm.
- **FR-EXAM-04 (Xuất Đơn thuốc PDF):** Hệ thống tạo file PDF đơn thuốc và phiếu khám bệnh chuẩn y tế có thể in hoặc tải về trực tuyến.
- **FR-EXAM-05 (Hàng chờ realtime):** Doctor Desk kết nối Spring WebSocket/STOMP qua endpoint `/ws`, subscribe user destination `/user/queue/doctor` và tự làm mới khi trạng thái hàng chờ thay đổi `CHECKED_IN`, `IN_PROGRESS` hoặc `COMPLETED`. Sự kiện chỉ được broadcast ở pha `AFTER_COMMIT`; payload không chứa tên bệnh nhân, chẩn đoán hay dữ liệu bệnh án.

### 3.6. Phân hệ Hóa đơn & Thu viện phí (Billing & Invoice)
- **FR-BILL-01 (Tự động sinh Hóa đơn):** Khi bác sĩ hoàn tất khám, hệ thống tự động tính tổng tiền (Tiền công khám + Tiền thuốc trong đơn) và tạo Hóa đơn viện phí (`Invoice`).
- **FR-BILL-02 (Thu viện phí):** Lễ tân có thể xác nhận tiền mặt tại quầy. Với chuyển khoản, hệ thống tạo VietQR chứa đúng ngân hàng, tài khoản, số tiền và nội dung riêng của hóa đơn; hóa đơn vẫn `UNPAID` cho tới khi đối soát thành công.
- **FR-BILL-03 (Xuất Hóa đơn PDF):** Hệ thống hỗ trợ xuất và in Hóa đơn thanh toán viện phí định dạng PDF.
- **FR-BILL-04 (Xác nhận chuyển khoản):** Callback server-to-server phải có HMAC SHA-256, event id và bank transaction id duy nhất, khớp tuyệt đối transfer code và số tiền, còn thời hạn và có tính idempotent. Lễ tân được xác nhận thủ công chỉ sau khi nhập mã giao dịch đã kiểm tra trên tài khoản ngân hàng; mọi kết quả được ghi audit trail.

### 3.7. Phân hệ Quản trị & Báo cáo Doanh thu (Admin Dashboard & Reports)
- **FR-ADMIN-01 (Dashboard KPI):** Admin theo dõi tổng số bệnh nhân, số bác sĩ, số ca khám hôm nay, tổng doanh thu thực thu.
- **FR-ADMIN-02 (Biểu đồ Chart.js):** Hiển thị biểu đồ phân bổ ca khám theo chuyên khoa và biểu đồ tỷ lệ trạng thái lịch hẹn (Hoàn tất, Đang khám, Hủy).
- **FR-ADMIN-03 (Quản lý Danh mục Kho thuốc):** Admin thêm mới thuốc, cập nhật giá bán, hướng dẫn sử dụng, trạng thái kinh doanh và cấu hình `reorderLevel` riêng cho từng thuốc. Form chỉnh sửa danh mục không dùng để thay đổi trực tiếp số lượng tồn kho.
- **FR-ADMIN-04 (Cảnh báo Tồn kho thấp):** Mỗi thuốc có ngưỡng nhập lại (`reorderLevel`, mặc định 50). Thuốc đang kinh doanh có `stockQuantity <= reorderLevel` phải được đánh dấu "Sắp hết"; `stockQuantity = 0` được đánh dấu "Hết hàng". Dashboard Admin và trang Kho thuốc phải hiển thị số thuốc cần bổ sung.
- **FR-ADMIN-05 (Nhập kho & Nhật ký Kho):** Admin nhập bổ sung số lượng dương, có thể ghi giá nhập và ghi chú. Hệ thống tăng tồn kho trong cùng transaction và tạo `InventoryTransaction` lưu tồn trước/sau, người thực hiện và thời điểm. Khi bác sĩ hoàn tất đơn thuốc, lượng thuốc xuất kho cũng phải tạo transaction `DISPENSE`; rollback ca khám phải rollback cả biến động kho và nhật ký tương ứng.

---

## 4. YÊU CẦU PHI CHỨC NĂNG (NON-FUNCTIONAL REQUIREMENTS)

### 4.1. Hiệu năng & Khả năng đáp ứng (Performance)
- **NFR-PERF-01:** Với các trang công khai và portal đã nêu trong `docs/performance/TEST_PLAN.md`, p95 thời gian tải tài liệu HTML sau warm-up dưới 1,5 giây trong môi trường nghiệm thu được ghi nhận. Kết quả baseline phải kèm cấu hình môi trường; khi chưa có kết quả, yêu cầu ở trạng thái chưa xác minh.
- **NFR-PERF-02:** Email chạy bất đồng bộ sau commit; QR và PDF được sinh trong bộ nhớ/stream với kích thước hữu hạn, không tạo file tạm ngoài ý muốn.
- **NFR-RT-01:** Kênh realtime hàng chờ dùng STOMP user destination theo session đã xác thực, chỉ broadcast sau commit thành công và client tự thử kết nối lại sau khi socket bị gián đoạn.

### 4.2. Bảo mật & An toàn thông tin (Security)
- **NFR-SEC-01:** Toàn bộ mật khẩu lưu trữ trong CSDL phải được băm bằng thuật toán **BCrypt** với salt ngẫu nhiên.
- **NFR-SEC-02:** Phòng chống tấn công **CSRF (Cross-Site Request Forgery)** trên toàn bộ các biểu mẫu POST/PUT/DELETE.
- **NFR-SEC-03:** Kiểm soát truy cập nghiêm ngặt ở cả tầng Filter URL và tầng Service method (`@PreAuthorize`).
- **NFR-SEC-04:** Chống **SQL Injection** thông qua cơ chế Parameterized Query của Spring Data JPA.
- **NFR-SEC-05:** Token khôi phục mật khẩu chỉ lưu dưới dạng SHA-256 hash, có hạn 24 giờ, có `used_at`, và response yêu cầu reset không làm lộ email có tồn tại hay không.
- **NFR-SEC-06:** Session phải chống session fixation, giới hạn thời gian sống hợp lý; cookie phiên dùng `HttpOnly` + `SameSite=Lax`, profile SQL Server production mặc định `Secure=true`; response không lộ stacktrace/binding error và bổ sung các security header cần thiết.
- **NFR-SEC-07:** Các endpoint xác thực/khôi phục mật khẩu nhạy cảm phải có rate limit theo remote IP + endpoint. Profile SQL Server dùng shared bucket để nhiều instance cùng áp dụng một ngưỡng; khóa bucket và log principal/IP được băm, không ghi password, token hoặc secret.
- **NFR-SEC-08:** Callback thanh toán ngân hàng được miễn CSRF vì không dùng browser session, nhưng bắt buộc chữ ký HMAC constant-time, idempotency và kiểm tra số tiền/nội dung trước khi cập nhật hóa đơn.

### 4.3. Khả năng quan sát & Vận hành (Operational Observability)
- **NFR-OPS-01:** Hệ thống phải cung cấp health endpoint tối thiểu cho giám sát liveness/readiness ở mức ứng dụng và metrics cho vận hành; health public chỉ lộ trạng thái tổng quát, còn metrics chỉ cho `ROLE_ADMIN`.
- **NFR-OPS-02:** Email có retry hữu hạn và metric success/retry/failure; dữ liệu SQL Server và uploads phải có backup kèm checksum, restore drill định kỳ, retention cho token/bucket/audit và secret nằm ngoài repository.

### 4.4. Tính toàn vẹn dữ liệu & Xử lý đồng thời (Data Integrity & Concurrency)
- **NFR-ACID-01:** Các workflow đặt lịch, check-in, hoàn tất bệnh án, trừ kho và lập hóa đơn được bao trong transaction Spring. Booking khai báo `READ_COMMITTED`; các transaction còn lại dùng isolation mặc định của datasource/SQL Server.
- **NFR-ACID-02:** Kết hợp row lock, `@Version`, unique/filter index cho queue và kiểm tra trạng thái nghiệp vụ để giảm race condition, lost update và double booking; các trường hợp cạnh tranh chính phải có integration test.

### 4.5. Tính khả dụng & Trải nghiệm người dùng (Usability & UX)
- **NFR-USAB-01:** Giao diện Responsive chuẩn Bootstrap 5.3, hiển thị tối ưu trên máy tính để bàn, máy tính bảng và điện thoại di động.
- **NFR-USAB-02:** Phản hồi tương tác thân thiện qua hộp thoại SweetAlert2 và thông báo Toast rõ ràng.


## 5. PHẠM VI TƯƠNG LAI (FUTURE SCOPE)
- Tích hợp trực tiếp MB VietQR Paygate/App-to-App sau khi phòng khám hoàn tất hợp đồng merchant và nhận credential/checksum chính thức. Bản hiện tại dùng VietQR tới tài khoản ngân hàng cấu hình và callback HMAC trung gian.
- Đánh giá bác sĩ sau ca khám.
- UI Admin quản trị lịch trực/TimeSlot.
- Notification center hoàn chỉnh trên nền entity/repository hiện có.
