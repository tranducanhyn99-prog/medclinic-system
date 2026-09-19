package com.medclinic.repository;

import com.medclinic.entity.DailyQueueCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyQueueCounterRepository extends JpaRepository<DailyQueueCounter, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM DailyQueueCounter c WHERE c.queueDate = :date")
    Optional<DailyQueueCounter> findByQueueDateForUpdate(@Param("date") LocalDate date);
}
