package com.medclinic;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.medclinic.entity.*;
import com.medclinic.repository.AppointmentRepository;
import com.medclinic.repository.InvoiceRepository;
import com.medclinic.service.PdfExportService;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrescriptionPdfPresentationTest {
    @Test void printsSavedClinicalNotesAndFrequencyWhenInstructionsAreEmpty() throws Exception {
        String text = export(null, "");
        assertTrue(text.contains("2 lần/ngày"));
        assertTrue(text.contains("Nghỉ ngơi DEMO"));
        assertTrue(text.contains("Paracetamol"));
    }

    @Test void retainsPrescriptionSpecificNotesAndAdditionalInstructions() throws Exception {
        String text = export("Lời dặn riêng DEMO", "Uống sau ăn");
        assertTrue(text.contains("2 lần/ngày"));
        assertTrue(text.contains("Uống sau ăn"));
        assertTrue(text.contains("Lời dặn riêng DEMO"));
        assertFalse(text.contains("Nghỉ ngơi DEMO"));
    }

    private String export(String prescriptionNotes, String instructions) throws Exception {
        var patient = User.builder().id(7L).fullName("Bệnh nhân DEMO").build();
        var doctor = DoctorProfile.builder().user(User.builder().id(8L).fullName("Bác sĩ DEMO").build()).build();
        var item = PrescriptionItem.builder().medicine(Medicine.builder().name("Paracetamol").build())
                .quantity(2).dosage("1 viên").frequency("2 lần/ngày").instructions(instructions).build();
        var prescription = Prescription.builder().doctorNotes(prescriptionNotes).items(List.of(item)).build();
        var record = MedicalRecord.builder().diagnosis("DEMO").doctorNotes("Nghỉ ngơi DEMO").prescription(prescription).build();
        var appointment = Appointment.builder().id(14L).patient(patient).doctor(doctor).medicalRecord(record).build();
        var appointments = mock(AppointmentRepository.class);
        when(appointments.findById(14L)).thenReturn(Optional.of(appointment));
        byte[] pdf = new PdfExportService(appointments, mock(InvoiceRepository.class)).exportPrescriptionPdf(14L,7L,"ROLE_PATIENT");
        try (PdfReader reader = new PdfReader(pdf)) {
            return new PdfTextExtractor(reader).getTextFromPage(1);
        }
    }
}
