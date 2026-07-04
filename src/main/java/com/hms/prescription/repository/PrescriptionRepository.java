package com.hms.prescription.repository;

import com.hms.prescription.domain.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientUpidOrderByCreatedAtDesc(String patientUpid);

    List<Prescription> findByAdmissionId(Long admissionId);
}
