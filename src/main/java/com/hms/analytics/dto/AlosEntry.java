package com.hms.analytics.dto;

public record AlosEntry(String icd10Code, String description, double averageLengthOfStayDays, long dischargeCount) {
}
