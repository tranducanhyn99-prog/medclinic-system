package com.medclinic.controller;

import com.medclinic.dto.BankTransferCallbackDto;
import com.medclinic.dto.BankTransferInstruction;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.service.BankTransferPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/bank")
@RequiredArgsConstructor
public class BankPaymentCallbackController {
    private final BankTransferPaymentService paymentService;

    @PostMapping("/callback")
    public ResponseEntity<?> callback(
            @Valid @RequestBody BankTransferCallbackDto callback,
            @RequestHeader(value = "X-MedClinic-Signature", required = false) String signature) {
        try {
            BankTransferInstruction confirmed = paymentService.confirmCallback(callback, signature);
            return ResponseEntity.ok(Map.of(
                    "accepted", true,
                    "paymentId", confirmed.paymentId(),
                    "status", confirmed.status().name()));
        } catch (SecurityException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("accepted", false, "code", "INVALID_SIGNATURE"));
        } catch (BusinessLogicException exception) {
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of("accepted", false, "code", "PAYMENT_REJECTED"));
        }
    }
}
