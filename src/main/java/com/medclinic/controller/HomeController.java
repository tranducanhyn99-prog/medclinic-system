package com.medclinic.controller;

import com.medclinic.service.DoctorService;
import com.medclinic.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final SpecialtyService specialtyService;
    private final DoctorService doctorService;

    @GetMapping({"/", "/home"})
    public String index(Model model) {
        model.addAttribute("specialties", specialtyService.findAllActive());
        model.addAttribute("featuredDoctors", doctorService.filterDoctors(null, null, null));
        return "home/index";
    }
}
