package com.medclinic.repository;

import com.medclinic.entity.SecurityRateLimitBucket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SecurityRateLimitBucketRepository extends JpaRepository<SecurityRateLimitBucket, String> {
    long deleteByWindowStartBefore(LocalDateTime cutoff);
}
