CREATE TABLE procedures (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    admission_id        BIGINT          NOT NULL,
    procedure_type      VARCHAR(30)     NOT NULL,
    procedure_date      DATE            NOT NULL,
    performed_by_name   VARCHAR(255)    NULL,
    notes               VARCHAR(1000)   NULL,
    details             JSON            NULL,

    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by           VARCHAR(100)    NULL,
    updated_by           VARCHAR(100)    NULL,

    CONSTRAINT fk_procedures_admission FOREIGN KEY (admission_id) REFERENCES ipd_admissions (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_procedures_admission_id ON procedures (admission_id);
CREATE INDEX idx_procedures_type ON procedures (procedure_type);

CREATE TABLE procedure_complications (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    procedure_id                BIGINT          NOT NULL,
    complication_description    VARCHAR(500)    NOT NULL,
    severity                    VARCHAR(15)     NULL,
    reported_date                DATE            NOT NULL,
    reported_by_name            VARCHAR(255)    NULL,

    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                   VARCHAR(100)    NULL,
    updated_by                   VARCHAR(100)    NULL,

    CONSTRAINT fk_procedure_complications_procedure FOREIGN KEY (procedure_id) REFERENCES procedures (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_procedure_complications_procedure_id ON procedure_complications (procedure_id);
