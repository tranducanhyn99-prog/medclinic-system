package com.medclinic.generator;

import static com.medclinic.generator.DocxModelHelper.*;

public class XacDinhYeuCauDocBuilder {

    public static String buildDocumentXml() {
        StringBuilder sb = new StringBuilder();

        // 1. TRANG BÌA
        sb.append(buildCoverPage("BÁO CÁO XÁC ĐỊNH YÊU CẦU", "HỆ THỐNG QUẢN LÝ VÀ ĐẶT LỊCH KHÁM PHÒNG KHÁM TƯ NHÂN (MEDCLINIC)", "YÊU CẦU CHỨC NĂNG VÀ PHI CHỨC NĂNG"));

        // 2. NỘI DUNG CHÍNH
        sb.append(p("XÁC ĐỊNH YÊU CẦU HỆ THỐNG", "Heading1", true, 16, "left"));

        // 1. Bản thuyết minh đề tài
        sb.append(p("1. Bản thuyết minh đề tài", "Heading2", true, 14, "left"));
        sb.append(p("1.1. Mục tiêu và phạm vi", "Heading3", true, 13, "left"));
        sb.append(p("Hệ thống MedClinic là giải pháp chuyển đổi số toàn diện cho phòng khám tư nhân, hỗ trợ tối ưu hóa quy trình từ khâu tiếp đón, đặt lịch, khám bệnh, kê đơn thuốc cho đến thu viện phí và báo cáo thống kê tài chính. Phần mềm vận hành trên nền tảng Web Responsive hiện đại, đảm bảo tính toàn vẹn dữ liệu và độ an toàn bảo mật cao.", null, false, 13, "both"));

        sb.append(p("1.2. Quy trình nghiệp vụ cốt lõi", "Heading3", true, 13, "left"));
        sb.append(p("• Quy trình Đặt lịch khám trực tuyến:", null, true, 12, "left"));
        sb.append(p("Bệnh nhân truy cập website, tra cứu bác sĩ/chuyên khoa, lựa chọn ngày khám và khung giờ trống 30 phút (TimeSlot). Hệ thống kiểm tra tính khả dụng bằng cơ chế Khóa lạc quan (Optimistic Locking), lưu lịch hẹn ở trạng thái CONFIRMED, sinh mã đặt hẹn duy nhất (MED-YYMMDD-XXXX), tạo mã QR check-in Base64 và kích hoạt dịch vụ gửi email xác nhận tự động chạy ngầm (@Async).", null, false, 12, "both"));

        sb.append(p("• Quy trình Tiếp nhận tại quầy & Check-in QR:", null, true, 12, "left"));
        sb.append(p("Khi người bệnh đến phòng khám, nhân viên lễ tân sử dụng máy quét mã QR hoặc tra cứu theo số điện thoại/họ tên. Khi bấm xác nhận Check-in, hệ thống chuyển trạng thái lịch sang CHECKED_IN, tự động cấp số thứ tự khám trong ngày (STT-XX) và đẩy bệnh nhân vào hàng chờ theo thời gian thực tại bàn khám của bác sĩ tương ứng.", null, false, 12, "both"));

        sb.append(p("• Quy trình Tiếp nhận bệnh nhân vãng lai (Walk-in):", null, true, 12, "left"));
        sb.append(p("Đối với người bệnh đến khám trực tiếp không đặt trước, nhân viên lễ tân nhập nhanh số điện thoại để kiểm tra hồ sơ cũ hoặc tạo hồ sơ mới, chọn bác sĩ và ca trực khả dụng, hệ thống lập tức cấp số thứ tự và phân bổ vào hàng chờ khám.", null, false, 12, "both"));

        sb.append(p("• Quy trình Bàn khám & Kê đơn thuốc tự trừ kho:", null, true, 12, "left"));
        sb.append(p("Bác sĩ theo dõi danh sách bệnh nhân đã check-in, gọi bệnh nhân vào khám, chuyển ca khám sang IN_PROGRESS. Bác sĩ tra cứu tiền sử bệnh lý, nhập triệu chứng lâm sàng, kết luận chẩn đoán y khoa, lời dặn và ngày tái khám. Khi kê đơn, bác sĩ chọn thuốc từ danh mục kho dược, hệ thống tự động kiểm tra số lượng tồn và trừ kho tức thời khi hoàn tất ca khám, đồng thời tự động kích hoạt sinh hóa đơn viện phí UNPAID.", null, false, 12, "both"));

        sb.append(p("• Quy trình Thu viện phí & Xuất hóa đơn tài chính:", null, true, 12, "left"));
        sb.append(p("Lễ tân tại quầy thu ngân mở danh sách hóa đơn chờ thanh toán, xác nhận phương thức thu tiền (tiền mặt CASH hoặc quét mã VNPay), chuyển hóa đơn sang PAID, ghi nhận thời điểm thanh toán và xuất phiếu hóa đơn/đơn thuốc định dạng PDF chuẩn y tế qua thư viện OpenPDF.", null, false, 12, "both"));

        // 2. Danh sách yêu cầu chức năng
        sb.append(p("2. Danh sách yêu cầu chức năng hệ thống", "Heading2", true, 14, "left"));
        sb.append(p("Bảng 2-1. Phân quyền và danh mục yêu cầu chức năng chi tiết theo vai trò", null, true, 11, "center"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Bộ phận / Vai trò", "Chức năng", "Loại công việc", "Mô tả chi tiết", "Ghi chú"));

        // Khách
        sb.append(tableRow(false, "1", "Khách (Guest)", "Đăng ký tài khoản", "Lưu trữ", "Đăng ký tài khoản bệnh nhân mới với họ tên, email, SĐT, ngày sinh, mật khẩu", "Mã hóa BCrypt"));
        sb.append(tableRow(false, "2", "Khách (Guest)", "Đăng nhập hệ thống", "Tra cứu", "Xác thực tài khoản qua Spring Security 6, chuyển hướng đúng vai trò", "Hỗ trợ Demo Nhanh"));
        sb.append(tableRow(false, "3", "Khách (Guest)", "Khôi phục mật khẩu", "Xử lý", "Gửi liên kết tạo mật khẩu mới an toàn qua email đăng ký", "Token hết hạn 24h"));
        sb.append(tableRow(false, "4", "Khách (Guest)", "Tra cứu chuyên khoa", "Tra cứu", "Xem thông tin các chuyên khoa, dịch vụ và bác sĩ phụ trách", "Công khai"));
        sb.append(tableRow(false, "5", "Khách (Guest)", "Tìm kiếm & Lọc bác sĩ", "Tra cứu", "Tìm bác sĩ theo tên, chuyên khoa, học hàm, kinh nghiệm và giá khám", "Lọc đa tiêu chí"));

        // Bệnh nhân
        sb.append(tableRow(false, "6", "Bệnh nhân (Patient)", "Quản lý thông tin cá nhân", "Lưu trữ", "Xem và cập nhật họ tên, SĐT, ngày sinh, giới tính, địa chỉ", "Trang cá nhân"));
        sb.append(tableRow(false, "7", "Bệnh nhân (Patient)", "Đổi mật khẩu", "Lưu trữ", "Thay đổi mật khẩu tài khoản với xác nhận mật khẩu cũ", "Bảo mật tài khoản"));
        sb.append(tableRow(false, "8", "Bệnh nhân (Patient)", "Xem khung giờ trống", "Tra cứu", "Tải động các khung giờ 30 phút khả dụng theo bác sĩ và ngày khám", "AJAX API"));
        sb.append(tableRow(false, "9", "Bệnh nhân (Patient)", "Đặt lịch khám theo khung giờ", "Xử lý", "Chọn slot, nhập lý do khám, hệ thống kiểm soát chống double-booking", "Optimistic Locking"));
        sb.append(tableRow(false, "10", "Bệnh nhân (Patient)", "Xem mã QR check-in", "Kết xuất", "Xem chi tiết lịch hẹn kèm mã QR Base64 để check-in tại quầy", "Hiển thị di động"));
        sb.append(tableRow(false, "11", "Bệnh nhân (Patient)", "Quản lý lịch hẹn & Hủy lịch", "Xử lý", "Xem lịch sử lịch hẹn; cho phép hủy trước giờ khám theo quy định", "Giải phóng slot"));
        sb.append(tableRow(false, "12", "Bệnh nhân (Patient)", "Xem lịch sử bệnh án", "Tra cứu", "Tra cứu các lần khám trước, chẩn đoán của bác sĩ và ngày tái khám", "Hồ sơ y bạ điện tử"));
        sb.append(tableRow(false, "13", "Bệnh nhân (Patient)", "Tải Đơn thuốc PDF", "Kết xuất", "Tải toa thuốc điện tử định dạng PDF chuẩn y tế", "OpenPDF"));
        sb.append(tableRow(false, "14", "Bệnh nhân (Patient)", "Đánh giá & Phản hồi bác sĩ", "Lưu trữ", "Chấm điểm 1-5 sao và để lại nhận xét sau khi ca khám hoàn tất", "Minh bạch dịch vụ"));

        // Bác sĩ
        sb.append(tableRow(false, "15", "Bác sĩ (Doctor)", "Xem lịch trực cá nhân", "Tra cứu", "Theo dõi lịch phân ca (ca sáng / ca chiều) trong 14 ngày tới", "Theo dõi lịch làm"));
        sb.append(tableRow(false, "16", "Bác sĩ (Doctor)", "Xem hàng chờ khám bệnh", "Tra cứu", "Xem danh sách bệnh nhân đã check-in theo số thứ tự STT", "Thời gian thực"));
        sb.append(tableRow(false, "17", "Bác sĩ (Doctor)", "Tiếp nhận & Bắt đầu khám", "Xử lý", "Gọi bệnh nhân vào phòng, chuyển trạng thái sang IN_PROGRESS", "Bàn khám bác sĩ"));
        sb.append(tableRow(false, "18", "Bác sĩ (Doctor)", "Tra cứu tiền sử bệnh án", "Tra cứu", "Xem toàn bộ hồ sơ điều trị và các đơn thuốc cũ của bệnh nhân", "Hỗ trợ chẩn đoán"));
        sb.append(tableRow(false, "19", "Bác sĩ (Doctor)", "Ghi chẩn đoán bệnh án", "Lưu trữ", "Nhập triệu chứng lâm sàng, chẩn đoán bệnh, dặn dò, hẹn tái khám", "Bệnh án điện tử"));
        sb.append(tableRow(false, "20", "Bác sĩ (Doctor)", "Kê đơn thuốc điện tử", "Xử lý", "Chọn thuốc từ kho, nhập số lượng, liều dùng, tự trừ tồn kho", "Liên kết kho dược"));
        sb.append(tableRow(false, "21", "Bác sĩ (Doctor)", "Hoàn tất khám & Xuất PDF", "Kết xuất", "Hoàn thành ca khám, hệ thống tự động sinh hóa đơn và xuất PDF", "Chuyển thu ngân"));

        // Lễ tân
        sb.append(tableRow(false, "22", "Lễ tân (Staff)", "Tra cứu tiếp nhận", "Tra cứu", "Tìm kiếm lịch hẹn theo mã đặt lịch, số điện thoại hoặc họ tên", "Quầy tiếp đón"));
        sb.append(tableRow(false, "23", "Lễ tân (Staff)", "Quét QR Check-in & Cấp STT", "Xử lý", "Quét mã QR bệnh nhân, tự động cấp số thứ tự khám STT-XX", "Đẩy vào hàng chờ"));
        sb.append(tableRow(false, "24", "Lễ tân (Staff)", "Tiếp nhận bệnh nhân vãng lai", "Lưu trữ", "Đăng ký hồ sơ và đặt lịch trực tiếp cho khách chưa đặt trước", "Walk-in Desk"));
        sb.append(tableRow(false, "25", "Lễ tân (Staff)", "Quản lý hóa đơn viện phí", "Tra cứu", "Xem danh sách hóa đơn viện phí chờ thanh toán của các ca khám", "Quầy thanh toán"));
        sb.append(tableRow(false, "26", "Lễ tân (Staff)", "Thu viện phí & Xác nhận", "Xử lý", "Thu tiền mặt tại quầy hoặc xác nhận thanh toán chuyển khoản/VNPay", "Chuyển trạng thái PAID"));
        sb.append(tableRow(false, "27", "Lễ tân (Staff)", "Xuất Hóa đơn viện phí PDF", "Kết xuất", "In phiếu thu và hóa đơn tài chính định dạng PDF chuẩn", "Thanh toán viện phí"));

        // Admin
        sb.append(tableRow(false, "28", "Quản trị viên (Admin)", "Dashboard KPI tổng quan", "Thống kê", "Theo dõi số lượng bệnh nhân, bác sĩ, ca khám và doanh thu thực thu", "Bảng điều khiển"));
        sb.append(tableRow(false, "29", "Quản trị viên (Admin)", "Biểu đồ thống kê doanh thu", "Thống kê", "Xem biểu đồ đường/cột thống kê doanh thu và tỷ lệ theo chuyên khoa", "Chart.js"));
        sb.append(tableRow(false, "30", "Quản trị viên (Admin)", "Quản lý Chuyên khoa", "Lưu trữ", "Thêm mới, cập nhật mô tả, icon và bật/tắt hoạt động chuyên khoa", "Danh mục y tế"));
        sb.append(tableRow(false, "31", "Quản trị viên (Admin)", "Quản lý Bác sĩ & Lịch trực", "Lưu trữ", "Thêm, cập nhật bác sĩ, chuyên khoa, học hàm, sinh lịch làm việc 14 ngày", "Nhân sự y tế"));
        sb.append(tableRow(false, "32", "Quản trị viên (Admin)", "Quản lý Kho thuốc & Dược phẩm", "Lưu trữ", "Thêm thuốc, cập nhật giá bán, số lượng tồn kho, hướng dẫn sử dụng", "Dược phẩm"));
        sb.append(tableRow(false, "33", "Quản trị viên (Admin)", "Quản lý Tài khoản người dùng", "Lưu trữ", "Xem danh sách tài khoản, kích hoạt hoặc khóa tài khoản vi phạm", "Phân quyền RBAC"));

        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        // 3. Danh sách yêu cầu phi chức năng
        sb.append(p("3. Danh sách yêu cầu phi chức năng", "Heading2", true, 14, "left"));
        sb.append(p("Bảng 2-2. Danh mục yêu cầu phi chức năng của hệ thống MedClinic", null, true, 11, "center"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Nội dung", "Tiêu chuẩn", "Mô tả chi tiết", "Ghi chú"));
        sb.append(tableRow(false, "1", "Giao diện Web Responsive", "Tiến hóa", "Giao diện tương thích hoàn hảo trên mọi kích cỡ màn hình (Desktop, Tablet, Smartphone) sử dụng HTML5, CSS3 và Bootstrap 5.3.", "Trải nghiệm đa nền tảng"));
        sb.append(tableRow(false, "2", "An toàn bảo mật thông tin", "An toàn", "Mã hóa mật khẩu bằng thuật toán BCrypt với Salt ngẫu nhiên; phân quyền RBAC chặt chẽ; phòng chống CSRF, XSS và SQL Injection qua Spring Security 6.", "Bảo vệ dữ liệu y tế"));
        sb.append(tableRow(false, "3", "Kiểm soát đồng thời (Concurrency)", "Hiệu quả", "Sử dụng Khóa lạc quan (Optimistic Locking) thông qua trường version trên bảng time_slots kết hợp Unique Constraint ở tầng CSDL để ngăn chặn 100% tình trạng đặt trùng lịch.", "Toàn vẹn đặt khám"));
        sb.append(tableRow(false, "4", "Hiệu năng và tốc độ phản hồi", "Hiệu quả", "Thời gian xử lý giao dịch đặt lịch khám không quá 3 giây; thời gian tải trang và hiển thị kết quả tra cứu không quá 2 giây dưới tải 100 người dùng đồng thời.", "Tối ưu hóa Index"));
        sb.append(tableRow(false, "5", "Xuất chứng từ PDF chuẩn y tế", "Tương thích", "Tích hợp thư viện OpenPDF để kết xuất toa thuốc điện tử và hóa đơn viện phí định dạng PDF khổ giấy A4/A5, hỗ trợ đầy đủ font Unicode tiếng Việt.", "In ấn trực tiếp"));
        sb.append(tableRow(false, "6", "Thông báo tự động đa kênh", "Tiện dụng", "Tự động gửi email xác nhận đặt lịch kèm mã QR và hướng dẫn khám bệnh thông qua Spring Mail (@Async) mà không gây tắc nghẽn luồng xử lý chính.", "Dịch vụ thông báo ngầm"));
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        return wrapDocument(sb.toString());
    }
}
