package com.hms.ipd.dto;

import com.hms.clinical.complaint.ComplaintType;
import com.hms.clinical.complaint.DurationUnit;
import com.hms.clinical.complaint.SeverityLevel;
import com.hms.clinical.complaint.TreatmentResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record IpdComplaintRequest(

        @NotNull(message = "Admission ID is required")
        Long admissionId,

        @NotNull(message = "Complaint type is required")
        ComplaintType complaintType,

        SeverityLevel severity,

        @Min(value = 1, message = "Severity score must be between 1 and 10")
        @Max(value = 10, message = "Severity score must be between 1 and 10")
        Integer severityScore,

        Integer durationValue,

        DurationUnit durationUnit,

        String associatedVitalsImpact,

        TreatmentResponse responseToInitialTreatment,

        String notes,

        Map<String, Object> details
) {
}
