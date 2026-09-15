package com.medclinic.generator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.zip.*;

public class DocxAndXmiGenerator {

    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU TẠO FILE DOCX VÀ FILE XML ENTERPRISE ARCHITECT ===");
        try {
            // 1. Tạo file XML XMI 1.1 chuẩn Enterprise Architect (Default & High Compatibility)
            String xmi11Content = buildEnterpriseArchitectXmi11();
            Path ea11Prj = Paths.get("c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs/MedClinic_EnterpriseArchitect_Model.xml");
            Path ea11Ltw = Paths.get("c:/Users/DUCANHZZ/Downloads/LTW/MedClinic_EnterpriseArchitect_Model.xml");
            Files.createDirectories(ea11Prj.getParent());
            Files.writeString(ea11Prj, xmi11Content, StandardCharsets.UTF_8);
            Files.writeString(ea11Ltw, xmi11Content, StandardCharsets.UTF_8);
            System.out.println("-> Đã tạo file XML XMI 1.1 (Khuyến nghị cho EA 15):");
            System.out.println("   " + ea11Prj.toAbsolutePath());
            System.out.println("   " + ea11Ltw.toAbsolutePath());

            // 1b. Tạo thêm file XML XMI 2.1 chuẩn đầy đủ cho EA 15
            String xmi21Content = buildEnterpriseArchitectXmi21();
            Path ea21Prj = Paths.get("c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs/MedClinic_EnterpriseArchitect_Model_XMI21.xml");
            Path ea21Ltw = Paths.get("c:/Users/DUCANHZZ/Downloads/LTW/MedClinic_EnterpriseArchitect_Model_XMI21.xml");
            Files.writeString(ea21Prj, xmi21Content, StandardCharsets.UTF_8);
            Files.writeString(ea21Ltw, xmi21Content, StandardCharsets.UTF_8);
            System.out.println("-> Đã tạo thêm file XML XMI 2.1:");
            System.out.println("   " + ea21Prj.toAbsolutePath());
            System.out.println("   " + ea21Ltw.toAbsolutePath());

            // 2. Tạo file DOCX
            Path docxFilePrj = Paths.get("c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs/KhaoSat_PhanTichHeThong_MedClinic.docx");
            Path docxFileLtw = Paths.get("c:/Users/DUCANHZZ/Downloads/LTW/KhaoSat_PhanTichHeThong_MedClinic.docx");
            buildDocxFile(docxFilePrj);
            Files.copy(docxFilePrj, docxFileLtw, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("-> Đã tạo thành công file Word DOCX tại:");
            System.out.println("   " + docxFilePrj.toAbsolutePath());
            System.out.println("   " + docxFileLtw.toAbsolutePath());

            System.out.println("=== HOÀN TẤT 100% ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildDocxFile(Path targetDocx) throws Exception {
        String documentXml = buildDocumentXml();
        Path inspectDir = Paths.get("C:/Users/DUCANHZZ/AppData/Local/Temp/docx_inspect");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(targetDocx))) {
            // [Content_Types].xml
            addZipEntry(zos, "[Content_Types].xml", getFileOrFallback(inspectDir.resolve("[Content_Types].xml"), getContentTypesXml()));
            // _rels/.rels
            addZipEntry(zos, "_rels/.rels", getFileOrFallback(inspectDir.resolve("_rels/.rels"), getRelsXml()));
            // docProps/app.xml
            addZipEntry(zos, "docProps/app.xml", getFileOrFallback(inspectDir.resolve("docProps/app.xml"), getAppXml()));
            // docProps/core.xml
            addZipEntry(zos, "docProps/core.xml", getFileOrFallback(inspectDir.resolve("docProps/core.xml"), getCoreXml()));
            // word/_rels/document.xml.rels
            addZipEntry(zos, "word/_rels/document.xml.rels", getFileOrFallback(inspectDir.resolve("word/_rels/document.xml.rels"), getDocRelsXml()));
            // word/styles.xml
            addZipEntry(zos, "word/styles.xml", getFileOrFallback(inspectDir.resolve("word/styles.xml"), getStylesXml()));
            // word/settings.xml
            addZipEntry(zos, "word/settings.xml", getFileOrFallback(inspectDir.resolve("word/settings.xml"), getSettingsXml()));
            // word/fontTable.xml
            addZipEntry(zos, "word/fontTable.xml", getFileOrFallback(inspectDir.resolve("word/fontTable.xml"), getFontTableXml()));
            // word/theme/theme1.xml
            if (Files.exists(inspectDir.resolve("word/theme/theme1.xml"))) {
                addZipEntry(zos, "word/theme/theme1.xml", Files.readString(inspectDir.resolve("word/theme/theme1.xml"), StandardCharsets.UTF_8));
            }
            // word/document.xml
            addZipEntry(zos, "word/document.xml", documentXml);
        }
    }

    private static String getFileOrFallback(Path path, String fallback) {
        try {
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    private static void addZipEntry(ZipOutputStream zos, String entryName, String content) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String p(String text, String style, boolean bold, int sizePt, String align) {
        StringBuilder sb = new StringBuilder("<w:p><w:pPr>");
        if (style != null) sb.append("<w:pStyle w:val=\"").append(style).append("\"/>");
        if (align != null) sb.append("<w:jc w:val=\"").append(align).append("\"/>");
        sb.append("<w:spacing w:line=\"276\" w:lineRule=\"auto\" w:after=\"120\"/></w:pPr>");
        if (text != null && !text.isEmpty()) {
            sb.append("<w:r><w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>");
            if (bold) sb.append("<w:b/>");
            if (sizePt > 0) sb.append("<w:sz w:val=\"").append(sizePt * 2).append("\"/>");
            sb.append("</w:rPr><w:t xml:space=\"preserve\">").append(escapeXml(text)).append("</w:t></w:r>");
        }
        sb.append("</w:p>");
        return sb.toString();
    }

    private static String tableRow(boolean isHeader, String... cells) {
        StringBuilder sb = new StringBuilder("<w:tr>");
        if (isHeader) {
            sb.append("<w:trPr><w:tblHeader/></w:trPr>");
        }
        for (String cell : cells) {
            sb.append("<w:tc><w:tcPr>");
            if (isHeader) {
                sb.append("<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"EBF1F5\"/>");
            }
            sb.append("<w:tcMar><w:top w:w=\"120\" w:type=\"dxa\"/><w:bottom w:w=\"120\" w:type=\"dxa\"/><w:left w:w=\"160\" w:type=\"dxa\"/><w:right w:w=\"160\" w:type=\"dxa\"/></w:tcMar>");
            sb.append("</w:tcPr>");
            
            // Tách các dòng xuống dòng bằng \n
            String[] lines = cell.split("\n");
            for (String line : lines) {
                sb.append("<w:p><w:pPr><w:spacing w:line=\"240\" w:lineRule=\"auto\" w:after=\"40\"/></w:pPr>");
                sb.append("<w:r><w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>");
                if (isHeader) sb.append("<w:b/>");
                sb.append("<w:sz w:val=\"22\"/></w:rPr><w:t xml:space=\"preserve\">").append(escapeXml(line)).append("</w:t></w:r></w:p>");
            }
            sb.append("</w:tc>");
        }
        sb.append("</w:tr>");
        return sb.toString();
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String buildDocumentXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">");
        sb.append("<w:body>");

        // --- TRANG BÌA ---
        sb.append(p("TRƯỜNG ĐẠI HỌC KHOA HỌC TỰ NHIÊN / BÁCH KHOA", null, true, 14, "center"));
        sb.append(p("KHOA CÔNG NGHỆ THÔNG TIN", null, true, 13, "center"));
        sb.append(p("", null, false, 12, "center"));
        sb.append(p("-------------------------***-------------------------", null, false, 12, "center"));
        sb.append(p("", null, false, 12, "center"));
        sb.append(p("", null, false, 12, "center"));
        sb.append(p("BÁO CÁO ĐỒ ÁN MÔN HỌC", null, true, 18, "center"));
        sb.append(p("LẬP TRÌNH WEB & CÔNG NGHỆ PHẦN MỀM", null, true, 16, "center"));
        sb.append(p("", null, false, 12, "center"));
        sb.append(p("ĐỀ TÀI:", null, false, 13, "center"));
        sb.append(p("KHẢO SÁT, PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG", null, true, 16, "center"));
        sb.append(p("QUẢN LÝ VÀ ĐẶT LỊCH KHÁM PHÒNG KHÁM TƯ NHÂN (MEDCLINIC)", null, true, 16, "center"));
        sb.append(p("", null, false, 12, "center"));
        sb.append(p("", null, false, 12, "center"));
        sb.append(p("Giảng viên hướng dẫn: Bộ môn Công nghệ Phần mềm & Lập trình Web", null, false, 13, "left"));
        sb.append(p("Sinh viên thực hiện: Nhóm phát triển MedClinic", null, false, 13, "left"));
        sb.append(p("Học kỳ: 2 - Năm học: 2025 - 2026", null, false, 13, "left"));
        sb.append(p("", null, false, 12, "center"));
        sb.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>");

        // --- CHƯƠNG 2: KHẢO SÁT HIỆN TRẠNG ---
        sb.append(p("Chương 2: KHẢO SÁT HIỆN TRẠNG", "Heading1", true, 16, "left"));
        sb.append(p("Nhằm xây dựng một hệ thống quản lý và đặt lịch khám toàn diện, việc khảo sát các giải pháp y tế số hiện có trên thị trường Việt Nam là vô cùng cần thiết. Dưới đây là phân tích ưu, nhược điểm của 4 nền tảng tiêu biểu: Medpro, BookingCare, YouMed và Cổng thông tin Bệnh viện Đa khoa Quốc tế Vinmec.", null, false, 13, "both"));

        // 2.1. Medpro
        sb.append(p("2.1. Medpro (Ứng dụng Đặt khám Bệnh viện & Phòng khám)", "Heading2", true, 14, "left"));
        sb.append(p("Medpro là nền tảng kết nối người bệnh với hơn 100 bệnh viện công lập và phòng khám đa khoa trên toàn quốc (như Bệnh viện Chợ Rẫy, ĐH Y Dược TP.HCM, Nhi Đồng 1). Hệ thống cung cấp dịch vụ đặt lịch khám theo chuyên khoa, bác sĩ, ngày giờ và thanh toán trực tuyến.", null, false, 13, "both"));
        sb.append(p("Bảng 2-1. Đánh giá ưu điểm và nhược điểm của nền tảng Medpro", null, true, 11, "center"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "Ưu điểm", "Nhược điểm"));
        sb.append(tableRow(false,
                "✓ Mạng lưới kết nối bệnh viện lớn và phòng khám uy tín rộng khắp cả nước\n" +
                "✓ Cho phép người bệnh chọn bác sĩ, chuyên khoa và ngày khám linh hoạt\n" +
                "✓ Cung cấp phiếu khám điện tử có mã QR check-in tiện lợi\n" +
                "✓ Đa dạng cổng thanh toán: MoMo, VNPAY, ZaloPay, Thẻ ATM/Visa\n" +
                "✓ Hỗ trợ nhắc lịch khám tự động qua Zalo và SMS",
                "× Giao diện ứng dụng nhiều banner quảng cáo và khuyến mãi gây rối mắt\n" +
                "× Không phân chia TimeSlot cụ thể (thường chỉ xếp số thứ tự trong buổi sáng/chiều, người bệnh vẫn phải chờ đợi lâu)\n" +
                "× Chưa tích hợp chức năng theo dõi đơn thuốc điện tử và tồn kho dược phẩm\n" +
                "× Khách vãng lai đến khám tại quầy không được đồng bộ tức thời với hàng đợi online"
        ));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // 2.2. BookingCare
        sb.append(p("2.2. BookingCare (Nền tảng Y tế Chăm sóc Sức khỏe Toàn diện)", "Heading2", true, 14, "left"));
        sb.append(p("BookingCare là nền tảng đặt lịch khám chuyên khoa với các bác sĩ giỏi tại các bệnh viện lớn và phòng khám tư nhân cao cấp ở Hà Nội và TP.HCM, cung cấp thông tin tiểu sử bác sĩ rất chi tiết và hỗ trợ tư vấn khám từ xa (Telemedicine).", null, false, 13, "both"));
        sb.append(p("Bảng 2-2. Đánh giá ưu điểm và nhược điểm của BookingCare", null, true, 11, "center"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "Ưu điểm", "Nhược điểm"));
        sb.append(tableRow(false,
                "✓ Thông tin bác sĩ cực kỳ chi tiết (học hàm, kinh nghiệm, quy trình khám, giá dịch vụ)\n" +
                "✓ Cho phép đặt lịch theo khung giờ cụ thể (30 - 60 phút)\n" +
                "✓ Có hệ thống đánh giá, nhận xét (Review & Rating) khách quan từ bệnh nhân thực tế\n" +
                "✓ Có cẩm nang y tế bài viết chuyên môn chất lượng cao",
                "× Quy trình đặt lịch yêu cầu nhân viên tổng đài gọi điện xác nhận thủ công, chưa tự động hóa 100%\n" +
                "× Không có phân hệ Bàn khám bác sĩ và Bệnh án điện tử nội bộ cho phòng khám\n" +
                "× Chưa hỗ trợ quản lý kho thuốc và xuất đơn thuốc PDF chuẩn y tế"
        ));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // 2.3. YouMed
        sb.append(p("2.3. YouMed (Ứng dụng Quản lý & Đặt khám Phòng khám Thông minh)", "Heading2", true, 14, "left"));
        sb.append(p("YouMed là giải pháp phần mềm y tế phục vụ cả hai đối tượng: người bệnh đặt lịch khám trực tuyến và bác sĩ/phòng khám quản lý lịch hẹn, bệnh án thông qua phần mềm YouMed Clinic.", null, false, 13, "both"));
        sb.append(p("Bảng 2-3. Đánh giá ưu điểm và nhược điểm của YouMed", null, true, 11, "center"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "Ưu điểm", "Nhược điểm"));
        sb.append(tableRow(false,
                "✓ Giao diện hiện đại, tối giản, chuẩn y tế, trải nghiệm người dùng mượt mà\n" +
                "✓ Có phân hệ quản lý phòng khám cho bác sĩ và nhân viên tiếp tân\n" +
                "✓ Tích hợp hồ sơ bệnh án điện tử và lưu trữ đơn thuốc\n" +
                "✓ Có nhắc lịch uống thuốc và tái khám thông minh",
                "× Chi phí bản quyền phần mềm quản lý phòng khám khá cao đối với các phòng khám tư nhân vừa và nhỏ\n" +
                "× Yêu cầu cài đặt ứng dụng trên điện thoại, trải nghiệm trên nền tảng Web còn hạn chế\n" +
                "× Khả năng xử lý đặt trùng lịch đồng thời (Concurrency Double Booking) chưa được tối ưu khi tải cao"
        ));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // 2.4. Vinmec Portal
        sb.append(p("2.4. Cổng Thông Tin & Đặt Khám Bệnh Viện Đa Khoa Quốc Tế Vinmec", "Heading2", true, 14, "left"));
        sb.append(p("Hệ thống đặt khám trực tuyến thuộc Tập đoàn Vingroup, tiêu chuẩn quốc tế JCI với dịch vụ chăm sóc y tế cao cấp, quản lý tập trung toàn bộ dữ liệu khám chữa bệnh của người bệnh trong toàn hệ thống.", null, false, 13, "both"));
        sb.append(p("Bảng 2-4. Đánh giá ưu điểm và nhược điểm của cổng thông tin Vinmec", null, true, 11, "center"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "Ưu điểm", "Nhược điểm"));
        sb.append(tableRow(false,
                "✓ Hệ thống bảo mật thông tin y tế chuẩn quốc tế (HIPAA/JCI)\n" +
                "✓ Liên kết trực tiếp kết quả xét nghiệm, chẩn đoán hình ảnh và lịch sử tiêm chủng\n" +
                "✓ Bệnh nhân theo dõi được toàn bộ lộ trình khám chữa bệnh",
                "× Hệ thống đóng kín (Closed ecosystem), chỉ áp dụng cho nội bộ bệnh viện Vinmec, không thể triển khai cho phòng khám tư nhân\n" +
                "× Thủ tục đặt khám và giao diện phân cấp quá sâu, phức tạp đối với người lớn tuổi\n" +
                "× Chi phí khám và dịch vụ ở phân khúc cao cấp, không phổ biến cho đại chúng"
        ));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // 2.5. Bảng tổng hợp đối sánh & Định hướng xây dựng MedClinic
        sb.append(p("2.5. Bảng Tổng Hợp Đối Sánh & Định Hướng Xây Dựng MedClinic", "Heading2", true, 14, "left"));
        sb.append(p("Dựa trên việc phân tích ưu và nhược điểm của các hệ thống đi trước, hệ thống MedClinic được định hướng xây dựng nhằm kế thừa những điểm mạnh và giải quyết triệt để các tồn tại:", null, false, 13, "both"));
        sb.append(p("Bảng 2-5. Bảng ma trận so sánh các tính năng chính giữa các hệ thống", null, true, 11, "center"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "Tiêu chí / Tính năng", "Medpro", "BookingCare", "YouMed", "Vinmec", "MedClinic (Đề xuất)"));
        sb.append(tableRow(false, "Nền tảng hoạt động", "App Mobile là chính", "Web & App", "App Mobile", "Web & App", "Web Responsive (Full HTML5/Bootstrap 5.3)"));
        sb.append(tableRow(false, "Phân chia TimeSlot cụ thể", "Một phần (theo buổi)", "Có (30 - 60p)", "Có (30p)", "Có", "Có (Slot 30 phút tự động sinh)"));
        sb.append(tableRow(false, "Chống Double-Booking Concurrency", "Chưa tối ưu", "Xác nhận thủ công", "Khóa cơ bản", "Khóa hàng đợi", "Optimistic Lock (@Version) + DB Constraint"));
        sb.append(tableRow(false, "Check-in QR Code tại quầy", "Có", "Không", "Có", "Có", "Có (Mã QR Base64 + Cấp STT tự động)"));
        sb.append(tableRow(false, "Tiếp nhận bệnh nhân vãng lai", "Hạn chế", "Không", "Có", "Có", "Có (Phân hệ quầy tiếp nhận Walk-in chuyên biệt)"));
        sb.append(tableRow(false, "Bàn khám bác sĩ & Bệnh án", "Không", "Không", "Có", "Có", "Có (Chẩn đoán lâm sàng, tiền sử bệnh án)"));
        sb.append(tableRow(false, "Kê đơn thuốc & Trừ kho tự động", "Không", "Không", "Một phần", "Có", "Có (Kê đơn thuốc động, trừ tồn kho dược)"));
        sb.append(tableRow(false, "Xuất Đơn thuốc & Hóa đơn PDF", "Không", "Không", "Có", "Có", "Có (Thư viện OpenPDF xuất file chuẩn y tế)"));
        sb.append(tableRow(false, "Thống kê doanh thu thời gian thực", "Nội bộ", "Nội bộ", "Có", "Nội bộ", "Có (Chart.js biểu đồ đường, cột, tròn theo ngày/tháng)"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));
        sb.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>");

        // --- CHƯƠNG 3: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG ---
        sb.append(p("Chương 3: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG", "Heading1", true, 16, "left"));
        sb.append(p("3.1. Phân tích chức năng", "Heading2", true, 14, "left"));

        // 3.1.1. Khách
        sb.append(p("3.1.1. Phía Khách (Guest)", "Heading3", true, 13, "left"));
        sb.append(p("Bảng 3-1. Danh mục chức năng phía Khách", null, true, 11, "center"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Chức năng", "Mô tả"));
        sb.append(tableRow(false, "1", "Đăng ký tài khoản", "Đăng ký tạo tài khoản bệnh nhân mới với đầy đủ thông tin cá nhân"));
        sb.append(tableRow(false, "2", "Đăng nhập hệ thống", "Đăng nhập vào hệ thống theo vai trò người dùng"));
        sb.append(tableRow(false, "3", "Khôi phục mật khẩu", "Yêu cầu khôi phục mật khẩu thông qua địa chỉ email đã đăng ký"));
        sb.append(tableRow(false, "4", "Tra cứu chuyên khoa", "Xem danh sách các chuyên khoa y tế và thông tin dịch vụ phòng khám"));
        sb.append(tableRow(false, "5", "Tìm kiếm & Lọc bác sĩ", "Tìm kiếm bác sĩ theo chuyên khoa, kinh nghiệm, học hàm và giá khám"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // 3.1.2. Bệnh nhân
        sb.append(p("3.1.2. Phía Bệnh nhân (Patient)", "Heading3", true, 13, "left"));
        sb.append(p("Bảng 3-2. Danh mục chức năng phía Bệnh nhân", null, true, 11, "center"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
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
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // 3.1.3. Bác sĩ
        sb.append(p("3.1.3. Phía Bác sĩ (Doctor)", "Heading3", true, 13, "left"));
        sb.append(p("Bảng 3-3. Danh mục chức năng phía Bác sĩ", null, true, 11, "center"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Chức năng", "Mô tả"));
        sb.append(tableRow(false, "1", "Xem lịch trực cá nhân", "Xem lịch phân ca làm việc (ca sáng / ca chiều) trong 14 ngày"));
        sb.append(tableRow(false, "2", "Xem hàng chờ khám bệnh", "Xem danh sách bệnh nhân đã check-in trong ngày theo thứ tự số STT"));
        sb.append(tableRow(false, "3", "Tiếp nhận & Bắt đầu khám", "Gọi bệnh nhân vào phòng khám, chuyển trạng thái ca khám sang IN_PROGRESS"));
        sb.append(tableRow(false, "4", "Tra cứu tiền sử bệnh án", "Xem hồ sơ bệnh án của các lần khám trước của bệnh nhân"));
        sb.append(tableRow(false, "5", "Ghi chẩn đoán bệnh án", "Nhập triệu chứng lâm sàng quan sát được, chẩn đoán, dặn dò, hẹn ngày tái khám"));
        sb.append(tableRow(false, "6", "Kê đơn thuốc điện tử", "Chọn thuốc từ kho dược, nhập số lượng, liều dùng, cách dùng (tự trừ tồn kho)"));
        sb.append(tableRow(false, "7", "Hoàn tất khám & Xuất đơn PDF", "Hoàn thành ca khám, hệ thống sinh hóa đơn tự động và cho phép in đơn thuốc PDF"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // 3.1.4. Lễ tân
        sb.append(p("3.1.4. Phía Lễ tân / Tiếp nhận (Staff)", "Heading3", true, 13, "left"));
        sb.append(p("Bảng 3-4. Danh mục chức năng phía Lễ tân", null, true, 11, "center"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Chức năng", "Mô tả"));
        sb.append(tableRow(false, "1", "Tra cứu tiếp nhận", "Tìm kiếm lịch hẹn bệnh nhân bằng mã đặt lịch, số điện thoại hoặc họ tên"));
        sb.append(tableRow(false, "2", "Quét QR Check-in & Cấp STT", "Quét mã QR hoặc bấm check-in, tự động cấp số thứ tự khám STT-XX trong ngày"));
        sb.append(tableRow(false, "3", "Tiếp nhận bệnh nhân vãng lai", "Đăng ký hồ sơ và đặt lịch trực tiếp tại quầy cho bệnh nhân chưa đặt trước"));
        sb.append(tableRow(false, "4", "Quản lý hóa đơn viện phí", "Xem danh sách hóa đơn viện phí chờ thanh toán của các ca khám hoàn tất"));
        sb.append(tableRow(false, "5", "Thu viện phí & Xác nhận", "Thu tiền mặt tại quầy hoặc xác nhận thanh toán chuyển khoản / VNPay"));
        sb.append(tableRow(false, "6", "Xuất Hóa đơn viện phí PDF", "Xuất và in phiếu thanh toán hóa đơn viện phí định dạng PDF"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // 3.1.5. Quản trị viên
        sb.append(p("3.1.5. Phía Quản trị viên (Admin)", "Heading3", true, 13, "left"));
        sb.append(p("Bảng 3-5. Danh mục chức năng phía Quản trị viên", null, true, 11, "center"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Chức năng", "Mô tả"));
        sb.append(tableRow(false, "1", "Dashboard KPI tổng quan", "Theo dõi số lượng bệnh nhân, bác sĩ, ca khám hôm nay và doanh thu thực thu"));
        sb.append(tableRow(false, "2", "Biểu đồ thống kê doanh thu", "Xem biểu đồ đường/cột thống kê doanh thu và tỷ lệ ca khám theo chuyên khoa"));
        sb.append(tableRow(false, "3", "Quản lý Chuyên khoa", "Xem, thêm mới, cập nhật mô tả, icon và bật/tắt hoạt động chuyên khoa"));
        sb.append(tableRow(false, "4", "Quản lý Hồ sơ Bác sĩ", "Thêm, cập nhật bác sĩ, phân chuyên khoa, học hàm, kinh nghiệm, giá khám"));
        sb.append(tableRow(false, "5", "Quản lý Kho thuốc & Dược phẩm", "Thêm thuốc, cập nhật giá bán, số lượng tồn kho, hướng dẫn sử dụng"));
        sb.append(tableRow(false, "6", "Quản lý Tài khoản người dùng", "Xem danh sách tài khoản, tìm kiếm, kích hoạt hoặc khóa tài khoản vi phạm"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));
        sb.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>");

        // 3.2. Bảng đặc tả Use Case chi tiết
        sb.append(p("3.2. Biểu Đồ Use Case & Bảng Đặc Tả Use Case Chi Tiết", "Heading2", true, 14, "left"));
        sb.append(p("Phần này đặc tả chi tiết toàn bộ các Use Case trong hệ thống MedClinic theo quy chuẩn IEEE 830 và biểu mẫu khảo sát hiện trạng.", null, false, 13, "both"));

        // Thêm các bảng đặc tả Use Case theo chuẩn mẫu
        addUseCaseTable(sb, "UC_001", "Đăng ký tài khoản (Sign up)", "Đăng ký tạo tài khoản bệnh nhân mới trong hệ thống",
                "Khách (Guest)", "Chưa có tài khoản trong hệ thống",
                "- Nếu thành công, tài khoản mới được tạo với quyền ROLE_PATIENT và mã hóa mật khẩu\n- Nếu thất bại, hiển thị thông báo lỗi",
                "1. Vào hệ thống website\n2. Chọn nút Đăng ký khám\n3. Nhập đầy đủ thông tin: họ tên, username, email, SĐT, ngày sinh, mật khẩu\n4. Bấm Hoàn tất đăng ký\n5. Hệ thống xác thực dữ liệu hợp lệ và lưu vào CSDL\n6. Chuyển về trang Đăng nhập kèm thông báo thành công",
                "4a. Bấm Hủy bỏ\n5a. Hủy thao tác, quay về trang chủ",
                "5b. Tên đăng nhập hoặc email đã tồn tại\n5b1. Thông báo lỗi: Tên đăng nhập hoặc email đã tồn tại\n5c. Mật khẩu xác nhận không khớp\n5c1. Báo lỗi mật khẩu xác nhận");

        addUseCaseTable(sb, "UC_002", "Đăng nhập hệ thống (Sign in)", "Đăng nhập vào hệ thống MedClinic để thực hiện các chức năng theo vai trò",
                "Khách (Guest), Bệnh nhân (Patient), Bác sĩ (Doctor), Lễ tân (Staff), Quản trị viên (Admin)", "Đã có tài khoản trong hệ thống",
                "- Nếu thành công, chuyển hướng đến trang tương ứng với vai trò (Patient: lịch hẹn, Doctor: bàn khám, Staff: quầy tiếp nhận, Admin: dashboard)\n- Nếu thất bại, hiển thị thông báo lỗi",
                "1. Vào hệ thống website\n2. Chọn nút Đăng nhập trên thanh điều hướng\n3. Nhập tên đăng nhập (hoặc email) và mật khẩu\n4. Tùy chọn tick Ghi nhớ đăng nhập (Remember-me)\n5. Bấm nút Đăng Nhập\n6. Hệ thống xác thực bằng Spring Security 6 và chuyển hướng đúng trang vai trò",
                "3a. Bấm chọn tài khoản Demo Nhanh (Admin, Bác sĩ, Lễ tân, Bệnh nhân)\n3a1. Hệ thống tự động điền username và password",
                "6b. Tên đăng nhập hoặc mật khẩu không chính xác\n6b1. Hiển thị thông báo đăng nhập thất bại\n6c. Tài khoản bị khóa\n6c1. Thông báo tài khoản đã bị khóa bởi quản trị viên");

        addUseCaseTable(sb, "UC_003", "Đăng xuất khỏi hệ thống (Sign out)", "Thoát phiên làm việc hiện tại",
                "Bệnh nhân, Bác sĩ, Lễ tân, Quản trị viên", "Đang đăng nhập trong hệ thống",
                "Đăng xuất khỏi hệ thống, hủy session và cookie, chuyển về trang đăng nhập/trang chủ",
                "1. Chọn biểu tượng tài khoản trên thanh navbar, hiển thị dropdown menu\n2. Chọn nút Đăng xuất\n3. Hệ thống xóa phiên làm việc và cookie JSESSIONID\n4. Chuyển hướng về trang Đăng nhập kèm thông báo đăng xuất thành công",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_004", "Khôi phục mật khẩu (Password Recovery)", "Tạo lại mật khẩu mới khi người dùng quên mật khẩu",
                "Khách, Bệnh nhân", "Đã có tài khoản với email hợp lệ",
                "- Nếu thành công, mật khẩu mới được cập nhật\n- Nếu thất bại, mật khẩu không thay đổi",
                "1. Tại trang Đăng nhập, chọn Quên mật khẩu?\n2. Nhập địa chỉ email đăng ký\n3. Bấm Gửi yêu cầu\n4. Hệ thống xác thực email và gửi liên kết đặt lại mật khẩu về email người dùng\n5. Người dùng kiểm tra email và nhấp vào liên kết\n6. Nhập mật khẩu mới và xác nhận mật khẩu mới\n7. Bấm Lưu mật khẩu\n8. Thông báo đổi mật khẩu thành công và quay lại trang đăng nhập",
                "3a. Bấm Quay lại đăng nhập",
                "4b. Email không tồn tại trong hệ thống\n4b1. Thông báo không tìm thấy tài khoản với email này");

        addUseCaseTable(sb, "UC_005", "Quản lý thông tin cá nhân & Đổi mật khẩu", "Xem, chỉnh sửa thông tin cá nhân và thay đổi mật khẩu tài khoản",
                "Bệnh nhân, Bác sĩ, Lễ tân, Quản trị viên", "Đã đăng nhập vào hệ thống",
                "Thông tin cá nhân hoặc mật khẩu được cập nhật vào CSDL",
                "1. Bấm vào tên người dùng trên thanh navbar, chọn Hồ sơ cá nhân (/profile)\n2. Hệ thống hiển thị thông tin chi tiết hiện tại\n3. Chỉnh sửa họ tên, số điện thoại, địa chỉ\n4. Bấm Cập nhật thông tin\n5. Hệ thống lưu thay đổi và hiển thị thông báo thành công",
                "3a. Cuộn xuống phần Đổi mật khẩu\n3a1. Nhập mật khẩu cũ, mật khẩu mới, xác nhận mật khẩu mới\n3a2. Bấm Đổi mật khẩu\n3a3. Hệ thống xác thực mật khẩu cũ và cập nhật mật khẩu mới",
                "3a4. Mật khẩu cũ không chính xác\n3a5. Thông báo lỗi mật khẩu cũ không đúng");

        addUseCaseTable(sb, "UC_006", "Tìm kiếm và lọc bác sĩ (Search/Filter doctors)", "Tìm kiếm bác sĩ phù hợp theo chuyên khoa, giá khám và từ khóa",
                "Khách (Guest), Bệnh nhân (Patient)", "N/A",
                "Hiển thị danh sách bác sĩ thỏa mãn tiêu chí tìm kiếm",
                "1. Vào trang Bác sĩ (/doctors)\n2. Nhập từ khóa tên bác sĩ hoặc học hàm\n3. Chọn chuyên khoa từ danh sách dropdown\n4. Nhập mức phí khám tối đa mong muốn\n5. Bấm Lọc kết quả\n6. Hệ thống hiển thị danh sách các thẻ bác sĩ phù hợp kèm ảnh, chuyên khoa, kinh nghiệm và giá khám",
                "5a. Bấm nút Đặt lịch trên thẻ bác sĩ\n5a1. Chuyển thẳng đến trang đặt lịch với bác sĩ đó",
                "N/A");

        addUseCaseTable(sb, "UC_007", "Xem chi tiết bác sĩ (View doctor profile)", "Xem đầy đủ thông tin chuyên môn, tiểu sử và lịch trực của bác sĩ",
                "Khách, Bệnh nhân", "N/A",
                "Hiển thị trang chi tiết bác sĩ",
                "1. Từ danh sách bác sĩ, bấm vào tên bác sĩ hoặc nút Xem chi tiết\n2. Hệ thống tải thông tin bác sĩ: học hàm, chuyên khoa, kinh nghiệm, tiểu sử đào tạo và bảng giá\n3. Hiển thị lịch khám dự kiến trong tuần của bác sĩ",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_008", "Đặt lịch khám theo khung giờ (Online Appointment Booking)", "Bệnh nhân chọn bác sĩ, ngày khám và khung giờ 30 phút để đặt lịch hẹn",
                "Bệnh nhân (ROLE_PATIENT)", "Đã đăng nhập tài khoản bệnh nhân",
                "- Nếu thành công, lịch hẹn được tạo với trạng thái CONFIRMED, slot chuyển sang BOOKED (tăng @Version), mã QR được sinh và email xác nhận được gửi ngầm\n- Nếu thất bại (trùng lịch), thông báo chọn slot khác",
                "1. Vào trang Đặt lịch khám (/patient/book-appointment)\n2. Chọn bác sĩ và ngày khám mong muốn\n3. Hệ thống gửi yêu cầu AJAX tải danh sách các khung giờ 30 phút còn khả dụng (status = AVAILABLE)\n4. Bệnh nhân bấm chọn một khung giờ phù hợp\n5. Nhập lý do khám và mô tả triệu chứng\n6. Bấm Xác nhận đặt lịch\n7. Giao dịch bắt đầu: kiểm tra khóa lạc quan Optimistic Locking trên TimeSlot\n8. Hệ thống cập nhật TimeSlot sang BOOKED, tăng version, tạo Appointment với mã booking duy nhất (MED-YYMMDD-XXXX)\n9. Sinh mã QR Base64 và kích hoạt task gửi email xác nhận ngầm (@Async)\n10. Chuyển hướng sang trang Chi tiết lịch hẹn",
                "6a. Bấm Hủy\n6a1. Hủy thao tác đặt lịch",
                "7b. Khung giờ vừa bị người khác đặt trước cùng lúc\n7b1. Hệ thống phát hiện xung đột version hoặc slot khác AVAILABLE\n7b2. Bắt lỗi SlotAlreadyBookedException và hiển thị thông báo: Khung giờ này vừa có người đặt trước, vui lòng chọn khung giờ khác");

        addUseCaseTable(sb, "UC_009", "Quản lý lịch hẹn & Hủy lịch (Manage appointments & Cancel)", "Xem danh sách các ca hẹn đã đặt và thực hiện hủy lịch khi có nhu cầu",
                "Bệnh nhân (ROLE_PATIENT)", "Đã đăng nhập vào hệ thống",
                "Lịch hẹn được hủy (status = CANCELLED), TimeSlot được giải phóng về AVAILABLE",
                "1. Vào trang Lịch hẹn của tôi (/patient/appointments)\n2. Hệ thống hiển thị danh sách các lịch hẹn (Sắp tới, Đã khám, Đã hủy)\n3. Bệnh nhân bấm Xem chi tiết để xem mã QR check-in\n4. Với lịch hẹn chưa khám, bấm nút Hủy lịch hẹn\n5. Hộp thoại xác nhận hiện ra, bấm Đồng ý hủy\n6. Hệ thống chuyển trạng thái lịch hẹn sang CANCELLED và trả lại TimeSlot về trạng thái AVAILABLE",
                "5a. Bấm Hủy bỏ trên hộp thoại\n5a1. Giữ nguyên lịch hẹn",
                "N/A");

        addUseCaseTable(sb, "UC_010", "Xem hồ sơ bệnh án & Toa thuốc cũ", "Tra cứu tiền sử bệnh lý và các đơn thuốc đã được bác sĩ kê",
                "Bệnh nhân (ROLE_PATIENT)", "Đã đăng nhập vào hệ thống",
                "Hiển thị danh sách hồ sơ bệnh án điện tử của bệnh nhân",
                "1. Vào mục Hồ sơ bệnh án (/patient/medical-records)\n2. Hệ thống hiển thị danh sách các lần khám theo thứ tự thời gian mới nhất\n3. Xem chi tiết chẩn đoán, lời dặn của bác sĩ và ngày tái khám\n4. Bấm nút Tải Đơn Thuốc PDF để lưu hoặc in đơn thuốc",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_011", "Quầy tiếp nhận & Tra cứu lịch hẹn (Reception Desk)", "Lễ tân tra cứu danh sách lịch hẹn trong ngày để tiếp đón bệnh nhân",
                "Lễ tân (ROLE_STAFF)", "Đã đăng nhập với quyền STAFF",
                "Hiển thị thông tin lịch hẹn và trạng thái tiếp nhận của bệnh nhân",
                "1. Lễ tân vào giao diện Quầy tiếp nhận (/staff/reception)\n2. Hệ thống hiển thị danh sách lịch hẹn trong ngày hôm nay\n3. Lễ tân nhập mã đặt lịch (MED-xxx) hoặc số điện thoại của bệnh nhân vào ô tìm kiếm\n4. Bấm Tìm kiếm\n5. Hệ thống hiển thị bản ghi lịch hẹn tương ứng kèm trạng thái",
                "N/A", "3b. Không tìm thấy lịch hẹn\n3b1. Thông báo không tìm thấy thông tin đặt lịch");

        addUseCaseTable(sb, "UC_012", "Quét QR Check-in & Cấp STT tiếp nhận", "Xác nhận bệnh nhân đã có mặt tại phòng khám và cấp số thứ tự vào khám",
                "Lễ tân (ROLE_STAFF)", "Đã đăng nhập với quyền STAFF",
                "Lịch hẹn chuyển sang CHECKED_IN, cấp số STT-XX, xuất hiện trên hàng chờ bác sĩ",
                "1. Bệnh nhân xuất trình mã QR check-in trên điện thoại hoặc mã đặt lịch\n2. Lễ tân quét mã QR bằng đầu đọc hoặc nhập mã đặt lịch trên giao diện quầy tiếp nhận\n3. Lễ tân đối chiếu thông tin và bấm nút Check-in (Cấp STT)\n4. Hệ thống đếm số lượng ca khám trong ngày và cấp mã STT tự động (VD: STT-01, STT-02)\n5. Cập nhật trạng thái ca khám sang CHECKED_IN\n6. Bệnh nhân tự động xuất hiện trên màn hình Bàn khám của bác sĩ chuyên khoa phụ trách",
                "N/A", "3b. Ca khám đã được check-in trước đó\n3b1. Thông báo bệnh nhân này đã được cấp STT");

        addUseCaseTable(sb, "UC_013", "Tiếp nhận bệnh nhân vãng lai (Walk-in Registration)", "Tạo hồ sơ và đăng ký ca khám trực tiếp tại quầy cho bệnh nhân chưa đặt online",
                "Lễ tân (ROLE_STAFF)", "Đã đăng nhập với quyền STAFF",
                "Tạo tài khoản bệnh nhân (nếu mới), tạo lịch hẹn hôm nay và cấp STT ngay lập tức",
                "1. Lễ tân vào mục Tiếp nhận vãng lai (/staff/walk-in)\n2. Nhập số điện thoại bệnh nhân để tra cứu hồ sơ cũ\n3. Nếu là bệnh nhân mới, nhập họ tên, ngày sinh, giới tính, địa chỉ\n4. Chọn bác sĩ chuyên khoa và ca khám trong ngày\n5. Bấm Đăng ký & Cấp STT\n6. Hệ thống tạo lịch hẹn, cấp số thứ tự tiếp nhận ngay và đưa vào hàng chờ khám",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_014", "Bàn khám bác sĩ & Hàng chờ khám (Doctor Desk)", "Bác sĩ theo dõi hàng đợi bệnh nhân đã check-in và gọi vào phòng khám",
                "Bác sĩ (ROLE_DOCTOR)", "Đã đăng nhập với quyền DOCTOR",
                "Hiển thị danh sách bệnh nhân chờ khám và gọi ca khám",
                "1. Bác sĩ mở trang Bàn khám (/doctor/desk)\n2. Hệ thống hiển thị hàng chờ bệnh nhân hôm nay theo thứ tự STT\n3. Bác sĩ bấm nút Khám Bệnh ở bệnh nhân đầu hàng chờ\n4. Trạng thái ca khám chuyển sang IN_PROGRESS\n5. Hệ thống chuyển hướng sang giao diện phòng khám bệnh án điện tử",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_015", "Khám bệnh & Ghi bệnh án điện tử", "Bác sĩ ghi nhận triệu chứng lâm sàng, chẩn đoán bệnh lý và lời dặn",
                "Bác sĩ (ROLE_DOCTOR)", "Đang mở phiên khám bệnh nhân",
                "Hồ sơ bệnh án được lưu trữ và liên kết với ca hẹn",
                "1. Tại giao diện phòng khám, bác sĩ xem thông tin bệnh nhân và tiền sử các lần khám trước\n2. Nhập triệu chứng lâm sàng ghi nhận\n3. Nhập kết luận chẩn đoán y khoa\n4. Nhập lời dặn, chế độ dinh dưỡng, kiêng cử\n5. Tùy chọn chọn ngày hẹn tái khám\n6. Bác sĩ tiến hành kê đơn thuốc (UC_016) hoặc bấm Hoàn tất khám",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_016", "Kê đơn thuốc động & Tự động trừ tồn kho", "Bác sĩ chọn thuốc từ kho dược, nhập liều lượng, cách dùng và hệ thống trừ kho tự động",
                "Bác sĩ (ROLE_DOCTOR)", "Đang ở giao diện khám bệnh",
                "Toa thuốc được lưu, số lượng thuốc trong kho được trừ tương ứng",
                "1. Tại mục Kê đơn thuốc, bác sĩ bấm nút Thêm thuốc\n2. Chọn loại thuốc từ danh mục kho dược (hiển thị rõ đơn vị tính và đơn giá)\n3. Nhập số lượng kê đơn\n4. Nhập liều dùng (VD: 1 viên / lần) và tần suất (VD: 2 lần / ngày sau ăn)\n5. Có thể bấm thêm nhiều loại thuốc vào đơn\n6. Bác sĩ bấm Hoàn tất khám bệnh\n7. Hệ thống kiểm tra tồn kho của từng loại thuốc trong bảng medicines\n8. Trừ số lượng tồn kho tương ứng trong CSDL\n9. Lưu thông tin Prescription và các PrescriptionItem\n10. Cập nhật trạng thái lịch hẹn sang COMPLETED và tự động sinh hóa đơn viện phí",
                "2a. Bấm nút Xóa ở dòng thuốc không muốn kê\n2a1. Dòng thuốc được xóa khỏi đơn",
                "7b. Thuốc trong kho không đủ số lượng kê\n7b1. Hệ thống báo lỗi: Thuốc X không đủ số lượng tồn kho (Còn Y viên), vui lòng giảm số lượng hoặc đổi thuốc khác");

        addUseCaseTable(sb, "UC_017", "Xuất Đơn thuốc ra file PDF", "Tạo file PDF đơn thuốc và phiếu khám bệnh chuẩn y tế",
                "Bác sĩ, Bệnh nhân, Lễ tân", "Ca khám đã hoàn tất và có đơn thuốc",
                "Tải về file PDF đơn thuốc chuẩn y tế",
                "1. Bấm nút Tải Đơn Thuốc PDF trên giao diện chi tiết lịch hẹn hoặc bàn khám\n2. Hệ thống sử dụng OpenPDF biên dịch dữ liệu bệnh án, thông tin phòng khám, bác sĩ và bảng chi tiết thuốc\n3. Trả về luồng byte PDF trực tiếp trên trình duyệt hoặc tải về máy",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_018", "Tự động sinh Hóa đơn viện phí", "Hệ thống tự động tính tổng tiền công khám và tiền thuốc khi bác sĩ hoàn tất khám",
                "Hệ thống (System Service)", "Bác sĩ bấm Hoàn tất khám bệnh",
                "Bản ghi Invoice được tạo với mã INV-YYMMDD-XXXX và trạng thái UNPAID",
                "1. Hệ thống nhận sự kiện ca khám chuyển sang COMPLETED\n2. Lấy giá tiền khám tư vấn của bác sĩ (consultationFee)\n3. Tính tổng tiền các loại thuốc trong đơn (medicineFee)\n4. Tính tổng viện phí: totalAmount = consultationFee + medicineFee\n5. Sinh mã hóa đơn duy nhất và lưu vào bảng invoices với trạng thái UNPAID\n6. Hóa đơn xuất hiện trên màn hình Thu viện phí của lễ tân",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_019", "Thu viện phí & Xác nhận thanh toán", "Lễ tân thu tiền mặt hoặc xác nhận thanh toán trực tuyến cho ca khám",
                "Lễ tân (ROLE_STAFF)", "Đã có hóa đơn ở trạng thái UNPAID",
                "Hóa đơn chuyển sang PAID, ghi nhận thời điểm thanh toán",
                "1. Lễ tân vào mục Hóa đơn viện phí (/staff/billing)\n2. Tìm kiếm hóa đơn theo mã lịch hẹn hoặc tên bệnh nhân\n3. Lễ tân chọn phương thức thanh toán: Tiền mặt tại quầy (CASH) hoặc Cổng VNPAY\n4. Thu tiền từ bệnh nhân và bấm Xác nhận thanh toán\n5. Hệ thống cập nhật trạng thái hóa đơn sang PAID, lưu thời điểm paidAt\n6. Hiển thị nút In hóa đơn PDF",
                "3a. Bệnh nhân chọn thanh toán online qua VNPay Sandbox\n3a1. Quét mã QR thanh toán hoặc nhập thông tin thẻ test\n3a2. Cổng thanh toán gửi IPN xác nhận thành công",
                "N/A");

        addUseCaseTable(sb, "UC_020", "Xuất Hóa đơn viện phí ra file PDF", "Xuất và in phiếu thanh toán hóa đơn viện phí định dạng PDF",
                "Lễ tân, Bệnh nhân, Quản trị viên", "Hóa đơn đã được tạo",
                "Tải về file PDF hóa đơn tài chính của phòng khám",
                "1. Bấm nút In Hóa Đơn PDF trên giao diện hóa đơn\n2. Hệ thống biên dịch thông tin chi phí khám, chi phí thuốc, tổng tiền, phương thức thanh toán\n3. Trả về file PDF phiếu thu viện phí",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_021", "Đánh giá & Phản hồi bác sĩ (Review & Rating)", "Bệnh nhân chấm sao và nhận xét chất lượng khám sau khi hoàn tất",
                "Bệnh nhân (ROLE_PATIENT)", "Ca khám đã hoàn tất (status = COMPLETED)",
                "Đánh giá được ghi nhận và cập nhật vào điểm trung bình của bác sĩ",
                "1. Bệnh nhân vào trang Chi tiết lịch hẹn đã khám\n2. Bấm nút Đánh giá bác sĩ\n3. Chọn số sao đánh giá (từ 1 đến 5 sao)\n4. Nhập nội dung nhận xét, góp ý về thái độ và chuyên môn của bác sĩ\n5. Bấm Gửi đánh giá\n6. Hệ thống lưu đánh giá và cập nhật hiển thị trên trang thông tin bác sĩ",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_022", "Quản lý Chuyên khoa (Specialty Management)", "Admin thêm, sửa, xem danh mục các chuyên khoa y tế",
                "Quản trị viên (ROLE_ADMIN)", "Đã đăng nhập tài khoản ADMIN",
                "Danh mục chuyên khoa được cập nhật trong CSDL",
                "1. Admin vào trang Quản lý chuyên khoa (/admin/specialties)\n2. Hệ thống hiển thị bảng danh sách các chuyên khoa\n3. Bấm Thêm chuyên khoa mới\n4. Nhập tên chuyên khoa, mô tả chi tiết và icon Bootstrap\n5. Bấm Lưu chuyên khoa\n6. Chuyên khoa mới xuất hiện trên hệ thống",
                "3a. Bấm nút Sửa tại chuyên khoa muốn chỉnh sửa\n3a1. Cập nhật thông tin và bấm Lưu thay đổi\n3b. Bấm Bật/Tắt trạng thái hoạt động",
                "4b. Tên chuyên khoa bị trùng\n4b1. Báo lỗi tên chuyên khoa đã tồn tại");

        addUseCaseTable(sb, "UC_023", "Quản lý Bác sĩ & Phân lịch trực (Doctor Management)", "Admin quản lý hồ sơ chuyên môn bác sĩ và thiết lập lịch làm việc",
                "Quản trị viên (ROLE_ADMIN)", "Đã đăng nhập tài khoản ADMIN",
                "Hồ sơ bác sĩ và lịch trực được cập nhật",
                "1. Admin vào mục Quản lý bác sĩ (/admin/doctors)\n2. Xem danh sách các bác sĩ và chuyên khoa trực thuộc\n3. Bấm Thêm bác sĩ mới hoặc chỉnh sửa bác sĩ hiện có\n4. Cập nhật học hàm, kinh nghiệm, chuyên khoa phụ trách, phí khám tư vấn và tiểu sử\n5. Bấm Lưu thông tin bác sĩ\n6. Hệ thống tự động kích hoạt sinh lịch trực ca sáng và ca chiều trong 14 ngày",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_024", "Quản lý Kho dược phẩm & Thuốc (Medicine Management)", "Admin quản lý danh mục thuốc, giá bán và số lượng tồn kho",
                "Quản trị viên (ROLE_ADMIN)", "Đã đăng nhập tài khoản ADMIN",
                "Kho thuốc được cập nhật số lượng và đơn giá",
                "1. Admin vào mục Quản lý thuốc (/admin/medicines)\n2. Xem danh sách thuốc, đơn vị tính, đơn giá bán và số lượng tồn kho\n3. Bấm Thêm thuốc mới\n4. Nhập tên thuốc, đơn vị (viên, vỉ, hộp), đơn giá, hướng dẫn sử dụng và số lượng nhập kho\n5. Bấm Lưu thuốc\n6. Thuốc mới sẵn sàng để bác sĩ kê đơn trong phòng khám",
                "3a. Bấm nút Cập nhật số lượng tồn kho\n3a1. Nhập số lượng nhập thêm và bấm Lưu",
                "N/A");

        addUseCaseTable(sb, "UC_025", "Quản lý Tài khoản người dùng (User Management)", "Admin xem danh sách người dùng và khóa/mở khóa tài khoản",
                "Quản trị viên (ROLE_ADMIN)", "Đã đăng nhập tài khoản ADMIN",
                "Trạng thái tài khoản người dùng được cập nhật",
                "1. Admin vào mục Quản lý người dùng (/admin/users)\n2. Hệ thống hiển thị danh sách toàn bộ tài khoản (Patient, Doctor, Staff, Admin)\n3. Tìm kiếm người dùng theo họ tên, email hoặc số điện thoại\n4. Bấm nút Khóa / Mở khóa tài khoản tại người dùng tương ứng\n5. Trạng thái isActive của tài khoản được cập nhật tức thì",
                "N/A", "N/A");

        addUseCaseTable(sb, "UC_026", "Thống kê Doanh thu & KPI Phòng khám (Revenue Statistics)", "Admin theo dõi báo cáo doanh thu và phân bổ ca khám thời gian thực",
                "Quản trị viên (ROLE_ADMIN)", "Đã đăng nhập tài khoản ADMIN",
                "Hiển thị các chỉ số KPI và biểu đồ Chart.js trực quan",
                "1. Admin vào Dashboard quản trị (/admin/dashboard)\n2. Hệ thống tải dữ liệu thống kê tổng hợp: tổng bệnh nhân, số ca khám hôm nay, tổng doanh thu thực thu\n3. Hiển thị Biểu đồ tròn: tỷ lệ ca khám phân bổ theo chuyên khoa (Tim mạch, Nhi, Da liễu...)\n4. Hiển thị Biểu đồ cột: tỷ lệ trạng thái lịch hẹn (Hoàn tất, Chờ khám, Đã hủy)\n5. Hiển thị Bảng danh sách các hóa đơn viện phí gần đây nhất",
                "N/A", "N/A");

        sb.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>");

        // 3.3. Biểu đồ Tuần tự
        sb.append(p("3.3. Biểu Đồ Tuần Tự (Sequence Diagrams - Boundary, Control, Entity)", "Heading2", true, 14, "left"));
        sb.append(p("Các biểu đồ tuần tự dưới đây biểu diễn chi tiết luồng tương tác giữa các thành phần theo mô hình chuẩn BCE (Boundary: Giao diện Form, Control: Lớp Service/Xử lý nghiệp vụ, Entity: Tầng Dữ liệu/CSDL).", null, false, 13, "both"));

        sb.append(p("3.3.1. Sơ đồ Tuần tự: Đăng ký tài khoản (Sign up)", "Heading3", true, 13, "left"));
        sb.append(p("• Tác nhân (Actor): Khách (Guest)\n" +
                   "• Boundary (Giao diện): Form Đăng ký (/register)\n" +
                   "• Control (Xử lý): AuthController -> UserServiceImpl -> BCryptPasswordEncoder\n" +
                   "• Entity (Dữ liệu): UserRepository, RoleRepository -> Bảng users, roles\n" +
                   "Luồng thông điệp: Khách gửi thông tin đăng ký -> Form gửi POST /register -> UserServiceImpl kiểm tra trùng lặp username/email -> Mã hóa mật khẩu BCrypt -> UserRepository.save() -> Trả về thông báo thành công.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        sb.append(p("3.3.2. Sơ đồ Tuần tự: Đăng nhập hệ thống (Sign in)", "Heading3", true, 13, "left"));
        sb.append(p("• Tác nhân (Actor): Người dùng (Bệnh nhân, Bác sĩ, Lễ tân, Admin)\n" +
                   "• Boundary (Giao diện): Form Đăng nhập (/login)\n" +
                   "• Control (Xử lý): SecurityFilterChain -> DaoAuthenticationProvider -> CustomUserDetailsService\n" +
                   "• Entity (Dữ liệu): UserRepository -> Bảng users, roles\n" +
                   "Luồng thông điệp: Người dùng nhập username, password -> Spring Security nạp UserDetails -> Kiểm tra BCrypt password -> Khởi tạo Authentication Token -> AuthenticationSuccessHandler chuyển hướng đúng trang vai trò.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        sb.append(p("3.3.3. Sơ đồ Tuần tự: Đặt lịch khám & Khóa đồng thời (Optimistic Lock Booking)", "Heading3", true, 13, "left"));
        sb.append(p("• Tác nhân (Actor): Bệnh nhân (Patient)\n" +
                   "• Boundary (Giao diện): Form Đặt lịch (/patient/book-appointment)\n" +
                   "• Control (Xử lý): PatientAppointmentController -> AppointmentServiceImpl -> EmailService (Async)\n" +
                   "• Entity (Dữ liệu): TimeSlotRepository, AppointmentRepository -> Bảng time_slots (@Version), appointments\n" +
                   "Luồng thông điệp: Bệnh nhân chọn Slot ID=100 -> Giao dịch bắt đầu (@Transactional) -> Đọc TimeSlot (version=0) -> Kiểm tra slot.status == AVAILABLE -> Cập nhật status = BOOKED (tự động tăng version lên 1) -> Lưu Appointment (status=CONFIRMED) -> Kích hoạt @Async gửi email xác nhận -> Redirect trang chi tiết lịch hẹn kèm mã QR Base64.\n" +
                   "Nếu 2 người cùng bấm đặt cùng 1 giây: Giao dịch thứ 2 gặp xung đột version -> Ném OptimisticLockingFailureException -> Bắt lỗi và thông báo thân thiện tới người dùng chọn slot khác.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        sb.append(p("3.3.4. Sơ đồ Tuần tự: Quẹt QR Check-in & Cấp STT tiếp nhận", "Heading3", true, 13, "left"));
        sb.append(p("• Tác nhân (Actor): Lễ tân (Staff)\n" +
                   "• Boundary (Giao diện): Giao diện Quầy tiếp nhận (/staff/reception)\n" +
                   "• Control (Xử lý): StaffReceptionController -> AppointmentServiceImpl\n" +
                   "• Entity (Dữ liệu): AppointmentRepository -> Bảng appointments\n" +
                   "Luồng thông điệp: Lễ tân quét QR hoặc nhập mã đặt lịch -> AppointmentService.checkInAppointment() -> Tính số thứ tự trong ngày: STT = countTodayQueue() + 1 -> Gán queueNumber = 'STT-01' -> Cập nhật status = CHECKED_IN -> Bệnh nhân xuất hiện trên hàng chờ bàn khám bác sĩ.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        sb.append(p("3.3.5. Sơ đồ Tuần tự: Khám bệnh, Kê đơn thuốc & Trừ tồn kho dược", "Heading3", true, 13, "left"));
        sb.append(p("• Tác nhân (Actor): Bác sĩ (Doctor)\n" +
                   "• Boundary (Giao diện): Bàn khám bác sĩ (/doctor/examine/{id})\n" +
                   "• Control (Xử lý): DoctorDeskController -> MedicalRecordServiceImpl -> InvoiceServiceImpl\n" +
                   "• Entity (Dữ liệu): MedicalRecordRepository, MedicineRepository, PrescriptionRepository, InvoiceRepository\n" +
                   "Luồng thông điệp: Bác sĩ nhập chẩn đoán + Thêm thuốc kê đơn -> Giao dịch lưu trữ: Kiểm tra tồn kho thuốc -> Trừ stockQuantity trong bảng medicines -> Lưu MedicalRecord & Prescription -> Cập nhật Appointment status = COMPLETED -> Gọi InvoiceService tạo hóa đơn tự động (totalAmount = công khám + tiền thuốc) -> Lưu Invoice (status = UNPAID) -> Bác sĩ bấm in Đơn thuốc PDF qua OpenPDF.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));

        sb.append(p("3.3.6. Sơ đồ Tuần tự: Thu viện phí & Xuất hóa đơn tài chính PDF", "Heading3", true, 13, "left"));
        sb.append(p("• Tác nhân (Actor): Lễ tân (Staff)\n" +
                   "• Boundary (Giao diện): Giao diện Thu ngân (/staff/billing)\n" +
                   "• Control (Xử lý): StaffReceptionController -> InvoiceServiceImpl -> PdfExportService\n" +
                   "• Entity (Dữ liệu): InvoiceRepository -> Bảng invoices\n" +
                   "Luồng thông điệp: Lễ tân chọn hóa đơn ca khám hoàn tất -> Chọn phương thức (CASH/VNPAY) -> Bấm Xác nhận thanh toán -> Cập nhật Invoice status = PAID, paidAt = now() -> PdfExportService biên dịch phiếu thu viện phí định dạng PDF trả về cho bệnh nhân.", null, false, 12, "both"));
        sb.append(p("", null, false, 12, "left"));
        sb.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>");

        // 3.4 & 3.5. Thiết kế Cơ sở dữ liệu chi tiết
        sb.append(p("3.4. Thiết Kế Cơ Sở Dữ Liệu Chi Tiết (Data Dictionary 3NF)", "Heading2", true, 14, "left"));
        sb.append(p("Toàn bộ cơ sở dữ liệu của MedClinic được thiết kế đạt Dạng chuẩn 3 (3NF), loại bỏ hoàn toàn các phụ thuộc hàm bộ phận và bắc cầu. Dưới đây là từ điển dữ liệu chi tiết của 13 bảng:", null, false, 13, "both"));

        // Bảng 1: users
        sb.append(p("3.4.1. Bảng users (Tài khoản người dùng)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính tự tăng\n+ primary-key\n+ auto-generated", "BIGINT"));
        sb.append(tableRow(false, "2", "username", "Tên đăng nhập hệ thống\n+ required\n+ unique\n+ maxLength 50", "VARCHAR(50)"));
        sb.append(tableRow(false, "3", "password", "Mật khẩu băm bằng thuật toán BCrypt\n+ required\n+ hash", "VARCHAR(255)"));
        sb.append(tableRow(false, "4", "full_name", "Họ và tên đầy đủ của người dùng\n+ required\n+ maxLength 100", "VARCHAR(100)"));
        sb.append(tableRow(false, "5", "email", "Địa chỉ email liên hệ\n+ required\n+ unique\n+ maxLength 100", "VARCHAR(100)"));
        sb.append(tableRow(false, "6", "phone", "Số điện thoại di động\n+ maxLength 20", "VARCHAR(20)"));
        sb.append(tableRow(false, "7", "gender", "Giới tính (MALE, FEMALE, OTHER)", "VARCHAR(10)"));
        sb.append(tableRow(false, "8", "date_of_birth", "Ngày tháng năm sinh", "DATE"));
        sb.append(tableRow(false, "9", "address", "Địa chỉ cư trú", "VARCHAR(255)"));
        sb.append(tableRow(false, "10", "role_id", "Khóa ngoại tham chiếu bảng roles\n+ required\n+ ref: roles(id)", "BIGINT"));
        sb.append(tableRow(false, "11", "is_active", "Trạng thái kích hoạt tài khoản\n+ default: true", "BOOLEAN"));
        sb.append(tableRow(false, "12", "created_at", "Thời điểm tạo tài khoản\n+ auto-generated", "DATETIME"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // Bảng 2: roles
        sb.append(p("3.4.2. Bảng roles (Vai trò người dùng)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính tự tăng\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "name", "Tên vai trò (ROLE_PATIENT, ROLE_DOCTOR, ROLE_STAFF, ROLE_ADMIN)\n+ required\n+ unique", "VARCHAR(50)"));
        sb.append(tableRow(false, "3", "description", "Mô tả chức năng vai trò", "VARCHAR(255)"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // Bảng 3: specialties
        sb.append(p("3.4.3. Bảng specialties (Chuyên khoa khám bệnh)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính chuyên khoa\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "name", "Tên chuyên khoa y tế (Tim mạch, Nhi, Da liễu...)\n+ required\n+ unique", "VARCHAR(100)"));
        sb.append(tableRow(false, "3", "description", "Mô tả chi tiết phạm vi khám chữa bệnh", "TEXT"));
        sb.append(tableRow(false, "4", "icon", "Mã class Bootstrap icon hiển thị", "VARCHAR(100)"));
        sb.append(tableRow(false, "5", "is_active", "Trạng thái hoạt động\n+ default: true", "BOOLEAN"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // Bảng 4: doctor_profiles
        sb.append(p("3.4.4. Bảng doctor_profiles (Hồ sơ chuyên môn Bác sĩ)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính hồ sơ bác sĩ\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "user_id", "Khóa ngoại liên kết tài khoản user\n+ required\n+ unique\n+ ref: users(id)", "BIGINT"));
        sb.append(tableRow(false, "3", "specialty_id", "Khóa ngoại chuyên khoa phụ trách\n+ required\n+ ref: specialties(id)", "BIGINT"));
        sb.append(tableRow(false, "4", "title", "Học hàm học vị (PGS.TS, ThS.BS, BS.CKII)\n+ required", "VARCHAR(50)"));
        sb.append(tableRow(false, "5", "experience_years", "Số năm kinh nghiệm làm việc\n+ required", "INT"));
        sb.append(tableRow(false, "6", "consultation_fee", "Giá tiền công khám tư vấn cơ bản (VNĐ)\n+ required", "DECIMAL(12,2)"));
        sb.append(tableRow(false, "7", "bio", "Tiểu sử quá trình đào tạo và công tác", "TEXT"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // Bảng 5: doctor_schedules
        sb.append(p("3.4.5. Bảng doctor_schedules (Lịch làm việc ca của Bác sĩ)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính lịch trực\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "doctor_id", "Bác sĩ trực khám\n+ required\n+ ref: doctor_profiles(id)", "BIGINT"));
        sb.append(tableRow(false, "3", "work_date", "Ngày làm việc trực tế\n+ required", "DATE"));
        sb.append(tableRow(false, "4", "shift", "Ca làm việc (MORNING, AFTERNOON)\n+ required", "VARCHAR(20)"));
        sb.append(tableRow(false, "5", "max_patients", "Số bệnh nhân tối đa trong ca\n+ default: 8", "INT"));
        sb.append("</w:tbl>");
        sb.append(p("Ràng buộc toàn vẹn: UNIQUE(doctor_id, work_date, shift) đảm bảo mỗi bác sĩ chỉ có tối đa 1 ca sáng và 1 ca chiều trong cùng 1 ngày.", null, false, 11, "left"));
        sb.append(p("", null, false, 12, "left"));

        // Bảng 6: time_slots
        sb.append(p("3.4.6. Bảng time_slots (Khung giờ khám 30 phút & Concurrency Lock)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính khung giờ\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "schedule_id", "Ca làm việc chứa khung giờ này\n+ required\n+ ref: doctor_schedules(id)", "BIGINT"));
        sb.append(tableRow(false, "3", "start_time", "Giờ bắt đầu slot (VD: 08:00:00)\n+ required", "TIME"));
        sb.append(tableRow(false, "4", "end_time", "Giờ kết thúc slot (VD: 08:30:00)\n+ required", "TIME"));
        sb.append(tableRow(false, "5", "status", "Trạng thái slot (AVAILABLE, BOOKED, BLOCKED)\n+ required", "VARCHAR(20)"));
        sb.append(tableRow(false, "6", "version", "Trường phiên bản khóa lạc quan (Optimistic Lock)\n+ required\n+ default: 0", "BIGINT"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // Bảng 7: appointments
        sb.append(p("3.4.7. Bảng appointments (Lịch hẹn khám bệnh)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính ca khám\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "booking_code", "Mã đặt lịch duy nhất (VD: MED-260905-XXXX)\n+ required\n+ unique\n+ index", "VARCHAR(30)"));
        sb.append(tableRow(false, "3", "patient_id", "Bệnh nhân đặt lịch\n+ required\n+ ref: users(id)", "BIGINT"));
        sb.append(tableRow(false, "4", "doctor_id", "Bác sĩ phụ trách\n+ required\n+ ref: doctor_profiles(id)", "BIGINT"));
        sb.append(tableRow(false, "5", "time_slot_id", "Khung giờ khám đã đặt\n+ required\n+ unique\n+ ref: time_slots(id)", "BIGINT"));
        sb.append(tableRow(false, "6", "appointment_date", "Ngày khám thực tế\n+ required\n+ index", "DATE"));
        sb.append(tableRow(false, "7", "reason", "Lý do khám và mô tả triệu chứng ban đầu", "TEXT"));
        sb.append(tableRow(false, "8", "status", "Trạng thái (CONFIRMED, CHECKED_IN, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW)\n+ required", "VARCHAR(30)"));
        sb.append(tableRow(false, "9", "queue_number", "Số thứ tự tiếp nhận trong ngày (VD: STT-01)", "VARCHAR(20)"));
        sb.append(tableRow(false, "10", "created_at", "Thời điểm đặt lịch hẹn\n+ auto-generated", "DATETIME"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // Bảng 8: medical_records
        sb.append(p("3.4.8. Bảng medical_records (Hồ sơ bệnh án điện tử)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính bệnh án\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "appointment_id", "Ca hẹn khám tương ứng\n+ required\n+ unique\n+ ref: appointments(id)", "BIGINT"));
        sb.append(tableRow(false, "3", "doctor_id", "Bác sĩ khám\n+ required\n+ ref: doctor_profiles(id)", "BIGINT"));
        sb.append(tableRow(false, "4", "patient_id", "Bệnh nhân\n+ required\n+ ref: users(id)", "BIGINT"));
        sb.append(tableRow(false, "5", "symptoms", "Triệu chứng lâm sàng ghi nhận\n+ required", "TEXT"));
        sb.append(tableRow(false, "6", "diagnosis", "Kết luận chẩn đoán y khoa\n+ required", "TEXT"));
        sb.append(tableRow(false, "7", "doctor_notes", "Lời dặn dò, chế độ ăn uống, kiêng cử", "TEXT"));
        sb.append(tableRow(false, "8", "re_exam_date", "Ngày hẹn tái khám", "DATE"));
        sb.append(tableRow(false, "9", "created_at", "Thời điểm hoàn thành khám\n+ auto-generated", "DATETIME"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // Bảng 9: medicines
        sb.append(p("3.4.9. Bảng medicines (Danh mục thuốc & Kho dược)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính thuốc\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "name", "Tên thuốc biệt dược / hoạt chất\n+ required\n+ unique", "VARCHAR(150)"));
        sb.append(tableRow(false, "3", "unit", "Đơn vị tính (Viên, Vỉ, Hộp, Chai, Gói)\n+ required", "VARCHAR(30)"));
        sb.append(tableRow(false, "4", "unit_price", "Đơn giá bán lẻ (VNĐ)\n+ required\n+ min: 0", "DECIMAL(12,2)"));
        sb.append(tableRow(false, "5", "usage_guide", "Hướng dẫn sử dụng cơ bản", "VARCHAR(255)"));
        sb.append(tableRow(false, "6", "stock_quantity", "Số lượng tồn kho\n+ required\n+ default: 1000", "INT"));
        sb.append(tableRow(false, "7", "is_active", "Trạng thái kinh doanh\n+ default: true", "BOOLEAN"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // Bảng 10 & 11: prescriptions & prescription_items
        sb.append(p("3.4.10. Bảng prescriptions & prescription_items (Đơn thuốc & Chi tiết toa thuốc)", "Heading3", true, 13, "left"));
        sb.append(p("Bảng prescriptions: id (PK), medical_record_id (FK unique), doctor_notes (TEXT), created_at (DATETIME).", null, false, 12, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field (prescription_items)", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính chi tiết toa thuốc\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "prescription_id", "Khóa ngoại đơn thuốc\n+ required\n+ ref: prescriptions(id)", "BIGINT"));
        sb.append(tableRow(false, "3", "medicine_id", "Thuốc được kê\n+ required\n+ ref: medicines(id)", "BIGINT"));
        sb.append(tableRow(false, "4", "quantity", "Số lượng thuốc kê đơn\n+ required\n+ min: 1", "INT"));
        sb.append(tableRow(false, "5", "dosage", "Liều lượng uống mỗi lần (VD: 1 viên / lần)\n+ required", "VARCHAR(100)"));
        sb.append(tableRow(false, "6", "frequency", "Tần suất uống trong ngày (VD: 2 lần / ngày)\n+ required", "VARCHAR(100)"));
        sb.append(tableRow(false, "7", "instructions", "Cách dùng chi tiết (VD: Uống sau khi ăn no)", "VARCHAR(255)"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // Bảng 12: invoices
        sb.append(p("3.4.11. Bảng invoices (Hóa đơn thanh toán viện phí)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính hóa đơn\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "appointment_id", "Ca khám được lập hóa đơn\n+ required\n+ unique\n+ ref: appointments(id)", "BIGINT"));
        sb.append(tableRow(false, "3", "invoice_code", "Mã hóa đơn tài chính (VD: INV-260905-XXXX)\n+ required\n+ unique\n+ index", "VARCHAR(30)"));
        sb.append(tableRow(false, "4", "consultation_fee", "Tiền công khám bác sĩ\n+ required", "DECIMAL(12,2)"));
        sb.append(tableRow(false, "5", "medicine_fee", "Tổng tiền thuốc trong đơn\n+ default: 0", "DECIMAL(12,2)"));
        sb.append(tableRow(false, "6", "total_amount", "Tổng tiền thanh toán (công khám + tiền thuốc)\n+ required", "DECIMAL(12,2)"));
        sb.append(tableRow(false, "7", "payment_method", "Phương thức thanh toán (CASH, VNPAY)\n+ required", "VARCHAR(20)"));
        sb.append(tableRow(false, "8", "payment_status", "Trạng thái thanh toán (UNPAID, PAID, REFUNDED)\n+ required", "VARCHAR(20)"));
        sb.append(tableRow(false, "9", "paid_at", "Thời điểm xác nhận thanh toán", "DATETIME"));
        sb.append(tableRow(false, "10", "created_at", "Thời điểm lập hóa đơn\n+ auto-generated", "DATETIME"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        // Bảng 13: notifications
        sb.append(p("3.4.12. Bảng notifications (Thông báo người dùng)", "Heading3", true, 13, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(true, "STT", "Tên field", "Mô tả", "Kiểu dữ liệu"));
        sb.append(tableRow(false, "1", "id", "Khóa chính thông báo\n+ primary-key", "BIGINT"));
        sb.append(tableRow(false, "2", "user_id", "Người dùng nhận thông báo\n+ required\n+ ref: users(id)", "BIGINT"));
        sb.append(tableRow(false, "3", "title", "Tiêu đề thông báo\n+ required", "VARCHAR(150)"));
        sb.append(tableRow(false, "4", "content", "Nội dung chi tiết thông báo\n+ required", "TEXT"));
        sb.append(tableRow(false, "5", "type", "Loại thông báo (BOOKING, QUEUE, INVOICE, SYSTEM)", "VARCHAR(50)"));
        sb.append(tableRow(false, "6", "is_read", "Đã đọc hay chưa\n+ default: false", "BOOLEAN"));
        sb.append(tableRow(false, "7", "created_at", "Thời điểm tạo thông báo\n+ auto-generated", "DATETIME"));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 12, "left"));

        sb.append("</w:body></w:document>");
        return sb.toString();
    }

    private static void addUseCaseTable(StringBuilder sb, String id, String name, String goal, String actors,
                                        String pre, String post, String mainFlow, String alt, String exc) {
        sb.append(p("3.2." + id.replace("UC_", "") + ". " + name, "Heading3", true, 12, "left"));
        sb.append("<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>");
        sb.append(tableRow(false, "Use case ID", id));
        sb.append(tableRow(false, "Name", name));
        sb.append(tableRow(false, "Goal", goal));
        sb.append(tableRow(false, "Actors", actors));
        sb.append(tableRow(false, "Pre-conditions", pre));
        sb.append(tableRow(false, "Post-conditions", post));
        sb.append(tableRow(false, "Main Flow", mainFlow));
        sb.append(tableRow(false, "Alternative", alt));
        sb.append(tableRow(false, "Exception", exc));
        sb.append("</w:tbl>");
        sb.append(p("", null, false, 10, "left"));
    }

    // --- 1. FILE XML CHUẨN XMI 1.1 / UML 1.3 (ĐỊNH DẠNG TƯƠNG THÍCH CAO NHẤT MỌI BẢN EA) ---
    private static String buildEnterpriseArchitectXmi11() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<XMI xmi.version=\"1.1\" xmlns:UML=\"omg.org/UML1.3\" timestamp=\"2026-09-05 15:00:00\">\n");
        sb.append("\t<XMI.header>\n");
        sb.append("\t\t<XMI.documentation>\n");
        sb.append("\t\t\t<XMI.exporter>Enterprise Architect</XMI.exporter>\n");
        sb.append("\t\t\t<XMI.exporterVersion>6.5</XMI.exporterVersion>\n");
        sb.append("\t\t</XMI.documentation>\n");
        sb.append("\t</XMI.header>\n");
        sb.append("\t<XMI.content>\n");
        sb.append("\t\t<UML:Model name=\"EA Model\" xmi.id=\"MX_EAID_00000000_1111_4444_8888_000000000000\">\n");
        sb.append("\t\t\t<UML:Namespace.ownedElement>\n");
        sb.append("\t\t\t\t<UML:Class name=\"EARootClass\" xmi.id=\"EAID_11111111_5487_4080_9756_E62EB21E4233\" isRoot=\"true\" isLeaf=\"false\" isAbstract=\"false\"/>\n");
        sb.append("\t\t\t\t<UML:Package name=\"MedClinic System\" xmi.id=\"EAPK_00000001_1111_4444_8888_000000000001\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\" visibility=\"public\">\n");
        sb.append("\t\t\t\t\t<UML:Namespace.ownedElement>\n");

        // Gói 1: Use Case Model
        sb.append("\t\t\t\t\t\t<UML:Package name=\"Use Case Model\" xmi.id=\"EAPK_00000002_2222_4444_8888_000000000002\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\" visibility=\"public\">\n");
        sb.append("\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_stype\" value=\"Package\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"package\" value=\"EAPK_00000001_1111_4444_8888_000000000001\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_eleType\" value=\"package\"/>\n");
        sb.append("\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t\t\t\t<UML:Namespace.ownedElement>\n");

        // 5 Actors (Định danh riêng EAID_ACT000xx để không bao giờ trùng với Attribute ID)
        String[][] actors = {
                {"01", "Khách (Guest)"},
                {"02", "Bệnh nhân (Patient)"},
                {"03", "Bác sĩ (Doctor)"},
                {"04", "Lễ tân (Staff)"},
                {"05", "Quản trị viên (Admin)"}
        };
        for (String[] act : actors) {
            String aId = "EAID_ACT000" + act[0] + "_1111_4444_8888_0000000000" + act[0];
            sb.append("\t\t\t\t\t\t\t\t<UML:Actor name=\"").append(escapeXml(act[1])).append("\" xmi.id=\"").append(aId)
                    .append("\" visibility=\"public\" namespace=\"EAPK_00000002_2222_4444_8888_000000000002\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\">\n");
            sb.append("\t\t\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"isSpecification\" value=\"false\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_stype\" value=\"Actor\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_ntype\" value=\"0\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"version\" value=\"1.0\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"package\" value=\"EAPK_00000002_2222_4444_8888_000000000002\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"date_created\" value=\"2026-09-05 15:00:00\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"date_modified\" value=\"2026-09-05 15:00:00\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"gentype\" value=\"&lt;none&gt;\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"package_name\" value=\"Use Case Model\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_eleType\" value=\"element\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
            sb.append("\t\t\t\t\t\t\t\t</UML:Actor>\n");
        }

        // 26 Use Cases
        String[][] ucs = getUseCases();
        for (String[] uc : ucs) {
            String uId = "EAID_U00000" + uc[0] + "_1111_4444_8888_0000000000" + uc[0];
            sb.append("\t\t\t\t\t\t\t\t<UML:UseCase name=\"").append(escapeXml(uc[1])).append("\" xmi.id=\"").append(uId)
                    .append("\" visibility=\"public\" namespace=\"EAPK_00000002_2222_4444_8888_000000000002\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\">\n");
            sb.append("\t\t\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"isSpecification\" value=\"false\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_stype\" value=\"UseCase\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_ntype\" value=\"0\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"version\" value=\"1.0\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"package\" value=\"EAPK_00000002_2222_4444_8888_000000000002\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"date_created\" value=\"2026-09-05 15:00:00\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"date_modified\" value=\"2026-09-05 15:00:00\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"gentype\" value=\"&lt;none&gt;\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"package_name\" value=\"Use Case Model\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_eleType\" value=\"element\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
            sb.append("\t\t\t\t\t\t\t\t</UML:UseCase>\n");
        }

        // Tự động thêm toàn bộ 39 đường kết nối (Associations) giữa Actor và Use Case
        String[][] ucAssocs = getUseCaseAssociations();
        for (int i = 0; i < ucAssocs.length; i++) {
            String[] asc = ucAssocs[i];
            String assocId = "EAID_ASU000" + String.format("%02d", i + 1) + "_1111_4444_8888_0000000000" + String.format("%02d", i + 1);
            String srcId = "EAID_ACT000" + asc[0] + "_1111_4444_8888_0000000000" + asc[0];
            String tgtId = "EAID_U00000" + asc[1] + "_1111_4444_8888_0000000000" + asc[1];
            appendAssociation11(sb, assocId, "", "Unspecified", srcId, asc[2], "Actor", "", tgtId, asc[3], "UseCase", "", "");
        }

        sb.append("\t\t\t\t\t\t\t</UML:Namespace.ownedElement>\n");
        sb.append("\t\t\t\t\t\t</UML:Package>\n");

        // Gói 2: Domain Class Model (3NF)
        sb.append("\t\t\t\t\t\t<UML:Package name=\"Domain Class Model (3NF)\" xmi.id=\"EAPK_00000003_3333_4444_8888_000000000003\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\" visibility=\"public\">\n");
        sb.append("\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_stype\" value=\"Package\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"package\" value=\"EAPK_00000001_1111_4444_8888_000000000001\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_eleType\" value=\"package\"/>\n");
        sb.append("\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t\t\t\t<UML:Namespace.ownedElement>\n");
        String[][] classes = getClasses();
        for (String[] cls : classes) {
            String cId = "EAID_C00000" + cls[0] + "_1111_4444_8888_0000000000" + cls[0];
            sb.append("\t\t\t\t\t\t\t\t<UML:Class name=\"").append(cls[1]).append("\" xmi.id=\"").append(cId)
                    .append("\" visibility=\"public\" namespace=\"EAPK_00000003_3333_4444_8888_000000000003\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\">\n");
            sb.append("\t\t\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"isSpecification\" value=\"false\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_stype\" value=\"Class\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_ntype\" value=\"0\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"version\" value=\"1.0\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"package\" value=\"EAPK_00000003_3333_4444_8888_000000000003\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"date_created\" value=\"2026-09-05 15:00:00\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"date_modified\" value=\"2026-09-05 15:00:00\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"gentype\" value=\"Java\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"package_name\" value=\"Domain Class Model (3NF)\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_eleType\" value=\"element\"/>\n");
            sb.append("\t\t\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
            sb.append("\t\t\t\t\t\t\t\t\t<UML:Classifier.feature>\n");
            String[] attrs = cls[2].split(", ");
            for (int i = 0; i < attrs.length; i++) {
                String[] parts = attrs[i].split(": ");
                String aName = parts[0];
                String aType = parts.length > 1 ? parts[1] : "String";
                String aId = "EAID_ATT" + cls[0] + "_" + String.format("%04d", i) + "_1111_4444_8888_" + String.format("%012d", i);
                sb.append("\t\t\t\t\t\t\t\t\t\t<UML:Attribute name=\"").append(escapeXml(aName)).append("\" xmi.id=\"").append(aId).append("\" visibility=\"private\">\n");
                sb.append("\t\t\t\t\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
                sb.append("\t\t\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"type\" value=\"").append(escapeXml(aType)).append("\"/>\n");
                sb.append("\t\t\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_guid\" value=\"{").append(String.format("ATT%s00-1111-4444-8888-%012d", cls[0], i)).append("}\"/>\n");
                sb.append("\t\t\t\t\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
                sb.append("\t\t\t\t\t\t\t\t\t\t</UML:Attribute>\n");
            }
            sb.append("\t\t\t\t\t\t\t\t\t</UML:Classifier.feature>\n");
            sb.append("\t\t\t\t\t\t\t\t</UML:Class>\n");
        }

        // Tự động thêm toàn bộ 14 đường quan hệ (Relationships/Associations) giữa các Lớp Thực thể
        String[][] clsAssocs = getClassAssociations();
        for (int i = 0; i < clsAssocs.length; i++) {
            String[] asc = clsAssocs[i];
            String assocId = "EAID_ASC000" + String.format("%02d", i + 1) + "_1111_4444_8888_0000000000" + String.format("%02d", i + 1);
            String srcId = "EAID_C00000" + asc[0] + "_1111_4444_8888_0000000000" + asc[0];
            String tgtId = "EAID_C00000" + asc[1] + "_1111_4444_8888_0000000000" + asc[1];
            appendAssociation11(sb, assocId, asc[7], "Source -> Destination", srcId, asc[2], "Class", asc[4], tgtId, asc[3], "Class", asc[5], asc[6]);
        }

        sb.append("\t\t\t\t\t\t\t</UML:Namespace.ownedElement>\n");
        sb.append("\t\t\t\t\t\t</UML:Package>\n");

        sb.append("\t\t\t\t\t</UML:Namespace.ownedElement>\n");
        sb.append("\t\t\t\t</UML:Package>\n");
        sb.append("\t\t\t</UML:Namespace.ownedElement>\n");
        sb.append("\t\t</UML:Model>\n");

        // Diagrams (XMI 1.1)
        sb.append("\t\t<UML:Diagram name=\"Sơ đồ Use Case Tổng quan\" xmi.id=\"EAID_D0000001_1111_4444_8888_000000000001\" diagramType=\"UseCaseDiagram\" owner=\"EAPK_00000002_2222_4444_8888_000000000002\" toolName=\"Enterprise Architect 2.5\">\n");
        sb.append("\t\t\t<UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"version\" value=\"1.0\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"author\" value=\"MedClinic Team\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"created_date\" value=\"2026-09-05 15:00:00\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"modified_date\" value=\"2026-09-05 15:00:00\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"package\" value=\"EAPK_00000002_2222_4444_8888_000000000002\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"type\" value=\"Use Case\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"ea_localid\" value=\"1\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"EAStyle\" value=\"ShowPrivate=1;ShowProtected=1;ShowPublic=1;HideRelationships=0;Locked=0;Border=1;HighlightForeign=1;PackageContents=1;DocSize.cx=1600;DocSize.cy=1300;Orientation=L;Zoom=100;\"/>\n");
        sb.append("\t\t\t</UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t<UML:Diagram.element>\n");
        // 5 Actors đặt hợp lý: Khách, Bệnh nhân, Bác sĩ bên Trái; Lễ tân, Quản trị viên bên Phải
        sb.append("\t\t\t\t<UML:DiagramElement geometry=\"Left=30;Top=80;Right=120;Bottom=170;\" subject=\"EAID_ACT00001_1111_4444_8888_000000000001\" seqno=\"1\"/>\n");
        sb.append("\t\t\t\t<UML:DiagramElement geometry=\"Left=30;Top=340;Right=120;Bottom=430;\" subject=\"EAID_ACT00002_1111_4444_8888_000000000002\" seqno=\"2\"/>\n");
        sb.append("\t\t\t\t<UML:DiagramElement geometry=\"Left=30;Top=690;Right=120;Bottom=780;\" subject=\"EAID_ACT00003_1111_4444_8888_000000000003\" seqno=\"3\"/>\n");
        sb.append("\t\t\t\t<UML:DiagramElement geometry=\"Left=1140;Top=340;Right=1230;Bottom=430;\" subject=\"EAID_ACT00004_1111_4444_8888_000000000004\" seqno=\"4\"/>\n");
        sb.append("\t\t\t\t<UML:DiagramElement geometry=\"Left=1140;Top=715;Right=1230;Bottom=805;\" subject=\"EAID_ACT00005_1111_4444_8888_000000000005\" seqno=\"5\"/>\n");
        int[][] uPos = getUseCasePositions();
        for (int i = 0; i < ucs.length; i++) {
            String uId = "EAID_U00000" + ucs[i][0] + "_1111_4444_8888_0000000000" + ucs[i][0];
            sb.append("\t\t\t\t<UML:DiagramElement geometry=\"Left=").append(uPos[i][0]).append(";Top=").append(uPos[i][1])
                    .append(";Right=").append(uPos[i][2]).append(";Bottom=").append(uPos[i][3])
                    .append(";\" subject=\"").append(uId).append("\" seqno=\"").append(i + 6).append("\"/>\n");
        }
        sb.append("\t\t\t</UML:Diagram.element>\n");
        sb.append("\t\t</UML:Diagram>\n");

        sb.append("\t\t<UML:Diagram name=\"Sơ đồ Lớp Thực thể (Domain Class Diagram 3NF)\" xmi.id=\"EAID_D0000002_1111_4444_8888_000000000002\" diagramType=\"ClassDiagram\" owner=\"EAPK_00000003_3333_4444_8888_000000000003\" toolName=\"Enterprise Architect 2.5\">\n");
        sb.append("\t\t\t<UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"version\" value=\"1.0\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"author\" value=\"MedClinic Team\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"created_date\" value=\"2026-09-05 15:00:00\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"modified_date\" value=\"2026-09-05 15:00:00\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"package\" value=\"EAPK_00000003_3333_4444_8888_000000000003\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"type\" value=\"Logical\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"ea_localid\" value=\"2\"/>\n");
        sb.append("\t\t\t\t<UML:TaggedValue tag=\"EAStyle\" value=\"ShowPrivate=1;ShowProtected=1;ShowPublic=1;HideRelationships=0;Locked=0;Border=1;HighlightForeign=1;PackageContents=1;DocSize.cx=1500;DocSize.cy=1200;Orientation=L;Zoom=100;\"/>\n");
        sb.append("\t\t\t</UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t<UML:Diagram.element>\n");
        int[][] cPos = getClassPositions();
        for (int i = 0; i < classes.length; i++) {
            String cId = "EAID_C00000" + classes[i][0] + "_1111_4444_8888_0000000000" + classes[i][0];
            sb.append("\t\t\t\t<UML:DiagramElement geometry=\"Left=").append(cPos[i][0]).append(";Top=").append(cPos[i][1])
                    .append(";Right=").append(cPos[i][2]).append(";Bottom=").append(cPos[i][3])
                    .append(";\" subject=\"").append(cId).append("\" seqno=\"").append(i + 1).append("\"/>\n");
        }
        sb.append("\t\t\t</UML:Diagram.element>\n");
        sb.append("\t\t</UML:Diagram>\n");

        sb.append("\t</XMI.content>\n");
        sb.append("\t<XMI.difference/>\n");
        sb.append("\t<XMI.extensions xmi.extender=\"Enterprise Architect 2.5\"/>\n");
        sb.append("</XMI>\n");
        return sb.toString();
    }

    private static void appendAssociation11(StringBuilder sb, String assocId, String name, String direction,
                                            String srcId, String srcName, String srcType, String srcMult,
                                            String tgtId, String tgtName, String tgtType, String tgtMult, String roleName) {
        sb.append("\t\t\t\t\t\t\t\t<UML:Association name=\"").append(escapeXml(name)).append("\" xmi.id=\"").append(assocId)
                .append("\" visibility=\"public\" isRoot=\"false\" isLeaf=\"false\" isAbstract=\"false\">\n");
        sb.append("\t\t\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"style\" value=\"3\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_type\" value=\"Association\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"direction\" value=\"").append(direction).append("\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"linemode\" value=\"3\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"line_color\" value=\"-1\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"linewidth\" value=\"0\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"seqno\" value=\"0\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"headStyle\" value=\"0\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"lineStyle\" value=\"0\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_sourceName\" value=\"").append(escapeXml(srcName)).append("\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_targetName\" value=\"").append(escapeXml(tgtName)).append("\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_sourceType\" value=\"").append(srcType).append("\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_targetType\" value=\"").append(tgtType).append("\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_sourceID\" value=\"").append(srcId).append("\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_targetID\" value=\"").append(tgtId).append("\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"privatedata5\" value=\"SX=0;SY=0;EX=0;EY=0;$LLB=;LLT=;LMT=;LMB=;LRT=;LRB=;IRHS=;ILHS=;\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"virtualInheritance\" value=\"0\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t\t\t\t\t\t<UML:Association.connection>\n");
        // Source End
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:AssociationEnd visibility=\"public\" multiplicity=\"").append(srcMult != null ? srcMult : "")
                .append("\" name=\"\" isOrdered=\"false\" isNavigable=\"false\" isAggregate=\"none\" targetScope=\"instance\" changeability=\"changeable\" type=\"").append(srcId).append("\">\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"containment\" value=\"Unspecified\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"sourcestyle\" value=\"Union=0;Derived=0;AllowDuplicates=0;Owned=0;Navigable=Non-Navigable;\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_end\" value=\"source\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t<UML:AssociationEnd.participant>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t<UML:Classifier xmi.idref=\"").append(srcId).append("\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t</UML:AssociationEnd.participant>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t</UML:AssociationEnd>\n");
        // Target End
        sb.append("\t\t\t\t\t\t\t\t\t\t<UML:AssociationEnd visibility=\"public\" multiplicity=\"").append(tgtMult != null ? tgtMult : "")
                .append("\" name=\"").append(roleName != null ? escapeXml(roleName) : "")
                .append("\" isOrdered=\"false\" isNavigable=\"true\" isAggregate=\"none\" targetScope=\"instance\" changeability=\"changeable\" type=\"").append(tgtId).append("\">\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t<UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"containment\" value=\"Unspecified\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"deststyle\" value=\"Union=0;Derived=0;AllowDuplicates=0;Owned=0;Navigable=Navigable;\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t<UML:TaggedValue tag=\"ea_end\" value=\"target\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t</UML:ModelElement.taggedValue>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t<UML:AssociationEnd.participant>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t\t<UML:Classifier xmi.idref=\"").append(tgtId).append("\"/>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t\t</UML:AssociationEnd.participant>\n");
        sb.append("\t\t\t\t\t\t\t\t\t\t</UML:AssociationEnd>\n");
        sb.append("\t\t\t\t\t\t\t\t\t</UML:Association.connection>\n");
        sb.append("\t\t\t\t\t\t\t\t</UML:Association>\n");
    }

    // --- 2. FILE XML CHUẨN XMI 2.1 ĐẦY ĐỦ PHẦN TỬ CHO ENTERPRISE ARCHITECT 15 ---
    private static String buildEnterpriseArchitectXmi21() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<xmi:XMI xmi:version=\"2.1\" xmlns:uml=\"http://schema.omg.org/spec/UML/2.1\" xmlns:xmi=\"http://schema.omg.org/spec/XMI/2.1\">\n");
        sb.append("\t<xmi:Documentation exporter=\"Enterprise Architect\" exporterVersion=\"6.5\"/>\n");
        sb.append("\t<uml:Model xmi:type=\"uml:Model\" name=\"EA_Model\" visibility=\"public\">\n");
        sb.append("\t\t<packagedElement xmi:type=\"uml:Package\" xmi:id=\"EAPK_00000001_1111_4444_8888_000000000001\" name=\"MedClinic System\" visibility=\"public\">\n");

        // 1. Package Use Case Model
        sb.append("\t\t\t<packagedElement xmi:type=\"uml:Package\" xmi:id=\"EAPK_00000002_2222_4444_8888_000000000002\" name=\"Use Case Model\" visibility=\"public\">\n");
        sb.append("\t\t\t\t<packagedElement xmi:type=\"uml:Actor\" xmi:id=\"EAID_ACT00001_1111_4444_8888_000000000001\" name=\"Khách (Guest)\" visibility=\"public\"/>\n");
        sb.append("\t\t\t\t<packagedElement xmi:type=\"uml:Actor\" xmi:id=\"EAID_ACT00002_1111_4444_8888_000000000002\" name=\"Bệnh nhân (Patient)\" visibility=\"public\"/>\n");
        sb.append("\t\t\t\t<packagedElement xmi:type=\"uml:Actor\" xmi:id=\"EAID_ACT00003_1111_4444_8888_000000000003\" name=\"Bác sĩ (Doctor)\" visibility=\"public\"/>\n");
        sb.append("\t\t\t\t<packagedElement xmi:type=\"uml:Actor\" xmi:id=\"EAID_ACT00004_1111_4444_8888_000000000004\" name=\"Lễ tân (Staff)\" visibility=\"public\"/>\n");
        sb.append("\t\t\t\t<packagedElement xmi:type=\"uml:Actor\" xmi:id=\"EAID_ACT00005_1111_4444_8888_000000000005\" name=\"Quản trị viên (Admin)\" visibility=\"public\"/>\n");

        String[][] ucs = getUseCases();
        for (String[] uc : ucs) {
            sb.append("\t\t\t\t<packagedElement xmi:type=\"uml:UseCase\" xmi:id=\"EAID_U00000").append(uc[0]).append("_1111_4444_8888_0000000000").append(uc[0])
                    .append("\" name=\"").append(escapeXml(uc[1])).append("\" visibility=\"public\"/>\n");
        }

        String[][] ucAssocs21 = getUseCaseAssociations();
        for (int i = 0; i < ucAssocs21.length; i++) {
            String[] asc = ucAssocs21[i];
            String aId = "EAID_ASU000" + String.format("%02d", i + 1) + "_1111_4444_8888_0000000000" + String.format("%02d", i + 1);
            String srcId = "EAID_ACT000" + asc[0] + "_1111_4444_8888_0000000000" + asc[0];
            String tgtId = "EAID_U00000" + asc[1] + "_1111_4444_8888_0000000000" + asc[1];
            sb.append("\t\t\t\t<packagedElement xmi:type=\"uml:Association\" xmi:id=\"").append(aId)
                    .append("\" name=\"").append(escapeXml(asc[2] + " - " + asc[3])).append("\" memberEnd=\"")
                    .append(aId).append("_src ").append(aId).append("_dst\">\n");
            sb.append("\t\t\t\t\t<ownedEnd xmi:type=\"uml:Property\" xmi:id=\"").append(aId).append("_src\" type=\"").append(srcId).append("\" association=\"").append(aId).append("\"/>\n");
            sb.append("\t\t\t\t\t<ownedEnd xmi:type=\"uml:Property\" xmi:id=\"").append(aId).append("_dst\" type=\"").append(tgtId).append("\" association=\"").append(aId).append("\"/>\n");
            sb.append("\t\t\t\t</packagedElement>\n");
        }
        sb.append("\t\t\t</packagedElement>\n");

        // 2. Package Domain Class Model (3NF)
        sb.append("\t\t\t<packagedElement xmi:type=\"uml:Package\" xmi:id=\"EAPK_00000003_3333_4444_8888_000000000003\" name=\"Domain Class Model (3NF)\" visibility=\"public\">\n");
        String[][] classes = getClasses();
        for (String[] cls : classes) {
            String cId = "EAID_C00000" + cls[0] + "_1111_4444_8888_0000000000" + cls[0];
            sb.append("\t\t\t\t<packagedElement xmi:type=\"uml:Class\" xmi:id=\"").append(cId).append("\" name=\"").append(cls[1]).append("\" visibility=\"public\">\n");
            String[] attrs = cls[2].split(", ");
            for (int i = 0; i < attrs.length; i++) {
                String aId = "EAID_ATT" + cls[0] + "_" + String.format("%04d", i) + "_1111_4444_8888_" + String.format("%012d", i);
                sb.append("\t\t\t\t\t<ownedAttribute xmi:type=\"uml:Property\" xmi:id=\"").append(aId).append("\" name=\"").append(escapeXml(attrs[i])).append("\" visibility=\"private\"/>\n");
            }
            sb.append("\t\t\t\t</packagedElement>\n");
        }

        String[][] clsAssocs21 = getClassAssociations();
        for (int i = 0; i < clsAssocs21.length; i++) {
            String[] asc = clsAssocs21[i];
            String aId = "EAID_ASC000" + String.format("%02d", i + 1) + "_1111_4444_8888_0000000000" + String.format("%02d", i + 1);
            String srcId = "EAID_C00000" + asc[0] + "_1111_4444_8888_0000000000" + asc[0];
            String tgtId = "EAID_C00000" + asc[1] + "_1111_4444_8888_0000000000" + asc[1];
            sb.append("\t\t\t\t<packagedElement xmi:type=\"uml:Association\" xmi:id=\"").append(aId)
                    .append("\" name=\"").append(escapeXml(asc[7])).append("\" memberEnd=\"")
                    .append(aId).append("_src ").append(aId).append("_dst\">\n");
            sb.append("\t\t\t\t\t<ownedEnd xmi:type=\"uml:Property\" xmi:id=\"").append(aId).append("_src\" type=\"").append(srcId).append("\" association=\"").append(aId).append("\"/>\n");
            sb.append("\t\t\t\t\t<ownedEnd xmi:type=\"uml:Property\" xmi:id=\"").append(aId).append("_dst\" type=\"").append(tgtId).append("\" association=\"").append(aId).append("\"/>\n");
            sb.append("\t\t\t\t</packagedElement>\n");
        }
        sb.append("\t\t\t</packagedElement>\n");

        sb.append("\t\t</packagedElement>\n");
        sb.append("\t</uml:Model>\n");

        // 3. xmi:Extension for Enterprise Architect 15
        sb.append("\t<xmi:Extension extender=\"Enterprise Architect\" extenderID=\"6.5\">\n");
        sb.append("\t\t<elements>\n");
        sb.append("\t\t\t<element xmi:idref=\"EAPK_00000001_1111_4444_8888_000000000001\" xmi:type=\"uml:Package\" name=\"MedClinic System\" scope=\"public\">\n");
        sb.append("\t\t\t\t<model package2=\"EAID_00000001_1111_4444_8888_000000000001\" tpos=\"0\" ea_localid=\"1\" ea_eleType=\"package\"/>\n");
        sb.append("\t\t\t\t<properties isSpecification=\"false\" sType=\"Package\" nType=\"0\" scope=\"public\"/>\n");
        sb.append("\t\t\t\t<project author=\"MedClinic Team\" version=\"1.0\" created=\"2026-09-05 15:00:00\" modified=\"2026-09-05 15:00:00\"/>\n");
        sb.append("\t\t\t</element>\n");
        sb.append("\t\t\t<element xmi:idref=\"EAPK_00000002_2222_4444_8888_000000000002\" xmi:type=\"uml:Package\" name=\"Use Case Model\" scope=\"public\">\n");
        sb.append("\t\t\t\t<model package2=\"EAID_00000002_2222_4444_8888_000000000002\" package=\"EAPK_00000001_1111_4444_8888_000000000001\" tpos=\"0\" ea_localid=\"2\" ea_eleType=\"package\"/>\n");
        sb.append("\t\t\t\t<properties isSpecification=\"false\" sType=\"Package\" nType=\"0\" scope=\"public\"/>\n");
        sb.append("\t\t\t\t<project author=\"MedClinic Team\" version=\"1.0\" created=\"2026-09-05 15:00:00\" modified=\"2026-09-05 15:00:00\"/>\n");
        sb.append("\t\t\t</element>\n");
        sb.append("\t\t\t<element xmi:idref=\"EAPK_00000003_3333_4444_8888_000000000003\" xmi:type=\"uml:Package\" name=\"Domain Class Model (3NF)\" scope=\"public\">\n");
        sb.append("\t\t\t\t<model package2=\"EAID_00000003_3333_4444_8888_000000000003\" package=\"EAPK_00000001_1111_4444_8888_000000000001\" tpos=\"0\" ea_localid=\"3\" ea_eleType=\"package\"/>\n");
        sb.append("\t\t\t\t<properties isSpecification=\"false\" sType=\"Package\" nType=\"0\" scope=\"public\"/>\n");
        sb.append("\t\t\t\t<project author=\"MedClinic Team\" version=\"1.0\" created=\"2026-09-05 15:00:00\" modified=\"2026-09-05 15:00:00\"/>\n");
        sb.append("\t\t\t</element>\n");
        sb.append("\t\t</elements>\n");

        sb.append("\t\t<diagrams>\n");
        // Diagram 1: Use Case
        sb.append("\t\t\t<diagram xmi:id=\"EAID_D0000001_1111_4444_8888_000000000001\">\n");
        sb.append("\t\t\t\t<model package=\"EAPK_00000002_2222_4444_8888_000000000002\" localID=\"2\" ea_localid=\"2\"/>\n");
        sb.append("\t\t\t\t<properties name=\"Sơ đồ Use Case Tổng quan\" type=\"Use Case\"/>\n");
        sb.append("\t\t\t\t<project author=\"MedClinic Team\" version=\"1.0\" created=\"2026-09-05 15:00:00\" modified=\"2026-09-05 15:00:00\"/>\n");
        sb.append("\t\t\t\t<style1 value=\"ShowPrivate=1;ShowProtected=1;ShowPublic=1;HideRelationships=0;Locked=0;Border=1;HighlightForeign=1;PackageContents=1;DocSize.cx=1600;DocSize.cy=1300;Orientation=L;Zoom=100;\"/>\n");
        sb.append("\t\t\t\t<style2 value=\"SaveTag=22338634;ExcludeRTF=0;DocAll=0;HideQuals=0;AttPkg=1;ShowTags=0;ShowReqs=0;ShowCons=0;OpPkg=1;ShowFinds=0;ShowAtts=1;\"/>\n");
        sb.append("\t\t\t\t<elements>\n");
        sb.append("\t\t\t\t\t<element geometry=\"Left=30;Top=80;Right=120;Bottom=170;\" subject=\"EAID_ACT00001_1111_4444_8888_000000000001\" seqno=\"1\" style=\"DUID=A1;\"/>\n");
        sb.append("\t\t\t\t\t<element geometry=\"Left=30;Top=340;Right=120;Bottom=430;\" subject=\"EAID_ACT00002_1111_4444_8888_000000000002\" seqno=\"2\" style=\"DUID=A2;\"/>\n");
        sb.append("\t\t\t\t\t<element geometry=\"Left=30;Top=690;Right=120;Bottom=780;\" subject=\"EAID_ACT00003_1111_4444_8888_000000000003\" seqno=\"3\" style=\"DUID=A3;\"/>\n");
        sb.append("\t\t\t\t\t<element geometry=\"Left=1140;Top=340;Right=1230;Bottom=430;\" subject=\"EAID_ACT00004_1111_4444_8888_000000000004\" seqno=\"4\" style=\"DUID=A4;\"/>\n");
        sb.append("\t\t\t\t\t<element geometry=\"Left=1140;Top=715;Right=1230;Bottom=805;\" subject=\"EAID_ACT00005_1111_4444_8888_000000000005\" seqno=\"5\" style=\"DUID=A5;\"/>\n");
        int[][] uPos = getUseCasePositions();
        for (int i = 0; i < ucs.length; i++) {
            String uId = "EAID_U00000" + ucs[i][0] + "_1111_4444_8888_0000000000" + ucs[i][0];
            sb.append("\t\t\t\t\t<element geometry=\"Left=").append(uPos[i][0]).append(";Top=").append(uPos[i][1])
                    .append(";Right=").append(uPos[i][2]).append(";Bottom=").append(uPos[i][3])
                    .append(";\" subject=\"").append(uId).append("\" seqno=\"").append(i + 6).append("\" style=\"DUID=U").append(i + 1).append(";\"/>\n");
        }
        sb.append("\t\t\t\t</elements>\n");
        sb.append("\t\t\t</diagram>\n");

        // Diagram 2: Class Diagram
        sb.append("\t\t\t<diagram xmi:id=\"EAID_D0000002_1111_4444_8888_000000000002\">\n");
        sb.append("\t\t\t\t<model package=\"EAPK_00000003_3333_4444_8888_000000000003\" localID=\"3\" ea_localid=\"3\"/>\n");
        sb.append("\t\t\t\t<properties name=\"Sơ đồ Lớp Thực thể (Domain Class Diagram 3NF)\" type=\"Logical\"/>\n");
        sb.append("\t\t\t\t<project author=\"MedClinic Team\" version=\"1.0\" created=\"2026-09-05 15:00:00\" modified=\"2026-09-05 15:00:00\"/>\n");
        sb.append("\t\t\t\t<style1 value=\"ShowPrivate=1;ShowProtected=1;ShowPublic=1;HideRelationships=0;Locked=0;Border=1;HighlightForeign=1;PackageContents=1;DocSize.cx=1500;DocSize.cy=1200;Orientation=L;Zoom=100;\"/>\n");
        sb.append("\t\t\t\t<style2 value=\"SaveTag=32438634;ExcludeRTF=0;DocAll=0;HideQuals=0;AttPkg=1;ShowTags=0;ShowReqs=0;ShowCons=0;OpPkg=1;ShowFinds=0;ShowAtts=1;\"/>\n");
        sb.append("\t\t\t\t<elements>\n");
        int[][] cPos = getClassPositions();
        for (int i = 0; i < classes.length; i++) {
            String cId = "EAID_C00000" + classes[i][0] + "_1111_4444_8888_0000000000" + classes[i][0];
            sb.append("\t\t\t\t\t<element geometry=\"Left=").append(cPos[i][0]).append(";Top=").append(cPos[i][1])
                    .append(";Right=").append(cPos[i][2]).append(";Bottom=").append(cPos[i][3])
                    .append(";\" subject=\"").append(cId).append("\" seqno=\"").append(i + 1).append("\" style=\"DUID=C").append(i + 1).append(";\"/>\n");
        }
        sb.append("\t\t\t\t</elements>\n");
        sb.append("\t\t\t</diagram>\n");

        sb.append("\t\t</diagrams>\n");
        sb.append("\t</xmi:Extension>\n");
        sb.append("</xmi:XMI>\n");
        return sb.toString();
    }

    private static String[][] getUseCases() {
        return new String[][]{
                {"01", "UC_001: Đăng ký tài khoản (Sign up)"},
                {"02", "UC_002: Đăng nhập hệ thống (Sign in)"},
                {"03", "UC_003: Đăng xuất khỏi hệ thống (Sign out)"},
                {"04", "UC_004: Khôi phục mật khẩu (Password Recovery)"},
                {"05", "UC_005: Quản lý thông tin cá nhân & Đổi mật khẩu"},
                {"06", "UC_006: Tìm kiếm và lọc bác sĩ"},
                {"07", "UC_007: Xem chi tiết bác sĩ & chuyên khoa"},
                {"08", "UC_008: Đặt lịch khám trực tuyến (Optimistic Lock)"},
                {"09", "UC_009: Quản lý lịch hẹn & Hủy lịch"},
                {"10", "UC_010: Xem hồ sơ bệnh án & Đơn thuốc cũ"},
                {"11", "UC_011: Quầy tiếp nhận & Tra cứu lịch hẹn"},
                {"12", "UC_012: Quét QR Check-in & Cấp STT tiếp nhận"},
                {"13", "UC_013: Tiếp nhận bệnh nhân vãng lai (Walk-in)"},
                {"14", "UC_014: Bàn khám bác sĩ & Hàng chờ khám"},
                {"15", "UC_015: Khám bệnh & Ghi bệnh án điện tử"},
                {"16", "UC_016: Kê đơn thuốc & Trừ kho dược tự động"},
                {"17", "UC_017: Xuất Đơn thuốc ra file PDF"},
                {"18", "UC_018: Tự động sinh Hóa đơn viện phí"},
                {"19", "UC_019: Thu viện phí & Xác nhận thanh toán"},
                {"20", "UC_020: Xuất Hóa đơn viện phí ra file PDF"},
                {"21", "UC_021: Đánh giá & Phản hồi bác sĩ"},
                {"22", "UC_022: Quản lý Chuyên khoa (Specialty CRUD)"},
                {"23", "UC_023: Quản lý Bác sĩ & Lịch trực"},
                {"24", "UC_024: Quản lý Kho dược phẩm & Thuốc"},
                {"25", "UC_025: Quản lý Tài khoản người dùng"},
                {"26", "UC_026: Thống kê Doanh thu & KPI phòng khám"}
        };
    }

    private static String[][] getClasses() {
        return new String[][]{
                {"01", "User", "id: Long, username: String, password: String, fullName: String, email: String, phone: String, gender: Gender, dateOfBirth: LocalDate, address: String, isActive: Boolean, createdAt: LocalDateTime"},
                {"02", "Role", "id: Long, name: RoleType, description: String"},
                {"03", "Specialty", "id: Long, name: String, description: String, icon: String, isActive: Boolean"},
                {"04", "DoctorProfile", "id: Long, title: String, experienceYears: Integer, consultationFee: BigDecimal, bio: String"},
                {"05", "DoctorSchedule", "id: Long, workDate: LocalDate, shift: ShiftType, maxPatients: Integer"},
                {"06", "TimeSlot", "id: Long, startTime: LocalTime, endTime: LocalTime, status: SlotStatus, version: Long"},
                {"07", "Appointment", "id: Long, bookingCode: String, appointmentDate: LocalDate, reason: String, status: AppointmentStatus, queueNumber: String, createdAt: LocalDateTime"},
                {"08", "MedicalRecord", "id: Long, symptoms: String, diagnosis: String, doctorNotes: String, reExamDate: LocalDate, createdAt: LocalDateTime"},
                {"09", "Medicine", "id: Long, name: String, unit: String, unitPrice: BigDecimal, usageGuide: String, stockQuantity: Integer, isActive: Boolean"},
                {"10", "Prescription", "id: Long, doctorNotes: String, createdAt: LocalDateTime"},
                {"11", "PrescriptionItem", "id: Long, quantity: Integer, dosage: String, frequency: String, instructions: String"},
                {"12", "Invoice", "id: Long, invoiceCode: String, consultationFee: BigDecimal, medicineFee: BigDecimal, totalAmount: BigDecimal, paymentMethod: PaymentMethod, paymentStatus: PaymentStatus, paidAt: LocalDateTime"},
                {"13", "Notification", "id: Long, title: String, content: String, type: String, isRead: Boolean, createdAt: LocalDateTime"}
        };
    }

    private static int[][] getUseCasePositions() {
        return new int[][]{
                {220, 40, 480, 95},     // UC_001 (Sign up) - Khách
                {560, 40, 820, 95},     // UC_002 (Sign in) - Chung
                {560, 120, 820, 175},   // UC_003 (Sign out) - Chung
                {220, 120, 480, 175},   // UC_004 (Password Recovery) - Khách, BN
                {560, 200, 820, 255},   // UC_005 (Profile & Pass) - Chung
                {220, 200, 480, 255},   // UC_006 (Tìm kiếm bác sĩ) - Khách, BN
                {220, 280, 480, 335},   // UC_007 (Xem chi tiết bác sĩ) - Khách, BN
                {220, 360, 480, 415},   // UC_008 (Đặt lịch khám) - Bệnh nhân
                {220, 440, 480, 495},   // UC_009 (Quản lý/Hủy lịch) - Bệnh nhân
                {220, 520, 480, 575},   // UC_010 (Xem hồ sơ bệnh án) - Bệnh nhân
                {920, 280, 1180, 335},  // UC_011 (Quầy tiếp nhận) - Lễ tân
                {920, 360, 1180, 415},  // UC_012 (QR Check-in) - Lễ tân, BN
                {920, 440, 1180, 495},  // UC_013 (Tiếp nhận vãng lai) - Lễ tân
                {220, 720, 480, 775},   // UC_014 (Hàng chờ khám) - Bác sĩ
                {220, 800, 480, 855},   // UC_015 (Khám bệnh & EMR) - Bác sĩ
                {220, 880, 480, 935},   // UC_016 (Kê đơn thuốc tự động) - Bác sĩ
                {540, 800, 800, 855},   // UC_017 (Xuất đơn thuốc PDF) - Bác sĩ
                {540, 880, 800, 935},   // UC_018 (Tự động sinh Hóa đơn) - Bác sĩ
                {920, 520, 1180, 575},  // UC_019 (Thu viện phí) - Lễ tân
                {920, 600, 1180, 655},  // UC_020 (Xuất hóa đơn PDF) - Lễ tân
                {220, 600, 480, 655},   // UC_021 (Đánh giá bác sĩ) - Bệnh nhân
                {1040, 720, 1300, 775}, // UC_022 (Quản lý Chuyên khoa) - Admin
                {1040, 800, 1300, 855}, // UC_023 (Quản lý Bác sĩ & Lịch trực) - Admin
                {1040, 880, 1300, 935}, // UC_024 (Quản lý Kho dược phẩm) - Admin
                {1040, 960, 1300, 1015},// UC_025 (Quản lý Tài khoản) - Admin
                {1040, 1040, 1300, 1095}// UC_026 (Thống kê Doanh thu & KPI) - Admin
        };
    }

    private static int[][] getClassPositions() {
        return new int[][]{
                {360, 40, 640, 270},    // 01: User
                {60, 40, 240, 150},     // 02: Role
                {60, 340, 260, 470},    // 03: Specialty
                {360, 340, 640, 480},   // 04: DoctorProfile
                {760, 340, 1020, 480},  // 05: DoctorSchedule
                {1120, 340, 1340, 480}, // 06: TimeSlot
                {360, 580, 640, 780},   // 07: Appointment
                {760, 580, 1020, 760},  // 08: MedicalRecord
                {1120, 600, 1360, 780}, // 09: Medicine
                {760, 860, 1020, 1000}, // 10: Prescription
                {1120, 860, 1380, 1020},// 11: PrescriptionItem
                {60, 600, 300, 800},    // 12: Invoice
                {760, 40, 1020, 200}    // 13: Notification
        };
    }

    private static String[][] getUseCaseAssociations() {
        return new String[][]{
                // Khách (01)
                {"01", "01", "Khách (Guest)", "UC_001: Đăng ký tài khoản (Sign up)"},
                {"01", "02", "Khách (Guest)", "UC_002: Đăng nhập hệ thống (Sign in)"},
                {"01", "04", "Khách (Guest)", "UC_004: Khôi phục mật khẩu (Password Recovery)"},
                {"01", "06", "Khách (Guest)", "UC_006: Tìm kiếm và lọc bác sĩ"},
                {"01", "07", "Khách (Guest)", "UC_007: Xem chi tiết bác sĩ & chuyên khoa"},

                // Bệnh nhân (02)
                {"02", "02", "Bệnh nhân (Patient)", "UC_002: Đăng nhập hệ thống (Sign in)"},
                {"02", "03", "Bệnh nhân (Patient)", "UC_003: Đăng xuất khỏi hệ thống (Sign out)"},
                {"02", "04", "Bệnh nhân (Patient)", "UC_004: Khôi phục mật khẩu (Password Recovery)"},
                {"02", "05", "Bệnh nhân (Patient)", "UC_005: Quản lý thông tin cá nhân & Đổi mật khẩu"},
                {"02", "06", "Bệnh nhân (Patient)", "UC_006: Tìm kiếm và lọc bác sĩ"},
                {"02", "07", "Bệnh nhân (Patient)", "UC_007: Xem chi tiết bác sĩ & chuyên khoa"},
                {"02", "08", "Bệnh nhân (Patient)", "UC_008: Đặt lịch khám trực tuyến (Optimistic Lock)"},
                {"02", "09", "Bệnh nhân (Patient)", "UC_009: Quản lý lịch hẹn & Hủy lịch"},
                {"02", "10", "Bệnh nhân (Patient)", "UC_010: Xem hồ sơ bệnh án & Đơn thuốc cũ"},
                {"02", "12", "Bệnh nhân (Patient)", "UC_012: Quét QR Check-in & Cấp STT tiếp nhận"},
                {"02", "21", "Bệnh nhân (Patient)", "UC_021: Đánh giá & Phản hồi bác sĩ"},

                // Bác sĩ (03)
                {"03", "02", "Bác sĩ (Doctor)", "UC_002: Đăng nhập hệ thống (Sign in)"},
                {"03", "03", "Bác sĩ (Doctor)", "UC_003: Đăng xuất khỏi hệ thống (Sign out)"},
                {"03", "05", "Bác sĩ (Doctor)", "UC_005: Quản lý thông tin cá nhân & Đổi mật khẩu"},
                {"03", "14", "Bác sĩ (Doctor)", "UC_014: Bàn khám bác sĩ & Hàng chờ khám"},
                {"03", "15", "Bác sĩ (Doctor)", "UC_015: Khám bệnh & Ghi bệnh án điện tử"},
                {"03", "16", "Bác sĩ (Doctor)", "UC_016: Kê đơn thuốc & Trừ kho dược tự động"},
                {"03", "17", "Bác sĩ (Doctor)", "UC_017: Xuất Đơn thuốc ra file PDF"},
                {"03", "18", "Bác sĩ (Doctor)", "UC_018: Tự động sinh Hóa đơn viện phí"},

                // Lễ tân (04)
                {"04", "02", "Lễ tân (Staff)", "UC_002: Đăng nhập hệ thống (Sign in)"},
                {"04", "03", "Lễ tân (Staff)", "UC_003: Đăng xuất khỏi hệ thống (Sign out)"},
                {"04", "05", "Lễ tân (Staff)", "UC_005: Quản lý thông tin cá nhân & Đổi mật khẩu"},
                {"04", "11", "Lễ tân (Staff)", "UC_011: Quầy tiếp nhận & Tra cứu lịch hẹn"},
                {"04", "12", "Lễ tân (Staff)", "UC_012: Quét QR Check-in & Cấp STT tiếp nhận"},
                {"04", "13", "Lễ tân (Staff)", "UC_013: Tiếp nhận bệnh nhân vãng lai (Walk-in)"},
                {"04", "19", "Lễ tân (Staff)", "UC_019: Thu viện phí & Xác nhận thanh toán"},
                {"04", "20", "Lễ tân (Staff)", "UC_020: Xuất Hóa đơn viện phí ra file PDF"},

                // Quản trị viên (05)
                {"05", "02", "Quản trị viên (Admin)", "UC_002: Đăng nhập hệ thống (Sign in)"},
                {"05", "03", "Quản trị viên (Admin)", "UC_003: Đăng xuất khỏi hệ thống (Sign out)"},
                {"05", "05", "Quản trị viên (Admin)", "UC_005: Quản lý thông tin cá nhân & Đổi mật khẩu"},
                {"05", "22", "Quản trị viên (Admin)", "UC_022: Quản lý Chuyên khoa (Specialty CRUD)"},
                {"05", "23", "Quản trị viên (Admin)", "UC_023: Quản lý Bác sĩ & Lịch trực"},
                {"05", "24", "Quản trị viên (Admin)", "UC_024: Quản lý Kho dược phẩm & Thuốc"},
                {"05", "25", "Quản trị viên (Admin)", "UC_025: Quản lý Tài khoản người dùng"},
                {"05", "26", "Quản trị viên (Admin)", "UC_026: Thống kê Doanh thu & KPI phòng khám"}
        };
    }

    private static String[][] getClassAssociations() {
        return new String[][]{
                // srcClassId, tgtClassId, srcName, tgtName, srcMult, tgtMult, roleName, assocName
                {"01", "02", "User", "Role", "*", "1", "role", "User_Role_FK"},
                {"04", "01", "DoctorProfile", "User", "0..1", "1", "user", "Doctor_User_FK"},
                {"04", "03", "DoctorProfile", "Specialty", "*", "1", "specialty", "Doctor_Specialty_FK"},
                {"05", "04", "DoctorSchedule", "DoctorProfile", "*", "1", "doctor", "Schedule_Doctor_FK"},
                {"06", "05", "TimeSlot", "DoctorSchedule", "*", "1", "schedule", "TimeSlot_Schedule_FK"},
                {"07", "01", "Appointment", "User", "*", "1", "patient", "Appointment_Patient_FK"},
                {"07", "04", "Appointment", "DoctorProfile", "*", "1", "doctor", "Appointment_Doctor_FK"},
                {"07", "06", "Appointment", "TimeSlot", "1", "1", "slot", "Appointment_Slot_FK"},
                {"08", "07", "MedicalRecord", "Appointment", "0..1", "1", "appointment", "Record_Appointment_FK"},
                {"10", "08", "Prescription", "MedicalRecord", "0..1", "1", "record", "Prescription_Record_FK"},
                {"11", "10", "PrescriptionItem", "Prescription", "*", "1", "prescription", "Item_Prescription_FK"},
                {"11", "09", "PrescriptionItem", "Medicine", "*", "1", "medicine", "Item_Medicine_FK"},
                {"12", "07", "Invoice", "Appointment", "0..1", "1", "appointment", "Invoice_Appointment_FK"},
                {"13", "01", "Notification", "User", "*", "1", "recipient", "Notification_User_FK"}
        };
    }

    private static String getContentTypesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
                "  <Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>\n" +
                "  <Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/>\n" +
                "  <Override PartName=\"/word/settings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml\"/>\n" +
                "  <Override PartName=\"/word/fontTable.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml\"/>\n" +
                "  <Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>\n" +
                "  <Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>\n" +
                "</Types>";
    }

    private static String getRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>\n" +
                "  <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/>\n" +
                "  <Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" Target=\"docProps/app.xml\"/>\n" +
                "</Relationships>";
    }

    private static String getDocRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>\n" +
                "  <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings\" Target=\"settings.xml\"/>\n" +
                "  <Relationship Id=\"rId3\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable\" Target=\"fontTable.xml\"/>\n" +
                "</Relationships>";
    }

    private static String getAppXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\"><Application>Microsoft Word</Application></Properties>";
    }

    private static String getCoreXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n" +
                "  <dc:title>Báo Cáo Khảo Sát &amp; Phân Tích Thiết Kế Hệ Thống MedClinic</dc:title>\n" +
                "  <dc:creator>MedClinic Team</dc:creator>\n" +
                "</cp:coreProperties>";
    }

    private static String getSettingsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<w:settings xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:defaultTabStop w:val=\"720\"/></w:settings>";
    }

    private static String getFontTableXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<w:fontTable xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:font w:name=\"Times New Roman\"/></w:fontTable>";
    }

    private static String getStylesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">\n" +
                "  <w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\">\n" +
                "    <w:name w:val=\"Normal\"/>\n" +
                "    <w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/><w:sz w:val=\"26\"/></w:rPr>\n" +
                "  </w:style>\n" +
                "  <w:style w:type=\"paragraph\" w:styleId=\"Heading1\">\n" +
                "    <w:name w:val=\"heading 1\"/>\n" +
                "    <w:basedOn w:val=\"Normal\"/>\n" +
                "    <w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/><w:b/><w:sz w:val=\"32\"/><w:color w:val=\"1F4E79\"/></w:rPr>\n" +
                "  </w:style>\n" +
                "  <w:style w:type=\"paragraph\" w:styleId=\"Heading2\">\n" +
                "    <w:name w:val=\"heading 2\"/>\n" +
                "    <w:basedOn w:val=\"Normal\"/>\n" +
                "    <w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/><w:b/><w:sz w:val=\"28\"/><w:color w:val=\"2F5597\"/></w:rPr>\n" +
                "  </w:style>\n" +
                "  <w:style w:type=\"paragraph\" w:styleId=\"Heading3\">\n" +
                "    <w:name w:val=\"heading 3\"/>\n" +
                "    <w:basedOn w:val=\"Normal\"/>\n" +
                "    <w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/><w:b/><w:sz w:val=\"26\"/></w:rPr>\n" +
                "  </w:style>\n" +
                "  <w:style w:type=\"table\" w:styleId=\"TableGrid\">\n" +
                "    <w:name w:val=\"Table Grid\"/>\n" +
                "    <w:tblPr>\n" +
                "      <w:tblBorders>\n" +
                "        <w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CCCCCC\"/>\n" +
                "        <w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CCCCCC\"/>\n" +
                "        <w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CCCCCC\"/>\n" +
                "        <w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"CCCCCC\"/>\n" +
                "        <w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"E0E0E0\"/>\n" +
                "        <w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"E0E0E0\"/>\n" +
                "      </w:tblBorders>\n" +
                "    </w:tblPr>\n" +
                "  </w:style>\n" +
                "</w:styles>";
    }
}
