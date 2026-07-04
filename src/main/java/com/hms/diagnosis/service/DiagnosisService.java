package com.hms.diagnosis.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.diagnosis.domain.Diagnosis;
import com.hms.diagnosis.domain.DiagnosisStatus;
import com.hms.diagnosis.domain.Icd10Code;
import com.hms.diagnosis.dto.DiagnosisRequest;
import com.hms.diagnosis.dto.DiagnosisResponse;
import com.hms.diagnosis.repository.DiagnosisRepository;
import com.hms.diagnosis.repository.Icd10CodeRepository;
import com.hms.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final Icd10CodeRepository icd10CodeRepository;
    private final PatientRepository patientRepository;

    public DiagnosisService(DiagnosisRepository diagnosisRepository,
                             Icd10CodeRepository icd10CodeRepository,
                             PatientRepository patientRepository) {
        this.diagnosisRepository = diagnosisRepository;
        this.icd10CodeRepository = icd10CodeRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public DiagnosisResponse create(DiagnosisRequest request) {
        requirePatientExists(request.patientId());
        Icd10Code icd10Code = requireActiveIcd10Code(request.icd10Code());

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setPatientUpid(request.patientId());
        diagnosis.setIcd10Code(icd10Code.getCode());
        diagnosis.setDiagnosisType(request.diagnosisType());
        diagnosis.setStatus(request.status() == null ? DiagnosisStatus.ACTIVE : request.status());
        diagnosis.setDiagnosedDate(request.diagnosedDate() == null ? LocalDate.now() : request.diagnosedDate());
        diagnosis.setNotes(request.notes());

        return toResponse(diagnosisRepository.save(diagnosis), icd10Code);
    }

    @Transactional
    public DiagnosisResponse update(Long id, DiagnosisRequest request) {
        requirePatientExists(request.patientId());
        Icd10Code icd10Code = requireActiveIcd10Code(request.icd10Code());

        Diagnosis diagnosis = diagnosisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis not found with id: " + id));

        diagnosis.setPatientUpid(request.patientId());
        diagnosis.setIcd10Code(icd10Code.getCode());
        diagnosis.setDiagnosisType(request.diagnosisType());
        diagnosis.setStatus(request.status() == null ? diagnosis.getStatus() : request.status());
        diagnosis.setDiagnosedDate(request.diagnosedDate() == null ? diagnosis.getDiagnosedDate() : request.diagnosedDate());
        diagnosis.setNotes(request.notes());

        return toResponse(diagnosisRepository.save(diagnosis), icd10Code);
    }

    @Transactional(readOnly = true)
    public List<DiagnosisResponse> getByPatientId(String patientId) {
        return diagnosisRepository.findByPatientUpidOrderByCreatedAtDesc(patientId).stream()
                .map(diagnosis -> toResponse(diagnosis, icd10CodeRepository.findById(diagnosis.getIcd10Code()).orElse(null)))
                .toList();
    }

    private DiagnosisResponse toResponse(Diagnosis diagnosis, Icd10Code icd10Code) {
        return new DiagnosisResponse(
                diagnosis.getId(),
                diagnosis.getPatientUpid(),
                diagnosis.getIcd10Code(),
                icd10Code == null ? null : icd10Code.getDescription(),
                icd10Code == null ? null : icd10Code.getCategory(),
                diagnosis.getDiagnosisType(),
                diagnosis.getStatus(),
                diagnosis.getDiagnosedDate(),
                diagnosis.getNotes(),
                diagnosis.getCreatedAt()
        );
    }

    private void requirePatientExists(String patientUpid) {
        if (patientRepository.findByUpid(patientUpid).isEmpty()) {
            throw new ResourceNotFoundException("Patient not found with UPID: " + patientUpid);
        }
    }

    private Icd10Code requireActiveIcd10Code(String code) {
        Icd10Code icd10Code = icd10CodeRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("ICD-10 code not found: " + code));
        if (!icd10Code.isActive()) {
            throw new IllegalArgumentException("ICD-10 code " + code + " is inactive and cannot be assigned");
        }
        return icd10Code;
    }
}
