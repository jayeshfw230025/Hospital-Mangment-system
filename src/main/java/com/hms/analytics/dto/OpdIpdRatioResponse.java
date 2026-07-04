package com.hms.analytics.dto;

/**
 * opdEncounters is approximated as the count of distinct visitId values seen
 * across OPD-scoped tables (complaints, vitals, clinical exams) - no dedicated
 * OPD Visit entity exists anywhere in the system to count directly.
 */
public record OpdIpdRatioResponse(long opdEncounters, long ipdAdmissions, double ratio) {
}
