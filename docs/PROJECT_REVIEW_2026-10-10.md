# Review MedClinic — 10/10/2026

**Mốc trước sửa.** Các phát hiện và kết quả dưới đây giữ nguyên để đối chiếu. Kết quả xử lý tiếp theo nằm trong [báo cáo nghiệm thu chín task](PROJECT_REVIEW_FIX_ACCEPTANCE_2026-10-10.md) và [tasklist](PROJECT_REVIEW_TASKLIST_2026-10-10.md).

MedClinic có một luồng nghiệp vụ liên thông đủ để bảo vệ đồ án Lập trình Web: đặt lịch → tiếp nhận → khám/kê đơn → xuất kho/lập hóa đơn → thanh toán/xem hồ sơ. Cấu trúc MVC và service transaction phù hợp quy mô một phòng khám. **Có thể dùng bản hiện tại để demo bằng dữ liệu mẫu; cần sửa các vấn đề về phiên đăng nhập, OTP và dữ liệu trước khi dùng với bệnh nhân thật.**

Review trên working tree, gồm các thay đổi giao diện chưa commit từ phiên trước. Lượt này dọn file và viết tài liệu, **chưa sửa các lỗi nghiệp vụ dưới đây, chưa commit/push**. Báo cáo theo ngày trước đây chỉ chứng minh phiên bản tại thời điểm đó.

## Phạm vi và bằng chứng

Đã lập bản đồ source, test, template/asset, cấu hình, migration, CI, Docker và tài liệu; đọc sâu các luồng xác thực, phân quyền, đặt/tiếp nhận lịch, khám/kho/hóa đơn, chuyển khoản, upload và xuất PDF. Đã chạy toàn bộ suite mặc định và sáu kiểm chứng độc lập. Không khẳng định đã đọc từng dòng của mọi template, generator, tài liệu lịch sử hoặc thư viện vendor.

Giả định vận hành để đánh giá: một phòng khám, một ứng dụng, vài chục người dùng đồng thời và lịch sử tăng đến hàng nghìn hồ sơ/hóa đơn. Đây là giả định để tìm giới hạn, không phải kết quả load test.

| Kiểm tra ngày 10/10 | Kết quả |
| --- | --- |
| `mvn --batch-mode --no-transfer-progress clean verify` sau dọn | Thành công; 256 test được báo cáo, **230 chạy và pass**, 26 SQL Server skipped; 0 failure/error |
| JaCoCo LINE | 2.086 covered / 2.435 tổng = **85.67%**; gate 70% |
| JaCoCo BRANCH | 720 covered / 1.039 tổng = **69.30%**; gate 50% |
| `node --test src/test/frontend/ui-regressions.cjs` | **26/26 pass** |
| `node ops/check-doc-links.cjs` | Không có link file nội bộ bị hỏng |
| Sáu probe review | Tái hiện đủ sáu ca ở mục 1–6; H2 riêng, upload riêng, không dùng database demo đang chạy |

Probe dùng service/validator thật và MockMvc có Spring Security; các request POST có CSRF token. Mục 5 kiểm chứng validator nhận tiền lẻ và phép chuyển số nguyên từ chối; không chạy một giao dịch ngân hàng thật. Raw log, classpath, mã probe và archive giữ local trong `output`, đã ignore. [Tóm tắt kết quả công khai](evidence/project-review-2026-10-10/verification-summary.json) không chứa OTP, token, email bệnh nhân hay secret.

Coverage đo mức source được test đi qua; không chứng minh mọi điều kiện nghiệp vụ hoặc bảo mật đều đúng. Các ca mới ở mục 1–6 vẫn lọt qua suite hiện có.

## Bản đồ dự án

| Phần | Thành phần và nhận xét |
| --- | --- |
| Web | Spring MVC, Thymeleaf/Layout, Bootstrap và JavaScript. HTML render từ server; API tải khung giờ và STOMP cập nhật hàng chờ. Chưa cần tách SPA/microservice cho phạm vi đồ án. |
| Nghiệp vụ | Controller nhận dữ liệu; DTO/Bean Validation kiểm tra form; service quyết định quyền, trạng thái, transaction; repository truy vấn/khóa. Có thêm kiểm tra service, không chỉ ẩn nút trên UI. |
| Dữ liệu | User/Role, Specialty/DoctorProfile, WorkSchedule/TimeSlot, Appointment, MedicalRecord/Prescription/PrescriptionItem, Medicine/InventoryTransaction, Invoice/BankTransferPayment và bảng xác thực/audit. |
| Môi trường | `application.yml` chứa nhiều profile. Dev: H2 RAM, seed, không gửi mail. Demo Compose: SQL Server + Mailpit. Ngoài dev/demo không cho xử lý lịch tương lai. |
| Triển khai | Flyway V1–V6 cho SQL Server, Hibernate validate; Dockerfile, Compose, script init/backup/restore, mẫu reverse proxy. SQL Server suite có guard chống trỏ nhầm database. |
| Kiểm chứng | Java unit/integration, frontend regression, JaCoCo, CI Windows. Nghiệm thu browser/SMTP/camera trước đây được giữ theo ngày; chưa chạy lại trong lượt này. |

Điểm có cơ sở trong source:

- [AppointmentService](../src/main/java/com/medclinic/service/AppointmentService.java) khóa khung giờ, kiểm tra bác sĩ/ngày/giờ/status trước khi đặt; quyền đặt thay chỉ dành cho vai trò được phép.
- [MedicalRecordService](../src/main/java/com/medclinic/service/MedicalRecordService.java) kiểm tra bác sĩ phụ trách, bắt đầu khám trước khi hoàn tất; cộng số lượng cùng thuốc, khóa thuốc theo ID tăng dần, kiểm tra tồn kho, chụp giá lúc kê. Hồ sơ, đơn thuốc, kho, hóa đơn nằm trong transaction.
- [PDF service](../src/main/java/com/medclinic/service/PdfExportService.java) có kiểm tra chủ sở hữu/vai trò; biết ID tài liệu không tự động có quyền tải.
- [HmacSignatureVerifier](../src/main/java/com/medclinic/security/HmacSignatureVerifier.java) kiểm tra định dạng chữ ký và so sánh bằng `MessageDigest.isEqual`; service thanh toán kiểm tra số tiền, reference và mã giao dịch. QR được tạo không đồng nghĩa tiền đã nhận.
- Listener hàng chờ/email hoạt động sau commit, tránh báo thành công khi transaction bị rollback. Frontend có kiểm tra responsive, trạng thái lỗi và thư viện local; regression này không thay thế thao tác browser thật.

## Phải sửa trước khi dùng dữ liệu thật

### 1. [P1] Khóa tài khoản chưa thu hồi phiên đang đăng nhập

**Vị trí:** [UserService.java](../src/main/java/com/medclinic/service/UserService.java#L126), [CustomUserDetails.java](../src/main/java/com/medclinic/security/CustomUserDetails.java), [SecurityConfig.java](../src/main/java/com/medclinic/config/SecurityConfig.java).

**Ca đã tái hiện:** đăng nhập `patient` → admin đổi `isActive` thành false → dùng lại session cũ gọi `/patient/appointments` vẫn trả HTTP 200. User trong principal đã được nạp lúc đăng nhập; thao tác khóa không loại session và chưa có kiểm tra active lại trên request.

**Hậu quả:** người quản trị tưởng đã chặn tài khoản nhưng phiên cũ vẫn qua bảo vệ route. Probe xác nhận đọc trang bệnh nhân, chưa thử tất cả thao tác ghi của từng vai trò.

**Sửa nhỏ nhất:** thu hồi các session liên quan khi khóa và bảo đảm request/session hoặc remember-me được kiểm tra lại trạng thái tài khoản. Thêm test đăng nhập → khóa → request tiếp theo bị từ chối, gồm một thao tác ghi; giữ test đăng nhập mới bị chặn.

### 2. [P2] Đăng ký cho phép trùng số điện thoại, tiếp nhận lại giả định chỉ có một hồ sơ

**Vị trí:** [UserService.java](../src/main/java/com/medclinic/service/UserService.java#L30), [UserRepository.java](../src/main/java/com/medclinic/repository/UserRepository.java#L17), [AppointmentService.java](../src/main/java/com/medclinic/service/AppointmentService.java#L112).

**Ca đã tái hiện:** đăng ký hai username/email khác nhau với cùng số điện thoại hợp lệ đều thành công. `findByPhone` trả `Optional<User>` nhưng query gặp hai dòng và ném `IncorrectResultSizeDataAccessException`. Luồng vãng lai dùng chính query đó; profile update lại có kiểm tra số điện thoại trùng, nên quy tắc đang không thống nhất.

**Hậu quả:** tra cứu/tiếp nhận có thể lỗi thay vì chọn đúng bệnh nhân. Đây cũng là điểm phải quyết định nghiệp vụ: gia đình có được dùng chung số liên hệ không?

**Sửa nhỏ nhất:** nếu một số tương ứng một tài khoản, dùng cùng validation ở đăng ký/cập nhật và constraint SQL phù hợp số rỗng/null, xử lý dữ liệu trùng trước migration. Nếu cho phép số chung, query trả danh sách và lễ tân phải chọn hồ sơ; không lấy tùy ý một người. Test hai đăng ký trùng số và tiếp nhận theo chính sách đã chọn.

### 3. [P2] Xác minh OTP chưa kiểm tra challenge thuộc session hiện tại

**Vị trí:** [AuthController.java](../src/main/java/com/medclinic/controller/AuthController.java#L131), [AuthVerificationService.java](../src/main/java/com/medclinic/service/AuthVerificationService.java#L76).

**Ca đã tái hiện:** tạo challenge đăng nhập, gửi năm mã sai từ năm session không liên quan, có CSRF hợp lệ → attempts của challenge đó thành 5, hết ngân sách nhập mã. POST verify chỉ nhận challenge ID/code; POST resend đã có kiểm tra `PENDING_OTP_CHALLENGE_ID`, nhưng GET/POST OTP chưa áp dụng cùng ràng buộc.

**Hậu quả:** người biết/đoán được ID có thể làm cạn lượt nhập của người khác. Trang GET còn hiển thị thông tin challenge/email đã che. **Không có bằng chứng đăng nhập chiếm tài khoản khi không biết mã OTP hợp lệ.** Rate limit không thay thế kiểm tra sở hữu challenge.

**Sửa nhỏ nhất:** tái dùng guard session hiện có cho màn hình và POST nhập OTP. Magic link có token riêng và mục đích mở từ email cần giữ luồng phù hợp. Cập nhật test OTP để dùng session đã tạo challenge, thêm test foreign-session không làm tăng attempts.

### 4. [P2] Upload avatar tin MIME do client gửi, chưa xác minh bytes là ảnh

**Vị trí:** [AvatarStorageService.java](../src/main/java/com/medclinic/service/AvatarStorageService.java#L33), [AuthController.java](../src/main/java/com/medclinic/controller/AuthController.java#L261).

**Ca đã tái hiện:** upload ba byte `1,2,3` với header `image/png` được lưu và trả URL thành công. UUID/path guard và giới hạn 5 MB đã có; MIME whitelist chưa kiểm tra nội dung ảnh.

**Hậu quả:** file hỏng có thể thay avatar hợp lệ; cấu hình này chưa đủ để chấp nhận upload từ người dùng thật. Probe không chứng minh RCE/XSS.

**Sửa nhỏ nhất:** kiểm tra signature/khả năng giải mã theo JPG, PNG, WebP thực sự hỗ trợ, giới hạn kích thước pixel và chỉ thay ảnh cũ khi nội dung mới hợp lệ. Không vô tình loại WebP vì decoder mặc định không hỗ trợ. Thêm test bytes giả, file bị cắt và một ảnh thật của từng loại hỗ trợ.

### 5. [P2] Validation giá cho phép phần lẻ nhưng VietQR yêu cầu VND nguyên

**Vị trí:** [AdminMedicineDto.java](../src/main/java/com/medclinic/dto/AdminMedicineDto.java#L16), [AdminDoctorCreateDto.java](../src/main/java/com/medclinic/dto/AdminDoctorCreateDto.java#L38), [BankTransferPaymentService.java](../src/main/java/com/medclinic/service/BankTransferPaymentService.java#L197).

**Ca đã kiểm chứng:** DTO thuốc nhận `1000.50` mà không có lỗi validation. Phép `stripTrailingZeros().longValueExact()` dùng khi tạo QR từ chối giá trị này. Nếu thuốc được kê một viên, cộng phí khám nguyên vẫn tạo tổng tiền có phần lẻ và QR không tạo được. HTML `step` không chặn request gửi trực tiếp.

**Hậu quả:** dữ liệu đã được admin lưu có thể làm hỏng thanh toán chuyển khoản ở bước sau. Chưa chạy toàn bộ chuỗi lưu thuốc → lập hóa đơn → ngân hàng thật trong probe này.

**Sửa nhỏ nhất:** xác định giá VND nguyên tại DTO và service, giới hạn độ lớn theo schema; kiểm tra dữ liệu hiện có trước khi siết constraint. Không tự làm tròn số tiền hóa đơn. Test giá có phần lẻ/overflow và tạo QR từ giá hợp lệ.

## Nên sửa để dữ liệu và vận hành ổn định

### 6. [P3] Regex số điện thoại chấp nhận ký tự `|`

**Vị trí:** [UserRegisterDto.java](../src/main/java/com/medclinic/dto/UserRegisterDto.java#L35), [AdminDoctorCreateDto.java](../src/main/java/com/medclinic/dto/AdminDoctorCreateDto.java#L27).

**Ca đã tái hiện:** `0|12345678` qua validation đăng ký. Trong character class `[3|5|7|8|9]`, dấu `|` cũng là ký tự hợp lệ.

**Hậu quả:** số liên hệ không hợp lệ đi vào database; validation hiển thị và kiểm tra ở luồng khác không đồng nhất.

**Sửa nhỏ nhất:** dùng `^0[35789][0-9]{8}$` ở các DTO bị ảnh hưởng, thống nhất với kiểm tra tại luồng vãng lai. Test các đầu số hợp lệ và trường hợp có `|`.

### 7. [P2] Thứ tự khóa ở tạo QR và xác nhận chuyển khoản bị đảo

**Vị trí:** [BankTransferPaymentService.java](../src/main/java/com/medclinic/service/BankTransferPaymentService.java#L39), [payment repository](../src/main/java/com/medclinic/repository/BankTransferPaymentRepository.java).

**Nhận định từ source, chưa tái hiện deadlock trên SQL Server:** tạo instruction khóa Invoice rồi đọc/cập nhật Payment; manual/callback khóa Payment rồi `confirm` khóa Invoice. Khi tạo lại instruction đã hết hạn đồng thời đối soát payment cũ, hai transaction có thể chờ khóa của nhau.

**Hậu quả có điều kiện:** một thao tác thanh toán có thể bị SQL Server chọn làm deadlock victim. Test H2/single-thread pass chưa loại được rủi ro này.

**Sửa nhỏ nhất:** thống nhất thứ tự khóa Invoice → Payment ở các đường ghi, vẫn giữ kiểm tra trạng thái và chống dùng lại giao dịch. Thêm ca concurrent regenerate/manual-confirm trên SQL Server kiểm thử riêng; chỉ cân nhắc retry sau khi thứ tự khóa đã thống nhất.

### 8. [P2] Thu ngân tải toàn bộ lịch sử hóa đơn vào một trang

**Vị trí:** [StaffReceptionController.java](../src/main/java/com/medclinic/controller/StaffReceptionController.java#L120), [InvoiceService.java](../src/main/java/com/medclinic/service/InvoiceService.java#L94), [InvoiceRepository.java](../src/main/java/com/medclinic/repository/InvoiceRepository.java#L27).

**Quan sát source:** route billing gọi `findAllByOrderByCreatedAtDesc`, không Pageable/bộ lọc tại database. Với 10.000 hóa đơn lịch sử, mỗi lần mở trang vẫn lấy và render cả lịch sử dù thu ngân chỉ cần xử lý một số hóa đơn chưa trả.

**Hậu quả dự kiến:** lượng dữ liệu/HTML và thời gian tải tăng theo lịch sử. Chưa đo latency ở 10.000 bản ghi; không dùng số liệu fixture nhỏ trước đây để khẳng định chịu tải thật.

**Sửa nhỏ nhất:** phân trang truy vấn và lọc ngày/trạng thái ở database, mặc định ưu tiên hóa đơn chưa trả. Giữ đường tìm lịch sử riêng qua bộ lọc. Kiểm tra bằng fixture lớn và test số dòng của page.

### 9. [P2] Một số đường tính ngày chưa dùng business clock

**Vị trí:** [DashboardService.java](../src/main/java/com/medclinic/service/DashboardService.java#L39), [DoctorDeskController.java](../src/main/java/com/medclinic/controller/DoctorDeskController.java#L153), [DataInitializer.java](../src/main/java/com/medclinic/config/DataInitializer.java#L94), [InvoiceCodeReservationCoordinator.java](../src/main/java/com/medclinic/service/impl/InvoiceCodeReservationCoordinator.java#L24).

**Điều kiện từ source:** nghiệp vụ lịch dùng Clock `Asia/Ho_Chi_Minh`, nhưng thống kê hôm nay/lịch trực/seed/prefix hóa đơn dùng `LocalDate.now()` theo JVM. Nếu JVM chạy UTC, trong khoảng 00:00–06:59 ở Việt Nam chúng đang tính ngày trước đó. Compose đặt timezone Việt Nam nên giảm rủi ro ở cấu hình demo hiện tại; không bảo đảm môi trường khác giống vậy.

**Hậu quả:** thống kê hoặc lịch hiển thị có thể lệch với ngày booking, prefix hóa đơn khác ngày nghiệp vụ.

**Sửa nhỏ nhất:** inject business Clock đã có vào các chỗ này; test clock cố định tại thời điểm ngày Việt Nam và UTC khác nhau. Không cần tạo một tầng xử lý thời gian mới.

## Repo đã gọn hơn ở đâu

Đã đưa 30 file không cần ra khỏi cây nguồn/local root: 14 ảnh cũ, 7 sidecar, script carousel không được tải, 7 log build và một ảnh README cũ. Tổng **8.88 MiB**, có bản sao SHA-256 local để khôi phục. Thay ảnh README bằng capture bảng màu đã duyệt; thêm ignore cho `tmp`; không thêm dependency. Xem [danh sách và cách khôi phục](REPO_CLEANUP_2026-10-10.md).

Giữ các file có mục đích học thuật/vận hành: rubric Excel, EAPX/XML UML, generator DOCX, migration, runbook, license, test và bằng chứng theo ngày. Generator không phải chức năng web; có thể đưa ra module/công cụ riêng khi cần giảm jar, nhưng chưa đáng đổi build chỉ để làm đẹp cây thư mục. Không xóa `.env`, backup, upload hay các thay đổi đang làm của người dùng.

## Thứ tự xử lý đề xuất

[Tasklist chi tiết](PROJECT_REVIEW_TASKLIST_2026-10-10.md) theo dõi chín mục, phạm vi sửa và tiêu chí nghiệm thu. Trạng thái được cập nhật tại tasklist; các kết quả bên trên là mốc review ban đầu.

1. Phiên sau khóa và sở hữu challenge OTP: lỗi ảnh hưởng trực tiếp kiểm soát truy cập.
2. Chính sách số điện thoại, validation file/tiền và regex: ngăn dữ liệu sai từ đầu.
3. Khóa thanh toán: kiểm chứng và sửa trước khi bật ngân hàng cho vận hành thật.
4. Phân trang thu ngân và clock nhất quán: xử lý giới hạn theo dữ liệu/môi trường.

Ưu tiên cho buổi bảo vệ là trình diễn một ca hoàn chỉnh, giải thích transaction/phân quyền và trình bày giới hạn trung thực. Không cần viết lại giao diện hoặc bổ sung framework để chuẩn bị thuyết trình. [Kịch bản 15 phút](PRESENTATION_SCRIPT_2026-10-10.md) đã chọn luồng phù hợp bản hiện tại.

## Chưa kiểm tra trong lượt này

Không chạy SQL Server integration có guard (26 skipped), Docker rebuild/backup-restore, trình duyệt E2E, camera thật, SMTP thật hoặc giao dịch ngân hàng thật. Không quét CVE toàn bộ dependency, load test, pentest hoặc audit từng byte thư viện vendor. Chưa chạy lại bằng chứng lịch sử được ghi trong [thư mục evidence](evidence/README.md).

**Đánh giá:** kiến trúc vừa sức đồ án và dễ giải thích; code có kiểm soát nghiệp vụ đáng trình bày. Suite mặc định xanh nhưng còn chín mục review mở, trong đó sáu ca đã kiểm chứng. Không coi bản demo là bản đã sẵn sàng phục vụ dữ liệu y tế thật.
