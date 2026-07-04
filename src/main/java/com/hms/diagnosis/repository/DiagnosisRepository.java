package com.hms.diagnosis.repository;

import com.hms.diagnosis.domain.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    List<Diagnosis> findByPatientUpidOrderByCreatedAtDesc(String patientUpid);
}
