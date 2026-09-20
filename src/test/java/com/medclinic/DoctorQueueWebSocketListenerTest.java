package com.medclinic;

import com.medclinic.dto.DoctorQueueUpdateMessage;
import com.medclinic.enums.AppointmentStatus;
import com.medclinic.event.DoctorQueueChangedEvent;
import com.medclinic.event.DoctorQueueWebSocketListener;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DoctorQueueWebSocketListenerTest {

    @Test
    void sendsMinimalQueueUpdateToAuthenticatedDoctorDestination() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        DoctorQueueWebSocketListener listener = new DoctorQueueWebSocketListener(messagingTemplate);
        Instant occurredAt = Instant.parse("2026-09-19T12:00:00Z");
        DoctorQueueChangedEvent event = new DoctorQueueChangedEvent(
                7L, "doctor", 42L, AppointmentStatus.CHECKED_IN,
                DoctorQueueChangedEvent.ChangeType.CHECKED_IN, occurredAt);

        listener.handleQueueChanged(event);
        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(eq("doctor"), eq("/queue/doctor"), payload.capture());
        DoctorQueueUpdateMessage message = (DoctorQueueUpdateMessage) payload.getValue();
        assertEquals(7L, message.doctorId());
        assertEquals(42L, message.appointmentId());
        assertEquals(AppointmentStatus.CHECKED_IN, message.status());
        assertEquals(DoctorQueueChangedEvent.ChangeType.CHECKED_IN, message.changeType());
        assertEquals(occurredAt, message.occurredAt());
    }

    @Test
    void skipsEventsWithoutDoctorUsername() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        DoctorQueueWebSocketListener listener = new DoctorQueueWebSocketListener(messagingTemplate);
        DoctorQueueChangedEvent event = new DoctorQueueChangedEvent(
                7L, " ", 42L, AppointmentStatus.CHECKED_IN,
                DoctorQueueChangedEvent.ChangeType.CHECKED_IN, Instant.now());

        listener.handleQueueChanged(event);

        verifyNoInteractions(messagingTemplate);
    }
}
