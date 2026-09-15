package com.medclinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItemDto {
    private Long medicineId;
    private Integer quantity;
    private String dosage;
    private String frequency;
    private String instructions;
}
