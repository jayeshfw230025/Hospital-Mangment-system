package com.hms.clinical.examination.service;

import com.hms.clinical.examination.ClinicalExamination;
import com.hms.clinical.examination.ClinicalExaminationMapper;
import com.hms.clinical.examination.ClinicalExaminationRepository;
import com.hms.clinical.examination.ExaminationContext;
import com.hms.clinical.examination.dto.ClinicalExaminationRequest;
import com.hms.clinical.examination.dto.ClinicalExaminationResponse;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClinicalExaminationService {

    private final ClinicalExaminationRepository clinicalExaminationRepository;
    private final PatientRepository patientRepository;
    private final ClinicalExaminationMapper mapper;

    public ClinicalExaminationService(ClinicalExaminationRepository clinicalExaminationRepository,
                                       PatientRepository patientRepository,
                                       ClinicalExaminationMapper mapper) {
        this.clinicalExaminationRepository = clinicalExaminationRepository;
        this.patientRepository = patientRepository;
        this.mapper = mapper;
    }

    @Transactional
    public ClinicalExaminationResponse createOpd(ClinicalExaminationRequest request) {
        if (request.visitId() == null) {
            throw new IllegalArgumentException("Visit ID is required for an OPD clinical examination");
        }
        requirePatientExists(request.patientId());

        ClinicalExamination exam = new ClinicalExamination();
        exam.setExaminationContext(ExaminationContext.OPD);
        exam.setVisitId(request.visitId());
        mapper.applyRequest(exam, request);

        return mapper.toResponse(clinicalExaminationRepository.save(exam));
    }

    @Transactional
    public ClinicalExaminationResponse createIpd(ClinicalExaminationRequest request) {
        if (request.admissionId() == null) {
            throw new IllegalArgumentException("Admission ID is required for an IPD clinical examination");
        }
        requirePatientExists(request.patientId());

        ClinicalExamination exam = new ClinicalExamination();
        exam.setExaminationContext(ExaminationContext.IPD);
        exam.setAdmissionId(request.admissionId());
        mapper.applyRequest(exam, request);

        return mapper.toResponse(clinicalExaminationRepository.save(exam));
    }

    @Transactional
    public ClinicalExaminationResponse update(Long id, ClinicalExaminationRequest request) {
        requirePatientExists(request.patientId());

        ClinicalExamination exam = clinicalExaminationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinical examination not found with id: " + id));

        if (exam.getExaminationContext() == ExaminationContext.OPD && request.visitId() != null) {
            exam.setVisitId(request.visitId());
        }
        if (exam.getExaminationContext() == ExaminationContext.IPD && request.admissionId() != null) {
            exam.setAdmissionId(request.admissionId());
        }

        mapper.applyRequest(exam, request);

        return mapper.toResponse(clinicalExaminationRepository.save(exam));
    }

    @Transactional(readOnly = true)
    public List<ClinicalExaminationResponse> getByVisitId(Long visitId) {
        return clinicalExaminationRepository.findByVisitIdOrderByCreatedAtAsc(visitId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClinicalExaminationResponse> getByAdmissionId(Long admissionId) {
        return clinicalExaminationRepository.findByAdmissionIdOrderByCreatedAtAsc(admissionId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void requirePatientExists(String patientUpid) {
        if (patientRepository.findByUpid(patientUpid).isEmpty()) {
            throw new ResourceNotFoundException("Patient not found with UPID: " + patientUpid);
        }
    }
}
