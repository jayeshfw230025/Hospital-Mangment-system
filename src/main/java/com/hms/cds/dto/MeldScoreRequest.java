package com.hms.cds.dto;

import jakarta.validation.constraints.NotNull;

public record MeldScoreRequest(

        @NotNull(message = "Bilirubin is required")
        Double bilirubinMgDl,

        @NotNull(message = "INR is required")
        Double inr,

        @NotNull(message = "Creatinine is required")
        Double creatinineMgDl,

        @NotNull(message = "Sodium is required")
        Double sodiumMeqL
) {
}
