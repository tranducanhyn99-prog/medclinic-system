package com.medclinic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** One row per working day gives check-in a stable, lockable queue allocator. */
@Entity
@Table(name = "daily_queue_counters", uniqueConstraints = @UniqueConstraint(name = "uk_daily_queue_counter_date", columnNames = "queue_date"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyQueueCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "queue_date", nullable = false, unique = true)
    private LocalDate queueDate;

    @Column(nullable = false)
    private Long nextSequence;

    @Version
    private Long version;
}
