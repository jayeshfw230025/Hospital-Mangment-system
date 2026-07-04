package com.hms.investigation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record InvestigationReportRequest(

        @NotNull(message = "Order ID is required")
        Long orderId,

        LocalDate reportDate,

        @NotEmpty(message = "At least one result parameter is required")
        @Valid
        List<ResultParameterRequest> resultParameters,

        String notes
) {
}
