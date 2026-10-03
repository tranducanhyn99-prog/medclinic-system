# Đối chiếu và hoàn tất backlog — 02/10/2026

Đã đối chiếu 22 mục có ID trong [backlog](RESOLUTION_BACKLOG_2026-10-01.md) với implementation, bộ test hiện tại và bằng chứng nghiệm thu local. Lượt cuối phát hiện thêm các trường hợp P2-01 chưa được bao phủ: độ dài text vượt schema, dấu phẩy bị tách khi bind một dòng thuốc và tràn số nguyên khi cộng số lượng thuốc lặp. Các trường hợp này đã sửa và nghiệm thu lại. Toàn bộ mục local trong backlog đạt; kết quả này không xác nhận môi trường production hoặc dịch vụ nhà cung cấp thật.

## Sửa bổ sung ở P2-01

- Trước sửa, DTO cho phép dosage/frequency 255 ký tự và instructions 1000, trong khi Flyway/entity chỉ cho 100/100/255. HTTP nhập quá dài trả 500. Giới hạn dùng chung tại `PrescriptionValidation` nay được áp dụng ở DTO, entity và service; input browser có `maxlength=100`. Schema dữ liệu không thay đổi.
- Spring mặc định tách dấu phẩy khi bind một giá trị vào `List<String>`. Binder riêng giữ mỗi parameter text thành một dòng thuốc; không tách câu “Sáng, tối”. Giá trị trống ở một dòng không làm lệch các dòng tiếp theo.
- Service kiểm tra đủ dữ liệu, ID/số lượng dương, tối đa 100 dòng và độ dài text trước thao tác kho. Tổng số lượng vượt `Integer.MAX_VALUE` trả lỗi nghiệp vụ vào cùng form, thay cho `ArithmeticException`/500.
- [Regression trước sửa — bằng chứng local, xem tóm tắt](evidence/README.md): 3/3 thất bại với các lỗi trên. [Regression sau sửa ban đầu — bằng chứng local, xem tóm tắt](evidence/README.md): 32/32 đạt. Sau đó bổ sung kiểm tra nhiều dòng và HTTP overflow vào lượt full clean bên dưới.

## Kiểm thử cuối

`mvn -B -o '-Dmaven.repo.local=C:/Users/DUCANHZZ/.m2/repository' clean verify`, với `RUN_SQLSERVER_IT=true`, URL SQL Server trỏ database riêng `medclinic_e2e`; hoàn tất **09:06:56 +07** ngày 02/10/2026.

- **194 tests / 67 classes; 0 failures, 0 errors, 0 skipped.**
- JaCoCo LINE: **2028 covered / 382 missed = 84,15%**; BRANCH: **659 covered / 320 missed = 67,31%**. Gate 70%/50% đạt.
- `node --test src/test/frontend/ui-regressions.cjs`: **9/9 PASS**, không skipped.
- 4 regression mới chạy trên mỗi database H2/SQL Server, tăng 186 lên 194 tests. Các case SQL Server trước đây bị skip vẫn chạy đầy đủ.
- [Maven clean verify — bằng chứng local, xem tóm tắt](evidence/README.md), [JS — bằng chứng local, xem tóm tắt](evidence/README.md), [số liệu và tên từng test](evidence/backlog-final-2026-10-02/verification-summary.json).

## Đối chiếu từng yêu cầu

| ID | Trạng thái | Bằng chứng/implementation |
|---|---|---|
| P1-01 | PASS | `BankTransferCallbackDto`, `BankTransferPaymentServiceImpl`; acceptance HTTP generated/legacy code, HMAC sai 401, amount sai 422, đúng 200, retry duy nhất; H2 + SQL Server. |
| P1-02 | PASS | `AuthVerificationServiceImpl.consume`, `AuthController.establishSession`, admin invalidate challenge; acceptance khóa OTP/magic không tạo session, khóa/mở lại vẫn vô hiệu token; đăng ký kích hoạt qua SMTP browser phiên trước. |
| P1-03 | PASS | Clinical DTO/service validation; acceptance input trắng giữ form, không đổi ca khám/bệnh án/hóa đơn/kho; assertion mở rộng không ghi inventory transaction khi lỗi. |
| P1-04 | PASS, camera thật | `SecurityHardeningIntegrationTest`: camera self chỉ màn tiếp nhận; webcam đã được người dùng xác nhận tự quét, tìm và check-in đúng lịch hẹn. [Ảnh camera — bằng chứng local, xem tóm tắt](evidence/README.md). Không cần lặp thao tác thiết bị khi source camera không đổi. |
| P1-05 | PASS | Guard document tôn trọng event bị hủy; 2 JS regression về thứ tự listener; browser hủy xác nhận hai lần đã đạt đợt UI. Form sau lỗi server ở lượt này cũng gửi lại được. |
| P2-01 | PASS, tái nghiệm thu | 16 acceptance cases trên mỗi database gồm list thiếu/lệch, text 101/256 bị chặn, biên 100/255 lưu đủ, dấu phẩy một/nhiều dòng, optional blank, service input sai và overflow; browser/SQL bên dưới. |
| P2-02 | PASS | `DoctorAvailability`, lọc public doctor/specialty, API và booking transaction; tests doctor/specialty inactive trả detail 404, slots rỗng, booking service từ chối, slot không đổi. |
| P2-03 | PASS | HTML advice status, REST advice JSON và message công khai; `BackendAcceptanceIntegrationTest`, `ApiErrorContractTest`, `GlobalExceptionHandlerTest`, security integration. Unexpected API failure 500 không lộ thông tin nội bộ. |
| P2-04 | PASS | `GlobalExceptionHandler.safeReferer`; test ngoại origin, protocol-relative, encoded slash, backslash, scheme không hợp lệ; cùng origin chuyển path nội bộ, fallback `/`. |
| P2-05 | PASS | Route doctor portrait public tách khỏi private patient avatar; 5 controller cases, security anonymous private route bị chặn, browser ảnh đã đạt đợt UI. |
| CHECK-01 | PASS | `slot-loader.js`: abort/sequence; JS response cũ không ghi đè, clear specialty vô hiệu request đang chờ. |
| CHECK-02 | PASS | Booking/schedule dùng business clock, loại slot đã bắt đầu và giới hạn ngày 14; `BookingTimeBoundaryTest`, service tests và acceptance trực tiếp/API. |
| CHECK-03 | PASS | Reset repository pessimistic lock + transaction; hai request đồng thời chỉ một consume trên H2/SQL Server; đăng ký/đổi/reset cùng giới hạn 8–72. |
| CHECK-04 | PASS | SW không cache navigation/API/private avatar; fallback offline trung lập, purge cache cũ của chính MedClinic; 2 JS regression, offline public/security. |
| CHECK-05 | PASS | Java default TZ + `BusinessTimeConfig` + Docker TZ Việt Nam; clock cố định qua nửa đêm; runtime app cập nhật vẫn +07. |
| CHECK-06 | PASS, SMTP local | Mailpit phiên trước: đăng ký, OTP sai/đúng, email activation và magic login qua SMTP. [OTP — bằng chứng local, xem tóm tắt](evidence/README.md), [magic — bằng chứng local, xem tóm tắt](evidence/README.md); dev email không đại diện cho SMTP. |
| VERIFY-01 | PASS | 194/194 clean verify, coverage gate và JS 9/9; logs/JSON cuối liên kết phía trên. |
| VERIFY-02 | PASS | Các class SQL Server chạy 0 skipped; 9 case cũ + 16 backend acceptance trên database test riêng; tên case từng suite trong JSON. |
| VERIFY-03 | PASS, browser local | Phiên trước: OTP mobile 390px, booking, webcam/tiếp nhận, khám, thu tiền DEMO, PDF; lượt này kiểm tra lại lỗi rồi lưu đơn thuốc. Mẫu PDF mới vẫn nằm trong bộ regression hiện tại. |
| VERIFY-04 | PASS | COPY_ONLY/CHECKSUM/VERIFYONLY, restore DB tạm + DBCC + đối chiếu dữ liệu; uploads PNG fixture restore có checksum trùng. [Restore content — bằng chứng local, xem tóm tắt](evidence/README.md), [restore uploads — bằng chứng local, xem tóm tắt](evidence/README.md). Runbook không đổi ở lượt này. |
| DOC-01 | PASS | README/backlog cập nhật 194 tests và coverage/môi trường; tách các mốc 153/182/186 trước đó khỏi kết quả hiện tại. |
| DOC-02 | PASS | `git diff --check`; source mới và các tài liệu nghiệm thu cuối không có trailing whitespace. Giữ hard break Markdown có chủ ý trong đặc tả Figma có sẵn. [Kết quả — bằng chứng local, xem tóm tắt](evidence/README.md). |

19 nhóm UI bổ sung giữ kết quả [báo cáo Impeccable](IMPECCABLE_FIXES_2026-10-01.md); các mục trùng đã ánh xạ ở backlog. Layout/render/security tests và 9 JS regression vẫn đạt trên source hiện tại. Các giới hạn screen reader/Core Web Vitals và production trong báo cáo UI được giữ rõ; không tuyên bố đã chạy lại mọi kiểm tra thiết bị hay Lighthouse.

## Browser và SQL cho bản sửa cuối

Ứng dụng được build Docker và cập nhật riêng container app ở **http://127.0.0.1:18087**; giữ SQL Server, Mailpit và volume uploads. Image: `sha256:b2b34fa3db8b0c27af121e0b60f4ddfc4135a0fe068099f3c9789cc15dd770e8`. [Docker build — bằng chứng local, xem tóm tắt](evidence/README.md).

Ca vãng lai DEMO mới `MED-261002-526A4F99` / STT-02:

1. Gửi 2 dòng cùng thuốc, số lượng `2147483647` và `1`: hiển thị lỗi “Tổng số lượng của một thuốc vượt giới hạn cho phép.”, giữ đầy đủ clinical fields và text có dấu phẩy. [Ảnh lỗi](evidence/backlog-final-2026-10-02/examination-error.png), [giá trị form](evidence/backlog-final-2026-10-02/preserved-form.json).
2. Snapshot SQL trước và sau lỗi giống nhau: IN_PROGRESS, 0 bệnh án, 0 hóa đơn, 0 inventory entries, stock Paracetamol 1998. [Trước — bằng chứng local, xem tóm tắt](evidence/README.md), [sau — bằng chứng local, xem tóm tắt](evidence/README.md), [đối chiếu](evidence/backlog-final-2026-10-02/rejected-prescription-result.txt).
3. Đổi số lượng dòng đầu về 1 và lưu: COMPLETED, một bệnh án, một hóa đơn UNPAID 353000, một inventory transaction xuất 2 viên, stock 1996. Hai dosage/frequency có dấu phẩy giữ nguyên; instructions trống. [SQL assertions](evidence/backlog-final-2026-10-02/browser-success-check.sql), [kết quả — bằng chứng local, xem tóm tắt](evidence/README.md), [ảnh thành công](evidence/backlog-final-2026-10-02/examination-success.png).

Dữ liệu trên là fixture local. Hóa đơn mới được để UNPAID; phiên trước đã nghiệm thu thu tiền DEMO. Không phát sinh thanh toán bên ngoài.

## Phạm vi chưa nghiệm thu

Ngân hàng và SMTP nhà cung cấp thật, HTTPS/cookie secure production, GitHub Actions cho working tree hiện tại và đánh giá CVE hiện hành chưa được kiểm chứng. Đây là các giới hạn đã ghi từ báo cáo ban đầu, không bị đổi thành PASS. Các điều kiện hoàn thành có ID trong backlog dùng HTTP callback test, localhost camera, Mailpit và kiểm tra local đã đạt. Source còn trong working tree, chưa commit hoặc triển khai ra bên ngoài.
