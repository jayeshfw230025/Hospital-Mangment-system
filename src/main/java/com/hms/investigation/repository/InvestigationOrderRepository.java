package com.hms.investigation.repository;

import com.hms.investigation.domain.InvestigationOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestigationOrderRepository extends JpaRepository<InvestigationOrder, Long> {

    List<InvestigationOrder> findByPatientUpidOrderByCreatedAtDesc(String patientUpid);

    List<InvestigationOrder> findByVisitIdOrderByCreatedAtDesc(Long visitId);
}
