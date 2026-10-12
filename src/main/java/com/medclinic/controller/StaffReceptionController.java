package com.medclinic.controller;

import com.medclinic.enums.PaymentMethod;
import com.medclinic.enums.PaymentStatus;
import com.medclinic.service.AppointmentService;
import com.medclinic.service.DoctorService;
import com.medclinic.service.InvoiceService;
import com.medclinic.service.BankTransferPaymentService;
import com.medclinic.service.SpecialtyService;
import com.medclinic.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffReceptionController {

    private java.time.Clock businessClock = java.time.Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
    @org.springframework.beans.factory.annotation.Autowired
    public void setBusinessClock(java.time.Clock clock) { this.businessClock = clock; }

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final SpecialtyService specialtyService;
    private final InvoiceService invoiceService;
    private final BankTransferPaymentService bankTransferPaymentService;

    @GetMapping("/reception")
    public String receptionDesk(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            Model model) {
        java.util.List<com.medclinic.entity.Appointment> todayAppointments = appointmentService.findTodayAppointments();
        java.util.List<com.medclinic.entity.Appointment> appointments = todayAppointments;

        if (keyword != null && !keyword.isBlank()) {
            String normalizedKeyword = keyword.trim();
            appointments = appointmentService.searchForReception(normalizedKeyword, LocalDate.now(businessClock));
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
        model.addAttribute("appointments", appointmentService.searchForReception(keyword, LocalDate.now(businessClock)));
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
        model.addAttribute("today", LocalDate.now(businessClock));
        model.addAttribute("doctors", doctorService.filterDoctors(null, null, null));
        model.addAttribute("specialties", specialtyService.findAllActive());
        return "staff/walk-in";
    }

    @PostMapping("/walk-in")
    public String processWalkIn(@RequestParam("fullName") String fullName,
                                @RequestParam("phone") String phone,
                                @RequestParam(value = "dateOfBirth", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                @RequestParam(value = "gender", required = false) com.medclinic.enums.Gender gender,
                                @RequestParam(value = "address", required = false) String address,
                                @RequestParam("doctorId") Long doctorId,
                                @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                @RequestParam("slotId") Long slotId,
                                @RequestParam(value = "reason", required = false) String reason,
                                RedirectAttributes redirectAttributes) {
        appointmentService.bookWalkInAppointment(fullName, phone, dateOfBirth, gender, address, doctorId, date, slotId, reason);
        redirectAttributes.addFlashAttribute("successMessage", "Đã đăng ký khám trực tiếp thành công");
        return "redirect:/staff/reception";
    }

    @GetMapping("/billing")
    public String billingList(
            @RequestParam(defaultValue = "UNPAID") String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size, Model model) {
        if (!java.util.Set.of("UNPAID", "PAID", "REFUNDED", "ALL").contains(status)) {
            status = "UNPAID";
            model.addAttribute("errorMessage", "Trạng thái không hợp lệ; đang hiển thị hóa đơn chưa trả.");
        }
        PaymentStatus paymentStatus = "ALL".equals(status) ? null : PaymentStatus.valueOf(status);
        var invoices = invoiceService.findForBilling(paymentStatus, from, to, page, size);
        long unpaid = invoiceService.countForBilling(PaymentStatus.UNPAID, from, to);
        long paid = invoiceService.countForBilling(PaymentStatus.PAID, from, to);
        long refunded = invoiceService.countForBilling(PaymentStatus.REFUNDED, from, to);
        model.addAttribute("invoices", invoices.getContent());
        model.addAttribute("invoicePage", invoices);
        model.addAttribute("currentStatus", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("countAll", unpaid + paid + refunded);
        model.addAttribute("countUnpaid", unpaid);
        model.addAttribute("countPaid", paid);
        model.addAttribute("bankPaymentAvailable", bankTransferPaymentService.isAvailable());
        return "staff/billing";
    }

    @PostMapping("/billing/pay/{invoiceId}")
    public String processPayment(@PathVariable Long invoiceId, 
                                 @RequestParam("method") PaymentMethod method,
                                 @RequestParam(defaultValue = "UNPAID") String status,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "25") int size,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        invoiceService.processPayment(invoiceId, method, userDetails == null ? null : userDetails.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Thanh toán thành công");
        if (!"UNPAID".equals(status) || from != null || to != null || page != 0 || size != 25) {
            redirectAttributes.addAttribute("status", status);
            if (from != null) redirectAttributes.addAttribute("from", from.toString());
            if (to != null) redirectAttributes.addAttribute("to", to.toString());
            redirectAttributes.addAttribute("page", Math.max(0, page));
            redirectAttributes.addAttribute("size", Math.max(1, Math.min(100, size)));
        }
        return "redirect:/staff/billing";
    }
}
