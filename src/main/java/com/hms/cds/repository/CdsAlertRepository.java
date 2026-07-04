package com.hms.cds.repository;

import com.hms.cds.domain.CdsAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CdsAlertRepository extends JpaRepository<CdsAlert, Long> {

    List<CdsAlert> findByPatientUpidOrderByCreatedAtDesc(String patientUpid);
}
