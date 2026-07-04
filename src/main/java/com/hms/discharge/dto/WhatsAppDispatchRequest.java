package com.hms.discharge.dto;

import jakarta.validation.constraints.NotNull;

public record WhatsAppDispatchRequest(

        @NotNull(message = "Discharge ID is required")
        Long dischargeId,

        String phoneNumberOverride
) {
}
