package com.medclinic.service.impl;

import com.medclinic.exception.BusinessLogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyQueueCounterAllocator {
    static final int MAX_ATTEMPTS = 8;
    private final DailyQueueCounterWriter writer;

    public long allocateNext(LocalDate date) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                return writer.allocateNext(date);
            } catch (DataIntegrityViolationException ignored) {
                // A different node inserted the first row between our read and insert. Retry in a new transaction.
            }
        }
        throw new BusinessLogicException("Không thể cấp số thứ tự, vui lòng thử lại");
    }
}
