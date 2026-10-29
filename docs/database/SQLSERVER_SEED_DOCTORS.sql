-- MedClinic SQL Server Seed Script: 6 Specialties, 12 Doctor Accounts (users) & 12 Doctor Profiles (doctor_profiles)
-- Idempotent script: safe to run multiple times on a SQL Server database migrated through Flyway V1-V10.
-- Default demo password for all seeded doctor accounts is '123456' (BCrypt hashed below).

SET NOCOUNT ON;
SET XACT_ABORT ON;

DECLARE @doctorRoleId BIGINT;
SELECT @doctorRoleId = id FROM roles WHERE name = N'ROLE_DOCTOR';

IF @doctorRoleId IS NULL
    THROW 51000, 'ROLE_DOCTOR is missing. Apply V2__reference_roles.sql first.', 1;

-- BCrypt hash of '123456' (strength 10)
DECLARE @defaultPasswordHash NVARCHAR(255) = N'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

BEGIN TRANSACTION;

-- 1. Seed 6 Specialties
MERGE INTO specialties AS target
USING (VALUES
    (N'Chuyên Khoa Tim Mạch', N'Chẩn đoán, điều trị tăng huyết áp, bệnh mạch vành, rối loạn nhịp tim và bệnh van tim.', N'bi-heart-pulse-fill'),
    (N'Chuyên Khoa Răng Hàm Mặt', N'Khám và điều trị các bệnh lý răng miệng, nha khoa thẩm mỹ, niềng răng và cấy ghép Implant.', N'bi-emoji-smile-fill'),
    (N'Chuyên Khoa Da Liễu', N'Điều trị mụn trứng cá, viêm da cơ địa, dị ứng, nám da và các bệnh lý da liễu chuyên sâu.', N'bi-shield-plus'),
    (N'Chuyên Khoa Nhi', N'Khám tổng quát, tư vấn dinh dưỡng, tiêm chủng và điều trị bệnh lý nhi khoa sơ sinh đến 15 tuổi.', N'bi-people-fill'),
    (N'Chuyên Khoa Tai Mũi Họng', N'Nội soi chẩn đoán viêm xoang, viêm họng hạt, viêm tai giữa, khàn tiếng và polyp thanh quản.', N'bi-headset'),
    (N'Chuyên Khoa Cơ Xương Khớp', N'Điều trị thoái hóa khớp, thoát vị đĩa đệm, viêm khớp dạng thấp, loãng xương và đau thần kinh tọa.', N'bi-activity')
) AS src (name, description, icon)
ON target.name = src.name
WHEN NOT MATCHED THEN
    INSERT (name, description, icon, is_active)
    VALUES (src.name, src.description, src.icon, 1);

-- 2. Seed 12 Doctor Accounts in [users] and Linked Profiles in [doctor_profiles]
DECLARE @doctors TABLE (
    username NVARCHAR(50),
    full_name NVARCHAR(100),
    email NVARCHAR(100),
    phone NVARCHAR(20),
    gender NVARCHAR(10),
    avatar_url NVARCHAR(500),
    specialty_name NVARCHAR(100),
    license_number NVARCHAR(50),
    title NVARCHAR(100),
    experience_years INT,
    consultation_fee NUMERIC(12, 2),
    bio NVARCHAR(MAX)
);

INSERT INTO @doctors VALUES
    (N'doctor',    N'Nguyễn Văn Hùng',   N'hung.nguyen@medclinic.vn',  N'0911000001', N'MALE',   N'/images/doctors/doctor_hung.jpg',  N'Chuyên Khoa Tim Mạch',      N'GPHN-DOC-01', N'PGS.TS',  18, 350000, N'Nguyên Trưởng khoa Tim mạch Bệnh viện Chợ Rẫy, hơn 18 năm kinh nghiệm điều trị tim mạch can thiệp và tăng huyết áp.'),
    (N'doc_mai',   N'Trần Thị Mai',      N'mai.tran@medclinic.vn',     N'0911000002', N'FEMALE', N'/images/doctors/doctor_mai.jpg',   N'Chuyên Khoa Răng Hàm Mặt',  N'GPHN-DOC-02', N'ThS.BS',  12, 250000, N'Thạc sĩ Bác sĩ Đại học Y Dược TP.HCM, chuyên gia phục hình răng sứ thẩm mỹ và chỉnh nha chuyên sâu.'),
    (N'doc_nam',   N'Lê Hoàng Nam',      N'nam.le@medclinic.vn',       N'0911000003', N'MALE',   N'/images/doctors/doctor_nam.jpg',   N'Chuyên Khoa Da Liễu',       N'GPHN-DOC-03', N'BS.CKII', 15, 300000, N'Bác sĩ Chuyên khoa II Da liễu, nguyên Phó khoa Da liễu Bệnh viện Da liễu Trung ương.'),
    (N'doc_ha',    N'Phạm Thu Hà',       N'ha.pham@medclinic.vn',      N'0911000004', N'FEMALE', N'/images/doctors/doctor_ha.jpg',    N'Chuyên Khoa Nhi',           N'GPHN-DOC-04', N'ThS.BS',  10, 200000, N'Tốt nghiệp Thủ khoa Bác sĩ Nội trú Nhi khoa, tận tâm, giàu kinh nghiệm chăm sóc sức khỏe trẻ nhỏ.'),
    (N'doc_bao',   N'Vũ Quốc Bảo',       N'bao.vu@medclinic.vn',       N'0911000005', N'MALE',   N'/images/doctors/doctor_bao.jpg',   N'Chuyên Khoa Tai Mũi Họng',  N'GPHN-DOC-05', N'BS.CKI',   8, 200000, N'Chuyên gia nội soi tai mũi họng công nghệ cao, phẫu thuật polyp và điều trị viêm mũi xoang mạn tính.'),
    (N'doc_tuan',  N'Đặng Minh Tuấn',    N'tuan.dang@medclinic.vn',    N'0911000006', N'MALE',   N'/images/doctors/doctor_tuan.jpg',  N'Chuyên Khoa Cơ Xương Khớp', N'GPHN-DOC-06', N'TS.BS',   20, 400000, N'Tiến sĩ Y khoa, tu nghiệp tại CHLB Đức, chuyên gia đầu ngành về thoái hóa khớp và phẫu thuật nội soi khớp.'),
    (N'doc_linh',  N'Hoàng Phương Linh', N'linh.hoang@medclinic.vn',   N'0911000007', N'FEMALE', N'/images/doctors/doctor_linh.jpg',  N'Chuyên Khoa Tim Mạch',      N'GPHN-DOC-07', N'TS.BS',   14, 320000, N'Tiến sĩ Y khoa chuyên ngành Nội Tim mạch, chuyên sâu siêu âm tim Doppler và điều trị suy tim, rối loạn nhịp.'),
    (N'doc_khoa',  N'Ngô Đăng Khoa',     N'khoa.ngo@medclinic.vn',     N'0911000008', N'MALE',   N'/images/doctors/doctor_khoa.jpg',  N'Chuyên Khoa Răng Hàm Mặt',  N'GPHN-DOC-08', N'BS.CKII', 16, 300000, N'Bác sĩ Chuyên khoa II Răng Hàm Mặt, chuyên gia phẫu thuật cấy ghép Implant kỹ thuật số và tiểu phẫu răng khôn.'),
    (N'doc_lan',   N'Bùi Ngọc Lan',      N'lan.bui@medclinic.vn',      N'0911000009', N'FEMALE', N'/images/doctors/doctor_lan.jpg',   N'Chuyên Khoa Da Liễu',       N'GPHN-DOC-09', N'ThS.BS',  11, 250000, N'Thạc sĩ Bác sĩ Da liễu thẩm mỹ nội khoa, chuyên điều trị sẹo rỗ, sắc tố da bằng Laser và viêm da dị ứng.'),
    (N'doc_duc',   N'Phan Minh Đức',     N'duc.phan@medclinic.vn',     N'0911000010', N'MALE',   N'/images/doctors/doctor_duc.jpg',   N'Chuyên Khoa Nhi',           N'GPHN-DOC-10', N'BS.CKII', 17, 280000, N'Nguyên Phó trưởng khoa Hô hấp - Tiêu hóa Nhi, hơn 17 năm kinh nghiệm khám và điều trị bệnh lý hô hấp trẻ em.'),
    (N'doc_trang', N'Đỗ Huyền Trang',    N'trang.do@medclinic.vn',     N'0911000011', N'FEMALE', N'/images/doctors/doctor_trang.jpg', N'Chuyên Khoa Tai Mũi Họng',  N'GPHN-DOC-11', N'ThS.BS',   9, 220000, N'Thạc sĩ Bác sĩ chuyên khoa Tai Mũi Họng, chuyên nội soi chẩn đoán sớm bệnh lý thanh quản, viêm tai giữa và viêm xoang.'),
    (N'doc_minh',  N'Trương Quang Minh', N'minh.truong@medclinic.vn',  N'0911000012', N'MALE',   N'/images/doctors/doctor_minh.jpg',  N'Chuyên Khoa Cơ Xương Khớp', N'GPHN-DOC-12', N'BS.CKI',  13, 260000, N'Bác sĩ Chuyên khoa I Cơ Xương Khớp và Phục hồi chức năng, chuyên điều trị đau cột sống cổ - thắt lưng và chấn thương thể thao.');

-- Upsert into [users]
MERGE INTO users AS target
USING @doctors AS src
ON target.username = src.username
WHEN MATCHED THEN
    UPDATE SET
        full_name = src.full_name,
        email = src.email,
        phone = src.phone,
        gender = src.gender,
        avatar_url = src.avatar_url,
        role_id = @doctorRoleId,
        email_verified = 1,
        otp_bypass = 1,
        is_active = 1,
        updated_at = SYSDATETIME()
WHEN NOT MATCHED THEN
    INSERT (
        username, password, full_name, email, phone, gender, date_of_birth,
        address, avatar_url, role_id, email_verified, otp_bypass, is_active,
        security_version, created_at, updated_at
    )
    VALUES (
        src.username, @defaultPasswordHash, src.full_name, src.email, src.phone, src.gender, '1980-05-15',
        N'TP. Hồ Chí Minh', src.avatar_url, @doctorRoleId, 1, 1, 1,
        0, SYSDATETIME(), SYSDATETIME()
    );

-- Upsert into [doctor_profiles] linked via user_id and specialty_id
MERGE INTO doctor_profiles AS target
USING (
    SELECT
        u.id AS user_id,
        s.id AS specialty_id,
        d.license_number,
        d.title,
        d.experience_years,
        d.consultation_fee,
        d.bio
    FROM @doctors d
    JOIN users u ON u.username = d.username
    JOIN specialties s ON s.name = d.specialty_name
) AS src
ON target.user_id = src.user_id
WHEN MATCHED THEN
    UPDATE SET
        specialty_id = src.specialty_id,
        title = src.title,
        experience_years = src.experience_years,
        consultation_fee = src.consultation_fee,
        bio = src.bio
WHEN NOT MATCHED THEN
    INSERT (
        user_id, specialty_id, license_number, title,
        experience_years, consultation_fee, bio, is_available
    )
    VALUES (
        src.user_id, src.specialty_id, src.license_number, src.title,
        src.experience_years, src.consultation_fee, src.bio, 1
    );

COMMIT TRANSACTION;

SELECT
    dp.id AS doctor_id,
    u.id AS user_id,
    u.username,
    dp.title,
    u.full_name,
    u.email,
    u.phone,
    s.name AS specialty_name,
    dp.consultation_fee,
    u.avatar_url
FROM doctor_profiles dp
JOIN users u ON u.id = dp.user_id
JOIN specialties s ON s.id = dp.specialty_id
ORDER BY dp.id;
