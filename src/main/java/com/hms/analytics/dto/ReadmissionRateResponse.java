package com.hms.analytics.dto;

public record ReadmissionRateResponse(double rate7DayPercent, double rate14DayPercent, double rate30DayPercent,
                                       long totalDischargesConsidered) {
}
