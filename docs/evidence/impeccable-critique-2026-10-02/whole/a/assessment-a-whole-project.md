# Assessment A — MedClinic UX/UI toàn dự án

Ngày đánh giá: 02/10/2026 · Phạm vi: 30 template Thymeleaf, layout/fragment dùng chung và `static/css/medclinic-design-system.css`; không sửa mã.

## Kết luận về tính đặc thù thiết kế

MedClinic có ngôn ngữ nhận diện khá rõ cho một phòng khám: tông cyan/deep teal trên nền slate, ảnh cơ sở y tế, hồ sơ bác sĩ, lịch hẹn 30 phút, phiếu QR và luồng cấp số tại quầy. Đây là chi tiết gắn với nghiệp vụ, không phải bộ giao diện SaaS có thể đổi logo là dùng được. Trang chủ và trang đặt lịch cho thấy ý đồ chăm sóc bệnh nhân rõ ràng; mẫu bác sĩ và nhân viên lại chuyển sang giao diện vận hành Bootstrap quen thuộc, vì vậy sản phẩm chưa giữ cùng một mức độ hoàn thiện qua các vai trò.

Bản sắc thị giác là **có chủ đích nhưng chưa liền mạch toàn sản phẩm**. `layout/main.html` và `layout/dashboard.html` dựng hai vỏ ứng dụng khác nhau, còn nhiều template ghép lớp Bootstrap và quy tắc riêng. CSS chung cung cấp token và trạng thái sáng/tối (`medclinic-design-system.css:3-55`), nhưng một số điều chỉnh vẫn nằm inline trong layout/auth hoặc ngay trong trang. Cơ hội lớn nhất là thống nhất các primitive vận hành và giải quyết khả năng sử dụng trên màn hình hẹp, nhất là bảng tại quầy.

## Ấn tượng chung

Phần bệnh nhân mang lại cảm giác phòng khám hiện đại và giải thích khá rõ cách đặt lịch, nhận QR rồi đến quầy. Trang booking có chia nhóm và tóm tắt lựa chọn bên cạnh form. Cảm giác giảm mạnh khi chuyển sang màn hình nội bộ: trên điện thoại bảng dữ liệu buộc kéo ngang; bộ lọc quầy đưa sáu trạng thái lên cùng một hàng quyết định; khi role không được phép vào Admin, trình duyệt hiển thị trang lỗi Whitelabel tiếng Anh.

### Chấm điểm sức khỏe thiết kế

| # | Heuristic | Điểm | Bằng chứng chính |
|---|---|---:|---|
| 1 | Visibility of System Status | 3/4 | Booking có trạng thái tải/trống/lỗi và thông báo lịch (booking:131-158); receptionist có trạng thái hàng đợi. Chưa quan sát trạng thái hoàn tất giao dịch thực tế. |
| 2 | Match System / Real World | 3/4 | Dùng thuật ngữ quen thuộc với phòng khám: chuyên khoa, ca 30 phút, QR, số thứ tự, bệnh án. Một số portal còn dùng nhãn nội bộ và từ “Realtime” mà không giải thích. |
| 3 | User Control and Freedom | 2/4 | Có quay lại, hủy lịch, đổi theme và thử tải lại; không phải luồng nào cũng cho xem/chỉnh bước trước khi hoàn tất, và thao tác hủy nằm sau xác nhận. |
| 4 | Consistency and Standards | 2/4 | Nút, thẻ và bảng có primitive chung; hai layout và nhiều quy tắc riêng tạo độ lệch rõ giữa website và portal. |
| 5 | Error Prevention | 2/4 | Trường bắt buộc, giới hạn đầu vào và trạng thái disabled giúp tránh thiếu dữ liệu; chưa thấy hướng dẫn lỗi nhất quán ở toàn bộ form hoặc bước kiểm tra trước các thao tác nhạy cảm. |
| 6 | Recognition Rather Than Recall | 3/4 | Breadcrumb, label, tóm tắt booking, trạng thái có chữ và các shortcut giúp nhận diện tác vụ. Một số trang quản trị vẫn dựa vào bảng và icon hành động nhỏ. |
| 7 | Flexibility and Efficiency | 1/4 | Không thấy lối tắt bàn phím hay thao tác hàng loạt nổi bật; các tác vụ CRUD chủ yếu mở từng modal hoặc xử lý từng dòng. |
| 8 | Aesthetic and Minimalist Design | 3/4 | Hero và thẻ bác sĩ được tạo tác tốt; trang chủ dài, carousel và mascot cùng tranh chú ý, portal bảng vẫn nặng thông tin. |
| 9 | Error Recovery | 2/4 | Có thông báo lỗi và nút thử lại ở booking; lỗi truy cập Admin trong phiên thử lại rơi vào trang Spring Whitelabel tiếng Anh, không có đường quay lại hữu ích. |
| 10 | Help and Documentation | 1/4 | Có hotline và mascot An Nhiên; thiếu trợ giúp theo tác vụ tại màn hình Admin, khám bệnh và thu ngân. |
| **Tổng** |  | **22/40 — Cần cải thiện đáng kể** | **55%: Acceptable theo thang Impeccable.** |

## Tải nhận thức

Đánh giá theo checklist 8 mục của Impeccable; kết luận dựa trên nguồn và những màn hình quan sát được.

| Mục | Kết quả | Lý do |
|---|---|---|
| Single focus | **Trượt** | Trang chủ ghép hero tìm kiếm, thẻ quy trình, sáu tiện ích, carousel, chỉ số, chuyên khoa, bác sĩ, quy trình và mascot; người mới phải quyết định giữa nhiều đường vào. |
| Chunking | Đạt | Booking nhóm chuyên khoa/bác sĩ, ngày/giờ và lý do theo các phần đánh số. |
| Grouping | Đạt | Card, panel, bảng và nhóm trường thường có ranh giới thị giác. |
| Visual hierarchy | Đạt | Heading, nút đặt lịch và tóm tắt phân cấp khá rõ; trang portal giữ title và bảng làm trọng tâm. |
| One thing at a time | Đạt | Booking lộ dần khung giờ sau khi chọn bác sĩ/ngày; có trạng thái loading/empty/error riêng. |
| Minimal choices | **Trượt** | Quầy tiếp nhận hiển thị 6 tab trạng thái cùng lúc (`staff/reception.html:87-118`); chuyên khoa cũng có 6 mục. Hai nhóm vượt ngưỡng 4 lựa chọn tại một điểm quyết định. |
| Working memory | Đạt | Booking giữ lựa chọn hiện tại trong tóm tắt; nhân viên nhìn số lượng lịch theo trạng thái ngay cạnh nhãn. |
| Progressive disclosure | Đạt có điều kiện | Modal thêm/sửa và accordion lịch sử giữ một phần chi tiết khỏi màn hình ban đầu; bảng danh sách vẫn dày và có thể cần sắp xếp lại. |

Các quyết định có hơn 4 lựa chọn cần xử lý: **trạng thái lễ tân (6)**, **chuyên khoa (6)**, và danh mục điều hướng gộp theo vai trò trong layout portal. Bốn shortcut tài khoản demo trên login không vượt ngưỡng nhưng là nội dung phát triển, cần đảm bảo không xuất hiện trong môi trường người dùng thật (`auth/login.html:157-162`).

## Điểm mạnh

- Luồng bệnh nhân giải thích trước cách chọn bác sĩ/khung giờ, nhận phiếu QR, check-in và tra cứu hồ sơ. Trang chủ có nhiều đường vào nhưng vẫn gắn với đúng mục tiêu chăm sóc sức khỏe (`home/index.html:222-341, 674-742`).
- Booking bảo toàn ngữ cảnh bằng tóm tắt chuyên khoa, bác sĩ, ngày, giờ và phí; trạng thái dữ liệu bất đồng bộ được diễn đạt qua các prompt, tải, trống và lỗi (`patient/booking.html:47-220`).
- Nhiều thành phần vận hành có nền tốt: breadcrumb, skip link, focus-visible, label cho form, reduced motion, live region và các token theme xuất hiện ở layout/CSS dùng chung (`layout/main.html:90-96`, `layout/dashboard.html:64-73`, `medclinic-design-system.css:3-55`). Đây là nền thuận lợi để chuẩn hóa.

## Vấn đề ưu tiên

### [P1] Bảng nội bộ cần kéo ngang trên điện thoại

**Bằng chứng:** CSS ép `.mc-data-table` rộng tối thiểu 760px ở breakpoint mobile (`static/css/medclinic-design-system.css:2168-2188`); reception và bàn bác sĩ bọc bảng trong `.table-responsive` (`staff/reception.html:157-171`, `doctor/desk.html:117-130`), các bảng Admin dùng cùng primitive (`admin/doctors.html:36-50`, `admin/medicines.html:41-56`). Ở 390px người dùng phải cuộn ngang để xem cột và hành động.

**Vì sao:** Lễ tân cần tìm đúng lịch và check-in liên tục; bác sĩ cần đọc hàng đợi. Hành động quan trọng nằm ở các cột cuối nên rất dễ khuất hoặc mất ngữ cảnh khi kéo.

**Hướng sửa:** Dùng bố cục thẻ trên mobile cho các dòng nghiệp vụ, ghim tên/trạng thái/hành động, hoặc mở chi tiết dòng theo progressive disclosure. Chỉ giữ các cột hỗ trợ trong vùng cuộn. Xác nhận ở 390px và zoom trình duyệt trước khi kết luận về khả năng đọc.

**Impeccable:** `$impeccable adapt`, sau đó `$impeccable layout`.

### [P1] Lỗi quyền truy cập không có đường phục hồi dễ hiểu

**Bằng chứng:** Khi role demo hiện tại là bác sĩ mở `/admin/dashboard`, live app trả 403 và màn hình “Whitelabel Error Page” bằng tiếng Anh, kèm dòng “This application has no explicit mapping for /error”. Template app có trang lỗi thân thiện (`error/error.html:8-21`), nhưng không được dùng cho trang 403 vừa quan sát.

**Vì sao:** Nhân viên không biết lỗi do quyền, phiên hết hạn hay hệ thống; họ không có nút trở lại vùng phù hợp. Đây là điểm kết thúc lạnh sau một lần điều hướng.

**Hướng sửa:** Đưa lỗi 403/404/500 vào cùng vỏ MedClinic, nói rõ “Tài khoản này không có quyền mở trang này”, thêm hành động về trang theo vai trò và đăng nhập bằng tài khoản phù hợp. Giữ chi tiết kỹ thuật trong log.

**Impeccable:** `$impeccable harden`, `$impeccable clarify`.

### [P2] Sáu bộ lọc trạng thái ở quầy cùng tranh tầm nhìn

**Bằng chứng:** Reception hiển thị `Tất cả`, `Đã xác nhận`, `Đã check-in`, `Đang khám`, `Hoàn tất`, `Đã hủy` cùng số đếm thành sáu tab (`staff/reception.html:87-118`). Trên màn hình hẹp, CSS chỉ cho cuộn ngang (`static/css/medclinic-design-system.css:2653-2659`).

**Vì sao:** Người trực quầy cần lọc nhanh theo tiến độ hiện tại. Sáu nhãn ngang tăng thời gian quét, và trạng thái “Đã hủy” chiếm độ nổi ngang với các bước làm việc đang diễn ra.

**Hướng sửa:** Đưa nhóm thường dùng vào 3–4 tab chính theo hành động trong ca; chuyển các trạng thái ít dùng vào menu “Khác” hoặc bộ lọc dropdown. Giữ số đếm gắn nhãn và ghi nhớ bộ lọc khi tìm kiếm.

**Impeccable:** `$impeccable distill`, `$impeccable adapt`.

### [P2] Khung giao diện chung phân nhánh quá nhiều

**Bằng chứng:** `layout/main.html` có inline token và style riêng; `layout/dashboard.html` khai báo lại theme bootstrapper, font và portal shell; một số auth view tự khai báo nhiều override dark mode (ví dụ `auth/login.html:10-99`). CSS chung đã có token và dark selector (`medclinic-design-system.css:3-55, 224-291`) nhưng các lớp Bootstrap như `text-dark` cần được ghi đè toàn cục.

**Vì sao:** Khi chỉnh màu, tương phản và trạng thái, cùng một primitive phải được theo dõi ở CSS chung, layout và template. Điều này làm các màn hình có thể lệch sáng/tối dù cùng một thành phần.

**Hướng sửa:** Gom biến và component dùng chung vào design system; giữ khác biệt portal ở token/variant có tên; thay inline override lặp lại bằng lớp semantic. Đo tương phản cho cặp màu cụ thể trước khi đưa ra tuyên bố WCAG.

**Impeccable:** `$impeccable extract`, `$impeccable document`, `$impeccable polish`.

### [P2] Widget điều dưỡng có hai nút mở cùng một lời nhắn

**Bằng chứng:** Fragment mascot đặt launcher hoạt họa và nút trạng thái “Điều dưỡng An Nhiên” cùng điều khiển `mascotBubble` (`fragments/mascot.html:4-39`); screenshot homepage cho thấy cả mascot nổi và pill trắng hiện đồng thời ở góc dưới phải.

**Vì sao:** Trên góc nhìn desktop hai affordance tranh diện tích với nội dung; ở mobile chúng có khả năng phủ nút/chi tiết phía dưới. Người dùng cũng không rõ nút nào là lối vào chính.

**Hướng sửa:** Gộp thành một launcher có nhãn dễ nhận biết; chỉ hiện bubble sau thao tác hoặc khi thông điệp thật sự có ích; kiểm tra vùng chạm và phần nội dung bị che ở 390px.

**Impeccable:** `$impeccable distill`, `$impeccable adapt`.

## Persona và cờ đỏ

**Bệnh nhân đặt lịch lần đầu:** Từ homepage họ dễ tìm bác sĩ, xem chuyên khoa và bắt đầu booking. Điểm cần cải thiện là trang chủ dài, có nhiều đường đi cạnh nhau; ở booking, sau bước chọn ngày/bác sĩ, người dùng cần hiểu vì sao chưa có giờ trống và liệu lựa chọn đã được lưu. Tóm tắt booking là điểm tựa tốt. Chưa quan sát submit/confirmation nên chưa khẳng định chất lượng bước hoàn tất.

**Bác sĩ / lễ tân trong ca khám:** Portal có bàn khám, lịch trực, hàng đợi, tìm kiếm, tab trạng thái và nút thao tác; đây là cấu trúc phù hợp việc vận hành. Trên điện thoại, bảng rộng 760px làm cột thao tác khuất. Lễ tân phải phân biệt sáu trạng thái cạnh nhau; bác sĩ điền triệu chứng, chẩn đoán, thuốc, liều/tần suất, lời dặn và ngày tái khám trong một form dài (`doctor/examine.html:104-199`). Hãy tách vùng kê đơn thành nhóm rõ ràng, giữ tóm tắt bệnh nhân và xác nhận bản nháp trước lưu.

**Admin / người dùng hỗ trợ công nghệ tiếp cận và mobile:** Các menu quản trị có đường dẫn và breadcrumb, nhưng bảng CRUD dùng cùng quy tắc kéo ngang trên mobile, các hành động từng dòng nhỏ và không có thao tác lô nổi bật. Người dùng keyboard có cơ sở ban đầu từ skip-link/focus-visible và label, song đánh giá này không thực hiện kiểm tra bàn phím/AT hoặc đo tương phản, nên chưa thể xác nhận conformance. Role khác Admin nhận lỗi 403 không có hướng dẫn tiếp tục.

## Quan sát nhỏ

- Trang chủ nêu cả “3 bước” ở thẻ quy trình hero và “4 bước” ở phần quy trình phía dưới (`home/index.html:246-293, 674-722`); nên thống nhất số bước để người đọc không tự hỏi có hai quy trình.
- Các trường tìm kiếm và filter thường có label thật; tuy nhiên có trang dùng icon-font trong nội dung accessibility tree, nên cần kiểm tra tên truy cập của từng nút ở lượt audit chuyên biệt.
- Lịch sử giao dịch kho có bảng dài nhưng rỗng có thông điệp hữu ích (`admin/medicines.html:197-206`).
- Walk-in có autocomplete ở tên/điện thoại và chia phần thông tin người đến khám với lịch khám (`staff/walk-in.html:35-139`).

## Câu hỏi mở cho lần cải thiện

1. Có thể thu gọn mọi trạng thái lễ tân thành ba bước làm việc chính, để ưu tiên thao tác của ca trực không?
2. Mobile có phải thiết bị vận hành thường dùng của bác sĩ/lễ tân không? Nếu có, hãy đặt thẻ dòng và hành động chính làm cấu trúc mặc định ở 390px.
3. Widget An Nhiên cần luôn nổi, hay nên xuất hiện theo ngữ cảnh khi bệnh nhân cần trợ giúp?

## Phạm vi và mức độ chắc chắn

Đã liệt kê và rà cấu trúc nguồn đủ 30 template: admin (5), auth (6), doctor (3), doctors (2), home (1), patient (4), staff (4), layout (2), fragments (2), error (1), cùng CSS token/component dùng chung. Live demo quan sát trực tiếp: homepage (ảnh + cây accessibility), booking (ảnh + cây accessibility), và doctor desk (cây accessibility). Route Admin được mở trong phiên demo role bác sĩ và hiển thị 403 Whitelabel, nên chỉ đánh giá lỗi truy cập; không nhìn được dashboard Admin thật. Không truy cập live reception/billing, patient appointments/history hoặc examination; các nhận xét đó suy ra từ template/CSS. Không xem qua màn hình đăng nhập hoặc đăng ký trong phiên này.

Browser CUA chỉ cung cấp viewport mặc định (ảnh homepage/booking khoảng 1280×720); không đặt được 1440×900 hay 390×844 trong lần đánh giá này. Theme ban đầu là dark do cấu hình thiết bị; không đổi nên theme được giữ nguyên. Các ảnh homepage và booking được xem inline trong phiên, nhưng không thể xuất bytes ảnh qua giao diện CUA sang file trong evidence folder; do đó **chưa có tệp screenshot được lưu**. Màn doctor desk không chụp ảnh vì DOM hiển thị tên và số điện thoại mẫu, dù ghi rõ dữ liệu demo. Đây là giới hạn chứng cứ, không phải kết luận rằng màn hình mobile hoặc light theme đã qua đánh giá.
