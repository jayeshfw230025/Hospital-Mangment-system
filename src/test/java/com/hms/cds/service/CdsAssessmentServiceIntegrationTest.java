package com.hms.cds.service;

import com.hms.cds.domain.AdditionalFindings;
import com.hms.cds.dto.CdsAlertResponse;
import com.hms.cds.dto.CdsAssessRequest;
import com.hms.clinical.complaint.ComplaintType;
import com.hms.clinical.complaint.FrequencyLevel;
import com.hms.clinical.complaint.SeverityLevel;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.investigation.dto.InvestigationOrderRequest;
import com.hms.investigation.dto.InvestigationReportRequest;
import com.hms.investigation.dto.ResultParameterRequest;
import com.hms.investigation.service.InvestigationOrderService;
import com.hms.investigation.service.InvestigationReportService;
import com.hms.opd.dto.OpdComplaintRequest;
import com.hms.opd.service.OpdComplaintService;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.history.dto.FamilyHistoryRequest;
import com.hms.patient.history.service.FamilyHistoryService;
import com.hms.patient.service.PatientService;
import com.hms.vitals.domain.GagReflexStatus;
import com.hms.vitals.domain.TemperatureUnit;
import com.hms.vitals.dto.IpdVitalsRequest;
import com.hms.vitals.dto.OpdVitalsRequest;
import com.hms.vitals.service.IpdVitalsService;
import com.hms.vitals.service.OpdVitalsService;
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
class CdsAssessmentServiceIntegrationTest {

    @Autowired
    private CdsAssessmentService cdsAssessmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private OpdComplaintService opdComplaintService;

    @Autowired
    private OpdVitalsService opdVitalsService;

    @Autowired
    private IpdVitalsService ipdVitalsService;

    @Autowired
    private FamilyHistoryService familyHistoryService;

    @Autowired
    private InvestigationOrderService investigationOrderService;

    @Autowired
    private InvestigationReportService investigationReportService;

    private String registerPatient(String contactNumber, LocalDate dob) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "CDS Test Patient", dob, Gender.MALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null
        );
        return patientService.register(request).upid();
    }

    @Test
    void triggersMelenaAlertFromOpdComplaint() {
        String patientId = registerPatient("9111100101", LocalDate.of(1980, 1, 1));
        Long visitId = 2001L;
        opdComplaintService.create(new OpdComplaintRequest(
                visitId, ComplaintType.MELENA, SeverityLevel.MODERATE, 2, null, FrequencyLevel.FREQUENT,
                LocalDate.now(), null, Map.of("amount", "moderate")));

        List<CdsAlertResponse> alerts = cdsAssessmentService.assess(
                new CdsAssessRequest(patientId, visitId, null, null));

        assertThat(alerts).extracting(CdsAlertResponse::ruleName).contains("MELENA");
    }

    @Test
    void triggersHematemesisHypotensionAlert() {
        String patientId = registerPatient("9111100102", LocalDate.of(1975, 3, 3));
        Long visitId = 2002L;
        opdComplaintService.create(new OpdComplaintRequest(
                visitId, ComplaintType.HEMATEMESIS, SeverityLevel.SEVERE, 1, null, FrequencyLevel.FREQUENT,
                LocalDate.now(), null, Map.of("amount", "large")));
        opdVitalsService.record(new OpdVitalsRequest(visitId, patientId, 85, 55, 110, 20,
                37.0, TemperatureUnit.CELSIUS, null, null, 96, 5, 100));

        List<CdsAlertResponse> alerts = cdsAssessmentService.assess(
                new CdsAssessRequest(patientId, visitId, null, null));

        assertThat(alerts).extracting(CdsAlertResponse::ruleName).contains("HEMATEMESIS_HYPOTENSION");
    }

    @Test
    void triggersJaundiceDarkUrineFromAdditionalFindings() {
        String patientId = registerPatient("9111100103", LocalDate.of(1970, 5, 5));
        Long visitId = 2003L;
        opdComplaintService.create(new OpdComplaintRequest(
                visitId, ComplaintType.JAUNDICE, SeverityLevel.MODERATE, 3, null, null,
                LocalDate.now(), null, Map.of("pruritus", "false")));

        List<CdsAlertResponse> alerts = cdsAssessmentService.assess(new CdsAssessRequest(
                patientId, visitId, null,
                new AdditionalFindings(true, false, false, false, false)));

        assertThat(alerts).extracting(CdsAlertResponse::ruleName).contains("JAUNDICE_DARK_URINE");
    }

    @Test
    void triggersFamilyHistoryCrcAgeAlertRegardlessOfComplaints() {
        String patientId = registerPatient("9111100104", LocalDate.of(1965, 1, 1));
        familyHistoryService.create(new FamilyHistoryRequest(
                patientId, false, false, true, "Colorectal cancer", false, false, false, null));

        List<CdsAlertResponse> alerts = cdsAssessmentService.assess(
                new CdsAssessRequest(patientId, 2004L, null, null));

        assertThat(alerts).extracting(CdsAlertResponse::ruleName).contains("FAMILY_HISTORY_CRC_AGE");
    }

    @Test
    void triggersDecreasedGcsAndRespiratoryDistressForIpdAdmission() {
        String patientId = registerPatient("9111100105", LocalDate.of(1960, 6, 6));
        Long admissionId = 3001L;
        ipdVitalsService.record(new IpdVitalsRequest(admissionId, patientId, 110, 70, 90, 18,
                37.0, TemperatureUnit.CELSIUS, null, null, 90, 4, 110,
                420, 0, 10, 8.0, GagReflexStatus.PRESENT));

        List<CdsAlertResponse> alerts = cdsAssessmentService.assess(
                new CdsAssessRequest(patientId, null, admissionId, null));

        assertThat(alerts).extracting(CdsAlertResponse::ruleName)
                .contains("DECREASED_GCS", "RESPIRATORY_DISTRESS");
    }

    @Test
    void triggersHPyloriPositiveFromInvestigationReport() {
        String patientId = registerPatient("9111100106", LocalDate.of(1990, 1, 1));
        Long visitId = 2005L;
        var order = investigationOrderService.createOrder(
                new InvestigationOrderRequest(patientId, visitId, null, "H_PYLORI", null));
        investigationReportService.submit(new InvestigationReportRequest(
                order.id(), LocalDate.now(),
                List.of(new ResultParameterRequest("H.pylori Antigen", "Positive", null, null, null, true)),
                null), null);

        List<CdsAlertResponse> alerts = cdsAssessmentService.assess(
                new CdsAssessRequest(patientId, visitId, null, null));

        assertThat(alerts).extracting(CdsAlertResponse::ruleName).contains("H_PYLORI_POSITIVE");
    }

    @Test
    void rejectsAssessWithoutVisitOrAdmission() {
        String patientId = registerPatient("9111100107", LocalDate.of(1990, 1, 1));

        assertThatThrownBy(() -> cdsAssessmentService.assess(new CdsAssessRequest(patientId, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAssessForNonExistentPatient() {
        assertThatThrownBy(() -> cdsAssessmentService.assess(
                new CdsAssessRequest("UPID-2026-444444", 2006L, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void persistsAlertsRetrievableByGetByPatientId() {
        String patientId = registerPatient("9111100108", LocalDate.of(1980, 1, 1));
        Long visitId = 2007L;
        opdComplaintService.create(new OpdComplaintRequest(
                visitId, ComplaintType.MELENA, SeverityLevel.MODERATE, 2, null, FrequencyLevel.FREQUENT,
                LocalDate.now(), null, Map.of("amount", "moderate")));
        cdsAssessmentService.assess(new CdsAssessRequest(patientId, visitId, null, null));

        List<CdsAlertResponse> history = cdsAssessmentService.getByPatientId(patientId);

        assertThat(history).extracting(CdsAlertResponse::ruleName).contains("MELENA");
    }
}
