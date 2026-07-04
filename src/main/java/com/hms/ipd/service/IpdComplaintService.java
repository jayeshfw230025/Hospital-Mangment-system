package com.hms.ipd.service;

import com.hms.clinical.complaint.ComplaintDetailValidator;
import com.hms.ipd.domain.IpdComplaint;
import com.hms.ipd.dto.IpdComplaintRequest;
import com.hms.ipd.dto.IpdComplaintResponse;
import com.hms.ipd.repository.IpdComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IpdComplaintService {

    private final IpdComplaintRepository ipdComplaintRepository;
    private final ComplaintDetailValidator complaintDetailValidator;

    public IpdComplaintService(IpdComplaintRepository ipdComplaintRepository,
                                ComplaintDetailValidator complaintDetailValidator) {
        this.ipdComplaintRepository = ipdComplaintRepository;
        this.complaintDetailValidator = complaintDetailValidator;
    }

    @Transactional
    public IpdComplaintResponse create(IpdComplaintRequest request) {
        complaintDetailValidator.validate(request.complaintType(), request.details());

        IpdComplaint complaint = new IpdComplaint();
        complaint.setAdmissionId(request.admissionId());
        complaint.setComplaintType(request.complaintType());
        complaint.setSeverity(request.severity());
        complaint.setSeverityScore(request.severityScore());
        complaint.setDurationValue(request.durationValue());
        complaint.setDurationUnit(request.durationUnit());
        complaint.setAssociatedVitalsImpact(request.associatedVitalsImpact());
        complaint.setResponseToInitialTreatment(request.responseToInitialTreatment());
        complaint.setNotes(request.notes());
        complaint.setDetails(request.details());

        return toResponse(ipdComplaintRepository.save(complaint));
    }

    @Transactional(readOnly = true)
    public List<IpdComplaintResponse> getByAdmissionId(Long admissionId) {
        return ipdComplaintRepository.findByAdmissionIdOrderByCreatedAtAsc(admissionId).stream()
                .map(this::toResponse)
                .toList();
    }

    private IpdComplaintResponse toResponse(IpdComplaint complaint) {
        return new IpdComplaintResponse(
                complaint.getId(),
                complaint.getAdmissionId(),
                complaint.getComplaintType(),
                complaint.getComplaintType().getLabel(),
                complaint.getSeverity(),
                complaint.getSeverityScore(),
                complaint.getDurationValue(),
                complaint.getDurationUnit(),
                complaint.getAssociatedVitalsImpact(),
                complaint.getResponseToInitialTreatment(),
                complaint.getNotes(),
                complaint.getDetails(),
                complaint.getCreatedAt()
        );
    }
}
