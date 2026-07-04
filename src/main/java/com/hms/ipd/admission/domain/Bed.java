package com.hms.ipd.admission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * currentAdmissionId is a plain column, not a JPA/DB foreign key - beds and
 * ipd_admissions reference each other (bed -> current admission, admission ->
 * assigned bed) and enforcing both as FKs would create a circular constraint.
 * IpdAdmission.bedId carries the real FK; this side is application-enforced.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "beds")
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ward_type", nullable = false, length = 20)
    private WardType wardType;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Column(name = "bed_number", nullable = false, length = 20)
    private String bedNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private BedStatus status;

    @Column(name = "current_admission_id")
    private Long currentAdmissionId;
}
