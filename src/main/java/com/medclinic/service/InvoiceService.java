package com.medclinic.service;

import com.medclinic.entity.Invoice;
import com.medclinic.enums.PaymentMethod;
import com.medclinic.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public interface InvoiceService {
    Invoice createInvoiceForAppointment(Long appointmentId, BigDecimal medicineFee);
    Invoice processPayment(Long invoiceId, PaymentMethod paymentMethod);
    Invoice findByAppointmentId(Long appointmentId);
    Invoice findById(Long invoiceId);
    List<Invoice> findAllByStatus(PaymentStatus status);
    List<Invoice> findAll();
}
