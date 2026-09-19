package com.medclinic.service.impl;

import com.medclinic.entity.DailyQueueCounter;
import com.medclinic.repository.DailyQueueCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Performs one allocation in an independent transaction so a first-row unique collision can be retried. */
@Service
@RequiredArgsConstructor
public class DailyQueueCounterWriter {
    private final DailyQueueCounterRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long allocateNext(LocalDate date) {
        DailyQueueCounter counter = repository.findByQueueDateForUpdate(date).orElse(null);
        if (counter == null) {
            counter = repository.saveAndFlush(DailyQueueCounter.builder()
                    .queueDate(date).nextSequence(0L).build());
        }
        long sequence = counter.getNextSequence() + 1;
        counter.setNextSequence(sequence);
        repository.saveAndFlush(counter);
        return sequence;
    }
}
