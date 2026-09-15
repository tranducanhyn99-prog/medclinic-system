package com.medclinic.generator;

import java.nio.file.*;
import java.util.*;

public class GenerateTemplateDocs {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("=== TIẾN HÀNH TẠO 5 TÀI LIỆU DOCX THEO MẪU TỪ NỘI DUNG BÁO CÁO KHẢO SÁT MEDCLINIC ===");
        System.out.println("================================================================================\n");

        try {
            // Định nghĩa các thư mục đích
            Path dirMauMHH = Paths.get("c:/Users/DUCANHZZ/Downloads/LTW/PRJ/Mau_Mo_Hinh_Hoa_Yeu_cau");
            Path dirMauCSDL = Paths.get("c:/Users/DUCANHZZ/Downloads/LTW/PRJ/Mau_Thiet_Ke_Du_Lieu");
            Path dirDocsPrj = Paths.get("c:/Users/DUCANHZZ/Downloads/LTW/PRJ/docs");
            Path dirDiagrams = dirDocsPrj.resolve("diagrams");
            Path dirLtw = Paths.get("c:/Users/DUCANHZZ/Downloads/LTW");

            Files.createDirectories(dirMauMHH);
            Files.createDirectories(dirMauCSDL);
            Files.createDirectories(dirDocsPrj);
            Files.createDirectories(dirDiagrams);
            Files.createDirectories(dirLtw);

            // Tự động tạo và render các biểu đồ độ phân giải cao
            System.out.println("[-] Đang tự động vẽ và kết xuất 4 sơ đồ kiến trúc hệ thống (PNG)...");
            DatabaseDiagramRenderer.renderAllDiagrams(dirDiagrams);
            System.out.println("    ✓ Sơ đồ đã sẵn sàng để nhúng vào tài liệu Word.\n");

            // 1. TÀI LIỆU 1: Khảo sát hiện trạng (theo Mau_Khao sat hien trang.docx)
            System.out.println("[1/5] Đang tạo tài liệu: MedClinic_KhaoSat_HienTrang.docx ...");
            String xmlKhaoSat = KhaoSatHienTrangDocBuilder.buildDocumentXml();
            Path doc1Prj = dirDocsPrj.resolve("MedClinic_KhaoSat_HienTrang.docx");
            DocxModelHelper.buildDocx(doc1Prj, xmlKhaoSat);
            Files.copy(doc1Prj, dirMauMHH.resolve("MedClinic_KhaoSat_HienTrang.docx"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(doc1Prj, dirLtw.resolve("MedClinic_KhaoSat_HienTrang.docx"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  ✓ Đã lưu tại:");
            System.out.println("    - " + dirMauMHH.resolve("MedClinic_KhaoSat_HienTrang.docx"));
            System.out.println("    - " + doc1Prj);
            System.out.println("    - " + dirLtw.resolve("MedClinic_KhaoSat_HienTrang.docx"));

            // 2. TÀI LIỆU 2: Xác định yêu cầu (theo Mau_XDYC.docx)
            System.out.println("\n[2/5] Đang tạo tài liệu: MedClinic_XacDinhYeuCau.docx ...");
            String xmlXdyc = XacDinhYeuCauDocBuilder.buildDocumentXml();
            Path doc2Prj = dirDocsPrj.resolve("MedClinic_XacDinhYeuCau.docx");
            DocxModelHelper.buildDocx(doc2Prj, xmlXdyc);
            Files.copy(doc2Prj, dirMauMHH.resolve("MedClinic_XacDinhYeuCau.docx"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(doc2Prj, dirLtw.resolve("MedClinic_XacDinhYeuCau.docx"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  ✓ Đã lưu tại:");
            System.out.println("    - " + dirMauMHH.resolve("MedClinic_XacDinhYeuCau.docx"));
            System.out.println("    - " + doc2Prj);
            System.out.println("    - " + dirLtw.resolve("MedClinic_XacDinhYeuCau.docx"));

            // 3. TÀI LIỆU 3: Đặc tả Use Case chi tiết (theo Mau_DacTaUsecase.docx)
            System.out.println("\n[3/5] Đang tạo tài liệu: MedClinic_DacTaUseCase.docx (26 Use Cases) ...");
            String xmlDacTa = DacTaUseCaseDocBuilder.buildDocumentXml();
            Path doc3Prj = dirDocsPrj.resolve("MedClinic_DacTaUseCase.docx");
            DocxModelHelper.buildDocx(doc3Prj, xmlDacTa);
            Files.copy(doc3Prj, dirMauMHH.resolve("MedClinic_DacTaUseCase.docx"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(doc3Prj, dirLtw.resolve("MedClinic_DacTaUseCase.docx"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  ✓ Đã lưu tại:");
            System.out.println("    - " + dirMauMHH.resolve("MedClinic_DacTaUseCase.docx"));
            System.out.println("    - " + doc3Prj);
            System.out.println("    - " + dirLtw.resolve("MedClinic_DacTaUseCase.docx"));

            // 4. TÀI LIỆU 4: Mô hình hóa yêu cầu (theo Mau MHH yeu cau.docx)
            System.out.println("\n[4/5] Đang tạo tài liệu: MedClinic_MoHinhHoaYeuCau.docx (kèm sơ đồ Use Case EA)...");
            String xmlMhh = MoHinhHoaYeuCauDocBuilder.buildDocumentXml();
            Path doc4Prj = dirDocsPrj.resolve("MedClinic_MoHinhHoaYeuCau.docx");
            Map<String, byte[]> mhhImages = new LinkedHashMap<>();
            mhhImages.put("word/media/image1.png", Files.readAllBytes(dirDiagrams.resolve("diagram_usecase_overview.png")));
            DocxModelHelper.buildDocxWithImages(doc4Prj, xmlMhh, mhhImages);
            Files.copy(doc4Prj, dirMauMHH.resolve("MedClinic_MoHinhHoaYeuCau.docx"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(doc4Prj, dirLtw.resolve("MedClinic_MoHinhHoaYeuCau.docx"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  ✓ Đã lưu kèm hình ảnh sơ đồ tại:");
            System.out.println("    - " + dirMauMHH.resolve("MedClinic_MoHinhHoaYeuCau.docx"));
            System.out.println("    - " + doc4Prj);
            System.out.println("    - " + dirLtw.resolve("MedClinic_MoHinhHoaYeuCau.docx"));

            // 5. TÀI LIỆU 5: Thiết kế CSDL (theo BaoCaoMau_ThietKeCSDL.docx)
            System.out.println("\n[5/5] Đang tạo tài liệu: MedClinic_ThietKeCSDL.docx (13 bảng dữ liệu 3NF + 3 sơ đồ ERD, RBAC, Physical)...");
            String xmlCsdl = ThietKeCsdlDocBuilder.buildDocumentXml();
            Path doc5Prj = dirDocsPrj.resolve("MedClinic_ThietKeCSDL.docx");
            Map<String, byte[]> csdlImages = new LinkedHashMap<>();
            csdlImages.put("word/media/image1.png", Files.readAllBytes(dirDiagrams.resolve("diagram_logical_erd.png")));
            csdlImages.put("word/media/image2.png", Files.readAllBytes(dirDiagrams.resolve("diagram_rbac.png")));
            csdlImages.put("word/media/image3.png", Files.readAllBytes(dirDiagrams.resolve("diagram_physical_schema.png")));
            DocxModelHelper.buildDocxWithImages(doc5Prj, xmlCsdl, csdlImages);
            Files.copy(doc5Prj, dirMauCSDL.resolve("MedClinic_ThietKeCSDL.docx"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(doc5Prj, dirLtw.resolve("MedClinic_ThietKeCSDL.docx"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  ✓ Đã lưu kèm 3 sơ đồ kiến trúc tại:");
            System.out.println("    - " + dirMauCSDL.resolve("MedClinic_ThietKeCSDL.docx"));
            System.out.println("    - " + doc5Prj);
            System.out.println("    - " + dirLtw.resolve("MedClinic_ThietKeCSDL.docx"));

            System.out.println("\n================================================================================");
            System.out.println("=== HOÀN TẤT TẠO 5 TÀI LIỆU DOCX THEO ĐÚNG MẪU THÀNH CÔNG 100% ===");
            System.out.println("================================================================================");

        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình tạo tài liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
