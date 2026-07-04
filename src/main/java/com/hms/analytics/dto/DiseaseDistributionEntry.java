package com.hms.analytics.dto;

public record DiseaseDistributionEntry(String icd10Code, String description, long count) {
}
