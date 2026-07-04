package com.hms.vitals.service;

import com.hms.vitals.domain.CoreVitals;
import com.hms.vitals.domain.VitalParameter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates the critical-alert thresholds from Module 4 against a vitals reading.
 * gcsScore is only supplied for IPD readings; pass null for OPD.
 */
@Component
public class VitalAlertEvaluator {

    public List<TriggeredAlert> evaluate(CoreVitals vitals, Integer gcsScore) {
        List<TriggeredAlert> alerts = new ArrayList<>();

        if (vitals.getSystolicBp() != null && (vitals.getSystolicBp() < 90 || vitals.getSystolicBp() > 180)) {
            alerts.add(new TriggeredAlert(VitalParameter.SYSTOLIC_BP, String.valueOf(vitals.getSystolicBp()),
                    "Systolic BP " + vitals.getSystolicBp() + " mmHg is outside the safe range (90-180)"));
        }

        if (vitals.getDiastolicBp() != null && (vitals.getDiastolicBp() < 60 || vitals.getDiastolicBp() > 120)) {
            alerts.add(new TriggeredAlert(VitalParameter.DIASTOLIC_BP, String.valueOf(vitals.getDiastolicBp()),
                    "Diastolic BP " + vitals.getDiastolicBp() + " mmHg is outside the safe range (60-120)"));
        }

        if (vitals.getHeartRate() != null && (vitals.getHeartRate() < 50 || vitals.getHeartRate() > 120)) {
            alerts.add(new TriggeredAlert(VitalParameter.HEART_RATE, String.valueOf(vitals.getHeartRate()),
                    "Heart rate " + vitals.getHeartRate() + " bpm is outside the safe range (50-120)"));
        }

        if (vitals.getTemperatureCelsius() != null
                && (vitals.getTemperatureCelsius() < 35.0 || vitals.getTemperatureCelsius() > 39.0)) {
            alerts.add(new TriggeredAlert(VitalParameter.TEMPERATURE, String.valueOf(vitals.getTemperatureCelsius()),
                    "Temperature " + vitals.getTemperatureCelsius() + "°C is outside the safe range (35-39)"));
        }

        if (vitals.getSpo2() != null && vitals.getSpo2() < 92) {
            alerts.add(new TriggeredAlert(VitalParameter.SPO2, String.valueOf(vitals.getSpo2()),
                    "SpO2 " + vitals.getSpo2() + "% is below the critical threshold of 92%"));
        }

        if (vitals.getPainScore() != null && vitals.getPainScore() > 7) {
            alerts.add(new TriggeredAlert(VitalParameter.PAIN_SCORE, String.valueOf(vitals.getPainScore()),
                    "Pain score " + vitals.getPainScore() + " exceeds the critical threshold of 7"));
        }

        if (vitals.getRandomBloodSugar() != null
                && (vitals.getRandomBloodSugar() < 70 || vitals.getRandomBloodSugar() > 300)) {
            alerts.add(new TriggeredAlert(VitalParameter.RANDOM_BLOOD_SUGAR, String.valueOf(vitals.getRandomBloodSugar()),
                    "Random blood sugar " + vitals.getRandomBloodSugar() + " mg/dL is outside the safe range (70-300)"));
        }

        if (gcsScore != null && gcsScore < 13) {
            alerts.add(new TriggeredAlert(VitalParameter.GCS, String.valueOf(gcsScore),
                    "GCS score " + gcsScore + " is below the critical threshold of 13"));
        }

        return alerts;
    }
}
