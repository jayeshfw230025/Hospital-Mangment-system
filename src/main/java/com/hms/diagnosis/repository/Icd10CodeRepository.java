package com.hms.diagnosis.repository;

import com.hms.diagnosis.domain.Icd10Code;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Icd10CodeRepository extends JpaRepository<Icd10Code, String> {

    List<Icd10Code> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCodeAsc(
            String codeQuery, String descriptionQuery);

    List<Icd10Code> findByActiveTrueOrderByCodeAsc();
}
