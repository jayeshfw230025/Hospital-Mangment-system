CREATE TABLE nutrition_assessments (
    id                                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_upid                        VARCHAR(20)     NOT NULL,
    admission_id                        BIGINT          NULL,
    assessment_date                     DATE            NOT NULL,

    weight_kg                           DOUBLE          NULL,
    height_cm                           DOUBLE          NULL,
    bmi                                 DOUBLE          NULL,

    weight_loss_percent                 DOUBLE          NULL,
    dietary_intake_percent              DOUBLE          NULL,
    disease_severity_score              INT             NULL,
    nrs_nutritional_status_score        INT             NULL,
    nrs_age_adjustment                  INT             NULL,
    nrs_total_score                     INT             NULL,
    nrs_at_risk                         BOOLEAN         NULL,

    acute_disease_effect                BOOLEAN         NULL,
    must_bmi_score                      INT             NULL,
    must_weight_loss_score              INT             NULL,
    must_acute_disease_score            INT             NULL,
    must_total_score                    INT             NULL,
    must_risk_category                  VARCHAR(10)     NULL,

    disease_category                    VARCHAR(30)     NULL,
    caloric_target_min_kcal_per_day     DOUBLE          NULL,
    caloric_target_max_kcal_per_day     DOUBLE          NULL,
    protein_target_min_g_per_day        DOUBLE          NULL,
    protein_target_max_g_per_day        DOUBLE          NULL,
    fluid_requirement_ml_per_day        DOUBLE          NULL,
    sodium_restriction_meq_per_day      DOUBLE          NULL,
    potassium_balance_meq_per_day       DOUBLE          NULL,
    enteral_parenteral_support          BOOLEAN         NULL,
    dietician_assessment                VARCHAR(2000)   NULL,
    weekly_follow_up_plan               VARCHAR(1000)   NULL,

    created_at                          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                           VARCHAR(100)    NULL,
    updated_by                           VARCHAR(100)    NULL,

    CONSTRAINT fk_nutrition_assessments_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid),
    CONSTRAINT fk_nutrition_assessments_admission FOREIGN KEY (admission_id) REFERENCES ipd_admissions (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_nutrition_assessments_patient_upid ON nutrition_assessments (patient_upid);
