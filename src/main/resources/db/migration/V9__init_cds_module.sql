CREATE TABLE cds_alerts (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_upid        VARCHAR(20)     NOT NULL,
    context             VARCHAR(10)     NOT NULL,
    rule_name           VARCHAR(50)     NOT NULL,
    finding             VARCHAR(255)    NOT NULL,
    suggestion          VARCHAR(255)    NOT NULL,

    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by           VARCHAR(100)    NULL,
    updated_by           VARCHAR(100)    NULL,

    CONSTRAINT fk_cds_alerts_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_cds_alerts_patient_upid ON cds_alerts (patient_upid);
