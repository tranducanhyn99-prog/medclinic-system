package com.medclinic.controller;

import com.medclinic.dto.UserRegisterDto;
import com.medclinic.entity.User;
import com.medclinic.enums.AuthChallengeType;
import com.medclinic.enums.Gender;
import com.medclinic.enums.RoleType;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.security.SecurityAuditService;
import com.medclinic.service.AuthVerificationService;
import com.medclinic.service.AvatarStorageService;
import com.medclinic.service.PasswordResetService;
import com.medclinic.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private static final String PENDING_OTP_CHALLENGE_ID = "PENDING_OTP_CHALLENGE_ID";
    private static final String CONTINUE_BOOKING = "CONTINUE_BOOKING";

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final AuthVerificationService authVerificationService;
    private final AvatarStorageService avatarStorageService;
    private final AuthenticationManager authenticationManager;
    private final SecurityAuditService securityAuditService;
    private final Environment environment;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String next,
                            HttpServletRequest request,
                            Model model) {
        model.addAttribute("demoLoginEnabled", environment.acceptsProfiles(Profiles.of("dev"))
                && !environment.acceptsProfiles(Profiles.of("prod")));
        if ("/patient/booking".equals(next)) {
            request.getSession(true).setAttribute(CONTINUE_BOOKING, Boolean.TRUE);
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công khỏi hệ thống!");
        }
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String handleLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {
        try {
            var authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));
            CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
            User user = userService.findById(details.getId());

            if (Boolean.TRUE.equals(user.getOtpBypass())) {
                establishSession(user, request);
                securityAuditService.authenticationSuccess(user.getUsername(), request.getRemoteAddr());
                redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công!");
                return "redirect:" + redirectFor(user, request);
            }

            var issued = authVerificationService.issueChallenge(user.getId(), AuthChallengeType.LOGIN);
            request.getSession(true).setAttribute(PENDING_OTP_CHALLENGE_ID, issued.challengeId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Mã OTP đã được gửi đến " + issued.maskedEmail() + ".");
            return "redirect:/auth/otp?challenge=" + issued.challengeId();
        } catch (AuthenticationException exception) {
            securityAuditService.authenticationFailure(username, request.getRemoteAddr(),
                    exception.getClass().getSimpleName());
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new UserRegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerDto") UserRegisterDto registerDto,
                                 BindingResult bindingResult,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            User user = userService.registerPatient(registerDto);
            var issued = authVerificationService.issueChallenge(user.getId(), AuthChallengeType.REGISTRATION);
            request.getSession(true).setAttribute(PENDING_OTP_CHALLENGE_ID, issued.challengeId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tài khoản đã được tạo. Hãy xác thực email " + issued.maskedEmail() + " để kích hoạt.");
            return "redirect:/auth/otp?challenge=" + issued.challengeId();
        } catch (Exception exception) {
            model.addAttribute("errorMessage", com.medclinic.util.PublicErrorMessages.forException(exception));
            return "auth/register";
        }
    }

    @GetMapping("/auth/otp")
    public String otpPage(@RequestParam Long challenge, Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            var info = authVerificationService.getChallengeInfo(challenge);
            long secondsRemaining = Math.max(0L,
                    Duration.between(LocalDateTime.now(), info.expiresAt()).getSeconds());
            model.addAttribute("challenge", info);
            model.addAttribute("secondsRemaining", secondsRemaining);
            return "auth/verify-otp";
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", com.medclinic.util.PublicErrorMessages.forException(exception));
            return "redirect:/login";
        }
    }

    @PostMapping("/auth/otp")
    public String verifyOtp(@RequestParam Long challengeId,
                            @RequestParam String code,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        try {
            var result = authVerificationService.verifyCode(challengeId, code);
            clearPendingChallenge(request);
            establishSession(result.user(), request);
            securityAuditService.authenticationSuccess(result.user().getUsername(), request.getRemoteAddr());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Xác thực thành công. Bạn đã đăng nhập vào MedClinic.");
            return "redirect:" + redirectFor(result.user(), request);
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", com.medclinic.util.PublicErrorMessages.forException(exception));
            return "redirect:/auth/otp?challenge=" + challengeId;
        }
    }

    @PostMapping("/auth/otp/resend")
    public String resendOtp(@RequestParam Long challengeId,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null || !challengeId.equals(session.getAttribute(PENDING_OTP_CHALLENGE_ID))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên xác thực không hợp lệ. Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }
        try {
            var issued = authVerificationService.resendChallenge(challengeId);
            session.setAttribute(PENDING_OTP_CHALLENGE_ID, issued.challengeId());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Mã OTP mới đã được gửi đến " + issued.maskedEmail() + ".");
            return "redirect:/auth/otp?challenge=" + issued.challengeId();
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", com.medclinic.util.PublicErrorMessages.forException(exception));
            return "redirect:/auth/otp?challenge=" + challengeId;
        }
    }

    @GetMapping("/auth/verify-email")
    public String verifyEmailLink(@RequestParam String token,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        try {
            var result = authVerificationService.verifyMagicToken(token);
            establishSession(result.user(), request);
            securityAuditService.authenticationSuccess(result.user().getUsername(), request.getRemoteAddr());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Email đã được xác thực. Bạn đã đăng nhập thành công vào MedClinic.");
            return "redirect:" + redirectFor(result.user(), request);
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", com.medclinic.util.PublicErrorMessages.forException(exception));
            return "redirect:/login";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam String email, RedirectAttributes redirectAttributes) {
        passwordResetService.requestPasswordReset(email);
        redirectAttributes.addFlashAttribute("successMessage",
                "Nếu email tồn tại, chúng tôi đã gửi liên kết khôi phục mật khẩu.");
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.resetPassword(token, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", com.medclinic.util.PublicErrorMessages.forException(exception));
            return "redirect:/reset-password?token=" + token;
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
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) Gender gender,
                                @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                @RequestParam(required = false) String address,
                                @RequestParam(required = false) MultipartFile avatar,
                                RedirectAttributes redirectAttributes) {
        try {
            User current = userService.findById(userDetails.getId());
            userService.updateUserProfile(userDetails.getId(), fullName, phone, gender, dateOfBirth, address);
            if (avatar != null && !avatar.isEmpty()) {
                String avatarUrl = avatarStorageService.storeAvatar(avatar, current.getAvatarUrl());
                try {
                    userService.updateAvatar(userDetails.getId(), avatarUrl);
                } catch (Exception exception) {
                    avatarStorageService.deleteAvatar(avatarUrl);
                    throw exception;
                }
                avatarStorageService.deleteAvatar(current.getAvatarUrl());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ cá nhân thành công!");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + com.medclinic.util.PublicErrorMessages.forException(exception));
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
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("errorMessage", com.medclinic.util.PublicErrorMessages.forException(exception));
        }
        return "redirect:/profile";
    }

    private void establishSession(User user, HttpServletRequest request) {
        user = userService.findById(user.getId());
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new com.medclinic.exception.BusinessLogicException("Tài khoản đã bị khóa. Vui lòng liên hệ phòng khám.");
        }
        CustomUserDetails details = new CustomUserDetails(user);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                details, null, details.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session = request.getSession(false);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    private void clearPendingChallenge(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(PENDING_OTP_CHALLENGE_ID);
        }
    }

    private String redirectFor(User user, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean continueBooking = session != null && Boolean.TRUE.equals(session.getAttribute(CONTINUE_BOOKING));
        if (session != null) {
            session.removeAttribute(CONTINUE_BOOKING);
        }
        if (continueBooking && user.getRole().getName() == RoleType.ROLE_PATIENT) {
            return "/patient/booking";
        }
        return switch (user.getRole().getName()) {
            case ROLE_ADMIN -> "/admin/dashboard";
            case ROLE_DOCTOR -> "/doctor/desk";
            case ROLE_STAFF -> "/staff/reception";
            case ROLE_PATIENT -> "/patient/appointments";
        };
    }
}
