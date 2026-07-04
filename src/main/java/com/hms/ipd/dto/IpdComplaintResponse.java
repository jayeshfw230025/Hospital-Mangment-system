package com.hms.ipd.dto;

import com.hms.clinical.complaint.ComplaintType;
import com.hms.clinical.complaint.DurationUnit;
import com.hms.clinical.complaint.SeverityLevel;
import com.hms.clinical.complaint.TreatmentResponse;

import java.time.Instant;
import java.util.Map;

public record IpdComplaintResponse(
        Long id,
        Long admissionId,
        ComplaintType complaintType,
        String complaintLabel,
        SeverityLevel severity,
        Integer severityScore,
        Integer durationValue,
        DurationUnit durationUnit,
        String associatedVitalsImpact,
        TreatmentResponse responseToInitialTreatment,
        String notes,
        Map<String, Object> details,
        Instant createdAt
) {
}
