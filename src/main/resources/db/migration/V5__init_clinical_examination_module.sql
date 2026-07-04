CREATE TABLE clinical_examinations (
    id                              BIGINT AUTO_INCREMENT PRIMARY KEY,
    examination_context             VARCHAR(10)     NOT NULL,
    visit_id                        BIGINT          NULL,
    admission_id                    BIGINT          NULL,
    patient_upid                    VARCHAR(20)     NOT NULL,

    abd_scars_present               BOOLEAN         NULL,
    abd_distension_present          BOOLEAN         NULL,
    abd_visible_peristalsis         BOOLEAN         NULL,
    abd_tenderness                  BOOLEAN         NULL,
    abd_tenderness_site             VARCHAR(255)    NULL,
    abd_guarding                    BOOLEAN         NULL,
    abd_rigidity                    BOOLEAN         NULL,
    abd_organomegaly                VARCHAR(255)    NULL,
    abd_percussion_dullness         BOOLEAN         NULL,
    abd_tympanic                    BOOLEAN         NULL,
    abd_bowel_sounds                VARCHAR(15)     NULL,
    abd_notes                       VARCHAR(500)    NULL,

    dre_fissures                    BOOLEAN         NULL,
    dre_fistula                     BOOLEAN         NULL,
    dre_external_piles              BOOLEAN         NULL,
    dre_sphincter_tone              VARCHAR(100)    NULL,
    dre_mass_present                BOOLEAN         NULL,
    dre_mass_description            VARCHAR(255)    NULL,
    dre_blood_on_finger             BOOLEAN         NULL,
    dre_proctoscopy_performed       BOOLEAN         NULL,
    dre_proctoscopy_findings        VARCHAR(500)    NULL,

    jaundice_icterus_sclera         BOOLEAN         NULL,
    jaundice_icterus_skin           BOOLEAN         NULL,
    jaundice_icterus_palmar         BOOLEAN         NULL,
    jaundice_scratch_marks_present  BOOLEAN         NULL,

    hernia_present                  BOOLEAN         NULL,
    hernia_site                     VARCHAR(255)    NULL,
    hernia_reducible                BOOLEAN         NULL,
    hernia_cough_impulse            BOOLEAN         NULL,

    lymph_cervical_palpable         BOOLEAN         NULL,
    lymph_supraclavicular_palpable  BOOLEAN         NULL,
    lymph_inguinal_palpable         BOOLEAN         NULL,
    lymph_notes                     VARCHAR(500)    NULL,

    gi_mass_present                 BOOLEAN         NULL,
    gi_mass_location                VARCHAR(255)    NULL,
    gi_mass_size_cm                 DOUBLE          NULL,
    gi_mass_mobility                VARCHAR(10)     NULL,
    gi_mass_consistency             VARCHAR(10)     NULL,

    ascites_shifting_dullness       BOOLEAN         NULL,
    ascites_fluid_thrill            BOOLEAN         NULL,
    ascites_notes                   VARCHAR(500)    NULL,

    sys_chest_expansion             VARCHAR(255)    NULL,
    sys_breath_sounds               VARCHAR(255)    NULL,
    sys_heart_sounds                VARCHAR(255)    NULL,
    sys_murmurs_present             BOOLEAN         NULL,
    sys_murmur_description          VARCHAR(255)    NULL,
    sys_jvp                         VARCHAR(255)    NULL,
    sys_gcs_score                   INT             NULL,
    sys_pupillary_reflex            VARCHAR(10)     NULL,
    sys_motor_findings              VARCHAR(500)    NULL,
    sys_sensory_findings            VARCHAR(500)    NULL,

    abdominal_girth_cm              DOUBLE          NULL,

    created_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                      VARCHAR(100)    NULL,
    updated_by                      VARCHAR(100)    NULL,

    CONSTRAINT fk_clinical_examinations_patient FOREIGN KEY (patient_upid) REFERENCES patients (upid)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_clinical_examinations_visit_id ON clinical_examinations (visit_id);
CREATE INDEX idx_clinical_examinations_admission_id ON clinical_examinations (admission_id);
CREATE INDEX idx_clinical_examinations_patient_upid ON clinical_examinations (patient_upid);
