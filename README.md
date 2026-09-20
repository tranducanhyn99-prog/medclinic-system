# 🏥 MedClinic - Hệ Thống Quản Lý & Đặt Lịch Khám Phòng Khám Tư Nhân

<div align="center">

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green.svg?style=for-the-badge&logo=springsecurity)](https://spring.io/projects/spring-security)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0E.svg?style=for-the-badge&logo=thymeleaf)](https://www.thymeleaf.org/)
[![Database](https://img.shields.io/badge/Database-SQL%20Server%20%7C%20H2-004880.svg?style=for-the-badge&logo=microsoftsqlserver)](https://www.microsoft.com/sql-server/)
[![CI](https://github.com/tranducanhyn99-prog/medclinic-system/actions/workflows/ci.yml/badge.svg)](https://github.com/tranducanhyn99-prog/medclinic-system/actions/workflows/ci.yml)
[![Line Coverage](https://img.shields.io/badge/Line%20Coverage-76.08%25-brightgreen.svg?style=for-the-badge)](https://github.com/tranducanhyn99-prog/medclinic-system/actions/workflows/ci.yml)
[![Branch Coverage](https://img.shields.io/badge/Branch%20Coverage-61.84%25-brightgreen.svg?style=for-the-badge)](https://github.com/tranducanhyn99-prog/medclinic-system/actions/workflows/ci.yml)
[![Tests](https://img.shields.io/badge/Tests-117%20total%20%7C%20109%20executed-success.svg?style=for-the-badge&logo=junit5)](https://junit.org/junit5/)
[![Docker](https://img.shields.io/badge/Docker-Compose%20Ready-2496ED.svg?style=for-the-badge&logo=docker)](compose.yml)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

*Giải pháp số hóa quy trình y tế toàn diện: Đặt lịch khám trực tuyến, Bàn khám Bác sĩ điện tử, Tiếp nhận tự động & Bệnh án điện tử chống Double Booking trên nền tảng **Microsoft SQL Server**.*

[Khởi Chạy Nhanh](#-hướng-dẫn-cài-đặt--khởi-chạy) • [Cấu Hình SQL Server](#bước-2-cấu-hình-cơ-sở-dữ-liệu-microsoft-sql-server) • [Tài Liệu Kỹ Thuật](#-tài-liệu-kỹ-thuật-phần-mềm) • [Tài Khoản Demo](#-hướng-dẫn-sử-dụng--tài-khoản-mẫu)

</div>

---

## 📑 Mục Lục (Table of Contents)

1. [Giới Thiệu Dự Án (Project Description)](#-giới-thiệu-dự-án-project-description)
   - [Động lực & Vấn đề giải quyết](#động-lực--vấn-đề-giải-quyết)
   - [Điểm nổi bật & Thách thức kỹ thuật](#điểm-nổi-bật--thách-thức-kỹ-thuật)
2. [Tính Năng Nổi Bật (Key Features)](#-tính-năng-nổi-bật-key-features)
3. [Kiến Trúc & Công Nghệ (Architecture & Tech Stack)](#-kiến-trúc--công-nghệ-architecture--tech-stack)
4. [Tài Liệu Kỹ Thuật Phần Mềm (Documentation)](#-tài-liệu-kỹ-thuật-phần-mềm)
5. [Hướng Dẫn Cài Đặt & Khởi Chạy (Installation & Run)](#-hướng-dẫn-cài-đặt--khởi-chạy)
   - [Yêu cầu hệ thống](#yêu-cầu-hệ-thống-prerequisites)
   - [Cấu hình Microsoft SQL Server (Chi tiết)](#bước-2-cấu-hình-cơ-sở-dữ-liệu-microsoft-sql-server)
   - [Khởi chạy ứng dụng](#bước-3-biên-dịch-và-khởi-chạy-ứng-dụng)
6. [Hướng Dẫn Sử Dụng & Tài Khoản Mẫu (Usage & Demo Accounts)](#-hướng-dẫn-sử-dụng--tài-khoản-mẫu)
7. [Kiểm Thử Tự Động (Testing & Coverage)](#-kiểm-thử-tự-động-testing--coverage)
8. [Cấu Trúc Thư Mục (Project Structure)](#-cấu-trúc-thư-mục-project-structure)
9. [Hướng Dẫn Đóng Góp (Contributing)](#-hướng-dẫn-đóng-góp-contributing)
10. [Tác Giả & Lời Cảm Ơn (Credits & Acknowledgments)](#-tác-giả--lời-cảm-ơn-credits--acknowledgments)
11. [Giấy Phép (License)](#-giấy-phép-license)

---

## 📖 Giới Thiệu Dự Án (Project Description)

### Động lực & Vấn đề giải quyết
Tại các phòng khám tư nhân quy mô vừa và nhỏ ở Việt Nam, việc xếp hàng lấy số khám thủ công gây ùn tắc tại sảnh chờ, tốn thời gian cho cả người bệnh lẫn nhân viên y tế. Thêm vào đó, việc lưu trữ hồ sơ bệnh án bằng giấy dễ thất lạc, đơn thuốc viết tay khó đọc và việc thanh toán thường chậm trễ.

**MedClinic** ra đời nhằm giải quyết triệt để các bài toán trên bằng cách:
- Cung cấp cổng đặt lịch khám bệnh trực tuyến mọi lúc mọi nơi theo chuyên khoa, bác sĩ và khung giờ 30 phút.
- Tự động hóa khâu tiếp nhận tại quầy: check-in qua mã QR, cấp số thứ tự điện tử (STT) và tiếp nhận bệnh nhân vãng lai (walk-in).
- Hỗ trợ bác sĩ khám bệnh với Bàn khám điện tử, kê đơn thuốc tự động trừ tồn kho dược phẩm và truy xuất nhanh tiền sử bệnh án.
- Tự động tạo hóa đơn viện phí, hỗ trợ thanh toán tiền mặt tại quầy và xuất PDF đơn thuốc/hóa đơn chuẩn y khoa. VNPay/IPN được giữ trong roadmap tương lai.

### Điểm nổi bật & Thách thức kỹ thuật
- **Kiểm soát đồng thời chống Double Booking:** Luồng đặt lịch khóa bản ghi TimeSlot khi ghi (`findByIdForUpdate`) và vẫn duy trì `@Version` để phát hiện cập nhật cạnh tranh. Kết hợp transaction `READ_COMMITTED`, trạng thái slot và ràng buộc CSDL giúp booking thất bại an toàn khi có tranh chấp.
- **Cơ sở dữ liệu Microsoft SQL Server đạt chuẩn 3NF:** Toàn bộ 16 bảng quan hệ được thiết kế chuẩn mực, tối ưu hóa kiểu dữ liệu (`NVARCHAR`, `DECIMAL(12,2)`, `DATETIME2`, `BIT`), hỗ trợ Unicode toàn vẹn tiếng Việt có dấu.
- **Phân quyền người dùng dựa trên vai trò (RBAC):** Bảo vệ chặt chẽ các tài nguyên theo 4 vai trò: `ROLE_PATIENT`, `ROLE_DOCTOR`, `ROLE_STAFF`, `ROLE_ADMIN` thông qua Spring Security 6.
- **Tích hợp đa dịch vụ:** Tự động tạo mã QR Base64 bằng Google ZXing, xuất hóa đơn & toa thuốc chuẩn PDF bằng OpenPDF, và gửi email xác nhận bất đồng bộ `@Async`.

---

## 💼 Điểm nhấn kỹ thuật cho Portfolio / CV

| Năng lực | Minh chứng trong project |
| :--- | :--- |
| **Concurrency & Transaction** | Row lock + `@Version`, chống double-booking, cấp STT đồng thời, khóa tồn kho và rollback nghiệp vụ |
| **Realtime Messaging** | Spring WebSocket/STOMP user destination; queue event chỉ broadcast `AFTER_COMMIT`, không phát khi transaction rollback |
| **SQL Server production-readiness** | Baseline 16 bảng, `ddl-auto=validate`, Unicode `NVARCHAR`, migration/runbook và integration test có guard |
| **Security** | Spring Security RBAC 4 vai trò, CSRF, BCrypt, remember-me secret ngoài repo, password-reset token SHA-256 dùng một lần |
| **Quality Engineering** | 117 test case, 109 executed PASS, JaCoCo **76.08% line / 61.84% branch**, quality gate trên `mvn verify` |
| **CI/CD & Delivery** | GitHub Actions cho push/PR, artifact JAR + JaCoCo report, Dockerfile multi-stage và SQL Server Compose |
| **UX & Accessibility** | Responsive dashboard, keyboard/focus contract, ARIA/live-region audit và UI smoke đa kích thước |

## 🖼️ Giao diện thực tế

<p align="center">
  <img src="docs/assets/readme/home.png" alt="Trang chủ MedClinic" width="92%" />
</p>
<p align="center">
  <img src="docs/assets/readme/doctors.png" alt="Danh sách bác sĩ MedClinic" width="49%" />
  <img src="docs/assets/readme/login.png" alt="Đăng nhập MedClinic" width="49%" />
</p>

## ✨ Tính Năng Nổi Bật (Key Features)

### 🧑‍💼 Phân Hệ Bệnh Nhân (Patient Portal)
- [x] **Tra cứu & Tìm kiếm:** Xem danh sách bác sĩ chuyên khoa, học hàm/học vị, số năm kinh nghiệm và giá khám.
- [x] **Đặt lịch thông minh:** Chọn ngày, chuyên khoa, bác sĩ và khung giờ trống được tải động bằng AJAX REST API.
- [x] **Mã QR Cuộc Hẹn:** Tự động nhận mã đặt lịch (`MED-YYMMDD-XXXX`) và mã QR định dạng Base64 dùng để check-in tức thì tại quầy.
- [x] **Quản lý & Hủy lịch:** Theo dõi danh sách lịch hẹn sắp tới, hủy lịch khám trực tuyến (tự động hoàn trả slot trống).
- [x] **Sổ tay Bệnh Án Điện Tử:** Tra cứu lịch sử chẩn đoán, lời dặn bác sĩ và đơn thuốc các lần khám trước.

### 👨‍⚕️ Phân Hệ Bác Sĩ (Doctor Desk)
- [x] **Hàng Chờ Khám Realtime:** Hiển thị hàng chờ theo STT và tự cập nhật qua Spring WebSocket/STOMP khi lễ tân check-in, bác sĩ bắt đầu khám hoặc hoàn tất khám. Broadcast chỉ xảy ra sau transaction commit và gửi tới đúng user destination của bác sĩ.
- [x] **Bàn Khám Lâm Sàng:** Xem nhanh triệu chứng, tiền sử bệnh án của bệnh nhân.
- [x] **Kê Đơn Thuốc Động:** Chọn thuốc theo tên, định lượng liều dùng, tần suất và cách dùng; hệ thống tự động kiểm tra và trừ số lượng tồn kho dược phẩm.
- [x] **Lịch Công Tác:** Xem lịch trực ca sáng/chiều trong vòng 14 ngày tới.

### 🛎️ Phân Hệ Tiếp Nhận & Thu Ngân (Staff Reception & Billing)
- [x] **Tra Cứu Nhanh Tiếp Nhận:** Tìm kiếm hồ sơ bệnh nhân qua mã đặt lịch, số điện thoại hoặc họ tên.
- [x] **Check-in & Cấp STT Tự Động:** Quét mã QR/xác nhận thông tin và cấp số thứ tự khám tự động (`STT-01`, `STT-02`...).
- [x] **Đăng Ký Khám Vãng Lai (Walk-in):** Đăng ký trực tiếp tại quầy cho người bệnh chưa đặt lịch trước.
- [x] **Quản Lý Hóa Đơn & Thu Phí:** Quản lý viện phí và xác nhận thanh toán tiền mặt tại quầy. VNPay/IPN là future scope.

### ⚙️ Phân Hệ Quản Trị Hệ Thống (Admin System)
- [x] **Bảng Điều Khiển KPI:** Thống kê tổng bệnh nhân, tổng bác sĩ, ca khám trong ngày và tổng doanh thu.
- [x] **Trực Quan Hóa Dữ Liệu:** Biểu đồ Chart.js phân bổ ca khám theo chuyên khoa và tỷ lệ trạng thái lịch hẹn.
- [x] **Quản Trị Danh Mục:** CRUD Chuyên khoa, Hồ sơ bác sĩ, Danh mục & Giá thuốc, Khóa/Mở tài khoản người dùng.

---

## 🏗️ Kiến Trúc & Công Nghệ (Architecture & Tech Stack)

### Sơ đồ Kiến trúc Phân tầng (Layered Architecture)

```mermaid
graph TD
    Client["Trình duyệt Web (Desktop / Mobile)"]
    
    subgraph Presentation_Layer ["Tầng Trình Diễn (Presentation Layer)"]
        Thymeleaf["Thymeleaf Templates + Bootstrap 5.3"]
        Controllers["Spring MVC Controllers & REST APIs"]
        Realtime["Spring WebSocket / STOMP User Destination"]
    end
    
    subgraph Security_Layer ["Tầng Bảo Mật (Security Layer)"]
        Security["Spring Security 6 (RBAC Authentication & Method Security)"]
    end
    
    subgraph Service_Layer ["Tầng Nghiệp Vụ (Service Layer)"]
        Services["Services (Appointment, MedicalRecord, Invoice, Doctor, Schedule...)"]
        AsyncUtils["Async Services (Email @Async, PDF OpenPDF, QR ZXing)"]
    end
    
    subgraph Data_Layer ["Tầng Dữ Liệu (Data Layer)"]
        Repositories["Spring Data JPA Repositories"]
        Hibernate["Hibernate ORM 6.5 (Optimistic Lock @Version)"]
        MSSQL[("Microsoft SQL Server 2019/2022 (Primary)")]
        H2[("H2 In-Memory DB (Dev / Test)")]
    end

    Client --> Controllers
    Client --> Realtime
    Controllers --> Security
    Realtime --> Security
    Security --> Services
    Services --> AsyncUtils
    Services --> Repositories
    Repositories --> Hibernate
    Hibernate --> MSSQL
    Hibernate -.-> H2
```

### Công nghệ sử dụng

| Lớp kiến trúc | Công nghệ / Thư viện | Phiên bản | Mục đích sử dụng |
| :--- | :--- | :---: | :--- |
| **Backend Core** | Java JDK | 21 LTS | Ngôn ngữ hiện đại, hiệu năng cao, Pattern Matching |
| **Framework** | Spring Boot | 3.3.5 | Nền tảng microservices & web monolithic vững chắc |
| **Bảo mật** | Spring Security | 6.x | Xác thực, phân quyền 4 vai trò (RBAC), CSRF, BCrypt |
| **Cơ sở dữ liệu chính** | **Microsoft SQL Server** | **2019 / 2022** | Hệ quản trị CSDL quan hệ chính thức cho dự án |
| **Driver CSDL** | Microsoft JDBC Driver for SQL Server (`mssql-jdbc`) | 12.6+ | Kết nối hiệu năng cao, bảo mật TLS/SSL |
| **Cơ sở dữ liệu Test** | H2 Database In-Memory | 2.x | Chạy kiểm thử tự động và chạy thử nghiệm zero-config |
| **ORM / JPA** | Hibernate / Spring Data JPA | 6.5.3 | Quản lý thực thể, Optimistic Lock (`@Version`) |
| **Giao diện** | Thymeleaf + Layout Dialect | 3.x | Template Engine kết xuất server-side |
| **Realtime** | Spring WebSocket + STOMP + SockJS | Spring Boot 3.3.5 | User destination cho hàng chờ bác sĩ; phát sự kiện `AFTER_COMMIT` |
| **CSS Framework** | Bootstrap | 5.3 | Giao diện Responsive thích ứng mọi kích cỡ màn hình |
| **Biểu đồ** | Chart.js | 4.x | Biểu đồ trực quan hóa số liệu KPI tại trang Admin |
| **Sinh mã QR** | Google ZXing (Zebra Crossing) | 3.5.3 | Sinh chuỗi Base64 mã QR cuộc hẹn |
| **Xuất PDF** | LibrePDF OpenPDF | 2.0.3 | Xuất đơn thuốc & hóa đơn PDF |
| **Kiểm thử** | JUnit 5, Mockito, JaCoCo | 5.x / 0.8.12 | Unit/integration/concurrency test + quality gate |
| **CI/CD** | GitHub Actions | Windows hosted runner | `mvn verify`, artifact JAR và JaCoCo report cho push/PR |
| **Container** | Docker + Compose | SQL Server 2022 | Build image đa tầng và dựng app + database cho local/demo |

---

## 📚 Tài Liệu Kỹ Thuật Phần Mềm

Toàn bộ tài liệu chuẩn kỹ thuật phần mềm được lưu trữ đầy đủ trong thư mục [`docs/`](docs/):

1. 📄 [**01_SRS_IEEE830.md**](docs/01_SRS_IEEE830.md): Đặc tả yêu cầu phần mềm theo chuẩn quốc tế **IEEE Std 830-1998** (29 Yêu cầu chức năng + 12 Yêu cầu phi chức năng).
2. 📐 [**02_UML_DESIGN.md**](docs/02_UML_DESIGN.md): Thiết kế Sơ đồ UML 2.5 (Use Case Diagram, Activity Diagrams, Sequence Diagrams luồng Concurrency, Class Diagram).
3. 🗄️ [**03_DATABASE_DESIGN_3NF.md**](docs/03_DATABASE_DESIGN_3NF.md): Thiết kế CSDL 16 bảng & Chứng minh chuẩn hóa 3NF kèm Từ điển dữ liệu chi tiết cho SQL Server.
4. 🧭 [**04_RTM_MATRIX.md**](docs/04_RTM_MATRIX.md): Ma trận truy vết yêu cầu 2 chiều (Requirements Traceability Matrix).
5. 🏛️ [**05_SYSTEM_ARCHITECTURE.md**](docs/05_SYSTEM_ARCHITECTURE.md): Tài liệu Kiến trúc Hệ thống chi tiết (Layered Architecture, Design Patterns).
6. 🎨 [**06_UI_UX_DESIGN.md**](docs/06_UI_UX_DESIGN.md): Đặc tả giao diện, responsive và accessibility.
7. ✅ [**09_RELEASE_ACCEPTANCE.md**](docs/09_RELEASE_ACCEPTANCE.md): Kịch bản nghiệm thu E2E trên SQL Server cho toàn bộ luồng đặt lịch → check-in → khám → kê thuốc → hóa đơn → thanh toán/PDF.

---

## 🚀 Hướng Dẫn Cài Đặt & Khởi Chạy

### Yêu cầu hệ thống (Prerequisites)
- **Java Development Kit (JDK):** Phiên bản 21 trở lên ([Tải OpenJDK 21](https://adoptium.net/))
- **Microsoft SQL Server:** Phiên bản 2017, 2019 hoặc 2022 ([Tải SQL Server Express miễn phí](https://www.microsoft.com/en-us/sql-server/sql-server-downloads))
- **SQL Server Management Studio (SSMS):** Công cụ quản trị trực quan ([Tải SSMS](https://learn.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms))
- **Apache Maven:** Phiên bản 3.9+ (hoặc dùng Maven Wrapper)
- **Git:** Quản lý mã nguồn ([Tải Git](https://git-scm.com/))
- **Docker Desktop (tùy chọn):** Dùng cho cách chạy nhanh toàn bộ App + SQL Server bằng Compose

---

### Bước 1: Clone kho mã nguồn
```bash
git clone https://github.com/tranducanhyn99-prog/medclinic-system.git
cd medclinic-system
```

---

### Cách chạy nhanh bằng Docker Compose

```bash
# Linux/macOS
cp .env.example .env

# Windows PowerShell
# Copy-Item .env.example .env
```

Đổi các giá trị mẫu trong `.env`, đặc biệt `MSSQL_SA_PASSWORD` và `REMEMBER_ME_KEY`, sau đó:

```bash
docker compose up --build
```

Compose sẽ khởi động SQL Server 2022, đợi database healthy, tạo `medclinic_db`, áp dụng baseline 16 bảng + reference roles nếu database còn sạch, rồi mới khởi động ứng dụng tại **http://localhost:8080**.

> Cấu hình Compose dùng tài khoản `sa` cho **local/demo để giảm bước setup**. Production vẫn phải dùng application login tối thiểu quyền theo `docs/database/SQLSERVER_RUNBOOK.md`.

Dừng stack bằng `docker compose down`; muốn xóa cả dữ liệu demo thì dùng `docker compose down -v`.

---

### Bước 2: Cấu hình cơ sở dữ liệu Microsoft SQL Server

#### 2.1. Tạo Cơ sở dữ liệu trong SQL Server
Mở SSMS (hoặc công cụ dòng lệnh `sqlcmd`) và chạy lệnh SQL sau:

```sql
-- Tạo Database cho MedClinic với Collation hỗ trợ tiếng Việt
CREATE DATABASE medclinic_db;
GO

USE medclinic_db;
GO
```

#### 2.2. Kiểm tra bật giao thức TCP/IP trên SQL Server (Lưu ý quan trọng!)
Để Spring Boot kết nối được qua cổng mạng `1433`:
1. Mở công cụ **SQL Server Configuration Manager** trên Windows.
2. Tìm đến mục **SQL Server Network Configuration** $\rightarrow$ **Protocols for MSSQLSERVER** (hoặc `Protocols for SQLEXPRESS`).
3. Đảm bảo giao thức **TCP/IP** đang ở trạng thái **Enabled**.
4. Nhấp đúp vào **TCP/IP**, chuyển sang thẻ **IP Addresses**, cuộn xuống mục **IPAll** và điền **TCP Port: `1433`** (xóa mục TCP Dynamic Ports nếu có).
5. Khởi động lại dịch vụ **SQL Server** trong thẻ *SQL Server Services*.

#### 2.3. Áp dụng schema/reference data và cấu hình secret qua môi trường
Production dùng `ddl-auto=validate`, vì vậy Hibernate **không tự tạo hoặc sửa schema**. Sau khi tạo database sạch, DBA áp dụng baseline và 4 role hệ thống:

```powershell
sqlcmd -S localhost -E -C -d medclinic_db -b -i docs\database\SQLSERVER_BASELINE_SCHEMA_2026-09-19.sql
sqlcmd -S localhost -E -C -d medclinic_db -b -i docs\database\SQLSERVER_REFERENCE_DATA_2026-09-19.sql
```

Không ghi username/password production vào repository. Cung cấp cấu hình qua biến môi trường hoặc secret manager:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=medclinic_db;encrypt=false;trustServerCertificate=true;sendStringParametersAsUnicode=true"
$env:DB_USERNAME="<application-login>"
$env:DB_PASSWORD="<secret>"
$env:REMEMBER_ME_KEY="<random-secret>"
$env:MAIL_USERNAME="<smtp-user>"
$env:MAIL_PASSWORD="<smtp-secret>"
```

Chi tiết triển khai xem `docs/database/SQLSERVER_RUNBOOK.md` và `docs/database/PRODUCTION_ADMIN_PROVISIONING.md`.

> 💡 **Mẹo chạy nhanh không cần cài SQL Server (Dev In-Memory):** Nếu chưa cài đặt SQL Server trên máy, bạn vẫn có thể khởi chạy ngay lập tức bằng CSDL nhúng **H2 Database** tích hợp sẵn (Profile `dev`), toàn bộ dữ liệu mẫu vẫn tự động nạp bình thường.

---

### Bước 3: Biên dịch và Khởi chạy ứng dụng

#### Chạy với Microsoft SQL Server:
```bash
# Biên dịch dự án
mvn clean compile

# Chạy ứng dụng kích hoạt Profile SQL Server
mvn spring-boot:run -Dspring-boot.run.profiles=sqlserver
```

Khuyến nghị chọn profile bằng tham số/biến môi trường khi deploy; không sửa `application.yml` chỉ để đổi môi trường chạy.

#### Chạy với H2 In-Memory (Zero-config):
```bash
mvn spring-boot:run
```

Sau khi màn hình Console thông báo `Started MedClinicApplication...`, mở trình duyệt truy cập:
👉 **`http://localhost:8080`**

Console H2 Database (nếu chạy profile dev H2):
👉 **`http://localhost:8080/h2-console`** *(JDBC URL: `jdbc:h2:mem:medclinic_db`, User: `sa`, Password: để trống)*

---

## 🔑 Hướng Dẫn Sử Dụng & Tài Khoản Mẫu (chỉ profile `dev`)

Khi khởi chạy với profile `dev`, hệ thống sẽ tự động nạp (seed) dữ liệu mẫu bao gồm:
- **6 Bác sĩ chuyên khoa hàng đầu** (Tim mạch, Răng hàm mặt, Da liễu, Nhi, Tai mũi họng, Cơ xương khớp).
- **14 ngày ca trực tự động** (Ca sáng 08:00-12:00, Ca chiều 13:30-17:30, mỗi ca 8 khung giờ 30 phút).
- **15 mặt hàng dược phẩm thiết yếu** có sẵn số lượng tồn kho và đơn giá niêm yết.

Bảng tài khoản mẫu cho 4 vai trò:

| Vai trò (Role) | Tên đăng nhập | Mật khẩu | Quyền hạn & Chức năng kiểm thử |
| :--- | :--- | :---: | :--- |
| 👑 **Quản Trị Viên (Admin)** | `admin` | `123456` | Xem Dashboard KPI, biểu đồ Chart.js, quản lý chuyên khoa, bác sĩ, thuốc, tài khoản |
| 👨‍⚕️ **Bác Sĩ (Doctor)** | `doctor` | `123456` | Bàn khám, gọi số STT, chẩn đoán & kê đơn thuốc, xem lịch trực |
| 🛎️ **Lễ Tân (Staff)** | `staff` | `123456` | Tra cứu bệnh nhân, check-in cấp STT, tiếp nhận vãng lai, thu viện phí |
| 🧑‍💼 **Bệnh Nhân (Patient)** | `patient` | `123456` | Đặt lịch khám, nhận mã QR, xem chi tiết lịch hẹn, xem lịch sử bệnh án |

> Production hiện tại dùng `sqlserver` và không seed các tài khoản này. Profile `mysql` được giữ cho roadmap tương lai. Xem [quy trình provisioning admin](docs/database/PRODUCTION_ADMIN_PROVISIONING.md) trước khi triển khai.

> 💡 **Khách vãng lai (Guest):** Có thể tự đăng ký tài khoản bệnh nhân mới tại trang `/register`.

### Quy trình nghiệp vụ mẫu (End-to-End Workflow)
```
1. [Bệnh nhân] Đăng nhập -> Vào "Đặt lịch khám" -> Chọn BS & Ngày -> Chọn Slot -> Xác nhận
   └──> Nhận mã cuộc hẹn MED-xxxx và mã QR Base64.
2. [Lễ tân] Vào "Tiếp nhận" -> Tìm mã đặt lịch -> Bấm "Check-in"
   └──> Hệ thống cấp số thứ tự STT-01, chuyển trạng thái sang "Chờ khám".
3. [Bác sĩ] Đăng nhập -> Vào "Bàn khám" -> Chọn bệnh nhân STT-01 -> Điền chẩn đoán & Toa thuốc -> "Lưu kết quả"
   └──> Hệ thống tự động trừ kho thuốc trong SQL Server và sinh hóa đơn viện phí.
4. [Lễ tân] Vào "Thu viện phí" -> Xem hóa đơn -> Bấm "Xác nhận thanh toán" -> In hóa đơn PDF.
```

---

## 🧪 Kiểm Thử Tự Động (Testing & Coverage)

Dự án được tích hợp bộ kiểm thử tự động với JUnit 5, Mockito và JaCoCo:

```bash
# Chạy toàn bộ verification lifecycle
mvn verify
```

Baseline kiểm chứng ngày 20/09/2026: **117 test case / 50 test class; 109 executed PASS, 8 environment-gated skipped, 0 failure, 0 error**. JaCoCo đạt **76.08% line / 61.84% branch**; build sẽ fail nếu xuống dưới **70% line / 50% branch**. Các SQL Server integration test chỉ chạy khi chủ động bật environment guard.

### CI và quality gate
- GitHub Actions chạy `mvn verify` trên **JDK 21 / Windows hosted runner** cho mọi push/PR vào `main`.
- JaCoCo kiểm tra ngưỡng **LINE ≥ 70%** và **BRANCH ≥ 50%** ngay trong phase `verify`.
- Workflow luôn lưu **Surefire test report** để chẩn đoán khi fail; khi build thành công sẽ lưu **JaCoCo HTML report** và **application JAR** dưới dạng artifacts.

### Các kịch bản kiểm thử trọng yếu:
- ✅ **Khởi tạo Context & Seed Data:** Đảm bảo toàn bộ Bean, Hibernate Entity, và dữ liệu mẫu nạp thành công (`MedClinicApplicationTests`).
- ✅ **Đăng ký tài khoản:** Kiểm thử mật khẩu mã hóa BCrypt, kiểm tra tính hợp lệ dữ liệu và ngăn chặn trùng lặp Username/Email (`UserServiceTest`).
- ✅ **Cơ chế chống Double Booking:** Kiểm thử cô lập xử lý đồng thời, kích hoạt `OptimisticLockingFailureException` khi xảy ra tranh chấp khung giờ (`AppointmentServiceTest`).
- ✅ **Cấp số thứ tự STT:** Kiểm thử tự động tính toán STT tăng dần cho hàng chờ khám bệnh trong ngày (`AppointmentServiceTest`).
- ✅ **Hủy lịch & Hoàn slot:** Đảm bảo slot được trả về trạng thái `AVAILABLE` ngay sau khi hủy (`AppointmentServiceTest`).

---

## 📁 Cấu Trúc Thư Mục (Project Structure)

```
medclinic-system/
├── .github/workflows/ci.yml              # CI: JDK 21 + mvn verify + JaCoCo + artifacts
├── docker/sqlserver/init-db.sh            # Khởi tạo SQL Server demo có kiểm tra schema
├── docs/                                 # Tài liệu chuẩn kỹ thuật + ảnh portfolio
├── Dockerfile                            # Multi-stage build, runtime non-root
├── compose.yml                           # App + SQL Server 2022 + init job
├── .env.example                          # Mẫu biến môi trường local/demo, không chứa secret thật
├── src/
│   ├── main/
│   │   ├── java/com/medclinic/
│   │   │   ├── config/                   # SecurityConfig, DataInitializer
│   │   │   ├── controller/               # AdminController, DoctorDeskController, StaffReceptionController...
│   │   │   ├── dto/                      # Data Transfer Objects (Form, Slot, Prescription)
│   │   │   ├── entity/                   # 16 JPA Entities chuẩn 3NF (tương thích SQL Server)
│   │   │   ├── enums/                    # 7 Enums (Status, Role, Shift, Payment...)
│   │   │   ├── exception/                # Bộ xử lý ngoại lệ nghiệp vụ tập trung
│   │   │   ├── repository/               # 15 Spring Data JPA Repositories
│   │   │   ├── security/                 # CustomUserDetails, CustomUserDetailsService
│   │   │   ├── service/                  # 12 service contract + các implementation/coordinator hỗ trợ
│   │   │   └── util/                     # Tiện ích QrCodeUtil (ZXing)
│   │   └── resources/
│   │       ├── templates/                # Thymeleaf HTML Templates
│   │       ├── static/                   # File tĩnh (CSS, JS, Images)
│   │       └── application.yml           # Profiles dev H2 / sqlserver / mysql-roadmap; nhận secret qua env
│   └── test/java/com/medclinic/          # Bộ bài kiểm thử tự động JUnit 5 & Mockito
├── pom.xml                               # Quản lý thư viện Maven & mssql-jdbc Driver
└── README.md                             # Tài liệu giới thiệu & hướng dẫn dự án (Chuẩn freeCodeCamp)
```

---

## 🤝 Hướng Dẫn Đóng Góp (Contributing)

Mọi đóng góp nhằm cải tiến dự án đều được hoan nghênh. Vui lòng tuân thủ các bước sau:

1. **Fork** repository này về tài khoản GitHub của bạn.
2. Tạo một nhánh tính năng mới:
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. Commit các thay đổi với thông điệp rõ ràng theo chuẩn Conventional Commits:
   ```bash
   git commit -m "feat: Add SMS notification service for booking confirmation"
   ```
4. Đẩy mã nguồn lên nhánh của bạn:
   ```bash
   git push origin feature/AmazingFeature
   ```
5. Mở một **Pull Request** trên GitHub mô tả chi tiết các thay đổi của bạn.

---

## 👥 Tác Giả & Lời Cảm Ơn (Credits & Acknowledgments)

- **Đồ án môn học:** Lập Trình Web (LTW) & Công Nghệ Phần Mềm (CNPM).
- **Hệ quản trị Cơ sở dữ liệu:** [Microsoft SQL Server](https://www.microsoft.com/sql-server/) & Driver `mssql-jdbc`.
- **Tài liệu tham khảo & Hướng dẫn:**
  - Tiêu chuẩn đặc tả yêu cầu phần mềm [IEEE Std 830-1998](https://standards.ieee.org/).
  - Thư viện mã nguồn mở: [Spring Boot](https://spring.io/), [OpenPDF](https://github.com/LibrePDF/OpenPDF), [ZXing](https://github.com/zxing/zxing), [Bootstrap](https://getbootstrap.com/).

---

## 📄 Giấy Phép (License)

Dự án được phân phối dưới giấy phép **MIT License**. Bạn được toàn quyền sử dụng, sửa đổi và phân phối cho mục đích học tập và nghiên cứu. Xem chi tiết tại file [LICENSE](LICENSE) (nếu có).

---

<div align="center">
  <sub>Được xây dựng với sự tận tâm và tình yêu công nghệ ❤️ • © 2026 MedClinic Project</sub>
</div>
