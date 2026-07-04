package com.hms.diagnosis.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.diagnosis.domain.DiagnosisStatus;
import com.hms.diagnosis.domain.DiagnosisType;
import com.hms.diagnosis.dto.DiagnosisRequest;
import com.hms.diagnosis.dto.DiagnosisResponse;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class DiagnosisServiceIntegrationTest {

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private PatientService patientService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Diagnosis Test Patient", LocalDate.of(1988, 2, 20), Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    @Test
    void createsDiagnosisAndDenormalizesIcd10Details() {
        String patientId = registerPatient("9666600001");

        DiagnosisRequest request = new DiagnosisRequest(patientId, "K20.0",
                DiagnosisType.PRIMARY, null, null, "Chronic reflux symptoms");
        DiagnosisResponse created = diagnosisService.create(request);

        assertThat(created.icd10Description()).isEqualTo("Gastro-oesophageal reflux disease");
        assertThat(created.status()).isEqualTo(DiagnosisStatus.ACTIVE);
        assertThat(created.diagnosedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void rejectsUnknownIcd10Code() {
        String patientId = registerPatient("9666600002");
        DiagnosisRequest request = new DiagnosisRequest(patientId, "Z99.9", DiagnosisType.PRIMARY, null, null, null);

        assertThatThrownBy(() -> diagnosisService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsDiagnosisForNonExistentPatient() {
        DiagnosisRequest request = new DiagnosisRequest("UPID-2026-555555", "K58.0", DiagnosisType.PRIMARY, null, null, null);

        assertThatThrownBy(() -> diagnosisService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesDiagnosisStatusToInactive() {
        String patientId = registerPatient("9666600003");
        DiagnosisResponse created = diagnosisService.create(new DiagnosisRequest(
                patientId, "K58.0", DiagnosisType.SECONDARY, DiagnosisStatus.ACTIVE, null, "IBS, diet-related"));

        DiagnosisResponse updated = diagnosisService.update(created.id(), new DiagnosisRequest(
                patientId, "K58.0", DiagnosisType.SECONDARY, DiagnosisStatus.INACTIVE, created.diagnosedDate(), "Resolved"));

        assertThat(updated.status()).isEqualTo(DiagnosisStatus.INACTIVE);
        assertThat(updated.notes()).isEqualTo("Resolved");
    }

    @Test
    void listsDiagnosesForPatient() {
        String patientId = registerPatient("9666600004");
        diagnosisService.create(new DiagnosisRequest(patientId, "K29.0", DiagnosisType.PRIMARY, null, null, null));
        diagnosisService.create(new DiagnosisRequest(patientId, "K58.0", DiagnosisType.SECONDARY, null, null, null));

        List<DiagnosisResponse> results = diagnosisService.getByPatientId(patientId);

        assertThat(results).hasSize(2);
    }
}
