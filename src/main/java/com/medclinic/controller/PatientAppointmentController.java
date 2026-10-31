package com.medclinic.controller;

import com.medclinic.entity.Appointment;
import com.medclinic.entity.MedicalRecord;
import com.medclinic.dto.BookingFormDto;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.AppointmentService;
import com.medclinic.service.DoctorService;
import com.medclinic.service.SpecialtyService;
import com.medclinic.service.MedicalRecordService;
import com.medclinic.util.QrCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientAppointmentController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final SpecialtyService specialtyService;
    private final MedicalRecordService medicalRecordService;

    @GetMapping("/appointments")
    public String listAppointments(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        appointmentService.expireOverdueTodayAppointments();
        List<Appointment> appointments = appointmentService.findByPatient(userDetails.getId());
        model.addAttribute("appointments", appointments);
        return "patient/appointments";
    }

    @GetMapping("/appointments/{id}")
    public String appointmentDetail(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        appointmentService.expireOverdueTodayAppointments();
        Appointment appointment = appointmentService.findById(id);
        
        if (!appointment.getPatient().getId().equals(userDetails.getId())) {
            return "redirect:/patient/appointments";
        }
        
        String qrCode = QrCodeUtil.generateQrCodeBase64(appointment.getBookingCode(), 200, 200);
        
        model.addAttribute("appointment", appointment);
        model.addAttribute("qrCode", qrCode);
        return "patient/appointment-detail";
    }

    @GetMapping("/booking")
    public String bookingForm(@RequestParam(required = false) Long doctorId, Model model) {
        prepareBookingForm(model);
        if (doctorId != null) {
            var doctor = doctorService.findById(doctorId);
            if (!com.medclinic.util.DoctorAvailability.acceptsBookings(doctor)) {
                throw new com.medclinic.exception.ResourceNotFoundException("Bác sĩ hiện không nhận lịch khám.");
            }
            BookingFormDto form = (BookingFormDto) model.getAttribute("bookingForm");
            form.setSpecialtyId(doctor.getSpecialty().getId());
            form.setDoctorId(doctor.getId());
        }
        return "patient/booking";
    }

    @PostMapping("/booking")
    public String processBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("bookingForm") BookingFormDto form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookingForm", form);
            prepareBookingForm(model);
            return "patient/booking";
        }
        try {
            Appointment newAppointment = appointmentService.bookAppointment(userDetails.getId(), form.getDoctorId(), form.getDate(), form.getSlotId(), form.getReason());
            redirectAttributes.addFlashAttribute("successMsg", "Đặt lịch khám thành công!");
            return "redirect:/patient/appointments/" + newAppointment.getId();
        } catch (Exception e) {
            model.addAttribute("bookingForm", form);
            model.addAttribute("errorMessage", com.medclinic.util.PublicErrorMessages.forException(e));
            prepareBookingForm(model);
            return "patient/booking";
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelAppointment(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMsg", "Hủy lịch khám thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", com.medclinic.util.PublicErrorMessages.forException(e));
        }
        return "redirect:/patient/appointments";
    }

    @GetMapping("/medical-history")
    public String medicalHistory(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<MedicalRecord> records = medicalRecordService.getPatientMedicalHistory(userDetails.getId());
        model.addAttribute("records", records);
        return "patient/medical-history";
    }

    private void prepareBookingForm(Model model) {
        model.addAttribute("specialties", specialtyService.findAllActive());
        model.addAttribute("doctors", doctorService.filterDoctors(null, null, null));
        if (!model.containsAttribute("bookingForm")) {
            model.addAttribute("bookingForm", new BookingFormDto());
        }
    }
}
