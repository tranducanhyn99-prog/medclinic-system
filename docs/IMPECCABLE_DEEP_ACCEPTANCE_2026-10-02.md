# MedClinic — Triển khai và nghiệm thu UX/UI chuyên sâu

Ngày: 02/10/2026. Tiếp nối `IMPECCABLE_IMPLEMENTATION_2026-10-02.md`, tập trung UX-07/10/12/13/15/16 bằng Impeccable harden và kiểm tra giao diện render.

## Thay đổi đã thực hiện

- **Validation Admin:** sửa liên kết label/input do `th:field` sinh ID khác ID cũ; thêm trạng thái invalid và liên kết thông báo lỗi. Sau lỗi máy chủ, modal tự mở và chỉ focus trường lỗi sau khi focus trap của Bootstrap hoạt động. Thông báo lỗi tổng quát của form có `role="alert"`.
- **Giữ chế độ sửa:** form thuốc/chuyên khoa trả lại sau validation giữ ID trong hidden input, tiêu đề và nút lưu phản ánh thao tác sửa. Trước đây lần gửi tiếp theo có thể đi vào nhánh tạo mới. Kiểm thử cả ID trống và ID 42 với dữ liệu invalid.
- **Chuyển khoản:** tách logic trạng thái sang `bank-payment-status.js`. Poll bằng timeout nối tiếp, không chồng request; timeout mạng 10 giây, có thử lại, thông báo chưa xác nhận và hướng đăng nhập lại. Dừng khi ẩn trang hoặc rời trang; tiếp tục khi quay lại. Không tự điều hướng sau 900ms để người dùng có thời gian đọc kết quả.
- **Trạng thái giao dịch:** PENDING/CONFIRMED/EXPIRED/REJECTED có thông báo riêng ngay từ HTML máy chủ. Trạng thái kết thúc ẩn QR và form đối soát. Khi phiên hết hạn cũng ẩn các điều khiển này. Không suy ra đã trả tiền chỉ từ việc quét QR hoặc lỗi mạng. Thông báo live region tránh ghi lại cùng nội dung sau mỗi poll.
- **QR lỗi tải:** có hướng dùng thông tin giao dịch bên cạnh hoặc tải lại trang.
- **Theme:** sửa nhãn sidebar, nút viền, breadcrumb, chữ secondary trên nền pastel, status Admin, status lịch hẹn bệnh nhân, chứng chỉ bác sĩ và lịch công tác. Hàng Đang khám ở reception desktop tối dùng màu bảng phù hợp với theme.
- **Lịch công tác:** header/footer theo token, giờ trống dễ đọc, ngày hiển thị Thứ 2…Chủ nhật và có chú thích trạng thái khung giờ.
- **Tên truy cập/vùng chạm:** tìm tài khoản Admin có label thật; nút đóng modal có tên tiếng Việt và kích thước 44px; kiểm tra liên kết label trong ba modal tạo Admin.
- **Reduced motion:** bỏ quy tắc ép mọi animation xuống 0.01ms ở cả hai shell và design system. Tắt chuyển cảnh và chuyển động trang trí, giữ spinner chậm và chỉ báo thời gian thông báo. Mascot kiểm tra tùy chọn giảm chuyển động khi theo con trỏ; biểu đồ cập nhật tùy chọn khi media query đổi trong phiên.

## Bằng chứng render

Preview H2 riêng: `http://127.0.0.1:18088`. Không thay đổi môi trường SQL Server 18087.

54 bản ghi kiểm tra ban đầu và 37 bản ghi xác nhận, có 23 đường dẫn được render trong các vai trò phù hợp. Kích thước chủ yếu 1440×900 và 390×844, light/dark. Không phải tích đầy đủ mọi route × theme × viewport × trạng thái.

Phạm vi: home, danh sách/chi tiết bác sĩ, login/register/forgot-password/profile; 5 màn Admin; desk/schedule/examine của bác sĩ; reception/walk-in/billing của staff; 4 màn bệnh nhân; 403 theo vai trò và đường dẫn không tồn tại. OTP/reset-password có token và giao dịch ngân hàng được kiểm tra từ nguồn/test, chưa thao tác live trong lượt này.

Kết quả cuối theo khóa route/theme/viewport: **32 tổ hợp đã xác nhận không còn cảnh báo trong phạm vi detector** về chữ trên nền đặc, label trường hiển thị, ID trùng, tham chiếu ARIA hỏng, ảnh tải hỏng và tràn ngang. Detector không đánh giá gradient, opacity, placeholder, pseudo-element, hover/focus hoặc screen reader thực. Kết quả này không phải chứng nhận WCAG.

| Component | Trước | Sau | Nền dùng để đo sau |
|---|---:|---:|---|
| Nhãn sidebar | 4.05:1 | 7.52:1 | #090e1a |
| Status hoạt động Admin dark | 1.45:1 | 5.21:1 | #ecfdf5 |
| Giờ trống lịch bác sĩ | 3.47:1 | 5.05:1 | #eef8f1 |
| Chữ chứng chỉ bác sĩ dark | 1.45:1 | 7.09:1 | #f0f9ff |
| Secondary trên nền trang sáng | 4.48:1 | 7.24:1 | #f8fafc |
| Nội dung chính hàng Đang khám reception dark | 1.12:1 | 8.71:1 | #164e63 |
| Chữ phụ cùng hàng reception dark | 2.20:1 | 6.14:1 | #164e63 |

Validation live: gửi tên catalog DEMO dài 125 ký tự vào form thuốc H2. Máy chủ trả lỗi tối đa 100 ký tự, giữ nguyên input, modal mở lại, focus `name`, `aria-invalid=true`, `aria-describedby=field-error-0`. Không tạo thuốc. Không lưu bệnh án hoặc xác nhận thanh toán.

Keyboard: sidebar mobile mở bằng Enter, Tab/Shift+Tab quay trong điều hướng, Escape về nút menu. Modal thuốc có vòng Tab và Escape trả về launcher. Ba modal tạo Admin có label đúng trong DOM; modal bác sĩ Escape đóng được sau khi hiệu ứng mở kết thúc. Kiểm tra này dùng browser và accessibility tree, không dùng phần mềm đọc màn hình.

Không có custom autocomplete/combobox trong nguồn hiện tại; mục này không áp dụng. Các trường dùng native input/select. Chưa nghiệm thu hoạt động autofill của trình duyệt trên thiết bị thật.

Ảnh và JSON: `evidence/impeccable-deep-2026-10-02/`. JSON giữ cả số đo trước/sau và giới hạn đo; bản ghi reception dark sớm còn lỗi đã được thay bằng kết quả cuối cùng không lỗi khi tổng hợp theo khóa. Console error ở tab xác nhận cuối: 0.

## Kiểm thử

- Node: **17/17 pass**. Có regression cho mở modal lỗi/focus, request ngân hàng không chồng, mất mạng/thử lại, trạng thái cuối, payload xác nhận mâu thuẫn, 401, timeout, abort và hide/resume; biểu đồ giảm chuyển động trong phiên.
- Maven offline toàn bộ: **204 test, 179 pass, 25 skipped, 0 failure/error**. 25 test SQL Server integration không chạy trong cấu hình này. Lượt này diễn ra trước những chỉnh sửa cuối về template/CSS.
- Sau chỉnh sửa template: **14/14 Java pass** (`DeepUiStateIntegrationTest`, `LayoutTemplateContractTest`), gồm 4 trạng thái ngân hàng được mock, form invalid giữ identity và lịch tiếng Việt.
- `node --check` và `git diff --check` pass. Các bước test không gửi giao dịch ngân hàng. HTML fixture ngân hàng không tạo instruction hoặc gọi confirmManual.

## Các tiêu chí còn mở

| Task | Phần đã hoàn thành trong lượt này | Cần nghiệm thu tiếp |
|---|---|---|
| UX-07 | Tương phản render theo vai trò; các cặp lỗi được sửa và lưu số đo | Gradient/placeholder, hover/focus và toàn bộ trạng thái dữ liệu thực |
| UX-10 | Giữ nguyên cơ chế rà soát và guard; không có thay đổi nghiệp vụ khám | Submit/cancel browser và kết quả lưu ca DEMO cần xác nhận riêng còn đang chờ |
| UX-12 | Label Admin, tìm tài khoản, nút đóng, modal DOM/AX | Screen reader, zoom 200%, autofill và vùng chạm trên máy thật |
| UX-13 | Bank timeout/error/retry/expiry/session/terminal, validation CRUD có retention | Provider thật, QR ngân hàng lỗi tải trực tiếp, trạng thái lưu khám và đầy đủ CRUD |
| UX-15 | Sidebar trap/Escape, vòng focus modal thuốc; label các modal tạo | Screen reader và hoàn tất workflow ghi dữ liệu; modal sửa/nhập kho ở mọi trạng thái |
| UX-16 | Reduced-motion CSS/JS, media-query chart test, desktop/mobile render | Reduced-motion OS, touch, zoom 200% thật và trình duyệt khác |

Ca khám DEMO vẫn chưa được lưu: automatic approval review ở lượt trước đã chặn thao tác ghi bệnh án/đơn thuốc/hóa đơn; xác nhận cụ thể gửi trước đó chưa có câu trả lời. Không thử đường vòng qua API/CLI để thực hiện thao tác bị chặn.

Lượt nghiệm thu trực tiếp tiếp theo: bật giảm chuyển động trong OS; zoom browser 200%; chạy NVDA/Narrator với label/error/modal/live region; thao tác touch trên điện thoại; sau xác nhận cụ thể mới lưu ca khám DEMO H2 và kiểm tra kết quả. Thanh toán thật do người dùng thao tác.
