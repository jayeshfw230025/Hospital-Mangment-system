package com.hms.analytics.dto;

public record MortalityEntry(String icd10Code, String description, long totalDischarges,
                              long expiredCount, double mortalityRatePercent) {
}
