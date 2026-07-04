package com.hms.cds.score;

import com.hms.cds.dto.BisapScoreRequest;
import com.hms.cds.dto.BisapScoreResponse;
import org.springframework.stereotype.Component;

@Component
public class BisapScoreCalculator {

    public BisapScoreResponse calculate(BisapScoreRequest request) {
        int score = (request.bunOver25() ? 1 : 0)
                + (request.impairedMentalStatus() ? 1 : 0)
                + (request.sirsPresent() ? 1 : 0)
                + (request.ageOver60() ? 1 : 0)
                + (request.pleuralEffusion() ? 1 : 0);

        String category = score <= 2 ? "Low risk" : "High risk";
        String interpretation = score <= 2
                ? "Score 0-2: mortality risk <2%"
                : "Score 3-5: mortality risk >15%, consider ICU-level monitoring";

        return new BisapScoreResponse(score, category, interpretation);
    }
}
