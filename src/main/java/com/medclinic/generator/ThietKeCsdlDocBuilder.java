package com.medclinic.generator;

import static com.medclinic.generator.DocxModelHelper.*;

public class ThietKeCsdlDocBuilder {

    public static String buildDocumentXml() {
        StringBuilder sb = new StringBuilder();

        // 1. TRANG BÌA
        sb.append(buildCoverPage("BÁO CÁO THIẾT KẾ CƠ SỞ DỮ LIỆU", "HỆ THỐNG QUẢN LÝ VÀ ĐẶT LỊCH KHÁM PHÒNG KHÁM TƯ NHÂN (MEDCLINIC)", "THIẾT KẾ CSDL QUAN HỆ 3NF & TỪ ĐIỂN DỮ LIỆU"));

        // 2. NỘI DUNG CHÍNH
        sb.append(p("CHƯƠNG 4: THIẾT KẾ DỮ LIỆU", "Heading1", true, 16, "left"));

        // 4.1. Lược đồ logic
        sb.append(p("4.1. Lược đồ logic (Logical Schema)", "Heading2", true, 14, "left"));
        sb.append(p("Hệ thống MedClinic được thiết kế theo mô hình cơ sở dữ liệu quan hệ đạt dạng chuẩn 3 (Third Normal Form - 3NF). Bản hiện hành bổ sung counter queue theo ngày, reservation mã hóa đơn và snapshot giá kê đơn để bảo toàn tính nhất quán nghiệp vụ:", null, false, 13, "both"));

        sb.append(p("Danh sách các quan hệ logic giữa các bảng:", null, true, 12, "left"));
        sb.append(p("• roles (1) ──< users (*) : Phân quyền vai trò người dùng\n" +
                   "• users (1) ─── doctor_profiles (1) : Hồ sơ chuyên môn bác sĩ (quan hệ 1 - 1)\n" +
                   "• specialties (1) ──< doctor_profiles (*) : Chuyên khoa của bác sĩ\n" +
                   "• doctor_profiles (1) ──< doctor_schedules (*) : Lịch làm việc/phân ca trực của bác sĩ\n" +
                   "• doctor_schedules (1) ──< time_slots (*) : Các khung giờ khám 30 phút trong ca\n" +
                   "• users (1) ──< appointments (*) : Bệnh nhân đặt lịch khám\n" +
                   "• doctor_profiles (1) ──< appointments (*) : Bác sĩ phụ trách ca khám\n" +
                   "• time_slots (1) ──< appointments (*) : Một slot có thể có lịch sử hủy và lần đặt lại\n" +
                   "• appointments (1) ─── medical_records (0..1) : Bệnh án điện tử của ca khám (1 - 1)\n" +
                   "• medical_records (1) ─── prescriptions (0..1) : Đơn thuốc của bệnh án (1 - 1)\n" +
                   "• prescriptions (1) ──< prescription_items (*) : Chi tiết các loại thuốc trong đơn\n" +
                   "• medicines (1) ──< prescription_items (*) : Thuốc được kê từ danh mục kho dược\n" +
                   "• appointments (1) ─── invoices (0..1) : Hóa đơn tài chính của ca khám (1 - 1)\n" +
                   "• users (1) ──< notifications (*) : Thông báo hệ thống gửi tới người dùng", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        // Hình 4.1: Lược đồ logic
        sb.append(drawingImage("rIdImg1", 101, "diagram_logical_erd.png", 5700000, 3720000, "Hình 4.1. Lược đồ quan hệ thực thể logic (Logical ERD - 3NF)"));
        sb.append(p("", null, false, 12, "left"));

        // 4.2. Sơ đồ phân quyền truy cập dữ liệu & chức năng (RBAC)
        sb.append(p("4.2. Yêu cầu phân quyền truy cập dữ liệu & chức năng (RBAC)", "Heading2", true, 14, "left"));
        sb.append(p("Hệ thống MedClinic áp dụng mô hình phân quyền dựa trên vai trò (Role-Based Access Control - RBAC) kết hợp cơ chế xác thực Spring Security và JWT. Ma trận phân quyền kiểm soát nghiêm ngặt quyền đọc/ghi trên từng bảng dữ liệu và phân hệ chức năng:\n" +
                   "• Quản trị viên (ROLE_ADMIN): Toàn quyền quản trị tài khoản, phân quyền, cấu hình chuyên khoa, quản lý bác sĩ, kho dược và theo dõi báo cáo doanh thu KPI.\n" +
                   "• Bác sĩ chuyên khoa (ROLE_DOCTOR): Quản lý lịch khám, tiếp nhận bệnh nhân theo hàng chờ STT, tra cứu tiền sử bệnh án, ghi nhận chẩn đoán và kê đơn thuốc điện tử (tự động trừ kho).\n" +
                   "• Nhân viên tiếp nhận / Lễ tân (ROLE_STAFF): Tra cứu thông tin lịch hẹn, quét mã QR check-in cấp STT tự động, đăng ký ca khám vãng lai (walk-in), thu viện phí (tiền mặt / VNPay) và xuất hóa đơn PDF.\n" +
                   "• Bệnh nhân (ROLE_PATIENT): Quản lý hồ sơ cá nhân, tra cứu bác sĩ/chuyên khoa, đặt lịch khám theo khung giờ với khóa lạc quan (@Version chống trùng lịch), xem mã QR check-in, tra cứu bệnh án và tải đơn thuốc PDF.\n" +
                   "• Khách vãng lai (ROLE_ANONYMOUS): Tra cứu danh mục dịch vụ, bảng giá chuyên khoa, danh sách bác sĩ và đăng ký tài khoản bệnh nhân mới.", null, false, 12, "both"));

        sb.append(drawingImage("rIdImg2", 102, "diagram_rbac.png", 5700000, 3420000, "Hình 4.2. Sơ đồ phân quyền truy cập dữ liệu và chức năng (RBAC)"));
        sb.append(p("", null, false, 12, "left"));

        // 4.3. Chi tiết các bảng dữ liệu
        sb.append(p("4.3. Chi tiết các bảng dữ liệu (Data Dictionary)", "Heading2", true, 14, "left"));

        // Bảng 1: users
        sb.append(p("Bảng 4.1. Người dùng (users)", "Heading3", true, 13, "left"));
        sb.append(p("users(id, username, password, full_name, email, phone, gender, date_of_birth, address, role_id, is_active, created_at)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã định danh người dùng", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "username", "VARCHAR(50)", "Duy nhất, Not Null", "Tên đăng nhập hệ thống", "Unique Index"));
        sb.append(tableRow(false, "3", "password", "VARCHAR(255)", "Not Null, 60 ký tự", "Mật khẩu đã mã hóa bằng BCrypt", "Bảo mật"));
        sb.append(tableRow(false, "4", "full_name", "VARCHAR(100)", "Not Null", "Họ và tên đầy đủ của người dùng", ""));
        sb.append(tableRow(false, "5", "email", "VARCHAR(100)", "Duy nhất, Email chuẩn", "Địa chỉ thư điện tử", "Unique Index"));
        sb.append(tableRow(false, "6", "phone", "VARCHAR(20)", "10 - 11 chữ số", "Số điện thoại liên hệ", "Index tìm kiếm"));
        sb.append(tableRow(false, "7", "gender", "VARCHAR(10)", "'MALE', 'FEMALE', 'OTHER'", "Giới tính", ""));
        sb.append(tableRow(false, "8", "date_of_birth", "DATE", "< Ngày hiện tại", "Ngày tháng năm sinh", ""));
        sb.append(tableRow(false, "9", "address", "VARCHAR(255)", "", "Địa chỉ nơi cư trú", ""));
        sb.append(tableRow(false, "10", "role_id", "BIGINT", "FK -> roles(id)", "Vai trò phân quyền của người dùng", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "11", "is_active", "BOOLEAN", "TRUE / FALSE", "Trạng thái kích hoạt tài khoản", "Default: TRUE"));
        sb.append(tableRow(false, "12", "created_at", "DATETIME", "Thời điểm tạo", "Thời điểm tạo tài khoản trong hệ thống", "Auto generated"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 2: roles
        sb.append(p("Bảng 4.2. Vai trò người dùng (roles)", "Heading3", true, 13, "left"));
        sb.append(p("roles(id, name, description)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã định danh vai trò", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "name", "VARCHAR(50)", "'ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_STAFF', 'ROLE_ADMIN'", "Tên vai trò chuẩn Spring Security", "Unique Index"));
        sb.append(tableRow(false, "3", "description", "VARCHAR(255)", "", "Mô tả chi tiết quyền hạn vai trò", ""));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 3: specialties
        sb.append(p("Bảng 4.3. Chuyên khoa (specialties)", "Heading3", true, 13, "left"));
        sb.append(p("specialties(id, name, description, icon, is_active)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã chuyên khoa", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "name", "VARCHAR(100)", "Duy nhất, Not Null", "Tên chuyên khoa y tế (Nội, Ngoại, Nhi, Da liễu...)", "Unique Index"));
        sb.append(tableRow(false, "3", "description", "TEXT", "", "Mô tả chi tiết phạm vi khám của chuyên khoa", ""));
        sb.append(tableRow(false, "4", "icon", "VARCHAR(100)", "Tên class icon Bootstrap", "Biểu tượng minh họa chuyên khoa", ""));
        sb.append(tableRow(false, "5", "is_active", "BOOLEAN", "TRUE / FALSE", "Trạng thái hoạt động", "Default: TRUE"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 4: doctor_profiles
        sb.append(p("Bảng 4.4. Hồ sơ bác sĩ (doctor_profiles)", "Heading3", true, 13, "left"));
        sb.append(p("doctor_profiles(id, user_id, specialty_id, title, experience_years, consultation_fee, bio)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã hồ sơ bác sĩ", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "user_id", "BIGINT", "Duy nhất, FK -> users(id)", "Tài khoản người dùng của bác sĩ", "Khóa ngoại 1-1"));
        sb.append(tableRow(false, "3", "specialty_id", "BIGINT", "FK -> specialties(id)", "Chuyên khoa công tác chính", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "4", "title", "VARCHAR(50)", "'ThS.BS', 'TS.BS', 'PGS.TS', 'BSCKI', 'BSCKII'", "Học hàm / Học vị bác sĩ", ""));
        sb.append(tableRow(false, "5", "experience_years", "INT", ">= 0", "Số năm kinh nghiệm làm việc", ""));
        sb.append(tableRow(false, "6", "consultation_fee", "DECIMAL(12,2)", ">= 0 VNĐ", "Đơn giá khám tư vấn ban đầu", "Not Null"));
        sb.append(tableRow(false, "7", "bio", "TEXT", "", "Tiểu sử đào tạo, quá trình công tác", ""));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 5: doctor_schedules
        sb.append(p("Bảng 4.5. Lịch phân ca bác sĩ (doctor_schedules)", "Heading3", true, 13, "left"));
        sb.append(p("doctor_schedules(id, doctor_id, work_date, shift, max_patients)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã ca làm việc", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "doctor_id", "BIGINT", "FK -> doctor_profiles(id)", "Bác sĩ phụ trách ca", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "3", "work_date", "DATE", "Ngày làm việc", "Ngày diễn ra ca trực", "Index tìm kiếm"));
        sb.append(tableRow(false, "4", "shift", "VARCHAR(20)", "'MORNING', 'AFTERNOON'", "Buổi trực trong ngày", ""));
        sb.append(tableRow(false, "5", "max_patients", "INT", "> 0, mặc định 8", "Số bệnh nhân tiếp nhận tối đa trong ca", "Default: 8"));
        sb.append(endTable());
        sb.append(p("Ràng buộc toàn vẹn: UNIQUE KEY uk_doctor_schedule (doctor_id, work_date, shift) ngăn chặn phân trùng ca làm việc.", null, false, 11, "left"));
        sb.append(p("", null, false, 11, "left"));

        // Bảng 6: time_slots
        sb.append(p("Bảng 4.6. Khung giờ khám (time_slots)", "Heading3", true, 13, "left"));
        sb.append(p("time_slots(id, schedule_id, start_time, end_time, status, version)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã khung giờ khám 30 phút", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "schedule_id", "BIGINT", "FK -> doctor_schedules(id)", "Ca làm việc chứa khung giờ", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "3", "start_time", "TIME", "HH:mm:ss", "Thời gian bắt đầu slot", "Not Null"));
        sb.append(tableRow(false, "4", "end_time", "TIME", "HH:mm:ss", "Thời gian kết thúc slot", "Not Null"));
        sb.append(tableRow(false, "5", "status", "VARCHAR(20)", "'AVAILABLE', 'BOOKED', 'BLOCKED'", "Trạng thái khả dụng của slot", "Not Null"));
        sb.append(tableRow(false, "6", "version", "BIGINT", ">= 0, mặc định 0", "Phiên bản khóa lạc quan (Optimistic Lock)", "@Version chống trùng lịch"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 7: appointments
        sb.append(p("Bảng 4.7. Lịch hẹn khám bệnh (appointments)", "Heading3", true, 13, "left"));
        sb.append(p("appointments(id, booking_code, patient_id, doctor_id, time_slot_id, appointment_date, reason, status, queue_number, queue_sequence, version, created_at)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã định danh lịch hẹn", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "booking_code", "VARCHAR(30)", "Duy nhất, chuẩn MED-YYMMDD-XXXX", "Mã đặt lịch duy nhất của bệnh nhân", "Unique Index"));
        sb.append(tableRow(false, "3", "patient_id", "BIGINT", "FK -> users(id)", "Bệnh nhân đặt khám", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "4", "doctor_id", "BIGINT", "FK -> doctor_profiles(id)", "Bác sĩ phụ trách khám", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "5", "time_slot_id", "BIGINT", "FK -> time_slots(id)", "Khung giờ đã đặt; cho phép lịch sử hủy", "Many-to-one"));
        sb.append(tableRow(false, "6", "appointment_date", "DATE", "Ngày hẹn khám", "Ngày diễn ra cuộc hẹn", "Index tìm kiếm"));
        sb.append(tableRow(false, "7", "reason", "TEXT", "", "Lý do khám / Triệu chứng bệnh nhân mô tả", ""));
        sb.append(tableRow(false, "8", "status", "VARCHAR(30)", "'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'", "Trạng thái vòng đời ca khám", "Not Null"));
        sb.append(tableRow(false, "9", "queue_number", "VARCHAR(20)", "Chuẩn STT-XX", "Số thứ tự khám trong ngày", "Cấp khi Check-in"));
        sb.append(tableRow(false, "10", "queue_sequence", "BIGINT", "Duy nhất cùng appointment_date", "Giá trị số để sắp queue", "Unique composite"));
        sb.append(tableRow(false, "11", "version", "BIGINT", "Optimistic lock", "Version trạng thái appointment", "@Version"));
        sb.append(tableRow(false, "10", "created_at", "DATETIME", "Thời điểm đặt", "Thời điểm đặt lịch hẹn", "Auto generated"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 8: medical_records
        sb.append(p("Bảng 4.8. Bệnh án điện tử (medical_records)", "Heading3", true, 13, "left"));
        sb.append(p("medical_records(id, appointment_id, doctor_id, patient_id, symptoms, diagnosis, doctor_notes, re_exam_date, created_at)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã bệnh án điện tử", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "appointment_id", "BIGINT", "Duy nhất, FK -> appointments(id)", "Ca khám tương ứng", "Khóa ngoại 1-1"));
        sb.append(tableRow(false, "3", "doctor_id", "BIGINT", "FK -> doctor_profiles(id)", "Bác sĩ khám và kết luận", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "4", "patient_id", "BIGINT", "FK -> users(id)", "Bệnh nhân được khám", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "5", "symptoms", "TEXT", "Not Null", "Triệu chứng lâm sàng ghi nhận", ""));
        sb.append(tableRow(false, "6", "diagnosis", "TEXT", "Not Null", "Kết luận chẩn đoán y khoa", ""));
        sb.append(tableRow(false, "7", "doctor_notes", "TEXT", "", "Lời dặn dò, chế độ ăn uống, kiêng cử", ""));
        sb.append(tableRow(false, "8", "re_exam_date", "DATE", "> appointment_date", "Ngày hẹn tái khám (nếu có)", ""));
        sb.append(tableRow(false, "9", "created_at", "DATETIME", "Thời điểm khám", "Thời điểm hoàn thành ca khám", "Auto generated"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 9: medicines
        sb.append(p("Bảng 4.9. Danh mục thuốc & Kho dược (medicines)", "Heading3", true, 13, "left"));
        sb.append(p("medicines(id, name, unit, unit_price, usage_guide, stock_quantity, is_active, version)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã thuốc", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "name", "VARCHAR(150)", "Duy nhất, Not Null", "Tên thuốc biệt dược / hoạt chất", "Unique Index"));
        sb.append(tableRow(false, "3", "unit", "VARCHAR(30)", "'Viên', 'Vỉ', 'Hộp', 'Chai', 'Gói'", "Đơn vị tính", "Not Null"));
        sb.append(tableRow(false, "4", "unit_price", "DECIMAL(12,2)", ">= 0 VNĐ", "Đơn giá bán lẻ một đơn vị", "Not Null"));
        sb.append(tableRow(false, "5", "usage_guide", "VARCHAR(255)", "", "Hướng dẫn sử dụng cơ bản", ""));
        sb.append(tableRow(false, "6", "stock_quantity", "INT", ">= 0, mặc định 1000", "Số lượng thuốc còn tồn trong kho", "Default: 1000"));
        sb.append(tableRow(false, "7", "is_active", "BOOLEAN", "TRUE / FALSE", "Trạng thái kinh doanh", "Default: TRUE"));
        sb.append(tableRow(false, "8", "version", "BIGINT", "Optimistic lock", "Bảo vệ thay đổi tồn kho", "@Version"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 10: prescriptions
        sb.append(p("Bảng 4.10. Đơn thuốc điện tử (prescriptions)", "Heading3", true, 13, "left"));
        sb.append(p("prescriptions(id, medical_record_id, doctor_notes, created_at)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã đơn thuốc", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "medical_record_id", "BIGINT", "Duy nhất, FK -> medical_records(id)", "Hồ sơ bệnh án gắn với đơn thuốc", "Khóa ngoại 1-1"));
        sb.append(tableRow(false, "3", "doctor_notes", "TEXT", "", "Ghi chú chung của bác sĩ về đơn thuốc", ""));
        sb.append(tableRow(false, "4", "created_at", "DATETIME", "Thời điểm kê đơn", "Thời điểm lập đơn thuốc", "Auto generated"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 11: prescription_items
        sb.append(p("Bảng 4.11. Chi tiết dòng thuốc trong đơn (prescription_items)", "Heading3", true, 13, "left"));
        sb.append(p("prescription_items(id, prescription_id, medicine_id, quantity, unit_price_at_time, dosage, frequency, instructions)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã chi tiết dòng thuốc", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "prescription_id", "BIGINT", "FK -> prescriptions(id)", "Đơn thuốc chứa dòng thuốc này", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "3", "medicine_id", "BIGINT", "FK -> medicines(id)", "Thuốc được kê", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "4", "quantity", "INT", "> 0", "Số lượng thuốc kê đơn", "Not Null"));
        sb.append(tableRow(false, "5", "unit_price_at_time", "DECIMAL(12,2)", ">= 0 VNĐ", "Giá thuốc tại thời điểm kê", "Historical snapshot"));
        sb.append(tableRow(false, "6", "dosage", "VARCHAR(100)", "VD: '1 viên / lần'", "Liều lượng dùng mỗi lần", "Not Null"));
        sb.append(tableRow(false, "7", "frequency", "VARCHAR(100)", "VD: '2 lần / ngày'", "Tần suất uống trong ngày", "Not Null"));
        sb.append(tableRow(false, "8", "instructions", "VARCHAR(255)", "VD: 'Uống sau bữa ăn 30 phút'", "Hướng dẫn dùng chi tiết", ""));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 12: invoices
        sb.append(p("Bảng 4.12. Hóa đơn thanh toán viện phí (invoices)", "Heading3", true, 13, "left"));
        sb.append(p("invoices(id, appointment_id, invoice_code, consultation_fee, medicine_fee, total_amount, payment_method, payment_status, paid_at, created_at)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã định danh hóa đơn", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "appointment_id", "BIGINT", "Duy nhất, FK -> appointments(id)", "Ca khám được thanh toán", "Khóa ngoại 1-1"));
        sb.append(tableRow(false, "3", "invoice_code", "VARCHAR(30)", "Duy nhất, chuẩn INV-YYMMDD-XXXX", "Mã hóa đơn tài chính duy nhất", "Unique Index"));
        sb.append(tableRow(false, "4", "consultation_fee", "DECIMAL(12,2)", ">= 0 VNĐ", "Tiền công khám tư vấn của bác sĩ", "Not Null"));
        sb.append(tableRow(false, "5", "medicine_fee", "DECIMAL(12,2)", ">= 0 VNĐ, mặc định 0", "Tổng tiền thuốc trong đơn", "Default: 0"));
        sb.append(tableRow(false, "6", "total_amount", "DECIMAL(12,2)", "= consultation_fee + medicine_fee", "Tổng tiền thanh toán cuối cùng", "Not Null"));
        sb.append(tableRow(false, "7", "payment_method", "VARCHAR(20)", "'CASH'", "Phương thức thanh toán", "VNPay/IPN là backlog"));
        sb.append(tableRow(false, "8", "payment_status", "VARCHAR(20)", "'UNPAID', 'PAID', 'REFUNDED'", "Trạng thái thanh toán", "Not Null"));
        sb.append(tableRow(false, "9", "paid_at", "DATETIME", "", "Thời điểm xác nhận thanh toán thành công", ""));
        sb.append(tableRow(false, "10", "created_at", "DATETIME", "Thời điểm tạo", "Thời điểm hệ thống tự động sinh hóa đơn", "Auto generated"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 13: daily_queue_counters
        sb.append(p("Bảng 4.13. Bộ cấp số thứ tự theo ngày (daily_queue_counters)", "Heading3", true, 13, "left"));
        sb.append(p("daily_queue_counters(id, queue_date, next_sequence, version)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã counter", "Khóa chính"));
        sb.append(tableRow(false, "2", "queue_date", "DATE", "Duy nhất, Not Null", "Ngày cấp số", "Một counter mỗi ngày"));
        sb.append(tableRow(false, "3", "next_sequence", "BIGINT", ">= 1", "Sequence tiếp theo", "Cấp STT dạng số"));
        sb.append(tableRow(false, "4", "version", "BIGINT", "Optimistic lock", "Bảo vệ counter", "@Version"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 14: invoice_code_reservations
        sb.append(p("Bảng 4.14. Giữ chỗ mã hóa đơn (invoice_code_reservations)", "Heading3", true, 13, "left"));
        sb.append(p("invoice_code_reservations(invoice_code)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "invoice_code", "VARCHAR(30)", "PK, Duy nhất", "Mã được reserve trước khi phát hành invoice", "Insert-only collision guard"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // Bảng 13: notifications
        sb.append(p("Bảng 4.15. Thông báo người dùng (notifications)", "Heading3", true, 13, "left"));
        sb.append(p("notifications(id, user_id, title, content, type, is_read, created_at)", null, true, 11, "left"));
        sb.append(startTable());
        sb.append(tableRow(true, "STT", "Thuộc tính", "Kiểu", "Miền giá trị", "Ý nghĩa", "Ghi chú"));
        sb.append(tableRow(false, "1", "id", "BIGINT", "Tự tăng, >0", "Mã định danh thông báo", "Khóa chính (PK)"));
        sb.append(tableRow(false, "2", "user_id", "BIGINT", "FK -> users(id)", "Người dùng nhận thông báo", "Khóa ngoại (FK)"));
        sb.append(tableRow(false, "3", "title", "VARCHAR(150)", "Not Null", "Tiêu đề thông báo", ""));
        sb.append(tableRow(false, "4", "content", "TEXT", "Not Null", "Nội dung chi tiết thông báo", ""));
        sb.append(tableRow(false, "5", "type", "VARCHAR(50)", "'BOOKING', 'QUEUE', 'INVOICE', 'SYSTEM'", "Phân loại thông báo", ""));
        sb.append(tableRow(false, "6", "is_read", "BOOLEAN", "TRUE / FALSE, mặc định FALSE", "Trạng thái đã đọc hay chưa", "Default: FALSE"));
        sb.append(tableRow(false, "7", "created_at", "DATETIME", "Thời điểm tạo", "Thời điểm gửi thông báo", "Auto generated"));
        sb.append(endTable());
        sb.append(p("", null, false, 11, "left"));

        // 4.4. Sơ đồ quan hệ CSDL chi tiết (Physical Database Schema)
        sb.append(p("4.4. Sơ đồ quan hệ cơ sở dữ liệu vật lý chi tiết (Physical ERD)", "Heading2", true, 14, "left"));
        sb.append(p("Sơ đồ quan hệ cơ sở dữ liệu vật lý thể hiện các bảng lõi, queue counter, reservation mã invoice, kiểu dữ liệu, PK/FK/UK và cơ chế @Version. Migration production cần được áp dụng theo tài liệu migration riêng:", null, false, 12, "both"));

        sb.append(drawingImage("rIdImg3", 103, "diagram_physical_schema.png", 5700000, 3735000, "Hình 4.3. Sơ đồ quan hệ cơ sở dữ liệu vật lý chi tiết (Physical Database Schema)"));
        sb.append(p("", null, false, 12, "left"));

        return wrapDocument(sb.toString());
    }
}
