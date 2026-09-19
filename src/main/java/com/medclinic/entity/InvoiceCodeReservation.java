package com.medclinic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoice_code_reservations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceCodeReservation {
    @Id
    @Column(name = "invoice_code", nullable = false, unique = true, length = 30)
    private String invoiceCode;
}
