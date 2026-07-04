package com.hms.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.audit.domain.AuditAction;
import com.hms.audit.domain.AuditLog;
import com.hms.audit.dto.AuditLogResponse;
import com.hms.audit.repository.AuditLogRepository;
import com.hms.auth.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuditLogService {

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Runs in its own transaction so a failure recording the audit trail (or a
     * rollback in the calling business transaction) never blocks the request itself.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String username, Role role, AuditAction action, String moduleName,
                        String recordId, String relatedPatientId, Object oldValue, Object newValue) {
        RequestMetadata meta = RequestMetadata.captureCurrent();
        AuditLog log = AuditLog.builder()
                .timestamp(Instant.now())
                .userId(userId)
                .username(username)
                .userRole(role)
                .action(action)
                .moduleName(moduleName)
                .recordId(recordId)
                .relatedPatientId(relatedPatientId)
                .oldValueJson(toJson(oldValue))
                .newValueJson(toJson(newValue))
                .ipAddress(meta.ipAddress())
                .sessionId(meta.sessionId())
                .deviceInfo(meta.deviceInfo())
                .build();
        repository.save(log);
    }

    public Page<AuditLogResponse> getAll(Pageable pageable) {
        return repository.findAllByOrderByTimestampDesc(pageable).map(this::toResponse);
    }

    public Page<AuditLogResponse> getByPatientId(String patientId, Pageable pageable) {
        return repository.findByRelatedPatientIdOrderByTimestampDesc(patientId, pageable).map(this::toResponse);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof org.springframework.web.multipart.MultipartFile file) {
            return "MultipartFile:" + file.getOriginalFilename() + " (" + file.getSize() + " bytes)";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(), log.getTimestamp(), log.getUserId(), log.getUsername(), log.getUserRole(),
                log.getAction(), log.getModuleName(), log.getRecordId(), log.getRelatedPatientId(),
                log.getOldValueJson(), log.getNewValueJson(), log.getIpAddress(), log.getSessionId(),
                log.getDeviceInfo());
    }
}
