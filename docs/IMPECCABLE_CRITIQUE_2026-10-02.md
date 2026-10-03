# Impeccable critique — MedClinic toàn dự án

Đánh giá ngày 02/10/2026. Phạm vi mã nguồn gồm 30 template Thymeleaf, hai layout, fragments và `medclinic-design-system.css`. Không sửa code trong lượt này.

## Kết quả

Đánh giá độc lập theo Nielsen được **22/40 (55%, Acceptable theo thang Impeccable)**. Nền nhận diện cyan/deep teal, token sáng/tối, breadcrumb, skip link, focus-visible, form labels và tóm tắt booking đã có. Chất lượng chưa đồng nhất giữa trang bệnh nhân được tạo tác tốt và portal vận hành dựa nhiều vào bảng Bootstrap. Ưu tiên UX là làm lỗi có đường phục hồi, đưa bảng vào tầm dùng trên mobile, sửa tương phản status và giảm tải thao tác tại quầy.

| Heuristic | Điểm / 4 |
|---|---:|
| Visibility of System Status | 3 |
| Match System / Real World | 3 |
| User Control and Freedom | 2 |
| Consistency and Standards | 2 |
| Error Prevention | 2 |
| Recognition Rather Than Recall | 3 |
| Flexibility and Efficiency | 1 |
| Aesthetic and Minimalist Design | 3 |
| Error Recovery | 2 |
| Help and Documentation | 1 |

## Tải nhận thức

Checklist Impeccable 8 mục: **5 đạt, 2 trượt, 1 đạt có điều kiện**. Trang chủ có nhiều hướng vào cùng lúc; lễ tân hiển thị 6 trạng thái cùng hàng và chuyên khoa có 6 mục. Booking làm tốt chunking, grouping, hierarchy, disclosure và tóm tắt để giảm tải nhớ.

## Các phát hiện chính

1. **P1 — Bảng nghiệp vụ tràn chiều ngang trên điện thoại.** CSS giữ bảng tối thiểu 760px; hàng đợi lễ tân/bác sĩ và các bảng quản trị đẩy thông tin nhận diện, trạng thái, hành động ra xa nhau. Lượt browser trước trên doctor desk ở 390px đo nội dung bảng 783px trong vùng 342px.
2. **P1 — Trang 403 không giúp người dùng tiếp tục.** Route admin mở bằng tài khoản bác sĩ trả Whitelabel tiếng Anh. Browser phụ cũng nhận 403 ở một số route ngoài role. Quyền có thể đúng; lỗi cần được giải thích và có điều hướng an toàn.
3. **P1 — Badge “Hoàn tất” có tương phản 3.577:1.** Chữ 12.8px #059669 trên #ecfdf5, dưới ngưỡng 4.5:1 cho chữ thường. Đo được trong phiên kiểm chứng trước trên doctor desk.
4. **P2 — Sáu tab trạng thái lễ tân làm tăng thời gian quét.** Mobile chỉ cho cuộn ngang; nhóm hành động thường dùng cần được đưa lên trước.
5. **P2 — Hai shell và nhiều theme override làm giảm tính nhất quán.** `layout/main` và `layout/dashboard` lặp font/theme/shell; một số auth page có override riêng.
6. **P2 — Widget An Nhiên có hai điều khiển cùng mở một lời nhắn và che nội dung khi cuộn.** Gộp launcher và đặt bubble theo ngữ cảnh.
7. **P2 — Trang chủ nói 3 bước và 4 bước ở hai phần.** Đồng thời trang dài, nhiều khối và đường vào cạnh tranh.
8. **P2 — Form khám là một biểu mẫu dài nhiều loại thông tin.** Nhận xét từ nguồn template; chưa thao tác trên trang thực trong role bác sĩ.
9. **P2 — Login hiển thị shortcut tài khoản demo.** Cần giới hạn vào build demo/dev.
10. **Cần xác minh — Ảnh động, contrast theme, tên accessibility của icon và vùng chạm.** Detector cảnh báo nhiều `img` thiếu `src`/`src=""`, 1.1:1 contrast và các heuristic typography; Thymeleaf, selector theme, Bootstrap và ảnh bind động có thể khiến static scan thành false positive. Chỉ lập task khắc phục sau khi kiểm tra rendered state.

## Trạng thái detector

Chạy detector toàn bộ thư mục template đúng một lần, thấy **46 phát hiện trên 23 template: 45 cảnh báo và 1 khuyến nghị thuộc 11 rule**. Phân bố: 20 flat type hierarchy; 16 broken image; 2 overused font; 8 phát hiện đơn lẻ khác (palette, clipped overflow, dark glow, shadow, transition, low contrast, spacing, pulsing dot). Đây là kiểm kê tĩnh, không phải 46 bug đã xác minh. Trong nhóm ảnh, 15 kết quả là do HTML thô chưa xử lý Thymeleaf; một ảnh hồ sơ chỉ kiểm tra null, còn ảnh booking có `src=""` nhưng đang ẩn ở trạng thái đầu. Màu low-contrast tĩnh ghép chữ body với nền sidebar khác vùng, nên được xem là false positive. Báo cáo browser/source độc lập được lưu trong thư mục bằng chứng; tasklist chỉ biến các điểm có bằng chứng thành việc cụ thể, còn heuristics chưa được xác nhận được giữ ở mức kiểm tra.

## Độ phủ và giới hạn

Đã rà cả 30 template (admin 5, auth 6, doctor 3, doctors 2, home 1, patient 4, staff 4, layout 2, fragments 2, error 1) và CSS chung. Trong phiên browser hiện tại đã mở homepage, danh sách bác sĩ, booking, doctor desk và thử một số route ngoài role. Một số route admin/patient/staff trả 403 khi phiên dùng tài khoản bác sĩ, vì vậy trang bên trong các role đó được xem qua source, không được coi là live-tested. Chưa kiểm thử ở vai trò Admin/Staff/Patient; chưa thực hiện gửi booking, lưu khám, thu tiền, kiểm tra accessibility bằng AT hoặc kiểm tra cả hai theme/viewport qua toàn bộ route. Đánh giá giữ theme dark ban đầu, không ghi dữ liệu nghiệp vụ.

## Tài liệu liên quan

- Chi tiết assessment độc lập: [`assessment-a-whole-project.md`](evidence/impeccable-critique-2026-10-02/whole/a/assessment-a-whole-project.md)
- Evidence detector: [`detector-raw.json`](evidence/impeccable-critique-2026-10-02/whole/b/detector-raw.json)
- Backlog có tiêu chí nghiệm thu: [`UX_UI_TASKLIST_2026-10-02.md`](UX_UI_TASKLIST_2026-10-02.md)
