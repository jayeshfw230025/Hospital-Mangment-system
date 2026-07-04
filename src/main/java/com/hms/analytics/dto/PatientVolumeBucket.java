package com.hms.analytics.dto;

public record PatientVolumeBucket(String periodLabel, long newRegistrations, long ipdAdmissions) {
}
