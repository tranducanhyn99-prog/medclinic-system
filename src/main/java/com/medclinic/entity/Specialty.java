package com.medclinic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "specialties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Lob
    private String description;

    @Column(length = 100)
    private String icon;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "specialty", cascade = CascadeType.ALL)
    @Builder.Default
    private List<DoctorProfile> doctors = new ArrayList<>();
}

