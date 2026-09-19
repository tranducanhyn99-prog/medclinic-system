package com.medclinic;

import com.medclinic.service.impl.DailyQueueCounterAllocator;
import com.medclinic.service.impl.DailyQueueCounterWriter;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DailyQueueCounterAllocatorTest {
    @Test
    void retriesWhenAnotherNodeWinsTheFirstRowInsert() {
        DailyQueueCounterWriter writer = mock(DailyQueueCounterWriter.class);
        LocalDate date = LocalDate.of(2026, 9, 18);
        doThrow(new DataIntegrityViolationException("daily counter already inserted"))
                .doReturn(2L).when(writer).allocateNext(date);

        assertEquals(2L, new DailyQueueCounterAllocator(writer).allocateNext(date));
    }
}
