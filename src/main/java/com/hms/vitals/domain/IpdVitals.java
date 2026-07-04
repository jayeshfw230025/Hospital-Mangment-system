package com.hms.vitals.domain;

import com.hms.common.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@Table(name = "ipd_vitals")
public class IpdVitals extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "patient_upid", nullable = false, length = 20)
    private String patientUpid;

    @Embedded
    private CoreVitals coreVitals;

    @Column(name = "qtc_ms")
    private Integer qtcMs;

    @Column(name = "map_value")
    private Double mapValue;

    @Column(name = "input_output_balance_ml")
    private Integer inputOutputBalanceMl;

    @Column(name = "gcs_score")
    private Integer gcsScore;

    @Column(name = "cvp_cm_h2o")
    private Double cvpCmH2o;

    @Enumerated(EnumType.STRING)
    @Column(name = "gag_reflex", length = 10)
    private GagReflexStatus gagReflex;
}
