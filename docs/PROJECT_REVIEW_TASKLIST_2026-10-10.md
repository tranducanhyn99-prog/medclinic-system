# Tasklist xử lý review MedClinic — 10/10/2026

**Trạng thái: 9/9 hoàn tất trong working tree.** Code, migration và regression test đã được bổ sung. Xem [báo cáo nghiệm thu](PROJECT_REVIEW_FIX_ACCEPTANCE_2026-10-10.md) để đối chiếu thay đổi, test và giới hạn; chưa commit/push/deploy.

Mã RV giữ đúng số thứ tự trong [review gốc](PROJECT_REVIEW_2026-10-10.md). Chỉ đánh dấu hoàn tất khi đã sửa, test phù hợp đạt và có bằng chứng mới; không dùng kết quả test trước khi sửa để đóng task.

Các mục “Việc làm”, “Tiêu chí hoàn tất” và “Kiểm thử” bên dưới giữ phạm vi kế hoạch ban đầu. Quyết định cuối: một số điện thoại cho một tài khoản, profile cho phép null; thu ngân mặc định UNPAID/25 dòng. Google OAuth hoãn theo yêu cầu và không thuộc chín task này.

## Thứ tự thực hiện

| Đợt | Task | Ưu tiên | Mục tiêu |
| --- | --- | --- | --- |
| 1 | RV-01, RV-03 | P1, P2 | Chặn phiên của tài khoản bị khóa; bảo vệ challenge OTP |
| 2 | RV-06, RV-02, RV-04, RV-05 | P3, P2 | Sửa đầu vào và thống nhất dữ liệu; RV-06 làm trước RV-02 để dùng chung quy tắc số điện thoại đúng |
| 3 | RV-07 | P2 | Kiểm chứng và thống nhất khóa thanh toán trên SQL Server |
| 4 | RV-09, RV-08 | P2 | Ngày nghiệp vụ nhất quán; phân trang thu ngân |

P1 là ưu tiên cao nhất trong danh sách; P2 ảnh hưởng nghiệp vụ/an toàn/vận hành; P3 là lỗi validation có phạm vi nhỏ. Thứ tự trên còn xét quan hệ giữa các thay đổi, không chỉ mức ưu tiên.

## Các task

### RV-01 — Thu hồi quyền của session khi khóa tài khoản · P1

- [x] Hoàn tất RV-01.

**Phạm vi:** [UserService](../src/main/java/com/medclinic/service/UserService.java), [SecurityConfig](../src/main/java/com/medclinic/config/SecurityConfig.java), [CustomUserDetails](../src/main/java/com/medclinic/security/CustomUserDetails.java) và đường remember-me.

**Việc làm:** thu hồi các session liên quan khi admin khóa; bảo đảm request tiếp theo không dùng principal cũ để vượt kiểm tra active. Dùng cơ chế sẵn có nếu phù hợp, tránh tạo một hệ thống session mới chỉ để xử lý task này.

**Tiêu chí hoàn tất:** session cũ không đọc/ghi dữ liệu được bảo vệ sau khóa; đăng nhập mới và remember-me không khôi phục quyền của user inactive; tài khoản khác đang hoạt động không bị logout hàng loạt.

**Kiểm thử:** login → khóa từ phiên admin → GET trang bệnh nhân và POST một thao tác ghi đều bị chặn; kiểm tra nhiều session của cùng user, remember-me và đăng nhập mới.

### RV-02 — Thống nhất chính sách số điện thoại · P2

- [x] Hoàn tất RV-02.

**Phạm vi:** [UserService](../src/main/java/com/medclinic/service/UserService.java), [UserRepository](../src/main/java/com/medclinic/repository/UserRepository.java), [AppointmentService](../src/main/java/com/medclinic/service/AppointmentService.java), tạo bác sĩ/cập nhật profile và schema nếu cần.

**Việc làm:** chốt một số điện thoại có được dùng chung cho nhiều hồ sơ không. Đề xuất mặc định theo hành vi profile hiện tại: một số cho một tài khoản. Nếu chọn chính sách này, chuẩn hóa/kiểm tra ở các đường ghi, rà dữ liệu trùng trước khi thêm constraint phù hợp SQL Server. Nếu cho phép dùng chung, tra cứu phải trả danh sách và lễ tân chọn đúng hồ sơ.

**Tiêu chí hoàn tất:** đăng ký, cập nhật và tiếp nhận dùng cùng chính sách; không còn lỗi single-result query vì số trùng; dữ liệu trùng cũ được phát hiện và xử lý có chủ đích, không tự xóa/gộp hồ sơ.

**Kiểm thử:** hai đăng ký trùng số, cập nhật giữ số của chính mình/đổi sang số của người khác, vãng lai, số null/rỗng theo form hiện có. Nếu thêm unique constraint, kiểm tra hai request đăng ký đồng thời trên SQL Server và thông báo lỗi cho người dùng.

### RV-03 — Ràng buộc challenge OTP với session · P2

- [x] Hoàn tất RV-03.

**Phạm vi:** [AuthController](../src/main/java/com/medclinic/controller/AuthController.java), [AuthVerificationService](../src/main/java/com/medclinic/service/AuthVerificationService.java), test đăng ký/đăng nhập/OTP.

**Việc làm:** tái dùng guard `PENDING_OTP_CHALLENGE_ID` cho GET và POST OTP; request từ session khác bị từ chối trước khi đọc thông tin challenge hoặc tăng attempts. Giữ magic link theo token và mục đích mở từ email.

**Tiêu chí hoàn tất:** foreign-session không xem challenge, không làm cạn lượt nhập và không xác minh OTP của session khác; session đúng vẫn verify/resend được; hết hạn và giới hạn attempts vẫn có hiệu lực.

**Kiểm thử:** mã đúng/sai từ session chủ, mã từ session lạ, không có session, challenge hết hạn, resend; cập nhật test cũ để giữ session đã tạo challenge và kiểm tra riêng magic link.

### RV-04 — Xác minh nội dung file avatar · P2

- [x] Hoàn tất RV-04.

**Phạm vi:** [AvatarStorageService](../src/main/java/com/medclinic/service/AvatarStorageService.java), luồng cập nhật avatar trong [AuthController](../src/main/java/com/medclinic/controller/AuthController.java), test upload.

**Việc làm:** kiểm tra nội dung/signature và khả năng giải mã phù hợp JPG/PNG/WebP; giới hạn pixel ngoài giới hạn 5 MB. Chỉ thay avatar sau khi file mới hợp lệ; bảo toàn UUID/path guard. Xác nhận hỗ trợ WebP trước khi chọn decoder.

**Tiêu chí hoàn tất:** MIME giả hoặc ảnh hỏng bị từ chối trước khi thay ảnh cũ; JPG/PNG/WebP thật vẫn dùng được; thông báo lỗi rõ; không để file tạm lỗi tích tụ.

**Kiểm thử:** ba byte giả PNG, ảnh bị cắt, mismatch MIME/nội dung, file vượt 5 MB, ảnh vượt giới hạn pixel, một ảnh thật mỗi loại và cập nhật thất bại giữ nguyên avatar trước đó.

### RV-05 — Validation tiền VND nhất quán · P2

- [x] Hoàn tất RV-05.

**Phạm vi:** [AdminMedicineDto](../src/main/java/com/medclinic/dto/AdminMedicineDto.java), [AdminDoctorCreateDto](../src/main/java/com/medclinic/dto/AdminDoctorCreateDto.java), service thuốc/bác sĩ, invoice và [BankTransferPaymentService](../src/main/java/com/medclinic/service/BankTransferPaymentService.java).

**Việc làm:** kiểm tra giá VND nguyên, không âm và giới hạn theo schema tại DTO/service; rà các đường sửa giá. Kiểm tra dữ liệu có phần lẻ đã tồn tại, ghi phương án xử lý trước khi siết constraint; không tự làm tròn tiền đã ghi trên hóa đơn.

**Tiêu chí hoàn tất:** request trực tiếp không lưu giá có phần lẻ hoặc vượt giới hạn; giá có giá trị nguyên như `1000.00` vẫn hợp lệ; tổng hóa đơn không vượt miền lưu trữ và QR tạo được từ tổng hợp lệ.

**Kiểm thử:** `1000.50`, số âm, zero nếu nghiệp vụ cho phép, giới hạn lớn nhất và vượt giới hạn; ca thuốc → đơn → invoice → tạo instruction QR với cấu hình giả lập, không chuyển tiền thật.

### RV-06 — Sửa regex số điện thoại · P3

- [x] Hoàn tất RV-06.

**Phạm vi:** [UserRegisterDto](../src/main/java/com/medclinic/dto/UserRegisterDto.java), [AdminDoctorCreateDto](../src/main/java/com/medclinic/dto/AdminDoctorCreateDto.java); rà kiểm tra số điện thoại tại các caller.

**Việc làm:** thay character class chứa `|` bằng quy tắc `^0[35789][0-9]{8}$`; thống nhất với vãng lai/cập nhật mà không thay đổi chính sách số chung khi chưa chốt RV-02.

**Tiêu chí hoàn tất:** `0|12345678` bị từ chối; các đầu số 03/05/07/08/09 đủ 10 chữ số hợp lệ; lỗi hiển thị đúng ở form.

**Kiểm thử:** từng đầu số hợp lệ, ký tự `|`, chữ cái, thiếu/thừa chữ số và request HTTP gửi thẳng vào form. RV-02 sử dụng quy tắc đã sửa này.

### RV-07 — Kiểm chứng và thống nhất thứ tự khóa thanh toán · P2

- [x] Hoàn tất RV-07.

**Phạm vi:** [BankTransferPaymentService](../src/main/java/com/medclinic/service/BankTransferPaymentService.java), repository Invoice/BankTransferPayment và SQL Server integration test.

**Việc làm:** dựng ca concurrent tạo lại instruction hết hạn với manual-confirm/callback; thống nhất Invoice → Payment ở các đường ghi. Không dùng retry để che thứ tự khóa bị đảo; chỉ bổ sung retry nếu còn nhu cầu sau khi phân tích kết quả.

**Tiêu chí hoàn tất:** source dùng thứ tự khóa nhất quán; test SQL Server đồng thời hoàn tất trong timeout, không deadlock, không tạo hai lần thanh toán; đối soát thủ công, callback lặp và tiền mặt giữ đúng trạng thái/invariant.

**Kiểm thử:** concurrent regenerate/manual-confirm; callback trùng; callback và cash cùng invoice; amount/signature sai vẫn bị từ chối. Chạy trên database kiểm thử riêng theo [runbook](database/SQLSERVER_RUNBOOK.md), ghi kết quả thực tế; không đóng task chỉ vì H2 pass.

### RV-08 — Phân trang và lọc hóa đơn thu ngân · P2

- [x] Hoàn tất RV-08.

**Phạm vi:** [StaffReceptionController](../src/main/java/com/medclinic/controller/StaffReceptionController.java), [InvoiceService](../src/main/java/com/medclinic/service/InvoiceService.java), [InvoiceRepository](../src/main/java/com/medclinic/repository/InvoiceRepository.java), [billing template](../src/main/resources/templates/staff/billing.html) và caller/test liên quan.

**Việc làm:** query có Pageable và lọc ngày/trạng thái ở database; đề xuất mặc định hóa đơn chưa trả, 25 dòng/trang. Giữ đường tìm lịch sử đã trả, bộ lọc khi đổi trang và cập nhật hiển thị sau thu tiền. Giới hạn tham số page/size trên server.

**Tiêu chí hoàn tất:** không lấy toàn bộ invoice để phân trang trong RAM; số dòng mỗi page bị giới hạn; ca chưa trả không bị lẫn/mất khi chuyển trang; sau trả tiền, danh sách/trang rỗng và trạng thái filter được xử lý đúng trên desktop/mobile.

**Kiểm thử:** filter ngày/status, page biên, page/size không hợp lệ, hóa đơn paid vẫn tìm được, cash/bank sau phân trang. Dùng fixture khoảng 10.000 hóa đơn để ghi thời gian tải và số dòng trả về; không tự đặt số latency khi chưa đo.

### RV-09 — Dùng business clock cho ngày nghiệp vụ · P2

- [x] Hoàn tất RV-09.

**Phạm vi:** [DashboardService](../src/main/java/com/medclinic/service/DashboardService.java), [DoctorDeskController](../src/main/java/com/medclinic/controller/DoctorDeskController.java), [DataInitializer](../src/main/java/com/medclinic/config/DataInitializer.java), [InvoiceCodeReservationCoordinator](../src/main/java/com/medclinic/service/impl/InvoiceCodeReservationCoordinator.java); rà các chỗ `LocalDate.now()` khác.

**Việc làm:** inject business Clock đang có cho các đường tính ngày trên; giữ quy tắc ngày demo/prod, định dạng mã và cơ chế cấp mã unique. Không viết lớp thời gian mới khi Clock hiện tại đáp ứng được.

**Tiêu chí hoàn tất:** ngày thống kê/lịch trực/seed/prefix invoice cùng theo Việt Nam dù JVM là UTC; cửa sổ booking +14 và tính duy nhất của mã không bị thay đổi.

**Kiểm thử:** Clock cố định ở 00:30 Việt Nam khi UTC còn ngày trước, thêm biên cuối ngày; kiểm tra từng caller và regression cấp mã hóa đơn/concurrency.

## Kiểm tra trước khi đóng một đợt

Chạy test đích của task trước; sau khi hoàn thành đợt, chạy suite phù hợp phạm vi thay đổi:

```powershell
mvn --batch-mode --no-transfer-progress clean verify
node --test src/test/frontend/ui-regressions.cjs
node ops/check-doc-links.cjs
```

Với RV-02 có migration và RV-07, SQL Server suite guarded trên database test riêng là phần nghiệm thu bắt buộc. Ghi số executed/pass/skipped riêng; không dùng database demo cần giữ.

Khi đóng task: ghi file thay đổi, ca test/kết quả và bằng chứng; cập nhật checkbox/tổng số hoàn tất ở tài liệu này. Giữ [review 10/10](PROJECT_REVIEW_2026-10-10.md) làm mốc ban đầu, bổ sung link báo cáo sửa mới thay vì ghi đè lịch sử. Cập nhật README/kịch bản nếu hành vi demo hoặc hướng dẫn chạy thay đổi.

**Đã thực hiện:** sửa đủ chín task, suite mặc định 258 pass, SQL Server riêng 37 pass và frontend 26 pass. README, runbook và kịch bản thuyết trình đã cập nhật. Bằng chứng chi tiết và phạm vi chưa kiểm tra ở [báo cáo nghiệm thu](PROJECT_REVIEW_FIX_ACCEPTANCE_2026-10-10.md).
