package com.hms.ipd.mar.repository;

import com.hms.ipd.mar.domain.MedicationAdministration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministration, Long> {

    List<MedicationAdministration> findByAdmissionIdOrderByScheduledTimeAsc(Long admissionId);
}
