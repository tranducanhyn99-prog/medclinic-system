# MedClinic — triển khai Impeccable, 02/10/2026

Áp dụng adapt, harden, distill và extract, giữ cyan/teal/slate và Geist hiện có. Tasklist có 16 mục: 10 đạt kiểm tra hiện tại, 6 đã có thay đổi nhưng còn nghiệm thu chuyên sâu. Critique gốc được giữ nguyên để đối chiếu.

## Thay đổi

- Bảng nghiệp vụ thành từng mục có nhãn trên mobile, giữ nguyên form/action và ARIA; thông tin nhận diện, nội dung dài và thao tác toàn chiều rộng. Desktop giữ bảng, vùng cuộn có tên và chỉ nhận Tab khi tràn.
- Tiếp nhận có ba filter chính và select lịch sử. Bàn bác sĩ có tìm kiếm, lọc ca cần khám, thời điểm dữ liệu và số kết quả; không lưu tên bệnh nhân vào storage.
- CSS shell tách file, dùng token/font/theme chung; gỡ dark auth trùng. Chỉnh status, STT đặc, chữ lý do khám, nút outline và placeholder theo render thực tế.
- Trang chủ thống nhất bốn bước, tiện ích thu gọn, tránh quy trình lặp ở desktop. An Nhiên có một launcher trong layout, nội dung theo role, hỗ trợ Escape/focus và không che nội dung thường trực.
- Form khám có ngữ cảnh bệnh nhân/chưa lưu, cảnh báo rời, empty thuốc và bản rà soát trước lưu. Tiền thuốc có nhãn “Thành tiền” đúng phép tính số lượng.
- 403/404/500 có hướng phục hồi theo role, giữ status và JSON API, bỏ thông báo lặp. Demo panel chỉ render khi dev và không có prod.
- Website/PDF dùng formatter số nhất quán: `353000.00` → `353 000 đ`, giữ phần lẻ khác 0. Profile/booking có fallback ảnh; button loading/guard phục hồi khi back.

## Test tự động

```powershell
node --test src/test/frontend/ui-regressions.cjs
mvn.cmd -o '-Dmaven.repo.local=C:/Users/DUCANHZZ/.m2/repository' test
git -c core.safecrlf=false diff --check
```

- **12/12 frontend pass**: format số lớn, table giữ action/label/colspan, queue lọc kết hợp và không lưu tên; regression submit cancellation/back, slot retry/race, chart theme, cache riêng tư.
- **Maven: 198 trường hợp, 173 pass, 25 skipped, 0 failures/errors; BUILD SUCCESS**, 1 phút 31 giây. 25 skipped là các integration test cần SQL Server, không tính là nghiệm thu SQL Server lượt này.
- 23 test mục tiêu trước đó cũng pass: security/error, layout contract, formatter, PDF invoice và public doctor.
- Diff không có lỗi whitespace. Hai bổ sung cuối chỉ đặt aria-hidden cho icon động, không đổi logic.

## Browser

Bản dev H2 riêng `http://127.0.0.1:18088`, bind 127.0.0.1; mail trỏ localhost:11025. Không đổi DB/container ở 18087. Dùng tài khoản mẫu và một cuộc hẹn demo ghi rõ mục đích nghiệm thu.

Một vòng quan sát gộp, một batch sửa lỗi thấy trên render, một vòng xác nhận; viewport **1440×900 / 390×844**, đã reset sau QA.

| Màn/luồng | Quan sát |
|---|---|
| Admin dashboard/doctors | Tiền `350 000 đ`, bảng mobile có label/action, bio rộng toàn mục; hint vuốt ngang được ẩn khi đã chuyển dạng |
| Reception/billing | Filter có số đếm; ca demo được check-in/cấp STT; billing empty giải thích nguồn hóa đơn; help đúng role và Escape trả focus |
| Patient appointments/booking/detail | Empty có CTA, slot dark đọc rõ, loading chuyển sang slot; đặt lịch demo thành công, có phiếu QR |
| Doctor queue/examination | Ca demo IN_PROGRESS hiện trong Cần khám; search không khớp có hướng thoát; form thêm thuốc, 2 × 1 500 = 3 000 đ; rà soát đầy đủ, chưa lưu |
| Home / 403 | Hero desktop/mobile có tìm bác sĩ; bốn bước thống nhất. Bác sĩ mở Admin thấy lỗi MedClinic với CTA Về bàn khám |

Document width mobile đo **375px** trong viewport 390px có thanh cuộn dọc, không vượt viewport. Theme/menu **44×44px**, Sửa cao 44px; bio Admin rộng **309.6px**. Booking dark: chữ slot `rgb(241,245,249)` trên nền `rgb(15,23,42)`. Avatar Admin: không có ảnh lỗi ở lượt quan sát. Đây là màn đại diện, không phải mọi trạng thái trên toàn bộ 30 template.

## Tương phản

Tính luminance sRGB từ màu CSS/computed; gradient/alpha cần kiểm tra thêm theo từng vị trí. Các cặp dưới đây là nền đặc đã đối chiếu.

| Thành phần | Chữ / nền | Sau sửa |
|---|---|---:|
| Success subtle | #047857 / #ecfdf5 | 5.21:1 |
| Primary subtle | #0e7490 / #ecfeff | 5.15:1 |
| Warning subtle | #92400e / #fffbeb | 6.84:1 |
| Info subtle | #075985 / #f0f9ff | 7.09:1 |
| STT đặc | #ffffff / #0e7490 | 5.36:1 |
| Lý do khám dark | #fde68a / #332701 | 11.78:1 |
| Thêm thuốc dark | #6ee7b7 / #0f172a | 11.71:1 |
| Thoát dark | #cbd5e1 / #0b1120 | 12.68:1 |

Trước batch cuối, form khám có STT 2.43:1, lý do khám 1.05:1, outline khoảng 3.8–4.0:1. Vòng xác nhận đã kiểm tra màu mới trên render.

## Bằng chứng và giới hạn

Ảnh trong `evidence/impeccable-implementation-2026-10-02/`: Admin desktop light/mobile dark, queue desktop light/mobile dark, examination mobile dark, reception mobile dark và help mobile dark.

- Browser chưa lưu form khám: automatic approval review từ chối thao tác tạo bệnh án/đơn thuốc/hóa đơn demo; đang chờ xác nhận riêng. Ca dừng ở IN_PROGRESS, chưa có bệnh án/hóa đơn từ lượt browser này.
- Không thu tiền, gọi provider thật, gửi OTP thật, thử lại webcam hay chạy SQL Server integration cần cấu hình trong lượt này.
- Còn theme states các route/modal, keyboard workflow/screen reader, zoom 200%, reduced-motion OS, thiết bị touch thật và trình duyệt khác.
- Preview H2 trong RAM khác instance SQL Server 18087. Áp dụng bản sửa cho instance hiện hữu cần build/restart đúng môi trường ở bước phát hành.
