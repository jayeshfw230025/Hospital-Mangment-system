package com.hms.ipd.admission.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.dto.IpdAdmissionResponse;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.history.domain.AllergySeverity;
import com.hms.patient.history.dto.AllergyDto;
import com.hms.patient.history.dto.CurrentMedicationDto;
import com.hms.patient.history.dto.PatientHistoryRequest;
import com.hms.patient.history.service.PatientHistoryService;
import com.hms.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class IpdAdmissionServiceIntegrationTest {

    @Autowired
    private IpdAdmissionService ipdAdmissionService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientHistoryService patientHistoryService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Admission Test Patient", LocalDate.of(1970, 1, 1), Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    private IpdAdmissionRequest admissionRequest(String patientId) {
        return new IpdAdmissionRequest(
                patientId, AdmissionType.EMERGENCY, AdmissionSource.ER,
                "Dr. Referring", "9876500001", "City Hospital", "9876500002",
                "K25.0", null, "Suspected acute GI bleed", "Patient signed consent");
    }

    @Test
    void createsAdmissionAndAutoPopulatesAllergiesAndMedications() {
        String patientId = registerPatient("9101100001");
        patientHistoryService.create(new PatientHistoryRequest(
                patientId,
                null, null,
                List.of(), List.of(),
                List.of(new CurrentMedicationDto("Metformin", "500mg", "Twice daily")),
                List.of(new AllergyDto("Penicillin", "Anaphylaxis", AllergySeverity.ANAPHYLAXIS, true)),
                List.of(),
                null, null,
                null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null));

        IpdAdmissionResponse response = ipdAdmissionService.create(admissionRequest(patientId), null);

        assertThat(response.hardStopAllergies()).containsExactly("Penicillin");
        assertThat(response.currentMedications()).extracting(CurrentMedicationDto::drugName).containsExactly("Metformin");
        assertThat(response.primaryDiagnosisDescription()).isEqualTo("Gastric ulcer, acute with haemorrhage");
        assertThat(response.bed()).isNull();
    }

    @Test
    void rejectsAdmissionForNonExistentPatient() {
        assertThatThrownBy(() -> ipdAdmissionService.create(admissionRequest("UPID-2026-333333"), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsAdmissionForInvalidIcd10Code() {
        String patientId = registerPatient("9101100002");
        IpdAdmissionRequest invalid = new IpdAdmissionRequest(
                patientId, AdmissionType.ELECTIVE, AdmissionSource.OPD,
                null, null, null, null, "Z99.9", null, null, "Signed");

        assertThatThrownBy(() -> ipdAdmissionService.create(invalid, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesAdmissionClinicalSummary() {
        String patientId = registerPatient("9101100003");
        IpdAdmissionResponse created = ipdAdmissionService.create(admissionRequest(patientId), null);

        IpdAdmissionRequest updateRequest = new IpdAdmissionRequest(
                patientId, AdmissionType.EMERGENCY, AdmissionSource.ER,
                "Dr. Referring", "9876500001", "City Hospital", "9876500002",
                "K25.0", "K29.0", "Updated: stabilized after transfusion", "Signed");

        IpdAdmissionResponse updated = ipdAdmissionService.update(created.id(), updateRequest, null);

        assertThat(updated.clinicalSummary()).isEqualTo("Updated: stabilized after transfusion");
        assertThat(updated.secondaryDiagnosisIcd10()).isEqualTo("K29.0");
    }

    @Test
    void storesUploadedConsentDocument() {
        String patientId = registerPatient("9101100004");
        MockMultipartFile file = new MockMultipartFile(
                "consentDocument", "consent.pdf", "application/pdf", "consent bytes".getBytes(StandardCharsets.UTF_8));

        IpdAdmissionResponse response = ipdAdmissionService.create(admissionRequest(patientId), file);

        assertThat(response.hasConsentDocument()).isTrue();
    }
}
