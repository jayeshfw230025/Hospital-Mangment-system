package com.hms.cds.domain;

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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cds_alerts")
public class CdsAlert extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_upid", nullable = false, length = 20)
    private String patientUpid;

    @Enumerated(EnumType.STRING)
    @Column(name = "context", nullable = false, length = 10)
    private CdsContext context;

    @Column(name = "rule_name", nullable = false, length = 50)
    private String ruleName;

    @Column(name = "finding", nullable = false, length = 255)
    private String finding;

    @Column(name = "suggestion", nullable = false, length = 255)
    private String suggestion;
}
