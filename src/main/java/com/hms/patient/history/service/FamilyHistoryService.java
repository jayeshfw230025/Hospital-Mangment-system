package com.hms.patient.history.service;

import com.hms.common.exception.DuplicateResourceException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.history.domain.FamilyHistory;
import com.hms.patient.history.dto.FamilyHistoryRequest;
import com.hms.patient.history.dto.FamilyHistoryResponse;
import com.hms.patient.history.repository.FamilyHistoryRepository;
import com.hms.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyHistoryService {

    private final FamilyHistoryRepository familyHistoryRepository;
    private final PatientRepository patientRepository;

    public FamilyHistoryService(FamilyHistoryRepository familyHistoryRepository, PatientRepository patientRepository) {
        this.familyHistoryRepository = familyHistoryRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public FamilyHistoryResponse create(FamilyHistoryRequest request) {
        requirePatientExists(request.patientId());

        if (familyHistoryRepository.existsByPatientUpid(request.patientId())) {
            throw new DuplicateResourceException(
                    "Family history already exists for patient " + request.patientId() + "; use PUT to update it");
        }

        FamilyHistory history = new FamilyHistory();
        applyRequest(history, request);

        return toResponse(familyHistoryRepository.save(history));
    }

    @Transactional
    public FamilyHistoryResponse update(Long id, FamilyHistoryRequest request) {
        requirePatientExists(request.patientId());

        FamilyHistory history = familyHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Family history not found with id: " + id));

        applyRequest(history, request);

        return toResponse(familyHistoryRepository.save(history));
    }

    @Transactional(readOnly = true)
    public FamilyHistoryResponse getByPatientId(String patientId) {
        FamilyHistory history = familyHistoryRepository.findByPatientUpid(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Family history not found for patient: " + patientId));
        return toResponse(history);
    }

    private void applyRequest(FamilyHistory history, FamilyHistoryRequest request) {
        history.setPatientUpid(request.patientId());
        history.setPepticUlcerDisease(request.pepticUlcerDisease());
        history.setInflammatoryBowelDisease(request.inflammatoryBowelDisease());
        history.setGiMalignancy(request.giMalignancy());
        history.setGiMalignancyType(request.giMalignancyType());
        history.setDiabetesMellitus(request.diabetesMellitus());
        history.setHypertension(request.hypertension());
        history.setCoronaryArteryDisease(request.coronaryArteryDisease());
        history.setOthersDescription(request.othersDescription());
    }

    private FamilyHistoryResponse toResponse(FamilyHistory history) {
        return new FamilyHistoryResponse(
                history.getId(),
                history.getPatientUpid(),
                history.isPepticUlcerDisease(),
                history.isInflammatoryBowelDisease(),
                history.isGiMalignancy(),
                history.getGiMalignancyType(),
                history.isDiabetesMellitus(),
                history.isHypertension(),
                history.isCoronaryArteryDisease(),
                history.getOthersDescription(),
                history.getCreatedAt()
        );
    }

    private void requirePatientExists(String patientUpid) {
        if (!patientRepository.findByUpid(patientUpid).isPresent()) {
            throw new ResourceNotFoundException("Patient not found with UPID: " + patientUpid);
        }
    }
}
