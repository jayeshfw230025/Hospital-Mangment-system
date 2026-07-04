CREATE TABLE abdm_consents (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    consent_id      VARCHAR(64)     NOT NULL,
    patient_upid    VARCHAR(50)     NOT NULL,
    purpose         VARCHAR(255)    NOT NULL,
    hi_types        VARCHAR(500)    NULL,
    status          VARCHAR(20)     NOT NULL,
    granted_at      TIMESTAMP       NOT NULL,
    expires_at      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_abdm_consents_consent_id UNIQUE (consent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_abdm_consents_patient ON abdm_consents (patient_upid);
