# Kế hoạch baseline hiệu năng

Mục tiêu NFR-PERF-01 trong SRS: p95 thời gian tải HTML sau warm-up dưới 1,5 giây. Đây là kế hoạch đo; kết quả chức năng xem [hướng dẫn kiểm thử](../KIEM_THU.md).

## Môi trường và kịch bản

Mỗi lần đo ghi commit/working-tree state, Java/Spring, profile, database, CPU/RAM, mạng, kích thước dữ liệu và công cụ. Dùng dữ liệu mô phỏng.

| ID | Trang / thao tác | Mẫu sau 2 warm-up | Chỉ số và mục tiêu |
| --- | --- | ---: | --- |
| PERF-01 | GET / | 30 | p50, p95 HTML; p95 <1,5 giây |
| PERF-02 | GET /patient/booking, đăng nhập bệnh nhân | 30 | p50, p95 HTML; p95 <1,5 giây |
| PERF-03 | GET /staff/reception, dữ liệu đại diện | 30 | p50, p95 HTML; p95 <1,5 giây |
| PERF-04 | GET /doctor/desk, hàng chờ đại diện | 30 | p50, p95 HTML; p95 <1,5 giây |
| PERF-05 | Tạo VietQR cho hóa đơn chưa thanh toán | 30 | p95 server response <1,5 giây, không lỗi |
| PERF-06 | Callback hợp lệ và gửi lặp trên fixture riêng | 30 giao dịch | p95 <500 ms; mỗi hóa đơn chỉ xác nhận một lần |

Mỗi mẫu phải đọc hết response và kiểm tra HTTP/body; bỏ kết quả có lỗi client hoặc phản hồi bị cắt. Sắp xếp mẫu, dùng nearest-rank ceil(p × n): 30 mẫu lấy p50 ở vị trí 15, p95 ở vị trí 29. Ghi HTTP error và lỗi client riêng.

## Công cụ có sẵn

- PortalPerformanceBaselineIntegrationTest và SqlServerPortalPerformanceBaselineIntegrationTest: 2 warm-up + 10 mẫu, đo tầng MockMvc; in số liệu, không tự khẳng định đạt NFR.
- ops/academic-acceptance.cjs: kiểm tra HTTP loopback và thời gian trang; báo cáo trong output, mặc định đọc ứng dụng local. Chế độ ghi fixture chỉ chấp nhận cổng 18090.
- Browser DevTools đo riêng tải asset, layout và tương tác khi cần đánh giá trải nghiệm.

Mẫu ghi nhận cho từng lượt:

| Ngày, commit | Môi trường và dữ liệu | Kịch bản | Số mẫu | p50 | p95 | Lỗi | Kết luận |
| --- | --- | --- | ---: | ---: | ---: | --- | --- |
| Điền khi đo | Profile, DB, CPU/RAM, số bản ghi, công cụ | PERF-xx | — | — | — | — | Chưa đo |

## Giới hạn

MockMvc không gồm TCP hoặc trình duyệt. HTTP HTML local không gồm toàn bộ asset, layout, LCP hoặc tải đồng thời. Nghiệm thu chức năng và test cạnh tranh có kiểm soát không thay benchmark tải lớn. Chưa có số đo mới của đợt dọn repo 15/10/2026; snapshot đo cũ có thể tra lại trong lịch sử Git.
