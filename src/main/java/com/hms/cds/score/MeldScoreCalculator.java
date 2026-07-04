package com.hms.cds.score;

import com.hms.cds.dto.MeldScoreRequest;
import com.hms.cds.dto.MeldScoreResponse;
import org.springframework.stereotype.Component;

@Component
public class MeldScoreCalculator {

    public MeldScoreResponse calculate(MeldScoreRequest request) {
        double bilirubin = Math.max(request.bilirubinMgDl(), 1.0);
        double inr = Math.max(request.inr(), 1.0);
        double creatinine = Math.min(Math.max(request.creatinineMgDl(), 1.0), 4.0);

        double meldRaw = 3.78 * Math.log(bilirubin) + 11.2 * Math.log(inr) + 9.57 * Math.log(creatinine) + 6.43;

        double meldNaRaw = meldRaw;
        if (meldRaw > 11) {
            double sodium = Math.min(Math.max(request.sodiumMeqL(), 125), 137);
            meldNaRaw = meldRaw + 1.32 * (137 - sodium) - (0.033 * meldRaw * (137 - sodium));
        }

        int meld = clamp((int) Math.round(meldRaw));
        int meldNa = clamp((int) Math.round(meldNaRaw));

        return new MeldScoreResponse(meld, meldNa, interpretationFor(meldNa));
    }

    private int clamp(int score) {
        return Math.min(Math.max(score, 6), 40);
    }

    private String interpretationFor(int meldNaScore) {
        if (meldNaScore < 10) {
            return "3-month mortality risk approximately 1.9% (low risk)";
        } else if (meldNaScore < 20) {
            return "3-month mortality risk approximately 6-20% (moderate risk)";
        } else if (meldNaScore < 30) {
            return "3-month mortality risk approximately 20-53% (high risk)";
        } else if (meldNaScore < 40) {
            return "3-month mortality risk approximately 53-71% (very high risk)";
        }
        return "3-month mortality risk greater than 71% (extremely high risk)";
    }
}
