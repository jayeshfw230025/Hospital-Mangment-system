package com.hms.diagnosis.service;

import com.hms.diagnosis.domain.Icd10Code;
import com.hms.diagnosis.dto.Icd10CodeResponse;
import com.hms.diagnosis.repository.Icd10CodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class Icd10CodeService {

    private final Icd10CodeRepository icd10CodeRepository;

    public Icd10CodeService(Icd10CodeRepository icd10CodeRepository) {
        this.icd10CodeRepository = icd10CodeRepository;
    }

    @Transactional(readOnly = true)
    public List<Icd10CodeResponse> search(String query) {
        return icd10CodeRepository
                .findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCodeAsc(query, query).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Icd10CodeResponse> getGastroCodes() {
        return icd10CodeRepository.findByActiveTrueOrderByCodeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private Icd10CodeResponse toResponse(Icd10Code code) {
        return new Icd10CodeResponse(code.getCode(), code.getDescription(), code.getCategory(), code.isActive());
    }
}
