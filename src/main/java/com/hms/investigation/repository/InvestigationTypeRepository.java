package com.hms.investigation.repository;

import com.hms.investigation.domain.InvestigationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationTypeRepository extends JpaRepository<InvestigationType, String> {
}
