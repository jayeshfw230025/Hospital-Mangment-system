package com.hms.ipd.admission.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.admission.domain.PreAuthStatus;
import com.hms.ipd.admission.domain.TpaPreAuthorization;
import com.hms.ipd.admission.dto.TpaPreAuthRequest;
import com.hms.ipd.admission.dto.TpaPreAuthResponse;
import com.hms.ipd.admission.repository.IpdAdmissionRepository;
import com.hms.ipd.admission.repository.TpaPreAuthorizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TpaPreAuthorizationService {

    private final TpaPreAuthorizationRepository tpaPreAuthorizationRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;

    public TpaPreAuthorizationService(TpaPreAuthorizationRepository tpaPreAuthorizationRepository,
                                       IpdAdmissionRepository ipdAdmissionRepository) {
        this.tpaPreAuthorizationRepository = tpaPreAuthorizationRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
    }

    @Transactional
    public TpaPreAuthResponse submit(TpaPreAuthRequest request) {
        if (ipdAdmissionRepository.findById(request.admissionId()).isEmpty()) {
            throw new ResourceNotFoundException("Admission not found with id: " + request.admissionId());
        }

        TpaPreAuthorization tpa = new TpaPreAuthorization();
        tpa.setAdmissionId(request.admissionId());
        tpa.setInsuranceCompanyName(request.insuranceCompanyName());
        tpa.setPolicyNumber(request.policyNumber());
        tpa.setPreAuthNumber(request.preAuthNumber());
        tpa.setPreAuthDate(request.preAuthDate());
        tpa.setApprovalStatus(request.approvalStatus() == null ? PreAuthStatus.PENDING : request.approvalStatus());
        tpa.setEstimatedCost(request.estimatedCost());

        TpaPreAuthorization saved = tpaPreAuthorizationRepository.save(tpa);
        return toResponse(saved);
    }

    private TpaPreAuthResponse toResponse(TpaPreAuthorization tpa) {
        return new TpaPreAuthResponse(tpa.getId(), tpa.getAdmissionId(), tpa.getInsuranceCompanyName(),
                tpa.getPolicyNumber(), tpa.getPreAuthNumber(), tpa.getPreAuthDate(), tpa.getApprovalStatus(),
                tpa.getEstimatedCost(), tpa.getCreatedAt());
    }
}
