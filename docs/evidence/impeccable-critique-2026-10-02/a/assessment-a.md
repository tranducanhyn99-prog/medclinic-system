# Assessment A — Bàn khám bác sĩ

Ngày 02/10/2026. Đánh giá độc lập, không đọc detector, các audit/backlog hay kết quả B. Target: `src/main/resources/templates/doctor/desk.html`; live: `http://127.0.0.1:18087/doctor/desk`. Mode Operate; giữ bản sắc cyan/navy và điều dưỡng An Nhiên.

## Phạm vi bằng chứng

Quan sát trực tiếp ba cấu hình: desktop 1440×900 dark, desktop 1440×900 light, mobile 390×844 light. Phiên demo bác sĩ hiển thị 2 cuộc hẹn đã hoàn tất, 0 chờ và 0 đang khám. Không tạo, bắt đầu, lưu ca khám hoặc thanh toán. Không mở form khám vì dữ liệu hiện tại không có ca đang khám và trần ba cấu hình đã đủ bằng chứng cho target. Đọc thêm examine/schedule/layout/CSS/mascot để hiểu hệ thống; không chấm các màn hình liên quan thành target riêng.

Đã khôi phục theme dark ban đầu, reset viewport và đóng tab riêng. Không sửa mã sản phẩm. Các trạng thái chờ, đang khám, rỗng, mất kết nối, lỗi nghiệp vụ, submit/loading và thành công sau lưu chưa được kích hoạt; nhận định về chúng bên dưới là từ nguồn, không phải đã kiểm thử live. Không đo contrast, hiệu năng hay thử NVDA.

Ảnh: `desk-desktop-dark.png`, `desk-desktop-light.png`, `desk-mobile-light.png` trong cùng thư mục. Ảnh mobile là full-page, còn hai ảnh desktop là viewport.

## Design specificity

Đặc thù nghiệp vụ ở mức khá: STT, khung giờ, chuyên khoa, bác sĩ, triệu chứng và thao tác phụ thuộc trạng thái tạo ra một bàn khám đúng ngữ cảnh. Sidebar theo vai trò và nhãn tiếng Việt giúp bác sĩ hiểu ngay mình ở đâu. Tuy nhiên bố cục bốn KPI ngang và bảng bên dưới vẫn mang mô hình dashboard chung; câu hỏi thiết yếu “ai cần tôi xử lý tiếp?” chưa có ưu tiên riêng. Hai hồ sơ đã hoàn tất vẫn chiếm toàn bộ bảng mang tên “Hàng chờ”. Cần củng cố thứ tự tác vụ, không cần đổi thế giới thị giác.

## Nielsen — 23/40, Acceptable

Các điểm là đánh giá thiết kế có giới hạn trạng thái, không phải chứng nhận khả dụng toàn luồng. Cả 10 heuristic áp dụng vì đây là Operate.

| # | Heuristic | Điểm /4 | Cơ sở |
|---|---|---:|---|
| 1 | Hiển thị trạng thái hệ thống | 3 | KPI và badge realtime rõ; thiếu thời điểm cập nhật dữ liệu gần nhất. |
| 2 | Phù hợp thế giới thực | 3 | STT, bác sĩ, chuyên khoa, triệu chứng rõ; “Realtime/client/check-in” còn là ngôn ngữ hệ thống. |
| 3 | Quyền kiểm soát và tự do | 3 | Điều hướng rõ, refresh thủ công; nguồn examine có Thoát/quay lại. Chưa thử undo hay trạng thái bị gián đoạn. |
| 4 | Nhất quán và chuẩn | 3 | Cyan/navy, bảng, trạng thái và navigation nhất quán ở hai theme; cột Thao tác lặp trạng thái đã hoàn tất. |
| 5 | Ngăn lỗi | 2 | Nguồn chỉ render hành động phù hợp trạng thái; chưa có disable/loading tại nút bắt đầu; chưa kiểm chứng phản hồi submit. |
| 6 | Nhận biết thay vì ghi nhớ | 3 | Tên bệnh nhân và thao tác có chữ; trên mobile danh tính và hành động nằm ở hai phía bảng cuộn ngang. |
| 7 | Linh hoạt và hiệu suất | 1 | Target không có tìm kiếm/lọc trạng thái/lối tắt gọi người tiếp theo; chưa thấy accelerator chuyên môn. |
| 8 | Thẩm mỹ và tối giản | 2 | Desktop sạch và đọc được; KPI lớn lấn nội dung mobile, mascot và trạng thái lặp thêm nhiễu. |
| 9 | Nhận biết, chẩn đoán, phục hồi lỗi | 2 | Nguồn có thông báo gián đoạn nhưng chưa giải thích dữ liệu còn tin cậy tới đâu hay nên làm gì. Lỗi chưa được kích hoạt. |
| 10 | Trợ giúp và tài liệu | 1 | Có số hỗ trợ; không thấy hướng dẫn tác vụ cho bác sĩ. Nội dung bubble trong nguồn hướng sang đặt lịch bệnh nhân. |

## Cognitive load — 5/8 thất bại theo checklist

| Mục | Kết quả | Nhận định |
|---|---|---|
| Single focus | Fail | Mobile viewport đầu dành cho header và 4 KPI; queue dưới fold, mascot vẫn hiện. |
| Chunking ≤4 | Fail | Một hàng có 6 cụm cột; hai cột cuối cùng lặp cùng thông tin hoàn tất. Không phải 6 lựa chọn hành động. |
| Grouping | Pass | Thông tin bệnh nhân, khung giờ và trạng thái nằm theo cụm có cấu trúc. |
| Visual hierarchy | Fail | Desktop ổn; mobile phần việc cần xử lý bắt đầu y≈887 trên viewport cao 844. |
| One thing at a time | Pass | Nguồn thể hiện một hành động phù hợp trạng thái mỗi hàng. |
| Minimal choices | Pass | Không quan sát điểm quyết định có >4 lựa chọn hành động trực tiếp. |
| Working memory | Fail | Mobile phải giữ tên/STT trong đầu khi cuộn sang trạng thái/thao tác; đây là suy luận từ cấu trúc nhìn thấy, chưa đo hành vi người dùng. |
| Progressive disclosure | Fail | Mã hẹn dài, hồ sơ đã xong và phần thao tác lặp luôn hiện; chưa có thu gọn/phân nhóm theo việc đang cần làm. |

Không có điểm quyết định >4 lựa chọn được quan sát: sidebar bác sĩ có 3 liên kết có nhãn trong hai nhóm; toolbar queue có 1 nút refresh; 2 hàng demo đã hoàn tất không có CTA hành động. Nếu hàng chờ tương lai có >4 bệnh nhân CHECKED_IN, việc chọn ai khám là >4 lựa chọn do mọi hàng cùng render; đó là điều kiện từ nguồn, chưa xảy ra trong dataset đang xem.

## Hành trình cảm xúc

Vào desktop: trật tự và tông cyan/navy tạo cảm giác chuyên nghiệp; tên bác sĩ/chuyên khoa xác nhận đúng phòng khám. Lúc xác định việc tiếp theo: KPI “0 chờ” nhưng bảng “Hàng chờ” chứa hai ca xong khiến người mới phải giải nghĩa. Mobile: phải vượt qua bốn thẻ số rồi cuộn bảng làm tăng cảm giác bị chậm lại. Kết thúc ca: hai badge “Đã hoàn tất” tạo tín hiệu đóng việc nhưng không đưa đường quay lại hồ sơ tại hàng; đây là kết thúc được quan sát trong danh sách, không phải trải nghiệm lưu ca. Rủi ro cảm xúc khi mất kết nối chỉ suy luận từ copy nguồn: bác sĩ không biết độ mới của danh sách.

## Điểm mạnh

1. Ngôn ngữ nghiệp vụ và phân quyền trực quan đúng sản phẩm; bác sĩ, chuyên khoa, STT và triệu chứng nằm gần nhau.
2. Badge trạng thái có chữ và icon, không phụ thuộc màu; dark/light đều giữ phân cấp nội dung chính tương đối tốt.
3. Navigation gọn, active rõ; nguồn có skip-link, aria-current và nhãn cho refresh/theme/menu. Không có dấu hiệu phải tái thiết kế nhận diện.

## Priority issues

### A1 — P1: Mobile đẩy việc khám ra khỏi màn hình và tách danh tính khỏi thao tác

Quan sát: queue bắt đầu y≈886.8; bảng bắt đầu y≈1020.9, rộng 783px trong vùng 356.8px. Viewport 390×844 chỉ cho thấy KPI, không thấy queue. Nguồn: desk.html:31–90 dùng 4 `col-sm-6` nên xuống một cột dưới 576px; CSS:1949 padding KPI 1.5rem; CSS:2186 đặt min-width bảng 760px. Hành động ở desk.html:175–188 thuộc cột ngoài màn hình. Chưa có nút bắt đầu/tiếp tục đang render nên chưa kiểm thử thao tác đó.

Fix: KPI compact 2×2 hoặc một hàng tóm tắt; queue trước phần thống kê trên mobile; mobile patient card giữ STT+tên+trạng thái+CTA trong cùng một khối. Nếu bắt buộc giữ bảng, cần cột danh tính sticky và tín hiệu cuộn ngang rõ. Giữ desktop và identity hiện tại. Command phù hợp: `$impeccable adapt` và `$impeccable layout`. Đây là khó khăn lớn trên mobile, không phải P0; tài liệu hiện tại ưu tiên desktop/tablet cho portal.

### A2 — P2: “Hàng chờ” chứa ca đã xong, thiếu lớp tác vụ ưu tiên

Quan sát: KPI chờ=0 nhưng bảng mang tên hàng chờ chứa 2 ca hoàn tất; mỗi hàng lặp “Đã hoàn tất” ở cả Trạng thái và Thao tác. Nguồn desk.html:96,130 render mọi `todayQueue`; :190–192 lặp badge; toàn toolbar :94–105 không có lọc/tìm. Dataset 2 hàng chưa chứng minh hiệu suất ở ca đông; tác hại khi 20+ ca là suy luận.

Fix: mặc định nhóm “Cần xử lý” (chờ/đang khám), “Đã hoàn tất” thu gọn có tổng số; empty khi không còn việc phải nói “Chưa có bệnh nhân đang chờ” thay vì tạo cảm giác mất dữ liệu; cột Thao tác của ca đã xong để trống hoặc đưa “Xem bệnh án” nếu sản phẩm hỗ trợ đúng route. Giữ hiển thị ngày tổng thể, thêm tìm theo tên/STT khi thực sự cần. Command: `$impeccable distill`, `$impeccable clarify`.

### A3 — P2: Trạng thái realtime nói về kết nối thay vì độ mới của hàng chờ

Quan sát: “Realtime đang hoạt động” live. Nguồn desk.html:231,233–235,241,244,249 dùng realtime/client và reload toàn trang sau 450ms; không có “cập nhật lần cuối”. Đứt kết nối, reload bất ngờ và tác động tới focus chưa được kích hoạt; đây là rủi ro từ nguồn, không phải lỗi đã nhìn thấy.

Fix: “Tự động cập nhật · lần cuối 10:32”; khi gián đoạn “Chưa nhận cập nhật mới. Danh sách lúc 10:32. Làm mới”; giải thích rõ tiếp tục dùng danh sách tới đâu. Ưu tiên cập nhật hàng thay vì reload cả trang, giữ focus/scroll và thông báo nhẹ khi có bệnh nhân mới. Command: `$impeccable clarify`, `$impeccable harden`.

### A4 — P2: Mascot cố định cạnh tranh không gian tác vụ, trợ giúp không đúng vai trò

Quan sát desktop: mascot/status pill phủ phần số hỗ trợ ở footer; mobile chiếm vùng x≈237–374,y≈706–828 trên KPI. Nguồn CSS:1021–1024 cố định, z-index1050; :1051 và :1071 float/pulse vô hạn. Có reduced-motion chung trong layout, nên không kết luận thiếu chế độ giảm chuyển động. Bubble chưa mở live; nguồn fragments/mascot.html:13–17 chào giúp đặt lịch/kết nối bác sĩ, CTA `/patient/booking` dù portal bác sĩ.

Fix: giữ An Nhiên như identity, chuyển launcher gọn ở topbar/trợ giúp, có thu gọn theo vai trò; bubble dành cho bác sĩ hướng dẫn hàng chờ và hỗ trợ hệ thống. Motion chỉ khi thông báo/nhấn; đảm bảo launcher không đè footer hay dữ liệu ở mọi breakpoint. Command: `$impeccable quieter`, `$impeccable onboard`, `$impeccable adapt`.

Không có P0 được xác nhận. P1=1, P2=3. Không suy đoán rằng toàn bộ khám/kê đơn không hoạt động.

## Persona red flags

- Alex, bác sĩ dùng nhiều lần mỗi ngày: không có tìm/lọc chờ, không có thao tác tới bệnh nhân tiếp theo; ca đã xong vẫn trong danh sách. Tải cao là suy luận, chưa stress-test.
- Sam, dùng bàn phím/low vision: AX tree đọc cả ký tự icon trang trí trong heading/sidebar/cell; nguồn các icon thiếu aria-hidden ở nhiều nơi. Chưa thử screen reader nên không khẳng định cách NVDA phát âm. Mobile cần đọc và theo dõi hai chiều; chưa thử zoom 200% hoặc keyboard cuộn bảng.
- Casey, kiểm tra hàng chờ bằng điện thoại: không thấy hàng chờ trong viewport đầu, CTA tương lai phải tìm ở cột phải; mascot nằm trong vùng chạm dưới. Không kiểm tra mạng chậm hay state persistence.

## Minor observations

- P3: KPI có hover lift CSS:1956–1959 nhưng thẻ là div không tương tác; cân nhắc bỏ lift hoặc làm lọc trạng thái thật, tránh affordance giả.
- P3: mã hẹn dài bẻ dòng trên desktop/mobile, cạnh tranh với khung giờ; nó ít phục vụ quyết định gọi khám hơn STT và tên, nên chuyển sang secondary detail.
- Tiêu đề rất dài, cùng nhắc bác sĩ và hàng chờ hôm nay rồi lặp ở tên bảng; có thể rút “Bàn khám hôm nay” và dùng dòng hỗ trợ cho phần việc tiếp theo. Không cần sửa nhận diện/copy chuyên môn lớn.
- Nguồn examine có patient/history ở cột trái, 3 nhóm form và confirmation khi hoàn tất — tín hiệu cấu trúc tốt, nhưng chưa quan sát live nên không cộng bằng chứng toàn luồng.

## Câu hỏi định hướng cho synthesis

1. Bàn khám nên mặc định cho thấy “việc đang cần làm”, hay nhật ký cả ngày? Hai chọn lựa dẫn tới IA khác nhau.
2. Mobile có phải thiết bị bác sĩ cần xử lý ca, hay chỉ xem nhanh? Nếu chỉ xem nhanh, một preview queue gọn có thể đủ thay cho mọi cột desktop.
3. An Nhiên trong portal có cần trợ giúp tác vụ bác sĩ hay chỉ giữ nhận diện? Giữ nhân vật nhưng giảm hiện diện có thể bảo toàn cảm giác thân thiện mà tăng tập trung.
