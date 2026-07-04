package com.hms.clinical.examination;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicalExaminationRepository extends JpaRepository<ClinicalExamination, Long> {

    List<ClinicalExamination> findByVisitIdOrderByCreatedAtAsc(Long visitId);

    List<ClinicalExamination> findByAdmissionIdOrderByCreatedAtAsc(Long admissionId);
}
