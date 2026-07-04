package com.hms.nutrition.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.nutrition.domain.DiseaseCategory;
import com.hms.nutrition.domain.MustRiskCategory;
import com.hms.nutrition.dto.NutritionAssessmentRequest;
import com.hms.nutrition.dto.NutritionAssessmentResponse;
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
import static org.assertj.core.api.Assertions.offset;

@SpringBootTest
@ActiveProfiles("test")
class NutritionAssessmentServiceIntegrationTest {

    @Autowired
    private NutritionAssessmentService nutritionAssessmentService;

    @Autowired
    private PatientService patientService;

    private String registerPatient(String contactNumber, LocalDate dob) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Nutrition Test Patient", dob, Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    @Test
    void createsAssessmentComputingBmiAgeAndScores() {
        String patientId = registerPatient("9101700001", LocalDate.now().minusYears(75));

        NutritionAssessmentRequest request = new NutritionAssessmentRequest(
                patientId, null, 70.0, 170.0, 16.0, 90.0, 1, false,
                DiseaseCategory.CIRRHOSIS, null, 80.0, 60.0, false,
                "Moderate malnutrition risk, cirrhotic patient", "Reassess in 1 week");

        NutritionAssessmentResponse response = nutritionAssessmentService.create(request);

        assertThat(response.bmi()).isCloseTo(24.22, offset(0.05));
        assertThat(response.age()).isEqualTo(75);
        assertThat(response.nrsAtRisk()).isTrue();
        assertThat(response.caloricTargetMinKcalPerDay()).isCloseTo(1750.0, offset(0.01));
        assertThat(response.fluidRequirementMlPerDay()).isCloseTo(2100.0, offset(0.01));
    }

    @Test
    void mustRiskCategoryIsPersistedAndReturned() {
        String patientId = registerPatient("9101700002", LocalDate.of(1990, 1, 1));

        NutritionAssessmentRequest request = new NutritionAssessmentRequest(
                patientId, null, 55.0, 165.0, 12.0, 40.0, 0, true,
                null, null, null, null, null, null, null);

        NutritionAssessmentResponse response = nutritionAssessmentService.create(request);

        assertThat(response.mustRiskCategory()).isEqualTo(MustRiskCategory.HIGH);
    }

    @Test
    void appliesFluidOverrideWhenProvided() {
        String patientId = registerPatient("9101700003", LocalDate.of(1985, 1, 1));

        NutritionAssessmentRequest request = new NutritionAssessmentRequest(
                patientId, null, 70.0, 170.0, 0.0, 100.0, 0, false,
                DiseaseCategory.IBD, 2500.0, null, null, null, null, null);

        NutritionAssessmentResponse response = nutritionAssessmentService.create(request);

        assertThat(response.fluidRequirementMlPerDay()).isEqualTo(2500.0);
    }

    @Test
    void rejectsAssessmentForNonExistentPatient() {
        NutritionAssessmentRequest request = new NutritionAssessmentRequest(
                "UPID-2026-222222", null, 70.0, 170.0, 0.0, 100.0, 0, false,
                null, null, null, null, null, null, null);

        assertThatThrownBy(() -> nutritionAssessmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesAssessmentAndRecomputesScores() {
        String patientId = registerPatient("9101700004", LocalDate.of(1980, 1, 1));
        NutritionAssessmentResponse created = nutritionAssessmentService.create(new NutritionAssessmentRequest(
                patientId, null, 70.0, 170.0, 2.0, 90.0, 0, false,
                null, null, null, null, null, null, null));

        NutritionAssessmentResponse updated = nutritionAssessmentService.update(created.id(), new NutritionAssessmentRequest(
                patientId, null, 65.0, 170.0, 12.0, 40.0, 2, true,
                DiseaseCategory.GI_CANCER, null, null, null, true, "Now at significant risk", "Daily monitoring"));

        assertThat(updated.nrsAtRisk()).isTrue();
        assertThat(updated.dieticianAssessment()).isEqualTo("Now at significant risk");
        assertThat(updated.enteralParenteralSupport()).isTrue();
    }

    @Test
    void listsAssessmentsForPatient() {
        String patientId = registerPatient("9101700005", LocalDate.of(1975, 1, 1));
        nutritionAssessmentService.create(new NutritionAssessmentRequest(
                patientId, null, 70.0, 170.0, 0.0, 100.0, 0, false, null, null, null, null, null, null, null));
        nutritionAssessmentService.create(new NutritionAssessmentRequest(
                patientId, null, 68.0, 170.0, 3.0, 90.0, 0, false, null, null, null, null, null, null, null));

        List<NutritionAssessmentResponse> results = nutritionAssessmentService.getByPatientId(patientId);

        assertThat(results).hasSize(2);
    }
}
