# Dọn repo — 10/10/2026

Dọn ở working tree; chưa commit/push. Các thay đổi giao diện từ phiên trước được giữ nguyên, trừ asset/script cũ đã xác nhận không còn dùng. Không sửa nghiệp vụ trong lượt dọn này.

## Đã loại bỏ

| Nhóm | Số file | Lý do |
| --- | ---: | --- |
| Ảnh JPG/WebP của 5 banner, nền team và hero cũ | 14 | Không còn được tham chiếu trong template, CSS, JavaScript, Java, test hay script vận hành hiện tại. Các tên ảnh xuất hiện trong JSON nghiệm thu cũ là ghi nhận lịch sử. |
| Sidecar nguồn gốc của 7 ảnh WebP đã bỏ | 7 | Đi kèm những asset đã loại bỏ; không còn asset để mô tả. |
| `src/main/resources/static/js/home-carousel.js` | 1 | Không template/script/test nào tải hoặc gọi; trang chủ mới không có carousel. |
| Log build/test ở thư mục gốc | 7 | File sinh tự động, đã được ignore; kết quả kiểm tra mới được tổng hợp riêng. |
| `docs/assets/readme/doctors.png` | 1 | Screenshot giao diện cũ, chỉ README trước đây sử dụng. README mới dùng ảnh home/login theo bảng màu được duyệt. |
| **Tổng** | **30** | Danh sách từng file và checksum được giữ trong manifest local. |

Ảnh/script runtime bỏ đi chiếm **5.05 MiB** (22 file, gồm sidecar). Tổng 30 file là **9.315.770 byte, khoảng 8.88 MiB**; đây là lượng file đưa khỏi cây nguồn, không phải lượng dung lượng đã giải phóng trên đĩa vì bản sao khôi phục vẫn tồn tại. Nhóm này gồm cả file tracked, untracked và log đã ignore, không phải 30 file đều từng được commit.

Đã cập nhật hai ảnh README từ capture thiết kế ngày 09/10/2026. Thêm `/tmp/` vào `.gitignore` và `tmp/` vào `.dockerignore` để file tạm không đi vào commit/build context. Không thêm dependency.

## Giữ lại vì có mục đích

- Rubric Excel ở gốc: tiêu chí môn học, dùng để soạn phần thuyết trình; không sửa nội dung.
- `MedClinic.eapx`, hai bản XML UML, diagram và năm DOCX trong `docs/generated`: nguồn mô hình và báo cáo môn học; không suy ra là file thừa chỉ vì ứng dụng không đọc.
- `src/main/java/com/medclinic/generator`: có entry point `GenerateTemplateDocs`, tái tạo DOCX/diagram bằng JDK. Chưa chuyển khỏi module runtime trong lượt này.
- Migration V1–V6, script init/backup/restore, template env, license thư viện/font, test và fixture: cần cho tái lập/vận hành.
- Báo cáo nghiệm thu theo ngày và bằng chứng công khai được liên kết: lịch sử kiểm chứng. README mới chỉ dẫn tới tài liệu đang cần, không xóa lịch sử để làm danh sách ngắn hơn.
- `.env`, backup, uploads và cấu hình máy cá nhân: không xóa. `target` được Maven tạo lại khi chạy verification. `.codex`, `.impeccable`, `output` và bằng chứng private đã ignore, không đưa vào Git; chúng chứa dữ liệu phục hồi/phiên làm việc nên không quét xóa hàng loạt.

## Khôi phục file đã bỏ

Bản sao giữ tại `output/repo-cleanup-2026-10-10/removed/`, theo đúng đường dẫn tương đối cũ. Manifest ở `output/repo-cleanup-2026-10-10/manifest.json` có số byte và SHA-256. Mọi bản sao được so checksum trước khi xóa file gốc.

Ví dụ khôi phục một file, chạy từ gốc repo:

```powershell
Copy-Item -LiteralPath 'output/repo-cleanup-2026-10-10/removed/src/main/resources/static/js/home-carousel.js' -Destination 'src/main/resources/static/js/home-carousel.js'
```

Chỉ khôi phục asset/script nếu có chức năng thực sự cần lại. Không phục hồi file log vào source và không đưa thư mục archive local lên Git.

## Kiểm chứng

Tìm tham chiếu theo tên file trong source/test/config/script, loại trừ thư viện minified bên thứ ba và sidecar tự mô tả. Đối chiếu thêm tài liệu/JSON lịch sử. Sau dọn chạy lại clean build, test Java, test frontend và kiểm tra link; kết quả cuối ghi ở [review dự án](PROJECT_REVIEW_2026-10-10.md).
