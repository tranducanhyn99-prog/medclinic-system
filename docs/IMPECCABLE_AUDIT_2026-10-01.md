# Impeccable audit — MedClinic — 01/10/2026

> **Cập nhật sau audit:** đã triển khai sửa lỗi, build và nghiệm thu browser theo [báo cáo xử lý](IMPECCABLE_FIXES_2026-10-01.md). Nội dung và điểm số bên dưới giữ nguyên làm mốc trước sửa.

**Implementation Integrity: FAIL (mốc trước sửa).** Có lỗi xác định được từ code/probe: nút lưu bệnh án bị khóa sau khi hủy xác nhận; response slot cũ ghi đè lựa chọn mới; hotline hiển thị khác số thực sự được gọi; HTML theo session được đưa vào offline cache. Không coi font Geist, gradient, màu tím hay shadow là lỗi chỉ vì detector đánh dấu.

## Kết quả

| Hạng mục | Điểm / 4 | Căn cứ chính |
|---|---:|---|
| Accessibility | 1 | Focus vào sidebar đóng, form thiếu tên truy cập, canvas thiếu nội dung thay thế, contrast và carousel |
| Performance | 2 | Có WebP/lazy loading; ảnh chân dung lớn, Chart.js tải đồng bộ trên mọi trang portal |
| Theming | 2 | Có token và dark mode thực sự; canvas không theo theme, màu primary chưa đạt contrast ở mọi vị trí |
| Responsive | 2 | Grid và bảng cuộn hoạt động; bubble che bộ lọc, vùng chạm carousel nhỏ |
| Implementation Integrity | 1 | Lỗi phối hợp event, request bất đồng bộ, nội dung và offline fallback |
| **Tổng** | **8 / 20 — Poor** | Điểm đánh giá kỹ thuật của lần audit này, không phải điểm Lighthouse |

**19 nhóm vấn đề: 1 P0, 7 P1, 11 P2, 0 P3.** P0: chặn hoàn tất tác vụ; P1: ảnh hưởng lớn hoặc rào cản accessibility; P2: lỗi cục bộ/có cách tránh. Mỗi nhóm chỉ tính một lần dù xuất hiện nhiều trang. Một số vấn đề đã nằm trong backlog kiểm tra dự án trước đó; không cộng trùng thành lỗi mới.

Ưu tiên: (1) sửa submit/confirmation và camera; (2) sửa focus, tên truy cập, thông báo và biểu đồ; (3) khóa response slot theo lựa chọn hiện tại; (4) giảm widget che thao tác và cải thiện contrast; (5) tối ưu ảnh, phụ thuộc và offline fallback.

## Phạm vi và bằng chứng

- Impeccable skill 4.3.1, engine 0.1.5. Chạy `context --target src/main/resources/templates` một lần; chạy detector một lần trên toàn bộ templates, CSS, JS và service worker.
- Kiểm tra nguồn 30 template, tổng 6.375 dòng, stylesheet dùng chung, mascot JS, manifest/SW và các phần SecurityConfig liên quan đến giao diện. Kiểm tra bằng source không đồng nghĩa đã chạy E2E tất cả trạng thái dữ liệu.
- Chạy jar từ lần `clean verify` gần nhất trên `127.0.0.1:18086`, profile dev, database H2 riêng `impeccable_audit`. Browser kiểm tra trang chủ, đặt lịch, đăng nhập demo, dashboard admin, kho thuốc/modal nhập kho, danh sách và chi tiết bác sĩ. Kiểm tra chiều rộng 320, 390 và 1440px; hai theme sáng/tối; tab focus; trạng thái tải slot. Không thực hiện thanh toán, tạo bệnh án hoặc ghi dữ liệu nghiệp vụ qua browser.
- Bảng kết quả detector: [impeccable-detector.json — bằng chứng local, xem tóm tắt](evidence/README.md). Phân loại từng cảnh báo: [impeccable-detector-disposition.json — bằng chứng local, xem tóm tắt](evidence/README.md).
- Source inventory: [impeccable-source-inventory.json — bằng chứng local, xem tóm tắt](evidence/README.md). Probe đảo thứ tự response: [impeccable-race-probe.cjs — bằng chứng local, xem tóm tắt](evidence/README.md), [kết quả — bằng chứng local, xem tóm tắt](evidence/README.md).
- Ảnh browser xác nhận widget che bộ lọc: [mobile 390px — bằng chứng local, xem tóm tắt](evidence/README.md).
- Dùng lại bằng chứng kiểm tra server và submit cancellation đã lưu trong [probe-results.txt — bằng chứng local, xem tóm tắt](evidence/README.md) và [frontend-probe-results.txt — bằng chứng local, xem tóm tắt](evidence/README.md). Những file trong `target` là bằng chứng tạm, có thể bị xóa khi chạy `clean`.
- Không chạy lại Maven vì chưa sửa code. Chưa đo Core Web Vitals, CPU/frame timing, mạng thiết bị thật; chưa nghiệm thu camera thật, screen reader hoặc mọi màn hình theo vai trò. Những giới hạn này không làm mất các lỗi đã chứng minh bằng code/probe.

## P0

### UI-01 — Hủy xác nhận khám làm nút lưu bị khóa

- **Category:** Implementation Integrity / trạng thái tương tác.
- **Vị trí:** [doctor/examine.html:325](../src/main/resources/templates/doctor/examine.html), [layout/dashboard.html:388](../src/main/resources/templates/layout/dashboard.html). Pattern tương tự ở [layout/main.html:426](../src/main/resources/templates/layout/main.html).
- **Bằng chứng/tác động:** Handler màn khám `preventDefault()` để chờ SweetAlert, nhưng handler global vẫn đặt `data-submitting=true` và disable nút. Probe: `DEFAULT_PREVENTED=true; BUTTON_DISABLED=true; FORM_SUBMITTING=true`. Chọn “Kiểm tra lại” không mở lại nút; bác sĩ phải tải lại trang và có thể mất nội dung đang nhập. Backlog cũ: P1-05; P0 ở đây phản ánh tác vụ UI bị chặn.
- **Khuyến nghị:** Chỉ khóa sau validation/confirmation khi request thực sự được gửi; bỏ qua event đã bị hủy và có cơ chế phục hồi khi gửi thất bại. Kiểm tra cancel → sửa → submit, đồng thời giữ chống double-submit.
- **Lệnh hỗ trợ:** `$impeccable harden`.

## P1

### UI-02 — Luồng quét QR dùng camera bị policy chặn

- **Category:** Implementation Integrity / capability.
- **Vị trí:** [SecurityConfig.java:149](../src/main/java/com/medclinic/config/SecurityConfig.java), [staff/reception.html:339](../src/main/resources/templates/staff/reception.html).
- **Bằng chứng/tác động:** Response có `Permissions-Policy: camera=()` trong khi modal khởi tạo `Html5QrcodeScanner`. Cấp quyền camera ở browser không khắc phục policy này; lễ tân phải tra cứu thủ công. Bằng chứng header đã có trong probe; chưa thử phần cứng camera. Backlog: P1-04.
- **Khuyến nghị:** Cho phép camera của origin ứng dụng ở phạm vi cần thiết; phân biệt thiếu quyền, thiếu camera và lỗi kết nối; giữ phương án nhập mã thủ công.
- **Lệnh hỗ trợ:** `$impeccable harden` (cần sửa cấu hình server kèm UI).

### UI-04 — Sidebar mobile đóng nhưng các link vẫn nhận focus

- **Category:** Accessibility / keyboard.
- **Vị trí:** [layout/dashboard.html:173](../src/main/resources/templates/layout/dashboard.html), [layout/dashboard.html:362](../src/main/resources/templates/layout/dashboard.html).
- **Bằng chứng/tác động:** Ở 390px, Tab sau skip link chuyển tới `.sidebar-brand` nằm ngoài màn hình. Sidebar chỉ được `translateX(-100%)`, không `inert`; khi mở cũng chưa quản lý focus trong drawer và nền. Người dùng bàn phím đi qua các điều khiển không nhìn thấy.
- **Chuẩn liên quan:** WCAG 2.4.3 Focus Order, 2.4.7 Focus Visible. Không gọi đây là keyboard trap; Escape có trả focus về nút mở.
- **Khuyến nghị:** Sidebar đóng phải khỏi tab order; mở drawer thì quản lý focus, nội dung nền và trả focus khi đóng; phục hồi đúng khi qua breakpoint desktop.
- **Lệnh hỗ trợ:** `$impeccable harden`, `$impeccable adapt`.

### UI-05 — Form nhập kho và dòng thuốc thiếu tên truy cập

- **Category:** Accessibility / forms.
- **Vị trí:** [admin/medicines.html:169](../src/main/resources/templates/admin/medicines.html), [doctor/examine.html:248](../src/main/resources/templates/doctor/examine.html), [auth/register.html:78](../src/main/resources/templates/auth/register.html).
- **Bằng chứng/tác động:** Modal nhập kho thực tế có `quantity`, `unitCost`, `note` với `id=''`, `labels=[]`; AX chỉ có stepper vô danh. Select/input của dòng kê đơn không có label liên kết; nút xóa chỉ có icon. Register có lỗi hiển thị nhưng không liên kết lỗi bằng `aria-describedby`/trạng thái `aria-invalid`. Screen reader khó nhận biết dữ liệu cần nhập và dòng cần xóa.
- **Chuẩn liên quan:** WCAG 1.3.1, 4.1.2; cần kiểm tra thêm 3.3.1 khi nghiệm thu lỗi form.
- **Khuyến nghị:** ID duy nhất cho từng thuốc/modal/dòng; label rõ theo chức năng và số dòng; tên nút xóa; gắn error text với trường và đưa focus đến lỗi đầu tiên khi submit thất bại.
- **Lệnh hỗ trợ:** `$impeccable harden`, `$impeccable clarify`.

### UI-06 — Hai biểu đồ chỉ có canvas, không có dữ liệu thay thế

- **Category:** Accessibility / non-text content.
- **Vị trí:** [admin/dashboard.html:100](../src/main/resources/templates/admin/dashboard.html), [admin/dashboard.html:112](../src/main/resources/templates/admin/dashboard.html).
- **Bằng chứng/tác động:** DOM thực tế: cả hai canvas không `aria-label`, không role, fallback rỗng; AX có tiêu đề nhưng không có số liệu biểu đồ. KPI tổng không thay thế được phân bố theo trạng thái/chuyên khoa.
- **Chuẩn liên quan:** WCAG 1.1.1 Non-text Content.
- **Khuyến nghị:** Bảng/tóm tắt dữ liệu có ngữ nghĩa dùng cùng nguồn dữ liệu với biểu đồ; tên/mô tả ngắn cho canvas; empty state có văn bản. Kiểm tra trường hợp có dữ liệu, không chỉ demo rỗng.
- **Lệnh hỗ trợ:** `$impeccable harden`.

### UI-07 — Contrast chưa đạt cho một số chữ chức năng

- **Category:** Accessibility / color.
- **Vị trí:** [medclinic-design-system.css:548](../src/main/resources/static/css/medclinic-design-system.css), [medclinic-design-system.css:1865](../src/main/resources/static/css/medclinic-design-system.css).
- **Bằng chứng/tác động:** Chữ trắng trên đầu gradient `#0891b2` có tỷ lệ khoảng 3,68:1; cần đánh giá nền thực tế dưới từng đoạn chữ của nút nhỏ. Sidebar thực tế dùng chữ `#64748b` trên `#090e1a`, khoảng 4,05:1, font 10,88px. Đây là các nhãn dùng để điều hướng, dễ khó đọc hơn với người thị lực kém.
- **Chuẩn liên quan:** [WCAG 1.4.3](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html): chữ thường tối thiểu 4,5:1; không áp ngưỡng này cho chữ lớn hoặc điều khiển disabled.
- **Khuyến nghị:** Tách token màu thương hiệu khỏi màu text/action đạt contrast; tăng sáng nhãn trên sidebar; kiểm tra cả normal/hover/selected/focus ở hai theme, bao gồm gradient dưới chữ. Không đổi toàn bộ palette chỉ để khớp detector.
- **Lệnh hỗ trợ:** `$impeccable colorize`, `$impeccable typeset`.

### UI-08 — Thông báo lỗi tự xóa sau 4,5 giây

- **Category:** Accessibility / thời gian đọc và phục hồi lỗi.
- **Vị trí:** [fragments/alerts.html:34](../src/main/resources/templates/fragments/alerts.html).
- **Bằng chứng/tác động:** Timer áp dụng cho tất cả `.mc-alert`, kể cả lỗi; đóng/xóa DOM sau 4500ms, không có pause/extend. Người đọc chậm hoặc đang sửa input có thể mất lý do thất bại. `role=alert` hỗ trợ thông báo ban đầu nhưng không thay thế khả năng đọc lại.
- **Chuẩn liên quan:** [WCAG 2.2.1 Timing Adjustable](https://www.w3.org/WAI/WCAG22/Understanding/timing-adjustable.html), cần tránh giới hạn thời gian do nội dung đặt ra.
- **Khuyến nghị:** Giữ lỗi tới khi người dùng đóng hoặc xử lý; nếu toast thành công tự ẩn thì có nơi xem lại, dừng timer khi focus/hover nếu cần; không xóa điều khiển đang giữ focus.
- **Lệnh hỗ trợ:** `$impeccable harden`.

### UI-09 — Carousel chưa có cơ chế dừng và quản lý slide ẩn đầy đủ

- **Category:** Accessibility / motion / keyboard.
- **Vị trí:** [home/index.html:771](../src/main/resources/templates/home/index.html), [home/index.html:858](../src/main/resources/templates/home/index.html).
- **Bằng chứng/tác động:** Autoplay mỗi 4800ms, chỉ tạm dừng lúc hover/focus. Không có nút dừng duy trì trạng thái. Track transform mà không loại link của slide ngoài viewport khỏi tab order; AX mobile liệt kê cả 5 slide. `role=tablist/tab` chưa có liên kết panel và cách di chuyển focus tương ứng. Desktop có 3 slide nhưng vẫn 5 dot; index vượt `maxIdx` bị đưa về 0.
- **Chuẩn liên quan:** [WCAG 2.2.2 Pause, Stop, Hide](https://www.w3.org/WAI/WCAG22/Understanding/pause-stop-hide.html), 2.4.3. `prefers-reduced-motion` đang được hỗ trợ, đó là điểm tốt nhưng chưa thay thế nút dừng.
- **Khuyến nghị:** Nút pause/resume rõ ràng; slide không hiện phải khỏi focus/AX khi phù hợp; chọn semantics carousel hoặc tabs nhất quán; indicator theo số nhóm hiển thị ở từng breakpoint.
- **Lệnh hỗ trợ:** `$impeccable harden`, `$impeccable animate`, `$impeccable adapt`.

## P2

### UI-03 — Avatar bác sĩ upload không hiển thị cho khách

- **Category:** Implementation Integrity / tài sản hình ảnh.
- **Vị trí:** [SecurityConfig.java:111](../src/main/java/com/medclinic/config/SecurityConfig.java), [doctors/list.html:76](../src/main/resources/templates/doctors/list.html).
- **Bằng chứng/tác động:** Route `/user-content/avatars/...` bị redirect login cho anonymous (probe HTTP 302); public doctor cards dùng chính `avatarUrl`. Ảnh seed `/images/...` hiển thị, nên demo không bộc lộ lỗi này. Backlog: P2-05.
- **Khuyến nghị:** Quyền public rõ ràng cho ảnh bác sĩ, tách quyền ảnh bệnh nhân; fallback khi ảnh tải lỗi; kiểm tra bằng avatar upload và phiên chưa đăng nhập.
- **Lệnh hỗ trợ:** `$impeccable harden` (cần sửa quyền phía server).

### UI-10 — Vùng chạm carousel quá nhỏ

- **Category:** Responsive / touch.
- **Vị trí:** [medclinic-design-system.css:3441](../src/main/resources/static/css/medclinic-design-system.css), [medclinic-design-system.css:3457](../src/main/resources/static/css/medclinic-design-system.css).
- **Bằng chứng/tác động:** Browser 390px đo dot 8×8px, dot active 26×8px, arrow 38×38px. Khoảng cách dot khoảng 7,2px; dễ bấm sai. Nút hồ sơ/đặt khám khác khoảng 40px cao nên cũng cần rà soát theo chuẩn touch của dự án.
- **Chuẩn liên quan:** [WCAG 2.5.8](https://www.w3.org/WAI/WCAG22/Understanding/target-size-minimum.html) có mức tối thiểu 24px và ngoại lệ spacing/equivalent; 44×44px là mục tiêu tốt hơn, không phải mặc nhiên ngưỡng AA. Không kết luận mọi nút dưới 44px đều vi phạm AA.
- **Khuyến nghị:** Giữ dot nhỏ về thị giác nhưng tăng hit area lên 44×44px; đủ khoảng cách; đảm bảo nút tương đương lớn và chức năng tương đương nếu dùng ngoại lệ.
- **Lệnh hỗ trợ:** `$impeccable adapt`.

### UI-11 — Mascot bubble che bộ lọc bác sĩ trên mobile

- **Category:** Responsive / interaction.
- **Vị trí:** [medclinic-design-system.css:1020](../src/main/resources/static/css/medclinic-design-system.css), [medclinic-design-system.css:1100](../src/main/resources/static/css/medclinic-design-system.css), [fragments/mascot.html:5](../src/main/resources/templates/fragments/mascot.html).
- **Bằng chứng/tác động:** Tại `/doctors`, viewport 390×844, bubble mặc định chiếm vùng x≈103–359, y≈551–715. `elementFromPoint` ở tâm `maxFee` và nút “Lọc bác sĩ” trả phần tử mascot. Bubble có thể đóng nhưng hiện lại mỗi trang; người dùng phải dọn widget để thao tác.
- **Khuyến nghị:** Mobile mặc định launcher gọn, mở bubble theo yêu cầu; tránh overlay lên input/CTA; ghi nhớ lần đóng trong phiên; trên portal dùng trợ giúp theo tác vụ thực tế. Giữ mascot như tài sản hiện có, không cần bỏ toàn bộ.
- **Lệnh hỗ trợ:** `$impeccable adapt`, `$impeccable quieter`.

### UI-12 — Response slot cũ ghi đè bác sĩ/ngày vừa chọn

- **Category:** Implementation Integrity / async state.
- **Vị trí:** [patient/booking.html:437](../src/main/resources/templates/patient/booking.html), [staff/walk-in.html:214](../src/main/resources/templates/staff/walk-in.html).
- **Bằng chứng/tác động:** Không abort/request sequence hoặc kiểm tra doctor/date trước render. Probe chạy function booking lấy từ source: response bác sĩ 2 render slot 202, sau đó response bác sĩ 1 về muộn render slot 101 dù `CURRENT_DOCTOR=2`. Người dùng nhìn lịch không khớp, chọn rồi bị server từ chối. Đây là xác nhận bằng VM/stub, chưa đo race trên mạng thật. Backlog CHECK-01 nay có bằng chứng.
- **Khuyến nghị:** Abort request cũ và/hoặc sequence token; chỉ response ứng với lựa chọn hiện tại được thay trạng thái loading/grid/summary. Kiểm tra đổi nhanh doctor/date, reset specialty trong lúc request chưa xong.
- **Lệnh hỗ trợ:** `$impeccable harden`.

### UI-13 — Lỗi tải lịch bị mô tả như hết lịch khám

- **Category:** Implementation Integrity / error state / UX copy.
- **Vị trí:** [patient/booking.html:177](../src/main/resources/templates/patient/booking.html), [patient/booking.html:494](../src/main/resources/templates/patient/booking.html), [staff/walk-in.html:254](../src/main/resources/templates/staff/walk-in.html).
- **Bằng chứng/tác động:** `.catch()` hiện cùng block “không có lịch làm việc hoặc tất cả khung giờ ... đã kín”; không có retry. Network/HTTP/JSON lỗi bị diễn giải thành hết chỗ, khiến người dùng đổi ngày không cần thiết. Walk-in còn không kiểm tra `res.ok`.
- **Khuyến nghị:** Tách loading/empty/error, thông báo tải thất bại và retry giữ lựa chọn; stale response không được hiện lỗi của request đã bị thay thế. Kiểm tra offline, 500, response HTML và empty JSON.
- **Lệnh hỗ trợ:** `$impeccable harden`, `$impeccable clarify`.

### UI-14 — Biểu đồ không đồng bộ khi đổi theme

- **Category:** Theming.
- **Vị trí:** [admin/dashboard.html:179](../src/main/resources/templates/admin/dashboard.html), [admin/dashboard.html:207](../src/main/resources/templates/admin/dashboard.html), [layout/dashboard.html:405](../src/main/resources/templates/layout/dashboard.html).
- **Bằng chứng/tác động:** Chart được tạo bằng literal màu, không lưu instance để update; toggle chỉ đổi attribute/localStorage. Nền card đổi nhưng legend/tick/grid/border được vẽ vào canvas không đọc lại token. Chưa định lượng contrast pixel của canvas.
- **Khuyến nghị:** Đọc token chart theo theme, giữ instance và update khi đổi theme; gồm text, grid, border, màu series và tooltip. Không dùng màu làm dấu hiệu duy nhất.
- **Lệnh hỗ trợ:** `$impeccable colorize`, `$impeccable harden`.

### UI-15 — Chart.js chặn tải đầu trang trên mọi màn portal

- **Category:** Performance / dependency.
- **Vị trí:** [layout/dashboard.html:36](../src/main/resources/templates/layout/dashboard.html).
- **Bằng chứng/tác động:** Script Chart.js trong head, không `defer`, URL không khóa version; browser kho thuốc cũng tải nó dù không có chart. Phụ thuộc mạng này chặn parse và có thể thay đổi độc lập với build. Đây là rủi ro xác định từ cấu trúc tải, chưa có số ms/LCP đo được.
- **Khuyến nghị:** Chỉ tải ở dashboard có chart, khóa version; sắp xếp defer/init đúng thứ tự; cân nhắc host cùng ứng dụng. Đánh giá các CDN khác theo mức thiết yếu và trạng thái mất mạng.
- **Lệnh hỗ trợ:** `$impeccable optimize`.

### UI-16 — Ảnh chân dung lớn hơn nhiều kích thước sử dụng

- **Category:** Performance / images.
- **Vị trí:** [doctors/list.html:76](../src/main/resources/templates/doctors/list.html), [doctors/detail.html:24](../src/main/resources/templates/doctors/detail.html), thư mục `src/main/resources/static/images/doctors`.
- **Bằng chứng/tác động:** 6 JPEG từ 594.013 đến 663.167 byte, tổng khoảng 3,8MB decimal. Browser list mobile hiển thị ảnh rộng ≈58,4px nhưng ảnh nguồn rộng 1024px. Lazy loading giảm tải ban đầu, không giảm byte khi các ảnh được tải.
- **Khuyến nghị:** Biến thể WebP/AVIF phù hợp card/detail và `srcset/sizes`; dự trữ tỷ lệ/kích thước để tránh dịch layout; áp dụng pipeline cho avatar upload. Giữ chất lượng đủ dùng để nhận diện bác sĩ.
- **Lệnh hỗ trợ:** `$impeccable optimize`.

### UI-17 — Offline fallback trả trang chủ thay cho tác vụ hiện tại

- **Category:** Implementation Integrity / resilience.
- **Vị trí:** [sw.js:3](../src/main/resources/static/sw.js), [sw.js:82](../src/main/resources/static/sw.js), [layout/main.html:28](../src/main/resources/templates/layout/main.html).
- **Bằng chứng/tác động:** Install cache gồm `/`, HTML có CSRF và menu theo session; mọi navigation mất mạng có thể nhận HTML đó khi không có cache đúng URL. Người dùng mở login/booking/portal offline lại thấy trang chủ mà URL vẫn là tác vụ. Nguy cơ HTML khác session cần nghiệm thu thêm; chưa chứng minh rò rỉ bệnh án. Backlog CHECK-04.
- **Khuyến nghị:** Offline shell riêng không chứa token/menu theo tài khoản; chỉ cache public static assets; thông báo mất mạng/khả năng thực hiện tác vụ rõ ràng. Quản lý version/update và chờ hoàn tất background cache writes khi cần.
- **Lệnh hỗ trợ:** `$impeccable harden`, `$impeccable clarify`.

### UI-18 — Nội dung public có số liên hệ và bằng chứng xã hội không đồng bộ

- **Category:** Implementation Integrity / content truthfulness.
- **Vị trí:** [home/index.html:376](../src/main/resources/templates/home/index.html), [home/index.html:413](../src/main/resources/templates/home/index.html), [home/index.html:558](../src/main/resources/templates/home/index.html), [home/index.html:634](../src/main/resources/templates/home/index.html).
- **Bằng chứng/tác động:** Banner ghi/đọc 1900 2267 hoặc 1900 2115 nhưng `href` gọi 1900 6868. “15+ bác sĩ” là literal trong khi dev có 6; 2.800+, 99%, mọi bác sĩ 4,9/120+ không có nguồn dữ liệu trong template. Các hứa hẹn video, tiêm chủng, đối tác BHYT đều đi tới booking chung, chưa có luồng tương ứng được kiểm chứng. Không kết luận dịch vụ ngoài hệ thống không tồn tại; cần chủ sản phẩm xác nhận trước public.
- **Khuyến nghị:** Một nguồn cấu hình số điện thoại; dùng số liệu thật hoặc đánh dấu nội dung demo và bỏ rating chưa có nguồn; CTA đi đúng dịch vụ và phản ánh năng lực đã được xác nhận.
- **Lệnh hỗ trợ:** `$impeccable clarify`, `$impeccable distill`.

### UI-19 — Cấu trúc heading chưa thống nhất ở các tác vụ

- **Category:** Accessibility / semantics.
- **Vị trí:** [doctor/examine.html:19](../src/main/resources/templates/doctor/examine.html), [doctor/schedule.html:20](../src/main/resources/templates/doctor/schedule.html), [error/error.html:16](../src/main/resources/templates/error/error.html), [patient/appointment-detail.html:90](../src/main/resources/templates/patient/appointment-detail.html).
- **Bằng chứng/tác động:** Màn khám bắt đầu bằng h3, trang lỗi h3, phiếu hẹn h5; empty states thường h1 → h5. Cỡ chữ đã được CSS chỉnh nhưng semantics không thay đổi. Điều hướng bằng danh sách heading khó phản ánh cấu trúc nội dung. Không coi mỗi h6 của modal là vi phạm tự động.
- **Chuẩn liên quan:** WCAG 1.3.1; skipped level đơn thuần không đủ để kết luận mọi trường hợp vi phạm.
- **Khuyến nghị:** Mỗi trang có tiêu đề chính h1; section h2/h3 theo cấu trúc, class typography độc lập; modal có heading/label theo dialog riêng; bảng và empty state dùng heading khi thực sự mở section.
- **Lệnh hỗ trợ:** `$impeccable typeset`, `$impeccable harden`.

## Diễn giải detector và nhận xét có ngữ cảnh

Detector trả **73 cảnh báo thuộc 14 rule**. Đây không phải 73 lỗi được xác nhận:

| Rule / số cảnh báo | Kết luận sau đối chiếu |
|---|---|
| flat-type-hierarchy / 20 | Không nhận thành lỗi: static scan không hiểu đầy đủ layout/Bootstrap/stylesheet `th:href`. Browser đo h1 home 50,4px, h2 section 37,6px; hierarchy thực tế không phẳng |
| broken-image / 16 | 15 ảnh được cấp `th:src`; 1 avatar booking khởi tạo `src=''` nhưng hidden tới khi có bác sĩ. Các ảnh seed hiện đúng sau lazy load. Không xác nhận ảnh hỏng bằng cảnh báo này; UI-03 là lỗi riêng về quyền avatar upload |
| skipped-heading / 13 | Rà soát semantics trong UI-19; không tính từng cảnh báo hay heading modal là một lỗi độc lập |
| low-contrast / 7 | Gom các báo trùng về UI-07. Nhãn sidebar xác nhận qua computed style; màu body trên sidebar 1,1:1 là suy luận ancestor sai; hover/nav phải xét CSS override thực tế |
| undersized-ui-text / 4 | Cùng nhãn sidebar 10,88px: vấn đề đọc và contrast ở UI-07; không có chuẩn WCAG quy định chữ tối thiểu 11px |
| clipped-overflow-container / 1 | Offcanvas là dụng ý; clipping tự nó không phải lỗi. Tab focus chưa được quản lý là UI-04 |
| layout-transition / 2 | Margin-left đổi ở breakpoint, width ở vài indicator; không có profile chứng minh tác động đáng kể. Không báo thành lỗi hiệu năng riêng |
| overused-font / 4 | Geist/Geist Mono đang là hệ thống incumbent; sự phổ biến của font không phải lỗi kỹ thuật |
| thin-border-wide-shadow, ai-color-palette, dark-glow, gradient-text, monotonous-spacing / mỗi rule 1 | Tín hiệu đánh giá thị giác, không có yêu cầu sản phẩm bị chứng minh là vi phạm; giữ để critique, không tự yêu cầu thay thiết kế |
| pulsing-dot / 1 | Trang chủ có animation trạng thái; có reduced-motion toàn cục. Không coi một dot là lỗi độc lập; nội dung “24/7” cần xác nhận ở UI-18 |

Không có PRODUCT.md/DESIGN.md ở gốc. Audit dùng giao diện hiện tại làm chuẩn; không bắt buộc chạy init hoặc redesign. Có thể dùng `document` để ghi lại token/semantics đã thống nhất sau khi sửa.

## Pattern và điểm tốt

Các lỗi tập trung ở lớp dùng chung: form lock không phối hợp với handler từng trang; overlay giữ điều khiển nền trong tab order; async state thiếu request identity; state lỗi và nội dung marketing không theo dữ liệu thực. CSS có token nhưng nhiều literal/override theo selector; canvas nằm ngoài cơ chế đổi CSS theme. Nên xử lý các pattern này rồi kiểm tra lại các trang dùng chung.

Điểm tốt đã thấy:

- `lang=vi`, viewport meta, main landmark và skip link; focus-visible có style; nav có tên và aria-current ở portal.
- Booking có label, loading/status live region, aria-busy, aria-pressed cho slot và khôi phục lựa chọn sau login.
- Dark mode được bootstrap trước render và lưu lựa chọn; có token cho text/surface/border; hai theme đều hiển thị được trong browser.
- Grid mobile reflow ở các trang đã thử; bảng kho thuốc cuộn trong vùng riêng (342px client /1189px nội dung tại 390px), có gợi ý vuốt. Không coi bảng dữ liệu cần cuộn ngang là lỗi tràn toàn trang.
- Có WebP cho banner/background, lazy loading ảnh list/home, JS mascot giới hạn pointer tracking trên thiết bị fine pointer.
- Carousel dừng khi focus/hover và khi reduced-motion; mascot không chạy Web Animations squash khi reduced-motion. Global CSS 0,01ms là cách xử lý thô nhưng chưa chứng minh mất thông tin chức năng, nên không thêm finding riêng.
- Modal nhập kho dùng Bootstrap dialog/focus management; ngân hàng có live status và phương án đối soát thủ công.

## Lộ trình dùng Impeccable

1. `$impeccable harden`: UI-01/02/03/04/05/06/08/09/12/13/17; sửa cấu hình server đi kèm khi cần.
2. `$impeccable clarify`: UI-13/17/18 và thông báo phục hồi.
3. `$impeccable adapt`: UI-04/09/10/11; nghiệm thu 320/390/768/1024/1440px và text zoom.
4. `$impeccable colorize` + `$impeccable typeset`: UI-07/14/19; kiểm tra đủ theme/state.
5. `$impeccable optimize`: UI-15/16; sau đó đo mạng và Core Web Vitals để đối chiếu.
6. `$impeccable polish`: chỉ sau khi các lỗi tương tác và accessibility ưu tiên đã được xử lý.

Có thể chạy từng lệnh, một yêu cầu xử lý toàn bộ, hoặc chọn thứ tự phù hợp. Sau khi sửa, chạy lại `$impeccable audit` để đối chiếu từng ID và kiểm tra regression. Lần audit này chỉ tạo báo cáo/bằng chứng và cập nhật trạng thái backlog; chưa sửa giao diện hay nghiệp vụ.
