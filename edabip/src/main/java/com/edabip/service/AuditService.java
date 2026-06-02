package com.edabip.service;

import com.edabip.entity.AuditLog;
import com.edabip.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long userId, String username, String action,
                    String entityType, Long entityId, String details) {
        AuditLog entry = AuditLog.builder()
            .userId(userId)
            .username(username)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .details(details)
            .timestamp(LocalDateTime.now())
            .build();
        auditLogRepository.save(entry);
    }

    public Page<AuditLog> getAllLogs(int page, int size) {
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
    }

    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }
}
