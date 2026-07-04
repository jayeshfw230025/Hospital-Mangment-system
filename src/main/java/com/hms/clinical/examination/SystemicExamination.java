package com.hms.clinical.examination;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SystemicExamination {

    @Column(name = "sys_chest_expansion")
    private String chestExpansion;

    @Column(name = "sys_breath_sounds")
    private String breathSounds;

    @Column(name = "sys_heart_sounds")
    private String heartSounds;

    @Column(name = "sys_murmurs_present")
    private Boolean murmursPresent;

    @Column(name = "sys_murmur_description")
    private String murmurDescription;

    @Column(name = "sys_jvp")
    private String jvp;

    @Column(name = "sys_gcs_score")
    private Integer gcsScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "sys_pupillary_reflex", length = 10)
    private PupillaryReflex pupillaryReflex;

    @Column(name = "sys_motor_findings", length = 500)
    private String motorFindings;

    @Column(name = "sys_sensory_findings", length = 500)
    private String sensoryFindings;
}
