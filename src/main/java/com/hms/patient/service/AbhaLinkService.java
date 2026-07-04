package com.hms.patient.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Placeholder for ABHA/FHIR R4 linkage. Validates format only; will be replaced
 * with a HAPI FHIR client call against the ABDM sandbox/production gateway once
 * the integration-layer stage is implemented.
 */
@Service
public class AbhaLinkService {

    private static final int ABHA_NUMBER_LENGTH = 14;

    public String verifyAndLink(String abhaNumber) {
        if (!StringUtils.hasText(abhaNumber)) {
            return null;
        }
        String normalized = abhaNumber.replace("-", "").trim();
        if (normalized.length() != ABHA_NUMBER_LENGTH || !normalized.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("ABHA number must be a 14-digit number");
        }
        return normalized;
    }
}
