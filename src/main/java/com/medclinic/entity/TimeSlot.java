package com.medclinic.entity;

import com.medclinic.enums.SlotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "time_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private DoctorSchedule schedule;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;

    /**
     * Optimistic Locking Version field
     * Ensures atomic concurrency control to prevent double booking.
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    public String getTimeRangeFormatted() {
        return String.format("%02d:%02d - %02d:%02d",
                startTime.getHour(), startTime.getMinute(),
                endTime.getHour(), endTime.getMinute());
    }
}

