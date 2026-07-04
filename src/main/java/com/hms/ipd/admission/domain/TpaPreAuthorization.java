package com.hms.ipd.admission.domain;

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

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tpa_pre_authorizations")
public class TpaPreAuthorization extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_id", nullable = false)
    private Long admissionId;

    @Column(name = "insurance_company_name", nullable = false)
    private String insuranceCompanyName;

    @Column(name = "policy_number", nullable = false)
    private String policyNumber;

    @Column(name = "pre_auth_number")
    private String preAuthNumber;

    @Column(name = "pre_auth_date")
    private LocalDate preAuthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 15)
    private PreAuthStatus approvalStatus;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;
}
