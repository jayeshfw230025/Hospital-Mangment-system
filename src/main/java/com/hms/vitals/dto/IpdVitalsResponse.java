package com.hms.vitals.dto;

import com.hms.vitals.domain.GagReflexStatus;

import java.time.Instant;
import java.util.List;

public record IpdVitalsResponse(
        Long id,
        Long admissionId,
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
        Integer qtcMs,
        Double mapValue,
        Integer inputOutputBalanceMl,
        Integer gcsScore,
        Double cvpCmH2o,
        GagReflexStatus gagReflex,
        List<VitalAlertResponse> triggeredAlerts,
        Instant recordedAt
) {
}
