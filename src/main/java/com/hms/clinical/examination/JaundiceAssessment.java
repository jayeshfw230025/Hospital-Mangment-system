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
public class JaundiceAssessment {

    @Column(name = "jaundice_icterus_sclera")
    private Boolean icterusSclera;

    @Column(name = "jaundice_icterus_skin")
    private Boolean icterusSkin;

    @Column(name = "jaundice_icterus_palmar")
    private Boolean icterusPalmar;

    @Column(name = "jaundice_scratch_marks_present")
    private Boolean scratchMarksPresent;
}
