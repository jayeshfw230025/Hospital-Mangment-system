package com.hms.investigation.repository;

import com.hms.investigation.domain.InvestigationReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestigationReportRepository extends JpaRepository<InvestigationReport, Long> {

    List<InvestigationReport> findByOrderId(Long orderId);

    Optional<InvestigationReport> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<InvestigationReport> findByPatientUpidAndInvestigationTypeCodeOrderByReportDateDescIdDesc(
            String patientUpid, String investigationTypeCode);

    List<InvestigationReport> findByPatientUpidOrderByReportDateDescIdDesc(String patientUpid);
}
