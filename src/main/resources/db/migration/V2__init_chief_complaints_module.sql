CREATE TABLE opd_complaints (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_id                    BIGINT          NOT NULL,
    complaint_type              VARCHAR(30)     NOT NULL,
    severity                    VARCHAR(15)     NULL,
    duration_value              INT             NULL,
    duration_unit               VARCHAR(10)     NULL,
    frequency                   VARCHAR(15)     NULL,
    onset_date                  DATE            NULL,
    notes                       VARCHAR(1000)   NULL,
    details                     JSON            NULL,

    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                  VARCHAR(100)    NULL,
    updated_by                  VARCHAR(100)    NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_opd_complaints_visit_id ON opd_complaints (visit_id);
CREATE INDEX idx_opd_complaints_type ON opd_complaints (complaint_type);

CREATE TABLE ipd_complaints (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    admission_id                BIGINT          NOT NULL,
    complaint_type              VARCHAR(30)     NOT NULL,
    severity                    VARCHAR(15)     NULL,
    severity_score              INT             NULL,
    duration_value              INT             NULL,
    duration_unit               VARCHAR(10)     NULL,
    associated_vitals_impact    VARCHAR(500)    NULL,
    response_to_initial_treatment VARCHAR(20)   NULL,
    notes                       VARCHAR(1000)   NULL,
    details                     JSON            NULL,

    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                  VARCHAR(100)    NULL,
    updated_by                  VARCHAR(100)    NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_ipd_complaints_admission_id ON ipd_complaints (admission_id);
CREATE INDEX idx_ipd_complaints_type ON ipd_complaints (complaint_type);
