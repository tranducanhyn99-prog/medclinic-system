package com.medclinic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import com.medclinic.util.PrescriptionValidation;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationFormDto {
    @NotBlank(message = "Vui lòng nhập triệu chứng.")
    @Size(max = 5000, message = "Triệu chứng không được vượt quá 5000 ký tự.")
    private String symptoms;
    @NotBlank(message = "Vui lòng nhập chẩn đoán.")
    @Size(max = 5000, message = "Chẩn đoán không được vượt quá 5000 ký tự.")
    private String diagnosis;
    @Size(max = 10000, message = "Lời dặn không được vượt quá 10000 ký tự.")
    private String doctorNotes;
    @Future(message = "Ngày tái khám phải sau hôm nay.")
    private LocalDate reExamDate;

    private List<Long> medicineIds;
    private List<Integer> quantities;
    private List<String> dosages;
    private List<String> frequencies;
    private List<String> instructions;

    @AssertTrue(message = "Đơn thuốc tối đa 100 dòng; mỗi dòng phải có thuốc, số lượng dương, liều dùng và tần suất tối đa 100 ký tự, hướng dẫn tối đa 255 ký tự.")
    public boolean isPrescriptionConsistent() {
        int size = medicineIds == null ? 0 : medicineIds.size();
        if (size == 0) return empty(quantities) && empty(dosages) && empty(frequencies) && empty(instructions);
        if (size > PrescriptionValidation.MAX_ITEMS || !sameSize(quantities, size)
                || !sameSize(dosages, size) || !sameSize(frequencies, size)
                || (!empty(instructions) && !sameSize(instructions, size))) return false;
        for (int i = 0; i < size; i++) {
            if (!PrescriptionValidation.validRow(medicineIds.get(i), quantities.get(i), dosages.get(i),
                    frequencies.get(i), empty(instructions) ? null : instructions.get(i))) return false;
        }
        return true;
    }

    private boolean empty(List<?> values) { return values == null || values.isEmpty(); }
    private boolean sameSize(List<?> values, int size) { return values != null && values.size() == size; }
}
