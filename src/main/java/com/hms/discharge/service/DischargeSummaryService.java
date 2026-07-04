package com.hms.discharge.service;

import com.hms.common.exception.DuplicateResourceException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.diagnosis.domain.Icd10Code;
import com.hms.diagnosis.repository.Icd10CodeRepository;
import com.hms.discharge.domain.DischargeMedicationItem;
import com.hms.discharge.domain.DischargeSummary;
import com.hms.discharge.dto.DischargeMedicationItemDto;
import com.hms.discharge.dto.DischargeSummaryRequest;
import com.hms.discharge.dto.DischargeSummaryResponse;
import com.hms.discharge.repository.DischargeSummaryRepository;
import com.hms.ipd.admission.domain.IpdAdmission;
import com.hms.ipd.admission.repository.IpdAdmissionRepository;
import com.hms.ipd.procedure.domain.Procedure;
import com.hms.ipd.procedure.repository.ProcedureComplicationRepository;
import com.hms.ipd.procedure.repository.ProcedureRepository;
import com.hms.nutrition.domain.NutritionAssessment;
import com.hms.nutrition.repository.NutritionAssessmentRepository;
import com.hms.patient.domain.Patient;
import com.hms.patient.repository.PatientRepository;
import com.hms.prescription.domain.Prescription;
import com.hms.prescription.domain.PrescriptionItem;
import com.hms.prescription.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DischargeSummaryService {

    private final DischargeSummaryRepository dischargeSummaryRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final PatientRepository patientRepository;
    private final Icd10CodeRepository icd10CodeRepository;
    private final ProcedureRepository procedureRepository;
    private final ProcedureComplicationRepository procedureComplicationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final NutritionAssessmentRepository nutritionAssessmentRepository;
    private final DietPlanGenerator dietPlanGenerator;

    public DischargeSummaryService(DischargeSummaryRepository dischargeSummaryRepository,
                                    IpdAdmissionRepository ipdAdmissionRepository,
                                    PatientRepository patientRepository,
                                    Icd10CodeRepository icd10CodeRepository,
                                    ProcedureRepository procedureRepository,
                                    ProcedureComplicationRepository procedureComplicationRepository,
                                    PrescriptionRepository prescriptionRepository,
                                    NutritionAssessmentRepository nutritionAssessmentRepository,
                                    DietPlanGenerator dietPlanGenerator) {
        this.dischargeSummaryRepository = dischargeSummaryRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.patientRepository = patientRepository;
        this.icd10CodeRepository = icd10CodeRepository;
        this.procedureRepository = procedureRepository;
        this.procedureComplicationRepository = procedureComplicationRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.nutritionAssessmentRepository = nutritionAssessmentRepository;
        this.dietPlanGenerator = dietPlanGenerator;
    }

    @Transactional
    public DischargeSummaryResponse create(DischargeSummaryRequest request) {
        IpdAdmission admission = requireAdmission(request.admissionId());
        if (dischargeSummaryRepository.existsByAdmissionId(request.admissionId())) {
            throw new DuplicateResourceException(
                    "A discharge summary already exists for admission " + request.admissionId() + "; use PUT to update it");
        }
        requireActiveIcd10Code(request.primaryDiagnosisIcd10());
        if (request.secondaryDiagnosisIcd10() != null && !request.secondaryDiagnosisIcd10().isBlank()) {
            requireActiveIcd10Code(request.secondaryDiagnosisIcd10());
        }

        DischargeSummary summary = new DischargeSummary();
        summary.setAdmissionId(request.admissionId());
        summary.setDischargeDateTime(Instant.now());
        applyRequest(summary, request, admission);

        return toResponse(dischargeSummaryRepository.save(summary), admission);
    }

    @Transactional
    public DischargeSummaryResponse update(Long id, DischargeSummaryRequest request) {
        DischargeSummary summary = dischargeSummaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discharge summary not found with id: " + id));

        IpdAdmission admission = requireAdmission(request.admissionId());
        requireActiveIcd10Code(request.primaryDiagnosisIcd10());
        if (request.secondaryDiagnosisIcd10() != null && !request.secondaryDiagnosisIcd10().isBlank()) {
            requireActiveIcd10Code(request.secondaryDiagnosisIcd10());
        }

        applyRequest(summary, request, admission);

        return toResponse(dischargeSummaryRepository.save(summary), admission);
    }

    @Transactional(readOnly = true)
    public DischargeSummaryResponse getByAdmissionId(Long admissionId) {
        DischargeSummary summary = dischargeSummaryRepository.findByAdmissionId(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Discharge summary not found for admission: " + admissionId));
        IpdAdmission admission = requireAdmission(admissionId);
        return toResponse(summary, admission);
    }

    @Transactional(readOnly = true)
    public DischargeSummary getEntityById(Long id) {
        return dischargeSummaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discharge summary not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public DischargeSummaryResponse getById(Long id) {
        DischargeSummary summary = getEntityById(id);
        IpdAdmission admission = requireAdmission(summary.getAdmissionId());
        return toResponse(summary, admission);
    }

    private void applyRequest(DischargeSummary summary, DischargeSummaryRequest request, IpdAdmission admission) {
        summary.setDischargeType(request.dischargeType());
        summary.setPrimaryDiagnosisIcd10(request.primaryDiagnosisIcd10());
        summary.setSecondaryDiagnosisIcd10(request.secondaryDiagnosisIcd10());
        summary.setDischargeDiagnosisText(request.dischargeDiagnosisText());
        summary.setSummaryOfHospitalStay(request.summaryOfHospitalStay());
        summary.setFollowUpDateTime(request.followUpDateTime());
        summary.setFollowUpInstructions(request.followUpInstructions());
        summary.setDischargeCondition(request.dischargeCondition());
        summary.setDischargedByDoctorName(request.dischargedByDoctorName());
        summary.setDischargedByDoctorSignature(request.dischargedByDoctorSignature());
        summary.setMedicalRecordsChecked(request.medicalRecordsChecked());
        summary.setDischargeInstructions(request.dischargeInstructions());

        summary.setLengthOfStayDays((int) ChronoUnit.DAYS.between(
                admission.getAdmissionDateTime(), summary.getDischargeDateTime()));

        summary.setSignificantProcedures(autoPopulateProcedures(admission.getId()));
        summary.setComplicationsDuringStay(autoPopulateComplications(admission.getId()));
        summary.setDischargeMedications(autoPopulateMedications(admission.getId()));
        summary.setDischargeDietPlan(request.dischargeDietPlanOverride() != null
                ? request.dischargeDietPlanOverride()
                : autoGenerateDietPlan(admission.getPatientUpid()));
    }

    private List<String> autoPopulateProcedures(Long admissionId) {
        List<String> result = new ArrayList<>();
        for (Procedure procedure : procedureRepository.findByAdmissionIdOrderByCreatedAtAsc(admissionId)) {
            result.add(procedure.getProcedureType().getLabel() + " on " + procedure.getProcedureDate());
        }
        return result;
    }

    private List<String> autoPopulateComplications(Long admissionId) {
        List<String> result = new ArrayList<>();
        for (Procedure procedure : procedureRepository.findByAdmissionIdOrderByCreatedAtAsc(admissionId)) {
            procedureComplicationRepository.findByProcedureIdOrderByCreatedAtAsc(procedure.getId())
                    .forEach(c -> result.add(procedure.getProcedureType().getLabel() + ": " + c.getComplicationDescription()));
        }
        return result;
    }

    private List<DischargeMedicationItem> autoPopulateMedications(Long admissionId) {
        List<DischargeMedicationItem> result = new ArrayList<>();
        for (Prescription prescription : prescriptionRepository.findByAdmissionId(admissionId)) {
            for (PrescriptionItem item : prescription.getItems()) {
                result.add(new DischargeMedicationItem(
                        item.getGenericName(), item.getDosage(), item.getFrequency(), item.getDurationDays()));
            }
        }
        return result;
    }

    private String autoGenerateDietPlan(String patientUpid) {
        List<NutritionAssessment> assessments = nutritionAssessmentRepository
                .findByPatientUpidOrderByCreatedAtDesc(patientUpid);
        if (assessments.isEmpty()) {
            return null;
        }
        NutritionAssessment latest = assessments.get(0);
        if (latest.getDiseaseCategory() == null || latest.getWeightKg() == null) {
            return null;
        }
        return dietPlanGenerator.generate(latest.getDiseaseCategory(), latest.getWeightKg());
    }

    private IpdAdmission requireAdmission(Long admissionId) {
        return ipdAdmissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with id: " + admissionId));
    }

    private void requireActiveIcd10Code(String code) {
        Icd10Code icd10Code = icd10CodeRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("ICD-10 code not found: " + code));
        if (!icd10Code.isActive()) {
            throw new IllegalArgumentException("ICD-10 code " + code + " is inactive");
        }
    }

    private DischargeSummaryResponse toResponse(DischargeSummary summary, IpdAdmission admission) {
        Patient patient = patientRepository.findByUpid(admission.getPatientUpid()).orElse(null);

        String primaryDesc = icd10CodeRepository.findById(summary.getPrimaryDiagnosisIcd10())
                .map(Icd10Code::getDescription).orElse(null);
        String secondaryDesc = summary.getSecondaryDiagnosisIcd10() == null ? null
                : icd10CodeRepository.findById(summary.getSecondaryDiagnosisIcd10())
                        .map(Icd10Code::getDescription).orElse(null);

        List<DischargeMedicationItemDto> medications = summary.getDischargeMedications().stream()
                .map(m -> new DischargeMedicationItemDto(m.getDrugName(), m.getDosage(), m.getFrequency(), m.getDurationDays()))
                .toList();

        return new DischargeSummaryResponse(
                summary.getId(),
                summary.getAdmissionId(),
                admission.getPatientUpid(),
                patient == null ? null : patient.getFullName(),
                patient == null ? null : patient.getDateOfBirth(),
                patient == null ? null : patient.getGender(),
                admission.getAdmissionDateTime(),
                summary.getDischargeDateTime(),
                summary.getLengthOfStayDays(),
                summary.getDischargeType(),
                summary.getPrimaryDiagnosisIcd10(),
                primaryDesc,
                summary.getSecondaryDiagnosisIcd10(),
                secondaryDesc,
                summary.getDischargeDiagnosisText(),
                summary.getSummaryOfHospitalStay(),
                new ArrayList<>(summary.getSignificantProcedures()),
                new ArrayList<>(summary.getComplicationsDuringStay()),
                medications,
                summary.getDischargeDietPlan(),
                summary.getFollowUpDateTime(),
                summary.getFollowUpInstructions(),
                summary.getDischargeCondition(),
                summary.getDischargedByDoctorName(),
                summary.getMedicalRecordsChecked(),
                summary.getDischargeInstructions(),
                summary.getCreatedAt()
        );
    }
}
