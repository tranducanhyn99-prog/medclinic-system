package com.medclinic.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminMedicineDto {
    private Long id;
    @NotBlank(message = "Tên thuốc không được để trống") @Size(max = 100, message = "Tên thuốc tối đa 100 ký tự") private String name;
    @NotBlank(message = "Đơn vị tính không được để trống") @Size(max = 30, message = "Đơn vị tính tối đa 30 ký tự") private String unit;
    @NotNull(message = "Đơn giá không được để trống") @DecimalMin(value = "0", message = "Đơn giá không hợp lệ") private BigDecimal unitPrice;
    private String usageGuide;
    @NotNull(message = "Tồn kho không được để trống") @Min(value = 0, message = "Tồn kho không hợp lệ") private Integer stockQuantity;
}
