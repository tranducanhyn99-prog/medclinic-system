# TÀI LIỆU KIẾN TRÚC HỆ THỐNG (SYSTEM ARCHITECTURE DOCUMENT)
**Dự án:** MedClinic - Phòng Khám Tư Nhân  
**Phiên bản:** 1.0  
**Học phần:** Lập trình Web & Công nghệ Phần mềm  

---

## 1. TỔNG QUAN KIẾN TRÚC (ARCHITECTURAL OVERVIEW)

Hệ thống MedClinic được xây dựng theo mô hình **Kiến trúc Phân tầng (Layered Architecture)** kết hợp mẫu thiết kế **Model-View-Controller (MVC)** nhằm đảm bảo tính phân tách trách nhiệm (Separation of Concerns), dễ bảo trì, dễ mở rộng và kiểm thử độc lập.

```mermaid
graph TD
    subgraph Client ["Tầng Khách (Client Layer)"]
        Browser[Trình duyệt Web Desktop / Mobile]
    end

    subgraph Security ["Tầng Bảo Mật (Security Layer)"]
        Filter[Spring Security 6 FilterChain]
        CSRF[CSRF Protection & Remember-Me]
        RBAC[Role-Based Access Control]
    end

    subgraph Web ["Tầng Trình Diễn (Presentation Layer)"]
        Ctrl[Spring MVC Controllers]
        RestCtrl[REST API Controllers]
        Views[Thymeleaf Templates + Bootstrap 5.3]
    end

    subgraph Service ["Tầng Nghiệp Vụ (Business Logic Layer)"]
        AppSvc[Appointment Booking Engine]
        LockMgr[Concurrency Lock Manager - @Version]
        MedSvc[Medical Record & Prescription Service]
        InvSvc[Billing & Invoice Service]
        MailSvc[Async Email Service - @Async]
        PdfSvc[OpenPDF Engine & ZXing QR]
    end

    subgraph Data ["Tầng Truy Cập Dữ Liệu (Data Access Layer)"]
        JPA[Spring Data JPA Repositories]
        Hibernate[Hibernate ORM Engine]
    end

    subgraph Storage ["Tầng Lưu Trữ (Database Layer)"]
        DB[(H2 Database / MySQL 8.0 / SQL Server)]
    end

    Browser --> Filter
    Filter --> CSRF
    CSRF --> RBAC
    RBAC --> Ctrl
    RBAC --> RestCtrl
    Ctrl --> Views
    Ctrl --> Service
    RestCtrl --> Service
    Service --> JPA
    JPA --> Hibernate
    Hibernate --> DB
```

---

## 2. CHI TIẾT CÁC TẦNG KIẾN TRÚC

### 2.1. Presentation Layer (Tầng Trình diễn)
- **Công nghệ:** Thymeleaf 3 + Thymeleaf Layout Dialect + Bootstrap 5.3 + Bootstrap Icons + Chart.js + SweetAlert2.
- **Vai trò:** Render giao diện phía máy chủ (Server-Side Rendering - SSR), đảm bảo thời gian tải trang nhanh, thân thiện với SEO và tương thích đa thiết bị.
- **Cơ chế Layout:** Sử dụng `layout:decorate="~{layout/main}"` cho trang công khai/bệnh nhân và `layout:decorate="~{layout/dashboard}"` cho khu vực quản trị/bác sĩ/lễ tân.

### 2.2. Security Layer (Tầng Bảo mật & Phân quyền)
- **Công nghệ:** Spring Security 6 với cấu hình `SecurityFilterChain`.
- **Cơ chế xác thực:** Session-based authentication kết hợp Remember-me token 14 ngày.
- **Phân quyền RBAC:**
  - `ROLE_PATIENT`: Truy cập `/patient/**`
  - `ROLE_DOCTOR`: Truy cập `/doctor/**`
  - `ROLE_STAFF`: Truy cập `/staff/**`
  - `ROLE_ADMIN`: Truy cập `/admin/**`
- **Mã hóa:** Giải thuật băm mật khẩu `BCryptPasswordEncoder` với salt ngẫu nhiên.

### 2.3. Business Service Layer & Concurrency Engine (Tầng Nghiệp vụ)
- **Concurrency Locking (Xử lý đồng thời chống Double Booking):**
  - Sử dụng **Optimistic Locking** thông qua trường `@Version private Long version;` trên Entity `TimeSlot`.
  - Kết hợp mức cô lập giao dịch `@Transactional(isolation = Isolation.READ_COMMITTED)`.
  - Khi 2 request cùng cố gắng đặt 1 slot, giao dịch đầu tiên cập nhật thành công (tăng version từ $0 \rightarrow 1$), giao dịch thứ hai phát hiện xung đột version và ném `OptimisticLockingFailureException`. Hệ thống bắt ngoại lệ và thông báo thân thiện tới người dùng.
- **Xử lý Bất đồng bộ (Async Processing):**
  - Tích hợp `@EnableAsync` với cấu hình `ThreadPoolTaskExecutor` (Core Pool: 4, Max Pool: 10, Queue: 100).
  - Tác vụ gửi email (`EmailService`) được tách khỏi luồng request chính, loại bỏ độ trễ phản hồi mạng.
- **Động cơ Xuất PDF & QR Code:**
  - `OpenPDF`: Sinh tài liệu PDF đơn thuốc và hóa đơn bằng luồng `ByteArrayOutputStream`, truyền trực tiếp qua `InputStreamResource` không ghi rác ra ổ đĩa máy chủ.
  - `ZXing`: Sinh mã QR định danh Base64 nhúng thẳng vào HTML hoặc gửi qua email.

### 2.4. Data Access Layer (Tầng Dữ liệu)
- **Công nghệ:** Spring Data JPA + Hibernate.
- **Cơ sở dữ liệu:** Cấu hình linh hoạt đa môi trường:
  - Profile `dev`: H2 In-Memory Database (tự động khởi tạo schema và seed data, zero-configuration).
  - Profile `mysql`: MySQL 8.0 cho môi trường sản xuất / Docker.
  - Profile `sqlserver`: Microsoft SQL Server.
- **Chuẩn hóa CSDL:** Toàn bộ quan hệ được thiết kế chuẩn mực **3NF**.
