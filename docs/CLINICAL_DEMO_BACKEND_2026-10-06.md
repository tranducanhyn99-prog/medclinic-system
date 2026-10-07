# Backend cho kịch bản bảo vệ — 06/10/2026

Profile `dev`/`demo` mặc định cho phép xử lý ngay lịch đã đặt từ hôm nay đến 14 ngày tới, gồm check-in, khám, kê đơn, thu tiền và xuất PDF. Ngoài hai profile này, backend vẫn yêu cầu ngày khám là hôm nay theo `Asia/Ho_Chi_Minh`.

## Hành vi

- `ClinicalDatePolicy` dùng cùng giới hạn ngày cho check-in, bắt đầu khám, mở form và lưu bệnh án. Không thay business clock hay ngày hẹn gốc.
- Lễ tân tìm mã/điện thoại/tên trong khoảng ngày demo. Bàn khám lấy ca đã tiếp nhận/đang khám/hoàn tất trong cùng khoảng, sắp xếp theo ngày hẹn và STT; hiển thị ngày hẹn để phân biệt STT của nhiều ngày.
- Giữ kiểm tra role, bác sĩ phụ trách, trạng thái, CSRF, tồn kho, transaction và phát sự kiện sau commit. Vãng lai vẫn chỉ tiếp nhận hôm nay.
- `DEMO_EARLY_CLINICAL_PROCESSING=false` tắt chế độ xử lý sớm trong `dev`/`demo`; Compose truyền biến này vào app. Cờ không có tác dụng ngoài `dev`/`demo`, hoặc khi có `prod`.

## Kiểm chứng

| Kiểm tra | Kết quả |
|---|---|
| Bốn suite tập trung về ngày khám/demo | 38/38 test đạt |
| Maven `clean verify` | 257 case: 231 chạy đạt, 26 SQL Server skipped; 0 failure/error; BUILD SUCCESS |
| JaCoCo | LINE 84,53%, BRANCH 68,59%; vượt gate 70%/50% |
| Node frontend | 25/25 test đạt |
| Docker Compose | `docker compose config --quiet` đạt |
| Tài liệu và patch | Kiểm tra link và `git diff --check` đạt |

`ClinicalDemoWorkflowIntegrationTest` chạy HTTP MockMvc với H2 và đúng vai trò: đặt lịch ngày mai/ngày thứ 14 → tra cứu ở hai endpoint lễ tân → check-in → xuất hiện ở bàn khám → mở/lưu bệnh án với thuốc → kiểm tra tồn kho/hóa đơn → thu tiền mặt → tải PDF. Ngày hẹn gốc không đổi. Case quá khứ và ngày thứ 15 bị chặn, không cấp STT hoặc tạo bệnh án/hóa đơn. Bộ kiểm thử chế độ strict vẫn từ chối ngày khác hôm nay.

Các script Playwright fixture đã được đổi từ từ chối lịch tương lai sang demo tiếp nhận, khám, thanh toán giả trong fixture và tải PDF; syntax đã kiểm tra. Chưa chạy lại browser CLI hoặc SQL Server trong lượt này vì Docker engine không hoạt động. Không diễn giải kết quả H2 thành nghiệm thu SQL Server/ngân hàng thật. Các biên bản 03–04/10 giữ kết quả lịch sử trước thay đổi này.

## Sử dụng

Mở Docker Desktop rồi chạy `docker compose up -d --build` để cập nhật bản SQL Server + Mailpit. Không xóa volume. Profile `sqlserver,demo` và cờ demo mặc định đã có trong Compose.

Chạy H2 bằng `mvn spring-boot:run` cũng bật chế độ này qua profile `dev`. Demo vãng lai vẫn cần khung giờ hôm nay còn khả dụng; demo lịch đặt trước có thể chọn ngày mai hoặc ngày trong 14 ngày tới.
