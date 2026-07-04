package com.hms.ipd.admission.repository;

import com.hms.ipd.admission.domain.TpaPreAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TpaPreAuthorizationRepository extends JpaRepository<TpaPreAuthorization, Long> {

    Optional<TpaPreAuthorization> findFirstByAdmissionIdOrderByCreatedAtDesc(Long admissionId);
}
