package com.hms.fhir.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.diagnosis.service.DiagnosisService;
import com.hms.discharge.dto.DischargeSummaryResponse;
import com.hms.discharge.service.DischargeSummaryService;
import com.hms.fhir.domain.DocumentSourceType;
import com.hms.fhir.dto.DocumentReferenceRequest;
import com.hms.fhir.dto.MedicationRequestSubmission;
import com.hms.fhir.mapper.FhirMapper;
import com.hms.fhir.model.FhirCondition;
import com.hms.fhir.model.FhirDocumentReference;
import com.hms.fhir.model.FhirEncounter;
import com.hms.fhir.model.FhirMedicationRequest;
import com.hms.fhir.model.FhirObservation;
import com.hms.fhir.model.FhirPatient;
import com.hms.fhir.model.FhirReference;
import com.hms.investigation.service.InvestigationReportService;
import com.hms.ipd.admission.dto.IpdAdmissionResponse;
import com.hms.ipd.admission.service.IpdAdmissionService;
import com.hms.patient.domain.Gender;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.service.PatientService;
import com.hms.prescription.dto.PrescriptionItemRequest;
import com.hms.prescription.dto.PrescriptionRequest;
import com.hms.prescription.dto.PrescriptionResponse;
import com.hms.prescription.service.PrescriptionService;
import com.hms.vitals.dto.OpdVitalsResponse;
import com.hms.vitals.service.IpdVitalsService;
import com.hms.vitals.service.OpdVitalsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FhirService {

    private final PatientService patientService;
    private final OpdVitalsService opdVitalsService;
    private final IpdVitalsService ipdVitalsService;
    private final InvestigationReportService investigationReportService;
    private final DiagnosisService diagnosisService;
    private final PrescriptionService prescriptionService;
    private final IpdAdmissionService ipdAdmissionService;
    private final DischargeSummaryService dischargeSummaryService;
    private final FhirMapper fhirMapper;

    public FhirService(PatientService patientService, OpdVitalsService opdVitalsService,
                        IpdVitalsService ipdVitalsService, InvestigationReportService investigationReportService,
                        DiagnosisService diagnosisService, PrescriptionService prescriptionService,
                        IpdAdmissionService ipdAdmissionService, DischargeSummaryService dischargeSummaryService,
                        FhirMapper fhirMapper) {
        this.patientService = patientService;
        this.opdVitalsService = opdVitalsService;
        this.ipdVitalsService = ipdVitalsService;
        this.investigationReportService = investigationReportService;
        this.diagnosisService = diagnosisService;
        this.prescriptionService = prescriptionService;
        this.ipdAdmissionService = ipdAdmissionService;
        this.dischargeSummaryService = dischargeSummaryService;
        this.fhirMapper = fhirMapper;
    }

    public FhirPatient getPatient(String upid) {
        return fhirMapper.toFhirPatient(patientService.getByUpid(upid));
    }

    public FhirPatient createPatient(FhirPatient fhirPatient) {
        String fullName = fhirPatient.name() == null || fhirPatient.name().isEmpty()
                ? null : fhirPatient.name().get(0).text();
        LocalDate birthDate = fhirPatient.birthDate() == null ? null : LocalDate.parse(fhirPatient.birthDate());
        Gender gender = mapGender(fhirPatient.gender());
        String primaryContact = fhirPatient.telecom() == null ? null : fhirPatient.telecom().stream()
                .filter(t -> "phone".equalsIgnoreCase(t.system()))
                .map(t -> t.value())
                .findFirst().orElse(null);
        String abhaNumber = fhirPatient.identifier() == null ? null : fhirPatient.identifier().stream()
                .filter(i -> i.system() != null && i.system().contains("ndhm"))
                .map(i -> i.value())
                .findFirst().orElse(null);

        PatientRegistrationRequest request = new PatientRegistrationRequest(
                abhaNumber, fullName, birthDate, gender, null, null,
                null, null, null, null, primaryContact, null, null, null, null,
                null, null, null, null, null);

        PatientResponse created = patientService.register(request);
        return fhirMapper.toFhirPatient(created);
    }

    private Gender mapGender(String fhirGender) {
        if (fhirGender == null) {
            return null;
        }
        return switch (fhirGender.toLowerCase()) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            default -> Gender.OTHER;
        };
    }

    public List<FhirObservation> getObservations(String patientId, String category) {
        PatientResponse patient = patientService.getByUpid(patientId);
        FhirReference subject = FhirReference.toPatient(patient.upid(), patient.fullName());
        List<FhirObservation> observations = new ArrayList<>();

        boolean includeVitals = category == null || category.equalsIgnoreCase("vital-signs");
        boolean includeLabs = category == null || category.equalsIgnoreCase("laboratory");

        if (includeVitals) {
            opdVitalsService.getByPatientId(patientId).forEach(v -> observations.add(fhirMapper.toVitalsObservation(v, subject)));
            ipdVitalsService.getByPatientId(patientId).forEach(v -> observations.add(fhirMapper.toVitalsObservation(v, subject)));
        }
        if (includeLabs) {
            investigationReportService.getByPatientId(patientId)
                    .forEach(r -> observations.add(fhirMapper.toLabObservation(r, subject)));
        }
        return observations;
    }

    public List<FhirCondition> getConditions(String patientId) {
        PatientResponse patient = patientService.getByUpid(patientId);
        FhirReference subject = FhirReference.toPatient(patient.upid(), patient.fullName());
        return diagnosisService.getByPatientId(patientId).stream()
                .map(d -> fhirMapper.toFhirCondition(d, subject))
                .toList();
    }

    /**
     * Not exposed as its own GET endpoint (only POST /MedicationRequest was
     * requested) - used internally to assemble the ABDM health-record FHIR Bundle.
     */
    public List<FhirMedicationRequest> getMedicationRequests(String patientId) {
        PatientResponse patient = patientService.getByUpid(patientId);
        FhirReference subject = FhirReference.toPatient(patient.upid(), patient.fullName());
        return prescriptionService.getByPatientId(patientId).stream()
                .map(rx -> fhirMapper.toFhirMedicationRequest(rx, subject))
                .toList();
    }

    public FhirMedicationRequest createMedicationRequest(MedicationRequestSubmission submission) {
        PrescriptionItemRequest item = new PrescriptionItemRequest(
                submission.drugId(), submission.dosage(), submission.frequency(), submission.route(),
                submission.durationDays(), submission.foodInstruction(), null, null);

        PrescriptionRequest request = new PrescriptionRequest(
                submission.patientId(), submission.visitId(), submission.admissionId(),
                submission.requester(), submission.digitalSignature(), null, List.of(item));

        PrescriptionResponse created = prescriptionService.create(request);
        PatientResponse patient = patientService.getByUpid(submission.patientId());
        FhirReference subject = FhirReference.toPatient(patient.upid(), patient.fullName());
        return fhirMapper.toFhirMedicationRequest(created, subject);
    }

    public List<FhirEncounter> getEncounters(String patientId) {
        PatientResponse patient = patientService.getByUpid(patientId);
        FhirReference subject = FhirReference.toPatient(patient.upid(), patient.fullName());
        List<FhirEncounter> encounters = new ArrayList<>();

        Map<Long, OpdVitalsResponse> latestByVisit = new LinkedHashMap<>();
        for (OpdVitalsResponse v : opdVitalsService.getByPatientId(patientId)) {
            latestByVisit.putIfAbsent(v.visitId(), v);
        }
        latestByVisit.forEach((visitId, vitals) -> encounters.add(fhirMapper.toOpdEncounter(
                visitId, vitals.recordedAt() == null ? null : vitals.recordedAt().toString(), subject)));

        for (IpdAdmissionResponse admission : ipdAdmissionService.getByPatientId(patientId)) {
            encounters.add(fhirMapper.toIpdEncounter(admission, subject));
        }
        return encounters;
    }

    public FhirDocumentReference createDocumentReference(DocumentReferenceRequest request) {
        if (request.sourceType() == DocumentSourceType.DISCHARGE_SUMMARY) {
            DischargeSummaryResponse summary = dischargeSummaryService.getById(request.sourceId());
            PatientResponse patient = patientService.getByUpid(summary.patientId());
            FhirReference subject = FhirReference.toPatient(patient.upid(), patient.fullName());
            return fhirMapper.toDischargeSummaryDocument(summary, subject);
        }
        if (request.sourceType() == DocumentSourceType.LAB_REPORT) {
            var report = investigationReportService.getById(request.sourceId());
            PatientResponse patient = patientService.getByUpid(report.patientId());
            FhirReference subject = FhirReference.toPatient(patient.upid(), patient.fullName());
            return new com.hms.fhir.model.FhirDocumentReference(
                    "lab-report-doc-" + report.id(), "current",
                    com.hms.fhir.model.FhirCodeableConcept.ofText("Laboratory Report"),
                    subject, report.reportDate() == null ? null : report.reportDate().toString(),
                    "Lab report: " + report.investigationName(),
                    List.of(new com.hms.fhir.model.FhirDocumentContent("application/pdf", report.investigationName())));
        }
        if (request.sourceType() == DocumentSourceType.CONSENT_FORM) {
            IpdAdmissionResponse admission = ipdAdmissionService.getById(request.sourceId());
            if (!admission.hasConsentDocument()) {
                throw new ResourceNotFoundException("No consent document on file for admission id: " + request.sourceId());
            }
            PatientResponse patient = patientService.getByUpid(admission.patientId());
            FhirReference subject = FhirReference.toPatient(patient.upid(), patient.fullName());
            return new com.hms.fhir.model.FhirDocumentReference(
                    "consent-doc-" + admission.id(), "current",
                    com.hms.fhir.model.FhirCodeableConcept.ofText("Consent Form"),
                    subject, admission.admissionDateTime() == null ? null : admission.admissionDateTime().toString(),
                    "Admission consent for admission " + admission.id(),
                    List.of(new com.hms.fhir.model.FhirDocumentContent("application/pdf", "Consent Form")));
        }
        throw new IllegalArgumentException("Unsupported document source type: " + request.sourceType());
    }
}
