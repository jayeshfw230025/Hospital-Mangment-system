package com.hms.vitals.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The 10 OPD vital parameters, shared as a value object by both OpdVitals and
 * IpdVitals (IPD vitals are a superset - all 10 of these plus 6 IPD-only ones).
 * bmi is always server-computed from heightCm/weightKg, never accepted as input.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CoreVitals {

    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "temperature_celsius")
    private Double temperatureCelsius;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "bmi")
    private Double bmi;

    @Column(name = "spo2")
    private Integer spo2;

    @Column(name = "pain_score")
    private Integer painScore;

    @Column(name = "random_blood_sugar")
    private Integer randomBloodSugar;
}
