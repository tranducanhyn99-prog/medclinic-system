# Trạng thái xử lý 7 mục GitHub — 03/10/2026

**Đã hoàn tất 7/7 mục.** Source commit `a83db02` đã push lên `feature/frontend-design-system`; [CI thực tế đã đạt](https://github.com/tranducanhyn99-prog/medclinic-system/actions/runs/37093057483). Kết quả mới nhất nằm trong [bàn giao source](GITHUB_RELEASE_2026-10-03.md); phần bên dưới giữ nguyên bối cảnh kiểm tra ban đầu.

- [x] **GH-01:** Spring Boot 4.1.1 / Security 7.1.1 / Framework 7.0.9; Tomcat 11.0.26, Jackson 3.1.7. Rà OSV 131 tọa độ runtime, không có finding trong lần rà cuối.
- [x] **GH-02:** Policy chung tối đa 72 byte UTF-8, DTO/service/encoder và regression HTTP đăng nhập ASCII/Unicode; giữ hash BCrypt cũ.
- [x] **GH-03:** CI có Maven clean verify, Node 24 cases, link checker; trigger push main/feature/codex và PR main.
- [x] **GH-04:** Ignore secret/output/hook/bằng chứng camera-mail riêng; dọn 54 file tạm khoảng 10 MB, giữ license và bằng chứng DEMO được chọn.
- [x] **GH-05:** README mới, link tương đối, kiểm tra link trong CI; số liệu cũ giữ dưới dạng lịch sử; cập nhật runbook V1–V6.
- [x] **GH-06:** URL ca COMPLETED redirect sau kiểm tra quyền, thông báo đã lưu; không mở form tạo bệnh án mới.
- [x] **GH-07:** Clean verify 214/214, gồm 25 SQL Server integration test trên DB riêng và 6 test artifact/schema; staged audit đạt; push thường `a83db02`; GitHub Actions Maven/Node/docs đạt.

---

# Kiểm tra ban đầu — dữ liệu lịch sử

**Kết luận: chưa sẵn sàng để bàn giao và đẩy bản hiện tại.** Build/test cơ bản đạt, nhưng dependency xác thực có lỗi đã công bố và tái hiện; CI chưa chạy regression frontend; bộ file đưa lên GitHub và tài liệu chưa được chuẩn hóa. Theo yêu cầu điều kiện của người dùng, lượt này lập tasklist, chưa commit/push và chưa xóa file hoặc sửa source/README.

Đích Git đang cấu hình: `https://github.com/tranducanhyn99-prog/medclinic-system.git`. Nhánh hiện tại: `feature/frontend-design-system`, HEAD `8a9beba`, ahead 2 theo tracking ref local. Không fetch trong lượt này, nên đây không phải xác nhận trạng thái remote mới nhất. Trước khi thêm báo cáo này, Git status có 122 dòng thay đổi/file/thư mục chưa tracked.

## Bằng chứng kiểm tra hôm nay

| Kiểm tra | Kết quả |
|---|---|
| Maven `verify`, Java 21, dependency cache offline | BUILD SUCCESS, hoàn tất 09:00:30 +07, 205 test / 69 suite; 180 pass, 25 SQL Server skipped, 0 failure/error |
| JaCoCo gate trong `verify` | Đạt LINE ≥70%, BRANCH ≥50%; không công bố coverage riêng của một clean run vì lượt này không dùng `clean` |
| Node `node --test src/test/frontend/ui-regressions.cjs` | 24/24 pass, không skip |
| `git diff --check` với CRLF hợp lệ | Đạt |
| Rà 498 đường dẫn tracked/untracked ứng viên | File văn bản không khớp secret hiện có trong `.env` hoặc pattern private key/GitHub/AWS đã kiểm tra |
| Hai commit chưa push theo tracking ref local | Không khớp secret hiện có trong `.env`; không phải scan toàn bộ lịch sử Git |
| Ignore | `.env`, `.env.production` được ignore; `output/`, `.codex/hooks.json`, `.impeccable/` chưa được ignore; `*.log` đang ignore cả log bằng chứng mà docs có liên kết |
| Workflow | Chỉ push/PR `main`, chạy Maven; thiếu bước Node regression và thiếu trigger push nhánh hiện tại |

Logs/reproducer nội bộ: `output/github-readiness-verify-2026-10-03.log`, `output/github-readiness-node-2026-10-03.log`, `output/github-readiness-bcrypt-2026-10-03.log`, `output/ReadinessBcryptCheck.java`. Output không phải bộ file đề xuất đưa lên GitHub. Scan giới hạn ở ứng viên Git/file văn bản và các pattern nêu trên; không tuyên bố đã kiểm tra mọi định dạng, ảnh camera hoặc toàn bộ lịch sử secret.

## Tasklist trước khi đẩy

- **GH-01 · P1 — Cập nhật dependency bảo mật bằng BOM tương thích.** `pom.xml` đang dùng Spring Boot 3.3.5; BOM/cache và JAR hiện tại dùng Spring Security 6.3.4, Spring Framework 6.1.14, Tomcat 10.1.31. Spring Security 6.3.4 nằm trong dải ảnh hưởng của [CVE-2025-22228](https://spring.io/security/cve-2025-22228/). Đã chạy trực tiếp BCrypt của dependency với fixture giả: hash mật khẩu 72 ký tự, `matches` với cùng tiền tố cộng một ký tự khác trả **true**. `SecurityConfig` dùng chính BCrypt này trong `DaoAuthenticationProvider`. Đây là tái hiện ở thư viện, không phải thử chiếm tài khoản hay test HTTP đăng nhập. **Hoàn thành khi:** chọn BOM còn được hỗ trợ và đã có bản vá, rà dependency transitive, regression mật khẩu dài không còn được chấp nhận sai; không chỉ lên 6.3.8 vì bản đó có [CVE-2025-22234](https://spring.io/security/cve-2025-22234/). Rà thêm [advisory Spring Framework](https://spring.io/security/cve-2025-41249/) và [Tomcat](https://tomcat.apache.org/security-10.html) theo điều kiện áp dụng; không suy diễn tất cả advisory đều khai thác được trên app này.

- **GH-02 · P1 — Thống nhất validation mật khẩu và giới hạn BCrypt.** Đăng ký/đổi/reset đang giới hạn theo `String.length()`; `AdminDoctorCreateDto` chỉ có min 8 và `DoctorServiceImpl.registerDoctor` encode trực tiếp. Kiểm tra giả với 40 ký tự Unicode / 80 byte vẫn encode được trên thư viện hiện tại. BCrypt có giới hạn byte, nên giới hạn ký tự hiện tại không giải quyết đầy đủ và có thể đổi hành vi sau cập nhật. **Hoàn thành khi:** validation dùng chung ở DTO/service, xử lý Unicode và giới hạn 72 byte nếu tiếp tục dùng BCrypt, input lỗi trả thông báo rõ; test đăng ký/đổi/reset/tạo bác sĩ và đăng nhập không chấp nhận mật khẩu bị cắt tiền tố. Không đổi mật khẩu người dùng hoặc loại encoder hiện hữu mà thiếu phương án tương thích hash.

- **GH-03 · P1 — Đưa frontend regression vào CI và chạy đúng nhánh bàn giao.** `.github/workflows/ci.yml` chưa chạy 24 Node cases; push nhánh `feature/frontend-design-system` không khởi động CI hiện tại. Các tag checkout v7/setup-java v6 là tag chính thức hợp lệ, không cần hạ phiên bản dựa trên giả định. **Hoàn thành khi:** thiết lập Node và chạy regression, trigger có chủ đích cho nhánh được push hoặc mở PR vào main; Maven/Node đều chặn khi lỗi. Sau push cần kiểm tra workflow thật, không coi local pass là GitHub Actions pass.

- **GH-04 · P1 — Chuẩn hóa bộ file commit và dọn artifact local.** `output/` có 62 file (~10,7 MB) gồm HTML mẫu/log/reproducer; `.codex/hooks.json` chứa đường dẫn skill riêng trên máy Windows; `.impeccable/` chứa bản critique phát sinh. **Hoàn thành khi:** ignore các thư mục local này và biến thể file `.env` có secret, giữ hai `.env*.example`; cập nhật `.dockerignore` tương ứng. Chọn ảnh/JSON/report nghiệm thu cần giữ; audit/che khung hình camera cá nhân và dữ liệu nhạy cảm trước khi công bố. Log bằng chứng hiện bị `*.log` loại trừ: chỉ đưa vào log đã làm sạch có chủ đích hoặc thay link bằng bản tóm tắt được commit. Chuyển tài liệu lịch sử sang archive khi hợp lý; không xóa mù toàn bộ `docs/evidence`, model `MedClinic.eapx`, license, font, vendor asset hoặc file còn được tham chiếu. Dọn output tạm sau khi giữ bằng chứng cần thiết.

- **GH-05 · P2 — Viết lại phần trạng thái/kiểm thử README và sửa link docs.** README còn gọi mốc 194 tests / JS 9 là “kết quả hiện tại”, ghi preview 18087 như môi trường còn chạy và chưa phản ánh UX/UI 16/16. Nhiều link báo cáo là `C:/Users/...`, không dùng được trên GitHub. **Hoàn thành khi:** phần chính nêu cách chạy H2/Compose, env/secret, Node + Maven, kết quả mới nhất kèm phạm vi skipped; chuyển mốc cũ sang lịch sử; dùng link tương đối; kiểm tra README image/link và mọi evidence được trỏ đến đều thuộc bộ commit. Phân biệt demo/local pass với production/ngân hàng/SMTP thật chưa nghiệm thu.

- **GH-06 · P2 — Hoàn thiện route đọc ca đã kết thúc.** Nghiệm thu UX-10 đã ghi: truy cập trực tiếp `/doctor/examine/1` sau COMPLETED vẫn render form trống và nhãn “Nội dung chưa được lưu”, dù tiền sử có bệnh án và bàn khám ghi “Bệnh án đã lưu”. Backend chặn lưu trùng; đây là vấn đề trình bày khi vào URL cũ. **Hoàn thành khi:** ca hoàn tất có view đọc hoặc redirect/thông báo rõ, không còn CTA tạo bệnh án mới; regression giữ quyền truy cập và chặn ghi trùng. Đây là follow-up ngoài phạm vi 16 task UX/UI đã nghiệm thu, không phải lỗi ghi đè đã được chứng minh.

- **GH-07 · P1 — Kiểm chứng và commit/push bộ cuối.** Sau GH-01–05 và xử lý/quyết định rõ GH-06, chạy `mvn clean verify` từ source cuối, Node 24 cases và SQL Server integration trên **database disposable riêng** theo guard/runbook; không dùng DB browser chứa ca cần giữ. Kiểm tra staged diff, ignore, secret và link một lần cuối; commit các nhóm thay đổi có mục đích, không `git add .` khi artifact chưa được chọn. Đọc remote mới nhất và dùng push thường lên nhánh bàn giao; không force-push hoặc merge main ngoài yêu cầu. **Hoàn thành khi:** commit đã push đúng repo/nhánh, CI thật hoàn tất và báo lại SHA/link; ghi rõ mọi SQL/provider case chưa chạy.

## Những việc không chặn việc công bố source demo sau khi xử lý các mục trên

Nghiệm thu ngân hàng thật, SMTP nhà cung cấp, HTTPS production và matrix thiết bị toàn diện thuộc gate triển khai riêng. 25 SQL Server cases bị skip hôm nay do không bật môi trường IT; kết quả này không phủ nhận nghiệm thu SQL Server trước đó, nhưng cần kiểm tra lại sau thay đổi dependency. Không cần thực hiện thanh toán thật để đủ điều kiện đẩy source lên GitHub.
