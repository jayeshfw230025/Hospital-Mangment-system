package com.hms.vitals.service;

import com.hms.vitals.domain.TemperatureUnit;
import org.springframework.stereotype.Component;

@Component
public class VitalsCalculator {

    public Double toCelsius(Double temperature, TemperatureUnit unit) {
        if (temperature == null) {
            return null;
        }
        if (unit == TemperatureUnit.FAHRENHEIT) {
            return (temperature - 32) * 5 / 9;
        }
        return temperature;
    }

    public Double toFahrenheit(Double celsius) {
        if (celsius == null) {
            return null;
        }
        return celsius * 9 / 5 + 32;
    }

    public Double calculateBmi(Double heightCm, Double weightKg) {
        if (heightCm == null || weightKg == null || heightCm <= 0) {
            return null;
        }
        double heightM = heightCm / 100.0;
        return round(weightKg / (heightM * heightM));
    }

    public Double calculateMap(Integer systolicBp, Integer diastolicBp) {
        if (systolicBp == null || diastolicBp == null) {
            return null;
        }
        return round(diastolicBp + (systolicBp - diastolicBp) / 3.0);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
