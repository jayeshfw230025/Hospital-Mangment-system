package com.hms.nutrition.domain;

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

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "nutrition_assessments")
public class NutritionAssessment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_upid", nullable = false, length = 20)
    private String patientUpid;

    @Column(name = "admission_id")
    private Long admissionId;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "bmi")
    private Double bmi;

    @Column(name = "weight_loss_percent")
    private Double weightLossPercent;

    @Column(name = "dietary_intake_percent")
    private Double dietaryIntakePercent;

    @Column(name = "disease_severity_score")
    private Integer diseaseSeverityScore;

    @Column(name = "nrs_nutritional_status_score")
    private Integer nrsNutritionalStatusScore;

    @Column(name = "nrs_age_adjustment")
    private Integer nrsAgeAdjustment;

    @Column(name = "nrs_total_score")
    private Integer nrsTotalScore;

    @Column(name = "nrs_at_risk")
    private Boolean nrsAtRisk;

    @Column(name = "acute_disease_effect")
    private Boolean acuteDiseaseEffect;

    @Column(name = "must_bmi_score")
    private Integer mustBmiScore;

    @Column(name = "must_weight_loss_score")
    private Integer mustWeightLossScore;

    @Column(name = "must_acute_disease_score")
    private Integer mustAcuteDiseaseScore;

    @Column(name = "must_total_score")
    private Integer mustTotalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "must_risk_category", length = 10)
    private MustRiskCategory mustRiskCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "disease_category", length = 30)
    private DiseaseCategory diseaseCategory;

    @Column(name = "caloric_target_min_kcal_per_day")
    private Double caloricTargetMinKcalPerDay;

    @Column(name = "caloric_target_max_kcal_per_day")
    private Double caloricTargetMaxKcalPerDay;

    @Column(name = "protein_target_min_g_per_day")
    private Double proteinTargetMinGPerDay;

    @Column(name = "protein_target_max_g_per_day")
    private Double proteinTargetMaxGPerDay;

    @Column(name = "fluid_requirement_ml_per_day")
    private Double fluidRequirementMlPerDay;

    @Column(name = "sodium_restriction_meq_per_day")
    private Double sodiumRestrictionMeqPerDay;

    @Column(name = "potassium_balance_meq_per_day")
    private Double potassiumBalanceMeqPerDay;

    @Column(name = "enteral_parenteral_support")
    private Boolean enteralParenteralSupport;

    @Column(name = "dietician_assessment", length = 2000)
    private String dieticianAssessment;

    @Column(name = "weekly_follow_up_plan", length = 1000)
    private String weeklyFollowUpPlan;
}
