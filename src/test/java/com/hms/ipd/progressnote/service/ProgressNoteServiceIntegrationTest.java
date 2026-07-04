package com.hms.ipd.progressnote.service;

import com.hms.clinical.complaint.SeverityLevel;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.service.IpdAdmissionService;
import com.hms.ipd.progressnote.domain.ActivityLevel;
import com.hms.ipd.progressnote.domain.AppetiteLevel;
import com.hms.ipd.progressnote.domain.MedicationPlanStatus;
import com.hms.ipd.progressnote.dto.MedicationPlanItemDto;
import com.hms.ipd.progressnote.dto.ProgressNoteRequest;
import com.hms.ipd.progressnote.dto.ProgressNoteResponse;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import com.hms.vitals.domain.GagReflexStatus;
import com.hms.vitals.domain.TemperatureUnit;
import com.hms.vitals.dto.IpdVitalsRequest;
import com.hms.vitals.service.IpdVitalsService;
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
class ProgressNoteServiceIntegrationTest {

    @Autowired
    private ProgressNoteService progressNoteService;

    @Autowired
    private IpdAdmissionService ipdAdmissionService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private IpdVitalsService ipdVitalsService;

    private Long createAdmission(String contactNumber) {
        PatientRegistrationRequest patientRequest = new PatientRegistrationRequest(
                null, "Progress Note Test Patient", LocalDate.of(1980, 1, 1), Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        String patientId = patientService.register(patientRequest).upid();

        IpdAdmissionRequest admissionRequest = new IpdAdmissionRequest(
                patientId, AdmissionType.EMERGENCY, AdmissionSource.ER,
                null, null, null, null, "K25.0", null, "Test admission", "Signed");
        return ipdAdmissionService.create(admissionRequest, null).id();
    }

    private ProgressNoteRequest sampleRequest(Long admissionId) {
        return new ProgressNoteRequest(
                admissionId,
                "Mild abdominal pain, improving", 3, false, AppetiteLevel.MODERATE,
                "1x/day", "Formed", "Slept well", "Feeling better",
                "Comfortable, no acute distress", "Soft, mild epigastric tenderness", "None",
                "Improving peptic ulcer disease", "Peptic ulcer disease", "K25.9", SeverityLevel.MILD,
                List.of("None"),
                List.of(new MedicationPlanItemDto("Omeprazole", MedicationPlanStatus.CONTINUED, "Continue 20mg OD")),
                List.of("CBC"), List.of("Dietician"), "Soft diet", ActivityLevel.ASSISTED_AMBULATION,
                "Plan discharge in 2 days if stable");
    }

    @Test
    void createsProgressNoteAndAutoLinksLatestVitals() {
        Long admissionId = createAdmission("9101400001");
        String patientId = ipdAdmissionService.getById(admissionId).patientId();

        ipdVitalsService.record(new IpdVitalsRequest(admissionId, patientId, 118, 76, 82, 16,
                37.0, TemperatureUnit.CELSIUS, null, null, 97, 2, 100,
                400, 0, 15, 7.0, GagReflexStatus.PRESENT));

        ProgressNoteResponse response = progressNoteService.create(sampleRequest(admissionId));

        assertThat(response.vitalsSnapshot()).isNotNull();
        assertThat(response.vitalsSnapshot().heartRate()).isEqualTo(82);
        assertThat(response.icd10Description()).isEqualTo("Gastric ulcer, unspecified");
        assertThat(response.medicationPlanItems()).extracting(MedicationPlanItemDto::drugName).containsExactly("Omeprazole");
    }

    @Test
    void createsProgressNoteWithoutVitalsWhenNoneRecorded() {
        Long admissionId = createAdmission("9101400002");

        ProgressNoteResponse response = progressNoteService.create(sampleRequest(admissionId));

        assertThat(response.vitalsSnapshot()).isNull();
    }

    @Test
    void rejectsProgressNoteForNonExistentAdmission() {
        assertThatThrownBy(() -> progressNoteService.create(sampleRequest(999999L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesProgressNoteFindings() {
        Long admissionId = createAdmission("9101400003");
        ProgressNoteResponse created = progressNoteService.create(sampleRequest(admissionId));

        ProgressNoteRequest updateRequest = new ProgressNoteRequest(
                admissionId, "Pain resolved", 0, false, AppetiteLevel.GOOD,
                "1x/day", "Formed", "Slept well", "Much better",
                "Comfortable", "Soft, non-tender", "None",
                "Resolved", "Peptic ulcer disease", "K25.9", SeverityLevel.MILD,
                List.of(), List.of(), List.of(), List.of(), "Regular diet",
                ActivityLevel.NORMAL_ACTIVITY, "Ready for discharge");

        ProgressNoteResponse updated = progressNoteService.update(created.id(), updateRequest);

        assertThat(updated.painScore()).isEqualTo(0);
        assertThat(updated.dischargePlanningNotes()).isEqualTo("Ready for discharge");
    }

    @Test
    void listsProgressNotesForAdmission() {
        Long admissionId = createAdmission("9101400004");
        progressNoteService.create(sampleRequest(admissionId));
        progressNoteService.create(sampleRequest(admissionId));

        List<ProgressNoteResponse> results = progressNoteService.getByAdmissionId(admissionId);

        assertThat(results).hasSize(2);
    }
}
