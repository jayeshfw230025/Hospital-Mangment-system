package com.hms.ipd.procedure.repository;

import com.hms.ipd.procedure.domain.ProcedureComplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcedureComplicationRepository extends JpaRepository<ProcedureComplication, Long> {

    List<ProcedureComplication> findByProcedureIdOrderByCreatedAtAsc(Long procedureId);
}
