package com.hms.clinical.examination.service;

import com.hms.clinical.examination.BowelSounds;
import com.hms.clinical.examination.ExaminationContext;
import com.hms.clinical.examination.MassConsistency;
import com.hms.clinical.examination.MassMobility;
import com.hms.clinical.examination.PupillaryReflex;
import com.hms.clinical.examination.dto.AbdominalExaminationDto;
import com.hms.clinical.examination.dto.AscitesAssessmentDto;
import com.hms.clinical.examination.dto.ClinicalExaminationRequest;
import com.hms.clinical.examination.dto.ClinicalExaminationResponse;
import com.hms.clinical.examination.dto.DigitalRectalExaminationDto;
import com.hms.clinical.examination.dto.GiMassExaminationDto;
import com.hms.clinical.examination.dto.HerniaExaminationDto;
import com.hms.clinical.examination.dto.JaundiceAssessmentDto;
import com.hms.clinical.examination.dto.LymphNodeExaminationDto;
import com.hms.clinical.examination.dto.SystemicExaminationDto;
import com.hms.common.exception.ResourceNotFoundException;
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
class ClinicalExaminationServiceIntegrationTest {

    @Autowired
    private ClinicalExaminationService clinicalExaminationService;

    @Autowired
    private PatientService patientService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Exam Test Patient", LocalDate.of(1980, 4, 10), Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    private ClinicalExaminationRequest opdRequest(String patientId, Long visitId) {
        return new ClinicalExaminationRequest(
                patientId, visitId, null,
                new AbdominalExaminationDto(false, true, false, true, "Epigastric", false, false,
                        "Mild hepatomegaly", true, false, BowelSounds.NORMAL, "Soft, mildly distended"),
                new DigitalRectalExaminationDto(false, false, true, "Normal", false, null, false, false, null),
                new JaundiceAssessmentDto(true, true, false, false),
                new HerniaExaminationDto(false, null, null, null),
                new LymphNodeExaminationDto(false, false, false, null),
                new GiMassExaminationDto(false, null, null, null, null),
                new AscitesAssessmentDto(false, false, null),
                null,
                null
        );
    }

    private ClinicalExaminationRequest ipdRequest(String patientId, Long admissionId) {
        return new ClinicalExaminationRequest(
                patientId, null, admissionId,
                new AbdominalExaminationDto(false, true, false, true, "Diffuse", true, true,
                        null, true, false, BowelSounds.DECREASED, "Guarding present"),
                new DigitalRectalExaminationDto(false, false, false, "Normal", false, null, true, false, null),
                new JaundiceAssessmentDto(false, false, false, false),
                new HerniaExaminationDto(false, null, null, null),
                new LymphNodeExaminationDto(false, false, false, null),
                new GiMassExaminationDto(false, null, null, null, null),
                new AscitesAssessmentDto(true, true, "Grade 2 ascites"),
                new SystemicExaminationDto("Normal", "Vesicular", "S1S2 normal", false, null,
                        "Not raised", 14, PupillaryReflex.NORMAL, "Normal power all limbs", "Intact"),
                92.5
        );
    }

    @Test
    void createsOpdExaminationAndFetchesByVisit() {
        String patientId = registerPatient("9555500001");

        ClinicalExaminationResponse created = clinicalExaminationService.createOpd(opdRequest(patientId, 501L));

        assertThat(created.examinationContext()).isEqualTo(ExaminationContext.OPD);
        assertThat(created.visitId()).isEqualTo(501L);
        assertThat(created.abdominalExamination().bowelSounds()).isEqualTo(BowelSounds.NORMAL);
        assertThat(created.systemicExamination()).isNull();

        List<ClinicalExaminationResponse> byVisit = clinicalExaminationService.getByVisitId(501L);
        assertThat(byVisit).hasSize(1);
    }

    @Test
    void createsIpdExaminationWithSystemicAndGirthAndFetchesByAdmission() {
        String patientId = registerPatient("9555500002");

        ClinicalExaminationResponse created = clinicalExaminationService.createIpd(ipdRequest(patientId, 601L));

        assertThat(created.examinationContext()).isEqualTo(ExaminationContext.IPD);
        assertThat(created.admissionId()).isEqualTo(601L);
        assertThat(created.systemicExamination().gcsScore()).isEqualTo(14);
        assertThat(created.abdominalGirthCm()).isEqualTo(92.5);
        assertThat(created.ascitesAssessment().shiftingDullnessPresent()).isTrue();

        List<ClinicalExaminationResponse> byAdmission = clinicalExaminationService.getByAdmissionId(601L);
        assertThat(byAdmission).hasSize(1);
    }

    @Test
    void rejectsOpdExaminationWithoutVisitId() {
        String patientId = registerPatient("9555500003");
        ClinicalExaminationRequest invalid = opdRequest(patientId, null);

        assertThatThrownBy(() -> clinicalExaminationService.createOpd(invalid))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsExaminationForNonExistentPatient() {
        assertThatThrownBy(() -> clinicalExaminationService.createOpd(opdRequest("UPID-2026-666666", 502L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesExaminationThroughSharedPutById() {
        String patientId = registerPatient("9555500004");
        ClinicalExaminationResponse created = clinicalExaminationService.createOpd(opdRequest(patientId, 503L));

        ClinicalExaminationRequest updateRequest = new ClinicalExaminationRequest(
                patientId, 503L, null,
                new AbdominalExaminationDto(false, false, false, false, null, false, false,
                        null, false, false, BowelSounds.NORMAL, "Improved, soft non-tender"),
                created.digitalRectalExamination(),
                created.jaundiceAssessment(),
                created.herniaExamination(),
                created.lymphNodeExamination(),
                created.giMassExamination(),
                created.ascitesAssessment(),
                null,
                null
        );

        ClinicalExaminationResponse updated = clinicalExaminationService.update(created.id(), updateRequest);

        assertThat(updated.abdominalExamination().tenderness()).isFalse();
        assertThat(updated.abdominalExamination().notes()).isEqualTo("Improved, soft non-tender");
    }
}
