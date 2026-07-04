package com.hms.opd.dto;

import com.hms.clinical.complaint.ComplaintType;
import com.hms.clinical.complaint.DurationUnit;
import com.hms.clinical.complaint.FrequencyLevel;
import com.hms.clinical.complaint.SeverityLevel;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public record OpdComplaintResponse(
        Long id,
        Long visitId,
        ComplaintType complaintType,
        String complaintLabel,
        SeverityLevel severity,
        Integer durationValue,
        DurationUnit durationUnit,
        FrequencyLevel frequency,
        LocalDate onsetDate,
        String notes,
        Map<String, Object> details,
        Instant createdAt
) {
}
