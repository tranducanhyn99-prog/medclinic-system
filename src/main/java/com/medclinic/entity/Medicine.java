package com.medclinic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 255)
    private String usageGuide;

    @Builder.Default
    @Column(nullable = false)
    private Integer stockQuantity = 1000;

    @Builder.Default
    @Column(nullable = false)
    private Integer reorderLevel = 50;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Version
    private Long version;
}
