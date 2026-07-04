package com.hms.vitals.repository;

import com.hms.vitals.domain.IpdVitals;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IpdVitalsRepository extends JpaRepository<IpdVitals, Long> {

    List<IpdVitals> findByAdmissionIdOrderByCreatedAtAsc(Long admissionId);

    List<IpdVitals> findByPatientUpidOrderByCreatedAtAsc(String patientUpid);
}
