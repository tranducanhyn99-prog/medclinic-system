package com.medclinic.config;

import com.medclinic.entity.*;
import com.medclinic.enums.*;
import com.medclinic.repository.*;
import com.medclinic.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@Profile({"dev", "demo"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final InvoiceRepository invoiceRepository;
    private final DailyQueueCounterRepository dailyQueueCounterRepository;
    private final ScheduleService scheduleService;
    private final MedicineRepository medicineRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final java.time.Clock businessClock;

    @Value("${app.demo.seed-appointments:false}")
    private boolean seedAppointments;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Khởi tạo Dữ liệu mẫu (Seed Data) cho hệ thống MedClinic...");

        // 1. Roles
        Role roleAdmin = initRole(RoleType.ROLE_ADMIN, "Quản trị viên toàn quyền hệ thống");
        Role roleDoctor = initRole(RoleType.ROLE_DOCTOR, "Bác sĩ khám bệnh và kê đơn");
        Role roleStaff = initRole(RoleType.ROLE_STAFF, "Lễ tân tiếp nhận và thu ngân");
        Role rolePatient = initRole(RoleType.ROLE_PATIENT, "Bệnh nhân đặt lịch khám");

        // 2. Default Users
        initUser("admin", "123456", "Quản Trị Viên Hệ Thống", "admin@medclinic.vn", "0900000001", roleAdmin);
        initUser("staff", "123456", "Nguyễn Thị Lễ Tân", "staff@medclinic.vn", "0900000002", roleStaff);
        User mainPatient = initUser("patient", "123456", "Trần Văn Bệnh Nhân", "patient@medclinic.vn", "0912345678", rolePatient);

        // 3. Specialties
        Specialty spTimMach = initSpecialty("Chuyên Khoa Tim Mạch", "Chẩn đoán, điều trị tăng huyết áp, bệnh mạch vành, rối loạn nhịp tim và bệnh van tim.", "bi-heart-pulse-fill");
        Specialty spRangHamMat = initSpecialty("Chuyên Khoa Răng Hàm Mặt", "Khám và điều trị các bệnh lý răng miệng, nha khoa thẩm mỹ, niềng răng và cấy ghép Implant.", "bi-emoji-smile-fill");
        Specialty spDaLieu = initSpecialty("Chuyên Khoa Da Liễu", "Điều trị mụn trứng cá, viêm da cơ địa, dị ứng, nám da và các bệnh lý da liễu chuyên sâu.", "bi-shield-plus");
        Specialty spNhiKhoa = initSpecialty("Chuyên Khoa Nhi", "Khám tổng quát, tư vấn dinh dưỡng, tiêm chủng và điều trị bệnh lý nhi khoa sơ sinh đến 15 tuổi.", "bi-people-fill");
        Specialty spTaiMuiHong = initSpecialty("Chuyên Khoa Tai Mũi Họng", "Nội soi chẩn đoán viêm xoang, viêm họng hạt, viêm tai giữa, khàn tiếng và polyp thanh quản.", "bi-headset");
        Specialty spXuongKhop = initSpecialty("Chuyên Khoa Cơ Xương Khớp", "Điều trị thoái hóa khớp, thoát vị đĩa đệm, viêm khớp dạng thấp, loãng xương và đau thần kinh tọa.", "bi-activity");

        // 4. Doctors (12 Doctors linked 1-1 to User accounts across 6 Specialties)
        DoctorProfile doc1 = initDoctor("doctor", "123456", "Nguyễn Văn Hùng", "hung.nguyen@medclinic.vn", "0911000001",
                spTimMach, "PGS.TS", 18, new BigDecimal("350000"),
                "Nguyên Trưởng khoa Tim mạch Bệnh viện Chợ Rẫy, hơn 18 năm kinh nghiệm điều trị tim mạch can thiệp và tăng huyết áp.",
                "/images/doctors/doctor_hung.jpg", Gender.MALE);

        DoctorProfile doc2 = initDoctor("doc_mai", "123456", "Trần Thị Mai", "mai.tran@medclinic.vn", "0911000002",
                spRangHamMat, "ThS.BS", 12, new BigDecimal("250000"),
                "Thạc sĩ Bác sĩ Đại học Y Dược TP.HCM, chuyên gia phục hình răng sứ thẩm mỹ và chỉnh nha chuyên sâu.",
                "/images/doctors/doctor_mai.jpg", Gender.FEMALE);

        DoctorProfile doc3 = initDoctor("doc_nam", "123456", "Lê Hoàng Nam", "nam.le@medclinic.vn", "0911000003",
                spDaLieu, "BS.CKII", 15, new BigDecimal("300000"),
                "Bác sĩ Chuyên khoa II Da liễu, nguyên Phó khoa Da liễu Bệnh viện Da liễu Trung ương.",
                "/images/doctors/doctor_nam.jpg", Gender.MALE);

        DoctorProfile doc4 = initDoctor("doc_ha", "123456", "Phạm Thu Hà", "ha.pham@medclinic.vn", "0911000004",
                spNhiKhoa, "ThS.BS", 10, new BigDecimal("200000"),
                "Tốt nghiệp Thủ khoa Bác sĩ Nội trú Nhi khoa, tận tâm, giàu kinh nghiệm chăm sóc sức khỏe trẻ nhỏ.",
                "/images/doctors/doctor_ha.jpg", Gender.FEMALE);

        DoctorProfile doc5 = initDoctor("doc_bao", "123456", "Vũ Quốc Bảo", "bao.vu@medclinic.vn", "0911000005",
                spTaiMuiHong, "BS.CKI", 8, new BigDecimal("200000"),
                "Chuyên gia nội soi tai mũi họng công nghệ cao, phẫu thuật polyp và điều trị viêm mũi xoang mạn tính.",
                "/images/doctors/doctor_bao.jpg", Gender.MALE);

        DoctorProfile doc6 = initDoctor("doc_tuan", "123456", "Đặng Minh Tuấn", "tuan.dang@medclinic.vn", "0911000006",
                spXuongKhop, "TS.BS", 20, new BigDecimal("400000"),
                "Tiến sĩ Y khoa, tu nghiệp tại CHLB Đức, chuyên gia đầu ngành về thoái hóa khớp và phẫu thuật nội soi khớp.",
                "/images/doctors/doctor_tuan.jpg", Gender.MALE);

        DoctorProfile doc7 = initDoctor("doc_linh", "123456", "Hoàng Phương Linh", "linh.hoang@medclinic.vn", "0911000007",
                spTimMach, "TS.BS", 14, new BigDecimal("320000"),
                "Tiến sĩ Y khoa chuyên ngành Nội Tim mạch, chuyên sâu siêu âm tim Doppler và điều trị suy tim, rối loạn nhịp.",
                "/images/doctors/doctor_linh.jpg", Gender.FEMALE);

        DoctorProfile doc8 = initDoctor("doc_khoa", "123456", "Ngô Đăng Khoa", "khoa.ngo@medclinic.vn", "0911000008",
                spRangHamMat, "BS.CKII", 16, new BigDecimal("300000"),
                "Bác sĩ Chuyên khoa II Răng Hàm Mặt, chuyên gia phẫu thuật cấy ghép Implant kỹ thuật số và tiểu phẫu răng khôn.",
                "/images/doctors/doctor_khoa.jpg", Gender.MALE);

        DoctorProfile doc9 = initDoctor("doc_lan", "123456", "Bùi Ngọc Lan", "lan.bui@medclinic.vn", "0911000009",
                spDaLieu, "ThS.BS", 11, new BigDecimal("250000"),
                "Thạc sĩ Bác sĩ Da liễu thẩm mỹ nội khoa, chuyên điều trị sẹo rỗ, sắc tố da bằng Laser và viêm da dị ứng.",
                "/images/doctors/doctor_lan.jpg", Gender.FEMALE);

        DoctorProfile doc10 = initDoctor("doc_duc", "123456", "Phan Minh Đức", "duc.phan@medclinic.vn", "0911000010",
                spNhiKhoa, "BS.CKII", 17, new BigDecimal("280000"),
                "Nguyên Phó trưởng khoa Hô hấp - Tiêu hóa Nhi, hơn 17 năm kinh nghiệm khám và điều trị bệnh lý hô hấp trẻ em.",
                "/images/doctors/doctor_duc.jpg", Gender.MALE);

        DoctorProfile doc11 = initDoctor("doc_trang", "123456", "Đỗ Huyền Trang", "trang.do@medclinic.vn", "0911000011",
                spTaiMuiHong, "ThS.BS", 9, new BigDecimal("220000"),
                "Thạc sĩ Bác sĩ chuyên khoa Tai Mũi Họng, chuyên nội soi chẩn đoán sớm bệnh lý thanh quản, viêm tai giữa và viêm xoang.",
                "/images/doctors/doctor_trang.jpg", Gender.FEMALE);

        DoctorProfile doc12 = initDoctor("doc_minh", "123456", "Trương Quang Minh", "minh.truong@medclinic.vn", "0911000012",
                spXuongKhop, "BS.CKI", 13, new BigDecimal("260000"),
                "Bác sĩ Chuyên khoa I Cơ Xương Khớp và Phục hồi chức năng, chuyên điều trị đau cột sống cổ - thắt lưng và chấn thương thể thao.",
                "/images/doctors/doctor_minh.jpg", Gender.MALE);

        markSeedAccountsOtpBypass();

        // 5. Generate 14-day Schedules & Slots for Doctors
        List<DoctorProfile> allDoctors = List.of(doc1, doc2, doc3, doc4, doc5, doc6, doc7, doc8, doc9, doc10, doc11, doc12);
        LocalDate today = LocalDate.now(businessClock);
        for (DoctorProfile doc : allDoctors) {
            for (int i = 0; i <= 14; i++) {
                LocalDate workDate = today.plusDays(i);
                scheduleService.createOrGetSchedule(doc.getId(), workDate, ShiftType.MORNING);
                scheduleService.createOrGetSchedule(doc.getId(), workDate, ShiftType.AFTERNOON);
            }
        }

        // 6. Medicines catalog
        initMedicines();

        // 7. Seed 10 Sample Clinical Cases ordered strictly by time (08:00 -> 15:00)
        if (seedAppointments && appointmentRepository.count() == 0) {
            initSampleAppointments(rolePatient, mainPatient, doc1, doc2, doc3, doc6, today);
        }

        log.info("Khởi tạo Seed Data thành công! Hệ thống MedClinic đã sẵn sàng.");
    }

    private void initSampleAppointments(
            Role rolePatient,
            User mainPatient,
            DoctorProfile docHung,
            DoctorProfile docMai,
            DoctorProfile docNam,
            DoctorProfile docTuan,
            LocalDate today) {

        User bnViet = initPatient("bn_viet", "Đặng Quốc Việt", "viet.dang@medclinic.vn", "0938112206", Gender.MALE, LocalDate.of(1972, 6, 30), rolePatient);
        User bnLanAnh = initPatient("bn_lananh", "Bùi Thị Lan Anh", "lananh.bui@medclinic.vn", "0938112207", Gender.FEMALE, LocalDate.of(1998, 12, 9), rolePatient);
        User bnDuyen = initPatient("bn_duyen", "Lê Thị Mỹ Duyên", "duyen.le@medclinic.vn", "0938112201", Gender.FEMALE, LocalDate.of(1995, 4, 12), rolePatient);
        User bnBao = initPatient("bn_bao", "Hoàng Gia Bảo", "bao.hoang@medclinic.vn", "0938112205", Gender.MALE, LocalDate.of(1983, 2, 14), rolePatient);
        User bnTam = initPatient("bn_tam", "Nguyễn Minh Tâm", "tam.nguyen@medclinic.vn", "0938112203", Gender.MALE, LocalDate.of(1976, 11, 5), rolePatient);
        User bnThao = initPatient("bn_thao", "Võ Thị Thanh Thảo", "thao.vo@medclinic.vn", "0938112204", Gender.FEMALE, LocalDate.of(1992, 7, 18), rolePatient);
        User bnKhang = initPatient("bn_khang", "Phạm Hoàng Khang", "khang.pham@medclinic.vn", "0938112202", Gender.MALE, LocalDate.of(1988, 9, 23), rolePatient);
        User bnQuan = initPatient("bn_quan", "Trương Minh Quân", "quan.truong@medclinic.vn", "0938112208", Gender.MALE, LocalDate.of(1985, 3, 21), rolePatient);

        List<Medicine> meds = medicineRepository.findAll();
        Medicine amlodipine = meds.stream().filter(m -> m.getName().contains("Amlodipine")).findFirst().orElse(meds.get(0));
        Medicine atorvastatin = meds.stream().filter(m -> m.getName().contains("Atorvastatin")).findFirst().orElse(meds.get(1));
        Medicine panadol = meds.stream().filter(m -> m.getName().contains("Panadol")).findFirst().orElse(meds.get(2));
        Medicine augmentin = meds.stream().filter(m -> m.getName().contains("Augmentin")).findFirst().orElse(meds.get(3));

        LocalDate yesterday = today.minusDays(1);

        // =====================================================================
        // DÒNG THỜI GIAN CA SÁNG (08:00 -> 11:30):
        // Các khung giờ đầu sáng đã khám xong (COMPLETED) -> Khung giờ giữa sáng đang khám (IN_PROGRESS)
        // -> Khung giờ cuối sáng đã check-in đang ngồi chờ (CHECKED_IN)
        // =====================================================================

        // 1. [08:00 - 08:30] MED-DEMO-001 | STT-001 | ĐÃ KHÁM HOÀN TẤT (COMPLETED) + ĐÃ THU TIỀN (PAID lúc 08:28)
        Appointment appt1 = createSeededAppointment(
                "MED-DEMO-001", mainPatient, docHung, today, ShiftType.MORNING, LocalTime.of(8, 0),
                "Đo huyết áp tại nhà tăng cao 150/95 mmHg, đau đầu nhẹ, kiểm tra tim mạch tổng quát",
                AppointmentStatus.COMPLETED, "STT-001", 1L, true,
                yesterday.atTime(15, 10), today.atTime(8, 22));
        attachMedicalRecordAndInvoice(
                appt1,
                "Huyết áp đo tại phòng khám 150/95 mmHg, mạch 84 lần/phút, đau mỏi vùng gáy nhẹ, tiếng tim T1 T2 đều rõ.",
                "Tăng huyết áp vô căn độ 1 (I10) kèm Rối loạn chuyển hóa Lipoprotein (E78).",
                "Hạn chế ăn mặn (< 5g muối/ngày), tập thể dục nhẹ 30 phút/ngày, đo huyết áp sáng - tối.",
                today.plusDays(14),
                List.of(
                        new SeedRxItem(amlodipine, 30, "1 viên / lần", "1 lần / ngày (Buổi sáng)", "Uống sau ăn sáng 30 phút"),
                        new SeedRxItem(atorvastatin, 30, "1 viên / lần", "1 lần / ngày (Buổi tối)", "Uống sau ăn tối")
                ),
                "HD-DEMO-001",
                PaymentStatus.PAID,
                PaymentMethod.CASH,
                today.atTime(8, 22),
                today.atTime(8, 28));

        // 2. [08:30 - 09:00] MED-DEMO-002 | STT-002 | ĐÃ KHÁM HOÀN TẤT (COMPLETED lúc 08:55) + CHỜ THU NGÂN (UNPAID)
        Appointment appt2 = createSeededAppointment(
                "MED-DEMO-002", bnViet, docHung, today, ShiftType.MORNING, LocalTime.of(8, 30),
                "Đau thắt ngực ổn định khi đi bộ xa, tê mỏi vai trái, theo dõi thiếu máu cơ tim",
                AppointmentStatus.COMPLETED, "STT-002", 2L, true,
                yesterday.atTime(16, 5), today.atTime(8, 55));
        attachMedicalRecordAndInvoice(
                appt2,
                "Đau tức sau xương ức từng cơn ngắn 3-5 phút khi gắng sức, giảm khi nghỉ ngơi; HA 140/90 mmHg, nhịp tim 78 chu kỳ/phút.",
                "Cơn đau thắt ngực ổn định (I20.8) - Theo dõi bệnh tim thiếu máu cục bộ mạn tính.",
                "Tránh vận động gắng sức đột ngột, kiêng rượu bia và thuốc lá, tái khám ngay nếu cơn đau kéo dài trên 15 phút.",
                today.plusDays(7),
                List.of(
                        new SeedRxItem(amlodipine, 30, "1 viên / lần", "1 lần / ngày (Buổi sáng)", "Uống cố định vào 7h sáng"),
                        new SeedRxItem(panadol, 2, "1 viên / lần", "Khi đau đầu nhiều (tối đa 2 lần/ngày)", "Uống sau khi ăn no")
                ),
                "HD-DEMO-002",
                PaymentStatus.UNPAID,
                PaymentMethod.CASH,
                today.atTime(8, 55),
                null);

        // 3. [09:00 - 09:30] MED-DEMO-003 | Không cấp STT | ĐÃ HỦY (CANCELLED lúc 07:30 sáng trước giờ khám)
        createSeededAppointment(
                "MED-DEMO-003", bnLanAnh, docTuan, today, ShiftType.MORNING, LocalTime.of(9, 0),
                "Đau mỏi vùng cột sống thắt lưng lan xuống chân trái (Bệnh nhân báo bận đột xuất xin hủy lịch lúc 07:30)",
                AppointmentStatus.CANCELLED, null, null, false,
                yesterday.atTime(17, 20), today.atTime(7, 30));

        // 4. [09:30 - 10:00] MED-DEMO-004 | STT-003 | ĐÃ KHÁM HOÀN TẤT (COMPLETED lúc 09:52) + ĐÃ THU TIỀN (PAID lúc 09:58)
        Appointment appt4 = createSeededAppointment(
                "MED-DEMO-004", bnDuyen, docMai, today, ShiftType.MORNING, LocalTime.of(9, 30),
                "Đau nhức răng hàm dưới bên phải khi ăn đồ lạnh, kiểm tra viêm tủy răng số 46",
                AppointmentStatus.COMPLETED, "STT-003", 3L, true,
                yesterday.atTime(18, 0), today.atTime(9, 52));
        attachMedicalRecordAndInvoice(
                appt4,
                "Răng số 46 sâu ngà sâu sát tủy, gõ dọc đau nhẹ, lợi quanh cổ răng sưng đỏ nhẹ, chưa lung lay.",
                "Viêm tủy răng có hồi phục răng 46 (K04.0) kèm viêm lợi khu trú.",
                "Vệ sinh răng miệng bằng nước muối sinh lý sau ăn, tránh nhai đồ cứng bên phải, hẹn tái khám hàn phục hồi.",
                today.plusDays(5),
                List.of(
                        new SeedRxItem(augmentin, 14, "1 viên / lần", "2 lần / ngày (Sáng, Tối)", "Uống ngay sau bữa ăn"),
                        new SeedRxItem(panadol, 1, "1 viên / lần", "2 lần / ngày khi đau nhức", "Uống sau ăn no")
                ),
                "HD-DEMO-003",
                PaymentStatus.PAID,
                PaymentMethod.CASH,
                today.atTime(9, 52),
                today.atTime(9, 58));

        // 5. [10:00 - 10:30] MED-DEMO-005 | STT-004 | ĐANG KHÁM (IN_PROGRESS — Bác sĩ Hùng bắt đầu khám lúc 10:05)
        createSeededAppointment(
                "MED-DEMO-005", bnBao, docHung, today, ShiftType.MORNING, LocalTime.of(10, 0),
                "Chóng mặt khi thay đổi tư thế, mệt mỏi, tiền sử rối loạn lipid máu cần kiểm tra",
                AppointmentStatus.IN_PROGRESS, "STT-004", 4L, true,
                yesterday.atTime(19, 15), today.atTime(10, 5));

        // 6. [10:30 - 11:00] MED-DEMO-006 | STT-005 | ĐÃ TIẾP NHẬN - CHỜ KHÁM (CHECKED_IN lúc 10:15, chờ sau STT-004)
        createSeededAppointment(
                "MED-DEMO-006", bnTam, docHung, today, ShiftType.MORNING, LocalTime.of(10, 30),
                "Huyết áp tăng cao 155/95 mmHg kèm đau đầu vùng chẩm buổi sáng",
                AppointmentStatus.CHECKED_IN, "STT-005", 5L, true,
                yesterday.atTime(20, 0), today.atTime(10, 15));

        // 7. [11:00 - 11:30] MED-DEMO-007 | STT-006 | ĐÃ TIẾP NHẬN - CHỜ KHÁM (CHECKED_IN lúc 10:45, chờ khám kế tiếp)
        createSeededAppointment(
                "MED-DEMO-007", bnThao, docHung, today, ShiftType.MORNING, LocalTime.of(11, 0),
                "Nhịp tim nhanh 105 lần/phút, khó thở nhẹ khi leo cầu thang",
                AppointmentStatus.CHECKED_IN, "STT-006", 6L, true,
                today.atTime(7, 40), today.atTime(10, 45));

        // =====================================================================
        // DÒNG THỜI GIAN CA CHIỀU (13:30 -> 15:00):
        // Vì là các khung giờ buổi chiều chưa diễn ra nên cả 3 ca đều ở trạng thái
        // CHƯA TIẾP NHẬN (CONFIRMED — Chờ bệnh nhân đến quầy Lễ tân Check-in)
        // =====================================================================

        // 8. [13:30 - 14:00] MED-DEMO-008 | Chưa cấp STT | CHƯA TIẾP NHẬN (CONFIRMED — Lịch hẹn đầu giờ chiều)
        createSeededAppointment(
                "MED-DEMO-008", bnKhang, docHung, today, ShiftType.AFTERNOON, LocalTime.of(13, 30),
                "Tái khám kiểm tra huyết áp định kỳ và siêu âm tim Doppler theo lịch hẹn chiều",
                AppointmentStatus.CONFIRMED, null, null, true,
                yesterday.atTime(21, 10), yesterday.atTime(21, 10));

        // 9. [14:00 - 14:30] MED-DEMO-009 | Chưa cấp STT | CHƯA TIẾP NHẬN (CONFIRMED — Lịch hẹn chiều của patient)
        createSeededAppointment(
                "MED-DEMO-009", mainPatient, docNam, today, ShiftType.AFTERNOON, LocalTime.of(14, 0),
                "Nổi mẩn đỏ ngứa dị ứng vùng cổ và hai cánh tay sau khi ăn hải sản",
                AppointmentStatus.CONFIRMED, null, null, true,
                today.atTime(8, 35), today.atTime(8, 35));

        // 10. [14:30 - 15:00] MED-DEMO-010 | Chưa cấp STT | CHƯA TIẾP NHẬN (CONFIRMED — Lịch hẹn chiều Cơ Xương Khớp)
        createSeededAppointment(
                "MED-DEMO-010", bnQuan, docTuan, today, ShiftType.AFTERNOON, LocalTime.of(14, 30),
                "Đau nhức khớp gối phải khi lên xuống cầu thang, cứng khớp buổi sáng",
                AppointmentStatus.CONFIRMED, null, null, true,
                today.atTime(9, 10), today.atTime(9, 10));

        // Đồng bộ bộ đếm STT hàng ngày (nextSequence = 6) để lượt Check-in tiếp theo cấp STT-007
        DailyQueueCounter counter = dailyQueueCounterRepository.findByQueueDateForUpdate(today)
                .orElseGet(() -> DailyQueueCounter.builder().queueDate(today).nextSequence(6L).build());
        counter.setNextSequence(6L);
        dailyQueueCounterRepository.save(counter);

        log.info("Đã khởi tạo 10 ca khám mẫu (MED-DEMO-001 .. MED-DEMO-010) theo đúng trình tự thời gian 08:00 -> 15:00!");
    }

    private record SeedRxItem(Medicine medicine, int quantity, String dosage, String frequency, String instructions) {}

    private Appointment createSeededAppointment(
            String bookingCode,
            User patient,
            DoctorProfile doctor,
            LocalDate date,
            ShiftType shift,
            LocalTime startTime,
            String reason,
            AppointmentStatus status,
            String queueNumber,
            Long queueSequence,
            boolean markSlotBooked,
            LocalDateTime bookedAt,
            LocalDateTime updatedAt) {
        scheduleService.createOrGetSchedule(doctor.getId(), date, shift);
        List<TimeSlot> availableSlots = timeSlotRepository.findAvailableSlotsByDoctorAndDate(
                doctor.getId(), date, SlotStatus.AVAILABLE);
        TimeSlot slot = availableSlots.stream()
                .filter(s -> s.getStartTime().equals(startTime))
                .findFirst()
                .orElseGet(availableSlots::getFirst);
        if (markSlotBooked) {
            slot.setStatus(SlotStatus.BOOKED);
            timeSlotRepository.save(slot);
        }
        Appointment saved = appointmentRepository.saveAndFlush(Appointment.builder()
                .bookingCode(bookingCode)
                .patient(patient)
                .doctor(doctor)
                .timeSlot(slot)
                .appointmentDate(date)
                .reason(reason)
                .status(status)
                .queueNumber(queueNumber)
                .queueSequence(queueSequence)
                .build());
        jdbcTemplate.update(
                "UPDATE appointments SET created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.valueOf(bookedAt), Timestamp.valueOf(updatedAt), saved.getId());
        return saved;
    }

    private void attachMedicalRecordAndInvoice(
            Appointment appointment,
            String symptoms,
            String diagnosis,
            String doctorNotes,
            LocalDate reExamDate,
            List<SeedRxItem> rxItems,
            String invoiceCode,
            PaymentStatus paymentStatus,
            PaymentMethod paymentMethod,
            LocalDateTime examinedAt,
            LocalDateTime paidAt) {
        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .doctor(appointment.getDoctor())
                .patient(appointment.getPatient())
                .symptoms(symptoms)
                .diagnosis(diagnosis)
                .doctorNotes(doctorNotes)
                .reExamDate(reExamDate)
                .build();

        Prescription prescription = Prescription.builder()
                .medicalRecord(record)
                .doctorNotes(doctorNotes)
                .build();

        for (SeedRxItem item : rxItems) {
            prescription.getItems().add(PrescriptionItem.builder()
                    .prescription(prescription)
                    .medicine(item.medicine())
                    .quantity(item.quantity())
                    .unitPriceAtTime(item.medicine().getUnitPrice())
                    .dosage(item.dosage())
                    .frequency(item.frequency())
                    .instructions(item.instructions())
                    .build());
        }
        record.setPrescription(prescription);
        MedicalRecord savedRecord = medicalRecordRepository.saveAndFlush(record);
        jdbcTemplate.update(
                "UPDATE medical_records SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(examinedAt), savedRecord.getId());
        if (savedRecord.getPrescription() != null && savedRecord.getPrescription().getId() != null) {
            jdbcTemplate.update(
                    "UPDATE prescriptions SET created_at = ? WHERE id = ?",
                    Timestamp.valueOf(examinedAt), savedRecord.getPrescription().getId());
        }

        BigDecimal consultationFee = appointment.getDoctor().getConsultationFee();
        BigDecimal medicineFee = prescription.calculateTotalMedicinePrice();
        BigDecimal totalAmount = consultationFee.add(medicineFee);

        Invoice savedInvoice = invoiceRepository.saveAndFlush(Invoice.builder()
                .appointment(appointment)
                .invoiceCode(invoiceCode)
                .consultationFee(consultationFee)
                .medicineFee(medicineFee)
                .totalAmount(totalAmount)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .paidAt(paidAt)
                .build());
        jdbcTemplate.update(
                "UPDATE invoices SET created_at = ?, paid_at = ? WHERE id = ?",
                Timestamp.valueOf(examinedAt),
                paidAt != null ? Timestamp.valueOf(paidAt) : null,
                savedInvoice.getId());
    }

    private User initPatient(String username, String fullName, String email, String phone,
                             Gender gender, LocalDate dob, Role rolePatient) {
        return userRepository.findByUsername(username).orElseGet(() ->
                userRepository.save(User.builder()
                        .username(username)
                        .password(passwordEncoder.encode("123456"))
                        .fullName(fullName)
                        .email(email)
                        .phone(phone)
                        .gender(gender)
                        .dateOfBirth(dob)
                        .address("TP. Hồ Chí Minh")
                        .role(rolePatient)
                        .emailVerified(true)
                        .otpBypass(true)
                        .isActive(true)
                        .build())
        );
    }

    private Role initRole(RoleType roleType, String description) {
        return roleRepository.findByName(roleType).orElseGet(() ->
                roleRepository.save(Role.builder().name(roleType).description(description).build())
        );
    }

    private User initUser(String username, String rawPassword, String fullName, String email, String phone, Role role) {
        return userRepository.findByUsername(username).orElseGet(() ->
                userRepository.save(User.builder()
                        .username(username)
                        .password(passwordEncoder.encode(rawPassword))
                        .fullName(fullName)
                        .email(email)
                        .phone(phone)
                        .gender(Gender.MALE)
                        .dateOfBirth(LocalDate.of(1990, 1, 1))
                        .address("Hồ Chí Minh")
                        .role(role)
                        .emailVerified(true)
                        .otpBypass(true)
                        .isActive(true)
                        .build())
        );
    }

    private void markSeedAccountsOtpBypass() {
        List<String> usernames = List.of(
                "admin", "staff", "patient",
                "doctor", "doc_mai", "doc_nam", "doc_ha", "doc_bao", "doc_tuan",
                "doc_linh", "doc_khoa", "doc_lan", "doc_duc", "doc_trang", "doc_minh"
        );
        for (String username : usernames) {
            userRepository.findByUsername(username).ifPresent(user -> {
                if (!Boolean.TRUE.equals(user.getOtpBypass()) || !Boolean.TRUE.equals(user.getEmailVerified())) {
                    user.setOtpBypass(true);
                    user.setEmailVerified(true);
                    userRepository.save(user);
                }
            });
        }
    }

    private Specialty initSpecialty(String name, String desc, String icon) {
        return specialtyRepository.findByName(name).orElseGet(() ->
                specialtyRepository.save(Specialty.builder()
                        .name(name)
                        .description(desc)
                        .icon(icon)
                        .isActive(true)
                        .build())
        );
    }

    private DoctorProfile initDoctor(String username, String rawPassword, String fullName, String email, String phone,
                                     Specialty specialty, String title, int expYears, BigDecimal fee, String bio,
                                     String avatarUrl, Gender gender) {
        Role doctorRole = roleRepository.findByName(RoleType.ROLE_DOCTOR).orElseThrow();
        User user = userRepository.findByUsername(username).map(existing -> {
            boolean changed = false;
            if (avatarUrl != null && !avatarUrl.equals(existing.getAvatarUrl())) {
                existing.setAvatarUrl(avatarUrl);
                changed = true;
            }
            if (gender != null && existing.getGender() != gender) {
                existing.setGender(gender);
                changed = true;
            }
            if (phone != null && !phone.equals(existing.getPhone())) {
                existing.setPhone(phone);
                changed = true;
            }
            if (email != null && !email.equals(existing.getEmail())) {
                existing.setEmail(email);
                changed = true;
            }
            if (fullName != null && !fullName.equals(existing.getFullName())) {
                existing.setFullName(fullName);
                changed = true;
            }
            return changed ? userRepository.save(existing) : existing;
        }).orElseGet(() ->
                userRepository.save(User.builder()
                        .username(username)
                        .password(passwordEncoder.encode(rawPassword))
                        .fullName(fullName)
                        .email(email)
                        .phone(phone)
                        .gender(gender)
                        .dateOfBirth(LocalDate.of(1980, 5, 15))
                        .address("TP. Hồ Chí Minh")
                        .avatarUrl(avatarUrl)
                        .role(doctorRole)
                        .emailVerified(true)
                        .otpBypass(true)
                        .isActive(true)
                        .build())
        );

        return doctorProfileRepository.findByUserId(user.getId()).orElseGet(() ->
                doctorProfileRepository.save(DoctorProfile.builder()
                        .user(user)
                        .specialty(specialty)
                        .licenseNumber("GPHN-" + user.getId())
                        .title(title)
                        .experienceYears(expYears)
                        .consultationFee(fee)
                        .bio(bio)
                        .build())
        );
    }

    private void initMedicines() {
        if (medicineRepository.count() > 0) {
            return;
        }

        List<Medicine> medicines = List.of(
                Medicine.builder().name("Paracetamol 500mg (Hasan)").unit("Viên").unitPrice(new BigDecimal("1500")).usageGuide("Hạ sốt, giảm đau từ nhẹ đến vừa. Uống sau khi ăn.").stockQuantity(2000).isActive(true).build(),
                Medicine.builder().name("Augmentin 1g (Amoxicillin/Clavulanate)").unit("Viên").unitPrice(new BigDecimal("18500")).usageGuide("Kháng sinh điều trị nhiễm khuẩn đường hô hấp, tai mũi họng.").stockQuantity(500).isActive(true).build(),
                Medicine.builder().name("Amlodipine 5mg").unit("Viên").unitPrice(new BigDecimal("3500")).usageGuide("Thuốc điều trị tăng huyết áp, đau thắt ngực. Uống buổi sáng.").stockQuantity(1200).isActive(true).build(),
                Medicine.builder().name("Nexium Mups 40mg (Esomeprazole)").unit("Viên").unitPrice(new BigDecimal("24000")).usageGuide("Điều trị viêm loét dạ dày tá tràng, trào ngược GERD. Uống trước ăn 30p.").stockQuantity(600).isActive(true).build(),
                Medicine.builder().name("Panadol Extra Đỏ").unit("Vỉ").unitPrice(new BigDecimal("22000")).usageGuide("Giảm đau đầu, đau cơ, hạ sốt nhanh chóng.").stockQuantity(800).isActive(true).build(),
                Medicine.builder().name("Berberin 100mg").unit("Lọ").unitPrice(new BigDecimal("15000")).usageGuide("Điều trị tiêu chảy, kiết lỵ, viêm ruột.").stockQuantity(400).isActive(true).build(),
                Medicine.builder().name("Cefuroxime 500mg").unit("Viên").unitPrice(new BigDecimal("12000")).usageGuide("Kháng sinh Cephalosporin thế hệ 2 điều trị nhiễm khuẩn.").stockQuantity(700).isActive(true).build(),
                Medicine.builder().name("Gliclazide MR 60mg").unit("Viên").unitPrice(new BigDecimal("5000")).usageGuide("Thuốc hạ đường huyết cho người đái tháo đường type 2.").stockQuantity(900).isActive(true).build(),
                Medicine.builder().name("Atorvastatin 20mg").unit("Viên").unitPrice(new BigDecimal("8000")).usageGuide("Hạ mỡ máu, giảm Cholesterol và phòng ngừa biến cố tim mạch.").stockQuantity(850).isActive(true).build(),
                Medicine.builder().name("Klacid 500mg (Clarithromycin)").unit("Viên").unitPrice(new BigDecimal("32000")).usageGuide("Kháng sinh điều trị nhiễm khuẩn hô hấp và diệt vi khuẩn HP dạ dày.").stockQuantity(300).isActive(true).build(),
                Medicine.builder().name("Eugica Đỏ (Viên nang mềm)").unit("Hộp").unitPrice(new BigDecimal("65000")).usageGuide("Bổ phế, giảm ho, long đờm, rát họng có nguồn gốc thảo dược.").stockQuantity(450).isActive(true).build(),
                Medicine.builder().name("Vitamin C 500mg").unit("Tuýp").unitPrice(new BigDecimal("35000")).usageGuide("Tăng cường sức đề kháng, bổ sung vitamin C hằng ngày.").stockQuantity(600).isActive(true).build(),
                Medicine.builder().name("Natri Clorid 0.9% (Nước muối sinh lý)").unit("Chai").unitPrice(new BigDecimal("10000")).usageGuide("Dùng nhỏ mắt, rửa mũi, súc miệng hàng ngày.").stockQuantity(1500).isActive(true).build(),
                Medicine.builder().name("Salonpas Gel 30g").unit("Tuýp").unitPrice(new BigDecimal("42000")).usageGuide("Giảm đau nhức cơ bắp, đau khớp, bầm tím.").stockQuantity(350).isActive(true).build(),
                Medicine.builder().name("Oresol 245mg").unit("Gói").unitPrice(new BigDecimal("4000")).usageGuide("Bù nước và điện giải khi bị sốt cao hoặc tiêu chảy.").stockQuantity(2000).isActive(true).build()
        );

        medicineRepository.saveAll(medicines);
    }
}
