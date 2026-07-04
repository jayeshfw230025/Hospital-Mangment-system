package com.hms.cds.score;

import com.hms.cds.dto.CtpScoreRequest;
import com.hms.cds.dto.CtpScoreResponse;
import org.springframework.stereotype.Component;

@Component
public class CtpScoreCalculator {

    public CtpScoreResponse calculate(CtpScoreRequest request) {
        int score = ascitesPoints(request.ascites())
                + encephalopathyPoints(request.encephalopathy())
                + bilirubinPoints(request.bilirubinMgDl())
                + albuminPoints(request.albuminGDl())
                + inrPoints(request.inr());

        String ctpClass;
        String interpretation;
        if (score <= 6) {
            ctpClass = "A";
            interpretation = "Class A (5-6 points): compensated liver disease, best prognosis, ~100% 1-year survival";
        } else if (score <= 9) {
            ctpClass = "B";
            interpretation = "Class B (7-9 points): significant functional compromise, ~80% 1-year survival";
        } else {
            ctpClass = "C";
            interpretation = "Class C (10-15 points): decompensated liver disease, poor prognosis, ~45% 1-year survival";
        }

        return new CtpScoreResponse(score, ctpClass, interpretation);
    }

    private int ascitesPoints(AscitesGrade ascites) {
        return switch (ascites) {
            case NONE -> 1;
            case MILD -> 2;
            case MODERATE_SEVERE -> 3;
        };
    }

    private int encephalopathyPoints(EncephalopathyGrade encephalopathy) {
        return switch (encephalopathy) {
            case NONE -> 1;
            case GRADE_1_2 -> 2;
            case GRADE_3_4 -> 3;
        };
    }

    private int bilirubinPoints(double bilirubinMgDl) {
        if (bilirubinMgDl < 2) {
            return 1;
        }
        return bilirubinMgDl <= 3 ? 2 : 3;
    }

    private int albuminPoints(double albuminGDl) {
        if (albuminGDl > 3.5) {
            return 1;
        }
        return albuminGDl >= 2.8 ? 2 : 3;
    }

    private int inrPoints(double inr) {
        if (inr < 1.7) {
            return 1;
        }
        return inr <= 2.3 ? 2 : 3;
    }
}
