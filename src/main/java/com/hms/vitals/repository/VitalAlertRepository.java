package com.hms.vitals.repository;

import com.hms.vitals.domain.SourceType;
import com.hms.vitals.domain.VitalAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VitalAlertRepository extends JpaRepository<VitalAlert, Long> {

    List<VitalAlert> findByPatientUpidOrderByCreatedAtDesc(String patientUpid);

    List<VitalAlert> findBySourceVitalsIdAndSourceType(Long sourceVitalsId, SourceType sourceType);
}
