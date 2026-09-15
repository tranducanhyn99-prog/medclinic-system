package com.medclinic.entity;

import com.medclinic.enums.ShiftType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor_schedules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"doctor_id", "work_date", "shift"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @Column(nullable = false)
    private LocalDate workDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShiftType shift;

    @Builder.Default
    @Column(nullable = false)
    private Integer maxPatients = 8;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime ASC")
    @Builder.Default
    private List<TimeSlot> timeSlots = new ArrayList<>();
}

