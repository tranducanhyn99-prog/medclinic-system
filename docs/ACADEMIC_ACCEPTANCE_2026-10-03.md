# Nghiệm thu đồ án MedClinic — 03/10/2026

**Kết luận:** hoàn thành 5 mục còn lại đã thống nhất; bản local đạt nghiệm thu trong phạm vi đồ án môn học. Không yêu cầu SMTP/ngân hàng thật hoặc staging để chốt phạm vi này.

**Source:** working tree nhánh `codex/completion-runtime-ci`, nền `fa56618` trên `origin/main`. Các thay đổi của lượt này chưa commit/push và chưa có lượt hosted CI riêng. Kết quả dưới đây được chạy trực tiếp trên source và image Docker đã build từ working tree này.

## 1. Tasklist đã hoàn thành

| Mục | Kết quả và bằng chứng |
|---|---|
| UI hoạt động không cần CDN | Bootstrap, icons, SweetAlert, Geist, html5-qrcode, SockJS và STOMP được đóng gói local, giữ license và manifest SHA-256. Chart.js đã local. Cho phép anonymous đọc đúng `/vendor/ui/**`; test vẫn kiểm tra route nghiệp vụ được bảo vệ. Browser không còn request CSS/font/JS thiết yếu ra CDN. |
| Hướng dẫn demo email/OTP rõ ràng | README và demo package phân biệt H2 `dev` (không gửi mail, dùng tài khoản seed) với Docker SQL Server + Mailpit (đăng ký/OTP/email đầy đủ). `MAIL_FROM` có cấu hình riêng. |
| Email đặt lịch có QR như SRS | Email MIME chứa PNG inline `cid:booking-qr`, mã lịch hẹn bằng chữ dự phòng và nội dung HTML được escape. Test giải mã PNG thành đúng booking code; Mailpit nhận đúng MIME thực tế. |
| RTM và tài liệu khớp source | 33 yêu cầu chức năng được đối chiếu với class/method test hiện có; bỏ mã TC dự kiến khỏi bảng RTM. Giữ các báo cáo cũ là lịch sử, thêm báo cáo hiện tại và lệnh tái chạy. |
| Baseline hiệu năng có bằng chứng | Đo đủ body HTML trên 6 trang của Docker SQL Server; mỗi trang 2 warm-up + 30 mẫu, p95 dưới 1,5 giây, 0 lỗi HTTP/client. Phạm vi và dữ liệu đo được ghi rõ bên dưới. |

Lần nghiệm thu còn phát hiện và sửa hai lỗi chức năng: lịch sử bệnh án có dữ liệu bị `LazyInitializationException` vì thiếu `doctor.specialty` trong EntityGraph; đơn thuốc PDF bỏ sót tần suất và lời dặn đã lưu ở bệnh án. Có regression H2/SQL Server cho lịch sử và test nội dung PDF cho tần suất, hướng dẫn thêm, lời dặn dự phòng và lời dặn riêng của đơn thuốc.

## 2. Kết quả kiểm thử cuối

| Kiểm tra | Kết quả |
|---|---|
| `mvn -B --no-transfer-progress clean verify` với SQL guard bật | **218/218 test**, 73 suite; 0 failure, 0 error, 0 skipped; BUILD SUCCESS |
| SQL Server trong tổng trên | **26 integration test** thực sự kết nối SQL Server, cộng 6 test artifact/schema; gồm migration fresh/legacy V1–V6, concurrency, rollback, rate limit, patient history và workflow E2E/PDF |
| JaCoCo | LINE **84,73%**, BRANCH **67,64%**; vượt gate 70%/50% |
| Frontend Node | **25/25 test**; gồm state/keyboard/offline/session và kiểm tra mọi template giải được tài nguyên local |
| Runtime SQL Server + Mailpit | Đăng nhập 4 vai trò, bảo vệ anonymous, 6 trang HTTP 200, **25 tài nguyên local** trả đúng nội dung/MIME |
| Workflow H2 cô lập + SMTP Mailpit thật | Đăng ký → OTP → đặt lịch → email QR → tiếp nhận → khám → lưu bệnh án/đơn thuốc/hóa đơn → đọc lịch sử → tải PDF; email reset và hủy lịch đều nhận được |
| PDF tải qua HTTP từ image cuối | Hóa đơn 1 trang, các số **350 000**, **3 000**, **353 000**, trạng thái chưa thanh toán; đơn thuốc 1 trang có liều dùng, cách dùng và lời dặn đã nhập; kiểm tra text và render ảnh đều đạt |
| Browser desktop/mobile | Trang chủ, đặt lịch, tiếp nhận ở 390 px và desktop; doctor/admin desktop; dark mode đọc được giờ, nút giờ cao khoảng 44 px; không tràn ngang ở các mẫu đã xem; doctor báo đang nhận cập nhật realtime |
| Persistence lần này | File probe trong volume uploads được giữ qua restart và đã xóa sau kiểm tra; không diễn giải probe này thành một lần upload avatar qua UI |
| Kiểm tra bàn giao source | 493 file tracked/non-ignored được đối chiếu với secret local trong `.env`, 0 giá trị secret bị phát hiện; 43 tên class test trong RTM giải được đến source; SHA-256 asset manifest đúng; `git diff --check` đạt; 224 đích link tài liệu local hợp lệ |

Workflow ghi dữ liệu chỉ chạy trong container H2 tạm `medclinic-academic-fixture` trên port **18090**, không ghi thêm ca khám vào `medclinic_db` của Compose. Lượt cuối tạo 1 bệnh nhân giả, 2 lịch hẹn (1 khám và 1 hủy), 1 bệnh án, 1 hóa đơn **chưa thanh toán**; không xác nhận thu tiền hoặc gọi ngân hàng thật. Email đi vào Mailpit local. Container fixture được dọn sau nghiệm thu; database/volume của Compose được giữ nguyên.

Bằng chứng đã chọn: [tổng hợp test](evidence/academic-acceptance-2026-10-03/test-summary.json), [runtime SQL Server](evidence/academic-acceptance-2026-10-03/runtime-sql.json), [workflow fixture](evidence/academic-acceptance-2026-10-03/runtime-fixture.json), [PDF](evidence/academic-acceptance-2026-10-03/pdf-summary.json), [browser](evidence/academic-acceptance-2026-10-03/ui-summary.json).

Ảnh: [đặt lịch mobile dark](evidence/academic-acceptance-2026-10-03/booking-mobile-dark.png), [tiếp nhận mobile dark](evidence/academic-acceptance-2026-10-03/reception-mobile-dark.png), [bàn khám](evidence/academic-acceptance-2026-10-03/doctor-desktop-dark.png), [quản trị](evidence/academic-acceptance-2026-10-03/admin-desktop-dark.png).

## 3. Baseline local

Windows 10.0.26200, Docker Linux engine được cấp **12 CPU / 7 925 800 960 byte RAM**, SQL Server 2022, Spring profile `sqlserver,demo`, HTTP loopback `127.0.0.1:8080`, Node 22. Database demo nhỏ (1 bệnh nhân seed, 6 bác sĩ, 3 lịch hẹn lịch sử ở màn quản trị; hàng chờ ngày hiện tại trống). Source/image như đầu báo cáo. Trong lúc đo SQL, harness H2 riêng cũng chạy; mỗi harness gửi request tuần tự, đây không phải phép đo tải đồng thời có kiểm soát.

| Trang | p50 ms | p95 ms | Max ms | Lỗi |
|---|---:|---:|---:|---:|
| `/` | 79,31 | 136,06 | 166,40 | 0 |
| `/patient/booking` | 59,98 | 74,38 | 76,99 | 0 |
| `/staff/reception` | 27,86 | 35,56 | 37,50 | 0 |
| `/doctor/desk` | 33,98 | 38,65 | 68,03 | 0 |
| `/staff/billing` | 27,46 | 44,64 | 63,67 | 0 |
| `/admin/dashboard` | 38,50 | 77,10 | 89,38 | 0 |

180 mẫu đo (không tính warm-up). Percentile dùng nearest-rank: p50 mẫu thứ 15, p95 mẫu thứ 29 trong 30 mẫu đã sắp xếp. Chỉ đo thời gian HTTP đến khi đọc hết HTML; không đo LCP, render, tốc độ camera, dữ liệu lớn hoặc tải nhiều người dùng. PERF-01–04 đạt ngưỡng HTML trong môi trường này; PERF-05/06 ngân hàng chưa benchmark. Test callback mock/idempotency không thay thế nghiệm thu hoặc benchmark ngân hàng thật.

## 4. Tái chạy

Chạy môi trường demo theo README (`.env` riêng, không commit secret), rồi:

```powershell
docker compose up --build -d
node ops/academic-acceptance.cjs --url http://127.0.0.1:8080 --output output/academic-runtime-sql.json
node --test src/test/frontend/ui-regressions.cjs
node ops/check-doc-links.cjs
```

Workflow có ghi dữ liệu phải dùng fixture riêng và Mailpit đang chạy:

```powershell
.\ops\start-academic-fixture.ps1
node ops/academic-acceptance.cjs --url http://127.0.0.1:18090 --fixture --output output/academic-runtime-fixture.json
# Sau khi xem PDF/email, chỉ xóa container fixture do helper vừa tạo:
docker stop medclinic-academic-fixture
docker rm medclinic-academic-fixture
```

Helper chỉ thay container đã có label `medclinic.academic-fixture=disposable-h2` khi truyền `-Reset`; lệnh reset hủy dữ liệu H2 thử. Harness yêu cầu loopback HTTP và chỉ cho phép workflow ghi trên port 18090. Phải còn giờ khám khả dụng trong ngày; không bypass đồng hồ/nghiệp vụ để tạo ca đã quá giờ.

Để tái lập đủ 218 test, làm theo [SQL Server runbook](database/SQLSERVER_RUNBOOK.md): `DB_URL` trỏ **`medclinic_e2e` disposable**, khai báo credentials qua environment và `RUN_SQLSERVER_IT=true`, sau đó chạy `mvn -B --no-transfer-progress clean verify`. Không dùng database demo đang cần giữ; migration test còn tạo/drop `medclinic_flyway_it`. Lệnh Maven mặc định không bật SQL guard sẽ có test SQL bị skipped, không thể coi là cùng kết quả trên.

## 5. Giới hạn và roadmap tùy chọn

- Webcam thật đã được người dùng xác nhận quét tự động ở [lượt trước](BACKEND_ACCEPTANCE_2026-10-02.md). Lượt này kiểm tra thư viện camera local/UI, chưa lặp lại thao tác trước webcam thật.
- Screen reader, zoom 200%, giảm chuyển động và touch đã được người dùng xác nhận ở lượt mẫu UX trước; không tuyên bố đã kiểm tra lại mọi OS/trình duyệt hoặc chứng nhận WCAG.
- UI quản lý lịch làm việc cho admin và trang riêng để bác sĩ xem lại ca hoàn tất là cải tiến tùy chọn. Hiện ca đã hoàn tất được chuyển về bàn khám; lịch sử bệnh án bệnh nhân đã nghiệm thu với dữ liệu thật trong fixture.
- SMTP nhà cung cấp, adapter/callback ngân hàng thật, HTTPS staging/production và tải quy mô lớn là roadmap nếu triển khai thật, không phải hạng mục bắt buộc của đồ án đã chốt.

Raw log, PDF fixture, OTP/magic link, cookie và `.env` nằm local trong thư mục bị ignore. Ảnh công khai chỉ chứa tài khoản seed hoặc dữ liệu giả; không lưu khung hình webcam cá nhân.

## 6. Khôi phục ứng dụng sau nghiệm thu

Trong lượt build, bản Java H2 xem trước ở 18088 được dừng để tránh khóa file JAR. Sau khi khởi động lại, người dùng báo ứng dụng tắt; kiểm tra thấy cả tiến trình Java và Docker engine không còn chạy, log Java không có exception cuối tiến trình. Chưa xác định tác nhân đã dừng runtime; không kết luận đây là lỗi nghiệp vụ.

Đã khởi động lại Docker Desktop và Compose, giữ database/volume. URL xem trước `http://127.0.0.1:18088` được chuyển sang container `medclinic-preview-18088` dùng image đã nghiệm thu, profile `dev`, label `medclinic.preview=dev-18088`, restart policy `unless-stopped`, chỉ bind localhost. Cổng 8080 vẫn là bản SQL Server + Mailpit đầy đủ. Cả hai endpoint health phải `UP` trước bàn giao. Nếu Docker Desktop bị tắt thì container cũng dừng; H2 mất dữ liệu khi restart và phiên đăng nhập cũ cần đăng nhập lại.

Sau này mở Docker Desktop rồi dùng `docker start medclinic-preview-18088` cho bản xem trước, hoặc `docker compose up -d` cho bản đầy đủ. Container xem trước đang được dùng, không thuộc fixture H2 đã dọn ở mục 2.
