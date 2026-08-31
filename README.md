# HỆ THỐNG QUẢN LÝ VÀ ĐẶT LỊCH KHÁM PHÒNG KHÁM TƯ NHÂN (MEDCLINIC)
**Đồ án:** Lập Trình Web (LTW) & Công Nghệ Phần Mềm (CNPM)  
**Giai đoạn:** WEEK 1 - Phân tích yêu cầu, Thiết kế UML, CSDL 3NF & Khởi tạo nền tảng Spring Boot.

---

## 📌 1. BỘ TÀI LIỆU CÔNG NGHỆ PHẦN MỀM (WEEK 1 DOCS)

Toàn bộ các tài liệu phân tích và thiết kế theo chuẩn kỹ thuật phần mềm nằm trong thư mục `docs/`:

1. [docs/01_SRS_IEEE830.md](file:///docs/01_SRS_IEEE830.md) - **Đặc tả yêu cầu phần mềm (SRS)** đầy đủ theo tiêu chuẩn quốc tế **IEEE Std 830-1998** với 20+ Yêu cầu chức năng và Yêu cầu phi chức năng.
2. [docs/02_UML_DESIGN.md](file:///docs/02_UML_DESIGN.md) - **Thiết kế Sơ đồ UML 2.5** bao gồm: Use Case Diagram, Đặc tả Use Case chi tiết, Activity Diagrams, Sequence Diagrams (luồng Đặt lịch Concurrency Lock & Khám bệnh), Class Diagram.
3. [docs/03_DATABASE_DESIGN_3NF.md](file:///docs/03_DATABASE_DESIGN_3NF.md) - **Thiết kế CSDL & Chứng minh chuẩn hóa 3NF** kèm Từ điển dữ liệu chi tiết cho 13 bảng.
4. [docs/04_RTM_MATRIX.md](file:///docs/04_RTM_MATRIX.md) - **Ma trận truy vết yêu cầu (Requirement Traceability Matrix)** ánh xạ 2 chiều giữa SRS, Use Case, Database, Class và Test Cases.
5. [docs/05_SYSTEM_ARCHITECTURE.md](file:///docs/05_SYSTEM_ARCHITECTURE.md) - **Tài liệu Kiến trúc Hệ thống** (Layered Architecture, Optimistic Concurrency Locking `@Version`, RBAC Security, Async Processing).

---

## 🛠️ 2. TECH STACK & NỀN TẢNG KỸ THUẬT

- **Ngôn ngữ:** Java 21 LTS
- **Framework:** Spring Boot 3.3.5 (Spring Web, Spring Security 6, Spring Data JPA, Thymeleaf, Spring Mail, Spring WebSocket)
- **Giao diện:** Thymeleaf Layout Dialect + Bootstrap 5.3 + Bootstrap Icons + SweetAlert2
- **Cơ sở dữ liệu:** H2 In-Memory (Zero-config Dev), MySQL 8.0, SQL Server
- **Kiểm thử:** JUnit 5, Mockito, JaCoCo Code Coverage plugin
- **Xây dựng:** Maven 3.9+

---

## 🚀 3. HƯỚNG DẪN KHỞI CHẠY (QUICK START)

### 3.1. Chạy ứng dụng Spring Boot:
```bash
mvn spring-boot:run
```
Sau đó truy cập: `http://localhost:8080`

### 3.2. Chạy kiểm thử tự động:
```bash
mvn clean test
```

---

## 🐙 4. HƯỚNG DẪN ĐẨY LÊN GITHUB (GIT COMMANDS)

Để đưa mã nguồn của bạn lên GitHub cá nhân:

```bash
# 1. Tạo một repository mới trên GitHub (ví dụ: medclinic-system)
# 2. Chạy các lệnh sau trong thư mục dự án:
git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPOSITORY>.git
git branch -M main
git push -u origin main
```
