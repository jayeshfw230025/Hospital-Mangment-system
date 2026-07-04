package com.hms.nutrition.repository;

import com.hms.nutrition.domain.NutritionAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NutritionAssessmentRepository extends JpaRepository<NutritionAssessment, Long> {

    List<NutritionAssessment> findByPatientUpidOrderByCreatedAtDesc(String patientUpid);
}
