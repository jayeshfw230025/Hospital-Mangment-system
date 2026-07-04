package com.hms.investigation.dto;

import java.util.List;

public record ResultParameterResponse(
        String parameterName,
        String value,
        String unit,
        Double referenceRangeLow,
        Double referenceRangeHigh,
        boolean abnormal,
        List<String> previousValues
) {
}
