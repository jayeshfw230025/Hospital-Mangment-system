package com.hms.patient.repository;

import com.hms.patient.domain.Patient;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class PatientSpecifications {

    private PatientSpecifications() {
    }

    public static Specification<Patient> fullNameContains(String fullName) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%");
    }

    public static Specification<Patient> primaryContactNumberContains(String contactNumber) {
        return (root, query, cb) -> cb.like(root.get("primaryContactNumber"), "%" + contactNumber + "%");
    }

    public static Specification<Patient> dateOfBirthEquals(LocalDate dateOfBirth) {
        return (root, query, cb) -> cb.equal(root.get("dateOfBirth"), dateOfBirth);
    }

    public static Specification<Patient> upidEquals(String upid) {
        return (root, query, cb) -> cb.equal(root.get("upid"), upid);
    }
}
