package com.hms.vitals.dto;

import com.hms.vitals.domain.TemperatureUnit;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OpdVitalsRequest(

        @NotNull(message = "Visit ID is required")
        Long visitId,

        @NotBlank(message = "Patient ID is required")
        String patientId,

        Integer systolicBp,
        Integer diastolicBp,
        Integer heartRate,
        Integer respiratoryRate,

        Double temperature,
        TemperatureUnit temperatureUnit,

        Double heightCm,
        Double weightKg,

        @Min(value = 0, message = "SpO2 must be between 0 and 100")
        @Max(value = 100, message = "SpO2 must be between 0 and 100")
        Integer spo2,

        @Min(value = 0, message = "Pain score must be between 0 and 10")
        @Max(value = 10, message = "Pain score must be between 0 and 10")
        Integer painScore,

        Integer randomBloodSugar
) {
}
