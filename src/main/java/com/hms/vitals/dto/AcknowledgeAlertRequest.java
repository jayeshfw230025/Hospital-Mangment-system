package com.hms.vitals.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AcknowledgeAlertRequest(

        @NotNull(message = "Alert ID is required")
        Long alertId,

        @NotBlank(message = "Acknowledged by is required")
        String acknowledgedBy
) {
}
