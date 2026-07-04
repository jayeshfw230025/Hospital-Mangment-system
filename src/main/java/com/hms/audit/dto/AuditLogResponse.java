package com.hms.audit.dto;

import com.hms.auth.domain.Role;
import com.hms.audit.domain.AuditAction;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Instant timestamp,
        Long userId,
        String username,
        Role userRole,
        AuditAction action,
        String moduleName,
        String recordId,
        String relatedPatientId,
        String oldValueJson,
        String newValueJson,
        String ipAddress,
        String sessionId,
        String deviceInfo) {
}
