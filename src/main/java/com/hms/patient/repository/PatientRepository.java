package com.hms.patient.repository;

import com.hms.patient.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {

    Optional<Patient> findByUpid(String upid);

    boolean existsByPrimaryContactNumber(String primaryContactNumber);

    boolean existsByPrimaryContactNumberAndUpidNot(String primaryContactNumber, String upid);

    List<Patient> findByFullNameIgnoreCaseAndDateOfBirthAndPrimaryContactNumber(
            String fullName, LocalDate dateOfBirth, String primaryContactNumber);

    long countByUpidStartingWith(String prefix);
}
