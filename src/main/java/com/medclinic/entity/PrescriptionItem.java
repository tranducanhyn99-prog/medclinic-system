package com.medclinic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "prescription_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 100)
    private String dosage; // e.g. 1 viên / lần

    @Column(nullable = false, length = 100)
    private String frequency; // e.g. 2 lần / ngày (Sáng, Tối)

    @Column(length = 255)
    private String instructions; // e.g. Uống sau khi ăn no 30 phút

    public BigDecimal calculateSubtotal() {
        if (medicine == null || medicine.getUnitPrice() == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return medicine.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }
}

