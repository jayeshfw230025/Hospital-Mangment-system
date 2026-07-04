package com.hms.vitals.service;

import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import com.hms.vitals.domain.GagReflexStatus;
import com.hms.vitals.domain.TemperatureUnit;
import com.hms.vitals.domain.VitalParameter;
import com.hms.vitals.dto.AcknowledgeAlertRequest;
import com.hms.vitals.dto.IpdVitalsRequest;
import com.hms.vitals.dto.IpdVitalsResponse;
import com.hms.vitals.dto.VitalAlertResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@SpringBootTest
@ActiveProfiles("test")
class IpdVitalsServiceIntegrationTest {

    @Autowired
    private IpdVitalsService ipdVitalsService;

    @Autowired
    private VitalAlertService vitalAlertService;

    @Autowired
    private PatientService patientService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "IPD Vitals Test Patient", LocalDate.of(1965, 6, 15), Gender.FEMALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    @Test
    void computesMapAndFlagsLowGcs() {
        String patientId = registerPatient("9444400001");

        IpdVitalsRequest request = new IpdVitalsRequest(401L, patientId, 120, 90, 88, 18,
                37.0, TemperatureUnit.CELSIUS, 165.0, 60.0, 96, 3, 100,
                420, 500, 11, 8.0, GagReflexStatus.PRESENT);

        IpdVitalsResponse response = ipdVitalsService.record(request);

        assertThat(response.mapValue()).isCloseTo(100.0, offset(0.1));
        assertThat(response.triggeredAlerts()).extracting(a -> a.parameter())
                .containsExactly(VitalParameter.GCS);
    }

    @Test
    void listsVitalsByAdmission() {
        String patientId = registerPatient("9444400002");
        IpdVitalsRequest request = new IpdVitalsRequest(402L, patientId, 120, 80, 78, 16,
                37.0, TemperatureUnit.CELSIUS, null, null, 98, 2, 110,
                400, 0, 15, 7.0, GagReflexStatus.PRESENT);
        ipdVitalsService.record(request);

        List<IpdVitalsResponse> results = ipdVitalsService.getByAdmissionId(402L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).gagReflex()).isEqualTo(GagReflexStatus.PRESENT);
    }

    @Test
    void acknowledgesRaisedAlert() {
        String patientId = registerPatient("9444400003");
        IpdVitalsRequest request = new IpdVitalsRequest(403L, patientId, 200, 130, 78, 16,
                37.0, TemperatureUnit.CELSIUS, null, null, 98, 2, 110,
                400, 0, 15, 7.0, GagReflexStatus.PRESENT);

        IpdVitalsResponse response = ipdVitalsService.record(request);
        assertThat(response.triggeredAlerts()).isNotEmpty();

        Long alertId = response.triggeredAlerts().get(0).id();
        VitalAlertResponse acknowledged = vitalAlertService.acknowledge(alertId, "nurse.jane");

        assertThat(acknowledged.acknowledged()).isTrue();
        assertThat(acknowledged.acknowledgedBy()).isEqualTo("nurse.jane");

        List<VitalAlertResponse> alertsForPatient = vitalAlertService.getByPatientId(patientId);
        assertThat(alertsForPatient).anyMatch(VitalAlertResponse::acknowledged);
    }
}
