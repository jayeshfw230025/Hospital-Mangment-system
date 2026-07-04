package com.hms.analytics.dto;

/**
 * No patient feedback/survey module exists anywhere in the system, so this is
 * honestly reported as unavailable rather than fabricated.
 */
public record PatientSatisfactionResponse(boolean available, String message) {

    public static PatientSatisfactionResponse notAvailable() {
        return new PatientSatisfactionResponse(false,
                "No patient satisfaction/feedback data source exists yet; requires a dedicated feedback capture module");
    }
}
