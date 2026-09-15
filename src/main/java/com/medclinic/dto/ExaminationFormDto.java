package com.medclinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationFormDto {
    private String symptoms;
    private String diagnosis;
    private String doctorNotes;
    private LocalDate reExamDate;

    private List<Long> medicineIds;
    private List<Integer> quantities;
    private List<String> dosages;
    private List<String> frequencies;
    private List<String> instructions;
}
