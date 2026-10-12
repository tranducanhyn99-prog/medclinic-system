# Hồ sơ minh chứng bảo vệ MedClinic — đồ án cá nhân

Cập nhật 12/10/2026. Bạn đã xác nhận làm một mình; dùng tài liệu này để chuẩn bị bằng chứng và luyện trình bày, không dùng như phiếu tự chấm thay giảng viên. Kịch bản chính nằm trong [bản trình bày 15 phút](PRESENTATION_SCRIPT_2026-10-10.md).

## 1. Đối chiếu rubric

Rubric gốc ở file Excel trong máy, được giữ nguyên. Các mức điểm của rubric là các mức rời rạc; không suy ra điểm chính xác chỉ từ số chức năng hoặc số test.

| Tiêu chí | Trọng số | Minh chứng có thể mở ngay | Việc bạn cần làm trước bảo vệ |
|---|---:|---|---|
| Độ khó, thực tiễn, phân công | 10% | Một lịch hẹn đi qua bốn vai trò; chống đặt trùng, thiếu thuốc, thu hai lần | Giải thích vì sao chọn phạm vi phòng khám và nói rõ đồ án cá nhân; xác nhận với giảng viên cách áp dụng mục phân công |
| AI, làm chủ code, GitHub | 20% | Hai tình huống ở mục 3; commit/diff thực tế | Tự đọc, giải thích và thử thay đổi một quy tắc; không nhận là đã làm chủ chỉ vì test xanh |
| Dữ liệu, vai trò, UI, phương pháp, thuyết trình | 20% | SRS → UML → schema → RTM; giao diện theo bốn công việc | Đi hết demo, giải thích trạng thái và quan hệ dữ liệu; thừa nhận liên kết dư/snapshot ở thiết kế CSDL |
| Ứng dụng, code, cài đặt, chức năng | 35% | README, transaction service, test và CI đúng commit | Thử chạy theo README và demo đúng môi trường đã chuẩn bị |
| Nội dung ngoài chương trình | 15% | Tám nhóm ứng viên ở mục 2 | Đối chiếu đề cương môn học; mỗi nhóm cần giải thích code và cách kiểm chứng |

Không dựng thành viên, review chéo hoặc lịch sử Git giả để bù tiêu chí nhóm. Lịch sử cá nhân, cách tách thay đổi và kiểm thử là bằng chứng thực tế; có được thay cho cộng tác nhóm hay không do giảng viên quyết định.

## 2. Tám nhóm nội dung mở rộng để đề xuất

Đây là **ứng viên**, chưa có đề cương để xác nhận nhóm nào nằm ngoài chương trình. Không đếm mỗi thư viện thành một nội dung; QR và camera thuộc cùng nhóm, Docker/Flyway/CI thuộc cùng nhóm vận hành. Hai nhóm xác thực cần được giảng viên chấp nhận là hai nội dung riêng.

| Nhóm | Bài toán, code chính | Demo / kiểm chứng | Giới hạn cần nói đúng |
|---|---|---|---|
| Google OIDC | `GoogleLoginConfig`, `GoogleAccountService`; subject ổn định và email đã xác thực | `GoogleCallbackProtocolIntegrationTest`, `GoogleLoginIntegrationTest`; Google → OTP → patient | OAuth Client và secret riêng; chỉ bệnh nhân; callback hợp lệ chưa mở session |
| OTP và magic link | `AuthVerificationService`, `AuthController`; hash, hạn dùng, session ownership, consume một lần | `OtpAuthenticationIntegrationTest`; đăng ký/mật khẩu + OTP, resend làm mã cũ mất hiệu lực | Google và OTP cùng Gmail không phải hai yếu tố độc lập; seed mật khẩu bỏ qua OTP |
| QR tiếp nhận | `QrCodeUtil`, `StaffReceptionController`, JS quét QR | Đặt lịch lấy QR/mã; lễ tân tiếp nhận đúng ca | QR là mã tra cứu, không phải quyền truy cập; camera cần thử trên thiết bị thật |
| WebSocket sau commit | `DoctorQueueWebSocketListener`, `DoctorQueueChangedEvent`, `WebSocketConfig` | `DoctorQueueWebSocketAfterCommitIntegrationTest`; lễ tân đổi trạng thái, bác sĩ thấy cập nhật ở browser riêng | Payload không mang bệnh án; realtime chưa phải bằng chứng chịu tải lớn |
| PDF tiếng Việt | `PdfExportService`, `InvoicePdfLayout`, font Unicode local | `PrescriptionPdfPresentationTest`; tải đơn thuốc/hóa đơn từ đúng tài khoản | Export vẫn phải kiểm tra quyền, không chỉ che nút trên UI |
| Transaction và cạnh tranh | `AppointmentService`, `MedicalRecordService`; khóa slot/thuốc, rollback khi thiếu kho | `ConcurrentBookingIntegrationTest`, `MedicineStockConcurrencyIntegrationTest`, `MedicalRecordRollbackIntegrationTest` | Test hai request có kiểm soát không thay thế benchmark tải thực tế |
| Thanh toán xác thực và idempotency | `BankTransferPaymentService`, `HmacSignatureVerifier`; khóa Invoice → Payment, unique giao dịch | `BankTransferPaymentServiceTest`; callback sai chữ ký/số tiền bị từ chối, gửi lại không thu hai lần | Module tích hợp mẫu; chưa chứng minh giao dịch ngân hàng thật; tạo VietQR không có nghĩa đã trả tiền |
| Vận hành có kiểm chứng | Docker Compose, Flyway V1–V10, `.github/workflows/ci.yml`, JaCoCo | Chạy README; health; migration SQL Server; Actions đúng SHA | CI thường bỏ qua test SQL có điều kiện; phải đọc executed/skipped, không cộng chúng thành test đã chạy |

## 3. Hai tình huống để chứng minh bạn hiểu code có AI hỗ trợ

Các tình huống có code và test thực tế; bạn cần tự chuẩn bị lời giải thích. Không nói đã tự viết mọi dòng hoặc đã tự phản biện AI nếu chưa làm.

### A. Số điện thoại: sửa quy tắc ở một nơi và kiểm tra từ đầu đến database

Mở `src/main/java/com/medclinic/util/PhoneNumbers.java`, các caller trong form/service, migration V7 và `PhoneAndVndValidationTest`, `PhoneUniquenessIntegrationTest`.

Điểm cần giải thích: biểu thức cũ có thể chấp nhận dấu `|` do hiểu sai character class. Quy tắc dùng chung tránh form và service xử lý khác nhau; unique constraint bảo vệ khi hai request cùng kiểm tra số chưa tồn tại. Bắt lỗi thân thiện ở service không thay được ràng buộc database.

Tự luyện: chỉ ra một số hợp lệ, một số sai, một số có dấu `|`; giải thích test nào kiểm tra cú pháp và test nào kiểm tra cạnh tranh/uniqueness. Mở diff thật bằng `git log --oneline -- src/main/java/com/medclinic/util/PhoneNumbers.java` rồi `git show <SHA> -- <file>`; không dựng commit minh họa.

### B. Google: xác thực nhà cung cấp xong vẫn phải chờ OTP của ứng dụng

Mở `src/main/java/com/medclinic/config/GoogleLoginConfig.java`, `AuthController`, `GoogleAccountService` và `GoogleCallbackProtocolIntegrationTest`.

Điểm cần giải thích: callback Google kiểm tra OIDC và chọn tài khoản bệnh nhân, nhưng nếu lưu SecurityContext ngay thì request tiếp theo có thể vào portal trước OTP. Luồng hiện tại giữ challenge trong phiên và chỉ tạo session sau khi xác nhận hợp lệ. Google subject là định danh liên kết ổn định; full name dùng để hiển thị. Tài khoản nhân viên không được tự liên kết thành bệnh nhân hoặc nâng quyền.

Tự luyện: đọc test callback hợp lệ vẫn chưa truy cập portal; sai nonce/chữ ký thất bại; đổi challenge sang phiên khác thất bại; resend vô hiệu mã cũ. Chỉ đúng tên test có trong source, đừng nói Google chỉ trả email là đủ để đăng nhập.

## 4. Công việc cá nhân và phương pháp phát triển

Trình bày theo các giai đoạn có tài liệu/code: xác định yêu cầu bốn vai trò → mô hình dữ liệu và trạng thái → luồng đặt/tiếp nhận/khám/thu → sửa lỗi qua review và regression → UI y khoa → Google/OTP → đồng bộ tài liệu và nghiệm thu. Đây là cấu trúc giải thích công việc, không phải bảng giờ làm hoặc bằng chứng mọi giai đoạn được thực hiện đúng thứ tự.

Tài liệu truy vết: [SRS](01_SRS_IEEE830.md), [UML](02_UML_DESIGN.md), [CSDL](03_DATABASE_DESIGN_3NF.md), [RTM](04_RTM_MATRIX.md), [kiến trúc](05_SYSTEM_ARCHITECTURE.md). Khi được hỏi phương pháp, mở một yêu cầu, tìm service và test tương ứng trong RTM. Giải thích thay đổi từ review dựa trên commit và test có thật.

## 5. Rehearsal một ca trong 15 phút

1. Mở môi trường đã thử; dùng dữ liệu giả. Ghi commit hiện tại và trạng thái health. Không build/reset database tại buổi bảo vệ.
2. Bệnh nhân `patient`: chọn **Nguyễn Văn Hùng** (tài khoản `doctor`), ngày mai, slot trống; ghi mã hẹn thực tế. Nhắc dev/demo cho xử lý sớm +14 ngày; môi trường ngoài demo yêu cầu ngày khám hôm nay.
3. Lễ tân `staff`: logout/login, tra đúng mã và cấp STT. QR chỉ hỗ trợ tìm ca. Muốn giữ hai vai trò để xem realtime, dùng hai profile/browser đã thử.
4. Bác sĩ `doctor`: bắt đầu khám, nhập chẩn đoán/lời dặn, chọn thuốc còn kho, hoàn tất. Mở **Bệnh án đã hoàn tất** và đọc lại ca vừa lưu. Giải thích đọc hồ sơ không trừ kho hoặc thu tiền lại.
5. Lễ tân: mở hóa đơn, đối chiếu phí khám + số lượng × giá kê đơn, thanh toán tiền mặt một lần. QR chuyển khoản chỉ trình bày nếu module đã cấu hình và bạn hiểu giới hạn.
6. Bệnh nhân: đọc lịch sử, tải PDF. Admin: xem biến động kho và thống kê; không sửa giá hoặc xóa dữ liệu của ca đang demo.
7. Mở service transaction và hai ví dụ AI ở mục 3; trình bày test, executed/skipped và CI của đúng SHA. Có thể dùng báo cáo đã chạy, không đợi Maven trong 15 phút.

Google/Gmail dùng tài khoản của bạn để thử trước buổi; tránh hiện secret, nội dung thư riêng hoặc hồ sơ thật khi chia sẻ màn hình. Thành công do bạn xác nhận trước đây chưa thay thế việc thử lại ngay trước bảo vệ. Có thể rút demo Google còn một ảnh/luồng giải thích nếu thời lượng hạn chế.

### Các câu hỏi phản biện nên tự trả lời được

- Hai người đặt cùng slot: khóa được lấy ở đâu, request đến sau kiểm tra điều gì?
- Thiếu thuốc ở dòng thứ hai: vì sao không lưu một nửa bệnh án hoặc trừ một nửa kho?
- Đổi giá thuốc hôm nay: giá đơn thuốc cũ lấy từ đâu? Tên/đơn vị thuốc có snapshot không?
- Đổi ID trên URL bệnh án: chỗ nào kiểm tra bác sĩ phụ trách, chỗ nào kiểm tra role?
- Google callback xong: vì sao chưa được coi là đăng nhập hoàn tất?
- Commit code chạy trên máy và commit CI có phải cùng SHA? Những test nào bị skip?
- Đồ án cá nhân: AI hỗ trợ phần nào, bạn đã tự đọc/sửa/thử phần nào?

## 6. Nghiệm thu kỹ thuật và việc còn cần bạn xác nhận

Kết quả tự động local ngày 12/10/2026:

| Kiểm tra | Kết quả | Phạm vi |
|---|---|---|
| `mvn clean verify` | 332 reported; 282 executed/pass; 50 skipped; 0 failure/error; coverage gate PASS | Test unit/H2/MockMvc; các test SQL có điều kiện không được tính là đã chạy |
| `SqlServerDoctorCompletedRecordIntegrationTest` | 4/4 executed/pass | Database mới `medclinic_records_it`, SQL Server 2022; phân quyền, hồ sơ quá khứ, giá snapshot, phân trang và offset lớn; database test đã dọn |
| JaCoCo local sau verify + test SQL trên | Line 88,89%; branch 72,27% | Báo cáo local; CI có phạm vi khác nên không hứa cùng tỷ lệ |
| Node regression | 26/26 pass | `node --test src/test/frontend/ui-regressions.cjs` |
| Playwright Edge headless | 176/176 pass | Bốn vai trò; desktop/tablet/mobile, sáng/tối; fixture H2 đi qua đặt → tiếp nhận → kê thuốc → đọc bệnh án → thanh toán tiền mặt → PDF; thư vào Mailpit |
| Link tài liệu | 374 local targets hợp lệ | `node ops/check-doc-links.cjs` |
| Docker app local | Health `UP` sau cập nhật | Giữ nguyên cấu hình local và volume dữ liệu; không thực hiện giao dịch ngân hàng thật |

Lượt browser đầu phát hiện lịch trực tràn ngang ở tablet và script cũ chờ lỗi server dù trình duyệt đã chặn số điện thoại trống. Đã sửa breakpoint/header lịch trực và kiểm tra validation đúng hành vi; script thanh toán chuyển sang bộ lọc `PAID` để kiểm tra hóa đơn đã trả. Lượt xác nhận 176/176 không còn lỗi.

Sau review giao diện, ba đoạn triệu chứng/chẩn đoán/lời dặn được giới hạn chiều dài dòng `70ch`, vẫn giữ xuống dòng và ngắt từ. Lượt xác nhận với lời dặn giả dài chạy lại 39/39 kiểm tra chuẩn bị/clinical và 4/4 test bệnh án MockMvc, đồng thời chụp lại desktop/mobile ở hai theme.

Test bảo mật bệnh án kiểm tra bác sĩ khác, bệnh nhân/lễ tân/admin bị từ chối; nội dung `<script>` được escape; đọc không đổi tồn kho, số hóa đơn, inventory transactions hoặc trạng thái ca. Đây là kiểm chứng kỹ thuật, chưa xác nhận bạn đã diễn đạt được các cơ chế đó khi bảo vệ.

Chạy lại kiểm thử trình duyệt khi app và Mailpit local đã sẵn sàng bằng `./ops/run-playwright-check.ps1 -WithFixture`. Script chỉ dùng fixture H2 cho thao tác nghiệp vụ rồi dọn fixture; public/portal matrix dùng app local. Không dùng dữ liệu thật để tạo chứng cứ. Log, HTML/screenshot thử nghiệm và `.env`, OAuth/SMTP, database, uploads được giữ ngoài Git. CI của commit xuất bản cần đối chiếu trực tiếp trên GitHub Actions.

- [ ] Đối chiếu đề cương để xác nhận từng nội dung ngoài chương trình.
- [ ] Giảng viên xác nhận cách chấm tiêu chí phân công/cộng tác cho đồ án cá nhân.
- [ ] Bạn tự đi hết rehearsal và ghi thời lượng thực tế.
- [ ] Bạn tự giải thích và thao tác hai tình huống code ở mục 3.
- [ ] Điền tên, MSSV và link repo thật vào bản trình bày khi cần.

Các mục trên không được đánh dấu hoàn tất chỉ bởi việc bổ sung code/tài liệu. Mục tiêu là giúp bạn có bằng chứng rõ và trình bày chắc; điểm do giảng viên quyết định.
