package com.medclinic.service.impl;

import com.medclinic.repository.InvoiceCodeReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceCodeReservationWriter {
    private final InvoiceCodeReservationRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserve(String code) {
        repository.insert(code);
    }
}
