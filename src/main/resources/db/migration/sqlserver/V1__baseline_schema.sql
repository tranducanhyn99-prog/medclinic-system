-- MedClinic SQL Server baseline schema
-- Generated/reviewed from current JPA entities on 2026-09-19.
-- Target: Microsoft SQL Server (current production database).
-- Strings use NVARCHAR via hibernate.use_nationalized_character_data=true.
-- Apply only to a clean database, then run the documented post-checks.
-- Application production profile uses ddl-auto=validate, not update.


SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;

    create table appointments (
        appointment_date date not null,
        created_at datetime2(6) not null,
        doctor_id bigint not null,
        id bigint identity not null,
        patient_id bigint not null,
        queue_sequence bigint,
        time_slot_id bigint not null,
        updated_at datetime2(6),
        version bigint,
        queue_number nvarchar(20),
        booking_code nvarchar(30) not null,
        status nvarchar(30) not null check (status in ('CONFIRMED','CHECKED_IN','IN_PROGRESS','COMPLETED','CANCELLED','NO_SHOW')),
        reason nvarchar(max),
        primary key (id)
    );

    create table daily_queue_counters (
        queue_date date not null,
        id bigint identity not null,
        next_sequence bigint not null,
        version bigint,
        primary key (id)
    );

    create table doctor_profiles (
        consultation_fee numeric(12,2) not null,
        experience_years int not null,
        id bigint identity not null,
        specialty_id bigint not null,
        user_id bigint not null,
        license_number nvarchar(50),
        title nvarchar(50) not null,
        bio nvarchar(max),
        primary key (id)
    );

    create table doctor_schedules (
        max_patients int not null,
        work_date date not null,
        doctor_id bigint not null,
        id bigint identity not null,
        shift nvarchar(20) not null check (shift in ('MORNING','AFTERNOON')),
        primary key (id)
    );

    create table invoice_code_reservations (
        invoice_code nvarchar(30) not null,
        primary key (invoice_code)
    );

    create table invoices (
        consultation_fee numeric(12,2) not null,
        medicine_fee numeric(12,2) not null,
        total_amount numeric(12,2) not null,
        appointment_id bigint not null,
        created_at datetime2(6) not null,
        id bigint identity not null,
        paid_at datetime2(6),
        payment_method nvarchar(20) not null check (payment_method in ('CASH','VNPAY')),
        payment_status nvarchar(20) not null check (payment_status in ('UNPAID','PAID','REFUNDED')),
        invoice_code nvarchar(30) not null,
        primary key (id)
    );

    create table medical_records (
        re_exam_date date,
        appointment_id bigint not null,
        created_at datetime2(6) not null,
        doctor_id bigint not null,
        id bigint identity not null,
        patient_id bigint not null,
        diagnosis nvarchar(max) not null,
        doctor_notes nvarchar(max),
        symptoms nvarchar(max) not null,
        primary key (id)
    );

    create table medicines (
        is_active bit not null,
        stock_quantity int not null,
        unit_price numeric(12,2) not null,
        id bigint identity not null,
        version bigint,
        unit nvarchar(30) not null,
        name nvarchar(150) not null,
        usage_guide nvarchar(255),
        primary key (id)
    );

    create table notifications (
        is_read bit not null,
        created_at datetime2(6) not null,
        id bigint identity not null,
        user_id bigint not null,
        type nvarchar(50),
        title nvarchar(150) not null,
        content nvarchar(max) not null,
        primary key (id)
    );

    create table password_reset_tokens (
        created_at datetime2(6) not null,
        expires_at datetime2(6) not null,
        id bigint identity not null,
        used_at datetime2(6),
        user_id bigint not null,
        token_hash nvarchar(64) not null,
        primary key (id)
    );

    create table prescription_items (
        quantity int not null,
        unit_price_at_time numeric(12,2) not null,
        id bigint identity not null,
        medicine_id bigint not null,
        prescription_id bigint not null,
        dosage nvarchar(100) not null,
        frequency nvarchar(100) not null,
        instructions nvarchar(255),
        primary key (id)
    );

    create table prescriptions (
        created_at datetime2(6) not null,
        id bigint identity not null,
        medical_record_id bigint not null,
        doctor_notes nvarchar(max),
        primary key (id)
    );

    create table roles (
        id bigint identity not null,
        name nvarchar(50) not null check (name in ('ROLE_PATIENT','ROLE_DOCTOR','ROLE_STAFF','ROLE_ADMIN')),
        description nvarchar(255),
        primary key (id)
    );

    create table specialties (
        is_active bit not null,
        id bigint identity not null,
        icon nvarchar(100),
        name nvarchar(100) not null,
        description nvarchar(max),
        primary key (id)
    );

    create table time_slots (
        end_time time not null,
        start_time time not null,
        id bigint identity not null,
        schedule_id bigint not null,
        version bigint not null,
        status nvarchar(20) not null check (status in ('AVAILABLE','BOOKED','BLOCKED')),
        primary key (id)
    );

    create table users (
        date_of_birth date,
        is_active bit not null,
        created_at datetime2(6) not null,
        id bigint identity not null,
        role_id bigint not null,
        updated_at datetime2(6),
        gender nvarchar(10) check (gender in ('MALE','FEMALE','OTHER')),
        phone nvarchar(20),
        username nvarchar(50) not null,
        email nvarchar(100) not null,
        full_name nvarchar(100) not null,
        address nvarchar(255),
        avatar_url nvarchar(255),
        password nvarchar(255) not null,
        primary key (id)
    );

    create index idx_booking_code
       on appointments (booking_code);

    create index idx_appointment_date
       on appointments (appointment_date);

    create unique nonclustered index uk_appointment_date_queue_sequence
       on appointments (appointment_date, queue_sequence) where appointment_date is not null and queue_sequence is not null;

    alter table appointments
       add constraint UK3q9npuf7n7qq4dy4gokw9kmri unique (booking_code);

    alter table daily_queue_counters
       add constraint uk_daily_queue_counter_date unique (queue_date);

    alter table doctor_profiles
       add constraint UKf2ac4saatw7tnup2kqa53oqkl unique (user_id);

    alter table doctor_schedules
       add constraint UKr4rirmjcwsw1bx5t0bl15ag1w unique (doctor_id, work_date, shift);

    create index idx_invoice_code
       on invoices (invoice_code);

    alter table invoices
       add constraint UKr1gsksmeq3yb5fipnxl93yqqv unique (appointment_id);

    alter table invoices
       add constraint UK9yfpp5o7v861ie3702mnhm3hv unique (invoice_code);

    alter table medical_records
       add constraint UK2nyonrbplqq716buy7u4ghmt8 unique (appointment_id);

    alter table medicines
       add constraint UKb6dv5kee0aoi485ijmu3n63ic unique (name);

    create index idx_password_reset_token_hash
       on password_reset_tokens (token_hash);

    alter table password_reset_tokens
       add constraint UKajre85ybxavf1tt4omkrs5p6g unique (token_hash);

    alter table prescriptions
       add constraint UK3ufvab73b86ot4b8q31ygd7uy unique (medical_record_id);

    alter table roles
       add constraint UKofx66keruapi6vyqpv6f2or37 unique (name);

    alter table specialties
       add constraint UKbhb8s9o5hv30lkbidtod9cixc unique (name);

    alter table users
       add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);

    alter table users
       add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);

    alter table appointments
       add constraint FKo690gyordobh8dp3ea367v871
       foreign key (doctor_id)
       references doctor_profiles;

    alter table appointments
       add constraint FKopb2h9yhin1rb4dqote8bws6w
       foreign key (patient_id)
       references users;

    alter table appointments
       add constraint FKlt1cehvx9m01l4s0ibvx60xv1
       foreign key (time_slot_id)
       references time_slots;

    alter table doctor_profiles
       add constraint FK9j8uwaq3c6a5j4d67g91uadcx
       foreign key (specialty_id)
       references specialties;

    alter table doctor_profiles
       add constraint FKhrpk2q09sjwf9en18301dioyr
       foreign key (user_id)
       references users;

    alter table doctor_schedules
       add constraint FKhd5567m3o90306cy8mv9sge8s
       foreign key (doctor_id)
       references doctor_profiles;

    alter table invoices
       add constraint FKngg5bc8atao2b9jehl9l8tdsw
       foreign key (appointment_id)
       references appointments;

    alter table medical_records
       add constraint FKifeec8p5v06rt258odelw8s7j
       foreign key (appointment_id)
       references appointments;

    alter table medical_records
       add constraint FKaf77sv16u6eefi6txeo9so8iw
       foreign key (doctor_id)
       references doctor_profiles;

    alter table medical_records
       add constraint FKe3g9v0pbec2843wd1rxeb0is3
       foreign key (patient_id)
       references users;

    alter table notifications
       add constraint FK9y21adhxn0ayjhfocscqox7bh
       foreign key (user_id)
       references users;

    alter table password_reset_tokens
       add constraint FKk3ndxg5xp6v7wd4gjyusp15gq
       foreign key (user_id)
       references users;

    alter table prescription_items
       add constraint FKacx7kmy9ksr443xpn5dccidro
       foreign key (medicine_id)
       references medicines;

    alter table prescription_items
       add constraint FK6uh7tdy2lv6sx34u1365acqsf
       foreign key (prescription_id)
       references prescriptions;

    alter table prescriptions
       add constraint FKpchnmvlttcajjovopclelmdjh
       foreign key (medical_record_id)
       references medical_records;

    alter table time_slots
       add constraint FKlmw5wd5jfucimijgnldief5cu
       foreign key (schedule_id)
       references doctor_schedules;

    alter table users
       add constraint FKp56c1712k691lhsyewcssf40f
       foreign key (role_id)
       references roles;
