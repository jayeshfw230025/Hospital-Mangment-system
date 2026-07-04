package com.hms.ipd.procedure.service;

import com.hms.clinical.complaint.SeverityLevel;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.admission.domain.AdmissionSource;
import com.hms.ipd.admission.domain.AdmissionType;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.service.IpdAdmissionService;
import com.hms.ipd.procedure.dto.ProcedureComplicationRequest;
import com.hms.ipd.procedure.dto.ProcedureRequest;
import com.hms.ipd.procedure.dto.ProcedureResponse;
import com.hms.ipd.procedure.dto.ProcedureTypeResponse;
import com.hms.ipd.procedure.domain.ProcedureType;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.service.PatientService;
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
class ProcedureServiceIntegrationTest {

    @Autowired
    private ProcedureService procedureService;

    @Autowired
    private ProcedureComplicationService procedureComplicationService;

    @Autowired
    private IpdAdmissionService ipdAdmissionService;

    @Autowired
    private PatientService patientService;

    private Long createAdmission(String contactNumber) {
        PatientRegistrationRequest patientRequest = new PatientRegistrationRequest(
                null, "Procedure Test Patient", LocalDate.of(1980, 1, 1), Gender.MALE, null, null,
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
    void listsElevenProcedureTypes() {
        List<ProcedureTypeResponse> types = procedureService.listTypes();

        assertThat(types).hasSize(11);
        assertThat(types).extracting(ProcedureTypeResponse::name).contains("OGD", "TIPS", "LIVER_BIOPSY");
    }

    @Test
    void createsOgdProcedureWithRequiredFindings() {
        Long admissionId = createAdmission("9101600001");

        ProcedureResponse response = procedureService.create(new ProcedureRequest(
                admissionId, ProcedureType.OGD, LocalDate.now(), "Dr. Mehta",
                "Uneventful procedure", Map.of("findings", "Mild gastritis, no ulcers",
                        "biopsyDone", "false")));

        assertThat(response.procedureTypeLabel()).isEqualTo("OGD (Upper GI Endoscopy)");
        assertThat(response.details()).containsEntry("findings", "Mild gastritis, no ulcers");
    }

    @Test
    void rejectsOgdProcedureMissingFindings() {
        Long admissionId = createAdmission("9101600002");

        ProcedureRequest request = new ProcedureRequest(
                admissionId, ProcedureType.OGD, LocalDate.now(), "Dr. Mehta", null, Map.of());

        assertThatThrownBy(() -> procedureService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("findings");
    }

    @Test
    void createsColonoscopyRequiringMultipleKeys() {
        Long admissionId = createAdmission("9101600003");

        ProcedureResponse response = procedureService.create(new ProcedureRequest(
                admissionId, ProcedureType.COLONOSCOPY, LocalDate.now(), "Dr. Rao", null,
                Map.of("bowelPreparationQuality", "Good", "findings", "Single 5mm polyp removed",
                        "cecalIntubation", "true")));

        assertThat(response.details()).containsEntry("cecalIntubation", "true");
    }

    @Test
    void rejectsColonoscopyMissingCecalIntubation() {
        Long admissionId = createAdmission("9101600004");

        ProcedureRequest request = new ProcedureRequest(
                admissionId, ProcedureType.COLONOSCOPY, LocalDate.now(), "Dr. Rao", null,
                Map.of("bowelPreparationQuality", "Good", "findings", "Normal"));

        assertThatThrownBy(() -> procedureService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cecalIntubation");
    }

    @Test
    void reportsComplicationAndSeesItNestedInProcedureResponse() {
        Long admissionId = createAdmission("9101600005");
        ProcedureResponse procedure = procedureService.create(new ProcedureRequest(
                admissionId, ProcedureType.LIVER_BIOPSY, LocalDate.now(), "Dr. Singh", null,
                Map.of("approach", "Percutaneous", "site", "Right lobe", "needleSize", "18G", "numberOfPasses", "3")));

        procedureComplicationService.create(new ProcedureComplicationRequest(
                procedure.id(), "Mild post-biopsy pain, resolved with analgesia", SeverityLevel.MILD,
                LocalDate.now(), "Nurse Priya"));

        List<ProcedureResponse> refreshed = procedureService.getByAdmissionId(admissionId);

        assertThat(refreshed.get(0).complications()).hasSize(1);
        assertThat(refreshed.get(0).complications().get(0).severity()).isEqualTo(SeverityLevel.MILD);
    }

    @Test
    void rejectsComplicationForNonExistentProcedure() {
        assertThatThrownBy(() -> procedureComplicationService.create(new ProcedureComplicationRequest(
                999999L, "Bleeding", SeverityLevel.SEVERE, LocalDate.now(), "Dr. Singh")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsProcedureForNonExistentAdmission() {
        ProcedureRequest request = new ProcedureRequest(
                999999L, ProcedureType.TIPS, LocalDate.now(), "Dr. Singh", null,
                Map.of("reason", "Variceal Bleed", "stentType", "Covered", "stentSizeMm", "10"));

        assertThatThrownBy(() -> procedureService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesProcedureNotes() {
        Long admissionId = createAdmission("9101600006");
        ProcedureResponse created = procedureService.create(new ProcedureRequest(
                admissionId, ProcedureType.PARACENTESIS, LocalDate.now(), "Dr. Kapoor", null,
                Map.of("site", "LLQ", "volumeMl", "1500")));

        ProcedureResponse updated = procedureService.update(created.id(), new ProcedureRequest(
                admissionId, ProcedureType.PARACENTESIS, created.procedureDate(), "Dr. Kapoor",
                "Ultrasound-guided, uneventful", Map.of("site", "LLQ", "volumeMl", "1500", "ultrasoundGuidance", "true")));

        assertThat(updated.notes()).isEqualTo("Ultrasound-guided, uneventful");
        assertThat(updated.details()).containsEntry("ultrasoundGuidance", "true");
    }

    @Test
    void listsProceduresForAdmission() {
        Long admissionId = createAdmission("9101600007");
        procedureService.create(new ProcedureRequest(
                admissionId, ProcedureType.CAPSULE_ENDOSCOPY, LocalDate.now(), "Dr. Iyer", null,
                Map.of("capsuleType", "PillCam SB3", "findings", "No abnormality detected")));
        procedureService.create(new ProcedureRequest(
                admissionId, ProcedureType.EUS, LocalDate.now(), "Dr. Iyer", null,
                Map.of("scope", "Radial", "findings", "No mass lesion")));

        List<ProcedureResponse> results = procedureService.getByAdmissionId(admissionId);

        assertThat(results).hasSize(2);
    }
}
