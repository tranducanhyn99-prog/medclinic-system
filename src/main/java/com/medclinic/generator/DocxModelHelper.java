package com.medclinic.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class DocxModelHelper {

    public static void buildDocx(Path targetPath, String documentXml) throws Exception {
        buildDocxWithImages(targetPath, documentXml, Collections.emptyMap(), Collections.emptyMap());
    }

    public static void buildDocxWithImages(Path targetPath, String documentXml, Map<String, byte[]> images) throws Exception {
        Map<String, String> rels = new LinkedHashMap<>();
        if (images != null) {
            int idx = 1;
            for (String entryName : images.keySet()) {
                String target = entryName.startsWith("word/") ? entryName.substring(5) : entryName;
                rels.put("rIdImg" + idx++, target);
            }
        }
        buildDocxWithImages(targetPath, documentXml, images, rels);
    }

    public static void buildDocxWithImages(Path targetPath, String documentXml,
                                           Map<String, byte[]> images,
                                           Map<String, String> imageRelationships) throws Exception {
        Files.createDirectories(targetPath.getParent());
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(targetPath))) {
            addZipEntry(zos, "[Content_Types].xml", getContentTypesXml());
            addZipEntry(zos, "_rels/.rels", getRelsXml());
            addZipEntry(zos, "docProps/app.xml", getAppXml());
            addZipEntry(zos, "docProps/core.xml", getCoreXml());
            addZipEntry(zos, "word/_rels/document.xml.rels", getDocRelsXml(imageRelationships));
            addZipEntry(zos, "word/styles.xml", getStylesXml());
            addZipEntry(zos, "word/settings.xml", getSettingsXml());
            addZipEntry(zos, "word/document.xml", documentXml);

            // Ghi các file ảnh nhị phân
            if (images != null) {
                for (Map.Entry<String, byte[]> entry : images.entrySet()) {
                    addZipBinaryEntry(zos, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public static void addZipEntry(ZipOutputStream zos, String entryName, String content) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    public static void addZipBinaryEntry(ZipOutputStream zos, String entryName, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }

    public static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static String p(String text, String style, boolean bold, int sizePt, String align) {
        StringBuilder sb = new StringBuilder("<w:p><w:pPr>");
        if (style != null) sb.append("<w:pStyle w:val=\"").append(style).append("\"/>");
        sb.append("<w:spacing w:line=\"276\" w:lineRule=\"auto\" w:after=\"120\"/>");
        if (align != null) sb.append("<w:jc w:val=\"").append(align).append("\"/>");
        sb.append("</w:pPr>");
        if (text != null && !text.isEmpty()) {
            sb.append("<w:r><w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>");
            if (bold) sb.append("<w:b/>");
            if (sizePt > 0) sb.append("<w:sz w:val=\"").append(sizePt * 2).append("\"/>");
            sb.append("</w:rPr><w:t xml:space=\"preserve\">").append(escapeXml(text)).append("</w:t></w:r>");
        }
        sb.append("</w:p>");
        return sb.toString();
    }

    public static String drawingImage(String rId, int docPrId, String name, int cxEmu, int cyEmu, String caption) {
        StringBuilder sb = new StringBuilder();
        sb.append("<w:p><w:pPr><w:spacing w:before=\"180\" w:after=\"60\"/><w:jc w:val=\"center\"/></w:pPr>");
        sb.append("<w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">");
        sb.append("<wp:extent cx=\"").append(cxEmu).append("\" cy=\"").append(cyEmu).append("\"/>");
        sb.append("<wp:docPr id=\"").append(docPrId).append("\" name=\"").append(escapeXml(name)).append("\"/>");
        sb.append("<wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" noChangeAspect=\"1\"/></wp:cNvGraphicFramePr>");
        sb.append("<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">");
        sb.append("<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">");
        sb.append("<pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">");
        sb.append("<pic:nvPicPr><pic:cNvPr id=\"").append(docPrId).append("\" name=\"").append(escapeXml(name)).append("\"/><pic:cNvPicPr/></pic:nvPicPr>");
        sb.append("<pic:blipFill><a:blip xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" r:embed=\"").append(rId).append("\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>");
        sb.append("<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"").append(cxEmu).append("\" cy=\"").append(cyEmu).append("\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr>");
        sb.append("</pic:pic></a:graphicData></a:graphic>");
        sb.append("</wp:inline></w:drawing></w:r></w:p>");
        if (caption != null && !caption.isEmpty()) {
            sb.append(p(caption, null, true, 11, "center"));
            sb.append(p("", null, false, 8, "center"));
        }
        return sb.toString();
    }

    public static String pageBreak() {
        return "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>";
    }

    public static String startTable() {
        return "<w:tbl><w:tblPr><w:tblStyle w:val=\"TableGrid\"/><w:tblW w:w=\"9360\" w:type=\"dxa\"/></w:tblPr>";
    }

    public static String endTable() {
        return "</w:tbl>";
    }

    public static String tableRow(boolean isHeader, String... cells) {
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

    public static String buildCoverPage(String title, String subtitle, String subject) {
        StringBuilder sb = new StringBuilder();
        sb.append(p("TRƯỜNG ĐẠI HỌC KHOA HỌC TỰ NHIÊN / BÁCH KHOA", null, true, 14, "center"));
        sb.append(p("KHOA CÔNG NGHỆ THÔNG TIN", null, true, 13, "center"));
        sb.append(p("-------------------------***-------------------------", null, false, 12, "center"));
        sb.append(p("", null, false, 12, "center"));
        sb.append(p("BÁO CÁO ĐỒ ÁN MÔN HỌC", null, true, 16, "center"));
        sb.append(p(subject, null, true, 15, "center"));
        sb.append(p("", null, false, 12, "center"));
        sb.append(p("ĐỀ TÀI: HỆ THỐNG QUẢN LÝ VÀ ĐẶT LỊCH KHÁM PHÒNG KHÁM TƯ NHÂN (MEDCLINIC)", null, true, 14, "center"));
        sb.append(p(title, null, true, 18, "center"));
        if (subtitle != null && !subtitle.isEmpty()) {
            sb.append(p(subtitle, null, true, 14, "center"));
        }
        sb.append(p("", null, false, 12, "center"));
        sb.append(p("Giảng viên hướng dẫn: Bộ môn Công nghệ Phần mềm & Lập trình Web", null, false, 13, "left"));
        sb.append(p("Sinh viên thực hiện: Nhóm phát triển MedClinic", null, false, 13, "left"));
        sb.append(p("Học kỳ: 2 - Năm học: 2025 - 2026", null, false, 13, "left"));
        sb.append(pageBreak());
        return sb.toString();
    }

    public static String wrapDocument(String bodyContent) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
                "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" " +
                "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" " +
                "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" " +
                "xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">\n" +
                "<w:body>\n" +
                bodyContent +
                "<w:sectPr>" +
                "<w:pgSz w:w=\"12240\" w:h=\"15840\"/>" +
                "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>" +
                "</w:sectPr>\n</w:body></w:document>";
    }

    private static String getContentTypesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
                "  <Default Extension=\"png\" ContentType=\"image/png\"/>\n" +
                "  <Default Extension=\"jpeg\" ContentType=\"image/jpeg\"/>\n" +
                "  <Default Extension=\"jpg\" ContentType=\"image/jpeg\"/>\n" +
                "  <Default Extension=\"gif\" ContentType=\"image/gif\"/>\n" +
                "  <Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>\n" +
                "  <Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/>\n" +
                "  <Override PartName=\"/word/settings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml\"/>\n" +
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

    private static String getDocRelsXml(Map<String, String> imageRelationships) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n");
        sb.append("  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>\n");
        sb.append("  <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings\" Target=\"settings.xml\"/>\n");
        if (imageRelationships != null) {
            for (Map.Entry<String, String> entry : imageRelationships.entrySet()) {
                sb.append("  <Relationship Id=\"").append(entry.getKey()).append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"").append(entry.getValue()).append("\"/>\n");
            }
        }
        sb.append("</Relationships>");
        return sb.toString();
    }

    private static String getAppXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\"><Application>Microsoft Word</Application></Properties>";
    }

    private static String getCoreXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n" +
                "  <dc:title>MedClinic System Documentation</dc:title>\n" +
                "  <dc:creator>MedClinic Team</dc:creator>\n" +
                "</cp:coreProperties>";
    }

    private static String getSettingsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<w:settings xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:defaultTabStop w:val=\"720\"/></w:settings>";
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
                "    <w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/><w:b/><w:color w:val=\"1F4E79\"/><w:sz w:val=\"32\"/></w:rPr>\n" +
                "  </w:style>\n" +
                "  <w:style w:type=\"paragraph\" w:styleId=\"Heading2\">\n" +
                "    <w:name w:val=\"heading 2\"/>\n" +
                "    <w:basedOn w:val=\"Normal\"/>\n" +
                "    <w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/><w:b/><w:color w:val=\"2F5597\"/><w:sz w:val=\"28\"/></w:rPr>\n" +
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
