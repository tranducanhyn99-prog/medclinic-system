package com.medclinic.event;

import com.medclinic.dto.DoctorQueueUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DoctorQueueWebSocketListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQueueChanged(DoctorQueueChangedEvent event) {
        if (event.doctorUsername() == null || event.doctorUsername().isBlank()) {
            return;
        }
        DoctorQueueUpdateMessage message = new DoctorQueueUpdateMessage(
                event.doctorId(), event.appointmentId(), event.status(),
                event.changeType(), event.occurredAt());
        messagingTemplate.convertAndSendToUser(
                event.doctorUsername(), "/queue/doctor", message);
    }
}
