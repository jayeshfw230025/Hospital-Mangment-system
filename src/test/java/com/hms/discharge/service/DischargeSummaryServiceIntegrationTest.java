package com.hms.discharge.service;

import com.hms.clinical.complaint.SeverityLevel;
import com.hms.common.exception.DuplicateResourceException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.discharge.domain.DischargeCondition;
import com.hms.discharge.domain.DischargeType;
import com.hms.discharge.dto.DischargeSummaryRequest;
import com.hms.discharge.dto.DischargeSummaryResponse;
import com.hms.discharge.dto.WhatsAppDispatchRequest;
import com.hms.discharge.dto.WhatsAppDispatchResponse;
import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.service.IpdAdmissionService;
import com.hms.ipd.procedure.domain.ProcedureType;
import com.hms.ipd.procedure.dto.ProcedureComplicationRequest;
import com.hms.ipd.procedure.dto.ProcedureRequest;
import com.hms.ipd.procedure.dto.ProcedureResponse;
import com.hms.ipd.procedure.service.ProcedureComplicationService;
import com.hms.ipd.procedure.service.ProcedureService;
import com.hms.nutrition.domain.DiseaseCategory;
import com.hms.nutrition.dto.NutritionAssessmentRequest;
import com.hms.nutrition.service.NutritionAssessmentService;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import com.hms.prescription.dto.PrescriptionItemRequest;
import com.hms.prescription.dto.PrescriptionRequest;
import com.hms.prescription.service.DrugService;
import com.hms.prescription.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class DischargeSummaryServiceIntegrationTest {

    @Autowired
    private DischargeSummaryService dischargeSummaryService;

    @Autowired
    private DischargePdfService dischargePdfService;

    @Autowired
    private WhatsAppDispatchService whatsAppDispatchService;

    @Autowired
    private IpdAdmissionService ipdAdmissionService;

    @Autowired
    private ProcedureService procedureService;

    @Autowired
    private ProcedureComplicationService procedureComplicationService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private NutritionAssessmentService nutritionAssessmentService;

    @Autowired
    private PatientService patientService;

    private String patientId;
    private Long admissionId;

    private void setUpAdmission(String contactNumber) {
        PatientRegistrationRequest patientRequest = new PatientRegistrationRequest(
                null, "Discharge Test Patient", LocalDate.of(1970, 1, 1), Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        patientId = patientService.register(patientRequest).upid();

        IpdAdmissionRequest admissionRequest = new IpdAdmissionRequest(
                patientId, AdmissionType.EMERGENCY, AdmissionSource.ER,
                null, null, null, null, "K25.0", null, "GI bleed workup", "Signed");
        admissionId = ipdAdmissionService.create(admissionRequest, null).id();
    }

    private DischargeSummaryRequest sampleRequest() {
        return new DischargeSummaryRequest(
                admissionId, DischargeType.IMPROVED, "K25.9", null,
                "Peptic ulcer disease, resolved with treatment", "Uneventful recovery, tolerating oral diet",
                null, "Follow up in OPD after 1 week", DischargeCondition.STABLE,
                "Dr. Sharma", "Dr. Sharma - signed", true, "Take medications as prescribed", null);
    }

    @Test
    void createsDischargeSummaryAutoPopulatingProceduresComplicationsAndMedications() {
        setUpAdmission("9101800001");

        ProcedureResponse procedure = procedureService.create(new ProcedureRequest(
                admissionId, ProcedureType.OGD, LocalDate.now(), "Dr. Mehta", null,
                Map.of("findings", "Gastric ulcer noted")));
        procedureComplicationService.create(new ProcedureComplicationRequest(
                procedure.id(), "Minor bleeding at biopsy site", SeverityLevel.MILD, LocalDate.now(), "Nurse Rita"));

        Long omeprazoleId = drugService.search("Omeprazole").get(0).id();
        prescriptionService.create(new PrescriptionRequest(
                patientId, null, admissionId, "Dr. Sharma", "Dr. Sharma - signed", null,
                List.of(new PrescriptionItemRequest(omeprazoleId, "20mg", "once daily", "Oral", 14, null, null, null))));

        nutritionAssessmentService.create(new NutritionAssessmentRequest(
                patientId, admissionId, 70.0, 170.0, 2.0, 90.0, 0, false,
                DiseaseCategory.CIRRHOSIS, null, null, null, false, null, null));

        DischargeSummaryResponse response = dischargeSummaryService.create(sampleRequest());

        assertThat(response.significantProcedures()).hasSize(1);
        assertThat(response.significantProcedures().get(0)).contains("OGD");
        assertThat(response.complicationsDuringStay()).hasSize(1);
        assertThat(response.dischargeMedications()).extracting(m -> m.drugName()).contains("Omeprazole");
        assertThat(response.dischargeDietPlan()).contains("Caloric target");
        assertThat(response.lengthOfStayDays()).isGreaterThanOrEqualTo(0);
        assertThat(response.patientName()).isEqualTo("Discharge Test Patient");
    }

    @Test
    void rejectsDuplicateDischargeForSameAdmission() {
        setUpAdmission("9101800002");
        dischargeSummaryService.create(sampleRequest());

        assertThatThrownBy(() -> dischargeSummaryService.create(sampleRequest()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void rejectsDischargeForNonExistentAdmission() {
        DischargeSummaryRequest request = new DischargeSummaryRequest(
                999999L, DischargeType.RECOVERED, "K25.9", null, null, null,
                null, null, DischargeCondition.STABLE, "Dr. Sharma", "Signed", true, null, null);

        assertThatThrownBy(() -> dischargeSummaryService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateRefreshesAutoPopulatedProcedures() {
        setUpAdmission("9101800003");
        DischargeSummaryResponse created = dischargeSummaryService.create(sampleRequest());
        assertThat(created.significantProcedures()).isEmpty();

        procedureService.create(new ProcedureRequest(
                admissionId, ProcedureType.COLONOSCOPY, LocalDate.now(), "Dr. Rao", null,
                Map.of("bowelPreparationQuality", "Good", "findings", "Normal", "cecalIntubation", "true")));

        DischargeSummaryResponse updated = dischargeSummaryService.update(created.id(), sampleRequest());

        assertThat(updated.significantProcedures()).hasSize(1);
    }

    @Test
    void generatesValidPdf() {
        setUpAdmission("9101800004");
        DischargeSummaryResponse created = dischargeSummaryService.create(sampleRequest());

        byte[] pdfBytes = dischargePdfService.generate(created);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void dispatchesWhatsAppWithQrCodeAndFollowUpScheduling() {
        setUpAdmission("9101800005");
        DischargeSummaryResponse created = dischargeSummaryService.create(sampleRequest());

        WhatsAppDispatchResponse response = whatsAppDispatchService.dispatch(
                new WhatsAppDispatchRequest(created.id(), null));

        assertThat(response.phoneNumberUsed()).isEqualTo("9101800005");
        assertThat(response.qrCodeBase64()).isNotBlank();
        assertThat(response.dispatchStatus().name()).isEqualTo("SENT");
        assertThat(response.followUpReminderScheduled()).isFalse();
    }
}
