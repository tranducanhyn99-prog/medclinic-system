package com.medclinic.controller;

import com.medclinic.util.ClinicalDatePolicy;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.medclinic.controller")
public class ClinicalDemoModelAdvice {
    private final Environment environment;

    public ClinicalDemoModelAdvice(Environment environment) {
        this.environment = environment;
    }

    @ModelAttribute("clinicalDemoMode")
    public boolean clinicalDemoMode() {
        return ClinicalDatePolicy.earlyProcessingEnabled(environment);
    }
}
