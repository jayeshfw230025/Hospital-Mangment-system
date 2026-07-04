package com.hms.patient.history.service;

import com.hms.common.exception.DuplicateResourceException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.history.domain.PatientHistory;
import com.hms.patient.history.dto.LifestyleResponse;
import com.hms.patient.history.dto.PatientHistoryRequest;
import com.hms.patient.history.dto.PatientHistoryResponse;
import com.hms.patient.history.mapper.PatientHistoryMapper;
import com.hms.patient.history.repository.PatientHistoryRepository;
import com.hms.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientHistoryService {

    private final PatientHistoryRepository patientHistoryRepository;
    private final PatientRepository patientRepository;
    private final PatientHistoryMapper patientHistoryMapper;

    public PatientHistoryService(PatientHistoryRepository patientHistoryRepository,
                                  PatientRepository patientRepository,
                                  PatientHistoryMapper patientHistoryMapper) {
        this.patientHistoryRepository = patientHistoryRepository;
        this.patientRepository = patientRepository;
        this.patientHistoryMapper = patientHistoryMapper;
    }

    @Transactional
    public PatientHistoryResponse create(PatientHistoryRequest request) {
        requirePatientExists(request.patientId());

        if (patientHistoryRepository.existsByPatientUpid(request.patientId())) {
            throw new DuplicateResourceException(
                    "Medical history already exists for patient " + request.patientId() + "; use PUT to update it");
        }

        PatientHistory history = new PatientHistory();
        patientHistoryMapper.applyRequest(history, request);

        return patientHistoryMapper.toResponse(patientHistoryRepository.save(history));
    }

    @Transactional
    public PatientHistoryResponse update(Long id, PatientHistoryRequest request) {
        requirePatientExists(request.patientId());

        PatientHistory history = patientHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical history not found with id: " + id));

        patientHistoryMapper.applyRequest(history, request);

        return patientHistoryMapper.toResponse(patientHistoryRepository.save(history));
    }

    @Transactional(readOnly = true)
    public PatientHistoryResponse getByPatientId(String patientId) {
        return patientHistoryMapper.toResponse(findOrThrow(patientId));
    }

    @Transactional(readOnly = true)
    public LifestyleResponse getLifestyle(String patientId) {
        return patientHistoryMapper.toLifestyleResponse(findOrThrow(patientId));
    }

    private PatientHistory findOrThrow(String patientId) {
        return patientHistoryRepository.findByPatientUpid(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical history not found for patient: " + patientId));
    }

    private void requirePatientExists(String patientUpid) {
        if (!patientRepository.findByUpid(patientUpid).isPresent()) {
            throw new ResourceNotFoundException("Patient not found with UPID: " + patientUpid);
        }
    }
}
