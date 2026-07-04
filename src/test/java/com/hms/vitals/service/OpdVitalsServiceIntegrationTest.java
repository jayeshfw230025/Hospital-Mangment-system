package com.hms.vitals.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import com.hms.vitals.domain.TemperatureUnit;
import com.hms.vitals.domain.VitalParameter;
import com.hms.vitals.dto.OpdVitalsRequest;
import com.hms.vitals.dto.OpdVitalsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;

@SpringBootTest
@ActiveProfiles("test")
class OpdVitalsServiceIntegrationTest {

    @Autowired
    private OpdVitalsService opdVitalsService;

    @Autowired
    private PatientService patientService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Vitals Test Patient", LocalDate.of(1990, 1, 1), Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    private OpdVitalsRequest normalVitalsRequest(Long visitId, String patientId) {
        return new OpdVitalsRequest(visitId, patientId, 120, 80, 78, 16,
                37.0, TemperatureUnit.CELSIUS, 170.0, 70.0, 98, 2, 110);
    }

    @Test
    void computesBmiAndFahrenheitAndRaisesNoAlertsForNormalVitals() {
        String patientId = registerPatient("9333300001");

        OpdVitalsResponse response = opdVitalsService.record(normalVitalsRequest(301L, patientId));

        assertThat(response.bmi()).isCloseTo(24.22, offset(0.05));
        assertThat(response.temperatureFahrenheit()).isCloseTo(98.6, offset(0.1));
        assertThat(response.triggeredAlerts()).isEmpty();
    }

    @Test
    void rejectsVitalsForNonExistentPatient() {
        assertThatThrownBy(() -> opdVitalsService.record(normalVitalsRequest(302L, "UPID-2026-777777")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void raisesAlertForLowSystolicBpAndLowSpo2() {
        String patientId = registerPatient("9333300002");
        OpdVitalsRequest request = new OpdVitalsRequest(303L, patientId, 85, 55, 45, 14,
                37.0, TemperatureUnit.CELSIUS, null, null, 88, 9, 350);

        OpdVitalsResponse response = opdVitalsService.record(request);

        assertThat(response.triggeredAlerts()).extracting(a -> a.parameter()).containsExactlyInAnyOrder(
                VitalParameter.SYSTOLIC_BP, VitalParameter.DIASTOLIC_BP, VitalParameter.HEART_RATE,
                VitalParameter.SPO2, VitalParameter.PAIN_SCORE, VitalParameter.RANDOM_BLOOD_SUGAR);
    }

    @Test
    void convertsFahrenheitInputToCelsiusAndEvaluatesAlertCorrectly() {
        String patientId = registerPatient("9333300003");
        // 104F = 40C, above the 39C critical threshold
        OpdVitalsRequest request = new OpdVitalsRequest(304L, patientId, 120, 80, 78, 16,
                104.0, TemperatureUnit.FAHRENHEIT, null, null, 98, 2, 110);

        OpdVitalsResponse response = opdVitalsService.record(request);

        assertThat(response.temperatureCelsius()).isCloseTo(40.0, offset(0.1));
        assertThat(response.triggeredAlerts()).extracting(a -> a.parameter())
                .containsExactly(VitalParameter.TEMPERATURE);
    }

    @Test
    void listsVitalsByVisitAndByPatient() {
        String patientId = registerPatient("9333300004");
        opdVitalsService.record(normalVitalsRequest(305L, patientId));
        opdVitalsService.record(normalVitalsRequest(305L, patientId));

        List<OpdVitalsResponse> byVisit = opdVitalsService.getByVisitId(305L);
        List<OpdVitalsResponse> byPatient = opdVitalsService.getByPatientId(patientId);

        assertThat(byVisit).hasSize(2);
        assertThat(byPatient).hasSize(2);
    }
}
