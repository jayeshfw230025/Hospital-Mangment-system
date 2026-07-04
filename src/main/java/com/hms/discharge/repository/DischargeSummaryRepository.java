package com.hms.discharge.repository;

import com.hms.discharge.domain.DischargeSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DischargeSummaryRepository extends JpaRepository<DischargeSummary, Long> {

    Optional<DischargeSummary> findByAdmissionId(Long admissionId);

    boolean existsByAdmissionId(Long admissionId);
}
