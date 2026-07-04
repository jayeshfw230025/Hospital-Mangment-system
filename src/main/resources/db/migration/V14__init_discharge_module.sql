CREATE TABLE discharge_summaries (
    id                              BIGINT AUTO_INCREMENT PRIMARY KEY,
    admission_id                    BIGINT          NOT NULL,
    discharge_date_time             TIMESTAMP       NOT NULL,
    length_of_stay_days             INT             NULL,
    discharge_type                  VARCHAR(15)     NOT NULL,
    primary_diagnosis_icd10         VARCHAR(10)     NOT NULL,
    secondary_diagnosis_icd10       VARCHAR(10)     NULL,
    discharge_diagnosis_text        VARCHAR(1000)   NULL,
    summary_of_hospital_stay        VARCHAR(4000)   NULL,
    discharge_diet_plan             VARCHAR(2000)   NULL,
    follow_up_date_time             TIMESTAMP       NULL,
    follow_up_instructions          VARCHAR(1000)   NULL,
    discharge_condition             VARCHAR(15)     NULL,
    discharged_by_doctor_name       VARCHAR(255)    NOT NULL,
    discharged_by_doctor_signature  VARCHAR(1000)   NOT NULL,
    medical_records_checked         BOOLEAN         NULL,
    discharge_instructions          VARCHAR(2000)   NULL,

    created_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                      VARCHAR(100)    NULL,
    updated_by                      VARCHAR(100)    NULL,

    CONSTRAINT uk_discharge_summaries_admission UNIQUE (admission_id),
    CONSTRAINT fk_discharge_summaries_admission FOREIGN KEY (admission_id) REFERENCES ipd_admissions (id),
    CONSTRAINT fk_discharge_summaries_primary_icd10 FOREIGN KEY (primary_diagnosis_icd10) REFERENCES icd10_codes (code),
    CONSTRAINT fk_discharge_summaries_secondary_icd10 FOREIGN KEY (secondary_diagnosis_icd10) REFERENCES icd10_codes (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE discharge_significant_procedures (
    discharge_summary_id   BIGINT          NOT NULL,
    procedure_description   VARCHAR(255)    NULL,
    CONSTRAINT fk_dsp_summary FOREIGN KEY (discharge_summary_id) REFERENCES discharge_summaries (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE discharge_complications (
    discharge_summary_id       BIGINT          NOT NULL,
    complication_description    VARCHAR(500)    NULL,
    CONSTRAINT fk_dc_summary FOREIGN KEY (discharge_summary_id) REFERENCES discharge_summaries (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE discharge_medications (
    discharge_summary_id   BIGINT          NOT NULL,
    drug_name               VARCHAR(255)    NULL,
    dosage                  VARCHAR(100)    NULL,
    frequency               VARCHAR(100)    NULL,
    duration_days           INT             NULL,
    CONSTRAINT fk_dm_summary FOREIGN KEY (discharge_summary_id) REFERENCES discharge_summaries (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
