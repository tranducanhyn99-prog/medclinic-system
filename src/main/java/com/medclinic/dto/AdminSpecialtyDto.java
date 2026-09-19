package com.medclinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminSpecialtyDto {
    private Long id;
    @NotBlank(message = "Tên chuyên khoa không được để trống") @Size(max = 100, message = "Tên chuyên khoa tối đa 100 ký tự")
    private String name;
    private String description;
    @Size(max = 100, message = "Tên icon tối đa 100 ký tự") private String icon;
}
