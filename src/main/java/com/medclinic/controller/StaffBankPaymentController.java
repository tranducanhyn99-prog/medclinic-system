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
import java.util.LinkedHashMap;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/staff/billing/bank")
@RequiredArgsConstructor
public class StaffBankPaymentController {
    private final BankTransferPaymentService paymentService;

    @PostMapping("/{invoiceId}")
    public String create(@PathVariable Long invoiceId, @RequestParam Map<String, String> filters,
                         RedirectAttributes redirectAttributes) {
        var instruction = paymentService.createInstruction(invoiceId);
        billingFilters(filters).forEach(redirectAttributes::addAttribute);
        return "redirect:/staff/billing/bank/payment/" + instruction.paymentId();
    }

    @GetMapping("/payment/{paymentId}")
    public String paymentPage(@PathVariable Long paymentId, @RequestParam Map<String, String> filters, Model model) {
        model.addAttribute("payment", paymentService.findInstruction(paymentId));
        var billingFilters = billingFilters(filters);
        model.addAttribute("billingFilters", billingFilters);
        var returnUrl = UriComponentsBuilder.fromPath("/staff/billing");
        billingFilters.forEach((name, value) -> returnUrl.queryParam(name, value));
        model.addAttribute("billingReturnUrl", returnUrl.build().encode().toUriString());
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
            @RequestParam Map<String, String> filters,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        Long actorId = userDetails == null ? null : userDetails.getId();
        paymentService.confirmManual(paymentId, bankTransactionId, actorId);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã đối soát chuyển khoản và cập nhật hóa đơn");
        billingFilters(filters).forEach(redirectAttributes::addAttribute);
        return "redirect:/staff/billing";
    }

    private Map<String, String> billingFilters(Map<String, String> parameters) {
        Map<String, String> filters = new LinkedHashMap<>();
        for (String key : new String[]{"status", "from", "to", "page", "size"}) {
            String value = parameters.get(key);
            if (value != null && !value.isBlank()) filters.put(key, value);
        }
        return filters;
    }
}
