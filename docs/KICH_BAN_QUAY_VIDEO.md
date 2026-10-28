# Kịch bản video MedClinic và lời thuyết minh

Cập nhật **15/10/2026**. Bản mở rộng dùng **SQL Server**, con trỏ di chuyển, vòng sáng khi bấm và phụ đề tiếng Việt. Thời lượng đi theo các chức năng đã quay; người thực hiện tự lồng tiếng. Điền họ tên, MSSV, lớp và giảng viên khi thu âm hoặc thêm tiêu đề đầu video.

<!-- VIDEO_RESULT_START -->
Bản mới: **18:03**, 1920×1080, H.264, 25 fps, khoảng **31.1 MB**, 14 chương; không có audio. Đã kiểm tra **67 mốc nghiệp vụ**, có con trỏ và dấu bấm chuột, giải mã toàn bộ MP4 không lỗi và xem các khung hình chính. Bản dựng ghép các đoạn liên tiếp của cùng phiên dữ liệu.

| Chương | Mốc trong MP4 | Nội dung |
| --- | --- | --- |
| 01 | 00:00–01:00 | MEDCLINIC — ĐỒ ÁN LẬP TRÌNH WEB |
| 02 | 01:00–01:37 | KIẾN TRÚC VÀ DỮ LIỆU |
| 03 | 01:37–02:07 | TÌM BÁC SĨ, GIAO DIỆN VÀ THIẾT BỊ |
| 04 | 02:07–03:22 | ĐĂNG KÝ, OTP, GỬI LẠI VÀ MAGIC LINK |
| 05 | 03:22–04:26 | HỒ SƠ, ẢNH ĐẠI DIỆN VÀ KHÔI PHỤC MẬT KHẨU |
| 06 | 04:26–04:49 | BỆNH NHÂN HỦY LỊCH VÀ NHẬN EMAIL |
| 07 | 04:49–06:06 | BỆNH NHÂN ĐẶT LỊCH |
| 08 | 06:06–07:08 | LỄ TÂN TIẾP NHẬN VÀ CẤP SỐ |
| 09 | 07:08–09:10 | BÁC SĨ KHÁM VÀ KÊ ĐƠN |
| 10 | 09:10–11:14 | THU NGÂN, HÓA ĐƠN VÀ PDF |
| 11 | 11:14–13:55 | VÃNG LAI VÀ CHUYỂN KHOẢN MÔ PHỎNG |
| 12 | 13:55–16:06 | QUẢN TRỊ: CHUYÊN KHOA, BÁC SĨ, KHO VÀ TÀI KHOẢN |
| 13 | 16:06–17:48 | CODE, KIỂM THỬ VÀ AI/GITHUB |
| 14 | 17:48–18:03 | KẾT QUẢ VÀ GIỚI HẠN |

Ca chính: `MED-261015-C47CD344`. Các ca hủy lịch, vãng lai/callback và đối soát là ca riêng.
<!-- VIDEO_RESULT_END -->

File mới nằm local trong `output/video-demo-v2/MedClinic_Demo_DayDu_SQL_1080p.mp4`; phụ đề rời cùng thư mục tên `MedClinic_Demo_DayDu_SQL.srt`. Bản H2 10 phút trước đó vẫn giữ tại `output/video-demo/MedClinic_Demo_1080p.mp4`. Video và nguyên liệu dựng được ignore, không đưa lên GitHub cùng source.

## Môi trường và phạm vi minh chứng

- Ứng dụng chạy profile `sqlserver,demo`, Flyway V1–V10 và Hibernate validate; database riêng `medclinic_video_20261015`. Không dùng database ứng dụng chính để quay.
- Đăng ký bệnh nhân mới, OTP, magic link, khôi phục mật khẩu và thư hủy lịch đi qua **SMTP thật vào Mailpit local**. Không gửi Gmail hay hộp thư cá nhân.
- Google được kiểm tra bằng nhà cung cấp giả lập trong bộ test giao thức trên SQL Server. Video không có phiên đăng nhập Google thật.
- Quét QR dùng camera mô phỏng chứa QR thật của lịch vừa đặt. Callback ngân hàng và mã đối soát đều là dữ liệu mô phỏng; không chuyển tiền.
- Đồng hồ nghiệp vụ của phiên quay cố định lúc **09:00 ngày quay** để trình diễn vãng lai cùng ngày. Đây là fixture local qua launcher; source ứng dụng và đồng hồ máy không bị thay đổi. Xử lý lịch ngày mai dùng chính sách cho xử lý sớm của profile demo.
- Hàng chờ được kiểm tra bằng hai phiên trình duyệt riêng. Sau WebSocket, client tự tải lại dữ liệu; người dùng không bấm refresh. Video trình bày ảnh chụp có chú thích từ phiên bác sĩ.
- Code và PDF lấy từ source hoặc kết quả xuất thật rồi trình bày lớn để đọc rõ. Các thẻ kiến trúc/kiểm thử là phần giải thích xen giữa thao tác ứng dụng.

## Lời đọc theo 14 chương

Đây là lời gợi ý để nói bằng giọng của mình, không cần đọc nguyên văn. Có thể dừng nói khi con trỏ đang nhập liệu và giải thích tiếp lúc màn hình đã hiện kết quả. Mốc thời gian phía trên giúp thu từng đoạn rồi ghép.

### 01. Giới thiệu đề tài

**Hình:** tiêu đề, bốn vai trò, trang chủ.

> Em chào thầy cô. Em là [họ tên], mã số sinh viên [MSSV]. Đây là MedClinic, đồ án môn Lập trình Web về quản lý và đặt lịch khám phòng khám tư nhân. Hệ thống kết nối lịch hẹn, tiếp nhận, khám, kê đơn và thu ngân để bốn vai trò cùng theo dõi một ca khám. Video dùng dữ liệu mẫu và SQL Server riêng. Em sẽ trình bày các nhóm chức năng, đi theo một ca khám từ đầu đến cuối, rồi giải thích code và kiểm thử.

### 02. Kiến trúc và dữ liệu

**Hình:** Controller → Service → Repository → Database; quan hệ lịch hẹn, bệnh án, đơn thuốc và hóa đơn.

> Backend dùng Java 21, Spring Boot và Spring MVC. Controller nhận request; service kiểm tra nghiệp vụ và quản lý transaction; repository truy cập dữ liệu qua JPA. Thymeleaf render HTML, JavaScript tải khung giờ và nhận sự kiện hàng chờ. Lịch hẹn liên kết bệnh nhân, bác sĩ và slot, từ đó phát sinh bệnh án, đơn thuốc và hóa đơn. Giá thuốc được giữ theo thời điểm kê đơn. H2 phục vụ chạy nhanh và test mặc định; bản quay dùng SQL Server với Flyway.

### 03. Tìm bác sĩ và giao diện

**Hình:** lọc theo tên, chuyên khoa, phí tối đa; hồ sơ bác sĩ; sáng/tối; viewport di động.

> Người chưa đăng nhập có thể tìm bác sĩ và xem thông tin trước khi đặt lịch. Các bộ lọc lấy dữ liệu từ backend. Giao diện có chế độ sáng, tối và bố cục thích ứng theo kích thước màn hình. Phần điện thoại là viewport di động của trình duyệt để minh họa responsive.

### 04. Đăng ký, OTP, gửi lại và magic link

**Hình:** đăng ký mới → thư Mailpit → mã sai → gửi lại → mã mới đúng → đăng nhập bằng magic link.

> Em tạo bệnh nhân mới. Tài khoản chưa được hoạt động ngay mà cần xác thực email. Đây là thư do SMTP gửi thật vào Mailpit local. Nhập mã sai bị từ chối. Gửi lại làm vô hiệu challenge cũ và tạo mã xác nhận mới. Mã đúng sẽ kích hoạt tài khoản. Khi đăng nhập bằng mật khẩu, tài khoản mới vẫn cần OTP; liên kết xác nhận trong email là cách thay thế việc nhập mã. Dữ liệu xác thực được lưu và kiểm tra trên SQL Server.

### 05. Hồ sơ và mật khẩu

**Hình:** sửa địa chỉ, ảnh; email reset mật khẩu; đăng nhập lại với OTP; đổi mật khẩu; URL admin trả 403.

Ảnh mẫu trong cảnh đầu có MIME không khớp nội dung và bị backend từ chối. Cuối chương 13 có cảnh kiểm tra bổ sung bằng JPEG đúng định dạng, lưu xong tải lại vẫn đọc được ảnh.

> Bệnh nhân có thể cập nhật hồ sơ và ảnh đại diện, backend kiểm tra file tải lên. Quên mật khẩu gửi liên kết có token qua email; form phản hồi không làm lộ email có tồn tại hay không. Sau khi đặt lại mật khẩu, em đăng nhập và xác thực OTP. Đổi mật khẩu trong hồ sơ còn yêu cầu mật khẩu hiện tại. Cuối phần này, em truy cập trực tiếp URL quản trị bằng bệnh nhân: server trả 403, cho thấy phân quyền được kiểm tra ở backend.

### 06. Hủy lịch và thông báo

**Hình:** tạo lịch phụ, mở hộp thoại hủy, xác nhận, trạng thái đã hủy.

> Em dùng một lịch riêng để minh họa hủy. Hệ thống kiểm tra trạng thái, sau đó trả khung giờ về để đặt lại. Trạng thái đã hủy được lưu trong SQL Server và thư thông báo gửi qua SMTP local. Ca này tách với ca chính để phần khám phía sau dễ theo dõi.

### 07. Đặt lịch và phiếu hẹn QR

**Hình:** chọn Nguyễn Văn Hùng, ngày, slot, lý do; phiếu hẹn và QR.

> Đây là ca chính. Em chọn bác sĩ, ngày và khung giờ còn trống. Backend kiểm tra lại slot có thuộc đúng bác sĩ, ngày và còn khả dụng hay không. Slot được khóa trong transaction trước khi cập nhật để ngăn hai request đặt cùng giờ. Phiếu hẹn có mã và QR. Từ đây tới thanh toán tiền mặt, em dùng cùng mã này. Lịch ngày mai được xử lý sớm nhờ profile demo; môi trường thực tế phải tuân thủ chính sách ngày khám.

### 08. Lễ tân, QR và hàng chờ

**Hình:** quét QR, tiếp nhận, STT; ảnh từ phiên bác sĩ riêng.

> Lễ tân có thể nhập mã hoặc quét QR. Camera trong video là mô phỏng nhưng QR thuộc ca vừa tạo; bộ quét thực sự giải mã và tìm được lịch. Tiếp nhận đổi trạng thái sang CHECKED_IN và cấp số thứ tự theo ngày khám. Phiên bác sĩ đã mở trước đó nhận WebSocket và tự làm mới hàng chờ. Ảnh đang hiển thị lấy từ phiên đó, không cần bấm refresh. Sự kiện được phát sau khi transaction commit thành công.

### 09. Bác sĩ khám và kê đơn

**Hình:** lịch trực, hàng chờ, bắt đầu khám, bệnh án, thuốc, xác nhận lưu, mở lại hồ sơ.

> Bác sĩ xem lịch trực và các ca được phân công. Bắt đầu khám cần đúng bác sĩ phụ trách và trạng thái hợp lệ. Em nhập dữ liệu mô phỏng rồi kê hai đơn vị thuốc để minh họa xử lý kho. Khi hoàn tất, bệnh án, đơn thuốc, xuất kho và hóa đơn được ghi trong cùng transaction; nếu lỗi hoặc thiếu tồn kho thì rollback. Đọc lại hồ sơ không trừ kho hay tạo thêm hóa đơn. Chẩn đoán và thuốc trong video chỉ là dữ liệu thử phần mềm.

### 10. Tiền mặt, hồ sơ và PDF

**Hình:** hóa đơn, xác nhận tiền mặt, trạng thái PAID; PDF hóa đơn, hồ sơ bệnh nhân, đơn thuốc và lịch sử.

> Hóa đơn ca chính gồm 350.000 đồng phí khám và hai đơn vị thuốc giá 1.500 đồng, tổng 353.000 đồng theo dữ liệu seed. Nhân viên xác nhận thu tiền thì hóa đơn chuyển PAID. Backend khóa hóa đơn và kiểm tra trạng thái để tránh thu lặp. PDF đang hiển thị là file thật vừa xuất, được render lớn để đọc rõ tiếng Việt. Bệnh nhân cũng xem lại hồ sơ và tải đơn thuốc của mình; backend kiểm tra quyền đọc và xuất file.

### 11. Vãng lai và chuyển khoản

**Hình:** vãng lai cùng ngày, bác sĩ hoàn tất; VietQR; callback sai/đúng; ca khác đối soát thủ công.

> Lễ tân có thể tạo ca vãng lai trong ngày. Phiên quay cố định giờ nghiệp vụ 09:00 để có khung giờ phù hợp, không sửa đồng hồ máy. Sau khi khám, em minh họa hai cách xác nhận chuyển khoản. Tạo VietQR chỉ tạo yêu cầu thanh toán. Callback sai chữ ký và sai tiền bị từ chối; callback đúng mới xác nhận hóa đơn, gửi lại cùng sự kiện không thu thêm lần nữa. Đối soát thủ công cho phép nhập mã giao dịch sau khi kiểm tra. Các callback và mã này đều mô phỏng local, không có tiền chuyển qua ngân hàng thật.

### 12. Quản trị và kho

**Hình:** thêm/sửa/tạm ngừng chuyên khoa; tạo/sửa bác sĩ; thuốc hết hàng, nhập kho, giá và trạng thái; khóa/mở tài khoản; dashboard.

> Quản trị viên quản lý danh mục để các màn hình khác sử dụng. Em thêm chuyên khoa, sửa mô tả và tạm ngừng nhận lịch; tạo bác sĩ và cập nhật phí khám. Thuốc hết tồn được cảnh báo, sau đó nhập bổ sung và xem lịch sử kho. Giá danh mục phục vụ các lần kê sau, hồ sơ lịch sử giữ giá lúc phát sinh. Quản trị viên tìm, khóa và mở tài khoản. Dashboard tổng hợp lịch hẹn, chuyên khoa và doanh thu từ dữ liệu demo trong SQL Server.

### 13. Code, kiểm thử và AI/GitHub

**Hình:** code khóa slot, transaction, listener; test SQL; commit thật; kiểm tra bổ sung upload JPEG hợp lệ.

> TimeSlotRepository dùng PESSIMISTIC_WRITE để tuần tự hóa thao tác trên một slot. Service kiểm tra nghiệp vụ trong transaction. Listener AFTER_COMMIT gửi cập nhật hàng chờ khi dữ liệu lưu thành công. Đợt bổ sung đã thực thi 12 test OTP và giao thức Google–OTP trên SQL Server, tất cả đạt và không skipped. Bộ mặc định có 282 test thực thi đạt; test SQL cần chạy riêng. Java test giả lập email và Google; phần đăng ký trong video gửi SMTP thật vào Mailpit. Lịch sử Git là các commit đang có trong repo. Khi dùng AI, em cần hiểu quy tắc, đọc lại code và kiểm chứng kết quả.

Chỉ nhận phần đóng góp thực tế. Không tính skipped vào số test đã chạy. Lệnh chạy lại và phạm vi xem [kiểm thử](KIEM_THU.md), [runbook SQL Server](database/SQLSERVER_RUNBOOK.md). Câu hỏi phản biện xem [hướng dẫn thuyết trình](HUONG_DAN_THUYET_TRINH.md).

> Em thử lại ảnh JPEG có MIME khớp nội dung. Sau khi lưu và tải lại trang, ảnh vẫn xuất hiện; backend trả về đúng dữ liệu ảnh. Cảnh trước từ chối file không hợp lệ, còn cảnh này xác nhận đường đi thành công.

### 14. Kết quả và giới hạn

**Hình:** mã ca chính và chuỗi kết quả cuối.

> MedClinic minh họa các nhóm chức năng của bốn vai trò và một ca khám từ đặt lịch tới hồ sơ, thanh toán, PDF trên SQL Server. OTP được kiểm tra qua cả bộ test và đăng ký mới. Ngân hàng thật, Google thật, camera vật lý và tải lớn cần nghiệm thu riêng. Em cảm ơn thầy cô đã theo dõi.

## Thu âm và ghép vào bản nộp

1. Mở video, đối chiếu mốc chương, thu từng đoạn. Đọc số tiền, mã và trạng thái đúng hình. Không cần nói kín các khoảng nhập liệu.
2. Ghi thử 20 giây, nghe lại bằng tai nghe, giữ âm lượng và khoảng cách micro ổn định. Thêm họ tên/MSSV thật.
3. Nhập MP4 và giọng đọc vào trình dựng đang dùng, đặt lời theo chương. Phụ đề đã có trên hình nên không chồng thêm SRT nếu không cần.
4. Xuất MP4 H.264, giữ 1920×1080, 25 fps, âm thanh AAC. Xem lại file có tiếng, đủ đầu/cuối và chữ đọc rõ.

Video local không có giọng đọc để người thực hiện tự lồng tiếng. Không cần giữ đúng 10 phút; có thể điều chỉnh nhịp theo giọng và yêu cầu nộp của giảng viên.
