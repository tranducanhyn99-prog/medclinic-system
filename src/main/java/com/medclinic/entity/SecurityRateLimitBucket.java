package com.medclinic.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_rate_limit_buckets")
@Getter
@Setter
@NoArgsConstructor
public class SecurityRateLimitBucket {
    @Id
    @Column(name = "bucket_key", length = 180)
    private String bucketKey;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "request_count", nullable = false)
    private int requestCount;
}
