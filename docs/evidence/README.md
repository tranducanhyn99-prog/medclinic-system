# Bằng chứng nghiệm thu được công bố

Các ca trong báo cáo là fixture DEMO/local, không đại diện cho dữ liệu bệnh nhân thật hoặc giao dịch ngân hàng thật. Ảnh/JSON đã chọn được lưu theo ngày; kết quả từng lượt nằm trong các báo cáo liên kết từ README dự án.

Log thô, mail OTP/magic-link, ảnh camera cá nhân và đường dẫn backup của máy nghiệm thu được giữ **local**, không đưa lên GitHub. Các báo cáo lịch sử trỏ đến trang này khi bằng chứng gốc thuộc nhóm đó; việc thay link không phải một lần chạy lại kiểm thử.

## Các mốc đã ghi nhận

| Mốc | Báo cáo công khai |
|---|---|
| Backend, camera thật do người dùng xác nhận, SMTP Mailpit, backup/restore | [Nghiệm thu backend](../BACKEND_ACCEPTANCE_2026-10-02.md) |
| 22 mục backlog local, 194/194 test với SQL Server riêng tại thời điểm đó | [Đối chiếu backlog](../BACKLOG_COMPLETION_2026-10-02.md) |
| UX/UI chuyên sâu, 15 task trước khi chốt UX-10 | [Chốt 6 task](../IMPECCABLE_FINAL_SIX_2026-10-02.md) |
| UX-10, một ca DEMO và hóa đơn 353 000 đ chưa thanh toán | [Nghiệm thu UX-10](../UX10_ACCEPTANCE_2026-10-03.md) |
| Bộ source chuẩn bị GitHub, cập nhật dependency và regression | [Bàn giao GitHub](../GITHUB_RELEASE_2026-10-03.md) |
| 5 mục hoàn thiện đồ án, 218/218 Java test, 25/25 frontend, email QR và PDF bản cuối | [Nghiệm thu đồ án](../ACADEMIC_ACCEPTANCE_2026-10-03.md) |
| Playwright CLI trước sửa: 149/152 assertion; phát hiện lỗi ngày tương lai và favicon | [Kiểm tra browser 04/10](../PLAYWRIGHT_CHECK_2026-10-04.md) |

| Sửa PW-01/PW-02: 242/242 Java, 25/25 frontend, 154/154 Playwright CLI | [Nghiệm thu hai bản sửa 04/10](../PLAYWRIGHT_FIX_ACCEPTANCE_2026-10-04.md) |

Không công bố raw log như một chứng nhận security/production. Nghiệm thu camera dựa trên xác nhận trực tiếp của người dùng; ảnh webcam không được dùng làm artifact công khai.
