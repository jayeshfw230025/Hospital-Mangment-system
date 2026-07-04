package com.hms.cds.service;

import com.hms.cds.domain.CdsAlert;
import com.hms.cds.domain.ClinicalSnapshot;
import com.hms.cds.dto.CdsAlertResponse;
import com.hms.cds.dto.CdsAssessRequest;
import com.hms.cds.repository.CdsAlertRepository;
import com.hms.cds.rules.CdsAlertRule;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class CdsAssessmentService {

    private final ClinicalSnapshotAggregator clinicalSnapshotAggregator;
    private final CdsAlertRepository cdsAlertRepository;
    private final PatientRepository patientRepository;

    public CdsAssessmentService(ClinicalSnapshotAggregator clinicalSnapshotAggregator,
                                 CdsAlertRepository cdsAlertRepository,
                                 PatientRepository patientRepository) {
        this.clinicalSnapshotAggregator = clinicalSnapshotAggregator;
        this.cdsAlertRepository = cdsAlertRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public List<CdsAlertResponse> assess(CdsAssessRequest request) {
        if (request.visitId() == null && request.admissionId() == null) {
            throw new IllegalArgumentException("Either visitId or admissionId must be provided");
        }
        if (patientRepository.findByUpid(request.patientId()).isEmpty()) {
            throw new ResourceNotFoundException("Patient not found with UPID: " + request.patientId());
        }

        ClinicalSnapshot snapshot = clinicalSnapshotAggregator.build(
                request.patientId(), request.visitId(), request.admissionId(), request.additionalFindings());

        return Arrays.stream(CdsAlertRule.values())
                .filter(rule -> rule.matches(snapshot))
                .map(rule -> persistAlert(request.patientId(), rule))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CdsAlertResponse> getByPatientId(String patientId) {
        return cdsAlertRepository.findByPatientUpidOrderByCreatedAtDesc(patientId).stream()
                .map(this::toResponse)
                .toList();
    }

    private CdsAlert persistAlert(String patientUpid, CdsAlertRule rule) {
        CdsAlert alert = new CdsAlert();
        alert.setPatientUpid(patientUpid);
        alert.setContext(rule.getContext());
        alert.setRuleName(rule.name());
        alert.setFinding(rule.getFinding());
        alert.setSuggestion(rule.getSuggestion());
        return cdsAlertRepository.save(alert);
    }

    private CdsAlertResponse toResponse(CdsAlert alert) {
        return new CdsAlertResponse(
                alert.getId(), alert.getPatientUpid(), alert.getContext(), alert.getRuleName(),
                alert.getFinding(), alert.getSuggestion(), alert.getCreatedAt());
    }
}
