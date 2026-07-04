package com.hms.opd.dto;

import com.hms.clinical.complaint.ComplaintType;
import com.hms.clinical.complaint.DurationUnit;
import com.hms.clinical.complaint.FrequencyLevel;
import com.hms.clinical.complaint.SeverityLevel;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Map;

public record OpdComplaintRequest(

        @NotNull(message = "Visit ID is required")
        Long visitId,

        @NotNull(message = "Complaint type is required")
        ComplaintType complaintType,

        SeverityLevel severity,

        Integer durationValue,

        DurationUnit durationUnit,

        FrequencyLevel frequency,

        LocalDate onsetDate,

        String notes,

        Map<String, Object> details
) {
}
