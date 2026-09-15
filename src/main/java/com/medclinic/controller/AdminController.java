package com.medclinic.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medclinic.service.DashboardService;
import com.medclinic.service.DoctorService;
import com.medclinic.service.MedicineService;
import com.medclinic.service.SpecialtyService;
import com.medclinic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final SpecialtyService specialtyService;
    private final DoctorService doctorService;
    private final MedicineService medicineService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) throws JsonProcessingException {
        model.addAttribute("totalPatients", dashboardService.countTotalPatients());
        model.addAttribute("totalDoctors", dashboardService.countTotalDoctors());
        model.addAttribute("todayAppointments", dashboardService.countTodayAppointments());
        model.addAttribute("totalRevenue", dashboardService.getTotalRevenue());
        
        model.addAttribute("statusChartData", objectMapper.writeValueAsString(dashboardService.getAppointmentsByStatusGroup()));
        model.addAttribute("specialtyChartData", objectMapper.writeValueAsString(dashboardService.getAppointmentsBySpecialtyGroup()));
        
        return "admin/dashboard";
    }

    @GetMapping("/specialties")
    public String listSpecialties(Model model) {
        model.addAttribute("specialties", specialtyService.findAll());
        return "admin/specialties";
    }

    @PostMapping("/specialties")
    public String addSpecialty(@RequestParam("name") String name,
                               @RequestParam(value = "description", required = false) String description,
                               @RequestParam(value = "icon", required = false) String icon,
                               RedirectAttributes redirectAttributes) {
        specialtyService.save(name, description, icon);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm chuyên khoa thành công");
        return "redirect:/admin/specialties";
    }

    @PostMapping("/specialties/{id}/toggle")
    public String toggleSpecialty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        specialtyService.toggleStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái chuyên khoa");
        return "redirect:/admin/specialties";
    }

    @GetMapping("/doctors")
    public String listDoctors(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        model.addAttribute("specialties", specialtyService.findAllActive());
        return "admin/doctors";
    }

    @PostMapping("/doctors")
    public String addDoctor(@RequestParam("userId") Long userId,
                            @RequestParam("specialtyId") Long specialtyId,
                            @RequestParam("title") String title,
                            @RequestParam("experienceYears") Integer experienceYears,
                            @RequestParam("consultationFee") BigDecimal consultationFee,
                            @RequestParam(value = "bio", required = false) String bio,
                            RedirectAttributes redirectAttributes) {
        doctorService.save(userId, specialtyId, title, experienceYears, consultationFee, bio);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm bác sĩ thành công");
        return "redirect:/admin/doctors";
    }

    @GetMapping("/medicines")
    public String listMedicines(Model model) {
        model.addAttribute("medicines", medicineService.findAll());
        return "admin/medicines";
    }

    @PostMapping("/medicines")
    public String saveMedicine(@RequestParam(value = "id", required = false) Long id,
                               @RequestParam("name") String name,
                               @RequestParam("unit") String unit,
                               @RequestParam("unitPrice") BigDecimal unitPrice,
                               @RequestParam("usageGuide") String usageGuide,
                               @RequestParam("stockQuantity") Integer stockQuantity,
                               RedirectAttributes redirectAttributes) {
        if (id != null) {
            medicineService.update(id, name, unit, unitPrice, usageGuide, stockQuantity);
        } else {
            medicineService.save(name, unit, unitPrice, usageGuide, stockQuantity);
        }
        redirectAttributes.addFlashAttribute("successMessage", "Lưu thuốc thành công");
        return "redirect:/admin/medicines";
    }

    @PostMapping("/medicines/{id}/toggle")
    public String toggleMedicine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        medicineService.toggleStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái thuốc");
        return "redirect:/admin/medicines";
    }

    @GetMapping("/users")
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("users", userService.searchUsers(keyword));
        model.addAttribute("keyword", keyword);
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái người dùng");
        return "redirect:/admin/users";
    }
}
