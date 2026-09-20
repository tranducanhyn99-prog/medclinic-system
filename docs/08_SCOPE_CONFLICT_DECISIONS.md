# Quyết định phạm vi và đối chiếu nguồn

**Ngày đối chiếu:** 19/09/2026 (Asia/Ho_Chi_Minh)<br>
**Nguồn chuẩn hiện tại:** mã nguồn, migration/entity, và kết quả lệnh kiểm tra được lưu trong working tree. Tài liệu thiết kế và DOCX là mô tả mục tiêu, không phải bằng chứng thay thế cho mã nguồn đã chạy.

## Quyết định

| Mục | Quyết định | Bằng chứng / hệ quả |
|---|---|---|
| Trạng thái kiểm chứng | Chỉ công bố tính năng hoàn tất sau khi mã nguồn, build và kiểm thử phù hợp có bằng chứng. | Bằng chứng được giữ trong test source, JaCoCo, `docs/performance/TEST_PLAN.md` và tài liệu triển khai tương ứng. |
| RTM | Mã `TC-*` trong RTM là kế hoạch kiểm thử, trừ khi cột kiểm chứng nêu rõ class/method nguồn và một lần chạy thành công. | Tránh đồng nhất ID kế hoạch với test đã chạy. |
| Tài liệu CSDL/DOCX | Không regenerate DOCX khi build source còn lỗi hoặc mô hình entity đang thay đổi. | Git chỉ lưu 5 DOCX canonical trong `docs/generated`; generator nội bộ được giữ ngoài version control và DOCX chỉ cập nhật sau render QA. |
| WebSocket hàng chờ bác sĩ | **Đã triển khai.** Check-in/bắt đầu khám/hoàn tất khám phát `DoctorQueueChangedEvent`; listener `AFTER_COMMIT` gửi STOMP user destination `/user/queue/doctor` tới đúng tài khoản bác sĩ. | `WebSocketConfig`, `DoctorQueueWebSocketListener`, Doctor Desk client và bộ test AFTER_COMMIT/rollback. |
| VNPay, audit trail | Giữ là backlog; không suy diễn đã có tính năng từ enum, dependency hoặc mô tả thiết kế. | Chưa có callback/IPN VNPay hoặc audit trail nghiệp vụ đầy đủ trong source hiện tại. |
| Hiệu năng | Baseline local đã được đo cho các trang đại diện và đều dưới mục tiêu p95 1,5 giây; production/CDN vẫn là nghiệm thu riêng. | Kế hoạch và biểu mẫu kết quả nằm ở `docs/performance/TEST_PLAN.md`. |

## Điều kiện cập nhật lại

Khi một thay đổi schema hoặc nghiệp vụ được merge, cập nhật đồng thời entity/migration, `03_DATABASE_DESIGN_3NF.md`, RTM và 5 DOCX canonical; generator nội bộ không commit; sau đó regenerate và kiểm tra trực quan DOCX. Với thay đổi UI, kiểm tra desktop, 768px, 390px và điều hướng bằng bàn phím trước khi ghi nhận NFR-USAB.
