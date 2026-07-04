package com.hms.ipd.mar.dto;

import com.hms.ipd.mar.domain.AdministrationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record MarRequest(

        @NotNull(message = "Admission ID is required")
        Long admissionId,

        Long drugId,

        @NotBlank(message = "Drug name is required")
        String drugName,

        String dosage,

        String route,

        @NotNull(message = "Scheduled time is required")
        Instant scheduledTime,

        Instant administeredTime,

        String administeredByName,

        AdministrationStatus status,

        String notes
) {
}
