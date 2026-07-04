package com.hms.fhir.dto;

import com.hms.fhir.domain.DocumentSourceType;
import jakarta.validation.constraints.NotNull;

public record DocumentReferenceRequest(
        @NotNull(message = "Document source type is required")
        DocumentSourceType sourceType,

        @NotNull(message = "Source record ID is required")
        Long sourceId
) {
}
