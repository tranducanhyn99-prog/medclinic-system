package com.medclinic.event;

import com.medclinic.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEmailListener {
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(AppointmentEmailEvent event) {
        try {
            if (event.type() == AppointmentEmailEvent.Type.BOOKED) {
                emailService.sendBookingConfirmationEmail(event.email(), event.patientName(), event.bookingCode(),
                        event.doctorName(), event.appointmentDate(), event.timeRange());
            } else {
                emailService.sendCancellationEmail(event.email(), event.patientName(), event.bookingCode());
            }
        } catch (RuntimeException exception) {
            // Notification failure is deliberately isolated from committed clinical/booking data.
            log.error("Appointment notification failed for booking {}", event.bookingCode(), exception);
        }
    }
}
