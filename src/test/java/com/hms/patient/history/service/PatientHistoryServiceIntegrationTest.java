package com.hms.patient.history.service;

import com.hms.clinical.complaint.DurationUnit;
import com.hms.common.exception.DuplicateResourceException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.history.domain.AlcoholFrequency;
import com.hms.patient.history.domain.AllergySeverity;
import com.hms.patient.history.domain.ChronicDiseaseType;
import com.hms.patient.history.domain.DietaryHabit;
import com.hms.patient.history.domain.PhysicalActivityLevel;
import com.hms.patient.history.domain.ProgressionType;
import com.hms.patient.history.domain.SmokingStatus;
import com.hms.patient.history.domain.StressLevel;
import com.hms.patient.history.dto.AllergyDto;
import com.hms.patient.history.dto.CurrentMedicationDto;
import com.hms.patient.history.dto.LifestyleResponse;
import com.hms.patient.history.dto.PastSurgeryDto;
import com.hms.patient.history.dto.PatientHistoryRequest;
import com.hms.patient.history.dto.PatientHistoryResponse;
import com.hms.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PatientHistoryServiceIntegrationTest {

    @Autowired
    private PatientHistoryService patientHistoryService;

    @Autowired
    private PatientService patientService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "History Test Patient", LocalDate.of(1975, 3, 20), Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        PatientResponse response = patientService.register(request);
        return response.upid();
    }

    private PatientHistoryRequest sampleRequest(String patientId) {
        return new PatientHistoryRequest(
                patientId,
                Set.of(ChronicDiseaseType.DIABETES_MELLITUS, ChronicDiseaseType.HYPERTENSION),
                null,
                List.of(new PastSurgeryDto("Appendectomy", LocalDate.of(2010, 6, 1), "Uneventful")),
                List.of(),
                List.of(new CurrentMedicationDto("Metformin", "500mg", "Twice daily")),
                List.of(new AllergyDto("Penicillin", "Anaphylaxis", AllergySeverity.ANAPHYLAXIS, true)),
                List.of(),
                true,
                "2015, one unit, road traffic accident",
                5,
                DurationUnit.DAYS,
                LocalDate.now().minusDays(5),
                ProgressionType.WORSENING,
                "Progressive abdominal pain",
                SmokingStatus.FORMER,
                10.0,
                AlcoholFrequency.OCCASIONAL,
                "Beer",
                "1-2 drinks",
                5,
                DietaryHabit.NON_VEGETARIAN,
                PhysicalActivityLevel.SEDENTARY,
                6.0,
                StressLevel.MODERATE,
                "Factory worker, dust exposure",
                "No recent travel"
        );
    }

    @Test
    void createsAndFetchesMedicalHistory() {
        String patientId = registerPatient("9111100001");

        PatientHistoryResponse created = patientHistoryService.create(sampleRequest(patientId));

        assertThat(created.id()).isNotNull();
        assertThat(created.chronicDiseases()).containsExactlyInAnyOrder(
                ChronicDiseaseType.DIABETES_MELLITUS, ChronicDiseaseType.HYPERTENSION);
        assertThat(created.allergies()).extracting(AllergyDto::allergen).containsExactly("Penicillin");
        assertThat(created.allergies().get(0).hardStop()).isTrue();

        PatientHistoryResponse fetched = patientHistoryService.getByPatientId(patientId);
        assertThat(fetched.pastSurgeries()).extracting(PastSurgeryDto::surgeryName).containsExactly("Appendectomy");
    }

    @Test
    void rejectsHistoryForNonExistentPatient() {
        assertThatThrownBy(() -> patientHistoryService.create(sampleRequest("UPID-2026-999999")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsDuplicateHistoryForSamePatient() {
        String patientId = registerPatient("9111100002");
        patientHistoryService.create(sampleRequest(patientId));

        assertThatThrownBy(() -> patientHistoryService.create(sampleRequest(patientId)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updatesExistingHistory() {
        String patientId = registerPatient("9111100003");
        PatientHistoryResponse created = patientHistoryService.create(sampleRequest(patientId));

        PatientHistoryRequest updateRequest = sampleRequest(patientId);
        PatientHistoryResponse updated = patientHistoryService.update(created.id(), updateRequest);

        assertThat(updated.smokingStatus()).isEqualTo(SmokingStatus.FORMER);
    }

    @Test
    void returnsLifestyleSubsetOnly() {
        String patientId = registerPatient("9111100004");
        patientHistoryService.create(sampleRequest(patientId));

        LifestyleResponse lifestyle = patientHistoryService.getLifestyle(patientId);

        assertThat(lifestyle.smokingStatus()).isEqualTo(SmokingStatus.FORMER);
        assertThat(lifestyle.dietaryHabit()).isEqualTo(DietaryHabit.NON_VEGETARIAN);
    }
}
