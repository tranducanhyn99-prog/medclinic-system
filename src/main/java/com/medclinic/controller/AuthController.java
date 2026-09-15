package com.medclinic.controller;

import com.medclinic.dto.UserRegisterDto;
import com.medclinic.entity.User;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công khỏi hệ thống!");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new UserRegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerDto") UserRegisterDto registerDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerPatient(registerDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findById(userDetails.getId());
        model.addAttribute("user", user);
        return "auth/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam String address,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserProfile(userDetails.getId(), fullName, phone, address);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!");
            return "redirect:/profile";
        }
        try {
            userService.changePassword(userDetails.getId(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }
}

