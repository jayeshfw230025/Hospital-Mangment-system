package com.hms.opd.service;

import com.hms.clinical.complaint.ComplaintDetailValidator;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.opd.domain.OpdComplaint;
import com.hms.opd.dto.OpdComplaintRequest;
import com.hms.opd.dto.OpdComplaintResponse;
import com.hms.opd.repository.OpdComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OpdComplaintService {

    private final OpdComplaintRepository opdComplaintRepository;
    private final ComplaintDetailValidator complaintDetailValidator;

    public OpdComplaintService(OpdComplaintRepository opdComplaintRepository,
                                ComplaintDetailValidator complaintDetailValidator) {
        this.opdComplaintRepository = opdComplaintRepository;
        this.complaintDetailValidator = complaintDetailValidator;
    }

    @Transactional
    public OpdComplaintResponse create(OpdComplaintRequest request) {
        complaintDetailValidator.validate(request.complaintType(), request.details());

        OpdComplaint complaint = new OpdComplaint();
        applyRequest(complaint, request);

        return toResponse(opdComplaintRepository.save(complaint));
    }

    @Transactional
    public OpdComplaintResponse update(Long id, OpdComplaintRequest request) {
        complaintDetailValidator.validate(request.complaintType(), request.details());

        OpdComplaint complaint = opdComplaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chief complaint not found with id: " + id));

        applyRequest(complaint, request);

        return toResponse(opdComplaintRepository.save(complaint));
    }

    @Transactional(readOnly = true)
    public List<OpdComplaintResponse> getByVisitId(Long visitId) {
        return opdComplaintRepository.findByVisitIdOrderByCreatedAtAsc(visitId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void applyRequest(OpdComplaint complaint, OpdComplaintRequest request) {
        complaint.setVisitId(request.visitId());
        complaint.setComplaintType(request.complaintType());
        complaint.setSeverity(request.severity());
        complaint.setDurationValue(request.durationValue());
        complaint.setDurationUnit(request.durationUnit());
        complaint.setFrequency(request.frequency());
        complaint.setOnsetDate(request.onsetDate());
        complaint.setNotes(request.notes());
        complaint.setDetails(request.details());
    }

    private OpdComplaintResponse toResponse(OpdComplaint complaint) {
        return new OpdComplaintResponse(
                complaint.getId(),
                complaint.getVisitId(),
                complaint.getComplaintType(),
                complaint.getComplaintType().getLabel(),
                complaint.getSeverity(),
                complaint.getDurationValue(),
                complaint.getDurationUnit(),
                complaint.getFrequency(),
                complaint.getOnsetDate(),
                complaint.getNotes(),
                complaint.getDetails(),
                complaint.getCreatedAt()
        );
    }
}
