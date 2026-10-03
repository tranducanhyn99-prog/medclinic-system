# Đánh giá B — detector và bằng chứng trình duyệt (2026-10-02)

## Phạm vi và cách làm

Phạm vi: `src/main/resources/templates` (30 template HTML theo yêu cầu). Impeccable detector được chạy đúng một lần bằng `detect --json`. JSON thô được lưu tại `docs/evidence/impeccable-critique-2026-10-02/whole/b/detector-raw.json`. Detector trả về 46 phát hiện trong 23 template: 45 cảnh báo và 1 khuyến nghị. Công cụ đánh dấu tất cả kết quả bằng `line: 0` (phát hiện ở cấp tệp); các dòng mã bên dưới là vị trí markup/CSS dùng để đối chiếu khi có.

Không sửa mã, không thực hiện thao tác nghiệp vụ, không khởi động server, không chèn title/script và không tạo overlay. Server localhost đã sẵn sàng tại `http://127.0.0.1:18087` trước khi đánh giá.

## Phát hiện detector

| Rule | Số lượng | Tệp (detector báo line 0) | Đối chiếu |
|---|---:|---|---|
| `flat-type-hierarchy` | 20 | `templates/admin/dashboard.html`, `admin/doctors.html`, `admin/medicines.html`, `admin/specialties.html`, `admin/users.html`; `auth/login.html`, `auth/profile.html`, `auth/register.html`; `doctor/desk.html`, `doctor/examine.html`, `doctor/schedule.html`; `doctors/detail.html`, `doctors/list.html`; `home/index.html`; `patient/appointments.html`, `patient/booking.html`, `patient/medical-history.html`; `staff/bank-payment.html`, `staff/billing.html`, `staff/walk-in.html` | Nhiều kết quả có vẻ là dương tính giả do parser không xét Bootstrap và CSS dùng chung bên ngoài: detector báo mọi vai trò chữ 16px, trong khi `static/css/medclinic-design-system.css:928-931` đặt `.mc-hero-title` ở 2–3.15rem. Ảnh chụp `/`, `/doctors`, `/patient/booking`, `/doctor/desk` cũng cho thấy tiêu đề lớn hơn nội dung. Chưa render riêng từng template trong cả 20 tệp, vì vậy không thể kết luận mọi kết quả đều sai. |
| `broken-image` | 16 | `templates/admin/doctors.html`; `auth/profile.html`; `doctors/detail.html`, `doctors/list.html`; `home/index.html` (6 lần); `layout/dashboard.html`; `layout/main.html` (2); `patient/appointment-detail.html`; `patient/booking.html`; `staff/bank-payment.html` | 15 kết quả là do detector đọc HTML thô mà không xử lý Thymeleaf: ảnh đại diện có điều kiện và `th:src` (`admin/doctors.html:55`, `doctors/detail.html:24`, `doctors/list.html:76`, `home/index.html:628`); logo (`home/index.html:385,419,456,490,524`, `layout/dashboard.html:222`, `layout/main.html:215,342`); QR (`patient/appointment-detail.html:113`, `staff/bank-payment.html:22`). Đây là dương tính giả khi render với dữ liệu. `auth/profile.html:23` chỉ kiểm tra null, không kiểm tra chuỗi rỗng nên vẫn có trường hợp ảnh hỏng. `patient/booking.html:91` có `src=""` tường minh; phần tử đang ẩn bởi `d-none` ở trạng thái đầu và có vẻ là chỗ giữ chỗ sẽ được JS điền sau khi chọn bác sĩ. Cảnh báo đúng với markup nguồn nhưng không tạo ảnh lỗi trong trạng thái ban đầu đang thấy. |
| `ai-color-palette` | 1 | `templates/home/index.html` | Detector nhận diện điểm nhấn tím/cyan trên nền tối. Trang chủ quan sát được chủ yếu dùng teal/cyan cho chăm sóc sức khỏe; tím chỉ xuất hiện giới hạn ở biểu tượng dịch vụ. Đây là đánh giá phong cách, không phải lỗi chức năng. |
| `clipped-overflow-container` | 1 | `templates/layout/dashboard.html` | Detector nêu `body` cắt phần tử con định vị. CSS có `body { overflow-x: hidden; }` tại dòng 60, có thể cắt nội dung tràn theo chiều ngang; trong lượt mở trang bác sĩ này chưa xác minh được popover nào bị cắt. |
| `dark-glow` | 1 | `templates/home/index.html` | Có bóng phát sáng màu cyan trong style trang chủ, ví dụ card ở dòng 18/25 và banner ở dòng 154. Có thật; mức độ có quá tay hay không là đánh giá chủ quan. |
| `gpt-thin-border-wide-shadow` | 1 khuyến nghị | `templates/home/index.html` | Xác nhận có viền ở dòng 15 và bóng blur 45px ở dòng 18 trên card tính năng. Đây là khuyến nghị, không phải lỗi cản trở người dùng. |
| `layout-transition` | 1 | `templates/layout/dashboard.html` | Xác nhận `transition: margin-left` ở dòng 207; cảnh báo về hiệu năng/phong cách. |
| `low-contrast` | 1 | `templates/layout/dashboard.html` | Detector báo `#0f172a` trên `#090e1a` (1.1:1), khớp màu chữ body ở dòng 59 và biến nền sidebar ở dòng 41. Ảnh chụp bàn khám cho thấy chữ sáng trên sidebar tối, nên nhiều khả năng detector ghép màu thuộc hai vùng khác nhau. Nền body mặc định là `#f8fafc` ở dòng 44. |
| `monotonous-spacing` | 1 | `templates/staff/bank-payment.html` | Rule thấy 12px được dùng ở 7/11 giá trị khoảng cách đã trích xuất. Không quan sát được hiệu ứng trực quan vì route staff trả về 403. |
| `overused-font` | 2 | `templates/layout/dashboard.html`, `templates/layout/main.html` | Xác nhận Geist được khai báo lần lượt ở `layout/dashboard.html:57` và `layout/main.html:66`. Đây là nhận xét thẩm mỹ, không phải vấn đề sử dụng. |
| `pulsing-dot` | 1 | `templates/home/index.html` | Có `.mc-status-pulse` cùng animation lặp vô hạn ở `static/css/medclinic-design-system.css:792-804` (bản inline trang chủ khoảng dòng 54-66). Trong hero, nhãn là “Hướng dẫn khám”, không phải trạng thái dữ liệu trực tiếp; quan ngại của detector có cơ sở. |

JSON thô lưu đầy đủ tên rule, mô tả, severity, snippet, đường dẫn tuyệt đối và `line: 0`. Tổng các nhóm trên là 46, tính cả một khuyến nghị.

## Quan sát trình duyệt

Dùng tab IAB mới (browser ID 2) trên localhost; chụp AX snapshot và ảnh cho `/`, `/doctors`, `/patient/booking`, `/doctor/desk`. API trình duyệt trong subagent này không hỗ trợ override viewport hoặc xuất ảnh thành tệp. Ảnh chỉ hiển thị inline trong đầu ra công cụ CUA; không có tệp ảnh được tạo. Canvas quan sát được khoảng 1264×712, nên không thể xác minh ở 1440×900 và 390×844 như kế hoạch. Không chèn overlay và không khẳng định có overlay để xem. Không điều khiển được visibility trong subagent nên tab ở nền.

| Route | Kết quả và bằng chứng |
|---|---|
| `/` | Trang chủ tiếng Việt tải ở giao diện tối; thanh điều hướng cao khoảng 72px; hero xấp xỉ x=73–1176, y=140–684; tiêu đề trắng/cyan nổi bật, ô tìm bác sĩ, chip chuyên khoa, thẻ dịch vụ và trợ lý y tế nổi ở góc dưới phải. Mở menu vai trò một lần để đọc các lựa chọn hiển thị; không đổi vai trò. Ảnh nằm inline trong đầu ra CUA của lượt này, không có tệp ảnh. |
| `/doctors` | Tải thành công với breadcrumb và tiêu đề; một dải lọc ngang; 6 thẻ bác sĩ, ba thẻ trên một hàng ở chiều rộng quan sát được; trợ lý y tế nổi ở góc dưới phải. Thẻ đầu hàng tiếp tục xuống dưới mép viewport. Ảnh chỉ nằm inline. |
| `/patient/booking` | Form tải ở trạng thái chưa chọn. Chuyên khoa chưa chọn, trường bác sĩ bị vô hiệu hóa, khu vực giờ trống hướng dẫn chọn bác sĩ/ngày, tổng phí tạm tính là 0. Không thay đổi trường và không gửi đặt lịch. Ảnh chỉ nằm inline. |
| `/doctor/desk` | Cổng bác sĩ tải với sidebar trái cố định rộng khoảng 252px, bốn thẻ thống kê lượt khám, trạng thái hàng chờ realtime, nút làm mới và hai hàng được ghi rõ là demo. Không nhấn thao tác hàng chờ. Ảnh chỉ nằm inline. |
| `/patient/appointments` | Truy cập trực tiếp trả về trang Spring Whitelabel Error Page, HTTP 403 Forbidden. |
| `/staff/walk-in` | Truy cập trực tiếp trả về trang Spring Whitelabel Error Page, HTTP 403 Forbidden. |

Các kích thước trên là từ ảnh công cụ, không phải giả lập thiết bị. Chưa quan sát được trang admin, bệnh án bệnh nhân, quy trình staff, theme sáng hoặc bố cục mobile. Route bệnh nhân và staff bị giới hạn bởi 403 nên không có bằng chứng trực quan hiện tại cho các trạng thái đó.

## Dọn dẹp và giới hạn bằng chứng

Đưa tab đánh giá về `/` rồi đóng tab do assessment tạo. Theme ban đầu là tối; không chạm nút đổi theme. Không áp dụng override viewport nên không cần khôi phục. Server có sẵn được để chạy; assessment không khởi động hay dừng server. Không đụng tới tab của người dùng. Không có tệp tạm cần dọn.

Tệp bằng chứng trong workspace:

- JSON thô: `docs/evidence/impeccable-critique-2026-10-02/whole/b/detector-raw.json`
- Báo cáo Assessment B này: `docs/evidence/impeccable-critique-2026-10-02/whole/b/assessment-b.md`
- Tệp ảnh: không có; ảnh chụp chỉ xuất hiện inline trong đầu ra CUA của phiên này.
