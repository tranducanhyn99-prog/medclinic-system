package com.medclinic;

import com.medclinic.config.AsyncConfig;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsyncConfigTest {

    @Test
    void applicationExecutorHasDedicatedBoundedPool() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) new AsyncConfig().taskExecutor();
        try {
            assertEquals(4, executor.getCorePoolSize());
            assertEquals(10, executor.getMaxPoolSize());
            assertEquals(100, executor.getThreadPoolExecutor().getQueue().remainingCapacity());
            assertEquals("medclinic-async-", executor.getThreadNamePrefix());
        } finally {
            executor.shutdown();
        }
    }
}
