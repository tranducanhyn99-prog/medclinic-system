# Assessment B — detector và browser evidence

Target: src/main/resources/templates/doctor/desk.html
Live route: http://127.0.0.1:18087/doctor/desk

## CLI một lần
Đã gọi launcher Windows đúng một lần: impeccable.cmd detect --json src/main/resources/templates/doctor/desk.html.
Raw JSON: detector-raw.json.
Launcher exit quan sát được: 0 (detector-exit.txt). JSON có finding; không được suy luận clean từ exit 0. Playbook ghi 2 là findings, nhưng invocation này trả 0; không chạy lại.

Tổng: 1 finding; 1 rule; warning=1, category slop=1.
- antipattern: flat-type-hierarchy
- name: Flat type hierarchy
- file: `src/main/resources/templates/doctor/desk.html`
- detector line: 0 (finding toàn file; không phải dòng source 1)
- snippet: Role sizes: body 16px, h1 16px, h2 16px, h3 16px (largest adjacent step 1.00:1; target 1.25:1)
- Phân loại: FALSE POSITIVE cho trang rendered. Fragment Thymeleaf decorate layout/dashboard (desk.html:4); shared layout nạp medclinic-design-system.css tại dashboard.html:214. h1 desk.html:14 dùng mc-page-title; CSS:706 clamp(1.5rem,2vw,2.1rem). h2 desk.html:95 dùng mc-data-title; CSS:2064 1rem. h3 desk.html:112 chỉ là nhánh empty-state, không render trong lượt này. DOM thực: body=16px, h1 desktop=28.8px/mobile=24px, h2=16px, KPI=32px; bước h1/body=1.8 desktop và 1.5 mobile, không phải 1.00. Không có thêm rule CLI.

## Browser một lượt desktop/mobile
Browser Codex IAB id2; tạo tab mới riêng B, kế thừa demo doctor đã xác thực. Không đăng nhập mới. Mẫu hiển thị có 2 hồ sơ DEMO đều COMPLETED; 0 CHECKED_IN, 0 IN_PROGRESS. Không start examination/save/payment hay sửa dữ liệu.

Desktop 1440x900, dark:
- Document/body scrollWidth=1440; không overflow ngang toàn trang.
- Bảng 1114.4px / scrollWidth1114; có 6 cột, hai rows.
- h1 28.8px, h2 16px, body16px; có hierarchy thật.
- Realtime status có role=status, aria-live=polite trong source; DOM thấy Realtime đang hoạt động.
- Refresh control165.16x40px; theme44x44px.
- Screenshot desktop-dark-1440x900.jpg; DOM metrics desktop-metrics.json.

Mobile 390x844, dark, document usable375px (scrollbar):
- h1 24px; không overflow document (scrollWidth375).
- 4 KPI xếp dọc (desk.html:32/46/61/76 col-sm-6 col-lg-3), đẩy toolbar y887.6px, h2 y902px, bảng y1020.9px: hàng chờ thực tế chưa xuất hiện ở viewport đầu dù là tác vụ chính.
- .table-responsive clientWidth342px, scrollWidth783px (width341.6px); table782.76px. Cột trạng thái bắt đầu x521.25px, Thao tác x670.53px; đều ngoài viewport khi scroll ngang=0. Tên bệnh nhân chỉ còn khoảng75.26px nội dung, bị xuống thành3–4 dòng; số điện thoại tách dòng. Xác nhận cần cuộn ngang để đối chiếu danh tính/trạng thái/thao tác. Đây là UX tradeoff/mobile issue; không phải overflow toàn trang.
- Container table-responsive tabIndex=-1, không role/aria-label trong DOM; chưa kiểm tra keyboard cuộn ngang nên KHÔNG khẳng định keyboard inaccessible.
- Toggle menu mobile37.46x44px: hẹp hơn chuẩn44px tham chiếu Casey; đây là thiếu kích thước mục tiêu, không suy diễn WCAG AA failure từ44px.
- Refresh mobile165.16x44px; theme44x44px.
- Sidebar lúc đầu screenshot trong transition; đã thay screenshot bằng settled capture. Settled transform(-256px), visibility:hidden, không có nội dung sidebar trong DOM snapshot sau. Không coi transient ghost sidebar là bug.
- Screenshot mobile-dark-390x844.jpg (top settled); mobile-dark-queue-390x844.jpg (PageDown scrollY630.4).
- Mascot fixed che một phần footer hỗ trợ tại PageDown và vùng góc dưới của nội dung/scrollbar; nhìn trực tiếp screenshot. Trên top nó chiếm một phần card KPI cuối. Không mở chatbot.
- metrics mobile-metrics.json có sidebar đo giữa transition, dùng mobile-style-accessibility.json cho trạng thái settled thay vì x=-80.52px trong metrics ban đầu.

## Khuyết điểm xác nhận bằng rendered style
Badge trạng thái Đã hoàn tất (desk.html:163–171, CSS:654–657) có foreground rgb(5,150,105) #059669, nền rgb(236,253,245) #ecfdf5, font12.8px. Contrast tính chuẩn sRGB=3.577:1, dưới4.5:1 cho chữ nhỏ. Đây là defect contrast được browser bắt thêm, không có trong CLI. Có text+icon nên không phải color-only information.
- Realtime white trên #198754=4.531:1; không nên gộp nó vào contrast failure.
- Muted #94a3b8 trên card#0f172a=6.963:1; secondary #cbd5e1 trên card=12.024:1, không nên khẳng định toàn bộ text muted trong dark có contrast thấp.
Raw style: mobile-style-accessibility.json; contrast-calculations.json.

## Chưa xác nhận
Empty state h3 và CHECKED_IN/IN_PROGRESS row/button/error states không render trong dữ liệu lượt này. Có source nhưng chưa exercise; không được nói primary start/resume workflow đã pass hay fail. Không đo loading/throttled network hay performance. Không browser detector overlay console: chưa inject.

## Run notes / cleanup
- A và B độc lập: B không đọc prior audit/backlog/critique/evidence hay Assessment A. Detector findings bị giữ kín với parent đến tín hiệu A complete.
- Không chạy context/init lại, không spawn thêm agent.
- Browser visibility được đọc false lúc đầu và cuối; không set visibility.
- Overlay unavailable: playwright.evaluate được tài liệu ghi READ-ONLY; không attempted document.title/script mutation. Không label Human, không injection và không user-visible overlay. Fallback screenshots + DOM computed styles/geometry.
- Impeccable live-server KHÔNG khởi động, không có process tạm cần stop. App server có sẵn giữ nguyên.
- Viewport override desktop/mobile đã reset trước đóng tab mới B. Theme dark giữ nguyên từ đầu đến cuối, không click theme toggle. Tab B đã đóng; không đóng hoặc tái sử dụng tab user/A.
- Không code/business changes.
- Temp-file cleanup: không tạo temp script/server; chỉ giữ artifacts bằng chứng trong /b/ có chủ ý.
- Parent owns target slug/ignore list; B không tự slug hoặc đọc previous ignore.
