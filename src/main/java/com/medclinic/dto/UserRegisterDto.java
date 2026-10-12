package com.medclinic.dto;

import com.medclinic.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class UserRegisterDto {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 30, message = "Tên đăng nhập từ 3 đến 30 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @com.medclinic.validation.ValidPassword
    private String password;

    @NotBlank(message = "Vui lòng xác nhận lại mật khẩu")
    private String confirmPassword;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = com.medclinic.util.PhoneNumbers.PATTERN, message = com.medclinic.util.PhoneNumbers.MESSAGE)
    private String phone;

    public void setPhone(String phone) { this.phone = phone == null ? null : phone.trim(); }

    private Gender gender = Gender.MALE;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String address;
}
