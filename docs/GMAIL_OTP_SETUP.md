# Gửi OTP đến Gmail thật

MedClinic yêu cầu xác nhận email bằng OTP (hoặc nút xác nhận trong thư) trước khi cấp phiên đăng nhập. Nhánh Google áp dụng bước này cả lần đầu tạo tài khoản lẫn các lần đăng nhập tiếp theo; nhánh email/mật khẩu dùng cơ chế OTP hiện có. Tài khoản seed `dev`/`demo` vẫn có ngoại lệ OTP cho đăng nhập bằng mật khẩu; đăng nhập Google luôn yêu cầu OTP.

## 1. Tạo Mật khẩu ứng dụng cho Gmail gửi thư

1. Dùng một Gmail bạn quản lý làm địa chỉ gửi thư của MedClinic.
2. Bật **Xác minh 2 bước** trong [Bảo mật tài khoản Google](https://myaccount.google.com/security).
3. Mở [Mật khẩu ứng dụng](https://myaccount.google.com/apppasswords), tạo mật khẩu cho `MedClinic` và lưu giá trị 16 ký tự vào cấu hình local. Có thể điền liền hoặc dán dạng 4 nhóm có dấu cách mà Google cung cấp; app tự bỏ dấu cách của định dạng này chỉ khi dùng `smtp.gmail.com`, không sửa file cấu hình.

Không dùng mật khẩu đăng nhập Gmail hoặc `GOOGLE_CLIENT_SECRET` cho SMTP. Nếu tài khoản không có mục Mật khẩu ứng dụng, kiểm tra điều kiện và chính sách tài khoản theo [hướng dẫn Google](https://support.google.com/accounts/answer/185833?hl=en).

## 2. Điền `.env` ở thư mục gốc

Sửa/thêm các dòng dưới đây, giữ nguyên cấu hình SQL Server, Google OAuth và các cấu hình khác:

```dotenv
MAIL_USERNAME=Gmail_gui_thu_cua_ban@gmail.com
MAIL_FROM=Gmail_gui_thu_cua_ban@gmail.com
MAIL_PASSWORD=Mat_khau_ung_dung_16_ky_tu
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED=true
```

`MAIL_USERNAME` và `MAIL_FROM` dùng cùng địa chỉ Gmail gửi thư. Mỗi người dùng nhận mã tại email tài khoản của họ. Gmail SMTP dùng cổng 587 với STARTTLS; tham khảo [cấu hình SMTP của Google](https://support.google.com/mail/answer/7126229?hl=en). Không gửi `.env` hoặc Mật khẩu ứng dụng vào chat/Git.

## 3. Nạp lại cấu hình và thử

Tại thư mục dự án:

```powershell
docker compose up -d --build --no-deps app
```

- Mở `http://localhost:8080/login` bằng Chrome/Edge thông thường. Chọn Google, hoàn tất xác thực Google rồi nhận OTP trong Gmail. Nhập mã trên MedClinic hoặc dùng nút xác nhận trong thư.
- Thử đăng ký bằng email/mật khẩu với tài khoản mới. Tài khoản được kích hoạt sau khi xác nhận email.
- OTP mặc định có hiệu lực 5 phút, tối đa 5 lần nhập sai; gửi lại thay mã cũ và có giới hạn. Trước khi xác nhận, không truy cập được Hồ sơ hoặc các trang bệnh nhân.
- Không nhận được thư: kiểm tra Spam, địa chỉ gửi, Mật khẩu ứng dụng và STARTTLS. Chọn Gửi lại OTP; tránh thử liên tục vì có giới hạn.

Compose mặc định vẫn dùng Mailpit nếu chưa cấu hình các biến SMTP. Sau khi chọn Gmail, thư không được lưu trong Mailpit. Profile `dev` (H2) chủ động không gửi email; để thử gửi thật, dùng Compose hiện tại (`sqlserver,demo`) hoặc chạy profile `sqlserver` với các biến môi trường trên. Maven không tự đọc `.env`.

## Hành vi Google và giới hạn

Google xác minh email và tạo/liên kết bệnh nhân theo chính sách [Google OAuth](GOOGLE_LOGIN_SETUP.md). Callback Google chỉ tạo phiên chờ OTP, chưa cấp quyền truy cập MedClinic. OTP Google dùng loại `LOGIN`, kể cả lần đầu, để không kích hoạt lại tài khoản đã bị quản trị viên khóa. Họ tên hiển thị lấy từ hồ sơ; username nội bộ được giữ nguyên.

OTP gửi cùng Gmail đã dùng đăng nhập là bước xác nhận bổ sung, không được coi là yếu tố bảo mật độc lập. Gửi email là bất đồng bộ; trang OTP không bảo đảm thư đã đến hộp thư. Gmail có giới hạn gửi và cần Internet. Google OAuth Client ID/Secret và Gmail SMTP Mật khẩu ứng dụng là hai bộ thông tin khác nhau.

## Kiểm chứng

Callback Google có test dùng JWT RSA local cho bước chờ OTP, quyền truy cập, CSRF/session, mã sai/gửi lại, tài khoản khóa và lỗi phát hành challenge. Xem [cách chạy và kết quả kiểm thử](KIEM_THU.md). Gửi OTP qua Gmail thật cần thử lại với cấu hình SMTP của môi trường đang dùng; test local không xác nhận thư đã đến hộp thư.
