# Kịch bản bảo vệ MedClinic — một người, khoảng 15 phút

Bạn trình bày một mình. Bản này mặc định 15 phút vì chưa có thời lượng từ giảng viên; gồm lời nói, thao tác và trả lời phản biện. **Trọng tâm là một ca đi qua đủ bốn vai trò, kèm giải thích code bạn có thể mở ngay.** Không cần đọc toàn bộ tài liệu thành tiếng.

**Cập nhật 12/10:** đã có đăng ký/đăng nhập Google cho bệnh nhân, sau đó xác nhận OTP email trước khi vào tài khoản. Muốn demo cần OAuth Client, cấu hình gửi thư và rehearsal theo [hướng dẫn Google](GOOGLE_LOGIN_SETUP.md) và [Gmail OTP](GMAIL_OTP_SETUP.md). Nếu chưa cấu hình, tiếp tục dùng tài khoản demo; không bấm Google lần đầu trong buổi bảo vệ.

Lời thoại dùng “em”. Trước buổi bảo vệ, thay tên/MSSV/link GitHub bằng thông tin thật và chỉ giữ những câu bạn hiểu, có thể giải thích tiếp. “Một người trình bày” không tự chứng minh một người viết toàn bộ code; phần đóng góp phải đúng với thực tế.

## Bám rubric môn học

Đối chiếu file Excel rubric giữ nguyên ở gốc repo, không tự chấm điểm thay giảng viên:

| Tiêu chí | Trọng số | Bằng chứng nên trình bày |
| --- | ---: | --- |
| Độ khó, thực tiễn, phân công | 10% | Luồng liên thông; tránh đặt trùng/thiếu thuốc/thu tiền hai lần; mô tả phần việc thực tế của mình, không đặt ra thành viên hoặc phân công giả. |
| AI hỗ trợ và cộng tác GitHub | 20% | Một ví dụ dùng AI rồi đọc/sửa/test code; history/diff/PR có thật. Nếu không có cộng tác nhóm, nói rõ cách quản lý phiên bản cá nhân. |
| Dữ liệu, vai trò, giao diện và thuyết trình | 20% | Quan hệ Appointment–MedicalRecord–Prescription–Invoice; demo bốn vai trò; bảng màu y khoa và bố cục phục vụ từng công việc. |
| Ứng dụng, code, cài đặt và chức năng | 35% | Demo hoàn chỉnh; mở service transaction; README chạy được; báo cáo test tách executed/skipped. |
| Sáng tạo ngoài chương trình | 15% | QR tiếp nhận, realtime, OTP/magic link, PDF Unicode, tồn kho/audit, HMAC/idempotency, Flyway/Docker/CI. Chỉ gọi là ngoài chương trình khi đúng với nội dung môn đã học. |

Không cần nhồi đủ “7–8 nội dung” để hứa mức điểm tối đa. Giải thích được ba nội dung có chiều sâu tốt hơn việc đọc tám tên công nghệ.

## Chuẩn bị trước giờ trình bày

1. Chọn **một môi trường đã chạy thử**: H2/dev cho demo chính; SQL Server/demo + Mailpit nếu thực sự cần trình diễn email. Không đổi database hoặc build Docker ngay trên sân khấu.
2. Làm thử toàn bộ chuỗi một lần. Với H2, khởi động lại sẽ mất dữ liệu; có thể reset và mở sẵn ứng dụng trước giờ. Với SQL Server, giữ volume và dùng ca mới, không xóa database để lấy trạng thái ban đầu.
3. Dùng tài khoản seed `patient`, `staff`, `doctor`, `admin`, mật khẩu `123456`. Tài khoản seed bypass OTP; đây không phải chứng minh OTP đã gửi. `dev` không gửi mail.
4. Chọn bác sĩ **Nguyễn Văn Hùng**, tương ứng tài khoản `doctor`; tránh đặt bác sĩ khác rồi đăng nhập `doctor` không thấy ca.
5. Chọn ngày mai, khung giờ còn trống. Dev/demo cho xử lý sớm lịch tới +14; giữ nguyên ngày hẹn và STT theo ngày. Nói rõ chế độ demo, không giải thích như quy tắc khám ngoài môi trường demo.
6. Mở sẵn README, review và các file code trong danh sách cuối tài liệu. Chuẩn bị kết quả test ngày 10/10, không chạy toàn bộ Maven trong 15 phút.
7. Dùng light mode, zoom khoảng 90–100%, đóng tab không liên quan. Bốn tab cùng browser chia sẻ session; **logout/login khi đổi vai trò**. Nếu muốn giữ bác sĩ đăng nhập để minh họa realtime, dùng browser/profile khác, đã thử trước buổi.
8. Đặt dữ liệu “Demo môn Lập trình Web”; không dùng hồ sơ bệnh nhân thật. Nhập mã hẹn thay cho camera trong demo chính để thời gian ổn định. QR camera có thể trình bày thêm khi được hỏi và thiết bị đã sẵn sàng.
9. Nếu dữ liệu seed chưa bị thay đổi: phí bác sĩ Hùng **350.000 đ**, Paracetamol **1.500 đ/viên**; hai viên làm tổng **353.000 đ**. Nếu dữ liệu đã đổi, đọc tổng thực tế và giải thích công thức, không cố khớp con số.

## Nhịp 15 phút

| Thời gian | Nội dung | Màn hình |
| --- | --- | --- |
| 00:00–01:00 | Bài toán và phạm vi | Trang chủ |
| 01:00–02:30 | Kiến trúc, dữ liệu, bốn vai trò | Sơ đồ/tài liệu hoặc source |
| 02:30–04:15 | Bệnh nhân đặt lịch | `/patient/booking` |
| 04:15–05:30 | Lễ tân tiếp nhận | `/staff/reception` |
| 05:30–08:00 | Bác sĩ khám, kê đơn; giải thích transaction | `/doctor/desk`, form khám, service |
| 08:00–09:15 | Thu ngân, hồ sơ và PDF | `/staff/billing`, chi tiết lịch bệnh nhân |
| 09:15–10:15 | Quản trị và giao diện | Dashboard, thuốc |
| 10:15–12:00 | Phân quyền, realtime, chức năng mở rộng | Security/event/PDF/payment source |
| 12:00–13:30 | Test, cài đặt, AI/GitHub | README, kết quả verify, diff/history |
| 13:30–15:00 | Giới hạn, hướng hoàn thiện và kết thúc | Review hiện tại |

Nếu có slide, bảng trên có thể gom thành tám slide: bài toán; kiến trúc/dữ liệu; bệnh nhân; lễ tân/bác sĩ; thu ngân/quản trị; kỹ thuật; kiểm thử/AI; giới hạn. Tài liệu này là kịch bản, chưa tạo file PowerPoint.

## Lời thoại và thao tác

### 00:00–01:00 — Mở đầu

**Mở trang chủ. Nói:**

> Em xin trình bày MedClinic, đồ án quản lý và đặt lịch khám. Bài toán em chọn là kết nối các bước của một lượt khám: bệnh nhân đặt lịch, lễ tân tiếp nhận, bác sĩ ghi kết quả và kê đơn, cuối cùng thu ngân thanh toán. Khi các bước dùng dữ liệu riêng, việc đổi trạng thái hoặc đối chiếu tiền dễ bị lệch. MedClinic lưu một lịch hẹn xuyên suốt quy trình để các vai trò cùng làm việc trên ca đó.
>
> Phạm vi là một phòng khám với bốn vai trò. Trong phần demo, em sẽ đi theo một lịch hẹn từ lúc tạo đến lúc có hồ sơ và hóa đơn. Dữ liệu trên màn hình là dữ liệu mẫu của đồ án.

Không kể tên tất cả thư viện ở phần mở đầu. Chuyển ngay sang cách tổ chức hệ thống.

### 01:00–02:30 — Kiến trúc và dữ liệu

**Hiển thị cấu trúc source hoặc mô hình trong tài liệu. Nói:**

> Backend dùng Java 21 và Spring Boot. Controller nhận request, service xử lý nghiệp vụ, repository làm việc với database qua JPA. Thymeleaf render HTML từ server; JavaScript gọi API để tải giờ khám và nhận cập nhật hàng chờ. Với quy mô đồ án, cách này giúp luồng xử lý rõ và dễ triển khai trong một ứng dụng.
>
> Trung tâm dữ liệu là Appointment: liên kết bệnh nhân, bác sĩ, ngày và khung giờ. Khi khám xong, ca đó có MedicalRecord; bệnh án có Prescription và các dòng thuốc. Mỗi dòng giữ số lượng và giá tại thời điểm kê. Invoice gắn với lịch hẹn, gồm phí khám và tiền thuốc. Nhờ vậy, đổi giá thuốc sau này không làm đơn thuốc cũ đổi giá.
>
> Bốn vai trò có màn hình theo công việc: bệnh nhân chọn lịch; lễ tân tra cứu và cấp số; bác sĩ làm việc với hàng chờ; admin quản lý dữ liệu danh mục. H2 dùng để chạy nhanh trên máy; SQL Server có migration Flyway để theo dõi schema ở môi trường demo triển khai.

**Chỉ vào quan hệ:** Appointment → MedicalRecord → Prescription → PrescriptionItem → Medicine; Appointment → Invoice. Mã QR, PDF và email đều dựa trên các dữ liệu này, không phải một bộ dữ liệu khác.

### 02:30–04:15 — Bệnh nhân đặt lịch

**Thao tác:** đăng nhập `patient` → đặt lịch → chọn Nguyễn Văn Hùng → ngày mai → giờ trống → nhập “Demo môn Lập trình Web” → xác nhận. Mở chi tiết lịch, ghi lại booking code và appointment ID thực tế; không dùng ID cố định.

> Em bắt đầu bằng vai trò bệnh nhân. Người dùng chọn bác sĩ, ngày và giờ còn trống, sau đó nhập lý do khám. Khi gửi form, backend kiểm tra lại dữ liệu: bác sĩ còn nhận lịch không, khung giờ có thuộc đúng bác sĩ và ngày đã chọn không, và còn trống không. Vì vậy, sửa dữ liệu trên trình duyệt không bỏ qua được các kiểm tra này.
>
> Để tránh hai người đặt cùng một giờ, service khóa bản ghi khung giờ trong transaction trước khi đổi trạng thái thành đã đặt. Người đến sau phải kiểm tra lại trạng thái sau khi lấy khóa. Sau khi tạo thành công, lịch có mã hẹn và QR để lễ tân tra cứu. QR hỗ trợ tìm lịch; quyền thao tác vẫn phải do tài khoản lễ tân quyết định.
>
> Em chọn ngày mai để demo không phụ thuộc giờ hiện tại. Profile dev/demo có chế độ xử lý sớm, còn ngoài hai profile đó việc tiếp nhận và khám yêu cầu đúng ngày.

**Mốc kiểm tra:** ca mới ở `CONFIRMED`. Không hủy ca này, vì còn dùng cho các bước sau.

### 04:15–05:30 — Lễ tân tiếp nhận

**Thao tác:** logout → login `staff` → `/staff/reception` → tìm booking code → “Tiếp Nhận & Cấp STT”. Nếu bộ lọc đang chỉ hôm nay, dùng tra cứu phù hợp/chế độ lịch demo tương lai và kiểm tra đúng mã, ngày.

> Lễ tân tìm lịch bằng mã hẹn. Có thể quét QR bằng camera; trong demo em nhập mã để thao tác nhanh. Khi tiếp nhận, trạng thái chuyển từ đã xác nhận sang đã check-in, đồng thời cấp số thứ tự theo ngày khám. Mã hẹn dùng để nhận diện ca; số thứ tự dùng cho hàng chờ, nên hai giá trị này có mục đích khác nhau.
>
> Sau khi transaction commit, hệ thống phát sự kiện cập nhật hàng chờ bác sĩ. Nếu thao tác bị rollback, không nên gửi một thông báo thành công cho màn hình bên kia. Ngoài lịch đặt trước, hệ thống có đăng ký vãng lai trong ngày; em để phần đó ngoài ca chính để giữ đúng thời gian.

**Mốc kiểm tra:** `CHECKED_IN`, STT có giá trị. Nếu chỉ một browser, lát nữa login bác sĩ sẽ tải lại queue; không gọi thao tác đổi tài khoản rồi tải trang là bằng chứng realtime.

### 05:30–08:00 — Bác sĩ và transaction

**Thao tác:** logout → login `doctor` → `/doctor/desk` → chọn ca vừa tiếp nhận → “Bắt đầu khám”. Form khám mở ở `/doctor/examine/{appointmentId}` sau POST bắt đầu khám.

> Bác sĩ chỉ xử lý ca thuộc mình. Bước bắt đầu khám chuyển trạng thái từ check-in sang đang khám. Khi mở form, backend kiểm tra bác sĩ phụ trách, vì ẩn nút trên giao diện chưa đủ ngăn người khác gửi request trực tiếp.

**Điền hồ sơ mẫu:** triệu chứng “Dữ liệu mô phỏng cho đồ án”; chẩn đoán “Hồ sơ demo, không dùng cho điều trị”; ghi chú “Minh họa quy trình phần mềm”. Chọn Paracetamol 500mg, số lượng 2. Các ô liều dùng/tần suất nhập “Demo” nếu form yêu cầu; không trình diễn như tư vấn điều trị. Bỏ ngày tái khám hoặc chọn sau hôm nay theo validation.

**Nhấn “Hoàn tất khám và lưu bệnh án”. Sau đó mở `MedicalRecordService.saveMedicalRecordAndPrescription`. Nói:**

> Điểm quan trọng của ca này là lưu hồ sơ liên quan đến nhiều bảng. Service kiểm tra ca đang khám, dữ liệu chẩn đoán và các dòng thuốc. Nếu một thuốc xuất hiện nhiều dòng, hệ thống cộng số lượng trước khi kiểm tra tồn kho. Các thuốc được khóa theo ID tăng dần để giảm việc hai ca cùng giữ các khóa theo thứ tự ngược nhau.
>
> Khi đủ tồn kho, service ghi biến động xuất kho, lưu giá lúc kê vào từng dòng đơn, lưu bệnh án và tạo hóa đơn. Các bước này nằm trong cùng transaction. Nếu phát hiện thiếu thuốc hoặc lỗi lưu hóa đơn, phần ghi dữ liệu của transaction bị rollback, tránh trường hợp đã trừ kho nhưng không có hồ sơ hoàn chỉnh.
>
> Sau khi thành công, ca chuyển sang hoàn tất. Hóa đơn lúc này mới ở trạng thái chưa thanh toán; khám xong không đồng nghĩa thu ngân đã nhận tiền.

**Mốc kiểm tra:** `COMPLETED`, có hồ sơ và Invoice `UNPAID`. Nếu thầy/cô muốn xem lỗi thiếu thuốc, dùng một ca phụ đã chuẩn bị; không phá ca chính ngay trên sân khấu.

### 08:00–09:15 — Thu ngân, hồ sơ và PDF

**Thao tác:** logout → login `staff` → `/staff/billing` → tìm đúng hóa đơn → kiểm tra phí → chọn thu tiền mặt và xác nhận. Sau đó logout/login `patient`, mở chi tiết lịch và tải PDF có sẵn.

> Với dữ liệu seed chưa đổi, phí khám là 350.000 đồng và tiền hai viên thuốc là 3.000 đồng; tổng là 353.000 đồng. Em chọn tiền mặt để hoàn thành ca chính. Backend khóa hóa đơn và kiểm tra chưa thanh toán trước khi đổi thành đã trả, nên gửi lại request không được thu thêm lần nữa.
>
> Bệnh nhân quay lại sẽ thấy kết quả của ca mình và tải tài liệu PDF. Font Unicode giữ dấu tiếng Việt. Trước khi xuất file, service còn kiểm tra vai trò và quan hệ với hồ sơ; không phải ai biết ID hóa đơn cũng tải được.

**Nếu được hỏi chuyển khoản:** có VietQR và callback HMAC trong source, nhưng mặc định chỉ bật khi đủ cấu hình. Không bật trong lúc bảo vệ nếu chưa rehearsal. Không nhập mã giả rồi nói đã được ngân hàng xác nhận; đối soát thủ công là thao tác của staff và có audit.

### 09:15–10:15 — Quản trị và giao diện

**Thao tác:** logout/login `admin` → dashboard → danh sách thuốc/lịch sử tồn kho; chỉ đọc để không làm đổi dữ liệu demo.

> Admin quản lý chuyên khoa, bác sĩ, tài khoản và thuốc. Sau ca vừa rồi, tồn kho thuốc giảm theo số lượng đã kê và có ghi nhận xuất kho. Dashboard tổng hợp hoạt động; dữ liệu dashboard không thay cho chi tiết hóa đơn khi đối soát.
>
> Giao diện dùng xanh lam y khoa và nền trắng. Bệnh nhân được dẫn theo bước chọn bác sĩ, ngày và giờ; lễ tân và bác sĩ ưu tiên mã hẹn, số thứ tự và trạng thái. Màu trạng thái đi cùng nhãn chữ để người dùng không cần suy luận chỉ bằng màu. Có chế độ tối và bố cục cho màn hình nhỏ, nhưng buổi này em dùng chế độ sáng để nội dung dễ nhìn khi chiếu.

### 10:15–12:00 — Phân quyền và phần mở rộng

**Chọn tối đa ba đoạn code để chỉ nhanh; không đọc cả file. Nói:**

> Phân quyền có nhiều lớp. Spring Security chặn route theo vai trò và bảo vệ các thao tác POST bằng CSRF. Ở luồng nghiệp vụ có kiểm tra thêm, chẳng hạn bác sĩ phải phụ trách lịch và bệnh nhân phải sở hữu hồ sơ. Hai lớp giải quyết hai câu hỏi khác nhau: người này có vai trò phù hợp không, và người này có quyền với đúng bản ghi này không?
>
> Với realtime, server phát sự kiện sau khi dữ liệu commit; listener gửi thông báo qua STOMP tới hàng chờ bác sĩ. API chọn khung giờ lại là request-response từ JavaScript. Em phân biệt hai cơ chế này thay vì gọi mọi cập nhật giao diện là realtime.
>
> Một số phần mở rộng là OTP/magic link, QR tiếp nhận và xuất PDF. Ở chuyển khoản, HMAC kiểm tra chữ ký callback, service đối chiếu số tiền và reference, đồng thời tránh dùng lại mã giao dịch. Đây là phần xử lý tích hợp trong đồ án; việc kết nối ngân hàng thật vẫn cần kiểm chứng với nhà cung cấp.

**Nếu còn thời gian:** thử vào `/admin` bằng patient trên browser riêng đã chuẩn bị, hoặc mở test phân quyền. Nêu đúng response thực tế; không hứa mọi tình huống đều 403 vì request chưa đăng nhập có thể redirect login.

### 12:00–13:30 — Kiểm thử, cài đặt, AI và GitHub

**Mở README và tóm tắt verification. Nói:**

> README có hai đường chạy: Maven với H2 để bắt đầu nhanh, hoặc Docker Compose cho SQL Server và Mailpit. H2 nằm trong RAM nên mất dữ liệu khi dừng; SQL Server dùng volume và Flyway migration. Người chấm có thể theo các bước trong README để tái lập, thay vì chỉ xem video.
>
> Em mở báo cáo nghiệm thu ngày 10/10 để trình bày số test và coverage mới nhất. Suite Java mặc định và lượt SQL Server trên database riêng được ghi tách biệt; số skip không tính thành pass. Các số này là bằng chứng kiểm thử của working tree, không phải chứng nhận ứng dụng không còn lỗi.
>
> Trong quá trình hoàn thiện, AI đã hỗ trợ redesign giao diện, rà soát, sửa lỗi và tài liệu. Cách em chứng minh làm chủ code là giải thích một luồng thật, kiểm tra đề xuất bằng test và chỉ ra giới hạn. Những lỗi tái hiện ở đợt review đã trở thành regression test, như khóa tài khoản, session OTP và giá tiền VND.

**Ví dụ bạn phải rehearsal:** giải thích vì sao `[3|5|7|8|9]` nhận cả ký tự `|`, mở helper `PhoneNumbers` đã dùng `[35789]` và test từ chối `0|12345678`. Phần sửa nằm trong working tree; chỉ gọi là commit khi đã có commit thật.

**GitHub:** mở history/diff/PR thật đang có; nói phần bạn đã thực hiện hoặc đã kiểm tra. Các chỉnh sửa cleanup/README trong lượt này chưa commit/push, chưa có CI remote mới. Không lấy tên tác giả trong Git làm bằng chứng số người thật, không tạo “lịch sử cộng tác” giả để đáp ứng rubric.

### 13:30–15:00 — Giới hạn và kết thúc

**Mở báo cáo nghiệm thu, đối chiếu ba lỗi đầu với review ban đầu. Nói:**

> Chín vấn đề của review đã được xử lý và có kiểm chứng. Request tiếp theo của tài khoản bị khóa bị chặn; mở khóa không khôi phục session cũ. OTP chỉ được đọc và xác minh từ session đã tạo challenge. Số điện thoại có một quy tắc chung và constraint ở database để bảo vệ cả request đồng thời.
>
> Thu ngân đã lọc và phân trang ở database; luồng thanh toán khóa Invoice trước Payment và đã kiểm tra các tình huống đồng thời trên SQL Server riêng. Dữ liệu cũ có điện thoại trùng hoặc giá lẻ sẽ làm migration dừng để xử lý có chủ đích. Ngân hàng thật, camera và SMTP vẫn cần nghiệm thu tích hợp riêng. Google login đã có code và test, cần OAuth Client thật để nghiệm thu callback live.
>
> Phần em muốn làm rõ qua đồ án là một request web thay đổi dữ liệu thế nào: ai được phép thao tác, trạng thái nào hợp lệ, các bước nào phải cùng transaction và kết quả được đưa trở lại giao diện ra sao. Em xin kết thúc phần trình bày và sẵn sàng mở code để giải thích các câu hỏi.

## Câu hỏi phản biện cần tập

| Câu hỏi | Cách trả lời và nơi kiểm chứng |
| --- | --- |
| Vì sao chọn MVC/Thymeleaf? | Quy mô một ứng dụng, form nghiệp vụ nhiều, cần render và triển khai đơn giản. JS bổ sung ở phần chọn giờ/realtime. Nêu trade-off: muốn SPA phức tạp sau này phải thiết kế API/UI riêng, chưa cần cho phạm vi hiện tại. |
| Hai bệnh nhân đặt cùng giờ thì sao? | `AppointmentService` lấy pessimistic lock TimeSlot trong transaction, rồi kiểm tra status. Mở repository và test concurrency; đối chiếu lượt H2/SQL Server riêng trong báo cáo nghiệm thu. |
| Vì sao `@Transactional` ở service? | Một thao tác hoàn tất khám ghi nhiều bảng. Transaction giữ dữ liệu liên quan nhất quán; controller chỉ điều phối request/view. Nêu trường hợp thiếu thuốc làm rollback. |
| Giá thuốc thay đổi thì đơn cũ thế nào? | `PrescriptionItem.unitPriceAtTime` lưu snapshot lúc kê, không tính lại bằng giá hiện tại. Phí khám được đưa vào invoice khi tạo invoice; đừng khẳng định snapshot phí ngay lúc booking nếu chưa có. |
| Bệnh nhân đổi ID trên URL thì sao? | Route authentication chưa đủ; controller/service kiểm tra owner hoặc vai trò trong đọc appointment/PDF. Mở đúng điều kiện và test, không trả lời chung chung “có Spring Security”. |
| Khóa tài khoản có logout ngay không? | Request tiếp theo recheck active và securityVersion, hủy session và cookie remember-me nếu không còn hợp lệ. Mở khóa không làm session cũ hợp lệ trở lại. Socket đang mở kiểm tra trước khi gửi tiếp dữ liệu. Mở `LockedAccountSessionIntegrationTest` và `ActiveAccountFilterTest`. |
| OTP được bảo vệ thế nào, còn thiếu gì? | Challenge có hạn dùng, giới hạn attempts, giá trị xác thực được hash và guard session cho GET/verify/resend; session lạ không làm tăng attempts. CSRF/rate limit vẫn bảo vệ endpoint. Magic link dùng token riêng để mở từ email. |
| Realtime có phải polling không? | Queue dùng STOMP/SockJS qua WebSocket và event sau commit. API chọn giờ là fetch. Nếu chưa giữ hai session khác browser, mô tả code/test chứ không nói vừa thấy push thật. |
| QR thanh toán có nghĩa đã nhận tiền? | Không. QR mang hướng dẫn chuyển khoản; trạng thái paid cần callback hợp lệ hoặc đối soát staff. Callback kiểm tra HMAC/amount/reference và chống dùng lại transaction. Không có giao dịch ngân hàng thật mới trong lượt này. |
| H2 pass có bảo đảm SQL Server pass? | Không; khác dialect, constraint và cách khóa. Lượt SQL Server dùng database riêng, có fresh/legacy migration và các ca tranh chấp; mở số pass thực tế trong báo cáo. |
| Coverage có đủ kết luận không còn lỗi? | Không; coverage phản ánh code được chạy. Review ban đầu đã tái hiện lỗi dù suite xanh; test mới kiểm tra bất biến và điều kiện bị thiếu. |
| Dữ liệu nhiều thì trang nào đáng lo? | Thu ngân dùng Pageable với filter ngày/trạng thái, mặc định 25 dòng, tối đa 100. Fixture 10.000 hóa đơn có đo query H2 local trong báo cáo; không coi đó là latency trình duyệt hay production. |
| Em đã dùng AI thế nào? | Trình bày ví dụ thật: yêu cầu redesign, tự đánh giá màu, review regex/session và kiểm chứng. Mở code giải thích đầu vào, nhánh và kết quả; nêu rõ việc nào AI hỗ trợ, việc nào đã được kiểm tra. Không nhận toàn bộ code là tự viết nếu không đúng. |
| Vì sao còn EAPX/XML/generator, có phải file rác? | Là nguồn mô hình/báo cáo môn học và công cụ tái tạo DOCX. Không phải chức năng runtime nhưng có mục đích; ảnh carousel/log không còn dùng đã được đưa khỏi source và có bản sao local. |
| Hướng phát triển tiếp? | Hoàn thiện rehearsal, cấu hình OAuth Client thật để thử Google, nghiệm thu tích hợp thực tế, kiểm tra deployment và dữ liệu nâng cấp. Chín task review đã được sửa trong working tree; không hứa sẵn sàng dùng trong bệnh viện. |

## Mở code ở đâu

- [AppointmentService](../src/main/java/com/medclinic/service/AppointmentService.java): `bookAppointment`, check-in và quyền đặt thay.
- [TimeSlotRepository](../src/main/java/com/medclinic/repository/TimeSlotRepository.java): query khóa khung giờ.
- [MedicalRecordService](../src/main/java/com/medclinic/service/MedicalRecordService.java): `startExamination`, `saveMedicalRecordAndPrescription`, khóa thuốc/transaction.
- [InvoiceService](../src/main/java/com/medclinic/service/InvoiceService.java): tạo invoice và thu tiền mặt.
- [SecurityConfig](../src/main/java/com/medclinic/config/SecurityConfig.java), [AuthController](../src/main/java/com/medclinic/controller/AuthController.java): route, session và OTP.
- [PdfExportService](../src/main/java/com/medclinic/service/PdfExportService.java): quyền tải và PDF.
- [BankTransferPaymentService](../src/main/java/com/medclinic/service/BankTransferPaymentService.java): QR/callback/idempotency, thứ tự khóa Invoice → Payment và test concurrent trong báo cáo nghiệm thu.
- [Thư mục event](../src/main/java/com/medclinic/event): cập nhật queue/email sau commit.
- [Cấu hình profile](../src/main/resources/application.yml), [CI](../.github/workflows/ci.yml), [README](../README.md): tái lập môi trường và kiểm thử.

## Khi có sự cố hoặc chỉ được 7 phút

| Tình huống | Xử lý |
| --- | --- |
| Không còn giờ hôm nay | Chọn ngày mai ở dev/demo, nói rõ xử lý sớm; không bật tắt rule production trên sân khấu. |
| Login doctor không thấy ca | Kiểm tra bác sĩ đã chọn là Hùng, đúng ngày và đã check-in; không sửa ID/quyền trực tiếp để lách. |
| QR camera lỗi/quyền thiết bị | Nhập booking code. Camera là đường nhập khác, không cần để hoàn thành ca. |
| WebSocket không kết nối | Refresh queue để tiếp tục, nói đây là tải lại; trình bày code event/test cho realtime, không nhận refresh là push. |
| Mail/QR ngân hàng chưa cấu hình | Giữ demo chính ở tài khoản seed và tiền mặt. Giải thích phần tích hợp bằng code và giới hạn thực tế. |
| H2 bị restart giữa buổi | Dữ liệu đã mất, nói rõ đặc tính dev. Dùng ca hoàn chỉnh/screenshot/PDF đã chuẩn bị trước, ghi rõ là minh họa đã lưu; không nói vẫn đang xem dữ liệu live. |
| Chỉ có 7 phút | 0:00–0:45 bài toán; 0:45–1:30 kiến trúc; 1:30–4:30 demo ca chính; 4:30–5:30 transaction/phân quyền; 5:30–6:15 test/AI; 6:15–7:00 giới hạn. Bỏ chỉnh danh mục, email và demo ngân hàng. |

Trước khi bảo vệ, luyện một lần có bấm giờ, ghi lại booking code để tìm nhanh, và tự giải thích service mà không nhìn lời thoại. [Review hiện tại](PROJECT_REVIEW_2026-10-10.md) là tài liệu phản biện; [báo cáo cleanup](REPO_CLEANUP_2026-10-10.md) giải thích repo đã được lọc như thế nào.
