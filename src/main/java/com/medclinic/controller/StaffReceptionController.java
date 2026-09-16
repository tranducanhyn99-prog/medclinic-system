package com.medclinic.controller;

import com.medclinic.enums.PaymentMethod;
import com.medclinic.service.AppointmentService;
import com.medclinic.service.DoctorService;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffReceptionController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final SpecialtyService specialtyService;
    private final InvoiceService invoiceService;

    @GetMapping("/reception")
    public String receptionDesk(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            Model model) {
        java.util.List<com.medclinic.entity.Appointment> todayAppointments = appointmentService.findTodayAppointments();
        java.util.List<com.medclinic.entity.Appointment> appointments = todayAppointments;

        if (keyword != null && !keyword.isBlank()) {
            String normalizedKeyword = keyword.trim();
            appointments = appointmentService.searchForReception(normalizedKeyword);
            model.addAttribute("keyword", normalizedKeyword);
        }

        long countAll = todayAppointments.size();
        long countConfirmed = todayAppointments.stream()
                .filter(appointment -> appointment.getStatus() == com.medclinic.enums.AppointmentStatus.CONFIRMED)
                .count();
        long countCheckedIn = todayAppointments.stream()
                .filter(appointment -> appointment.getStatus() == com.medclinic.enums.AppointmentStatus.CHECKED_IN)
                .count();
        long countInProgress = todayAppointments.stream()
                .filter(appointment -> appointment.getStatus() == com.medclinic.enums.AppointmentStatus.IN_PROGRESS)
                .count();
        long countCompleted = todayAppointments.stream()
                .filter(appointment -> appointment.getStatus() == com.medclinic.enums.AppointmentStatus.COMPLETED)
                .count();
        long countCancelled = todayAppointments.stream()
                .filter(appointment -> appointment.getStatus() == com.medclinic.enums.AppointmentStatus.CANCELLED)
                .count();

        if (!"ALL".equalsIgnoreCase(status)) {
            appointments = appointments.stream()
                    .filter(appointment -> appointment.getStatus().name().equalsIgnoreCase(status))
                    .toList();
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("currentStatus", status.toUpperCase());
        model.addAttribute("countAll", countAll);
        model.addAttribute("countConfirmed", countConfirmed);
        model.addAttribute("countCheckedIn", countCheckedIn);
        model.addAttribute("countInProgress", countInProgress);
        model.addAttribute("countCompleted", countCompleted);
        model.addAttribute("countCancelled", countCancelled);
        return "staff/reception";
    }
    @GetMapping("/reception/search")
    public String searchAppointments(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("appointments", appointmentService.searchForReception(keyword));
        model.addAttribute("keyword", keyword);
        return "staff/reception";
    }

    @PostMapping("/reception/check-in/{appointmentId}")
    public String checkInPatient(@PathVariable Long appointmentId, RedirectAttributes redirectAttributes) {
        appointmentService.checkInAppointment(appointmentId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã check-in thành công và cấp số thứ tự");
        return "redirect:/staff/reception";
    }

    @GetMapping("/walk-in")
    public String walkInForm(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        model.addAttribute("specialties", specialtyService.findAllActive());
        return "staff/walk-in";
    }

    @PostMapping("/walk-in")
    public String processWalkIn(@RequestParam("fullName") String fullName,
                                @RequestParam("phone") String phone,
                                @RequestParam("doctorId") Long doctorId,
                                @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                @RequestParam("slotId") Long slotId,
                                @RequestParam(value = "reason", required = false) String reason,
                                RedirectAttributes redirectAttributes) {
        appointmentService.bookWalkInAppointment(fullName, phone, doctorId, date, slotId, reason);
        redirectAttributes.addFlashAttribute("successMessage", "Đã đăng ký khám trực tiếp thành công");
        return "redirect:/staff/reception";
    }

    @GetMapping("/billing")
    public String billingList(Model model) {
        model.addAttribute("invoices", invoiceService.findAll());
        return "staff/billing";
    }

    @PostMapping("/billing/pay/{invoiceId}")
    public String processPayment(@PathVariable Long invoiceId, 
                                 @RequestParam("method") PaymentMethod method,
                                 RedirectAttributes redirectAttributes) {
        invoiceService.processPayment(invoiceId, method);
        redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công");
        return "redirect:/staff/billing";
    }
}
