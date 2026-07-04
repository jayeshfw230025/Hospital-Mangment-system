package com.hms.fhir;

import com.hms.diagnosis.domain.DiagnosisType;
import com.hms.diagnosis.dto.DiagnosisRequest;
import com.hms.diagnosis.service.DiagnosisService;
import com.hms.fhir.dto.DocumentReferenceRequest;
import com.hms.fhir.dto.MedicationRequestSubmission;
import com.hms.fhir.domain.DocumentSourceType;
import com.hms.fhir.model.FhirCondition;
import com.hms.fhir.model.FhirDocumentReference;
import com.hms.fhir.model.FhirEncounter;
import com.hms.fhir.model.FhirMedicationRequest;
import com.hms.fhir.model.FhirObservation;
import com.hms.fhir.model.FhirPatient;
import com.hms.fhir.service.FhirService;
import com.hms.integration.abdm.dto.AbdmConsentRequest;
import com.hms.integration.abdm.dto.AbdmConsentResponse;
import com.hms.integration.abdm.dto.AbdmHealthRecordRequest;
import com.hms.integration.abdm.dto.AbdmHealthRecordResponse;
import com.hms.integration.abdm.dto.AbdmLinkRequest;
import com.hms.integration.abdm.service.AbdmIntegrationService;
import com.hms.integration.lis.dto.LisImportResponse;
import com.hms.integration.lis.service.LisRisIntegrationService;
import com.hms.investigation.dto.InvestigationOrderRequest;
import com.hms.investigation.dto.InvestigationReportRequest;
import com.hms.investigation.dto.ResultParameterRequest;
import com.hms.investigation.service.InvestigationOrderService;
import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.service.IpdAdmissionService;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
import com.hms.prescription.domain.FoodInstruction;
import com.hms.prescription.service.DrugService;
import com.hms.vitals.domain.TemperatureUnit;
import com.hms.vitals.dto.OpdVitalsRequest;
import com.hms.vitals.service.OpdVitalsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class FhirAndIntegrationServiceIntegrationTest {

    @Autowired
    private FhirService fhirService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private OpdVitalsService opdVitalsService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private IpdAdmissionService ipdAdmissionService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private AbdmIntegrationService abdmIntegrationService;

    @Autowired
    private InvestigationOrderService investigationOrderService;

    @Autowired
    private LisRisIntegrationService lisRisIntegrationService;

    private String registerPatient(String contactNumber) {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                null, "Fhir Test Patient", LocalDate.of(1988, 5, 5), Gender.FEMALE, null, null,
                "Indian", null, null, null, contactNumber, null, null, null, null,
                null, null, null, null, null);
        return patientService.register(request).upid();
    }

    private Long drugIdFor(String genericName) {
        return drugService.search(genericName).stream()
                .filter(d -> d.genericName().equalsIgnoreCase(genericName))
                .findFirst().orElseThrow().id();
    }

    @Test
    void getPatientReturnsFhirShapedResourceWithUpidIdentifier() {
        String upid = registerPatient("9102000001");

        FhirPatient fhirPatient = fhirService.getPatient(upid);

        assertThat(fhirPatient.resourceType()).isEqualTo("Patient");
        assertThat(fhirPatient.id()).isEqualTo(upid);
        assertThat(fhirPatient.identifier()).anyMatch(i -> i.value().equals(upid));
        assertThat(fhirPatient.name().get(0).text()).isEqualTo("Fhir Test Patient");
        assertThat(fhirPatient.gender()).isEqualTo("female");
    }

    @Test
    void createPatientFromFhirResourceRegistersRealPatient() {
        FhirPatient incoming = new FhirPatient(
                null,
                java.util.List.of(),
                java.util.List.of(new com.hms.fhir.model.FhirHumanName("Inbound Fhir Patient")),
                "male",
                "1979-03-03",
                java.util.List.of(new com.hms.fhir.model.FhirContactPoint("phone", "9102000002", "mobile")));

        FhirPatient created = fhirService.createPatient(incoming);

        assertThat(created.id()).isNotBlank();
        assertThat(created.name().get(0).text()).isEqualTo("Inbound Fhir Patient");

        FhirPatient fetched = fhirService.getPatient(created.id());
        assertThat(fetched.id()).isEqualTo(created.id());
    }

    @Test
    void observationsIncludeVitalsPanelWithComponents() {
        String upid = registerPatient("9102000003");
        opdVitalsService.record(new OpdVitalsRequest(
                77770001L, upid, 120, 80, 78, 16, 37.0, TemperatureUnit.CELSIUS,
                170.0, 70.0, 98, 2, 110));

        var observations = fhirService.getObservations(upid, "vital-signs");

        assertThat(observations).isNotEmpty();
        FhirObservation observation = observations.get(0);
        assertThat(observation.resourceType()).isEqualTo("Observation");
        assertThat(observation.category().text()).isEqualTo("Vital Signs");
        assertThat(observation.component()).isNotEmpty();
    }

    @Test
    void conditionsMapDiagnosisToIcd10Coding() {
        String upid = registerPatient("9102000004");
        diagnosisService.create(new DiagnosisRequest(upid, "K61.0", DiagnosisType.PRIMARY, null, null, null));

        var conditions = fhirService.getConditions(upid);

        assertThat(conditions).isNotEmpty();
        FhirCondition condition = conditions.get(0);
        assertThat(condition.code().coding().get(0).code()).isEqualTo("K61.0");
        assertThat(condition.subject().reference()).isEqualTo("Patient/" + upid);
    }

    @Test
    void encountersIncludeIpdAdmissionAsInpatientClass() {
        String upid = registerPatient("9102000005");
        ipdAdmissionService.create(new IpdAdmissionRequest(
                upid, AdmissionType.EMERGENCY, AdmissionSource.ER, null, null, null, null,
                "K25.0", null, "Fhir test admission", "Signed"), null);

        var encounters = fhirService.getEncounters(upid);

        assertThat(encounters).anyMatch(e -> e.classCoding().code().equals("IMP"));
    }

    @Test
    void medicationRequestSubmissionCreatesRealPrescription() {
        String upid = registerPatient("9102000006");
        Long omeprazoleId = drugIdFor("Omeprazole");

        MedicationRequestSubmission submission = new MedicationRequestSubmission(
                upid, 77770002L, null, "Dr. Fhir Tester", "Signed",
                omeprazoleId, "20mg", "Once daily", "Oral", 14, FoodInstruction.BEFORE_FOOD);

        FhirMedicationRequest created = fhirService.createMedicationRequest(submission);

        assertThat(created.status()).isEqualTo("active");
        assertThat(created.subject().reference()).isEqualTo("Patient/" + upid);
        assertThat(created.dosageInstruction()).isNotEmpty();
    }

    @Test
    void documentReferenceForConsentRequiresConsentDocumentOnFile() {
        String upid = registerPatient("9102000007");
        var admission = ipdAdmissionService.create(new IpdAdmissionRequest(
                upid, AdmissionType.EMERGENCY, AdmissionSource.ER, null, null, null, null,
                "K25.0", null, "Fhir test admission no consent", "Signed"), null);

        assertThatThrownBy(() -> fhirService.createDocumentReference(
                new DocumentReferenceRequest(DocumentSourceType.CONSENT_FORM, admission.id())))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void documentReferenceForLabReportReflectsReportMetadata() {
        String upid = registerPatient("9102000008");
        var order = investigationOrderService.createOrder(new InvestigationOrderRequest(
                upid, 77770003L, null, "CBC", "Fhir test order"));
        var lisResponse = lisRisIntegrationService.importLabResult(new InvestigationReportRequest(
                order.id(), LocalDate.now(),
                java.util.List.of(new ResultParameterRequest("Hemoglobin", "13.5", "g/dL", 12.0, 16.0, null)),
                "Auto-imported"));

        FhirDocumentReference documentReference = fhirService.createDocumentReference(
                new DocumentReferenceRequest(DocumentSourceType.LAB_REPORT, lisResponse.report().id()));

        assertThat(documentReference.resourceType()).isEqualTo("DocumentReference");
        assertThat(documentReference.subject().reference()).isEqualTo("Patient/" + upid);
    }

    @Test
    void risImportPersistsRadiologyReportThroughSamePipeline() {
        String upid = registerPatient("9102000009");
        var order = investigationOrderService.createOrder(new InvestigationOrderRequest(
                upid, 77770004L, null, "USG_ABDOMEN", "Fhir test imaging order"));

        LisImportResponse response = lisRisIntegrationService.importRadiologyResult(new InvestigationReportRequest(
                order.id(), LocalDate.now(),
                java.util.List.of(new ResultParameterRequest("Impression", "Normal study", null, null, null, false)),
                null));

        assertThat(response.report().patientId()).isEqualTo(upid);
        assertThat(response.message()).contains("RIS");
    }

    @Test
    void lisStatusHonestlyReportsNoLiveConnection() {
        var status = lisRisIntegrationService.getStatus();

        assertThat(status.connected()).isFalse();
        assertThat(status.message()).isNotBlank();
    }

    @Test
    void abdmConsentGatesHealthRecordAccess() {
        String upid = registerPatient("9102000010");
        diagnosisService.create(new DiagnosisRequest(upid, "K62.4", DiagnosisType.PRIMARY, null, null, null));

        AbdmConsentResponse consent = abdmIntegrationService.createConsent(new AbdmConsentRequest(
                upid, "Treatment", java.util.List.of("DiagnosticReport", "Prescription"), 30));

        assertThat(consent.consentId()).isNotBlank();

        AbdmHealthRecordResponse healthRecord = abdmIntegrationService.getHealthRecord(
                new AbdmHealthRecordRequest(upid, consent.consentId()));

        assertThat(healthRecord.bundle().resourceType()).isEqualTo("Bundle");
        assertThat(healthRecord.bundle().entry()).isNotEmpty();
    }

    @Test
    void abdmHealthRecordRejectsUnknownConsent() {
        String upid = registerPatient("9102000011");

        assertThatThrownBy(() -> abdmIntegrationService.getHealthRecord(
                new AbdmHealthRecordRequest(upid, "non-existent-consent-id")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void abdmLinkDelegatesToExistingAbhaFlow() {
        String upid = registerPatient("9102000012");

        var response = abdmIntegrationService.link(new AbdmLinkRequest(upid, "12-3456-7890-1234"));

        assertThat(response.txnId()).isNotBlank();
    }
}
