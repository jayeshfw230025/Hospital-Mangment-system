package com.hms.integration.abdm.repository;

import com.hms.integration.abdm.domain.AbdmConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AbdmConsentRepository extends JpaRepository<AbdmConsent, Long> {

    Optional<AbdmConsent> findByConsentId(String consentId);
}
