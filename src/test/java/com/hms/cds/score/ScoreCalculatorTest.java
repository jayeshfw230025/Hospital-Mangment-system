package com.hms.cds.score;

import com.hms.cds.dto.BisapScoreRequest;
import com.hms.cds.dto.CdaiScoreRequest;
import com.hms.cds.dto.CtpScoreRequest;
import com.hms.cds.dto.MayoScoreRequest;
import com.hms.cds.dto.MeldScoreRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ScoreCalculatorTest {

    @Autowired
    private CtpScoreCalculator ctpScoreCalculator;

    @Autowired
    private MeldScoreCalculator meldScoreCalculator;

    @Autowired
    private MayoScoreCalculator mayoScoreCalculator;

    @Autowired
    private BisapScoreCalculator bisapScoreCalculator;

    @Autowired
    private CdaiScoreCalculator cdaiScoreCalculator;

    @Test
    void ctpScoreClassifiesAsClassA() {
        var response = ctpScoreCalculator.calculate(new CtpScoreRequest(
                AscitesGrade.NONE, EncephalopathyGrade.NONE, 1.5, 3.8, 1.2));

        assertThat(response.totalScore()).isEqualTo(5);
        assertThat(response.ctpClass()).isEqualTo("A");
    }

    @Test
    void ctpScoreClassifiesAsClassC() {
        var response = ctpScoreCalculator.calculate(new CtpScoreRequest(
                AscitesGrade.MODERATE_SEVERE, EncephalopathyGrade.GRADE_3_4, 4.0, 2.0, 3.0));

        assertThat(response.totalScore()).isEqualTo(15);
        assertThat(response.ctpClass()).isEqualTo("C");
    }

    @Test
    void meldScoreComputesWithinExpectedRange() {
        var response = meldScoreCalculator.calculate(new MeldScoreRequest(2.0, 1.5, 1.5, 137.0));

        assertThat(response.meldScore()).isBetween(6, 40);
        assertThat(response.meldNaScore()).isBetween(6, 40);
    }

    @Test
    void meldScoreClampsToMinimumOfSix() {
        var response = meldScoreCalculator.calculate(new MeldScoreRequest(0.5, 0.8, 0.5, 140.0));

        assertThat(response.meldScore()).isEqualTo(6);
    }

    @Test
    void mayoScoreClassifiesModerateDisease() {
        var response = mayoScoreCalculator.calculate(new MayoScoreRequest(2, 2, 2, 2));

        assertThat(response.totalScore()).isEqualTo(8);
        assertThat(response.diseaseActivity()).isEqualTo("Moderate");
    }

    @Test
    void bisapScoreIdentifiesHighRisk() {
        var response = bisapScoreCalculator.calculate(new BisapScoreRequest(true, true, true, false, false));

        assertThat(response.totalScore()).isEqualTo(3);
        assertThat(response.mortalityRiskCategory()).isEqualTo("High risk");
    }

    @Test
    void bisapScoreIdentifiesLowRisk() {
        var response = bisapScoreCalculator.calculate(new BisapScoreRequest(false, false, true, false, false));

        assertThat(response.totalScore()).isEqualTo(1);
        assertThat(response.mortalityRiskCategory()).isEqualTo("Low risk");
    }

    @Test
    void cdaiScoreClassifiesActiveDisease() {
        var response = cdaiScoreCalculator.calculate(new CdaiScoreRequest(50, 3, 3, 2));

        assertThat(response.totalScore()).isEqualTo(2 * 50 + 5 * 3 + 7 * 3 + 20 * 2);
        assertThat(response.diseaseActivity()).isEqualTo("Active");
    }

    @Test
    void cdaiScoreClassifiesRemission() {
        var response = cdaiScoreCalculator.calculate(new CdaiScoreRequest(0, 0, 0, 0));

        assertThat(response.totalScore()).isEqualTo(0);
        assertThat(response.diseaseActivity()).isEqualTo("Remission");
    }
}
