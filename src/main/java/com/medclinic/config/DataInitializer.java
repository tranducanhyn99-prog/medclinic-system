package com.medclinic.config;

import com.medclinic.entity.*;
import com.medclinic.enums.Gender;
import com.medclinic.enums.RoleType;
import com.medclinic.enums.ShiftType;
import com.medclinic.repository.*;
import com.medclinic.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final ScheduleService scheduleService;
    private final MedicineRepository medicineRepository;
    private final PasswordEncoder passwordEncoder;

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
        initUser("patient", "123456", "Trần Văn Bệnh Nhân", "patient@medclinic.vn", "0912345678", rolePatient);

        // 3. Specialties
        Specialty spTimMach = initSpecialty("Chuyên Khoa Tim Mạch", "Chẩn đoán, điều trị tăng huyết áp, bệnh mạch vành, rối loạn nhịp tim và bệnh van tim.", "bi-heart-pulse-fill");
        Specialty spRangHamMat = initSpecialty("Chuyên Khoa Răng Hàm Mặt", "Khám và điều trị các bệnh lý răng miệng, nha khoa thẩm mỹ, niềng răng và cấy ghép Implant.", "bi-emoji-smile-fill");
        Specialty spDaLieu = initSpecialty("Chuyên Khoa Da Liễu", "Điều trị mụn trứng cá, viêm da cơ địa, dị ứng, nám da và các bệnh lý da liễu chuyên sâu.", "bi-shield-plus");
        Specialty spNhiKhoa = initSpecialty("Chuyên Khoa Nhi", "Khám tổng quát, tư vấn dinh dưỡng, tiêm chủng và điều trị bệnh lý nhi khoa sơ sinh đến 15 tuổi.", "bi-people-fill");
        Specialty spTaiMuiHong = initSpecialty("Chuyên Khoa Tai Mũi Họng", "Nội soi chẩn đoán viêm xoang, viêm họng hạt, viêm tai giữa, khàn tiếng và polyp thanh quản.", "bi-headset");
        Specialty spXuongKhop = initSpecialty("Chuyên Khoa Cơ Xương Khớp", "Điều trị thoái hóa khớp, thoát vị đĩa đệm, viêm khớp dạng thấp, loãng xương và đau thần kinh tọa.", "bi-activity");

        // 4. Doctors
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

        markSeedAccountsOtpBypass();

        // 5. Generate 14-day Schedules & Slots for Doctors
        List<DoctorProfile> allDoctors = List.of(doc1, doc2, doc3, doc4, doc5, doc6);
        LocalDate today = LocalDate.now();
        for (DoctorProfile doc : allDoctors) {
            for (int i = 0; i < 14; i++) {
                LocalDate workDate = today.plusDays(i);
                scheduleService.createOrGetSchedule(doc.getId(), workDate, ShiftType.MORNING);
                scheduleService.createOrGetSchedule(doc.getId(), workDate, ShiftType.AFTERNOON);
            }
        }

        // 6. Medicines catalog
        initMedicines();

        log.info("Khởi tạo Seed Data thành công! Hệ thống MedClinic đã sẵn sàng.");
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
        List<String> usernames = List.of("admin", "staff", "patient", "doctor", "doc_mai", "doc_nam", "doc_ha", "doc_bao", "doc_tuan");
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

