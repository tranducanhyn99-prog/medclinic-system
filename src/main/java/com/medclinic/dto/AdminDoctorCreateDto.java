package com.medclinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminDoctorCreateDto {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 30, message = "Tên đăng nhập từ 3 đến 30 ký tự")
    private String username;
    @NotBlank(message = "Mật khẩu không được để trống")
    @com.medclinic.validation.ValidPassword
    private String password;
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = com.medclinic.util.PhoneNumbers.PATTERN, message = com.medclinic.util.PhoneNumbers.MESSAGE)
    private String phone;
    public void setPhone(String phone) { this.phone = phone == null ? null : phone.trim(); }
    @NotBlank(message = "Số giấy phép hành nghề không được để trống")
    @Size(max = 50, message = "Số giấy phép hành nghề quá dài")
    private String licenseNumber;
    @NotNull(message = "Vui lòng chọn chuyên khoa")
    private Long specialtyId;
    @NotBlank(message = "Học vị không được để trống")
    private String title;
    @NotNull(message = "Số năm kinh nghiệm không được để trống") @Min(value = 0, message = "Số năm kinh nghiệm không hợp lệ")
    private Integer experienceYears;
    @NotNull(message = "Giá khám không được để trống") @com.medclinic.validation.ValidVnd
    private BigDecimal consultationFee;
    private String bio;
}
