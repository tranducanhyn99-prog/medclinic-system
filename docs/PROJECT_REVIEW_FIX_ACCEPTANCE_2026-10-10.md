# Nghiệm thu sửa review MedClinic — 10/10/2026

Chín task từ [review ban đầu](PROJECT_REVIEW_2026-10-10.md) đã có thay đổi và regression test trong working tree. Google login được hoãn theo yêu cầu; không có cấu hình hay nút Google trong bản chạy này. Chưa commit/push/deploy các thay đổi.

Suite mặc định đã pass; SQL Server được chạy riêng để kiểm chứng migration và các tình huống tranh chấp trên database có thể hủy. Bảng dưới ghi tách số pass và skip của từng lượt.

## Thay đổi và kiểm chứng từng task

| Task | Hành vi sau sửa | Kiểm chứng |
| --- | --- | --- |
| RV-01 | Mỗi request của principal đăng nhập kiểm tra active và `securityVersion` trong DB. Khi khóa, tăng version; session cũ bị hủy ở request tiếp theo, cookie remember-me bị xóa. Mở khóa vẫn yêu cầu login mới. Socket đang mở kiểm tra trước khi gửi thêm dữ liệu riêng. | [LockedAccountSessionIntegrationTest](../src/test/java/com/medclinic/LockedAccountSessionIntegrationTest.java): hai session, khóa/mở khóa từ admin, GET/POST bị chặn, password không đổi, login mới hoạt động. [ActiveAccountFilterTest](../src/test/java/com/medclinic/ActiveAccountFilterTest.java): remember-me và private socket push. |
| RV-02 | Một số điện thoại cho một tài khoản; chuẩn hóa trim, profile cho phép null. Registration, profile, doctor và walk-in dùng chung chính sách. V7 thêm filtered unique index và CHECK trên SQL Server; constraint race được chuyển thành lỗi nghiệp vụ. | [PhoneUniquenessIntegrationTest](../src/test/java/com/medclinic/PhoneUniquenessIntegrationTest.java), bản [SQL Server](../src/test/java/com/medclinic/SqlServerPhoneUniquenessIntegrationTest.java): request tranh cùng số có một người thắng, không tạo hai hồ sơ, nhiều null hợp lệ. |
| RV-03 | GET OTP, POST verify và resend chỉ nhận challenge của session đã tạo. Session lạ bị từ chối trước khi đọc challenge/tăng attempts. Magic link vẫn dùng token riêng để mở từ email. | [OtpAuthenticationIntegrationTest](../src/test/java/com/medclinic/OtpAuthenticationIntegrationTest.java): session đúng, session lạ/không có session, attempts không đổi, resend và magic link. |
| RV-04 | Kiểm tra nội dung thực và giải mã JPG/PNG/WebP; từ chối MIME giả, ảnh bị cắt, quá 5 MB, 16 triệu pixel hoặc chiều 8192. WebP hỗ trợ ảnh tĩnh qua ImageIO/TwelveMonkeys. Ảnh cũ chỉ bị thay sau khi lưu thành công. | [AvatarStorageServiceImplTest](../src/test/java/com/medclinic/AvatarStorageServiceImplTest.java): ảnh thật ba định dạng và đầu vào sai; [ProfileManagementIntegrationTest](../src/test/java/com/medclinic/ProfileManagementIntegrationTest.java): upload HTTP lỗi giữ URL/file cũ. |
| RV-05 | DTO/service và invoice/QR chấp nhận VND nguyên, không âm, tối đa 9.999.999.999; `1000.00` hợp lệ. Tổng vượt miền lưu trữ bị từ chối trong transaction. V9 kiểm tra catalog/giá nhập kho cũ, giữ nguyên hóa đơn lịch sử. | [PhoneAndVndValidationTest](../src/test/java/com/medclinic/PhoneAndVndValidationTest.java), service tests; [BankPaymentConcurrencyIntegrationTest](../src/test/java/com/medclinic/BankPaymentConcurrencyIntegrationTest.java) và bản SQL Server: thuốc → đơn → invoice → QR; overflow rollback hồ sơ, stock và appointment. |
| RV-06 | Quy tắc dùng chung `^0[35789][0-9]{8}$`; form và backend cùng từ chối dấu `|`, chữ và sai độ dài. | DTO/service và HTTP trong phone tests; đầu số 03/05/07/08/09 hợp lệ. |
| RV-07 | Các đường ghi thanh toán khóa Invoice → Payment. Callback lặp, cash và đối soát không thanh toán hai lần; signature/amount/reference vẫn được kiểm tra. | [BankTransferPaymentServiceTest](../src/test/java/com/medclinic/BankTransferPaymentServiceTest.java): thứ tự khóa và validation; [SqlServerBankPaymentConcurrencyIntegrationTest](../src/test/java/com/medclinic/SqlServerBankPaymentConcurrencyIntegrationTest.java): regenerate với manual-confirm, callback lặp, callback với cash; tất cả hoàn tất trong timeout. |
| RV-08 | Thu ngân lọc status/ngày và phân trang tại DB, mặc định UNPAID/25 dòng, tối đa 100. Giữ filter/page/size qua cash, tạo QR, màn hình QR và xác nhận chuyển khoản. Trang vượt phạm vi được đưa về trang còn dữ liệu. | [BillingPaginationIntegrationTest](../src/test/java/com/medclinic/BillingPaginationIntegrationTest.java): fixture 10.000 invoice, 7.500 UNPAID, 25 dòng trả về; filter/page biên, cash/bank HTTP và HTML. Form dùng layout responsive hiện có; chưa nghiệm thu trực quan trên browser desktop/mobile. |
| RV-09 | Dashboard, lịch bác sĩ, seed và prefix hóa đơn dùng business Clock Việt Nam. Seed đủ hôm nay đến +14 gồm hai ca mỗi ngày; test fixture tái sử dụng lịch đã seed. | [BusinessClockConsistencyTest](../src/test/java/com/medclinic/BusinessClockConsistencyTest.java): Clock cố định hai phía mốc đổi ngày UTC/Việt Nam; [DataInitializerBootstrapTest](../src/test/java/com/medclinic/DataInitializerBootstrapTest.java): sáu bác sĩ seed có đủ ca ngày +14; regression booking/email/cấp mã. |

## Kiểm chứng tổng

| Lượt | Kết quả |
| --- | --- |
| `mvn --batch-mode --no-transfer-progress clean verify` | **258 pass**, 37 SQL Server skipped, 0 failure/error; 295 test báo cáo. JAR được đóng gói; coverage gate đạt. |
| JaCoCo LINE | **87,76%**, 2.288 covered / 2.607 tổng; gate ≥70%. |
| JaCoCo BRANCH | **71,37%**, 850 covered / 1.191 tổng; gate ≥50%. |
| SQL Server tổng | **28/28 pass**, `medclinic_e2e`: fresh/legacy/preflight migration, booking, stock, rollback, cấp mã, EntityGraph, rate limit, performance baseline và release E2E. |
| SQL Server điện thoại | **4/4 pass**, `medclinic_phone_it`, migration V1–V9 + Hibernate validate. |
| SQL Server thanh toán | **5/5 pass**, `medclinic_payments_it`: ba ca concurrency và hai ca pipeline/overflow. |
| Frontend regression | **26/26 pass**, 0 skipped/fail. |
| Doc links / diff / package | **350 local targets hợp lệ**, `git diff --check` pass; JAR chứa đủ chín migration và không chứa bản nháp Google. |

Coverage lấy từ suite `clean verify` mặc định trước lượt SQL Server riêng; không cộng số skip vào pass. SQL chạy tuần tự trên container SQL Server 2022 CU27 riêng, engine 16.0.4295.3, cổng localhost 14334. Database demo đang chạy không được dùng làm fixture và không bị xóa. Migration test tự tạo/drop `medclinic_flyway_it` trong container kiểm thử này.

Ba lượt SQL có **37/37 pass**, 0 failure/error/skipped. Cộng theo test riêng biệt: 258 Java mặc định + 37 SQL Server = **295 test Java thực sự chạy và pass**; frontend ghi riêng 26 test. Container SQL dùng cho lượt này đã được dừng sau kiểm chứng.

Đo thu ngân ở lượt tổng: query đầu trả 25/7.500 hóa đơn UNPAID từ fixture 10.000 invoice trong **77 ms** trên H2 local. Đây là một phép đo query, chưa có percentile/load test hay browser latency; không suy ra SLA production.

Kết quả công khai được lưu trong [verification-summary.json](evidence/project-review-fixes-2026-10-10/verification-summary.json). Log thô và cấu hình database test chỉ ở thư mục `output` bị ignore.

## Khi nâng cấp database

V1–V6 giữ nguyên; V7–V9 là migration mới. Fresh DB và legacy baseline cần áp dụng đủ chín version rồi Hibernate validate; số bảng ứng dụng vẫn là 21. Xem [SQL Server runbook](database/SQLSERVER_RUNBOOK.md).

V7 cố ý dừng nếu điện thoại cũ trùng/rỗng/sai định dạng. V9 cố ý dừng nếu giá catalog hoặc giá nhập kho âm/có phần lẻ. Backup, rà dữ liệu và thống nhất cách sửa trước nâng cấp. Không có bước tự xóa/gộp bệnh nhân hoặc làm tròn tiền; hóa đơn đã phát hành giữ dữ liệu lịch sử. Test preflight kiểm tra migration thất bại giữ nguyên các giá trị cũ.

## Phạm vi chưa kiểm tra

Chưa chạy browser E2E của form phân trang/socket, camera QR thật, SMTP thật, chuyển tiền ngân hàng thật, load test production hay deploy lên môi trường hiện có. HTTP/HTML, security filter/socket unit, service, DB constraints và các race SQL Server đã có kiểm chứng riêng. Rehearsal buổi bảo vệ theo [kịch bản cập nhật](PRESENTATION_SCRIPT_2026-10-10.md).

Google OAuth để sau theo yêu cầu; bản nháp nằm trong thư mục `output` bị ignore, không nằm trong source/JAR của tính năng chạy hiện tại.
