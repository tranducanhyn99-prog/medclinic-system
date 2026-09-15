package com.medclinic.generator;

import static com.medclinic.generator.DocxModelHelper.*;

public class KhaoSatHienTrangDocBuilder {

    public static String buildDocumentXml() {
        StringBuilder sb = new StringBuilder();

        // 1. TRANG BÌA
        sb.append(buildCoverPage("BÁO CÁO KHẢO SÁT HIỆN TRẠNG", "HỆ THỐNG QUẢN LÝ VÀ ĐẶT LỊCH KHÁM PHÒNG KHÁM TƯ NHÂN (MEDCLINIC)", "KHẢO SÁT HIỆN TRẠNG VÀ PHÂN TÍCH GIẢI PHÁP"));

        // 2. NỘI DUNG CHÍNH
        sb.append(p("KHẢO SÁT HIỆN TRẠNG", "Heading1", true, 16, "left"));
        sb.append(p("1. Đặt vấn đề và tính cấp thiết", "Heading2", true, 14, "left"));
        sb.append(p("Trong những năm gần đây, ngành y tế tại Việt Nam đang chứng kiến sự bùng nổ của quá trình chuyển đổi số. Đối với các phòng khám tư nhân và cơ sở y tế chuyên khoa vừa và nhỏ, nhu cầu tối ưu hóa quy trình tiếp nhận, giảm thiểu thời gian chờ đợi của người bệnh, số hóa bệnh án điện tử và kiểm soát kho dược phẩm trở thành yêu cầu sống còn. Việc quản lý thủ công truyền thống bằng sổ sách hoặc phần mềm rời rạc dẫn đến hàng loạt hạn chế như: tình trạng đặt trùng lịch khám (double-booking), quá tải cục bộ tại khung giờ cao điểm, thất lạc lịch sử điều trị của bệnh nhân và sai lệch tồn kho thuốc.", null, false, 13, "both"));
        sb.append(p("Hệ thống MedClinic ra đời nhằm giải quyết triệt để các hạn chế trên, cung cấp nền tảng quản lý tập trung và thông minh cho cả 5 nhóm đối tượng: Khách vãng lai, Bệnh nhân, Bác sĩ chuyên khoa, Nhân viên lễ tân tiếp nhận và Quản trị viên phòng khám.", null, false, 13, "both"));

        sb.append(p("2. Khảo sát các giải pháp y tế số hiện hành", "Heading2", true, 14, "left"));
        sb.append(p("Để xây dựng hệ thống đạt hiệu quả cao, nhóm nghiên cứu đã tiến hành khảo sát và đánh giá 4 nền tảng tiêu biểu nhất trên thị trường Việt Nam hiện nay:", null, false, 13, "both"));

        // 2.1. Medpro
        sb.append(p("2.1. Nền tảng Medpro (Ứng dụng Đặt khám Bệnh viện & Phòng khám)", "Heading3", true, 13, "left"));
        sb.append(p("Medpro là nền tảng kết nối người bệnh với hơn 100 bệnh viện công lập và phòng khám đa khoa trên toàn quốc (Bệnh viện Chợ Rẫy, ĐH Y Dược TP.HCM, Nhi Đồng 1). Hệ thống cung cấp dịch vụ đặt lịch khám theo chuyên khoa, bác sĩ, ngày giờ và thanh toán trực tuyến.", null, false, 13, "both"));
        sb.append(p("Bảng 2-1. Đánh giá ưu điểm và nhược điểm của nền tảng Medpro", null, true, 11, "center"));
        sb.append(startTable());
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
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        // 2.2. BookingCare
        sb.append(p("2.2. Nền tảng BookingCare (Chăm sóc Sức khỏe Toàn diện)", "Heading3", true, 13, "left"));
        sb.append(p("BookingCare là nền tảng đặt lịch khám chuyên khoa với các bác sĩ giỏi tại các bệnh viện lớn và phòng khám tư nhân cao cấp ở Hà Nội và TP.HCM, cung cấp thông tin tiểu sử bác sĩ rất chi tiết và hỗ trợ tư vấn khám từ xa (Telemedicine).", null, false, 13, "both"));
        sb.append(p("Bảng 2-2. Đánh giá ưu điểm và nhược điểm của BookingCare", null, true, 11, "center"));
        sb.append(startTable());
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
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        // 2.3. YouMed
        sb.append(p("2.3. Nền tảng YouMed (Quản lý & Đặt khám Phòng khám Thông minh)", "Heading3", true, 13, "left"));
        sb.append(p("YouMed là giải pháp phần mềm y tế phục vụ cả hai đối tượng: người bệnh đặt lịch khám trực tuyến và bác sĩ/phòng khám quản lý lịch hẹn, bệnh án thông qua phần mềm YouMed Clinic.", null, false, 13, "both"));
        sb.append(p("Bảng 2-3. Đánh giá ưu điểm và nhược điểm của YouMed", null, true, 11, "center"));
        sb.append(startTable());
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
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        // 2.4. Vinmec
        sb.append(p("2.4. Cổng Thông Tin & Đặt Khám Bệnh Viện Đa Khoa Quốc Tế Vinmec", "Heading3", true, 13, "left"));
        sb.append(p("Hệ thống đặt khám trực tuyến thuộc Tập đoàn Vingroup, tiêu chuẩn quốc tế JCI với dịch vụ chăm sóc y tế cao cấp, quản lý tập trung toàn bộ dữ liệu khám chữa bệnh của người bệnh trong toàn hệ thống.", null, false, 13, "both"));
        sb.append(p("Bảng 2-4. Đánh giá ưu điểm và nhược điểm của cổng thông tin Vinmec", null, true, 11, "center"));
        sb.append(startTable());
        sb.append(tableRow(true, "Ưu điểm", "Nhược điểm"));
        sb.append(tableRow(false,
                "✓ Hệ thống bảo mật thông tin y tế chuẩn quốc tế (HIPAA/JCI)\n" +
                "✓ Liên kết trực tiếp kết quả xét nghiệm, chẩn đoán hình ảnh và lịch sử tiêm chủng\n" +
                "✓ Bệnh nhân theo dõi được toàn bộ lộ trình khám chữa bệnh",
                "× Hệ thống đóng kín (Closed ecosystem), chỉ áp dụng cho nội bộ bệnh viện Vinmec, không thể triển khai cho phòng khám tư nhân\n" +
                "× Thủ tục đặt khám và giao diện phân cấp quá sâu, phức tạp đối với người lớn tuổi\n" +
                "× Chi phí khám và dịch vụ ở phân khúc cao cấp, không phổ biến cho đại chúng"
        ));
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        // 3. BẢNG TỔNG HỢP VÀ ĐỐI SÁNH
        sb.append(p("3. Bảng tổng hợp đối sánh và Định hướng xây dựng MedClinic", "Heading2", true, 14, "left"));
        sb.append(p("Dựa trên việc phân tích ưu và nhược điểm của các hệ thống đi trước, hệ thống MedClinic được định hướng xây dựng nhằm kế thừa những điểm mạnh và giải quyết triệt để các tồn tại:", null, false, 13, "both"));
        sb.append(p("Bảng 2-5. Bảng ma trận so sánh các tính năng chính giữa các hệ thống", null, true, 11, "center"));
        sb.append(startTable());
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
        sb.append(endTable());
        sb.append(p("", null, false, 12, "left"));

        return wrapDocument(sb.toString());
    }
}
