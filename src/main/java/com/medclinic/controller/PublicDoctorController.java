package com.medclinic.controller;

import com.medclinic.entity.DoctorProfile;
import com.medclinic.service.DoctorService;
import com.medclinic.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class PublicDoctorController {

    private final DoctorService doctorService;
    private final SpecialtyService specialtyService;

    @GetMapping
    public String listDoctors(
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(required = false) String keyword,
            Model model) {
        model.addAttribute("doctors", doctorService.filterDoctors(specialtyId, maxFee, keyword));
        model.addAttribute("specialties", specialtyService.findAllActive());
        model.addAttribute("selectedSpecialtyId", specialtyId);
        model.addAttribute("maxFee", maxFee);
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        return "doctors/list";
    }

    @GetMapping("/{id}")
    public String doctorDetail(@PathVariable Long id, Model model) {
        DoctorProfile doctor = doctorService.findById(id);
        if (!com.medclinic.util.DoctorAvailability.acceptsBookings(doctor)) {
            throw new com.medclinic.exception.ResourceNotFoundException("Bác sĩ hiện không nhận lịch khám.");
        }
        model.addAttribute("doctor", doctor);
        return "doctors/detail";
    }
}
