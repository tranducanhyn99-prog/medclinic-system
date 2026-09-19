package com.medclinic.generator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/** Generates the project DOCX artifacts into a caller-controlled workspace path. */
public class GenerateTemplateDocs {

    public static void main(String[] args) {
        Path outputDirectory = args.length > 0
                ? Paths.get(args[0])
                : Paths.get(System.getProperty("medclinic.docs.output", "docs/generated"));

        try {
            Files.createDirectories(outputDirectory);
            Path diagrams = outputDirectory.resolve("diagrams");
            DatabaseDiagramRenderer.renderAllDiagrams(diagrams);

            build(outputDirectory, "MedClinic_KhaoSat_HienTrang.docx", KhaoSatHienTrangDocBuilder.buildDocumentXml());
            build(outputDirectory, "MedClinic_XacDinhYeuCau.docx", XacDinhYeuCauDocBuilder.buildDocumentXml());
            build(outputDirectory, "MedClinic_DacTaUseCase.docx", DacTaUseCaseDocBuilder.buildDocumentXml());

            Map<String, byte[]> useCaseImages = new LinkedHashMap<>();
            useCaseImages.put("word/media/image1.png", Files.readAllBytes(diagrams.resolve("diagram_usecase_overview.png")));
            DocxModelHelper.buildDocxWithImages(outputDirectory.resolve("MedClinic_MoHinhHoaYeuCau.docx"),
                    MoHinhHoaYeuCauDocBuilder.buildDocumentXml(), useCaseImages);

            Map<String, byte[]> databaseImages = new LinkedHashMap<>();
            databaseImages.put("word/media/image1.png", Files.readAllBytes(diagrams.resolve("diagram_logical_erd.png")));
            databaseImages.put("word/media/image2.png", Files.readAllBytes(diagrams.resolve("diagram_rbac.png")));
            databaseImages.put("word/media/image3.png", Files.readAllBytes(diagrams.resolve("diagram_physical_schema.png")));
            DocxModelHelper.buildDocxWithImages(outputDirectory.resolve("MedClinic_ThietKeCSDL.docx"),
                    ThietKeCsdlDocBuilder.buildDocumentXml(), databaseImages);

            System.out.println("Generated DOCX artifacts in " + outputDirectory.toAbsolutePath());
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể tạo tài liệu DOCX", exception);
        }
    }

    private static void build(Path outputDirectory, String filename, String xml) throws Exception {
        DocxModelHelper.buildDocx(outputDirectory.resolve(filename), xml);
    }
}
