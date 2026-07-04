package com.hms.cds.score;

import com.hms.cds.dto.CdaiScoreRequest;
import com.hms.cds.dto.CdaiScoreResponse;
import org.springframework.stereotype.Component;

/**
 * Crohn's Disease Activity Index, computed from exactly the 4 factors Module 9
 * lists (stool frequency, abdominal pain, well-being, extraintestinal
 * manifestations), using their official weights. The full CDAI also factors in
 * antidiarrheal use, abdominal mass, hematocrit deviation and body-weight
 * deviation - omitted here since the spec didn't call them out as parameters.
 */
@Component
public class CdaiScoreCalculator {

    public CdaiScoreResponse calculate(CdaiScoreRequest request) {
        int score = 2 * request.stoolFrequencySum()
                + 5 * request.abdominalPainSum()
                + 7 * request.wellBeingSum()
                + 20 * request.extraintestinalManifestationsCount();

        String activity;
        String interpretation;
        if (score < 150) {
            activity = "Remission";
            interpretation = "Score <150: clinical remission";
        } else if (score <= 450) {
            activity = "Active";
            interpretation = "Score 150-450: active disease (mild to moderate-severe)";
        } else {
            activity = "Severe";
            interpretation = "Score >450: severe/fulminant disease";
        }

        return new CdaiScoreResponse(score, activity, interpretation);
    }
}
