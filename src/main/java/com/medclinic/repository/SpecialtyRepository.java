package com.medclinic.repository;

import com.medclinic.entity.Specialty;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    Optional<Specialty> findByName(String name);
    List<Specialty> findByIsActiveTrue();

    @EntityGraph(attributePaths = "doctors")
    @Query("select distinct specialty from Specialty specialty")
    List<Specialty> findAllWithDoctors();
}

