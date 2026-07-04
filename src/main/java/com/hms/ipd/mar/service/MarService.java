package com.hms.ipd.mar.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.admission.repository.IpdAdmissionRepository;
import com.hms.ipd.mar.domain.AdministrationStatus;
import com.hms.ipd.mar.domain.MedicationAdministration;
import com.hms.ipd.mar.dto.MarRequest;
import com.hms.ipd.mar.dto.MarResponse;
import com.hms.ipd.mar.repository.MedicationAdministrationRepository;
import com.hms.prescription.repository.DrugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarService {

    private final MedicationAdministrationRepository medicationAdministrationRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final DrugRepository drugRepository;

    public MarService(MedicationAdministrationRepository medicationAdministrationRepository,
                       IpdAdmissionRepository ipdAdmissionRepository,
                       DrugRepository drugRepository) {
        this.medicationAdministrationRepository = medicationAdministrationRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.drugRepository = drugRepository;
    }

    @Transactional
    public MarResponse create(MarRequest request) {
        if (ipdAdmissionRepository.findById(request.admissionId()).isEmpty()) {
            throw new ResourceNotFoundException("Admission not found with id: " + request.admissionId());
        }
        if (request.drugId() != null && drugRepository.findById(request.drugId()).isEmpty()) {
            throw new ResourceNotFoundException("Drug not found with id: " + request.drugId());
        }

        MedicationAdministration entry = new MedicationAdministration();
        entry.setAdmissionId(request.admissionId());
        entry.setDrugId(request.drugId());
        entry.setDrugName(request.drugName());
        entry.setDosage(request.dosage());
        entry.setRoute(request.route());
        entry.setScheduledTime(request.scheduledTime());
        entry.setAdministeredTime(request.administeredTime());
        entry.setAdministeredByName(request.administeredByName());
        entry.setStatus(request.status() == null ? AdministrationStatus.SCHEDULED : request.status());
        entry.setNotes(request.notes());

        return toResponse(medicationAdministrationRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public List<MarResponse> getByAdmissionId(Long admissionId) {
        return medicationAdministrationRepository.findByAdmissionIdOrderByScheduledTimeAsc(admissionId).stream()
                .map(this::toResponse)
                .toList();
    }

    private MarResponse toResponse(MedicationAdministration entry) {
        return new MarResponse(
                entry.getId(), entry.getAdmissionId(), entry.getDrugId(), entry.getDrugName(), entry.getDosage(),
                entry.getRoute(), entry.getScheduledTime(), entry.getAdministeredTime(), entry.getAdministeredByName(),
                entry.getStatus(), entry.getNotes(), entry.getCreatedAt());
    }
}
