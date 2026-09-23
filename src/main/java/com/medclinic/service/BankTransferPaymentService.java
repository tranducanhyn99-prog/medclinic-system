package com.medclinic.service;

import com.medclinic.dto.BankTransferCallbackDto;
import com.medclinic.dto.BankTransferInstruction;

public interface BankTransferPaymentService {
    BankTransferInstruction createInstruction(Long invoiceId);
    BankTransferInstruction findInstruction(Long paymentId);
    BankTransferInstruction confirmCallback(BankTransferCallbackDto callback, String signature);
    BankTransferInstruction confirmManual(Long paymentId, String bankTransactionId, Long actorId);
    boolean isAvailable();
}
