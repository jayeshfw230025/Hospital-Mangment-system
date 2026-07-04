package com.hms.ipd.admission.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.diagnosis.domain.Icd10Code;
import com.hms.diagnosis.repository.Icd10CodeRepository;
import com.hms.ipd.admission.domain.Bed;
import com.hms.ipd.admission.domain.IpdAdmission;
import com.hms.ipd.admission.domain.TpaPreAuthorization;
import com.hms.ipd.admission.dto.BedResponse;
import com.hms.ipd.admission.dto.IpdAdmissionRequest;
import com.hms.ipd.admission.dto.IpdAdmissionResponse;
import com.hms.ipd.admission.dto.TpaPreAuthResponse;
import com.hms.ipd.admission.repository.BedRepository;
import com.hms.ipd.admission.repository.IpdAdmissionRepository;
import com.hms.ipd.admission.repository.TpaPreAuthorizationRepository;
import com.hms.patient.history.dto.CurrentMedicationDto;
import com.hms.patient.history.repository.PatientHistoryRepository;
import com.hms.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
public class IpdAdmissionService {

    private final IpdAdmissionRepository ipdAdmissionRepository;
    private final PatientRepository patientRepository;
    private final Icd10CodeRepository icd10CodeRepository;
    private final PatientHistoryRepository patientHistoryRepository;
    private final BedRepository bedRepository;
    private final TpaPreAuthorizationRepository tpaPreAuthorizationRepository;
    private final AdmissionConsentStorageService consentStorageService;

    public IpdAdmissionService(IpdAdmissionRepository ipdAdmissionRepository,
                                PatientRepository patientRepository,
                                Icd10CodeRepository icd10CodeRepository,
                                PatientHistoryRepository patientHistoryRepository,
                                BedRepository bedRepository,
                                TpaPreAuthorizationRepository tpaPreAuthorizationRepository,
                                AdmissionConsentStorageService consentStorageService) {
        this.ipdAdmissionRepository = ipdAdmissionRepository;
        this.patientRepository = patientRepository;
        this.icd10CodeRepository = icd10CodeRepository;
        this.patientHistoryRepository = patientHistoryRepository;
        this.bedRepository = bedRepository;
        this.tpaPreAuthorizationRepository = tpaPreAuthorizationRepository;
        this.consentStorageService = consentStorageService;
    }

    @Transactional
    public IpdAdmissionResponse create(IpdAdmissionRequest request, MultipartFile consentDocument) {
        if (patientRepository.findByUpid(request.patientId()).isEmpty()) {
            throw new ResourceNotFoundException("Patient not found with UPID: " + request.patientId());
        }
        requireActiveIcd10Code(request.primaryDiagnosisIcd10());
        if (request.secondaryDiagnosisIcd10() != null && !request.secondaryDiagnosisIcd10().isBlank()) {
            requireActiveIcd10Code(request.secondaryDiagnosisIcd10());
        }

        IpdAdmission admission = new IpdAdmission();
        admission.setAdmissionDateTime(Instant.now());
        applyRequest(admission, request);

        if (consentDocument != null && !consentDocument.isEmpty()) {
            admission.setConsentDocumentFileKey(consentStorageService.store(consentDocument));
            admission.setConsentDocumentFileName(consentDocument.getOriginalFilename());
        }

        IpdAdmission saved = ipdAdmissionRepository.save(admission);
        return toResponse(saved);
    }

    @Transactional
    public IpdAdmissionResponse update(Long id, IpdAdmissionRequest request, MultipartFile consentDocument) {
        IpdAdmission admission = ipdAdmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with id: " + id));

        requireActiveIcd10Code(request.primaryDiagnosisIcd10());
        if (request.secondaryDiagnosisIcd10() != null && !request.secondaryDiagnosisIcd10().isBlank()) {
            requireActiveIcd10Code(request.secondaryDiagnosisIcd10());
        }

        applyRequest(admission, request);

        if (consentDocument != null && !consentDocument.isEmpty()) {
            admission.setConsentDocumentFileKey(consentStorageService.store(consentDocument));
            admission.setConsentDocumentFileName(consentDocument.getOriginalFilename());
        }

        return toResponse(ipdAdmissionRepository.save(admission));
    }

    @Transactional(readOnly = true)
    public IpdAdmissionResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<IpdAdmissionResponse> getByPatientId(String patientUpid) {
        return ipdAdmissionRepository.findByPatientUpidOrderByAdmissionDateTimeDesc(patientUpid).stream()
                .map(this::toResponse)
                .toList();
    }

    private void applyRequest(IpdAdmission admission, IpdAdmissionRequest request) {
        admission.setPatientUpid(request.patientId());
        admission.setAdmissionType(request.admissionType());
        admission.setAdmissionSource(request.admissionSource());
        admission.setReferralDoctorName(request.referralDoctorName());
        admission.setReferralDoctorContact(request.referralDoctorContact());
        admission.setReferringHospitalName(request.referringHospitalName());
        admission.setReferringHospitalContact(request.referringHospitalContact());
        admission.setPrimaryDiagnosisIcd10(request.primaryDiagnosisIcd10());
        admission.setSecondaryDiagnosisIcd10(request.secondaryDiagnosisIcd10());
        admission.setClinicalSummary(request.clinicalSummary());
        admission.setConsentSignature(request.consentSignature());
    }

    private IpdAdmission findOrThrow(Long id) {
        return ipdAdmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found with id: " + id));
    }

    private Icd10Code requireActiveIcd10Code(String code) {
        Icd10Code icd10Code = icd10CodeRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("ICD-10 code not found: " + code));
        if (!icd10Code.isActive()) {
            throw new IllegalArgumentException("ICD-10 code " + code + " is inactive");
        }
        return icd10Code;
    }

    private IpdAdmissionResponse toResponse(IpdAdmission admission) {
        String primaryDesc = icd10CodeRepository.findById(admission.getPrimaryDiagnosisIcd10())
                .map(Icd10Code::getDescription).orElse(null);
        String secondaryDesc = admission.getSecondaryDiagnosisIcd10() == null ? null
                : icd10CodeRepository.findById(admission.getSecondaryDiagnosisIcd10())
                        .map(Icd10Code::getDescription).orElse(null);

        List<String> hardStopAllergies = patientHistoryRepository.findByPatientUpid(admission.getPatientUpid())
                .map(h -> h.getAllergies().stream()
                        .filter(a -> a.isHardStop())
                        .map(a -> a.getAllergen())
                        .toList())
                .orElse(List.of());

        List<CurrentMedicationDto> currentMedications = patientHistoryRepository.findByPatientUpid(admission.getPatientUpid())
                .map(h -> h.getCurrentMedications().stream()
                        .map(m -> new CurrentMedicationDto(m.getDrugName(), m.getDosage(), m.getFrequency()))
                        .toList())
                .orElse(List.of());

        BedResponse bedResponse = admission.getBedId() == null ? null
                : bedRepository.findById(admission.getBedId()).map(this::toBedResponse).orElse(null);

        TpaPreAuthResponse latestTpaPreAuth = tpaPreAuthorizationRepository
                .findFirstByAdmissionIdOrderByCreatedAtDesc(admission.getId())
                .map(this::toTpaResponse)
                .orElse(null);

        return new IpdAdmissionResponse(
                admission.getId(),
                admission.getPatientUpid(),
                admission.getAdmissionDateTime(),
                admission.getAdmissionType(),
                admission.getAdmissionSource(),
                admission.getReferralDoctorName(),
                admission.getReferralDoctorContact(),
                admission.getReferringHospitalName(),
                admission.getReferringHospitalContact(),
                admission.getPrimaryDiagnosisIcd10(),
                primaryDesc,
                admission.getSecondaryDiagnosisIcd10(),
                secondaryDesc,
                admission.getClinicalSummary(),
                hardStopAllergies,
                currentMedications,
                admission.getConsentDocumentFileKey() != null,
                bedResponse,
                latestTpaPreAuth,
                admission.getCreatedAt()
        );
    }

    private BedResponse toBedResponse(Bed bed) {
        return new BedResponse(bed.getId(), bed.getWardType(), bed.getRoomNumber(), bed.getBedNumber(),
                bed.getStatus(), bed.getCurrentAdmissionId());
    }

    private TpaPreAuthResponse toTpaResponse(TpaPreAuthorization tpa) {
        return new TpaPreAuthResponse(tpa.getId(), tpa.getAdmissionId(), tpa.getInsuranceCompanyName(),
                tpa.getPolicyNumber(), tpa.getPreAuthNumber(), tpa.getPreAuthDate(), tpa.getApprovalStatus(),
                tpa.getEstimatedCost(), tpa.getCreatedAt());
    }
}
