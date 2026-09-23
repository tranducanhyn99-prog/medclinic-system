# Kế hoạch baseline hiệu năng

**Trạng thái:** kế hoạch đo; không phải kết quả đạt NFR.<br>
**Mục tiêu liên quan:** `NFR-PERF-01` trong SRS đặt mục tiêu thời gian tải trang trung bình dưới 1,5 giây.

## Môi trường cần ghi nhận

Mỗi lần đo phải ghi commit/working-tree state, profile Spring, database (H2/MySQL/SQL Server), cấu hình máy, trình duyệt, mạng và dữ liệu seed. Không so sánh kết quả giữa các môi trường khác nhau như cùng một baseline.

## Kịch bản

| ID | URL / thao tác | Mẫu đo | Chỉ số | Ngưỡng mục tiêu |
|---|---|---:|---|---:|
| PERF-01 | `GET /` | 10 lần sau warm-up | p50, p95 tải tài liệu HTML | p95 < 1,5 giây |
| PERF-02 | `GET /patient/booking` khi đã đăng nhập | 10 lần sau warm-up | p50, p95 tải HTML | p95 < 1,5 giây |
| PERF-03 | `GET /staff/reception` với dữ liệu ngày hiện tại | 10 lần sau warm-up | p50, p95 tải HTML | p95 < 1,5 giây |
| PERF-04 | `GET /doctor/desk` với hàng chờ đại diện | 10 lần sau warm-up | p50, p95 tải HTML | p95 < 1,5 giây |
| PERF-05 | Tạo trang VietQR cho hóa đơn chưa thanh toán | 30 lần sau warm-up | p50, p95 server response; lỗi | p95 < 1,5 giây, 0 lỗi |
| PERF-06 | Callback payment hợp lệ + 10 lần retry cùng event id | 30 giao dịch | p50, p95; duplicate invoice update | p95 < 500 ms, mỗi invoice chỉ `PAID` một lần |

## Cách chạy đề xuất

1. Chạy build/test xanh trước khi đo; khởi động ứng dụng với profile và database đã ghi nhận.
2. Dùng trình duyệt DevTools hoặc công cụ HTTP có lưu raw result; chạy hai lượt warm-up, rồi 10 lượt đo tuần tự cho từng kịch bản.
3. Ghi kết quả vào bảng dưới, kèm lỗi HTTP và dữ liệu test. Chỉ công bố NFR-PERF-01 đạt khi tất cả kịch bản áp dụng cùng đạt p95.

| Ngày | Commit/state | Môi trường | Kịch bản | p50 | p95 | Lỗi | Người chạy | Kết luận |
|---|---|---|---|---:|---:|---|---|---|
| 17/09/2026 | Working tree chưa commit; Spring Boot 3.3.5, Java 21 | Windows local, profile `dev`, H2 in-memory, `curl.exe --http1.1`, 2 warm-up + 10 request tuần tự | PERF-01 `GET /` | 32,088 ms | 38,883 ms | HTTP 200 cho 10/10 mẫu, nhưng curl trả exit 18 (response kết thúc khi còn content) | Codex | Số đo thăm dò; không dùng nghiệm thu NFR |
| 17/09/2026 | Working tree chưa commit; Spring Boot 3.3.5, Java 21 | Windows local, profile `dev`, H2 in-memory, `curl.exe --http1.1`, 2 warm-up + 10 request tuần tự | `GET /login` | 49,405 ms | 60,737 ms | HTTP 200 cho 10/10 mẫu, nhưng curl trả exit 18 | Codex | Số đo thăm dò; không dùng nghiệm thu NFR |
| 17/09/2026 | Working tree chưa commit; Spring Boot 3.3.5, Java 21 | Windows local, profile `dev`, H2 in-memory, `curl.exe --http1.1`, 2 warm-up + 10 request tuần tự | `GET /register` | 48,622 ms | 64,908 ms | HTTP 200 cho 10/10 mẫu, nhưng curl trả exit 18 | Codex | Số đo thăm dò; không dùng nghiệm thu NFR |
| 18/09/2026 | Working tree chưa commit sau sửa CSRF render; Spring Boot 3.3.5, Java 21 | Windows local, profile `dev`, H2 `perfclean18082`, port 18082, `HttpClient` .NET, devtools restart tắt, seed hoàn tất, 2 warm-up + 10 request tuần tự | `GET /` | 31,694 ms | 39,851 ms | HTTP 200 cho 10/10; client error 0; max 44,778 ms | Codex | Baseline public-page hợp lệ trong môi trường này; chưa nghiệm thu toàn bộ NFR |
| 18/09/2026 | Working tree chưa commit sau sửa CSRF render; Spring Boot 3.3.5, Java 21 | Windows local, profile `dev`, H2 `perfclean18082`, port 18082, `HttpClient` .NET, devtools restart tắt, seed hoàn tất, 2 warm-up + 10 request tuần tự | `GET /login` | 21,203 ms | 23,483 ms | HTTP 200 cho 10/10; client error 0; max 24,266 ms | Codex | Baseline public-page hợp lệ trong môi trường này; chưa nghiệm thu toàn bộ NFR |
| 18/09/2026 | Working tree chưa commit sau sửa CSRF render; Spring Boot 3.3.5, Java 21 | Windows local, profile `dev`, H2 `perfclean18082`, port 18082, `HttpClient` .NET, devtools restart tắt, seed hoàn tất, 2 warm-up + 10 request tuần tự | `GET /register` | 21,130 ms | 23,942 ms | HTTP 200 cho 10/10; client error 0; max 23,946 ms | Codex | Baseline public-page hợp lệ trong môi trường này; chưa nghiệm thu toàn bộ NFR |
| 18/09/2026 | Working tree chưa commit sau sửa CSRF render; Spring Boot 3.3.5, Java 21 | Local MockMvc, profile `dev`, H2 in-memory, principal PATIENT, 2 warm-up + 10 request tuần tự | PERF-02 `GET /patient/booking` | 70,162 ms | 97,846 ms | HTTP 200 cho 10/10; max 97,846 ms | Codex | Baseline tầng MVC nội bộ, không gồm TCP/CDN/trình duyệt |
| 18/09/2026 | Working tree chưa commit sau sửa CSRF render; Spring Boot 3.3.5, Java 21 | Local MockMvc, profile `dev`, H2 in-memory, principal STAFF, 2 warm-up + 10 request tuần tự | PERF-03 `GET /staff/reception` | 52,560 ms | 73,677 ms | HTTP 200 cho 10/10; max 73,677 ms | Codex | Baseline tầng MVC nội bộ, không gồm TCP/CDN/trình duyệt |
| 18/09/2026 | Working tree chưa commit sau sửa CSRF render; Spring Boot 3.3.5, Java 21 | Local MockMvc, profile `dev`, H2 in-memory, seeded DOCTOR principal, 2 warm-up + 10 request tuần tự | PERF-04 `GET /doctor/desk` | 37,357 ms | 41,434 ms | HTTP 200 cho 10/10; max 41,434 ms | Codex | Baseline tầng MVC nội bộ, không gồm TCP/CDN/trình duyệt |

| 19/09/2026 | Working tree chưa commit sau direct continuation; Spring Boot 3.3.5, Java 21 | Windows local, jar build hiện tại, profile `dev`, H2 in-memory, port 18083, .NET `HttpClient`, 2 warm-up + 20 request tuần tự | PERF-01 `GET /` | 37,386 ms | 59,181 ms | HTTP 200 cho 20/20; client error 0; max 67,659 ms | ChatGPT direct | Baseline TCP/HTTP local hợp lệ; chưa đại diện production/CDN |
| 19/09/2026 | Cùng working tree/môi trường | Windows local, jar build hiện tại, profile `dev`, H2 in-memory, port 18083, .NET `HttpClient`, 2 warm-up + 20 request tuần tự | `GET /login` | 25,758 ms | 30,663 ms | HTTP 200 cho 20/20; client error 0; max 31,911 ms | ChatGPT direct | Baseline TCP/HTTP local hợp lệ |
| 19/09/2026 | Cùng working tree/môi trường | Windows local, jar build hiện tại, profile `dev`, H2 in-memory, port 18083, .NET `HttpClient`, 2 warm-up + 20 request tuần tự | `GET /register` | 22,367 ms | 31,707 ms | HTTP 200 cho 20/20; client error 0; max 35,896 ms | ChatGPT direct | Baseline TCP/HTTP local hợp lệ |

| 19/09/2026 | Working tree hiện tại sau accessibility/portal QA; Spring Boot 3.3.5, Java 21 | Windows local, profile `dev`, H2 in-memory, .NET `HttpClient`, 2 warm-up + 20 request tuần tự | `GET /` | 37,386 ms | 59,181 ms | HTTP 200, client error 0; max 67,659 ms | ChatGPT + Desktop Commander | Local TCP/HTTP baseline; không thay thế production/CDN acceptance |
| 19/09/2026 | Cùng môi trường | Windows local, profile `dev`, H2 in-memory, .NET `HttpClient`, 2 warm-up + 20 request tuần tự | `GET /login` | 25,758 ms | 30,663 ms | HTTP 200, client error 0; max 31,911 ms | ChatGPT + Desktop Commander | Local TCP/HTTP baseline |
| 19/09/2026 | Cùng môi trường | Windows local, profile `dev`, H2 in-memory, .NET `HttpClient`, 2 warm-up + 20 request tuần tự | `GET /register` | 22,367 ms | 31,707 ms | HTTP 200, client error 0; max 35,896 ms | ChatGPT + Desktop Commander | Local TCP/HTTP baseline |
| 19/09/2026 | Full `mvn verify`; working tree hiện tại | Local MockMvc, profile `dev`, H2 in-memory, role-appropriate principal | PERF-02 `GET /patient/booking` | 55,742 ms | 69,857 ms | HTTP 200 | ChatGPT + Desktop Commander | Authenticated MVC baseline, chưa gồm TCP/CDN/browser |
| 19/09/2026 | Cùng lượt verify | Local MockMvc, profile `dev`, H2 in-memory, role-appropriate principal | PERF-03 `GET /staff/reception` | 41,191 ms | 46,848 ms | HTTP 200 | ChatGPT + Desktop Commander | Authenticated MVC baseline |
| 19/09/2026 | Cùng lượt verify | Local MockMvc, profile `dev`, H2 in-memory, role-appropriate principal | PERF-04 `GET /doctor/desk` | 29,969 ms | 36,406 ms | HTTP 200 | ChatGPT + Desktop Commander | Authenticated MVC baseline |

## Giới hạn hiện tại

Các số đo ngày 17/09 chỉ là thăm dò. Đã xác định nguyên nhân `curl` exit 18: Thymeleaf xử lý `th:action` ở cuối template khi phản hồi đã được commit; việc tạo CSRF session bị từ chối, vì vậy server gửi HTTP 200 nhưng body bị cắt. Base layout nay buộc resolve token CSRF trong `<head>` và `PublicPageRenderIntegrationTest` kiểm tra body hoàn chỉnh của `/login` và `/register`. Lượt đo lại ngày 18/09 dùng `HttpClient` đọc hết body, không có HTTP/client error.

p50 là trung vị của mẫu thứ 5 và 6 sau khi sắp xếp; p95 là nearest-rank (mẫu thứ 10 trong 10 mẫu). Đây chỉ là thời gian tải HTML local, không gồm CDN font/CSS/JS hay rendering của trình duyệt.

PERF-02 đến PERF-04 đã có baseline MockMvc với principal phù hợp và HTTP 200. Các số này không đo TCP, tải tài nguyên CDN, layout hay tương tác trình duyệt; vì thế vẫn chưa thể công bố `NFR-PERF-01` đạt cho toàn hệ thống. Kế hoạch này không đo CDN, email, QR hoặc PDF như một bằng chứng cho NFR-PERF-02; các luồng đó cần benchmark riêng cùng dữ liệu kích thước lớn.
