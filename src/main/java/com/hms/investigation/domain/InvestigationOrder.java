package com.hms.investigation.domain;

import com.hms.common.audit.Auditable;
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

import java.time.LocalDate;

/**
 * Shared by OPD and IPD orders (single table) since a single POST /order endpoint
 * covers both contexts - whichever of visitId/admissionId is populated determines
 * context, which in turn determines whether IPD-only investigation types are allowed.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "investigation_orders")
public class InvestigationOrder extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_upid", nullable = false, length = 20)
    private String patientUpid;

    @Column(name = "visit_id")
    private Long visitId;

    @Column(name = "admission_id")
    private Long admissionId;

    @Column(name = "investigation_type_code", nullable = false, length = 40)
    private String investigationTypeCode;

    @Column(name = "ordered_date", nullable = false)
    private LocalDate orderedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private OrderStatus status;

    @Column(name = "notes", length = 500)
    private String notes;
}
