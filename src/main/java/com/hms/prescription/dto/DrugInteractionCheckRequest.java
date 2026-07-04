package com.hms.prescription.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DrugInteractionCheckRequest(

        @NotEmpty(message = "At least two drug IDs are required to check interactions")
        List<Long> drugIds,

        String patientId
) {
}
