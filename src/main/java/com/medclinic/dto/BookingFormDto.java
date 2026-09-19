package com.medclinic.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class BookingFormDto {
    private Long specialtyId;

    @NotNull(message = "Vui lòng chọn bác sĩ")
    private Long doctorId;

    @NotNull(message = "Vui lòng chọn ngày khám")
    @FutureOrPresent(message = "Ngày khám không thể ở quá khứ")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @NotNull(message = "Vui lòng chọn khung giờ")
    private Long slotId;

    @NotBlank(message = "Lý do khám là bắt buộc")
    @Size(max = 1000, message = "Lý do khám không được vượt quá 1000 ký tự")
    private String reason;
}
