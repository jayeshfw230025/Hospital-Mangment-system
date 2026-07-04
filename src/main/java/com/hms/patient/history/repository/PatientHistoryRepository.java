package com.hms.patient.history.repository;

import com.hms.patient.history.domain.PatientHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientHistoryRepository extends JpaRepository<PatientHistory, Long> {

    Optional<PatientHistory> findByPatientUpid(String patientUpid);

    boolean existsByPatientUpid(String patientUpid);
}
