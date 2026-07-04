package com.hms.fhir.mapper;

import com.hms.diagnosis.dto.DiagnosisResponse;
import com.hms.discharge.dto.DischargeSummaryResponse;
import com.hms.fhir.model.FhirCodeableConcept;
import com.hms.fhir.model.FhirCoding;
import com.hms.fhir.model.FhirCondition;
import com.hms.fhir.model.FhirContactPoint;
import com.hms.fhir.model.FhirDocumentContent;
import com.hms.fhir.model.FhirDocumentReference;
import com.hms.fhir.model.FhirDosage;
import com.hms.fhir.model.FhirEncounter;
import com.hms.fhir.model.FhirHumanName;
import com.hms.fhir.model.FhirIdentifier;
import com.hms.fhir.model.FhirMedicationRequest;
import com.hms.fhir.model.FhirObservation;
import com.hms.fhir.model.FhirObservationComponent;
import com.hms.fhir.model.FhirPatient;
import com.hms.fhir.model.FhirPeriod;
import com.hms.fhir.model.FhirProcedure;
import com.hms.fhir.model.FhirQuantity;
import com.hms.fhir.model.FhirReference;
import com.hms.investigation.dto.InvestigationReportResponse;
import com.hms.investigation.dto.ResultParameterResponse;
import com.hms.ipd.admission.dto.IpdAdmissionResponse;
import com.hms.ipd.procedure.dto.ProcedureComplicationResponse;
import com.hms.ipd.procedure.dto.ProcedureResponse;
import com.hms.patient.dto.PatientResponse;
import com.hms.prescription.dto.PrescriptionItemResponse;
import com.hms.prescription.dto.PrescriptionResponse;
import com.hms.vitals.dto.IpdVitalsResponse;
import com.hms.vitals.dto.OpdVitalsResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-rolled FHIR R4-shaped resource mapping. Deliberately does NOT depend on
 * the official HAPI FHIR object model / validation library (ca.uhn.hapi.fhir) -
 * that pulls in a large terminology/validation stack meant for running an actual
 * FHIR server, which is out of scope here the same way Kafka/Redis/Elasticsearch/
 * MinIO/a real WhatsApp gateway are: real target infrastructure, deferred.
 * These records follow the R4 field names and resource shape closely (resourceType,
 * identifier, code.coding, valueQuantity, subject reference, etc.) so swapping in
 * the real HAPI FHIR library later is a mechanical replacement of this mapper, not
 * a redesign of the API surface.
 */
@Component
public class FhirMapper {

    private static final String SYSTEM_UPID = "https://hms.local/fhir/identifier/upid";
    private static final String SYSTEM_ABHA = "https://healthid.ndhm.gov.in";
    private static final String SYSTEM_ICD10 = "http://hl7.org/fhir/sid/icd-10";
    private static final String SYSTEM_OBSERVATION_CATEGORY = "http://terminology.hl7.org/CodeSystem/observation-category";
    private static final String SYSTEM_ENCOUNTER_CLASS = "http://terminology.hl7.org/CodeSystem/v3-ActCode";

    public FhirPatient toFhirPatient(PatientResponse patient) {
        List<FhirIdentifier> identifiers = new ArrayList<>();
        identifiers.add(new FhirIdentifier(SYSTEM_UPID, patient.upid()));
        if (patient.abhaNumber() != null && !patient.abhaNumber().isBlank()) {
            identifiers.add(new FhirIdentifier(SYSTEM_ABHA, patient.abhaNumber()));
        }

        List<FhirContactPoint> telecom = new ArrayList<>();
        if (patient.primaryContactNumber() != null) {
            telecom.add(new FhirContactPoint("phone", patient.primaryContactNumber(), "mobile"));
        }
        if (patient.email() != null && !patient.email().isBlank()) {
            telecom.add(new FhirContactPoint("email", patient.email(), "home"));
        }

        return new FhirPatient(
                patient.upid(),
                identifiers,
                List.of(new FhirHumanName(patient.fullName())),
                mapGender(patient.gender() == null ? null : patient.gender().name()),
                patient.dateOfBirth() == null ? null : patient.dateOfBirth().toString(),
                telecom);
    }

    private String mapGender(String gender) {
        if (gender == null) {
            return "unknown";
        }
        return switch (gender) {
            case "MALE" -> "male";
            case "FEMALE" -> "female";
            default -> "other";
        };
    }

    public FhirObservation toVitalsObservation(OpdVitalsResponse vitals, FhirReference subject) {
        List<FhirObservationComponent> components = new ArrayList<>();
        addQuantityComponent(components, "8480-6", "Systolic blood pressure", vitals.systolicBp(), "mmHg");
        addQuantityComponent(components, "8462-4", "Diastolic blood pressure", vitals.diastolicBp(), "mmHg");
        addQuantityComponent(components, "8867-4", "Heart rate", vitals.heartRate(), "bpm");
        addQuantityComponent(components, "9279-1", "Respiratory rate", vitals.respiratoryRate(), "breaths/min");
        addQuantityComponent(components, "8310-5", "Body temperature", vitals.temperatureCelsius(), "Cel");
        addQuantityComponent(components, "39156-5", "BMI", vitals.bmi(), "kg/m2");
        addQuantityComponent(components, "2708-6", "Oxygen saturation", vitals.spo2(), "%");

        return new FhirObservation(
                "opd-vitals-" + vitals.id(),
                "final",
                FhirCodeableConcept.ofCode(SYSTEM_OBSERVATION_CATEGORY, "vital-signs", "Vital Signs"),
                FhirCodeableConcept.ofText("Vital signs panel"),
                subject,
                vitals.recordedAt() == null ? null : vitals.recordedAt().toString(),
                components);
    }

    public FhirObservation toVitalsObservation(IpdVitalsResponse vitals, FhirReference subject) {
        List<FhirObservationComponent> components = new ArrayList<>();
        addQuantityComponent(components, "8480-6", "Systolic blood pressure", vitals.systolicBp(), "mmHg");
        addQuantityComponent(components, "8462-4", "Diastolic blood pressure", vitals.diastolicBp(), "mmHg");
        addQuantityComponent(components, "8867-4", "Heart rate", vitals.heartRate(), "bpm");
        addQuantityComponent(components, "9279-1", "Respiratory rate", vitals.respiratoryRate(), "breaths/min");
        addQuantityComponent(components, "8310-5", "Body temperature", vitals.temperatureCelsius(), "Cel");
        addQuantityComponent(components, "39156-5", "BMI", vitals.bmi(), "kg/m2");
        addQuantityComponent(components, "2708-6", "Oxygen saturation", vitals.spo2(), "%");
        addQuantityComponent(components, "9269-2", "Glasgow coma score total", vitals.gcsScore(), "score");

        return new FhirObservation(
                "ipd-vitals-" + vitals.id(),
                "final",
                FhirCodeableConcept.ofCode(SYSTEM_OBSERVATION_CATEGORY, "vital-signs", "Vital Signs"),
                FhirCodeableConcept.ofText("Vital signs panel"),
                subject,
                vitals.recordedAt() == null ? null : vitals.recordedAt().toString(),
                components);
    }

    public FhirObservation toLabObservation(InvestigationReportResponse report, FhirReference subject) {
        List<FhirObservationComponent> components = new ArrayList<>();
        for (ResultParameterResponse param : report.resultParameters()) {
            FhirCodeableConcept code = FhirCodeableConcept.ofText(param.parameterName());
            Double numeric = tryParseDouble(param.value());
            if (numeric != null) {
                components.add(new FhirObservationComponent(code, new FhirQuantity(numeric, param.unit()), null));
            } else {
                components.add(new FhirObservationComponent(code, null, param.value()));
            }
        }

        return new FhirObservation(
                "lab-report-" + report.id(),
                "final",
                FhirCodeableConcept.ofCode(SYSTEM_OBSERVATION_CATEGORY, "laboratory", "Laboratory"),
                FhirCodeableConcept.ofText(report.investigationName() == null
                        ? report.investigationTypeCode() : report.investigationName()),
                subject,
                report.reportDate() == null ? null : report.reportDate().toString(),
                components);
    }

    private void addQuantityComponent(List<FhirObservationComponent> components, String loincCode, String display,
                                       Number value, String unit) {
        if (value == null) {
            return;
        }
        components.add(new FhirObservationComponent(
                FhirCodeableConcept.ofCode("http://loinc.org", loincCode, display),
                new FhirQuantity(value.doubleValue(), unit),
                null));
    }

    private Double tryParseDouble(String value) {
        try {
            return value == null ? null : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public FhirCondition toFhirCondition(DiagnosisResponse diagnosis, FhirReference subject) {
        String clinicalStatus = diagnosis.status() == null ? "unknown" : diagnosis.status().name().toLowerCase();
        return new FhirCondition(
                "diagnosis-" + diagnosis.id(),
                FhirCodeableConcept.ofText(clinicalStatus),
                FhirCodeableConcept.ofCode(SYSTEM_ICD10, diagnosis.icd10Code(), diagnosis.icd10Description()),
                subject,
                diagnosis.diagnosedDate() == null ? null : diagnosis.diagnosedDate().toString(),
                diagnosis.notes());
    }

    public FhirMedicationRequest toFhirMedicationRequest(PrescriptionResponse prescription, FhirReference subject) {
        PrescriptionItemResponse firstItem = prescription.items().isEmpty() ? null : prescription.items().get(0);
        FhirCodeableConcept medication = firstItem == null
                ? FhirCodeableConcept.ofText("Unspecified")
                : FhirCodeableConcept.ofText(firstItem.genericName() != null ? firstItem.genericName() : firstItem.brandName());

        List<FhirDosage> dosageInstructions = prescription.items().stream()
                .map(item -> new FhirDosage(
                        item.generatedInstructions() != null ? item.generatedInstructions()
                                : (item.dosage() + " " + item.frequency()),
                        item.route()))
                .toList();

        return new FhirMedicationRequest(
                "prescription-" + prescription.id(),
                "active",
                "order",
                medication,
                subject,
                prescription.doctorName(),
                dosageInstructions,
                prescription.prescribedDate() == null ? null : prescription.prescribedDate().toString());
    }

    public FhirEncounter toOpdEncounter(Long visitId, String recordedAt, FhirReference subject) {
        return new FhirEncounter(
                "opd-visit-" + visitId,
                "finished",
                new FhirCoding(SYSTEM_ENCOUNTER_CLASS, "AMB", "ambulatory"),
                subject,
                new FhirPeriod(recordedAt, recordedAt),
                List.of());
    }

    public FhirEncounter toIpdEncounter(IpdAdmissionResponse admission, FhirReference subject) {
        List<FhirCodeableConcept> reasons = new ArrayList<>();
        if (admission.primaryDiagnosisIcd10() != null) {
            reasons.add(FhirCodeableConcept.ofCode(SYSTEM_ICD10, admission.primaryDiagnosisIcd10(),
                    admission.primaryDiagnosisDescription()));
        }
        return new FhirEncounter(
                "ipd-admission-" + admission.id(),
                "in-progress",
                new FhirCoding(SYSTEM_ENCOUNTER_CLASS, "IMP", "inpatient encounter"),
                subject,
                new FhirPeriod(admission.admissionDateTime() == null ? null : admission.admissionDateTime().toString(), null),
                reasons);
    }

    public FhirProcedure toFhirProcedure(ProcedureResponse procedure, FhirReference subject) {
        List<FhirCodeableConcept> complications = procedure.complications() == null ? List.of()
                : procedure.complications().stream()
                        .map(ProcedureComplicationResponse::complicationDescription)
                        .map(FhirCodeableConcept::ofText)
                        .toList();

        return new FhirProcedure(
                "procedure-" + procedure.id(),
                "completed",
                FhirCodeableConcept.ofText(procedure.procedureTypeLabel()),
                subject,
                procedure.procedureDate() == null ? null : procedure.procedureDate().toString(),
                complications,
                procedure.notes());
    }

    public FhirDocumentReference toDischargeSummaryDocument(DischargeSummaryResponse summary, FhirReference subject) {
        return new FhirDocumentReference(
                "discharge-summary-" + summary.id(),
                "current",
                FhirCodeableConcept.ofText("Discharge Summary"),
                subject,
                summary.dischargeDateTime() == null ? null : summary.dischargeDateTime().toString(),
                "Discharge summary for admission " + summary.admissionId(),
                List.of(new FhirDocumentContent("application/pdf", "Discharge Summary")));
    }
}
