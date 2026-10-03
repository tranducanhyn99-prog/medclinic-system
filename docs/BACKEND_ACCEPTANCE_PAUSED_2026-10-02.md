# Nghiệm thu backend: tạm dừng theo yêu cầu người dùng

> Trạng thái lịch sử: người dùng đã yêu cầu tiếp tục ngày 02/10/2026. Công việc còn dở đã hoàn tất; kết quả mới nhất trong [báo cáo nghiệm thu](BACKEND_ACCEPTANCE_2026-10-02.md).

Ngày 2026-10-02, người dùng yêu cầu tạm ngừng test và sẽ cho phép chạy lại sau. Không tự động tiếp tục kiểm thử.

## Kết quả đã thu được

- Camera thật: người dùng xác nhận webcam hiển thị hình thật và tự động quét QR thành công. Tiếp nhận tìm đúng lịch hẹn và check-in thành công.
- Lượt `clean verify` hoàn tất trước sửa đổi cuối: 180 tests, 0 failures, 0 errors, 0 skipped; SQL Server integration tests đã chạy. JaCoCo line 81.22%, branch 59.63%.
- Frontend regression tests: 9/9 passed trước sửa đổi Java cuối.
- Trình duyệt trên môi trường SQL Server riêng: đăng ký, SMTP qua Mailpit, OTP sai/đúng, đăng nhập bằng magic link, đặt lịch, check-in và mở màn khám đã thực hiện. Form khám không hợp lệ giữ lại dữ liệu đã nhập.
- Bằng chứng: `docs/evidence/backend-2026-10-01/`.

## Điểm dừng và giới hạn kết quả

- Sửa đổi mới nhất cho phép dòng thuốc có hướng dẫn dùng tùy chọn để trống; đã thêm test HTTP tương ứng cho H2 và SQL Server. Sửa đổi này chưa có lượt kiểm tra hoàn tất.
- Lượt Maven dự kiến 182 tests (session 45731) và Docker build (session 17434) đã được ngắt bằng Ctrl+C theo yêu cầu tạm dừng. Không tính lượt bị ngắt là đạt hoặc thất bại chức năng.
- Log tạm của lượt cuối tại `%TEMP%/medclinic-backend-clean-verify.log` và `%TEMP%/medclinic-acceptance-docker-build.log` đã ghi đè log cũ; có thể chưa đầy đủ. `target/` cũng có thể chứa kết quả chưa hoàn tất.
- Container ứng dụng đang chạy bản trước sửa đổi tùy chọn hướng dẫn dùng. Không coi mã mới đã được triển khai vào container này.
- Chưa hoàn tất lưu bệnh án hợp lệ và thanh toán tiền mặt qua trình duyệt; chưa chạy backup/restore drill. Callback ngân hàng đã kiểm tra bằng HTTP integration tests với dữ liệu giả, chưa nghiệm thu nhà cung cấp ngân hàng thật.

## Môi trường được giữ lại

- Ứng dụng nghiệm thu: http://127.0.0.1:18087, container `medclinic-acceptance-app`.
- SQL Server riêng: `medclinic-acceptance-sqlserver`, cổng 14334; `medclinic_db` giữ dữ liệu trình duyệt, `medclinic_e2e` dùng cho tests.
- Mailpit: http://127.0.0.1:18025, container `medclinic-acceptance-mailpit`.
- Network `medclinic-acceptance`; uploads volume `medclinic-acceptance-uploads`. Giữ nguyên các container và dữ liệu; không xóa volume khi tiếp tục.
- Không sử dụng dữ liệu của các container gốc `medclinic-app`/`medclinic-sqlserver` cho nghiệm thu này. Preview H2 cổng 18086 đã dừng trong quá trình build trước đó.
- Tab khám được giữ nguyên tại `/doctor/examine/1`. Lịch hẹn DEMO `MED-261002-6EAF8B62`, trạng thái `IN_PROGRESS`; chưa lưu bệnh án/hóa đơn trong luồng trình duyệt này.
- Nội dung form thử: triệu chứng và chẩn đoán đang chỉ có khoảng trắng; lời dặn `DEMO: giữ lời dặn khi validation thất bại`; thuốc Paracetamol ID 1, số lượng 2, liều `DEMO liều thử`, tần suất `DEMO tần suất thử`, hướng dẫn dùng trống.

## Khi người dùng cho phép tiếp tục

1. Chạy lại kiểm tra cuối trên mã mới với SQL Server riêng, lưu log hoàn chỉnh làm bằng chứng.
2. Build và cập nhật riêng container nghiệm thu, bảo toàn database và uploads.
3. Tiếp tục ca khám DEMO hợp lệ và luồng thu tiền; xác nhận lưu bệnh án, tồn kho, hóa đơn và PDF.
4. Nghiệm thu backup/restore bằng tham số container riêng trong `ops/backup.ps1` và `ops/restore-drill.ps1`.
5. Cập nhật báo cáo nghiệm thu và backlog theo kết quả thực tế, phân biệt kiểm thử local với tích hợp nhà cung cấp thật.
