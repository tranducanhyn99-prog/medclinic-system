package com.medclinic.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medclinic.service.DashboardService;
import com.medclinic.service.DoctorService;
import com.medclinic.service.MedicineService;
import com.medclinic.service.SpecialtyService;
import com.medclinic.service.UserService;
import com.medclinic.dto.AdminDoctorCreateDto;
import com.medclinic.dto.AdminMedicineDto;
import com.medclinic.dto.AdminSpecialtyDto;
import com.medclinic.exception.BusinessLogicException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;

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
        prepareSpecialties(model);
        return "admin/specialties";
    }

    @PostMapping("/specialties")
    public String addSpecialty(@Valid @ModelAttribute("specialtyForm") AdminSpecialtyDto form,
                               BindingResult bindingResult, Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) { model.addAttribute("specialtyForm", form); prepareSpecialties(model); return "admin/specialties"; }
        try {
        if (form.getId() == null) {
            specialtyService.save(form.getName(), form.getDescription(), form.getIcon());
            redirectAttributes.addFlashAttribute("successMessage", "Thêm chuyên khoa thành công");
        } else {
            specialtyService.update(form.getId(), form.getName(), form.getDescription(), form.getIcon());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chuyên khoa thành công");
        }
        } catch (BusinessLogicException ex) { bindingResult.rejectValue("name", "duplicate", ex.getMessage()); model.addAttribute("specialtyForm", form); prepareSpecialties(model); return "admin/specialties"; }
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
        prepareDoctors(model);
        return "admin/doctors";
    }

    @PostMapping("/doctors")
    public String addDoctor(@Valid @ModelAttribute("doctorForm") AdminDoctorCreateDto form,
                            BindingResult bindingResult, Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) { model.addAttribute("doctorForm", form); prepareDoctors(model); return "admin/doctors"; }
        try { doctorService.registerDoctor(form); }
        catch (BusinessLogicException ex) { bindingResult.reject("doctor", ex.getMessage()); model.addAttribute("doctorForm", form); prepareDoctors(model); return "admin/doctors"; }
        redirectAttributes.addFlashAttribute("successMessage", "Thêm bác sĩ thành công");
        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/{id}")
    public String updateDoctor(@PathVariable Long id,
                               @RequestParam("specialtyId") Long specialtyId,
                               @RequestParam("title") String title,
                               @RequestParam("experienceYears") Integer experienceYears,
                               @RequestParam("consultationFee") BigDecimal consultationFee,
                               @RequestParam(value = "bio", required = false) String bio,
                               RedirectAttributes redirectAttributes) {
        doctorService.update(id, specialtyId, title, experienceYears, consultationFee, bio);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ bác sĩ thành công");
        return "redirect:/admin/doctors";
    }

    @GetMapping("/medicines")
    public String listMedicines(Model model) {
        prepareMedicines(model);
        return "admin/medicines";
    }

    @PostMapping("/medicines")
    public String saveMedicine(@Valid @ModelAttribute("medicineForm") AdminMedicineDto form,
                               BindingResult bindingResult, Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) { model.addAttribute("medicineForm", form); prepareMedicines(model); return "admin/medicines"; }
        try {
        if (form.getId() != null) {
            medicineService.update(form.getId(), form.getName(), form.getUnit(), form.getUnitPrice(), form.getUsageGuide(), form.getStockQuantity());
        } else {
            medicineService.save(form.getName(), form.getUnit(), form.getUnitPrice(), form.getUsageGuide(), form.getStockQuantity());
        }
        } catch (BusinessLogicException ex) { bindingResult.rejectValue("name", "duplicate", ex.getMessage()); model.addAttribute("medicineForm", form); prepareMedicines(model); return "admin/medicines"; }
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

    private void prepareSpecialties(Model model) { model.addAttribute("specialties", specialtyService.findAll()); if (!model.containsAttribute("specialtyForm")) model.addAttribute("specialtyForm", new AdminSpecialtyDto()); }
    private void prepareDoctors(Model model) { model.addAttribute("doctors", doctorService.findAll()); model.addAttribute("specialties", specialtyService.findAllActive()); if (!model.containsAttribute("doctorForm")) model.addAttribute("doctorForm", new AdminDoctorCreateDto()); }
    private void prepareMedicines(Model model) { model.addAttribute("medicines", medicineService.findAll()); if (!model.containsAttribute("medicineForm")) model.addAttribute("medicineForm", new AdminMedicineDto()); }
}
