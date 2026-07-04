package com.hms.ipd.procedure.repository;

import com.hms.ipd.procedure.domain.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcedureRepository extends JpaRepository<Procedure, Long> {

    List<Procedure> findByAdmissionIdOrderByCreatedAtAsc(Long admissionId);
}
