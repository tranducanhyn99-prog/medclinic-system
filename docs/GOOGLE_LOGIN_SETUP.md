# Đăng ký và đăng nhập Google — MedClinic

Tích hợp ngày 12/10/2026. Nút Google xuất hiện trên trang đăng nhập/đăng ký khi bật cấu hình. Lần đầu tạo bệnh nhân; lần sau dùng lại tài khoản theo Google `sub`. Callback dùng Spring Security OpenID Connect.

## Tạo OAuth Client

1. Mở [Google Cloud Console](https://console.cloud.google.com/), tạo/chọn project MedClinic.
2. Vào **Google Auth Platform → Branding**, điền tên ứng dụng và email liên hệ. Trong **Audience**, chọn External khi dùng Gmail cá nhân; nếu đang Testing, thêm Gmail thử nghiệm vào Test users. Xem [hướng dẫn consent của Google](https://developers.google.com/workspace/guides/configure-oauth-consent).
3. Vào **Clients → Create client → Web application**. Thêm Authorized redirect URI:

   ```text
   http://localhost:8080/login/oauth2/code/google
   ```

   Nếu dùng `127.0.0.1` hoặc cổng khác, thêm URI tương ứng. Host/cổng/scheme/path phải khớp. Luồng server OAuth này không cần Authorized JavaScript origins. Xem [tạo client web](https://developers.google.com/workspace/guides/create-credentials#web-application).
4. Lưu Client ID/Client Secret vào cấu hình local; không gửi secret vào chat hay commit. Scope của app chỉ gồm `openid`, `email`, `profile`.

## Chạy bằng Docker Compose

Thêm vào `.env` local đã có SQL Server/remember-me:

```dotenv
GOOGLE_LOGIN_ENABLED=true
GOOGLE_CLIENT_ID=<Web client ID>
GOOGLE_CLIENT_SECRET=<Client secret>
```

Sau khi điền cấu hình, rebuild app:

```powershell
docker compose up -d --build app
```

Giữ volume database/uploads. Flyway áp dụng V10 khi app khởi động: thêm `google_subject` nullable và unique index non-null, giữ nguyên V1–V9.

## Chạy bằng Maven/H2

Maven không tự đọc `.env`. Điền biến trong terminal local:

```powershell
$env:GOOGLE_LOGIN_ENABLED="true"
$env:GOOGLE_CLIENT_ID="<Web client ID>"
$env:GOOGLE_CLIENT_SECRET="<Client secret>"
mvn spring-boot:run
```

Mở `http://localhost:8080/login` hoặc `/register` trong Chrome/Edge thường. Google có thể từ chối browser nhúng theo [chính sách OAuth](https://developers.google.com/identity/protocols/oauth2/policies#embedded-user-agents). H2 mất dữ liệu khi dừng app; tài khoản Google local được tạo lại lần sau.

## Hành vi tài khoản

- Gmail `@gmail.com`, hoặc Workspace có `email_verified=true` và `hd` phù hợp, được chấp nhận. Email bên thứ ba không có `hd` dùng đăng ký/mật khẩu/OTP thông thường vì Google không bảo đảm quyền sở hữu email hiện tại. Xem [quy tắc email của Google](https://developers.google.com/identity/sign-in/web/backend-auth).
- Email mới tạo **ROLE_PATIENT**, email đã xác thực, tên Google, subject duy nhất và mật khẩu ngẫu nhiên không cung cấp cho người dùng. Bổ sung số điện thoại/ngày sinh/địa chỉ trong Hồ sơ.
- Email đã có chỉ liên kết với bệnh nhân active, email đã xác thực và chưa gắn Google khác. So sánh email không phân biệt hoa/thường; giữ nguyên mật khẩu, hồ sơ và quyền.
- Admin/bác sĩ/lễ tân dùng tài khoản phòng khám; Google không tự liên kết hoặc cấp các quyền này.
- Google đã gắn nhưng thay đổi email sẽ bị từ chối để xử lý khôi phục hồ sơ có chủ đích; không chuyển tài khoản y tế bằng email hay tạo bản sao khi đã có subject.
- Tài khoản khóa không đăng nhập Google được. Khóa/mở khóa không khôi phục session cũ; Google dùng chung kiểm tra active/securityVersion.
- Sau khi Google xác thực, MedClinic gửi OTP email và chỉ cấp phiên đăng nhập khi xác nhận mã hoặc nút trong thư. Áp dụng cả lần đầu tạo bệnh nhân và các lần đăng nhập sau, kể cả tài khoản seed có `otpBypass`; nhánh mật khẩu vẫn giữ ngoại lệ seed hiện có. Xem [cấu hình Gmail gửi OTP thật](GMAIL_OTP_SETUP.md).
- Sau khi xác nhận OTP, vào từ đặt lịch sẽ quay lại đặt lịch; nhánh khác vào lịch hẹn bệnh nhân. Logout dùng POST/CSRF như hiện tại.
- Muốn có mật khẩu MedClinic cho tài khoản mới từ Google: dùng Quên mật khẩu với email đã lưu, qua SMTP/Mailpit và OTP của luồng mật khẩu.

## Lỗi thường gặp

| Hiện tượng | Xử lý |
| --- | --- |
| Không thấy nút Google | Bật flag trong đúng terminal/container rồi restart. |
| Bật Google làm app không khởi động | Phải có cả Client ID và Client Secret; tắt flag nếu chưa có. |
| `redirect_uri_mismatch` | Đối chiếu callback với Google Cloud; `localhost` khác `127.0.0.1`, cổng và path phải khớp. |
| `invalid_client` | Kiểm tra client loại Web application và secret còn hiệu lực, cập nhật rồi restart. |
| Google từ chối truy cập | Kiểm tra Audience/Test users, chính sách Workspace và dùng browser thường. |
| Quay về login với lỗi Google | Có thể hủy consent, session callback hết hạn, email không phù hợp, tài khoản khóa/khác vai trò hoặc binding khác. Thử lại từ nút Google; dùng mật khẩu/OTP hoặc liên hệ phòng khám để khôi phục. |
| Hai callback đăng ký cùng lúc | Unique index chỉ cho một hồ sơ; nếu một lượt bị từ chối do tranh chấp, đăng nhập lại. |

Production dùng callback HTTPS của domain thật, credentials trong secret manager/môi trường và proxy theo [runbook](database/SQLSERVER_RUNBOOK.md). Callback tính từ request; chỉ tin forwarded headers từ reverse proxy được kiểm soát.

## Kiểm chứng

Test callback dùng token RSA ký bằng khóa local để chạy authorization → callback → session và từ chối nonce sai/chữ ký giả. Test account phủ quyền, email trùng, khóa/mở khóa, Workspace và callback đồng thời. Enabled thiếu credentials làm startup fail; disabled không yêu cầu credentials.

Kết quả lần chạy hiện tại được tổng hợp trong [hướng dẫn kiểm thử](KIEM_THU.md). Các class dưới đây giữ trong source để chạy lại; test SQL Server cần environment guard và database riêng.

- [GoogleLoginIntegrationTest](../src/test/java/com/medclinic/GoogleLoginIntegrationTest.java)
- [GoogleCallbackProtocolIntegrationTest](../src/test/java/com/medclinic/GoogleCallbackProtocolIntegrationTest.java)
- [GoogleLoginConfigurationTest](../src/test/java/com/medclinic/GoogleLoginConfigurationTest.java)
- [SqlServerGoogleLoginIntegrationTest](../src/test/java/com/medclinic/SqlServerGoogleLoginIntegrationTest.java)

Để thử luồng hiện tại, cấu hình email theo [hướng dẫn Gmail OTP](GMAIL_OTP_SETUP.md), rồi hoàn tất Google → xác nhận email trên trình duyệt thật. Test với nhà cung cấp giả lập không thay thế lần thử tích hợp thực tế này.
