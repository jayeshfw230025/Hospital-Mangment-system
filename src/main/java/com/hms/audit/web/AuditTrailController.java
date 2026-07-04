package com.hms.audit.web;

import com.hms.audit.dto.AuditLogResponse;
import com.hms.audit.service.AuditLogService;
import com.hms.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Audit Trail", description = "Immutable system-wide audit log (Admin only)")
@RestController
@RequestMapping("/api/v1/audit-trail")
public class AuditTrailController {

    private final AuditLogService auditLogService;

    public AuditTrailController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "List all audit log entries, most recent first")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<AuditLogResponse> result = auditLogService.getAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "Audit log entries related to a specific patient (by UPID)")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByPatientId(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<AuditLogResponse> result = auditLogService.getByPatientId(
                patientId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
