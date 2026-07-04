package com.hms.patient.history.service;

import com.hms.common.exception.DuplicateResourceException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.history.dto.FamilyHistoryRequest;
import com.hms.patient.history.dto.FamilyHistoryResponse;
import com.hms.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class FamilyHistoryServiceIntegrationTest {

    @Autowired
    private FamilyHistoryService familyHistoryService;

    @Autowired
    private PatientService patientService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Family History Test Patient", LocalDate.of(1980, 1, 1), Gender.FEMALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    @Test
    void createsAndFetchesFamilyHistory() {
        String patientId = registerPatient("9222200001");

        FamilyHistoryRequest request = new FamilyHistoryRequest(
                patientId, false, false, true, "Colorectal cancer", true, false, false, null);

        FamilyHistoryResponse created = familyHistoryService.create(request);

        assertThat(created.giMalignancy()).isTrue();
        assertThat(created.giMalignancyType()).isEqualTo("Colorectal cancer");

        FamilyHistoryResponse fetched = familyHistoryService.getByPatientId(patientId);
        assertThat(fetched.diabetesMellitus()).isTrue();
    }

    @Test
    void rejectsFamilyHistoryForNonExistentPatient() {
        FamilyHistoryRequest request = new FamilyHistoryRequest(
                "UPID-2026-888888", false, false, false, null, false, false, false, null);

        assertThatThrownBy(() -> familyHistoryService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsDuplicateFamilyHistory() {
        String patientId = registerPatient("9222200002");
        FamilyHistoryRequest request = new FamilyHistoryRequest(
                patientId, true, false, false, null, false, false, false, null);
        familyHistoryService.create(request);

        assertThatThrownBy(() -> familyHistoryService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updatesExistingFamilyHistory() {
        String patientId = registerPatient("9222200003");
        FamilyHistoryResponse created = familyHistoryService.create(new FamilyHistoryRequest(
                patientId, false, false, false, null, false, false, false, null));

        FamilyHistoryResponse updated = familyHistoryService.update(created.id(), new FamilyHistoryRequest(
                patientId, false, true, false, null, false, false, false, "Uncle with early-onset IBD"));

        assertThat(updated.inflammatoryBowelDisease()).isTrue();
        assertThat(updated.othersDescription()).isEqualTo("Uncle with early-onset IBD");
    }
}
