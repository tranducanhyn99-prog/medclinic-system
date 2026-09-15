package com.medclinic.generator;

import static com.medclinic.generator.DocxModelHelper.*;

public class DacTaUseCaseDocBuilder {

    public static String buildDocumentXml() {
        StringBuilder sb = new StringBuilder();

        // 1. TRANG BÌA
        sb.append(buildCoverPage("BÁO CÁO ĐẶC TẢ USE CASE CHI TIẾT", "HỆ THỐNG QUẢN LÝ VÀ ĐẶT LỊCH KHÁM PHÒNG KHÁM TƯ NHÂN (MEDCLINIC)", "ĐẶC TẢ TOÀN BỘ 26 USE CASE THEO CHUẨN IEEE 830"));

        // 2. NỘI DUNG CHÍNH
        sb.append(p("ĐẶC TẢ USE CASE CHI TIẾT", "Heading1", true, 16, "left"));
        sb.append(p("Tài liệu này đặc tả chi tiết toàn bộ 26 Use Case của hệ thống MedClinic theo quy chuẩn chuẩn hóa công nghệ phần mềm, bao gồm mục tiêu, tác nhân, tiền điều kiện, hậu điều kiện, luồng sự kiện chính, luồng thay thế và các kịch bản ngoại lệ.", null, false, 13, "both"));

        // Bảng tóm tắt 26 Use Case
        sb.append(p("1. Bảng danh mục tổng hợp 26 Use Case", "Heading2", true, 14, "left"));
        sb.append(p("Bảng 1-1. Danh sách 26 Use Case và Tác nhân chính", null, true, 11, "center"));
        sb.append(startTable());
        sb.append(tableRow(true, "Mã UC", "Tên Use Case", "Tác nhân chính", "Phân hệ nghiệp vụ"));
        sb.append(tableRow(false, "UC_001", "Đăng ký tài khoản (Sign up)", "Khách (Guest)", "Xác thực & Tài khoản"));
        sb.append(tableRow(false, "UC_002", "Đăng nhập hệ thống (Sign in)", "Tất cả tác nhân", "Xác thực & Phân quyền"));
        sb.append(tableRow(false, "UC_003", "Đăng xuất khỏi hệ thống (Sign out)", "Tất cả tác nhân", "Xác thực & Phiên làm việc"));
        sb.append(tableRow(false, "UC_004", "Khôi phục mật khẩu (Password Recovery)", "Khách, Bệnh nhân", "Bảo mật tài khoản"));
        sb.append(tableRow(false, "UC_005", "Quản lý thông tin cá nhân & Đổi mật khẩu", "Tất cả người dùng", "Hồ sơ cá nhân"));
        sb.append(tableRow(false, "UC_006", "Tìm kiếm và lọc bác sĩ", "Khách, Bệnh nhân", "Cổng thông tin y tế"));
        sb.append(tableRow(false, "UC_007", "Xem chi tiết bác sĩ", "Khách, Bệnh nhân", "Cổng thông tin y tế"));
        sb.append(tableRow(false, "UC_008", "Đặt lịch khám theo khung giờ", "Bệnh nhân", "Đặt khám trực tuyến"));
        sb.append(tableRow(false, "UC_009", "Quản lý lịch hẹn & Hủy lịch", "Bệnh nhân", "Quản lý lịch hẹn"));
        sb.append(tableRow(false, "UC_010", "Xem hồ sơ bệnh án & Toa thuốc cũ", "Bệnh nhân", "Hồ sơ y bạ điện tử"));
        sb.append(tableRow(false, "UC_011", "Quầy tiếp nhận & Tra cứu lịch hẹn", "Lễ tân (Staff)", "Tiếp đón & Quầy quầy"));
        sb.append(tableRow(false, "UC_012", "Quét QR Check-in & Cấp STT tiếp nhận", "Lễ tân (Staff)", "Tiếp đón & Hàng đợi"));
        sb.append(tableRow(false, "UC_013", "Tiếp nhận bệnh nhân vãng lai", "Lễ tân (Staff)", "Tiếp nhận trực tiếp (Walk-in)"));
        sb.append(tableRow(false, "UC_014", "Bàn khám bác sĩ & Hàng chờ khám", "Bác sĩ (Doctor)", "Bàn khám lâm sàng"));
        sb.append(tableRow(false, "UC_015", "Khám bệnh & Ghi bệnh án điện tử", "Bác sĩ (Doctor)", "Khám chữa bệnh"));
        sb.append(tableRow(false, "UC_016", "Kê đơn thuốc động & Tự động trừ tồn kho", "Bác sĩ (Doctor)", "Kê đơn & Kho dược"));
        sb.append(tableRow(false, "UC_017", "Xuất Đơn thuốc ra file PDF", "Bác sĩ, Bệnh nhân", "Chứng từ điện tử"));
        sb.append(tableRow(false, "UC_018", "Tự động sinh Hóa đơn viện phí", "Hệ thống (System)", "Tài chính & Thu ngân"));
        sb.append(tableRow(false, "UC_019", "Thu viện phí & Xác nhận thanh toán", "Lễ tân (Staff)", "Thu ngân viện phí"));
        sb.append(tableRow(false, "UC_020", "Xuất Hóa đơn viện phí ra file PDF", "Lễ tân, Bệnh nhân", "Chứng từ tài chính"));
        sb.append(tableRow(false, "UC_021", "Đánh giá & Phản hồi bác sĩ", "Bệnh nhân", "Chăm sóc khách hàng"));
        sb.append(tableRow(false, "UC_022", "Quản lý Chuyên khoa", "Quản trị viên (Admin)", "Quản trị danh mục y tế"));
        sb.append(tableRow(false, "UC_023", "Quản lý Bác sĩ & Phân lịch trực", "Quản trị viên (Admin)", "Quản trị nhân sự y tế"));
        sb.append(tableRow(false, "UC_024", "Quản lý Kho dược phẩm & Thuốc", "Quản trị viên (Admin)", "Quản trị dược phẩm"));
        sb.append(tableRow(false, "UC_025", "Quản lý Tài khoản người dùng", "Quản trị viên (Admin)", "Quản trị tài khoản & RBAC"));
        sb.append(tableRow(false, "UC_026", "Thống kê Doanh thu & KPI Phòng khám", "Quản trị viên (Admin)", "Báo cáo & Thống kê KPI"));
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));
        sb.append(pageBreak());

        // 2. Chi tiết 26 Use Case
        sb.append(p("2. Chi tiết các bảng đặc tả Use Case", "Heading2", true, 14, "left"));

        addUseCase(sb, "UC_001", "Đăng ký tài khoản (Sign up)",
                "Đăng ký tạo tài khoản bệnh nhân mới trong hệ thống MedClinic",
                "Khách (Guest)", "Chưa có tài khoản trong hệ thống",
                "- Nếu thành công, tài khoản mới được tạo với quyền ROLE_PATIENT và mã hóa mật khẩu BCrypt\n- Nếu thất bại, hiển thị thông báo lỗi chi tiết",
                "1. Vào hệ thống website\n2. Chọn nút Đăng ký khám\n3. Nhập đầy đủ thông tin: họ tên, username, email, SĐT, ngày sinh, mật khẩu\n4. Bấm Hoàn tất đăng ký\n5. Hệ thống xác thực dữ liệu hợp lệ và lưu vào CSDL\n6. Chuyển về trang Đăng nhập kèm thông báo thành công",
                "4a. Bấm Hủy bỏ\n5a. Hủy thao tác, quay về trang chủ",
                "5b. Tên đăng nhập hoặc email đã tồn tại\n5b1. Thông báo lỗi: Tên đăng nhập hoặc email đã tồn tại\n5c. Mật khẩu xác nhận không khớp\n5c1. Báo lỗi mật khẩu xác nhận");

        addUseCase(sb, "UC_002", "Đăng nhập hệ thống (Sign in)",
                "Đăng nhập vào hệ thống MedClinic để thực hiện các chức năng theo vai trò",
                "Khách (Guest), Bệnh nhân (Patient), Bác sĩ (Doctor), Lễ tân (Staff), Quản trị viên (Admin)",
                "Đã có tài khoản trong hệ thống",
                "- Nếu thành công, chuyển hướng đến trang tương ứng với vai trò (Patient: lịch hẹn, Doctor: bàn khám, Staff: quầy tiếp nhận, Admin: dashboard)\n- Nếu thất bại, hiển thị thông báo lỗi",
                "1. Vào hệ thống website\n2. Chọn nút Đăng nhập trên thanh điều hướng\n3. Nhập tên đăng nhập (hoặc email) và mật khẩu\n4. Tùy chọn tick Ghi nhớ đăng nhập (Remember-me)\n5. Bấm nút Đăng Nhập\n6. Hệ thống xác thực bằng Spring Security 6 và chuyển hướng đúng trang vai trò",
                "3a. Bấm chọn tài khoản Demo Nhanh (Admin, Bác sĩ, Lễ tân, Bệnh nhân)\n3a1. Hệ thống tự động điền username và password",
                "6b. Tên đăng nhập hoặc mật khẩu không chính xác\n6b1. Hiển thị thông báo đăng nhập thất bại\n6c. Tài khoản bị khóa\n6c1. Thông báo tài khoản đã bị khóa bởi quản trị viên");

        addUseCase(sb, "UC_003", "Đăng xuất khỏi hệ thống (Sign out)",
                "Thoát phiên làm việc hiện tại và xóa sạch thông tin xác thực",
                "Bệnh nhân, Bác sĩ, Lễ tân, Quản trị viên", "Đang đăng nhập trong hệ thống",
                "Đăng xuất khỏi hệ thống, hủy session và cookie JSESSIONID, chuyển về trang đăng nhập",
                "1. Chọn biểu tượng tài khoản trên thanh navbar, hiển thị dropdown menu\n2. Chọn nút Đăng xuất\n3. Hệ thống xóa phiên làm việc và cookie JSESSIONID\n4. Chuyển hướng về trang Đăng nhập kèm thông báo đăng xuất thành công",
                "N/A", "N/A");

        addUseCase(sb, "UC_004", "Khôi phục mật khẩu (Password Recovery)",
                "Tạo lại mật khẩu mới khi người dùng quên mật khẩu đăng nhập",
                "Khách, Bệnh nhân", "Đã có tài khoản với email hợp lệ trong hệ thống",
                "- Nếu thành công, mật khẩu mới được cập nhật và mã hóa\n- Nếu thất bại, mật khẩu không thay đổi",
                "1. Tại trang Đăng nhập, chọn Quên mật khẩu?\n2. Nhập địa chỉ email đăng ký\n3. Bấm Gửi yêu cầu\n4. Hệ thống xác thực email và gửi liên kết đặt lại mật khẩu về email người dùng\n5. Người dùng kiểm tra email và nhấp vào liên kết\n6. Nhập mật khẩu mới và xác nhận mật khẩu mới\n7. Bấm Lưu mật khẩu\n8. Thông báo đổi mật khẩu thành công và quay lại trang đăng nhập",
                "3a. Bấm Quay lại đăng nhập\n3a1. Quay lại form login",
                "4b. Email không tồn tại trong hệ thống\n4b1. Thông báo không tìm thấy tài khoản với email này");

        addUseCase(sb, "UC_005", "Quản lý thông tin cá nhân & Đổi mật khẩu",
                "Xem, chỉnh sửa thông tin cá nhân và thay đổi mật khẩu tài khoản",
                "Bệnh nhân, Bác sĩ, Lễ tân, Quản trị viên", "Đã đăng nhập vào hệ thống",
                "Thông tin cá nhân hoặc mật khẩu được cập nhật vào CSDL",
                "1. Bấm vào tên người dùng trên thanh navbar, chọn Hồ sơ cá nhân (/profile)\n2. Hệ thống hiển thị thông tin chi tiết hiện tại\n3. Chỉnh sửa họ tên, số điện thoại, địa chỉ\n4. Bấm Cập nhật thông tin\n5. Hệ thống lưu thay đổi và hiển thị thông báo thành công",
                "3a. Cuộn xuống phần Đổi mật khẩu\n3a1. Nhập mật khẩu cũ, mật khẩu mới, xác nhận mật khẩu mới\n3a2. Bấm Đổi mật khẩu\n3a3. Hệ thống xác thực mật khẩu cũ và cập nhật mật khẩu mới",
                "3a4. Mật khẩu cũ không chính xác\n3a5. Thông báo lỗi mật khẩu cũ không đúng");

        addUseCase(sb, "UC_006", "Tìm kiếm và lọc bác sĩ (Search/Filter doctors)",
                "Tìm kiếm bác sĩ phù hợp theo chuyên khoa, giá khám và từ khóa",
                "Khách (Guest), Bệnh nhân (Patient)", "N/A",
                "Hiển thị danh sách bác sĩ thỏa mãn tiêu chí tìm kiếm",
                "1. Vào trang Bác sĩ (/doctors)\n2. Nhập từ khóa tên bác sĩ hoặc học hàm\n3. Chọn chuyên khoa từ danh sách dropdown\n4. Nhập mức phí khám tối đa mong muốn\n5. Bấm Lọc kết quả\n6. Hệ thống hiển thị danh sách các thẻ bác sĩ phù hợp kèm ảnh, chuyên khoa, kinh nghiệm và giá khám",
                "5a. Bấm nút Đặt lịch trên thẻ bác sĩ\n5a1. Chuyển thẳng đến trang đặt lịch với bác sĩ đó",
                "N/A");

        addUseCase(sb, "UC_007", "Xem chi tiết bác sĩ (View doctor profile)",
                "Xem đầy đủ thông tin chuyên môn, tiểu sử và lịch trực của bác sĩ",
                "Khách, Bệnh nhân", "N/A",
                "Hiển thị trang chi tiết bác sĩ với đầy đủ bằng cấp và lịch khám",
                "1. Từ danh sách bác sĩ, bấm vào tên bác sĩ hoặc nút Xem chi tiết\n2. Hệ thống tải thông tin bác sĩ: học hàm, chuyên khoa, kinh nghiệm, tiểu sử đào tạo và bảng giá\n3. Hiển thị lịch khám dự kiến trong tuần của bác sĩ",
                "N/A", "N/A");

        addUseCase(sb, "UC_008", "Đặt lịch khám theo khung giờ (Online Appointment Booking)",
                "Bệnh nhân chọn bác sĩ, ngày khám và khung giờ 30 phút để đặt lịch hẹn",
                "Bệnh nhân (ROLE_PATIENT)", "Đã đăng nhập tài khoản bệnh nhân",
                "- Nếu thành công, lịch hẹn được tạo với trạng thái CONFIRMED, slot chuyển sang BOOKED (tăng @Version), mã QR được sinh và email xác nhận được gửi ngầm\n- Nếu thất bại (trùng lịch), thông báo chọn slot khác",
                "1. Vào trang Đặt lịch khám (/patient/book-appointment)\n2. Chọn bác sĩ và ngày khám mong muốn\n3. Hệ thống gửi yêu cầu AJAX tải danh sách các khung giờ 30 phút còn khả dụng (status = AVAILABLE)\n4. Bệnh nhân bấm chọn một khung giờ phù hợp\n5. Nhập lý do khám và mô tả triệu chứng\n6. Bấm Xác nhận đặt lịch\n7. Giao dịch bắt đầu: kiểm tra khóa lạc quan Optimistic Locking trên TimeSlot\n8. Hệ thống cập nhật TimeSlot sang BOOKED, tăng version, tạo Appointment với mã booking duy nhất (MED-YYMMDD-XXXX)\n9. Sinh mã QR Base64 và kích hoạt task gửi email xác nhận ngầm (@Async)\n10. Chuyển hướng sang trang Chi tiết lịch hẹn",
                "6a. Bấm Hủy\n6a1. Hủy thao tác đặt lịch",
                "7b. Khung giờ vừa bị người khác đặt trước cùng lúc\n7b1. Hệ thống phát hiện xung đột version hoặc slot khác AVAILABLE\n7b2. Bắt lỗi SlotAlreadyBookedException và hiển thị thông báo: Khung giờ này vừa có người đặt trước, vui lòng chọn khung giờ khác");

        addUseCase(sb, "UC_009", "Quản lý lịch hẹn & Hủy lịch (Manage appointments & Cancel)",
                "Xem danh sách các ca hẹn đã đặt và thực hiện hủy lịch khi có nhu cầu",
                "Bệnh nhân (ROLE_PATIENT)", "Đã đăng nhập vào hệ thống",
                "Lịch hẹn được hủy (status = CANCELLED), TimeSlot được giải phóng về AVAILABLE",
                "1. Vào trang Lịch hẹn của tôi (/patient/appointments)\n2. Hệ thống hiển thị danh sách các lịch hẹn (Sắp tới, Đã khám, Đã hủy)\n3. Bệnh nhân bấm Xem chi tiết để xem mã QR check-in\n4. Với lịch hẹn chưa khám, bấm nút Hủy lịch hẹn\n5. Hộp thoại xác nhận hiện ra, bấm Đồng ý hủy\n6. Hệ thống chuyển trạng thái lịch hẹn sang CANCELLED và trả lại TimeSlot về trạng thái AVAILABLE",
                "5a. Bấm Hủy bỏ trên hộp thoại\n5a1. Giữ nguyên lịch hẹn",
                "N/A");

        addUseCase(sb, "UC_010", "Xem hồ sơ bệnh án & Toa thuốc cũ",
                "Tra cứu tiền sử bệnh lý và các đơn thuốc đã được bác sĩ kê",
                "Bệnh nhân (ROLE_PATIENT)", "Đã đăng nhập vào hệ thống",
                "Hiển thị danh sách hồ sơ bệnh án điện tử của bệnh nhân",
                "1. Vào mục Hồ sơ bệnh án (/patient/medical-records)\n2. Hệ thống hiển thị danh sách các lần khám theo thứ tự thời gian mới nhất\n3. Xem chi tiết chẩn đoán, lời dặn của bác sĩ và ngày tái khám\n4. Bấm nút Tải Đơn Thuốc PDF để lưu hoặc in đơn thuốc",
                "N/A", "N/A");

        addUseCase(sb, "UC_011", "Quầy tiếp nhận & Tra cứu lịch hẹn (Reception Desk)",
                "Lễ tân tra cứu danh sách lịch hẹn trong ngày để tiếp đón bệnh nhân",
                "Lễ tân (ROLE_STAFF)", "Đã đăng nhập với quyền STAFF",
                "Hiển thị thông tin lịch hẹn và trạng thái tiếp nhận của bệnh nhân",
                "1. Lễ tân vào giao diện Quầy tiếp nhận (/staff/reception)\n2. Hệ thống hiển thị danh sách lịch hẹn trong ngày hôm nay\n3. Lễ tân nhập mã đặt lịch (MED-xxx) hoặc số điện thoại của bệnh nhân vào ô tìm kiếm\n4. Bấm Tìm kiếm\n5. Hệ thống hiển thị bản ghi lịch hẹn tương ứng kèm trạng thái",
                "N/A", "3b. Không tìm thấy lịch hẹn\n3b1. Thông báo không tìm thấy thông tin đặt lịch");

        addUseCase(sb, "UC_012", "Quét QR Check-in & Cấp STT tiếp nhận",
                "Xác nhận bệnh nhân đã có mặt tại phòng khám và cấp số thứ tự vào khám",
                "Lễ tân (ROLE_STAFF)", "Đã đăng nhập với quyền STAFF",
                "Lịch hẹn chuyển sang CHECKED_IN, cấp số STT-XX, xuất hiện trên hàng chờ bác sĩ",
                "1. Bệnh nhân xuất trình mã QR check-in trên điện thoại hoặc mã đặt lịch\n2. Lễ tân quét mã QR bằng đầu đọc hoặc nhập mã đặt lịch trên giao diện quầy tiếp nhận\n3. Lễ tân đối chiếu thông tin và bấm nút Check-in (Cấp STT)\n4. Hệ thống đếm số lượng ca khám trong ngày và cấp mã STT tự động (VD: STT-01, STT-02)\n5. Cập nhật trạng thái ca khám sang CHECKED_IN\n6. Bệnh nhân tự động xuất hiện trên màn hình Bàn khám của bác sĩ chuyên khoa phụ trách",
                "N/A", "3b. Ca khám đã được check-in trước đó\n3b1. Thông báo bệnh nhân này đã được cấp STT");

        addUseCase(sb, "UC_013", "Tiếp nhận bệnh nhân vãng lai (Walk-in Registration)",
                "Tạo hồ sơ và đăng ký ca khám trực tiếp tại quầy cho bệnh nhân chưa đặt online",
                "Lễ tân (ROLE_STAFF)", "Đã đăng nhập với quyền STAFF",
                "Tạo tài khoản bệnh nhân (nếu mới), tạo lịch hẹn hôm nay và cấp STT ngay lập tức",
                "1. Lễ tân vào mục Tiếp nhận vãng lai (/staff/walk-in)\n2. Nhập số điện thoại bệnh nhân để tra cứu hồ sơ cũ\n3. Nếu là bệnh nhân mới, nhập họ tên, ngày sinh, giới tính, địa chỉ\n4. Chọn bác sĩ chuyên khoa và ca khám trong ngày\n5. Bấm Đăng ký & Cấp STT\n6. Hệ thống tạo lịch hẹn, cấp số thứ tự tiếp nhận ngay và đưa vào hàng chờ khám",
                "N/A", "N/A");

        addUseCase(sb, "UC_014", "Bàn khám bác sĩ & Hàng chờ khám (Doctor Desk)",
                "Bác sĩ theo dõi hàng đợi bệnh nhân đã check-in và gọi vào phòng khám",
                "Bác sĩ (ROLE_DOCTOR)", "Đã đăng nhập với quyền DOCTOR",
                "Hiển thị danh sách bệnh nhân chờ khám và gọi ca khám",
                "1. Bác sĩ mở trang Bàn khám (/doctor/desk)\n2. Hệ thống hiển thị hàng chờ bệnh nhân hôm nay theo thứ tự STT\n3. Bác sĩ bấm nút Khám Bệnh ở bệnh nhân đầu hàng chờ\n4. Trạng thái ca khám chuyển sang IN_PROGRESS\n5. Hệ thống chuyển hướng sang giao diện phòng khám bệnh án điện tử",
                "N/A", "N/A");

        addUseCase(sb, "UC_015", "Khám bệnh & Ghi bệnh án điện tử",
                "Bác sĩ ghi nhận triệu chứng lâm sàng, chẩn đoán bệnh lý và lời dặn",
                "Bác sĩ (ROLE_DOCTOR)", "Đang mở phiên khám bệnh nhân (IN_PROGRESS)",
                "Hồ sơ bệnh án được lưu trữ và liên kết với ca hẹn",
                "1. Tại giao diện phòng khám, bác sĩ xem thông tin bệnh nhân và tiền sử các lần khám trước\n2. Nhập triệu chứng lâm sàng ghi nhận\n3. Nhập kết luận chẩn đoán y khoa\n4. Nhập lời dặn, chế độ dinh dưỡng, kiêng cử\n5. Tùy chọn chọn ngày hẹn tái khám\n6. Bác sĩ tiến hành kê đơn thuốc (UC_016) hoặc bấm Hoàn tất khám",
                "N/A", "N/A");

        addUseCase(sb, "UC_016", "Kê đơn thuốc động & Tự động trừ tồn kho",
                "Bác sĩ chọn thuốc từ kho dược, nhập liều lượng, cách dùng và hệ thống trừ kho tự động",
                "Bác sĩ (ROLE_DOCTOR)", "Đang ở giao diện khám bệnh",
                "Toa thuốc được lưu, số lượng thuốc trong kho được trừ tương ứng",
                "1. Tại mục Kê đơn thuốc, bác sĩ bấm nút Thêm thuốc\n2. Chọn loại thuốc từ danh mục kho dược (hiển thị rõ đơn vị tính và đơn giá)\n3. Nhập số lượng kê đơn\n4. Nhập liều dùng (VD: 1 viên / lần) và tần suất (VD: 2 lần / ngày sau ăn)\n5. Có thể bấm thêm nhiều loại thuốc vào đơn\n6. Bác sĩ bấm Hoàn tất khám bệnh\n7. Hệ thống kiểm tra tồn kho của từng loại thuốc trong bảng medicines\n8. Trừ số lượng tồn kho tương ứng trong CSDL\n9. Lưu thông tin Prescription và các PrescriptionItem\n10. Cập nhật trạng thái lịch hẹn sang COMPLETED và tự động sinh hóa đơn viện phí",
                "2a. Bấm nút Xóa ở dòng thuốc không muốn kê\n2a1. Dòng thuốc được xóa khỏi đơn",
                "7b. Thuốc trong kho không đủ số lượng kê\n7b1. Hệ thống báo lỗi: Thuốc X không đủ số lượng tồn kho (Còn Y viên), vui lòng giảm số lượng hoặc đổi thuốc khác");

        addUseCase(sb, "UC_017", "Xuất Đơn thuốc ra file PDF",
                "Tạo file PDF đơn thuốc và phiếu khám bệnh chuẩn y tế",
                "Bác sĩ, Bệnh nhân, Lễ tân", "Ca khám đã hoàn tất và có đơn thuốc",
                "Tải về file PDF đơn thuốc chuẩn y tế",
                "1. Bấm nút Tải Đơn Thuốc PDF trên giao diện chi tiết lịch hẹn hoặc bàn khám\n2. Hệ thống sử dụng OpenPDF biên dịch dữ liệu bệnh án, thông tin phòng khám, bác sĩ và bảng chi tiết thuốc\n3. Trả về luồng byte PDF trực tiếp trên trình duyệt hoặc tải về máy",
                "N/A", "N/A");

        addUseCase(sb, "UC_018", "Tự động sinh Hóa đơn viện phí",
                "Hệ thống tự động tính tổng tiền công khám và tiền thuốc khi bác sĩ hoàn tất khám",
                "Hệ thống (System Service)", "Bác sĩ bấm Hoàn tất khám bệnh",
                "Bản ghi Invoice được tạo với mã INV-YYMMDD-XXXX và trạng thái UNPAID",
                "1. Hệ thống nhận sự kiện ca khám chuyển sang COMPLETED\n2. Lấy giá tiền khám tư vấn của bác sĩ (consultationFee)\n3. Tính tổng tiền các loại thuốc trong đơn (medicineFee)\n4. Tính tổng viện phí: totalAmount = consultationFee + medicineFee\n5. Sinh mã hóa đơn duy nhất và lưu vào bảng invoices với trạng thái UNPAID\n6. Hóa đơn xuất hiện trên màn hình Thu viện phí của lễ tân",
                "N/A", "N/A");

        addUseCase(sb, "UC_019", "Thu viện phí & Xác nhận thanh toán",
                "Lễ tân thu tiền mặt hoặc xác nhận thanh toán trực tuyến cho ca khám",
                "Lễ tân (ROLE_STAFF)", "Đã có hóa đơn ở trạng thái UNPAID",
                "Hóa đơn chuyển sang PAID, ghi nhận thời điểm thanh toán",
                "1. Lễ tân vào mục Hóa đơn viện phí (/staff/billing)\n2. Tìm kiếm hóa đơn theo mã lịch hẹn hoặc tên bệnh nhân\n3. Lễ tân chọn phương thức thanh toán: Tiền mặt tại quầy (CASH) hoặc Cổng VNPAY\n4. Thu tiền từ bệnh nhân và bấm Xác nhận thanh toán\n5. Hệ thống cập nhật trạng thái hóa đơn sang PAID, lưu thời điểm paidAt\n6. Hiển thị nút In hóa đơn PDF",
                "3a. Bệnh nhân chọn thanh toán online qua VNPay Sandbox\n3a1. Quét mã QR thanh toán hoặc nhập thông tin thẻ test\n3a2. Cổng thanh toán gửi IPN xác nhận thành công",
                "N/A");

        addUseCase(sb, "UC_020", "Xuất Hóa đơn viện phí ra file PDF",
                "Xuất và in phiếu thanh toán hóa đơn viện phí định dạng PDF",
                "Lễ tân, Bệnh nhân, Quản trị viên", "Hóa đơn đã được tạo",
                "Tải về file PDF hóa đơn tài chính của phòng khám",
                "1. Bấm nút In Hóa Đơn PDF trên giao diện hóa đơn\n2. Hệ thống biên dịch thông tin chi phí khám, chi phí thuốc, tổng tiền, phương thức thanh toán\n3. Trả về file PDF phiếu thu viện phí",
                "N/A", "N/A");

        addUseCase(sb, "UC_021", "Đánh giá & Phản hồi bác sĩ (Review & Rating)",
                "Bệnh nhân chấm sao và nhận xét chất lượng khám sau khi hoàn tất",
                "Bệnh nhân (ROLE_PATIENT)", "Ca khám đã hoàn tất (status = COMPLETED)",
                "Đánh giá được ghi nhận và cập nhật vào điểm trung bình của bác sĩ",
                "1. Bệnh nhân vào trang Chi tiết lịch hẹn đã khám\n2. Bấm nút Đánh giá bác sĩ\n3. Chọn số sao đánh giá (từ 1 đến 5 sao)\n4. Nhập nội dung nhận xét, góp ý về thái độ và chuyên môn của bác sĩ\n5. Bấm Gửi đánh giá\n6. Hệ thống lưu đánh giá và cập nhật hiển thị trên trang thông tin bác sĩ",
                "N/A", "N/A");

        addUseCase(sb, "UC_022", "Quản lý Chuyên khoa (Specialty Management)",
                "Admin thêm, sửa, xem danh mục các chuyên khoa y tế",
                "Quản trị viên (ROLE_ADMIN)", "Đã đăng nhập tài khoản ADMIN",
                "Danh mục chuyên khoa được cập nhật trong CSDL",
                "1. Admin vào trang Quản lý chuyên khoa (/admin/specialties)\n2. Hệ thống hiển thị bảng danh sách các chuyên khoa\n3. Bấm Thêm chuyên khoa mới\n4. Nhập tên chuyên khoa, mô tả chi tiết và icon Bootstrap\n5. Bấm Lưu chuyên khoa\n6. Chuyên khoa mới xuất hiện trên hệ thống",
                "3a. Bấm nút Sửa tại chuyên khoa muốn chỉnh sửa\n3a1. Cập nhật thông tin và bấm Lưu thay đổi\n3b. Bấm Bật/Tắt trạng thái hoạt động",
                "4b. Tên chuyên khoa bị trùng\n4b1. Báo lỗi tên chuyên khoa đã tồn tại");

        addUseCase(sb, "UC_023", "Quản lý Bác sĩ & Phân lịch trực (Doctor Management)",
                "Admin quản lý hồ sơ chuyên môn bác sĩ và thiết lập lịch làm việc",
                "Quản trị viên (ROLE_ADMIN)", "Đã đăng nhập tài khoản ADMIN",
                "Hồ sơ bác sĩ và lịch trực được cập nhật",
                "1. Admin vào mục Quản lý bác sĩ (/admin/doctors)\n2. Xem danh sách các bác sĩ và chuyên khoa trực thuộc\n3. Bấm Thêm bác sĩ mới hoặc chỉnh sửa bác sĩ hiện có\n4. Cập nhật học hàm, kinh nghiệm, chuyên khoa phụ trách, phí khám tư vấn và tiểu sử\n5. Bấm Lưu thông tin bác sĩ\n6. Hệ thống tự động kích hoạt sinh lịch trực ca sáng và ca chiều trong 14 ngày",
                "N/A", "N/A");

        addUseCase(sb, "UC_024", "Quản lý Kho dược phẩm & Thuốc (Medicine Management)",
                "Admin quản lý danh mục thuốc, giá bán và số lượng tồn kho",
                "Quản trị viên (ROLE_ADMIN)", "Đã đăng nhập tài khoản ADMIN",
                "Kho thuốc được cập nhật số lượng và đơn giá",
                "1. Admin vào mục Quản lý thuốc (/admin/medicines)\n2. Xem danh sách thuốc, đơn vị tính, đơn giá bán và số lượng tồn kho\n3. Bấm Thêm thuốc mới\n4. Nhập tên thuốc, đơn vị (viên, vỉ, hộp), đơn giá, hướng dẫn sử dụng và số lượng nhập kho\n5. Bấm Lưu thuốc\n6. Thuốc mới sẵn sàng để bác sĩ kê đơn trong phòng khám",
                "3a. Bấm nút Cập nhật số lượng tồn kho\n3a1. Nhập số lượng nhập thêm và bấm Lưu",
                "N/A");

        addUseCase(sb, "UC_025", "Quản lý Tài khoản người dùng (User Management)",
                "Admin xem danh sách người dùng và khóa/mở khóa tài khoản",
                "Quản trị viên (ROLE_ADMIN)", "Đã đăng nhập tài khoản ADMIN",
                "Trạng thái tài khoản người dùng được cập nhật",
                "1. Admin vào mục Quản lý người dùng (/admin/users)\n2. Hệ thống hiển thị danh sách toàn bộ tài khoản (Patient, Doctor, Staff, Admin)\n3. Tìm kiếm người dùng theo họ tên, email hoặc số điện thoại\n4. Bấm nút Khóa / Mở khóa tài khoản tại người dùng tương ứng\n5. Trạng thái isActive của tài khoản được cập nhật tức thì",
                "N/A", "N/A");

        addUseCase(sb, "UC_026", "Thống kê Doanh thu & KPI Phòng khám (Revenue Statistics)",
                "Admin theo dõi báo cáo doanh thu và phân bổ ca khám thời gian thực",
                "Quản trị viên (ROLE_ADMIN)", "Đã đăng nhập tài khoản ADMIN",
                "Hiển thị các chỉ số KPI và biểu đồ Chart.js trực quan",
                "1. Admin vào Dashboard quản trị (/admin/dashboard)\n2. Hệ thống tải dữ liệu thống kê tổng hợp: tổng bệnh nhân, số ca khám hôm nay, tổng doanh thu thực thu\n3. Hiển thị Biểu đồ tròn: tỷ lệ ca khám phân bổ theo chuyên khoa (Tim mạch, Nhi, Da liễu...)\n4. Hiển thị Biểu đồ cột: tỷ lệ trạng thái lịch hẹn (Hoàn tất, Chờ khám, Đã hủy)\n5. Hiển thị Bảng danh sách các hóa đơn viện phí gần đây nhất",
                "N/A", "N/A");

        return wrapDocument(sb.toString());
    }

    private static void addUseCase(StringBuilder sb, String id, String name, String goal, String actors,
                                   String pre, String post, String mainFlow, String alt, String exc) {
        sb.append(p("Đặc tả " + id + ": " + name, "Heading3", true, 13, "left"));
        sb.append(startTable());
        sb.append(tableRow(false, "Use Case ID", id));
        sb.append(tableRow(false, "Tên chức năng", name));
        sb.append(tableRow(false, "Mục tiêu (Goal)", goal));
        sb.append(tableRow(false, "Tác nhân (Actors)", actors));
        sb.append(tableRow(false, "Điều kiện trước", pre));
        sb.append(tableRow(false, "Điều kiện sau", post));
        sb.append(tableRow(false, "Luồng sự kiện chính", mainFlow));
        sb.append(tableRow(false, "Luồng thay thế", alt));
        sb.append(tableRow(false, "Ngoại lệ (Exceptions)", exc));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));
    }
}
