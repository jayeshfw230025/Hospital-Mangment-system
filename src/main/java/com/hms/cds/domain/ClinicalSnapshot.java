package com.hms.cds.domain;

import com.hms.clinical.complaint.ComplaintType;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class ClinicalSnapshot {

    private final Integer ageYears;
    @Builder.Default
    private final Set<ComplaintType> complaints = Set.of();

    private final Integer systolicBp;
    private final Integer heartRate;
    private final Double temperatureCelsius;
    private final Integer spo2;
    private final Integer gcsScore;

    private final boolean rigidityPresent;
    private final boolean ascitesPresent;
    private final boolean giMassPresent;
    private final boolean jaundiceExamPresent;
    private final boolean familyHistoryGiMalignancy;

    private final Double hemoglobinLatest;
    private final Double hemoglobinPrevious;

    private final Double creatinineLatest;
    private final Double creatininePrevious;
    private final Double creatinineReferenceHigh;

    private final Double plateletsLatest;
    private final Double plateletsReferenceLow;

    private final boolean ptOrInrAbnormal;
    private final boolean hPyloriPositive;
    private final boolean lftElevatedOver3x;

    @Builder.Default
    private final AdditionalFindings additionalFindings = AdditionalFindings.none();

    public boolean isJaundicePresent() {
        return jaundiceExamPresent || complaints.contains(ComplaintType.JAUNDICE);
    }

    public boolean isHypotensive() {
        return systolicBp != null && systolicBp < 90;
    }

    public boolean isTachycardic() {
        return heartRate != null && heartRate > 120;
    }

    public boolean isFeverPresent() {
        return complaints.contains(ComplaintType.FEVER) || (temperatureCelsius != null && temperatureCelsius > 38.0);
    }

    public boolean isAnemiaPresent() {
        return hemoglobinLatest != null && hemoglobinLatest < 12.0;
    }

    public boolean isCreatinineRising() {
        return creatinineLatest != null && creatininePrevious != null && creatinineReferenceHigh != null
                && creatinineLatest > creatininePrevious && creatinineLatest > creatinineReferenceHigh;
    }

    public boolean isHemoglobinDropping() {
        return hemoglobinLatest != null && hemoglobinPrevious != null
                && (hemoglobinPrevious - hemoglobinLatest) >= 2.0;
    }

    public boolean isPlateletsLow() {
        return plateletsLatest != null && plateletsReferenceLow != null && plateletsLatest < plateletsReferenceLow;
    }
}
