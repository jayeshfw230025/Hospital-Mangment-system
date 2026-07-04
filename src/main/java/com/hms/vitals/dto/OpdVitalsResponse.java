package com.hms.vitals.dto;

import java.time.Instant;
import java.util.List;

public record OpdVitalsResponse(
        Long id,
        Long visitId,
        String patientId,
        Integer systolicBp,
        Integer diastolicBp,
        Integer heartRate,
        Integer respiratoryRate,
        Double temperatureCelsius,
        Double temperatureFahrenheit,
        Double heightCm,
        Double weightKg,
        Double bmi,
        Integer spo2,
        Integer painScore,
        Integer randomBloodSugar,
        List<VitalAlertResponse> triggeredAlerts,
        Instant recordedAt
) {
}
