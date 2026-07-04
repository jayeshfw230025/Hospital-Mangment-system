package com.hms.cds.score;

import com.hms.cds.dto.MayoScoreRequest;
import com.hms.cds.dto.MayoScoreResponse;
import org.springframework.stereotype.Component;

@Component
public class MayoScoreCalculator {

    public MayoScoreResponse calculate(MayoScoreRequest request) {
        int total = request.stoolFrequencySubscore() + request.rectalBleedingSubscore()
                + request.endoscopySubscore() + request.physicianGlobalAssessmentSubscore();

        String activity;
        String interpretation;
        if (total <= 2) {
            activity = "Remission";
            interpretation = "Score 0-2: disease in remission";
        } else if (total <= 5) {
            activity = "Mild";
            interpretation = "Score 3-5: mildly active disease";
        } else if (total <= 10) {
            activity = "Moderate";
            interpretation = "Score 6-10: moderately active disease";
        } else {
            activity = "Severe";
            interpretation = "Score 11-12: severely active disease";
        }

        return new MayoScoreResponse(total, activity, interpretation);
    }
}
