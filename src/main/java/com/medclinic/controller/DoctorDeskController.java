package com.medclinic.controller;

import com.medclinic.dto.ExaminationFormDto;
import com.medclinic.dto.PrescriptionItemDto;
import com.medclinic.entity.Appointment;
import com.medclinic.entity.DoctorProfile;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.exception.ResourceNotFoundException;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.AppointmentService;
import com.medclinic.service.DoctorService;
import com.medclinic.service.MedicalRecordService;
import com.medclinic.service.MedicineService;
import com.medclinic.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.beans.PropertyEditorSupport;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorDeskController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final MedicineService medicineService;
    private final ScheduleService scheduleService;
    private final java.time.Clock businessClock;

    @InitBinder("examinationForm")
    void bindPrescriptionText(WebDataBinder binder) {
        // Each repeated form parameter is one row, even when its text contains commas.
        for (String field : List.of("dosages", "frequencies", "instructions")) {
            binder.registerCustomEditor(List.class, field, new PropertyEditorSupport() {
                @Override public void setAsText(String text) { setValue(List.of(text)); }
                @Override public void setValue(Object value) {
                    super.setValue(value instanceof String[] texts ? Arrays.asList(texts) : value);
                }
            });
        }
    }

    @GetMapping("/desk")
    public String doctorDesk(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        DoctorProfile doctorProfile = doctorService.findByUserId(userDetails.getId());
        List<Appointment> todayQueue = appointmentService.findTodayQueueByDoctor(doctorProfile.getId());
        
        model.addAttribute("todayQueue", todayQueue);
        model.addAttribute("doctorProfile", doctorProfile);
        return "doctor/desk";
    }

    @GetMapping("/examine/{appointmentId}")
    public String showExaminationForm(@PathVariable Long appointmentId, 
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      Model model) {
        Appointment appointment = medicalRecordService.getAppointmentForExamination(appointmentId, userDetails.getId());
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return "redirect:/doctor/desk?closed=true";
        }

        model.addAttribute("appointment", appointment);
        model.addAttribute("patientHistory", medicalRecordService.getPatientMedicalHistory(appointment.getPatient().getId()));
        model.addAttribute("activeMedicines", medicineService.findAllActive());
        if (!model.containsAttribute("examinationForm")) model.addAttribute("examinationForm", new ExaminationFormDto());
        return "doctor/examine";
    }

    @PostMapping("/start-examination/{appointmentId}")
    public String startExamination(@PathVariable Long appointmentId,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            medicalRecordService.startExamination(appointmentId, userDetails.getId());
            return "redirect:/doctor/examine/" + appointmentId;
        } catch (BusinessLogicException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/doctor/desk";
        }
    }

    @ExceptionHandler(BusinessLogicException.class)
    public String invalidExamination(BusinessLogicException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/doctor/desk";
    }

    @PostMapping("/examine/{appointmentId}")
    public String processExaminationForm(@PathVariable Long appointmentId,
                                         @Valid @ModelAttribute("examinationForm") ExaminationFormDto formDto,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage()).distinct().collect(java.util.stream.Collectors.joining(" ")));
            return showExaminationForm(appointmentId, userDetails, model);
        }
        
        List<PrescriptionItemDto> prescriptionItems = new ArrayList<>();
        if (formDto.getMedicineIds() != null && !formDto.getMedicineIds().isEmpty()) {
            for (int i = 0; i < formDto.getMedicineIds().size(); i++) {
                if (formDto.getMedicineIds().get(i) != null) {
                    prescriptionItems.add(PrescriptionItemDto.builder()
                            .medicineId(formDto.getMedicineIds().get(i))
                            .quantity(formDto.getQuantities().get(i))
                            .dosage(formDto.getDosages().get(i))
                            .frequency(formDto.getFrequencies().get(i))
                            .instructions(formDto.getInstructions() != null && i < formDto.getInstructions().size()
                                    ? formDto.getInstructions().get(i) : "")
                            .build());
                }
            }
        }
        
        try {
            medicalRecordService.saveMedicalRecordAndPrescription(
                appointmentId, 
                userDetails.getId(),
                formDto.getSymptoms(), 
                formDto.getDiagnosis(), 
                formDto.getDoctorNotes(), 
                formDto.getReExamDate(), 
                prescriptionItems
            );
        } catch (BusinessLogicException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return showExaminationForm(appointmentId, userDetails, model);
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Đã lưu kết quả khám thành công");
        return "redirect:/doctor/desk";
    }

    @GetMapping("/schedule")
    public String showDoctorSchedule(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        DoctorProfile doctorProfile = doctorService.findByUserId(userDetails.getId());
        LocalDate today = LocalDate.now(businessClock);
        model.addAttribute("schedules", scheduleService.findByDoctorAndDateRange(doctorProfile.getId(), today, today.plusDays(14)));
        return "doctor/schedule";
    }

    @GetMapping("/records")
    public String completedRecords(@RequestParam(defaultValue = "0") int page,
                                   @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("records", medicalRecordService.getCompletedRecords(userDetails.getId(), page));
        return "doctor/records";
    }

    @GetMapping("/records/{appointmentId}")
    public String completedRecord(@PathVariable Long appointmentId,
                                  @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("record", medicalRecordService.getCompletedRecordForDoctor(appointmentId, userDetails.getId()));
        return "doctor/record-detail";
    }
}
