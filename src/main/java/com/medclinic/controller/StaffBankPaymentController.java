package com.medclinic.controller;

import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.BankTransferPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/staff/billing/bank")
@RequiredArgsConstructor
public class StaffBankPaymentController {
    private final BankTransferPaymentService paymentService;

    @PostMapping("/{invoiceId}")
    public String create(@PathVariable Long invoiceId) {
        var instruction = paymentService.createInstruction(invoiceId);
        return "redirect:/staff/billing/bank/payment/" + instruction.paymentId();
    }

    @GetMapping("/payment/{paymentId}")
    public String paymentPage(@PathVariable Long paymentId, Model model) {
        model.addAttribute("payment", paymentService.findInstruction(paymentId));
        return "staff/bank-payment";
    }

    @GetMapping("/payment/{paymentId}/status")
    @ResponseBody
    public Map<String, Object> status(@PathVariable Long paymentId) {
        var payment = paymentService.findInstruction(paymentId);
        return Map.of(
                "status", payment.status().name(),
                "confirmed", payment.confirmedAt() != null);
    }

    @PostMapping("/payment/{paymentId}/confirm")
    public String confirmManual(
            @PathVariable Long paymentId,
            @RequestParam String bankTransactionId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        Long actorId = userDetails == null ? null : userDetails.getId();
        paymentService.confirmManual(paymentId, bankTransactionId, actorId);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã đối soát chuyển khoản và cập nhật hóa đơn");
        return "redirect:/staff/billing";
    }
}
