package com.hms.patient.history.dto;

import com.hms.patient.history.domain.AlcoholFrequency;
import com.hms.patient.history.domain.DietaryHabit;
import com.hms.patient.history.domain.PhysicalActivityLevel;
import com.hms.patient.history.domain.SmokingStatus;
import com.hms.patient.history.domain.StressLevel;

public record LifestyleResponse(
        String patientId,
        SmokingStatus smokingStatus,
        Double smokingPackYears,
        AlcoholFrequency alcoholFrequency,
        String alcoholType,
        String alcoholQuantity,
        Integer alcoholHistoryYears,
        DietaryHabit dietaryHabit,
        PhysicalActivityLevel physicalActivity,
        Double sleepHoursPerNight,
        StressLevel stressLevel,
        String occupationExposureHistory,
        String recentTravelHistory
) {
}
