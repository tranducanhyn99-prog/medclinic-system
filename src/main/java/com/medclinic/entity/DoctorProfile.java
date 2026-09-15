package com.medclinic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(nullable = false, length = 50)
    private String title; // PGS.TS, ThS.BS, BS.CKI, BS.CKII

    @Column(nullable = false)
    private Integer experienceYears;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consultationFee;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DoctorSchedule> schedules = new ArrayList<>();
}

