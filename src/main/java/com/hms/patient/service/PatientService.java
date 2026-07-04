package com.hms.patient.service;

import com.hms.common.exception.DuplicateResourceException;
import com.hms.common.exception.ResourceNotFoundException;
import com.hms.patient.domain.Patient;
import com.hms.patient.dto.AbhaLinkInitiationResponse;
import com.hms.patient.dto.PatientRegistrationRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.dto.PatientUpdateRequest;
import com.hms.patient.dto.ReferralDetailsDto;
import com.hms.patient.mapper.PatientMapper;
import com.hms.patient.repository.PatientRepository;
import com.hms.patient.repository.PatientSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final UpidGeneratorService upidGeneratorService;
    private final AbhaLinkService abhaLinkService;
    private final QrCodeService qrCodeService;
    private final OtpService otpService;

    public PatientService(PatientRepository patientRepository,
                           PatientMapper patientMapper,
                           UpidGeneratorService upidGeneratorService,
                           AbhaLinkService abhaLinkService,
                           QrCodeService qrCodeService,
                           OtpService otpService) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.upidGeneratorService = upidGeneratorService;
        this.abhaLinkService = abhaLinkService;
        this.qrCodeService = qrCodeService;
        this.otpService = otpService;
    }

    @Transactional
    public PatientResponse register(PatientRegistrationRequest request) {
        checkForDuplicate(request);

        Patient patient = patientMapper.toEntity(request);
        patient.setAbhaNumber(abhaLinkService.verifyAndLink(request.abhaNumber()));
        patient.setUpid(upidGeneratorService.generate());

        Patient saved = patientRepository.save(patient);
        return patientMapper.toResponse(saved);
    }

    private void checkForDuplicate(PatientRegistrationRequest request) {
        boolean contactAlreadyRegistered = patientRepository.existsByPrimaryContactNumber(request.primaryContactNumber());
        if (!contactAlreadyRegistered) {
            return;
        }

        List<Patient> exactMatches = patientRepository
                .findByFullNameIgnoreCaseAndDateOfBirthAndPrimaryContactNumber(
                        request.fullName(), request.dateOfBirth(), request.primaryContactNumber());

        if (!exactMatches.isEmpty()) {
            Patient existing = exactMatches.get(0);
            throw new DuplicateResourceException(
                    "A patient with the same name, date of birth and contact number already exists (UPID: "
                            + existing.getUpid() + ")");
        }

        throw new DuplicateResourceException(
                "Primary contact number " + request.primaryContactNumber() + " is already registered to another patient");
    }

    @Transactional(readOnly = true)
    public PatientResponse getByUpid(String upid) {
        Patient patient = patientRepository.findByUpid(upid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UPID: " + upid));
        return patientMapper.toResponse(patient);
    }

    @Transactional(readOnly = true)
    public String getQrCode(String upid) {
        Patient patient = patientRepository.findByUpid(upid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UPID: " + upid));
        String qrContent = patient.getUpid() + (patient.getAbhaNumber() != null ? "|" + patient.getAbhaNumber() : "");
        return qrCodeService.generateBase64Png(qrContent);
    }

    @Transactional
    public PatientResponse update(String upid, PatientUpdateRequest request) {
        Patient patient = patientRepository.findByUpid(upid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UPID: " + upid));

        if (patientRepository.existsByPrimaryContactNumberAndUpidNot(request.primaryContactNumber(), upid)) {
            throw new DuplicateResourceException(
                    "Primary contact number " + request.primaryContactNumber() + " is already registered to another patient");
        }

        patientMapper.updateEntity(patient, request);
        Patient saved = patientRepository.save(patient);
        return patientMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> search(String fullName, String primaryContactNumber,
                                         LocalDate dateOfBirth, String upid, Pageable pageable) {
        Specification<Patient> spec = Specification.where(null);

        if (StringUtils.hasText(fullName)) {
            spec = spec.and(PatientSpecifications.fullNameContains(fullName));
        }
        if (StringUtils.hasText(primaryContactNumber)) {
            spec = spec.and(PatientSpecifications.primaryContactNumberContains(primaryContactNumber));
        }
        if (dateOfBirth != null) {
            spec = spec.and(PatientSpecifications.dateOfBirthEquals(dateOfBirth));
        }
        if (StringUtils.hasText(upid)) {
            spec = spec.and(PatientSpecifications.upidEquals(upid));
        }

        return patientRepository.findAll(spec, pageable).map(patientMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AbhaLinkInitiationResponse initiateAbhaLink(String upid, String abhaNumber) {
        Patient patient = patientRepository.findByUpid(upid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UPID: " + upid));

        String normalizedAbha = abhaLinkService.verifyAndLink(abhaNumber);
        String txnId = otpService.initiate(patient.getUpid(), normalizedAbha);

        return new AbhaLinkInitiationResponse(txnId, "OTP sent for ABHA linkage verification");
    }

    @Transactional
    public PatientResponse verifyOtpAndCompleteAbhaLink(String txnId, String otp) {
        OtpService.OtpTransaction transaction = otpService.verify(txnId, otp);

        Patient patient = patientRepository.findByUpid(transaction.upid())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UPID: " + transaction.upid()));

        patient.setAbhaNumber(transaction.abhaNumber());
        Patient saved = patientRepository.save(patient);
        return patientMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReferralDetailsDto getReferral(String upid) {
        Patient patient = patientRepository.findByUpid(upid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with UPID: " + upid));

        if (patient.getReferralDetails() == null || patient.getReferralDetails().getReferringDoctorName() == null) {
            throw new ResourceNotFoundException("No referral details found for UPID: " + upid);
        }

        var r = patient.getReferralDetails();
        return new ReferralDetailsDto(r.getReferringDoctorName(), r.getReferringHospitalName(),
                r.getReferralDate(), r.getReferralReason(), r.getReferralContactNumber());
    }
}
