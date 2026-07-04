package com.hms.vitals.repository;

import com.hms.vitals.domain.OpdVitals;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpdVitalsRepository extends JpaRepository<OpdVitals, Long> {

    List<OpdVitals> findByVisitIdOrderByCreatedAtAsc(Long visitId);

    List<OpdVitals> findByPatientUpidOrderByCreatedAtAsc(String patientUpid);
}
