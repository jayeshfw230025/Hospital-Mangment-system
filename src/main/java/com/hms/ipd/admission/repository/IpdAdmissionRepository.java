package com.hms.ipd.admission.repository;

import com.hms.ipd.admission.domain.IpdAdmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IpdAdmissionRepository extends JpaRepository<IpdAdmission, Long> {

    List<IpdAdmission> findByPatientUpidOrderByAdmissionDateTimeDesc(String patientUpid);
}
