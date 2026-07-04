package com.hms.analytics.dto;

import java.util.List;

public record DiseaseDistributionResponse(
        List<NamedCount> byAgeGroup,
        List<NamedCount> byGender,
        List<NamedCount> byLocation,
        List<NamedCount> byTime,
        List<NamedCount> byClinicalSeverity,
        List<DiseaseDistributionEntry> byIcd10Code,
        List<NamedCount> byTreatmentOutcome
) {
}
