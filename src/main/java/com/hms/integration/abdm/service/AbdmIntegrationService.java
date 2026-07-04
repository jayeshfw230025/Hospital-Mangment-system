package com.hms.integration.abdm.service;

import com.hms.common.exception.ResourceNotFoundException;
import com.hms.fhir.model.FhirBundle;
import com.hms.fhir.service.FhirService;
import com.hms.integration.abdm.domain.AbdmConsent;
import com.hms.integration.abdm.domain.ConsentStatus;
import com.hms.integration.abdm.dto.AbdmConsentRequest;
import com.hms.integration.abdm.dto.AbdmConsentResponse;
import com.hms.integration.abdm.dto.AbdmHealthRecordRequest;
import com.hms.integration.abdm.dto.AbdmHealthRecordResponse;
import com.hms.integration.abdm.dto.AbdmLinkRequest;
import com.hms.integration.abdm.repository.AbdmConsentRepository;
import com.hms.patient.dto.AbhaLinkInitiationResponse;
import com.hms.patient.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The actual ABDM sandbox/production gateway (ABHA creation, HIU/HIP consent
 * flow, health-information exchange) is external infrastructure not stood up at
 * this stage - same "deferred integration" treatment as WhatsApp/Kafka/MinIO
 * elsewhere in this codebase. What IS real here: patient ABHA linking (delegates
 * to the existing Module 1 flow), consent state (persisted, queryable, actually
 * gates the health-record endpoint), and FHIR bundle assembly from live data.
 */
@Service
public class AbdmIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(AbdmIntegrationService.class);
    private static final int DEFAULT_VALIDITY_DAYS = 30;

    private final PatientService patientService;
    private final AbdmConsentRepository consentRepository;
    private final FhirService fhirService;

    public AbdmIntegrationService(PatientService patientService, AbdmConsentRepository consentRepository,
                                   FhirService fhirService) {
        this.patientService = patientService;
        this.consentRepository = consentRepository;
        this.fhirService = fhirService;
    }

    public AbhaLinkInitiationResponse link(AbdmLinkRequest request) {
        return patientService.initiateAbhaLink(request.patientId(), request.abhaNumber());
    }

    @Transactional
    public AbdmConsentResponse createConsent(AbdmConsentRequest request) {
        patientService.getByUpid(request.patientId());

        Instant now = Instant.now();
        int validityDays = request.validityDays() == null ? DEFAULT_VALIDITY_DAYS : request.validityDays();

        AbdmConsent consent = AbdmConsent.builder()
                .consentId(UUID.randomUUID().toString())
                .patientUpid(request.patientId())
                .purpose(request.purpose())
                .hiTypes(String.join(",", request.hiTypes()))
                .status(ConsentStatus.GRANTED)
                .grantedAt(now)
                .expiresAt(now.plus(java.time.Duration.ofDays(validityDays)))
                .createdAt(now)
                .build();
        AbdmConsent saved = consentRepository.save(consent);

        log.info("[STUB] Would notify ABDM Consent Manager of consent {} for patient {} (purpose: {}) "
                + "- pending ABDM gateway integration", saved.getConsentId(), saved.getPatientUpid(), saved.getPurpose());

        return toResponse(saved);
    }

    public AbdmHealthRecordResponse getHealthRecord(AbdmHealthRecordRequest request) {
        AbdmConsent consent = consentRepository.findByConsentId(request.consentId())
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found: " + request.consentId()));

        if (!consent.getPatientUpid().equals(request.patientId())) {
            throw new IllegalArgumentException("Consent does not belong to patient: " + request.patientId());
        }
        if (consent.getStatus() != ConsentStatus.GRANTED) {
            throw new IllegalArgumentException("Consent " + request.consentId() + " is not in GRANTED status");
        }
        if (Instant.now().isAfter(consent.getExpiresAt())) {
            throw new IllegalArgumentException("Consent " + request.consentId() + " has expired");
        }

        List<Object> resources = new ArrayList<>();
        resources.add(fhirService.getPatient(request.patientId()));
        resources.addAll(fhirService.getConditions(request.patientId()));
        resources.addAll(fhirService.getObservations(request.patientId(), null));
        resources.addAll(fhirService.getEncounters(request.patientId()));
        resources.addAll(fhirService.getMedicationRequests(request.patientId()));

        FhirBundle bundle = FhirBundle.collection(resources);

        log.info("[STUB] Would push a {}-resource FHIR Bundle to the ABDM Health Information Exchange for patient {} "
                + "under consent {} - pending ABDM gateway integration", resources.size(), request.patientId(), request.consentId());

        return new AbdmHealthRecordResponse(request.patientId(), request.consentId(),
                "Health record bundle assembled (transmission to ABDM HIE not yet wired up)", bundle);
    }

    private AbdmConsentResponse toResponse(AbdmConsent consent) {
        return new AbdmConsentResponse(
                consent.getConsentId(), consent.getPatientUpid(), consent.getPurpose(),
                Arrays.asList(consent.getHiTypes().split(",")),
                consent.getStatus(), consent.getGrantedAt(), consent.getExpiresAt());
    }
}
