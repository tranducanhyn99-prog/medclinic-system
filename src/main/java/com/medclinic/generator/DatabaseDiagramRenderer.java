package com.medclinic.generator;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import javax.imageio.ImageIO;

public class DatabaseDiagramRenderer {

    public static void main(String[] args) throws Exception {
        Path outputDir = args.length > 0
                ? Paths.get(args[0])
                : Paths.get(System.getProperty("medclinic.docs.output", "docs/generated")).resolve("diagrams");
        renderAllDiagrams(outputDir);
    }

    public static void renderAllDiagrams(Path outputDir) throws Exception {
        Files.createDirectories(outputDir);
        System.setProperty("java.awt.headless", "true");

        renderLogicalErd(outputDir.resolve("diagram_logical_erd.png"));
        renderRbacDiagram(outputDir.resolve("diagram_rbac.png"));
        renderPhysicalSchema(outputDir.resolve("diagram_physical_schema.png"));
        renderUseCaseDiagram(outputDir.resolve("diagram_usecase_overview.png"));

        System.out.println("-> Đã vẽ và xuất thành công 4 sơ đồ tại: " + outputDir);
    }

    // =========================================================================
    // 1. SƠ ĐỒ LOGIC (LOGICAL ERD DIAGRAM)
    // =========================================================================
    private static void renderLogicalErd(Path target) throws Exception {
        int W = 1350;
        int H = 880;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        setupHighQuality(g);

        // Nền
        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, W, H);

        // Header banner
        g.setColor(new Color(30, 58, 138));
        g.fillRect(0, 0, W, 70);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times New Roman", Font.BOLD, 22));
        drawCenteredString(g, "HỆ THỐNG MEDCLINIC - LƯỢC ĐỒ QUAN HỆ THỰC THỂ LOGIC (3NF)", W / 2, 42);

        // Nhóm 1: Tài khoản & Phân quyền (Trái)
        drawEntityCard(g, 50, 110, 190, 130, "Role", new Color(71, 85, 105),
                new String[]{"id (PK)", "name", "description"});

        drawEntityCard(g, 290, 110, 210, 160, "User", new Color(30, 58, 138),
                new String[]{"id (PK)", "username", "password", "email", "role_id (FK)", "is_active"});

        drawEntityCard(g, 290, 310, 210, 130, "Notification", new Color(71, 85, 105),
                new String[]{"id (PK)", "user_id (FK)", "title", "content", "is_read"});

        // Nhóm 2: Bác sĩ & Lịch làm việc (Giữa trên)
        drawEntityCard(g, 570, 110, 220, 160, "Specialty", new Color(13, 148, 136),
                new String[]{"id (PK)", "name", "description", "icon", "is_active"});

        drawEntityCard(g, 570, 310, 220, 160, "DoctorProfile", new Color(13, 148, 136),
                new String[]{"id (PK)", "user_id (FK 1-1)", "specialty_id (FK)", "title", "consultation_fee"});

        drawEntityCard(g, 840, 310, 220, 140, "DoctorSchedule", new Color(13, 148, 136),
                new String[]{"id (PK)", "doctor_id (FK)", "work_date", "shift", "max_patients"});

        drawEntityCard(g, 1100, 310, 200, 140, "TimeSlot", new Color(2, 132, 199),
                new String[]{"id (PK)", "schedule_id (FK)", "start_time, end_time", "status", "version (@Version)"});

        // Nhóm 3: Lịch hẹn & Bệnh án (Giữa dưới)
        drawEntityCard(g, 570, 520, 230, 170, "Appointment", new Color(225, 29, 72),
                new String[]{"id (PK)", "booking_code", "patient_id (FK)", "doctor_id (FK)", "time_slot_id (FK N-1)", "queue_sequence", "version (@Version)"});

        drawEntityCard(g, 850, 520, 220, 160, "MedicalRecord", new Color(147, 51, 234),
                new String[]{"id (PK)", "appointment_id (FK 1-1)", "doctor_id (FK)", "patient_id (FK)", "symptoms, diagnosis", "re_exam_date"});

        drawEntityCard(g, 1110, 520, 200, 130, "Prescription", new Color(147, 51, 234),
                new String[]{"id (PK)", "medical_record_id (FK 1-1)", "doctor_notes", "created_at"});

        // Nhóm 4: Dược phẩm & Hóa đơn (Đáy)
        drawEntityCard(g, 1110, 690, 200, 140, "PrescriptionItem", new Color(217, 119, 6),
                new String[]{"id (PK)", "prescription_id (FK)", "medicine_id (FK)", "quantity", "unit_price_at_time", "dosage, frequency"});

        drawEntityCard(g, 850, 710, 220, 130, "Medicine", new Color(217, 119, 6),
                new String[]{"id (PK)", "name", "unit", "unit_price", "stock_quantity", "version (@Version)"});

        drawEntityCard(g, 290, 550, 220, 160, "Invoice", new Color(5, 150, 105),
                new String[]{"id (PK)", "appointment_id (FK 1-1)", "invoice_code", "consultation_fee", "medicine_fee", "total_amount", "payment_status"});

        // Vẽ các đường nối quan hệ (Orthogonal Connectors)
        // 1. Role -> User (1 - *)
        drawConnector(g, 240, 170, 290, 170, "1", "*", "role_id");
        // 2. User -> Notification (1 - *)
        drawConnector(g, 395, 270, 395, 310, "1", "*", "user_id");
        // 3. User -> DoctorProfile (1 - 1)
        drawOrthogonalConnector(g, 500, 190, 535, 380, 570, 380, "1", "1");
        // 4. Specialty -> DoctorProfile (1 - *)
        drawConnector(g, 680, 270, 680, 310, "1", "*", "specialty_id");
        // 5. DoctorProfile -> Schedule (1 - *)
        drawConnector(g, 790, 380, 840, 380, "1", "*", "doctor_id");
        // 6. Schedule -> TimeSlot (1 - *)
        drawConnector(g, 1060, 380, 1100, 380, "1", "*", "schedule_id");
        // 7. TimeSlot -> Appointment (1 - many historical appointments)
        drawOrthogonalConnector(g, 1150, 450, 1150, 485, 750, 485, 750, 520, "1", "*");
        // 8. User (Patient) -> Appointment (1 - *)
        drawOrthogonalConnector(g, 340, 270, 340, 485, 630, 485, 630, 520, "1", "*");
        // 9. DoctorProfile -> Appointment (1 - *)
        drawConnector(g, 680, 470, 680, 520, "1", "*", "doctor_id");
        // 10. Appointment -> MedicalRecord (1 - 0..1)
        drawConnector(g, 800, 600, 850, 600, "1", "0..1", "appointment_id");
        // 11. MedicalRecord -> Prescription (1 - 0..1)
        drawConnector(g, 1070, 600, 1110, 600, "1", "0..1", "record_id");
        // 12. Prescription -> PrescriptionItem (1 - *)
        drawConnector(g, 1210, 650, 1210, 690, "1", "*", "presc_id");
        // 13. Medicine -> PrescriptionItem (1 - *)
        drawConnector(g, 1070, 760, 1110, 760, "1", "*", "medicine_id");
        // 14. Appointment -> Invoice (1 - 0..1)
        drawConnector(g, 570, 630, 510, 630, "1", "0..1", "appointment_id");

        // Chú thích (Legend)
        g.setColor(new Color(241, 245, 249));
        g.fillRoundRect(50, 780, 600, 60, 10, 10);
        g.setColor(new Color(148, 163, 184));
        g.drawRoundRect(50, 780, 600, 60, 10, 10);
        g.setColor(new Color(51, 65, 85));
        g.setFont(new Font("Times New Roman", Font.BOLD, 13));
        g.drawString("Chú thích:  1 ──< * : Quan hệ Một - Nhiều (1-N)    |    1 ──── 1 : Quan hệ Một - Một (1-1)", 70, 805);
        g.drawString("Màu sắc: Xanh dương: Người dùng/Auth | Xanh ngọc: Bác sĩ | Đỏ: Đặt hẹn | Tím: Bệnh án | Xanh lá: Viện phí", 70, 825);

        g.dispose();
        ImageIO.write(img, "png", target.toFile());
    }

    // =========================================================================
    // 2. SƠ ĐỒ PHÂN QUYỀN (RBAC DIAGRAM)
    // =========================================================================
    private static void renderRbacDiagram(Path target) throws Exception {
        int W = 1200;
        int H = 720;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        setupHighQuality(g);

        // Nền
        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, W, H);

        // Header banner
        g.setColor(new Color(15, 23, 42));
        g.fillRect(0, 0, W, 65);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times New Roman", Font.BOLD, 21));
        drawCenteredString(g, "HỆ THỐNG MEDCLINIC - SƠ ĐỒ PHÂN QUYỀN TRUY CẬP CHỨC NĂNG (RBAC)", W / 2, 40);

        // Cột trái: 5 Roles
        int roleX = 80;
        int roleW = 240;
        int roleH = 65;

        drawRoleBox(g, roleX, 100, roleW, roleH, "Quản trị viên (Admin)", "ROLE_ADMIN (Toàn quyền hệ thống)", new Color(225, 29, 72));
        drawRoleBox(g, roleX, 210, roleW, roleH, "Bác sĩ (Doctor)", "ROLE_DOCTOR (Khám, Bệnh án, Kê đơn)", new Color(13, 148, 136));
        drawRoleBox(g, roleX, 320, roleW, roleH, "Lễ tân (Staff)", "ROLE_STAFF (Check-in, Tiếp nhận, Thu viện phí)", new Color(217, 119, 6));
        drawRoleBox(g, roleX, 430, roleW, roleH, "Bệnh nhân (Patient)", "ROLE_PATIENT (Đặt lịch, Xem bệnh án, Đánh giá)", new Color(2, 132, 199));
        drawRoleBox(g, roleX, 540, roleW, roleH, "Khách (Guest)", "ROLE_ANONYMOUS (Tra cứu, Đăng ký)", new Color(100, 116, 139));

        // Cột phải: 6 Phân hệ chức năng
        int modX = 680;
        int modW = 440;
        int modH = 65;

        drawModuleBox(g, modX, 90, modW, modH, "Phân hệ 1: Cổng thông tin & Tra cứu Bác sĩ", "Xem danh mục chuyên khoa, tìm kiếm bác sĩ, xem lịch trực tuần", new Color(71, 85, 105));
        drawModuleBox(g, modX, 180, modW, modH, "Phân hệ 2: Đặt lịch khám & Quản lý Lịch hẹn", "Chọn TimeSlot 30p, đặt lịch có Optimistic Lock, hủy lịch hẹn, mã QR", new Color(2, 132, 199));
        drawModuleBox(g, modX, 270, modW, modH, "Phân hệ 3: Quầy tiếp nhận & Cấp STT (Walk-in)", "Tra cứu bệnh nhân, quét QR check-in, cấp số thứ tự, đăng ký vãng lai", new Color(217, 119, 6));
        drawModuleBox(g, modX, 360, modW, modH, "Phân hệ 4: Bàn khám lâm sàng & Bệnh án điện tử", "Gọi BN theo STT, ghi chẩn đoán, tra cứu tiền sử bệnh, hẹn ngày tái khám", new Color(147, 51, 234));
        drawModuleBox(g, modX, 450, modW, modH, "Phân hệ 5: Kê đơn thuốc & Quản trị Kho dược", "Chọn thuốc từ kho, trừ tồn kho tự động, xuất toa thuốc PDF chuẩn y tế", new Color(13, 148, 136));
        drawModuleBox(g, modX, 540, modW, modH, "Phân hệ 6: Quản lý Thu viện phí & Báo cáo KPI", "Thu tiền mặt/VNPay, xuất hóa đơn PDF, thống kê doanh thu và KPI", new Color(5, 150, 105));

        // Đường nối phân quyền
        g.setStroke(new BasicStroke(1.5f));
        drawRbacLine(g, roleX + roleW, 132, modX, 122, new Color(225, 29, 72));
        drawRbacLine(g, roleX + roleW, 132, modX, 572, new Color(225, 29, 72));

        drawRbacLine(g, roleX + roleW, 242, modX, 392, new Color(13, 148, 136));
        drawRbacLine(g, roleX + roleW, 242, modX, 482, new Color(13, 148, 136));

        drawRbacLine(g, roleX + roleW, 352, modX, 302, new Color(217, 119, 6));
        drawRbacLine(g, roleX + roleW, 352, modX, 572, new Color(217, 119, 6));

        drawRbacLine(g, roleX + roleW, 462, modX, 122, new Color(2, 132, 199));
        drawRbacLine(g, roleX + roleW, 462, modX, 212, new Color(2, 132, 199));
        drawRbacLine(g, roleX + roleW, 462, modX, 392, new Color(2, 132, 199));

        drawRbacLine(g, roleX + roleW, 572, modX, 122, new Color(100, 116, 139));

        // Chú thích
        g.setColor(new Color(51, 65, 85));
        g.setFont(new Font("Times New Roman", Font.ITALIC, 13));
        g.drawString("* Mô hình phân quyền RBAC được kiểm soát nghiêm ngặt tại tầng Service và SecurityFilterChain (@PreAuthorize).", 80, 680);

        g.dispose();
        ImageIO.write(img, "png", target.toFile());
    }

    // =========================================================================
    // 3. SƠ ĐỒ CSDL VẬT LÝ CHI TIẾT (PHYSICAL SCHEMA)
    // =========================================================================
    private static void renderPhysicalSchema(Path target) throws Exception {
        int W = 1450;
        int H = 950;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        setupHighQuality(g);

        // Nền
        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, W, H);

        // Header banner
        g.setColor(new Color(15, 23, 42));
        g.fillRect(0, 0, W, 65);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times New Roman", Font.BOLD, 22));
        drawCenteredString(g, "HỆ THỐNG MEDCLINIC - SƠ ĐỒ QUAN HỆ CƠ SỞ DỮ LIỆU VẬT LÝ (PHYSICAL ERD)", W / 2, 40);

        // Cột 1: users, roles, notifications
        drawTableSchema(g, 40, 90, 240, "roles",
                new String[]{"PK  id : BIGINT", "UK  name : VARCHAR(50)", "    description : VARCHAR(255)"});

        drawTableSchema(g, 40, 250, 240, "users",
                new String[]{"PK  id : BIGINT", "UK  username : VARCHAR(50)", "    password : VARCHAR(255)", "    full_name : VARCHAR(100)", "UK  email : VARCHAR(100)", "    phone : VARCHAR(20)", "    gender : VARCHAR(10)", "    date_of_birth : DATE", "    address : VARCHAR(255)", "FK  role_id : BIGINT", "    is_active : BOOLEAN", "    created_at : DATETIME"});

        drawTableSchema(g, 40, 600, 240, "notifications",
                new String[]{"PK  id : BIGINT", "FK  user_id : BIGINT", "    title : VARCHAR(150)", "    content : TEXT", "    type : VARCHAR(50)", "    is_read : BOOLEAN", "    created_at : DATETIME"});

        // Cột 2: doctor_profiles, specialties, invoices
        drawTableSchema(g, 340, 90, 260, "specialties",
                new String[]{"PK  id : BIGINT", "UK  name : VARCHAR(100)", "    description : TEXT", "    icon : VARCHAR(100)", "    is_active : BOOLEAN"});

        drawTableSchema(g, 340, 270, 260, "doctor_profiles",
                new String[]{"PK  id : BIGINT", "UK,FK  user_id : BIGINT (1-1)", "FK  specialty_id : BIGINT", "    title : VARCHAR(50)", "    experience_years : INT", "    consultation_fee : DECIMAL(12,2)", "    bio : TEXT"});

        drawTableSchema(g, 340, 520, 260, "invoices",
                new String[]{"PK  id : BIGINT", "UK,FK  appointment_id : BIGINT", "UK  invoice_code : VARCHAR(30)", "    consultation_fee : DECIMAL", "    medicine_fee : DECIMAL", "    total_amount : DECIMAL(12,2)", "    payment_method : VARCHAR", "    payment_status : VARCHAR", "    paid_at : DATETIME", "    created_at : DATETIME"});

        // Cột 3: doctor_schedules, appointments, medical_records
        drawTableSchema(g, 660, 90, 260, "doctor_schedules",
                new String[]{"PK  id : BIGINT", "FK  doctor_id : BIGINT", "    work_date : DATE", "    shift : VARCHAR(20)", "    max_patients : INT"});

        drawTableSchema(g, 660, 310, 260, "appointments",
                new String[]{"PK  id : BIGINT", "UK  booking_code : VARCHAR(30)", "FK  patient_id : BIGINT", "FK  doctor_id : BIGINT", "FK  time_slot_id : BIGINT", "    appointment_date : DATE", "    status : VARCHAR(30)", "UK* queue_sequence : BIGINT", "    version : BIGINT", "    created_at : DATETIME"});

        drawTableSchema(g, 660, 630, 260, "medical_records",
                new String[]{"PK  id : BIGINT", "UK,FK  appointment_id : BIGINT", "FK  doctor_id : BIGINT", "FK  patient_id : BIGINT", "    symptoms : TEXT", "    diagnosis : TEXT", "    doctor_notes : TEXT", "    re_exam_date : DATE", "    created_at : DATETIME"});

        // Cột 4: time_slots, medicines, prescriptions, prescription_items
        drawTableSchema(g, 980, 90, 270, "time_slots",
                new String[]{"PK  id : BIGINT", "FK  schedule_id : BIGINT", "    start_time : TIME", "    end_time : TIME", "    status : VARCHAR(20)", "    version : BIGINT (@Version)"});

        drawTableSchema(g, 980, 310, 270, "medicines",
                new String[]{"PK  id : BIGINT", "UK  name : VARCHAR(150)", "    unit : VARCHAR(30)", "    unit_price : DECIMAL(12,2)", "    stock_quantity : INT", "    is_active : BOOLEAN", "    version : BIGINT"});

        drawTableSchema(g, 980, 530, 270, "prescriptions",
                new String[]{"PK  id : BIGINT", "UK,FK  medical_record_id : BIGINT", "    doctor_notes : TEXT", "    created_at : DATETIME"});

        drawTableSchema(g, 980, 710, 270, "prescription_items",
                new String[]{"PK  id : BIGINT", "FK  prescription_id : BIGINT", "FK  medicine_id : BIGINT", "    quantity : INT", "    unit_price_at_time : DECIMAL", "    dosage : VARCHAR(100)", "    frequency : VARCHAR(100)"});

        // Đường nối khóa ngoại với Orthogonal routing
        g.setStroke(new BasicStroke(1.4f));
        g.setColor(new Color(79, 70, 229));

        // roles -> users
        drawFkLine(g, 160, 190, 160, 250);
        // users -> notifications
        drawFkLine(g, 160, 550, 160, 600);
        // users -> doctor_profiles (1-1)
        drawFkLine(g, 280, 320, 340, 320);
        // specialties -> doctor_profiles
        drawFkLine(g, 470, 220, 470, 270);
        // doctor_profiles -> schedules
        drawOrthogonalLine(g, 600, 300, 630, 300, 630, 140, 660, 140);
        // schedules -> time_slots
        drawFkLine(g, 920, 140, 980, 140);
        // time_slots -> appointments
        drawOrthogonalLine(g, 1110, 260, 1110, 285, 940, 285, 940, 390, 920, 390);
        // doctor_profiles -> appointments
        drawOrthogonalLine(g, 600, 360, 630, 360, 630, 380, 660, 380);
        // users (patient) -> appointments
        drawOrthogonalLine(g, 280, 480, 310, 480, 310, 505, 630, 505, 630, 420, 660, 420);
        // appointments -> invoices
        drawFkLine(g, 660, 560, 600, 560);
        // appointments -> medical_records
        drawFkLine(g, 790, 570, 790, 630);
        // medical_records -> prescriptions
        drawOrthogonalLine(g, 920, 670, 950, 670, 950, 600, 980, 600);
        // prescriptions -> prescription_items
        drawFkLine(g, 1110, 640, 1110, 710);
        // medicines -> prescription_items
        drawOrthogonalLine(g, 1110, 490, 1110, 510, 1270, 510, 1270, 770, 1250, 770);

        // Chú thích
        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("Times New Roman", Font.BOLD, 13));
        g.drawString("Ký hiệu:  PK: Khóa chính (Primary Key)  |  FK: Khóa ngoại (Foreign Key)  |  UK: Duy nhất (Unique Key)  |  Đường xanh: Khóa ngoại liên kết", 40, 930);

        g.dispose();
        ImageIO.write(img, "png", target.toFile());
    }

    // =========================================================================
    // 4. SƠ ĐỒ USE CASE TỔNG QUAN (USE CASE DIAGRAM)
    // =========================================================================
    private static void renderUseCaseDiagram(Path target) throws Exception {
        int W = 1350;
        int H = 860;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        setupHighQuality(g);

        // Nền
        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, W, H);

        // Header banner
        g.setColor(new Color(30, 58, 138));
        g.fillRect(0, 0, W, 65);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times New Roman", Font.BOLD, 22));
        drawCenteredString(g, "HỆ THỐNG MEDCLINIC - SƠ ĐỒ USE CASE TỔNG QUAN (ENTERPRISE ARCHITECT)", W / 2, 40);

        // Khung ranh giới hệ thống (System Boundary)
        int boxX = 220;
        int boxY = 85;
        int boxW = 880;
        int boxH = 750;
        g.setColor(Color.WHITE);
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setColor(new Color(148, 163, 184));
        g.setStroke(new BasicStroke(2f));
        g.drawRect(boxX, boxY, boxW, boxH);

        g.setColor(new Color(30, 58, 138));
        g.setFont(new Font("Times New Roman", Font.BOLD, 15));
        g.drawString("MedClinic System Boundary", boxX + 15, boxY + 25);

        // 5 Tác nhân (Actors)
        // Bên trái
        drawActorStick(g, 100, 200, "Khách (Guest)");
        drawActorStick(g, 100, 500, "Bệnh nhân (Patient)");

        // Bên phải
        drawActorStick(g, 1220, 180, "Bác sĩ (Doctor)");
        drawActorStick(g, 1220, 420, "Lễ tân (Staff)");
        drawActorStick(g, 1220, 670, "Admin (Quản trị viên)");

        // 4 Cột Use Case bên trong System Boundary
        // Cột 1: Xác thực & Tra cứu (x = 250)
        int c1X = 250, wUc = 185, hUc = 42;
        drawUseCaseOval(g, c1X, 130, wUc, hUc, "UC_001: Đăng ký tài khoản");
        drawUseCaseOval(g, c1X, 220, wUc, hUc, "UC_002: Đăng nhập hệ thống");
        drawUseCaseOval(g, c1X, 310, wUc, hUc, "UC_003: Đăng xuất hệ thống");
        drawUseCaseOval(g, c1X, 400, wUc, hUc, "UC_004: Khôi phục mật khẩu");
        drawUseCaseOval(g, c1X, 490, wUc, hUc, "UC_005: Quản lý thông tin");
        drawUseCaseOval(g, c1X, 580, wUc, hUc, "UC_006: Tìm kiếm & Lọc BS");
        drawUseCaseOval(g, c1X, 670, wUc, hUc, "UC_007: Xem chi tiết BS");

        // Cột 2: Đặt lịch & Bệnh án (x = 470)
        int c2X = 470;
        drawUseCaseOval(g, c2X, 170, wUc, hUc, "UC_008: Đặt lịch trực tuyến");
        drawUseCaseOval(g, c2X, 260, wUc, hUc, "UC_009: Quản lý & Hủy lịch");
        drawUseCaseOval(g, c2X, 350, wUc, hUc, "UC_010: Xem hồ sơ bệnh án");
        drawUseCaseOval(g, c2X, 440, wUc, hUc, "UC_017: Xuất đơn thuốc PDF");
        drawUseCaseOval(g, c2X, 530, wUc, hUc, "UC_020: Xuất hóa đơn PDF");
        drawUseCaseOval(g, c2X, 620, wUc, hUc, "UC_021: Đánh giá bác sĩ");

        // Cột 3: Khám bệnh & Tiếp nhận (x = 690)
        int c3X = 690;
        drawUseCaseOval(g, c3X, 130, wUc, hUc, "UC_011: Quầy tiếp nhận");
        drawUseCaseOval(g, c3X, 220, wUc, hUc, "UC_012: Quét QR & Cấp STT");
        drawUseCaseOval(g, c3X, 310, wUc, hUc, "UC_013: Tiếp nhận vãng lai");
        drawUseCaseOval(g, c3X, 400, wUc, hUc, "UC_014: Bàn khám & Hàng chờ");
        drawUseCaseOval(g, c3X, 490, wUc, hUc, "UC_015: Khám bệnh & Ghi án");
        drawUseCaseOval(g, c3X, 580, wUc, hUc, "UC_016: Kê đơn trừ tồn kho");
        drawUseCaseOval(g, c3X, 670, wUc, hUc, "UC_018: Tự sinh hóa đơn");

        // Cột 4: Thu ngân & Quản trị (x = 900)
        int c4X = 900;
        drawUseCaseOval(g, c4X, 170, wUc, hUc, "UC_019: Thu viện phí");
        drawUseCaseOval(g, c4X, 260, wUc, hUc, "UC_022: Quản lý chuyên khoa");
        drawUseCaseOval(g, c4X, 350, wUc, hUc, "UC_023: Quản lý bác sĩ");
        drawUseCaseOval(g, c4X, 440, wUc, hUc, "UC_024: Quản lý kho thuốc");
        drawUseCaseOval(g, c4X, 530, wUc, hUc, "UC_025: Quản lý tài khoản");
        drawUseCaseOval(g, c4X, 620, wUc, hUc, "UC_026: Thống kê KPI");

        // Các đường liên kết (Association Lines)
        g.setStroke(new BasicStroke(1.2f));
        g.setColor(new Color(100, 116, 139));

        // Khách -> UC1, 2, 4, 6, 7
        drawUcLine(g, 120, 200, c1X, 151);
        drawUcLine(g, 120, 200, c1X, 241);
        drawUcLine(g, 120, 200, c1X, 421);
        drawUcLine(g, 120, 200, c1X, 601);
        drawUcLine(g, 120, 200, c1X, 691);

        // Bệnh nhân -> UC2, 3, 5, 8, 9, 10, 17, 20, 21
        drawUcLine(g, 120, 500, c1X + wUc, 241);
        drawUcLine(g, 120, 500, c1X + wUc, 511);
        drawUcLine(g, 120, 500, c2X, 191);
        drawUcLine(g, 120, 500, c2X, 281);
        drawUcLine(g, 120, 500, c2X, 371);
        drawUcLine(g, 120, 500, c2X, 641);

        // Bác sĩ -> UC14, 15, 16, 17
        drawUcLine(g, 1200, 180, c3X + wUc, 421);
        drawUcLine(g, 1200, 180, c3X + wUc, 511);
        drawUcLine(g, 1200, 180, c3X + wUc, 601);
        drawUcLine(g, 1200, 180, c2X + wUc, 461);

        // Lễ tân -> UC11, 12, 13, 19, 20
        drawUcLine(g, 1200, 420, c3X + wUc, 151);
        drawUcLine(g, 1200, 420, c3X + wUc, 241);
        drawUcLine(g, 1200, 420, c3X + wUc, 331);
        drawUcLine(g, 1200, 420, c4X + wUc, 191);
        drawUcLine(g, 1200, 420, c2X + wUc, 551);

        // Admin -> UC22, 23, 24, 25, 26
        drawUcLine(g, 1200, 670, c4X + wUc, 281);
        drawUcLine(g, 1200, 670, c4X + wUc, 371);
        drawUcLine(g, 1200, 670, c4X + wUc, 461);
        drawUcLine(g, 1200, 670, c4X + wUc, 551);
        drawUcLine(g, 1200, 670, c4X + wUc, 641);

        g.dispose();
        ImageIO.write(img, "png", target.toFile());
    }

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH VẼ (DRAWING HELPERS)
    // =========================================================================
    private static void setupHighQuality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static void drawCenteredString(Graphics2D g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textX = x - (fm.stringWidth(text) / 2);
        g.drawString(text, textX, y);
    }

    private static void drawEntityCard(Graphics2D g, int x, int y, int w, int h, String name, Color headerCol, String[] attrs) {
        g.setColor(new Color(226, 232, 240));
        g.fillRoundRect(x + 3, y + 3, w, h, 8, 8);

        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, w, h, 8, 8);
        g.setColor(new Color(203, 213, 225));
        g.drawRoundRect(x, y, w, h, 8, 8);

        g.setColor(headerCol);
        g.fillRoundRect(x, y, w, 28, 8, 8);
        g.fillRect(x, y + 18, w, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times New Roman", Font.BOLD, 14));
        drawCenteredString(g, name, x + w / 2, y + 19);

        g.setColor(new Color(30, 41, 59));
        g.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        int py = y + 46;
        for (String attr : attrs) {
            if (attr.contains("(PK)")) {
                g.setColor(new Color(185, 28, 28));
                g.setFont(new Font("Times New Roman", Font.BOLD, 12));
            } else if (attr.contains("(FK")) {
                g.setColor(new Color(29, 78, 216));
                g.setFont(new Font("Times New Roman", Font.BOLD, 12));
            } else {
                g.setColor(new Color(51, 65, 85));
                g.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            }
            g.drawString("• " + attr, x + 10, py);
            py += 18;
        }
    }

    private static void drawTableSchema(Graphics2D g, int x, int y, int w, String name, String[] cols) {
        int h = 32 + cols.length * 19;

        g.setColor(Color.WHITE);
        g.fillRect(x, y, w, h);
        g.setColor(new Color(148, 163, 184));
        g.drawRect(x, y, w, h);

        g.setColor(new Color(30, 58, 138));
        g.fillRect(x, y, w, 26);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times New Roman", Font.BOLD, 14));
        g.drawString(name, x + 10, y + 18);

        int py = y + 42;
        for (String col : cols) {
            if (col.startsWith("PK")) {
                g.setColor(new Color(185, 28, 28));
                g.setFont(new Font("Times New Roman", Font.BOLD, 11));
            } else if (col.startsWith("FK") || col.contains("FK")) {
                g.setColor(new Color(29, 78, 216));
                g.setFont(new Font("Times New Roman", Font.BOLD, 11));
            } else if (col.startsWith("UK")) {
                g.setColor(new Color(4, 120, 87));
                g.setFont(new Font("Times New Roman", Font.BOLD, 11));
            } else {
                g.setColor(new Color(30, 41, 59));
                g.setFont(new Font("Times New Roman", Font.PLAIN, 11));
            }
            g.drawString(col, x + 8, py);
            py += 19;
        }
    }

    private static void drawRoleBox(Graphics2D g, int x, int y, int w, int h, String title, String sub, Color col) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(col);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, w, h, 10, 10);

        g.setColor(col);
        g.fillRect(x, y, 12, h);

        g.setColor(new Color(15, 23, 42));
        g.setFont(new Font("Times New Roman", Font.BOLD, 14));
        g.drawString(title, x + 22, y + 26);

        g.setColor(new Color(100, 116, 139));
        g.setFont(new Font("Times New Roman", Font.PLAIN, 11));
        g.drawString(sub, x + 22, y + 48);
    }

    private static void drawModuleBox(Graphics2D g, int x, int y, int w, int h, String title, String sub, Color col) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(new Color(203, 213, 225));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(x, y, w, h, 10, 10);

        g.setColor(col);
        g.fillRoundRect(x, y, 8, h, 6, 6);

        g.setColor(new Color(15, 23, 42));
        g.setFont(new Font("Times New Roman", Font.BOLD, 14));
        g.drawString(title, x + 20, y + 26);

        g.setColor(new Color(100, 116, 139));
        g.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        g.drawString(sub, x + 20, y + 48);
    }

    private static void drawConnector(Graphics2D g, int x1, int y1, int x2, int y2, String card1, String card2, String label) {
        g.setColor(new Color(100, 116, 139));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(x1, y1, x2, y2);

        g.setFont(new Font("Times New Roman", Font.BOLD, 12));
        g.setColor(new Color(185, 28, 28));
        g.drawString(card1, x1 + (x2 > x1 ? 5 : -15), y1 - 4);
        g.drawString(card2, x2 + (x2 > x1 ? -20 : 5), y2 - 4);
    }

    private static void drawOrthogonalConnector(Graphics2D g, int... pts) {
        g.setColor(new Color(100, 116, 139));
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < pts.length - 2; i += 2) {
            g.drawLine(pts[i], pts[i + 1], pts[i + 2], pts[i + 3]);
        }
    }

    private static void drawOrthogonalConnector(Graphics2D g, int x1, int y1, int x2, int y2, int x3, int y3, String card1, String card2) {
        g.setColor(new Color(100, 116, 139));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2, y2, x3, y3);

        g.setFont(new Font("Times New Roman", Font.BOLD, 12));
        g.setColor(new Color(185, 28, 28));
        g.drawString(card1, x1 + 4, y1 - 4);
        g.drawString(card2, x3 - 18, y3 - 4);
    }

    private static void drawOrthogonalConnector(Graphics2D g, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, String card1, String card2) {
        g.setColor(new Color(100, 116, 139));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2, y2, x3, y3);
        g.drawLine(x3, y3, x4, y4);

        g.setFont(new Font("Times New Roman", Font.BOLD, 12));
        g.setColor(new Color(185, 28, 28));
        g.drawString(card1, x1 + 4, y1 - 4);
        g.drawString(card2, x4 + 4, y4 - 4);
    }

    private static void drawRbacLine(Graphics2D g, int x1, int y1, int x2, int y2, Color col) {
        g.setColor(col);
        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(new int[]{x2, x2 - 8, x2 - 8}, new int[]{y2, y2 - 4, y2 + 4}, 3);
    }

    private static void drawFkLine(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
        g.fillOval(x2 - 3, y2 - 3, 6, 6);
    }

    private static void drawOrthogonalLine(Graphics2D g, int... pts) {
        for (int i = 0; i < pts.length - 2; i += 2) {
            g.drawLine(pts[i], pts[i + 1], pts[i + 2], pts[i + 3]);
        }
        int lastX = pts[pts.length - 2];
        int lastY = pts[pts.length - 1];
        g.fillOval(lastX - 3, lastY - 3, 6, 6);
    }

    private static void drawActorStick(Graphics2D g, int x, int y, String label) {
        g.setColor(new Color(30, 41, 59));
        g.setStroke(new BasicStroke(2f));
        // Đầu
        g.drawOval(x - 12, y - 35, 24, 24);
        // Thân
        g.drawLine(x, y - 11, x, y + 20);
        // Tay
        g.drawLine(x - 18, y, x + 18, y);
        // Chân
        g.drawLine(x, y + 20, x - 14, y + 42);
        g.drawLine(x, y + 20, x + 14, y + 42);

        // Nhãn
        g.setFont(new Font("Times New Roman", Font.BOLD, 13));
        drawCenteredString(g, label, x, y + 60);
    }

    private static void drawUseCaseOval(Graphics2D g, int x, int y, int w, int h, String label) {
        // Đổ bóng
        g.setColor(new Color(226, 232, 240));
        g.fillOval(x + 2, y + 2, w, h);

        // Oval thân
        g.setColor(new Color(239, 246, 255));
        g.fillOval(x, y, w, h);
        g.setColor(new Color(59, 130, 246));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x, y, w, h);

        // Chữ
        g.setColor(new Color(30, 58, 138));
        g.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        drawCenteredString(g, label, x + w / 2, y + h / 2 + 4);
    }

    private static void drawUcLine(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
    }
}
