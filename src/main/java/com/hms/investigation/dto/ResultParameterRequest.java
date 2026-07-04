package com.hms.investigation.dto;

import jakarta.validation.constraints.NotBlank;

public record ResultParameterRequest(

        @NotBlank(message = "Parameter name is required")
        String parameterName,

        @NotBlank(message = "Value is required")
        String value,

        String unit,

        Double referenceRangeLow,

        Double referenceRangeHigh,

        Boolean abnormalOverride
) {
}
