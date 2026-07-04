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
public class AbdominalExamination {

    @Column(name = "abd_scars_present")
    private Boolean scarsPresent;

    @Column(name = "abd_distension_present")
    private Boolean distensionPresent;

    @Column(name = "abd_visible_peristalsis")
    private Boolean visiblePeristalsis;

    @Column(name = "abd_tenderness")
    private Boolean tenderness;

    @Column(name = "abd_tenderness_site")
    private String tendernessSite;

    @Column(name = "abd_guarding")
    private Boolean guarding;

    @Column(name = "abd_rigidity")
    private Boolean rigidity;

    @Column(name = "abd_organomegaly")
    private String organomegaly;

    @Column(name = "abd_percussion_dullness")
    private Boolean percussionDullness;

    @Column(name = "abd_tympanic")
    private Boolean tympanic;

    @Enumerated(EnumType.STRING)
    @Column(name = "abd_bowel_sounds", length = 15)
    private BowelSounds bowelSounds;

    @Column(name = "abd_notes", length = 500)
    private String notes;
}
