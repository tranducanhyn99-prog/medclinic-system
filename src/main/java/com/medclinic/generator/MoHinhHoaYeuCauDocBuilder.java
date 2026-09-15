package com.medclinic.generator;

import static com.medclinic.generator.DocxModelHelper.*;

public class MoHinhHoaYeuCauDocBuilder {

    public static String buildDocumentXml() {
        StringBuilder sb = new StringBuilder();

        // 1. TRANG BÌA
        sb.append(buildCoverPage("BÁO CÁO MÔ HÌNH HÓA YÊU CẦU", "HỆ THỐNG QUẢN LÝ VÀ ĐẶT LỊCH KHÁM PHÒNG KHÁM TƯ NHÂN (MEDCLINIC)", "MÔ HÌNH HÓA USE CASE VÀ BIỂU ĐỒ TUẦN TỰ BCE"));

        // 2. NỘI DUNG CHÍNH
        sb.append(p("MÔ HÌNH HÓA YÊU CẦU HỆ THỐNG", "Heading1", true, 16, "left"));

        // 1. Nhận diện tác nhân và chức năng
        sb.append(p("1. Nhận diện tác nhân và phân rã chức năng", "Heading2", true, 14, "left"));
        sb.append(p("Hệ thống MedClinic bao gồm 5 tác nhân chính (Actors) tương tác trực tiếp với các phân hệ của phần mềm:", null, false, 13, "both"));

        sb.append(p("Bảng 1-1. Bảng ma trận liên kết giữa Tác nhân và các Use Case chức năng", null, true, 11, "center"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Tác nhân (Actor)", "Mô tả vai trò", "Số lượng Use Case", "Các Use Case liên kết"));
        sb.append(tableRow(false, "1", "Khách (Guest)", "Người dùng vãng lai chưa đăng nhập", "5 UC", "UC_001, UC_002, UC_004, UC_006, UC_007"));
        sb.append(tableRow(false, "2", "Bệnh nhân (Patient)", "Người bệnh đã có tài khoản và đặt lịch khám", "9 UC", "UC_002, UC_003, UC_005, UC_008, UC_009, UC_010, UC_017, UC_020, UC_021"));
        sb.append(tableRow(false, "3", "Bác sĩ (Doctor)", "Bác sĩ chuyên khoa phụ trách khám và kê đơn", "7 UC", "UC_002, UC_003, UC_005, UC_014, UC_015, UC_016, UC_017"));
        sb.append(tableRow(false, "4", "Lễ tân (Staff)", "Nhân viên tiếp đón tại quầy, cấp STT và thu ngân", "8 UC", "UC_002, UC_003, UC_005, UC_011, UC_012, UC_013, UC_019, UC_020"));
        sb.append(tableRow(false, "5", "Quản trị viên (Admin)", "Quản lý toàn quyền hệ thống, nhân sự, danh mục", "8 UC", "UC_002, UC_003, UC_005, UC_020, UC_022, UC_023, UC_024, UC_025, UC_026"));
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        // Danh mục chức năng từng tác nhân
        sb.append(p("1.1. Danh mục chức năng phía Khách (Guest)", "Heading3", true, 13, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Chức năng", "Mô tả"));
        sb.append(tableRow(false, "1", "Đăng ký tài khoản", "Đăng ký tạo tài khoản bệnh nhân mới với đầy đủ thông tin cá nhân"));
        sb.append(tableRow(false, "2", "Đăng nhập hệ thống", "Đăng nhập vào hệ thống theo vai trò người dùng"));
        sb.append(tableRow(false, "3", "Khôi phục mật khẩu", "Yêu cầu khôi phục mật khẩu thông qua địa chỉ email đã đăng ký"));
        sb.append(tableRow(false, "4", "Tra cứu chuyên khoa", "Xem danh sách các chuyên khoa y tế và thông tin dịch vụ phòng khám"));
        sb.append(tableRow(false, "5", "Tìm kiếm & Lọc bác sĩ", "Tìm kiếm bác sĩ theo chuyên khoa, kinh nghiệm, học hàm và giá khám"));
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        sb.append(p("1.2. Danh mục chức năng phía Bệnh nhân (Patient)", "Heading3", true, 13, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Chức năng", "Mô tả"));
        sb.append(tableRow(false, "1", "Quản lý thông tin cá nhân", "Xem và chỉnh sửa họ tên, số điện thoại, ngày sinh, địa chỉ cư trú"));
        sb.append(tableRow(false, "2", "Đổi mật khẩu", "Thay đổi mật khẩu tài khoản người dùng"));
        sb.append(tableRow(false, "3", "Xem khung giờ trống", "Xem các slot khám khả dụng của bác sĩ theo ngày (tải động qua AJAX)"));
        sb.append(tableRow(false, "4", "Đặt lịch khám theo khung giờ", "Chọn khung giờ, nhập lý do/triệu chứng và xác nhận đặt lịch khám"));
        sb.append(tableRow(false, "5", "Xem mã QR check-in", "Xem chi tiết lịch hẹn kèm mã QR Base64 và mã đặt lịch duy nhất"));
        sb.append(tableRow(false, "6", "Quản lý lịch hẹn & Hủy lịch", "Xem danh sách lịch hẹn (sắp tới, đã khám, đã hủy), hủy lịch trước giờ khám"));
        sb.append(tableRow(false, "7", "Xem lịch sử bệnh án", "Tra cứu hồ sơ bệnh án, chẩn đoán của bác sĩ và ngày hẹn tái khám"));
        sb.append(tableRow(false, "8", "Xem & Tải Đơn thuốc PDF", "Xem toa thuốc điện tử và tải file PDF đơn thuốc chuẩn y tế"));
        sb.append(tableRow(false, "9", "Đánh giá & Phản hồi bác sĩ", "Chấm điểm số sao (1-5 sao) và nhận xét dịch vụ sau khi hoàn tất ca khám"));
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        sb.append(p("1.3. Danh mục chức năng phía Bác sĩ (Doctor)", "Heading3", true, 13, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Chức năng", "Mô tả"));
        sb.append(tableRow(false, "1", "Xem lịch trực cá nhân", "Xem lịch phân ca làm việc (ca sáng / ca chiều) trong 14 ngày"));
        sb.append(tableRow(false, "2", "Xem hàng chờ khám bệnh", "Xem danh sách bệnh nhân đã check-in trong ngày theo thứ tự số STT"));
        sb.append(tableRow(false, "3", "Tiếp nhận & Bắt đầu khám", "Gọi bệnh nhân vào phòng khám, chuyển trạng thái ca khám sang IN_PROGRESS"));
        sb.append(tableRow(false, "4", "Tra cứu tiền sử bệnh án", "Xem hồ sơ bệnh án của các lần khám trước của bệnh nhân"));
        sb.append(tableRow(false, "5", "Ghi chẩn đoán bệnh án", "Nhập triệu chứng lâm sàng quan sát được, chẩn đoán, dặn dò, hẹn ngày tái khám"));
        sb.append(tableRow(false, "6", "Kê đơn thuốc điện tử", "Chọn thuốc từ kho dược, nhập số lượng, liều dùng, cách dùng (tự trừ tồn kho)"));
        sb.append(tableRow(false, "7", "Hoàn tất khám & Xuất đơn PDF", "Hoàn thành ca khám, hệ thống sinh hóa đơn tự động và cho phép in đơn thuốc PDF"));
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        sb.append(p("1.4. Danh mục chức năng phía Lễ tân / Tiếp nhận (Staff)", "Heading3", true, 13, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Chức năng", "Mô tả"));
        sb.append(tableRow(false, "1", "Tra cứu tiếp nhận", "Tìm kiếm lịch hẹn bệnh nhân bằng mã đặt lịch, số điện thoại hoặc họ tên"));
        sb.append(tableRow(false, "2", "Quét QR Check-in & Cấp STT", "Quét mã QR hoặc bấm check-in, tự động cấp số thứ tự khám STT-XX trong ngày"));
        sb.append(tableRow(false, "3", "Tiếp nhận bệnh nhân vãng lai", "Đăng ký hồ sơ và đặt lịch trực tiếp tại quầy cho bệnh nhân chưa đặt trước"));
        sb.append(tableRow(false, "4", "Quản lý hóa đơn viện phí", "Xem danh sách hóa đơn viện phí chờ thanh toán của các ca khám hoàn tất"));
        sb.append(tableRow(false, "5", "Thu viện phí & Xác nhận", "Thu tiền mặt tại quầy hoặc xác nhận thanh toán chuyển khoản / VNPay"));
        sb.append(tableRow(false, "6", "Xuất Hóa đơn viện phí PDF", "Xuất và in phiếu thanh toán hóa đơn viện phí định dạng PDF"));
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        sb.append(p("1.5. Danh mục chức năng phía Quản trị viên (Admin)", "Heading3", true, 13, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Chức năng", "Mô tả"));
        sb.append(tableRow(false, "1", "Dashboard KPI tổng quan", "Theo dõi số lượng bệnh nhân, bác sĩ, ca khám hôm nay và doanh thu thực thu"));
        sb.append(tableRow(false, "2", "Biểu đồ thống kê doanh thu", "Xem biểu đồ đường/cột thống kê doanh thu và tỷ lệ ca khám theo chuyên khoa"));
        sb.append(tableRow(false, "3", "Quản lý Chuyên khoa", "Xem, thêm mới, cập nhật mô tả, icon và bật/tắt hoạt động chuyên khoa"));
        sb.append(tableRow(false, "4", "Quản lý Hồ sơ Bác sĩ", "Thêm, cập nhật bác sĩ, phân chuyên khoa, học hàm, kinh nghiệm, giá khám"));
        sb.append(tableRow(false, "5", "Quản lý Kho thuốc & Dược phẩm", "Thêm thuốc, cập nhật giá bán, số lượng tồn kho, hướng dẫn sử dụng"));
        sb.append(tableRow(false, "6", "Quản lý Tài khoản người dùng", "Xem danh sách tài khoản, tìm kiếm, kích hoạt hoặc khóa tài khoản vi phạm"));
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));
        sb.append(pageBreak());

        // 2. Mô hình Sơ đồ Use Case
        sb.append(p("2. Mô hình Sơ đồ Use Case tổng quan", "Heading2", true, 14, "left"));
        sb.append(p("Sơ đồ Use Case của MedClinic được thiết kế và mô hình hóa trong Enterprise Architect (EA 15) với 5 Tác nhân và 26 Use Case được tổ chức trong package 'Use Case Model'. Các liên kết giữa Actor và Use Case được kết nối bằng quan hệ Association hai chiều, cùng các quan hệ phụ thuộc nội bộ:", null, false, 13, "both"));

        sb.append(drawingImage("rIdImg1", 101, "diagram_usecase_overview.png", 5700000, 3630000, "Hình 2.1. Sơ đồ Use Case tổng quan hệ thống MedClinic (5 Tác nhân & 26 Use Case)"));
        sb.append(p("", null, false, 12, "left"));

        sb.append(p("• Quan hệ <<include>> (Bắt buộc):", null, true, 12, "left"));
        sb.append(p("- 'Đặt lịch khám theo khung giờ' <<include>> 'Xem khung giờ trống' (Bệnh nhân phải tải slot trước khi chọn).\n" +
                   "- 'Bác sĩ kê đơn thuốc' <<include>> 'Tra cứu kho thuốc' (Phải chọn thuốc từ danh mục tồn kho).\n" +
                   "- 'Hoàn tất khám bệnh' <<include>> 'Tự động sinh Hóa đơn viện phí' (Hệ thống tự động tính tiền và lập hóa đơn UNPAID).", null, false, 12, "both"));
        sb.append(p("• Quan hệ <<extend>> (Mở rộng tùy chọn):", null, true, 12, "left"));
        sb.append(p("- 'Đánh giá bác sĩ' <<extend>> 'Xem chi tiết lịch hẹn đã hoàn tất'.\n" +
                   "- 'Xuất đơn thuốc PDF' <<extend>> 'Xem bệnh án'.\n" +
                   "- 'Thanh toán VNPay' <<extend>> 'Thu viện phí tại quầy'.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        // 3. Mô hình Sơ đồ Tuần tự theo BCE
        sb.append(p("3. Mô hình Sơ đồ Tuần tự (Sequence Diagrams) phân tích theo BCE", "Heading2", true, 14, "left"));
        sb.append(p("Mô hình BCE (Boundary - Control - Entity) phân tách rõ ràng trách nhiệm giữa tầng Giao diện (Boundary: Form/Web View), tầng Điều khiển nghiệp vụ (Control: Controller/Service) và tầng Thực thể dữ liệu (Entity: Repository/Database Model). Dưới đây là 6 kịch bản tuần tự cốt lõi:", null, false, 13, "both"));

        // 3.1 Sign up
        sb.append(p("3.1. Sơ đồ Tuần tự: Đăng ký tài khoản (Sign up)", "Heading3", true, 13, "left"));
        sb.append(p("• Actor: Khách (Guest)\n" +
                   "• Boundary: Form Đăng ký (/register)\n" +
                   "• Control: AuthController, UserServiceImpl, BCryptPasswordEncoder\n" +
                   "• Entity: UserRepository, RoleRepository -> CSDL users, roles\n\n" +
                   "Các bước thực thi tuần tự:\n" +
                   "1. Khách điền thông tin (họ tên, username, email, mật khẩu) trên Boundary Form\n" +
                   "2. Boundary gửi HTTP POST /register tới AuthController\n" +
                   "3. AuthController gọi UserServiceImpl.registerPatient(dto)\n" +
                   "4. UserServiceImpl gọi UserRepository.existsByUsername() và existsByEmail() để kiểm tra trùng lặp\n" +
                   "5. UserServiceImpl gọi BCryptPasswordEncoder.encode() để mã hóa mật khẩu\n" +
                   "6. UserServiceImpl lấy Role 'ROLE_PATIENT' từ RoleRepository và gán cho User mới\n" +
                   "7. UserServiceImpl gọi UserRepository.save(newUser)\n" +
                   "8. AuthController trả về thông báo đăng ký thành công và chuyển hướng đến trang /login.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        // 3.2 Sign in
        sb.append(p("3.2. Sơ đồ Tuần tự: Đăng nhập hệ thống (Sign in)", "Heading3", true, 13, "left"));
        sb.append(p("• Actor: Người dùng (Patient, Doctor, Staff, Admin)\n" +
                   "• Boundary: Form Đăng nhập (/login)\n" +
                   "• Control: SecurityFilterChain, DaoAuthenticationProvider, CustomUserDetailsService\n" +
                   "• Entity: UserRepository -> CSDL users\n\n" +
                   "Các bước thực thi tuần tự:\n" +
                   "1. Người dùng nhập username và password tại Boundary Form\n" +
                   "2. Gửi POST /login tới SecurityFilterChain\n" +
                   "3. DaoAuthenticationProvider kích hoạt CustomUserDetailsService.loadUserByUsername()\n" +
                   "4. CustomUserDetailsService truy vấn UserRepository tìm User theo username/email\n" +
                   "5. DaoAuthenticationProvider so sánh mật khẩu nhập vào với mật khẩu băm BCrypt trong CSDL\n" +
                   "6. Nếu khớp, sinh Authentication Token và lưu vào SecurityContext\n" +
                   "7. CustomAuthenticationSuccessHandler kiểm tra Role và chuyển hướng: ROLE_PATIENT -> /patient/appointments, ROLE_DOCTOR -> /doctor/desk, ROLE_STAFF -> /staff/reception, ROLE_ADMIN -> /admin/dashboard.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        // 3.3 Booking
        sb.append(p("3.3. Sơ đồ Tuần tự: Đặt lịch khám & Khóa đồng thời Optimistic Locking", "Heading3", true, 13, "left"));
        sb.append(p("• Actor: Bệnh nhân (Patient)\n" +
                   "• Boundary: Giao diện Đặt khám (/patient/book-appointment)\n" +
                   "• Control: PatientAppointmentController, AppointmentServiceImpl, EmailService\n" +
                   "• Entity: TimeSlotRepository (@Version), AppointmentRepository -> CSDL time_slots, appointments\n\n" +
                   "Các bước thực thi tuần tự:\n" +
                   "1. Bệnh nhân chọn Bác sĩ, Ngày khám -> AJAX gọi lấy danh sách TimeSlot AVAILABLE\n" +
                   "2. Bệnh nhân bấm chọn Slot và gửi form Đặt lịch\n" +
                   "3. AppointmentServiceImpl mở transaction (@Transactional):\n" +
                   "   a. Đọc bản ghi TimeSlot với version hiện tại (VD: version = 0)\n" +
                   "   b. Kiểm tra slot.getStatus() == AVAILABLE\n" +
                   "   c. Đổi slot.setStatus(BOOKED) -> Khi Flush CSDL kiểm tra WHERE id=? AND version=0\n" +
                   "   d. Tự động tăng version lên 1\n" +
                   "   e. Tạo Appointment mới, sinh mã đặt lịch MED-YYMMDD-XXXX, tạo mã QR Base64\n" +
                   "   f. AppointmentRepository.save(appointment)\n" +
                   "4. Transaction Commit thành công -> Gọi EmailService.sendBookingConfirmationAsync() gửi email ngầm\n" +
                   "5. Trả về trang chi tiết lịch hẹn kèm mã QR\n" +
                   "Trường hợp xung đột: Nếu 2 bệnh nhân bấm đặt cùng 1 slot tại cùng thời điểm, người thứ hai commit sẽ thấy version đã thay đổi -> JPA ném OptimisticLockingFailureException -> Controller bắt lỗi và thông báo người dùng chọn slot khác.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        // 3.4 Check-in
        sb.append(p("3.4. Sơ đồ Tuần tự: Quét QR Check-in & Cấp STT tiếp nhận", "Heading3", true, 13, "left"));
        sb.append(p("• Actor: Lễ tân (Staff)\n" +
                   "• Boundary: Quầy tiếp nhận (/staff/reception)\n" +
                   "• Control: StaffReceptionController, AppointmentServiceImpl\n" +
                   "• Entity: AppointmentRepository -> CSDL appointments\n\n" +
                   "Các bước thực thi tuần tự:\n" +
                   "1. Lễ tân quét mã QR hoặc nhập mã MED-xxx tại Boundary\n" +
                   "2. Gửi yêu cầu POST /staff/reception/check-in\n" +
                   "3. StaffReceptionController gọi AppointmentServiceImpl.checkInAppointment(bookingCode)\n" +
                   "4. Service tính toán số thứ tự trong ngày: countByDoctorAndDate() + 1\n" +
                   "5. Gán queueNumber = 'STT-' + formattedNumber (VD: STT-05)\n" +
                   "6. Cập nhật status = CHECKED_IN\n" +
                   "7. AppointmentRepository.save(appointment)\n" +
                   "8. Bệnh nhân lập tức xuất hiện trên Bàn khám bác sĩ theo thời gian thực.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        // 3.5 Examine
        sb.append(p("3.5. Sơ đồ Tuần tự: Bàn khám bệnh, Kê đơn thuốc & Trừ tồn kho dược", "Heading3", true, 13, "left"));
        sb.append(p("• Actor: Bác sĩ (Doctor)\n" +
                   "• Boundary: Bàn khám (/doctor/examine/{id})\n" +
                   "• Control: DoctorDeskController, MedicalRecordServiceImpl, InvoiceServiceImpl\n" +
                   "• Entity: MedicalRecordRepository, MedicineRepository, PrescriptionRepository, InvoiceRepository\n\n" +
                   "Các bước thực thi tuần tự:\n" +
                   "1. Bác sĩ bấm Khám bệnh -> Ca khám đổi status = IN_PROGRESS\n" +
                   "2. Bác sĩ nhập triệu chứng, chẩn đoán, dặn dò và chọn thuốc kê đơn\n" +
                   "3. Bác sĩ bấm Hoàn tất ca khám\n" +
                   "4. MedicalRecordServiceImpl mở transaction:\n" +
                   "   a. Lưu bản ghi MedicalRecord liên kết với Appointment\n" +
                   "   b. Tạo Prescription và danh sách PrescriptionItem\n" +
                   "   c. Với mỗi thuốc, gọi MedicineRepository.decreaseStock(medicineId, qty)\n" +
                   "   d. Đổi appointment.status = COMPLETED\n" +
                   "5. Kích hoạt InvoiceServiceImpl.generateInvoice(appointmentId):\n" +
                   "   a. Tính tổng tiền: consultationFee + tổng tiền các dòng thuốc\n" +
                   "   b. Sinh mã INV-YYMMDD-XXXX và lưu Invoice với status = UNPAID\n" +
                   "6. Redirect sang trang xem bệnh án kèm nút In Đơn Thuốc PDF.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        // 3.6 Billing
        sb.append(p("3.6. Sơ đồ Tuần tự: Thu viện phí & Xuất hóa đơn tài chính PDF", "Heading3", true, 13, "left"));
        sb.append(p("• Actor: Lễ tân (Staff)\n" +
                   "• Boundary: Quầy thu ngân (/staff/billing)\n" +
                   "• Control: StaffReceptionController, InvoiceServiceImpl, PdfExportService\n" +
                   "• Entity: InvoiceRepository -> CSDL invoices\n\n" +
                   "Các bước thực thi tuần tự:\n" +
                   "1. Lễ tân tra cứu hóa đơn UNPAID của bệnh nhân\n" +
                   "2. Chọn phương thức thanh toán (CASH hoặc VNPAY) và bấm Xác nhận thanh toán\n" +
                   "3. InvoiceServiceImpl cập nhật invoice.status = PAID và lưu thời điểm paidAt\n" +
                   "4. Lễ tân bấm In Hóa Đơn PDF\n" +
                   "5. PdfExportService sử dụng thư viện OpenPDF biên dịch toàn bộ bảng kê chi phí thành file PDF chuẩn y tế\n" +
                   "6. Trình duyệt xuất luồng byte PDF và mở hộp thoại in phiếu thu viện phí.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        return wrapDocument(sb.toString());
    }
}
