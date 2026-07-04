package com.hms.ipd.admission.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.ipd.admission.domain.Bed;
import com.hms.ipd.admission.domain.BedStatus;
import com.hms.ipd.admission.domain.BedTransferHistory;
import com.hms.ipd.admission.domain.IpdAdmission;
import com.hms.ipd.admission.domain.WardType;
import com.hms.ipd.admission.dto.BedAllocateRequest;
import com.hms.ipd.admission.dto.BedResponse;
import com.hms.ipd.admission.dto.BedTransferRequest;
import com.hms.ipd.admission.repository.BedRepository;
import com.hms.ipd.admission.repository.BedTransferHistoryRepository;
import com.hms.ipd.admission.repository.IpdAdmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BedService {

    private final BedRepository bedRepository;
    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final BedTransferHistoryRepository bedTransferHistoryRepository;

    public BedService(BedRepository bedRepository,
                       IpdAdmissionRepository ipdAdmissionRepository,
                       BedTransferHistoryRepository bedTransferHistoryRepository) {
        this.bedRepository = bedRepository;
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.bedTransferHistoryRepository = bedTransferHistoryRepository;
    }

    @Transactional
    public BedResponse allocate(BedAllocateRequest request) {
        IpdAdmission admission = requireAdmission(request.admissionId());
        Bed bed = requireBed(request.bedId());

        if (admission.getBedId() != null) {
            throw new IllegalArgumentException(
                    "Admission " + admission.getId() + " already has a bed assigned; use transfer instead");
        }
        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new IllegalArgumentException("Bed " + bed.getId() + " is not available");
        }

        bed.setStatus(BedStatus.OCCUPIED);
        bed.setCurrentAdmissionId(admission.getId());
        bedRepository.save(bed);

        admission.setBedId(bed.getId());
        ipdAdmissionRepository.save(admission);

        return toResponse(bed);
    }

    @Transactional
    public BedResponse transfer(BedTransferRequest request) {
        IpdAdmission admission = requireAdmission(request.admissionId());
        Bed newBed = requireBed(request.newBedId());

        if (newBed.getStatus() != BedStatus.AVAILABLE) {
            throw new IllegalArgumentException("Bed " + newBed.getId() + " is not available");
        }

        Long fromBedId = admission.getBedId();
        if (fromBedId != null) {
            Bed oldBed = requireBed(fromBedId);
            oldBed.setStatus(BedStatus.AVAILABLE);
            oldBed.setCurrentAdmissionId(null);
            bedRepository.save(oldBed);
        }

        newBed.setStatus(BedStatus.OCCUPIED);
        newBed.setCurrentAdmissionId(admission.getId());
        bedRepository.save(newBed);

        admission.setBedId(newBed.getId());
        ipdAdmissionRepository.save(admission);

        BedTransferHistory history = new BedTransferHistory();
        history.setAdmissionId(admission.getId());
        history.setFromBedId(fromBedId);
        history.setToBedId(newBed.getId());
        history.setReason(request.reason());
        bedTransferHistoryRepository.save(history);

        return toResponse(newBed);
    }

    @Transactional(readOnly = true)
    public List<BedResponse> getAvailableBeds(WardType wardType) {
        List<Bed> beds = wardType == null
                ? bedRepository.findByStatus(BedStatus.AVAILABLE)
                : bedRepository.findByStatusAndWardType(BedStatus.AVAILABLE, wardType);
        return beds.stream().map(this::toResponse).toList();
    }

    private IpdAdmission requireAdmission(Long id) {
        return ipdAdmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with id: " + id));
    }

    private Bed requireBed(Long id) {
        return bedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found with id: " + id));
    }

    private BedResponse toResponse(Bed bed) {
        return new BedResponse(bed.getId(), bed.getWardType(), bed.getRoomNumber(), bed.getBedNumber(),
                bed.getStatus(), bed.getCurrentAdmissionId());
    }
}
