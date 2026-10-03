package com.medclinic;

import com.medclinic.enums.AppointmentStatus;
import com.medclinic.event.DoctorQueueChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class DoctorQueueWebSocketAfterCommitIntegrationTest {

    @Autowired private ApplicationEventPublisher applicationEventPublisher;
    @MockitoBean private SimpMessagingTemplate messagingTemplate;

    @Test
    @Rollback(false)
    void broadcastsOnlyAfterTransactionCommit() {
        DoctorQueueChangedEvent event = new DoctorQueueChangedEvent(
                3L, "doctor", 99L, AppointmentStatus.IN_PROGRESS,
                DoctorQueueChangedEvent.ChangeType.EXAMINATION_STARTED, Instant.now());

        applicationEventPublisher.publishEvent(event);
        verifyNoInteractions(messagingTemplate);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        verify(messagingTemplate).convertAndSendToUser(
                eq("doctor"), eq("/queue/doctor"), any());
    }

    @Test
    void rollbackDoesNotBroadcastQueueUpdate() {
        DoctorQueueChangedEvent event = new DoctorQueueChangedEvent(
                3L, "doctor", 100L, AppointmentStatus.COMPLETED,
                DoctorQueueChangedEvent.ChangeType.EXAMINATION_COMPLETED, Instant.now());

        applicationEventPublisher.publishEvent(event);
        TestTransaction.flagForRollback();
        TestTransaction.end();

        verifyNoInteractions(messagingTemplate);
    }
}
