package com.medclinic;

import com.medclinic.controller.ApiController;
import com.medclinic.exception.ApiExceptionHandler;
import com.medclinic.service.ScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiErrorContractTest {
    @Test void internalFailureReturnsSafeJsonAndFiveHundred() throws Exception {
        var schedules = mock(ScheduleService.class);
        when(schedules.getAvailableSlots(1L, LocalDate.of(2026, 10, 2)))
                .thenThrow(new IllegalStateException("jdbc:internal-secret must not be exposed"));
        var mvc = MockMvcBuilders.standaloneSetup(new ApiController(schedules))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/api/public/doctors/1/slots").param("date", "2026-10-02"))
                .andExpect(status().isInternalServerError()).andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("internal-secret"))));
    }
}
