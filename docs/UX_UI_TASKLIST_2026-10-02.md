# MedClinic — UX/UI tasklist toàn dự án

Ngày lập: 02/10/2026 <br>
Nguồn: Impeccable critique toàn bộ 30 template, CSS dùng chung, kiểm tra detector và những route truy cập được trong phiên demo. Cập nhật triển khai 02/10/2026: danh sách có 16 task. UX-17 trước đây là lỗi đánh số, đã thống nhất thành UX-04. Nhận xét và tiêu chí gốc được giữ để đối chiếu; bảng cập nhật cuối tài liệu là trạng thái hiện tại.

## P1 — Ưu tiên trước

- [x] **UX-01 · Dùng được bảng nghiệp vụ trên màn hình hẹp.** Phạm vi: hàng đợi lễ tân, bàn bác sĩ và bảng Admin/billing dùng `.mc-data-table`. Từ 390px, cột hành động hiện nằm xa bên phải trong bảng tối thiểu 760px; người dùng phải kéo ngang và dễ mất ngữ cảnh bệnh nhân/hóa đơn. Chuyển hàng thành card gọn hoặc ghim thông tin nhận diện và hành động chính; nếu vẫn cuộn ngang, giữ tiêu đề/cột khóa hợp lý, có chỉ báo cuộn và hỗ trợ bàn phím. **Nghiệm thu:** tại 390px người dùng đọc được bệnh nhân + trạng thái + hành động chính mà không phải dò nhiều màn; không có tràn trang; cuộn ngang có tên truy cập/điều khiển bàn phím; kiểm tra zoom 200% và desktop.
- [x] **UX-02 · Thay lỗi Whitelabel bằng trang lỗi có hướng thoát.** Phạm vi: 403/404/500, phiên hết hạn. Trong phiên bác sĩ, `/admin/dashboard` và `/patient/appointments` trả trang Spring Whitelabel tiếng Anh; `/staff/walk-in` cũng bị chặn tương tự. Đã nối lỗi quyền từ security filter sang trang MedClinic 403 và thêm CTA theo vai trò. **Còn nghiệm thu:** xác nhận cả role hợp lệ/không hợp lệ, trang branding, 404/500 và đường đăng nhập khi phiên hết hạn; không lộ chi tiết nội bộ.
- [x] **UX-03 · Sửa tương phản chữ trạng thái đã hoàn tất.** Badge “Hoàn tất” trên bàn bác sĩ dùng #059669 trên #ecfdf5, chữ 12.8px, tương phản đo được 3.577:1. Đã đổi màu chữ component `.bg-success-subtle` sang #047857 (5.484:1 trên cùng nền). **Còn nghiệm thu:** xác nhận màu thực tế ở cả dark/light và rà các badge cùng component; giữ nhãn văn bản, không chỉ màu.

## P2 — Cải thiện luồng và tính nhất quán

- [x] **UX-05 · Giảm độ nặng của sáu bộ lọc lễ tân.** Gộp sáu tab hiện tại thành nhóm trạng thái thường dùng trong ca; đưa “Hoàn tất/Đã hủy” vào “Khác” hoặc bộ lọc. Giữ số đếm gắn với nhãn và bộ lọc đang chọn. **Nghiệm thu:** nhân viên tìm nhanh lịch cần check-in/đang xử lý; mobile không buộc cuộn ngang qua dải tab; thứ tự bàn phím hợp lý.
- [x] **UX-06 · Thống nhất hai shell và hệ token.** `layout/main.html` và `layout/dashboard.html` đang khai báo hai vỏ, font/theme và override khác nhau; một số auth page còn CSS dark mode riêng. **Nghiệm thu:** màu, kiểu chữ, focus, trạng thái, spacing và điều khiển theme đến từ token/component có tên; khác biệt portal là variant có chủ đích; trang mẫu ở mỗi role hiển thị thống nhất trong dark/light.
- [x] **UX-07 · Đưa theme qua kiểm tra tương phản và trạng thái trên mọi role.** Detector báo cặp 1.1:1 trong dashboard layout; đây cần xác minh trên giao diện render vì lớp Bootstrap/selector theme có thể ghi đè. **Nghiệm thu:** kiểm tra cặp màu thực tế ở trang bệnh nhân, staff, doctor, admin; mọi chữ thường đạt 4.5:1 và chữ lớn 3:1; focus/disabled/error/loading vẫn nhận biết trong cả hai theme; lưu bảng đo trước/sau.
- [x] **UX-08 · Làm rõ luồng chính trang chủ.** Trang chủ dài và có nhiều khối/đường vào; phần quy trình ghi “3 bước” ở khu vực đầu nhưng “4 bước” ở phần sau. Rút gọn thứ tự ưu tiên cho mục tiêu đặt lịch, thống nhất số bước và kiểm soát mật độ carousel/tiện ích. **Nghiệm thu:** nội dung quy trình nhất quán; đường đặt lịch dễ nhận ra ở đầu trang; không lặp thông điệp; kiểm tra mobile và desktop.
- [x] **UX-09 · Hợp nhất widget An Nhiên.** Fragment mascot đồng thời hiện launcher hoạt họa và pill “Điều dưỡng An Nhiên” cùng mở một bubble; widget che một phần nội dung/footer khi cuộn. **Nghiệm thu:** một launcher duy nhất, bubble chỉ mở theo thao tác/ngữ cảnh hữu ích, không che nút/nội dung ở 390px, có nhãn truy cập và điều khiển bàn phím.
- [x] **UX-10 · Chia nhóm form khám để hỗ trợ đọc và rà soát trước lưu.** `doctor/examine.html` gom triệu chứng, chẩn đoán, thuốc, liều/tần suất, dặn dò và ngày tái khám trong một form dài. Đây là nhận xét từ nguồn template, chưa được thao tác trực tiếp trong phiên critique ban đầu. **Nghiệm thu:** nhóm khám/chẩn đoán/đơn thuốc/hướng dẫn rõ ràng; thông tin bệnh nhân và trạng thái bản nháp luôn hiện; có bước rà soát trước lưu và xác nhận dữ liệu đã lưu; test keyboard, mobile và lỗi validation. Đã chốt lưu qua browser 03/10/2026; bằng chứng trong `UX10_ACCEPTANCE_2026-10-03.md`.
- [x] **UX-11 · Loại bỏ shortcut tài khoản demo khỏi trải nghiệm production.** Login đang hiển thị bốn lựa chọn tài khoản demo. **Nghiệm thu:** shortcut chỉ có trong cấu hình demo/dev có chủ đích; build production không hiển thị thông tin đăng nhập mẫu; luồng lỗi đăng nhập vẫn hướng dẫn khôi phục tài khoản.
- [x] **UX-12 · Kiểm tra quy tắc ảnh, tên truy cập và vùng chạm.** Các trang dùng icon/font icon và nút menu có thể cần tên accessible; nút menu mobile đo rộng 37.46px trong lượt quan sát trước. Đây là hạng mục kiểm chứng/tinh chỉnh, chưa kết luận vi phạm WCAG từ kích thước đơn lẻ. **Nghiệm thu:** kiểm tra bằng keyboard + accessibility tree; icon-only controls có accessible name; mục tiêu chạm đạt tối thiểu 44×44px khi phù hợp; không làm mất focus-visible.
- [x] **UX-04 · Hoàn thiện fallback ảnh/avatar sau khi xác minh render.** Detector ghi 16 kết quả ảnh, trong đó 15 là dương tính giả do Thymeleaf/ảnh QR/logo bind động. Còn `auth/profile.html` chỉ kiểm tra null và `patient/booking.html` có một `src=""` ẩn đến khi chọn bác sĩ; cần xác nhận lỗi tải ảnh/missing data trên trang đã render trước khi coi là bug. **Nghiệm thu:** ảnh có URL hợp lệ hoặc fallback có chủ đích (initials/placeholder); lỗi tải không tạo icon hỏng; alt text phù hợp; test thiếu URL, chuỗi rỗng và ảnh lỗi.

## P3 — Hoàn thiện và vận hành chất lượng

- [x] **UX-13 · Rà lại empty/loading/error/success theo từng vai trò.** Ưu tiên booking, lịch bệnh nhân, hàng đợi, billing, bank payment, quản lý thuốc/người dùng. **Nghiệm thu:** mỗi trạng thái có câu chữ nêu điều đang xảy ra và hành động tiếp theo; không hiển thị trạng thái rỗng như lỗi; thao tác thanh toán/lưu có kết quả rõ và tránh gửi lặp. Các trạng thái giao dịch chưa được kiểm thử trong lượt audit.
- [x] **UX-14 · Chuẩn hóa format tiền và hóa đơn trên toàn app.** Dùng một formatter VND không có `.00`, phân tách hàng nghìn nhất quán và thể hiện rõ từng khoản/tổng. **Nghiệm thu:** cùng một giá trị hiển thị giống nhau ở booking, bill, thanh toán ngân hàng và lịch sử; kiểm thử số 0, null, giá trị lớn và in hóa đơn. Hạng mục này giữ trong backlog tổng thể để xác nhận đồng nhất sau thay đổi hóa đơn trước đây.
- [x] **UX-15 · Xác minh thao tác bàn phím và hỗ trợ người dùng.** Kiểm tra menu, bảng cuộn, modal CRUD, autocomplete, toast/live region, đổi theme và trợ giúp theo ngữ cảnh. **Nghiệm thu:** luồng chính có thể hoàn tất không dùng chuột; focus không bị kẹt/mất; dynamic updates được thông báo; có hỗ trợ ngắn ngay tại màn hình khám, tiếp nhận và thu ngân.
- [x] **UX-16 · Kiểm tra chuyển động, layout và nội dung trên thiết bị thực.** Có cảnh báo tĩnh về transition margin, glow và pulsing dot; phần lớn chưa đủ để kết luận là lỗi nếu chưa thấy hiệu ứng thực tế. **Nghiệm thu:** chuyển trang/sidebar không tạo tràn/cắt; animation tôn trọng `prefers-reduced-motion`; không dùng glow/pulse gây nhiễu; kiểm tra viewport 390px và desktop ở dark/light.

## Thứ tự đề xuất

1. UX-02 lỗi và phục hồi; UX-03 tương phản badge.
2. UX-01 bảng mobile và UX-04 fallback ảnh.
3. UX-05 lọc lễ tân; UX-06/07 chuẩn hóa shell và theme.
4. UX-08/09 trang chủ, mascot; UX-10 form khám; UX-11 login demo.
5. UX-12 đến UX-16 làm cổng nghiệm thu toàn sản phẩm.

## Độ phủ và ghi chú của critique trước triển khai

- Đã rà cấu trúc cả 30 template: Admin (5), auth (6), doctor (3), doctors (2), home (1), patient (4), staff (4), layouts (2), fragments (2), errors (1), cùng CSS design system.
- Quan sát trực tiếp trong browser: homepage, trang danh sách bác sĩ, booking, bàn bác sĩ; một số route ngoài role hiện tại trả 403. Không kiểm thử thao tác ghi dữ liệu, submit booking, lưu khám hay thanh toán.
- Chưa render trực tiếp từng trang Admin, reception, billing, examination, patient history/appointments trong role tương ứng; các mục liên quan được đánh dấu suy ra từ source hoặc cần xác minh.
- 46 detector findings (45 warnings, 1 recommendation) không được chép nguyên xi thành bug: 20 cảnh báo hierarchy và các cảnh báo về font/spacing/glow/transition có thể là false positive do template/layout/theme hợp thành. Chỉ dùng cảnh báo như tín hiệu cần xác minh. Trong 16 cảnh báo ảnh, 15 là false positive đã đối chiếu với binding; các trường hợp còn lại cần xác minh fallback thực tế.

## Lịch sử triển khai và nghiệm thu — trước lượt chốt 6 task

10 task đạt kiểm tra hiện tại; 6 task đã có thay đổi nhưng còn tiêu chí nghiệm thu. Dấu `[x]` không đồng nghĩa mọi tổ hợp màn hình/trạng thái đã đạt WCAG. Chi tiết và bằng chứng: `IMPECCABLE_IMPLEMENTATION_2026-10-02.md` và `IMPECCABLE_DEEP_ACCEPTANCE_2026-10-02.md`. Lượt chuyên sâu đã sửa validation/modal, identity form sửa, trạng thái ngân hàng, tương phản theo role và reduced motion. 6 task vẫn mở vì còn tiêu chí cần thiết bị/AT hoặc xác nhận thao tác ghi.

| Task | Thay đổi và kiểm tra hiện tại | Còn nghiệm thu |
|---|---|---|
| UX-01 | Bảng thành mục có nhãn dưới 768px, giữ form/nút và ARIA; nội dung dài/action toàn chiều rộng. Admin, staff, doctor có dữ liệu demo ở 390px không tràn ngang; action 44px. | Zoom/AT theo UX-15/16 |
| UX-02 | 403/404/500 có branding và hướng thoát; giữ HTTP status/JSON API, bỏ thông báo lặp. Browser 403 và regression 404/500 pass. | Phiên hết hạn dài hạn ngoài kiểm tra redirect |
| UX-03 | Badge success/primary/warning/danger/info và STT đặc đã chỉnh; computed colors + tỷ lệ sau sửa được ghi trong báo cáo. | Theme coverage theo UX-07 |
| UX-04 | Profile URL rỗng có initials, ảnh lỗi có fallback; booking bỏ src rỗng, giữ handler lỗi. Admin/booking không thấy ảnh hỏng. | Mạng ảnh lỗi trên thiết bị thật |
| UX-05 | Lễ tân có 3 filter chính + lịch sử; bác sĩ có tìm/lọc Cần khám/Hoàn tất/Tất cả, thời điểm dữ liệu và số kết quả. Browser và JS test pass. | — |
| UX-06 | Hai shell dùng token/font/theme chung, CSS shell tách file, gỡ dark auth trùng. Role pages đại diện + layout tests pass. | — |
| UX-07 | Sửa thêm sidebar, status Admin, lịch bác sĩ, hàng Đang khám reception dark, thông tin chứng chỉ và chữ trên pastel. 23 đường dẫn được render; 32 tổ hợp xác nhận không còn cảnh báo trong phạm vi detector, có JSON/số đo trước-sau. | Gradient/placeholder, hover/focus và toàn bộ trạng thái thực; không coi detector là chứng nhận WCAG |
| UX-08 | Thống nhất 4 bước; quy trình chỉ xuất hiện một lần mỗi breakpoint; tiện ích trong disclosure, copy bớt thuật ngữ. | — |
| UX-09 | Một launcher trong layout, copy/CTA theo role; hidden/inert khi đóng. Staff mobile: Escape đóng và trả focus đúng. | — |
| UX-10 | Context bệnh nhân/chưa lưu, cảnh báo rời, empty thuốc, rà soát đầy đủ và xác nhận theo ca. Demo 2 × 1 500 = 3 000 đ đúng, desktop/mobile pass. | Browser submit/cancel và kết quả lưu; auto-review chặn ghi bệnh án demo, đang chờ xác nhận riêng |
| UX-11 | Shortcut chỉ khi dev và không có prod. Tests dev/prod/dev+prod pass. | — |
| UX-12 | Nhãn theme/menu/icon-only, icon trang trí aria-hidden, label tạo Admin đúng ID Thymeleaf, search Admin có label, close modal 44px. Đã xem DOM/AX của ba modal. | Screen reader, zoom 200%, autofill và thiết bị thật; custom autocomplete không có trong nguồn, N/A |
| UX-13 | Thêm poll ngân hàng không chồng, timeout/thử lại, phiên hết hạn, PENDING/CONFIRMED/EXPIRED/REJECTED, ẩn QR/form cũ và fallback QR. Validation thuốc live mở modal/focus, giữ input; form sửa thuốc/chuyên khoa giữ ID. JS + Java render test pass. | UI lưu khám, provider thật, QR lỗi tải live và đầy đủ trạng thái CRUD |
| UX-14 | @money.vnd + MedClinic.money; formatter số chung với PDF, bỏ .00 và dùng dấu cách. Java/JS/PDF test 0/null/số lớn/phần lẻ pass. | — |
| UX-15 | Sidebar mobile Enter/Tab/Shift+Tab/Escape, modal thuốc vòng focus/Escape và server-error focus đã nghiệm thu; help/region/filter/live count giữ nguyên. Không có custom autocomplete. | Screen reader, keyboard workflow ghi dữ liệu, modal sửa/nhập kho và các trạng thái khác |
| UX-16 | Bỏ transition margin-left; bỏ blanket 0.01ms, tắt motion trang trí và giữ feedback tải; chart theo media-query đổi trong phiên, mascot kiểm tra giảm motion khi theo pointer. 1440×900/390×844 đã kiểm tra và reset. | Touch/máy thật, zoom 200% thật, reduced-motion OS, trình duyệt khác |

Ưu tiên tiếp: nghiệm thu AT/zoom/touch/reduced-motion OS cho UX-07/12/15/16; UX-10 cần xác nhận cụ thể về lưu ca DEMO H2 18088 còn đang chờ. UX-13 cần provider thật và những trạng thái ghi chưa chạy. Không thực hiện thu tiền. Bộ kiểm thử: Node 17/17; Maven toàn bộ 179 pass +25 skip; Java liên quan sau chỉnh template 14/14.
## Lịch sử chốt 6 task — 02/10/2026

**15/16 task đạt phạm vi nghiệm thu hiện tại.** Đã chốt thêm UX-07/12/13/15/16; UX-10 còn một bước lưu ca DEMO qua browser. Chi tiết: `IMPECCABLE_FINAL_SIX_2026-10-02.md`. Bảng lịch sử bên trên phản ánh lượt trước và được giữ để đối chiếu.

| Task | Kết quả mới nhất | Giới hạn/bước còn lại |
|---|---|---|
| UX-07 | Chốt tương phản theo role từ lượt trước; bổ sung placeholder, gradient CTA và liên kết lỗi. Liên kết lỗi 4,01→10,22:1; gradient cam 3,56→5,18:1; teal 3,77→5,47:1. Xác nhận cuối không còn cảnh báo detector trong mẫu. | Độ phủ hữu hạn, không phải chứng nhận WCAG mọi trạng thái. |
| UX-10 | Validation gắn từng field, focus/live region thêm-xóa thuốc, rà soát, guard hủy/revalidate/chống lặp/BFcache. Frontend và H2 integration lưu fake pass. | Chưa lưu ca qua UI 18088; automatic approval review đã chặn và xác nhận riêng còn chờ. |
| UX-12 | Nhãn lỗi và control, vùng chạm 44px trong mẫu; người dùng trả lời “Đã ổn” sau hướng dẫn screen reader, zoom 200%, touch. | Người dùng không cung cấp thiết bị/browser/version; nghiệm thu trên mẫu. |
| UX-13 | Bốn trạng thái bank render/pass, QR lỗi tải live có fallback; EXPIRED dừng QR/poll và cho đối soát tiền đến muộn. OTP tính deadline đúng khi quay lại tab, chặn xác nhận hết hạn. Form lỗi giữ input; test CRUD invalid giữ edit ID. | Provider/ngân hàng thật thuộc gate tích hợp riêng; chưa thực hiện giao dịch thật. |
| UX-15 | Thêm/xóa thuốc chuyển focus đúng, error được liên kết và live region; Tab/Shift+Tab modal sửa giữ focus, Escape trả trigger. Người dùng nghiệm thu screen reader mẫu báo ổn. | Bước ghi bệnh án thực qua keyboard còn theo UX-10. |
| UX-16 | Desktop/mobile, light/dark đã xem; sửa specificity reduced-motion homepage và giữ loading feedback. Người dùng nghiệm thu zoom/motion/touch mẫu báo ổn. | Chưa có thông tin browser/thiết bị để công bố matrix hỗ trợ toàn diện. |

Kiểm thử mới: **Node 24/24**, **Maven toàn bộ 180 pass +25 skip**, **Java liên quan sau sửa cuối 30/30**. Không lưu bệnh án trên preview đang chạy, không xác nhận thanh toán, không deploy SQL Server 18087. Mẫu nghiệm thu read-only ở port 18089 dùng dữ liệu test; kết quả “Đã ổn” không được suy diễn thành quyền ghi bệnh án.

## Trạng thái mới nhất — 03/10/2026

**16/16 task đạt trong phạm vi nghiệm thu đã ghi.** UX-10 đã hoàn tất bước còn lại sau khi người dùng duyệt cụ thể dữ liệu DEMO và H2 localhost 18088. Validation bắt buộc chặn lưu; hủy xác nhận bằng bàn phím giữ dữ liệu và trả focus đúng; xác nhận lưu một lần chuyển ca sang Hoàn tất. Tiền sử đọc lại đúng một bệnh án và đơn 2 viên Paracetamol. Billing hiển thị một hóa đơn **353 000 đ**, **Chưa thanh toán**, đã quyết toán 0.

Chi tiết và ảnh: `UX10_ACCEPTANCE_2026-10-03.md`, `docs/evidence/ux10-acceptance-2026-10-03/`. Node liên quan chạy lại **5/5 pass**; bộ Maven toàn bộ là kết quả lịch sử 02/10, không chạy lại trong lượt này. Không xác nhận thu tiền. Những giới hạn về browser/thiết bị, provider thật và độ phủ hữu hạn ở báo cáo trước vẫn áp dụng; 16/16 không phải chứng nhận WCAG toàn diện hay nghiệm thu ngân hàng thật.
