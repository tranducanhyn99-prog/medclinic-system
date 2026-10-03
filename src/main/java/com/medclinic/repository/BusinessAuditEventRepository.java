package com.medclinic.repository;

import com.medclinic.entity.BusinessAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface BusinessAuditEventRepository extends JpaRepository<BusinessAuditEvent, Long> {
    long deleteByCreatedAtBefore(LocalDateTime cutoff);
}
