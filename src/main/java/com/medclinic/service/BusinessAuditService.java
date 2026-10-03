package com.medclinic.service;

import com.medclinic.entity.BusinessAuditEvent;
import com.medclinic.repository.BusinessAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessAuditService {
    private final BusinessAuditEventRepository repository;

    @Transactional
    public void record(Long actorId, String action, String entityType, Object entityId,
                       String outcome, String correlationId, String detailCode) {
        persist(actorId, action, entityType, entityId, outcome, correlationId, detailCode);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordIndependent(Long actorId, String action, String entityType, Object entityId,
                                  String outcome, String correlationId, String detailCode) {
        persist(actorId, action, entityType, entityId, outcome, correlationId, detailCode);
    }

    private void persist(Long actorId, String action, String entityType, Object entityId,
                         String outcome, String correlationId, String detailCode) {
        repository.save(BusinessAuditEvent.builder()
                .actorId(actorId)
                .action(limit(action, 60))
                .entityType(limit(entityType, 40))
                .entityId(limit(String.valueOf(entityId), 100))
                .outcome(limit(outcome, 20))
                .correlationId(limit(correlationId, 100))
                .detailCode(limit(detailCode, 100))
                .build());
    }

    private String limit(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String sanitized = value.replace('\n', '_').replace('\r', '_').replace('\t', '_');
        return sanitized.length() <= max ? sanitized : sanitized.substring(0, max);
    }
}
