package com.hms.patient.history.repository;

import com.hms.patient.history.domain.FamilyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyHistoryRepository extends JpaRepository<FamilyHistory, Long> {

    Optional<FamilyHistory> findByPatientUpid(String patientUpid);

    boolean existsByPatientUpid(String patientUpid);
}
