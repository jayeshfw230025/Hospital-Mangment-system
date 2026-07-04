package com.hms.ipd.progressnote.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.diagnosis.domain.Icd10Code;
import com.hms.diagnosis.repository.Icd10CodeRepository;
import com.hms.ipd.admission.repository.IpdAdmissionRepository;
import com.hms.ipd.progressnote.domain.MedicationPlanItem;
import com.hms.ipd.progressnote.domain.ProgressNote;
import com.hms.ipd.progressnote.dto.MedicationPlanItemDto;
import com.hms.ipd.progressnote.dto.ProgressNoteRequest;
import com.hms.ipd.progressnote.dto.ProgressNoteResponse;
import com.hms.ipd.progressnote.repository.ProgressNoteRepository;
import com.hms.vitals.domain.IpdVitals;
import com.hms.vitals.dto.IpdVitalsResponse;
import com.hms.vitals.repository.IpdVitalsRepository;
import com.hms.vitals.service.IpdVitalsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgressNoteService {

    private final ProgressNoteRepository progressNoteRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final IpdVitalsRepository ipdVitalsRepository;
    private final IpdVitalsService ipdVitalsService;
    private final Icd10CodeRepository icd10CodeRepository;

    public ProgressNoteService(ProgressNoteRepository progressNoteRepository,
                                IpdAdmissionRepository ipdAdmissionRepository,
                                IpdVitalsRepository ipdVitalsRepository,
                                IpdVitalsService ipdVitalsService,
                                Icd10CodeRepository icd10CodeRepository) {
        this.progressNoteRepository = progressNoteRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.ipdVitalsRepository = ipdVitalsRepository;
        this.ipdVitalsService = ipdVitalsService;
        this.icd10CodeRepository = icd10CodeRepository;
    }

    @Transactional
    public ProgressNoteResponse create(ProgressNoteRequest request) {
        requireAdmissionExists(request.admissionId());
        validateIcd10Code(request.icd10Code());

        ProgressNote note = new ProgressNote();
        note.setAdmissionId(request.admissionId());
        note.setNoteDate(LocalDate.now());
        applyRequest(note, request);

        List<IpdVitals> vitals = ipdVitalsRepository.findByAdmissionIdOrderByCreatedAtAsc(request.admissionId());
        if (!vitals.isEmpty()) {
            note.setIpdVitalsId(vitals.get(vitals.size() - 1).getId());
        }

        return toResponse(progressNoteRepository.save(note));
    }

    @Transactional
    public ProgressNoteResponse update(Long id, ProgressNoteRequest request) {
        ProgressNote note = progressNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Progress note not found with id: " + id));

        requireAdmissionExists(request.admissionId());
        validateIcd10Code(request.icd10Code());

        note.setAdmissionId(request.admissionId());
        applyRequest(note, request);

        return toResponse(progressNoteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public List<ProgressNoteResponse> getByAdmissionId(Long admissionId) {
        return progressNoteRepository.findByAdmissionIdOrderByCreatedAtAsc(admissionId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void applyRequest(ProgressNote note, ProgressNoteRequest request) {
        note.setChiefComplaintToday(request.chiefComplaintToday());
        note.setPainScore(request.painScore());
        note.setNauseaVomiting(request.nauseaVomiting());
        note.setAppetite(request.appetite());
        note.setBowelMovementFrequency(request.bowelMovementFrequency());
        note.setBowelMovementCharacter(request.bowelMovementCharacter());
        note.setSleepPattern(request.sleepPattern());
        note.setGeneralWellBeing(request.generalWellBeing());

        note.setGeneralAppearance(request.generalAppearance());
        note.setAbdominalExaminationFindings(request.abdominalExaminationFindings());
        note.setNewFindings(request.newFindings());

        note.setClinicalImpression(request.clinicalImpression());
        note.setCurrentDiagnosis(request.currentDiagnosis());
        note.setIcd10Code(request.icd10Code());
        note.setSeverityAssessment(request.severityAssessment());
        note.setComplicationFlags(nullToEmpty(request.complicationFlags()));

        note.setMedicationPlanItems(nullToEmpty(request.medicationPlanItems()).stream()
                .map(m -> new MedicationPlanItem(m.drugName(), m.planStatus(), m.notes()))
                .collect(Collectors.toCollection(ArrayList::new)));
        note.setInvestigationsOrdered(nullToEmpty(request.investigationsOrdered()));
        note.setConsultationsRequired(nullToEmpty(request.consultationsRequired()));
        note.setDietPlan(request.dietPlan());
        note.setActivityLevel(request.activityLevel());
        note.setDischargePlanningNotes(request.dischargePlanningNotes());
    }

    private void requireAdmissionExists(Long admissionId) {
        if (ipdAdmissionRepository.findById(admissionId).isEmpty()) {
            throw new ResourceNotFoundException("Admission not found with id: " + admissionId);
        }
    }

    private void validateIcd10Code(String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        Icd10Code icd10Code = icd10CodeRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("ICD-10 code not found: " + code));
        if (!icd10Code.isActive()) {
            throw new IllegalArgumentException("ICD-10 code " + code + " is inactive");
        }
    }

    private ProgressNoteResponse toResponse(ProgressNote note) {
        IpdVitalsResponse vitalsSnapshot = note.getIpdVitalsId() == null ? null
                : ipdVitalsService.getById(note.getIpdVitalsId());

        String icd10Description = note.getIcd10Code() == null ? null
                : icd10CodeRepository.findById(note.getIcd10Code()).map(Icd10Code::getDescription).orElse(null);

        List<MedicationPlanItemDto> medicationPlan = note.getMedicationPlanItems().stream()
                .map(m -> new MedicationPlanItemDto(m.getDrugName(), m.getPlanStatus(), m.getNotes()))
                .toList();

        return new ProgressNoteResponse(
                note.getId(),
                note.getAdmissionId(),
                note.getNoteDate(),
                note.getChiefComplaintToday(),
                note.getPainScore(),
                note.getNauseaVomiting(),
                note.getAppetite(),
                note.getBowelMovementFrequency(),
                note.getBowelMovementCharacter(),
                note.getSleepPattern(),
                note.getGeneralWellBeing(),
                vitalsSnapshot,
                note.getGeneralAppearance(),
                note.getAbdominalExaminationFindings(),
                note.getNewFindings(),
                note.getClinicalImpression(),
                note.getCurrentDiagnosis(),
                note.getIcd10Code(),
                icd10Description,
                note.getSeverityAssessment(),
                new ArrayList<>(note.getComplicationFlags()),
                medicationPlan,
                new ArrayList<>(note.getInvestigationsOrdered()),
                new ArrayList<>(note.getConsultationsRequired()),
                note.getDietPlan(),
                note.getActivityLevel(),
                note.getDischargePlanningNotes(),
                note.getCreatedAt()
        );
    }

    private <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }
}
