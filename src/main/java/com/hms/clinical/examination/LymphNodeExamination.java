package com.hms.clinical.examination;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class LymphNodeExamination {

    @Column(name = "lymph_cervical_palpable")
    private Boolean cervicalNodesPalpable;

    @Column(name = "lymph_supraclavicular_palpable")
    private Boolean supraclavicularNodesPalpable;

    @Column(name = "lymph_inguinal_palpable")
    private Boolean inguinalNodesPalpable;

    @Column(name = "lymph_notes", length = 500)
    private String notes;
}
