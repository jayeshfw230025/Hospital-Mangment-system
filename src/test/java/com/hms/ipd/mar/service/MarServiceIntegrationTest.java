package com.hms.ipd.mar.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.service.IpdAdmissionService;
import com.hms.ipd.mar.domain.AdministrationStatus;
import com.hms.ipd.mar.dto.MarRequest;
import com.hms.ipd.mar.dto.MarResponse;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class MarServiceIntegrationTest {

    @Autowired
    private MarService marService;

    @Autowired
    private IpdAdmissionService ipdAdmissionService;

    @Autowired
    private PatientService patientService;

    private Long createAdmission(String contactNumber) {
        PatientRegistrationRequest patientRequest = new PatientRegistrationRequest(
                null, "MAR Test Patient", LocalDate.of(1980, 1, 1), Gender.FEMALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        String patientId = patientService.register(patientRequest).upid();

        IpdAdmissionRequest admissionRequest = new IpdAdmissionRequest(
                patientId, AdmissionType.EMERGENCY, AdmissionSource.ER,
                null, null, null, null, "K25.0", null, "Test admission", "Signed");
        return ipdAdmissionService.create(admissionRequest, null).id();
    }

    @Test
    void recordsMedicationAdministrationDefaultingToScheduledStatus() {
        Long admissionId = createAdmission("9101500001");

        MarResponse response = marService.create(new MarRequest(
                admissionId, null, "Omeprazole", "20mg", "Oral", Instant.now(), null, null, null, null));

        assertThat(response.status()).isEqualTo(AdministrationStatus.SCHEDULED);
    }

    @Test
    void recordsAdministeredEntryWithNurseAndTime() {
        Long admissionId = createAdmission("9101500002");
        Instant scheduled = Instant.now();

        MarResponse response = marService.create(new MarRequest(
                admissionId, null, "Ceftriaxone", "1g", "IV", scheduled, scheduled.plusSeconds(300),
                "Nurse Anjali", AdministrationStatus.ADMINISTERED, "Given without issue"));

        assertThat(response.administeredByName()).isEqualTo("Nurse Anjali");
        assertThat(response.status()).isEqualTo(AdministrationStatus.ADMINISTERED);
    }

    @Test
    void rejectsMarForNonExistentAdmission() {
        assertThatThrownBy(() -> marService.create(new MarRequest(
                999999L, null, "Omeprazole", "20mg", "Oral", Instant.now(), null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsMarForNonExistentDrugId() {
        Long admissionId = createAdmission("9101500003");

        assertThatThrownBy(() -> marService.create(new MarRequest(
                admissionId, 999999L, "Unknown Drug", "10mg", "Oral", Instant.now(), null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listsMarEntriesForAdmissionOrderedByScheduledTime() {
        Long admissionId = createAdmission("9101500004");
        Instant now = Instant.now();
        marService.create(new MarRequest(admissionId, null, "Drug B", null, null, now.plusSeconds(3600), null, null, null, null));
        marService.create(new MarRequest(admissionId, null, "Drug A", null, null, now, null, null, null, null));

        List<MarResponse> results = marService.getByAdmissionId(admissionId);

        assertThat(results).extracting(MarResponse::drugName).containsExactly("Drug A", "Drug B");
    }
}
