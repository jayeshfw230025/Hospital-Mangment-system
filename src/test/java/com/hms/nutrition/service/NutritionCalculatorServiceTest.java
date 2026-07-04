package com.hms.nutrition.service;

import com.hms.nutrition.domain.DiseaseCategory;
import com.hms.nutrition.domain.MustRiskCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@SpringBootTest
@ActiveProfiles("test")
class NutritionCalculatorServiceTest {

    @Autowired
    private NutritionCalculatorService calculator;

    @Test
    void nrsScoresLowRiskPatientAsNotAtRisk() {
        NrsResult result = calculator.calculateNrs(2.0, 22.0, 90.0, 0, 40);

        assertThat(result.totalScore()).isEqualTo(0);
        assertThat(result.atRisk()).isFalse();
    }

    @Test
    void nrsScoresSevereWeightLossElderlyPatientAsAtRisk() {
        NrsResult result = calculator.calculateNrs(16.0, 22.0, 90.0, 1, 75);

        assertThat(result.nutritionalStatusScore()).isEqualTo(3);
        assertThat(result.ageAdjustment()).isEqualTo(1);
        assertThat(result.totalScore()).isEqualTo(5);
        assertThat(result.atRisk()).isTrue();
    }

    @Test
    void nrsTakesWorstOfTheThreeImpairmentParameters() {
        // dietary intake alone is severely impaired even though weight loss and BMI are normal
        NrsResult result = calculator.calculateNrs(1.0, 23.0, 20.0, 0, 30);

        assertThat(result.nutritionalStatusScore()).isEqualTo(3);
    }

    @Test
    void mustClassifiesHighRisk() {
        MustResult result = calculator.calculateMust(17.0, 12.0, true);

        assertThat(result.totalScore()).isEqualTo(6);
        assertThat(result.riskCategory()).isEqualTo(MustRiskCategory.HIGH);
    }

    @Test
    void mustClassifiesLowRisk() {
        MustResult result = calculator.calculateMust(21.0, 2.0, false);

        assertThat(result.totalScore()).isEqualTo(0);
        assertThat(result.riskCategory()).isEqualTo(MustRiskCategory.LOW);
    }

    @Test
    void mustClassifiesMediumRisk() {
        MustResult result = calculator.calculateMust(19.0, 2.0, false);

        assertThat(result.totalScore()).isEqualTo(1);
        assertThat(result.riskCategory()).isEqualTo(MustRiskCategory.MEDIUM);
    }

    @Test
    void calculatesCirrhosisTargetsForSeventyKgPatient() {
        NutritionTargets targets = calculator.calculateTargets(DiseaseCategory.CIRRHOSIS, 70.0);

        assertThat(targets.caloricTargetMinKcalPerDay()).isCloseTo(1750.0, offset(0.01));
        assertThat(targets.caloricTargetMaxKcalPerDay()).isCloseTo(2450.0, offset(0.01));
        assertThat(targets.proteinTargetMinGPerDay()).isCloseTo(84.0, offset(0.01));
        assertThat(targets.proteinTargetMaxGPerDay()).isCloseTo(105.0, offset(0.01));
        assertThat(targets.fluidRequirementMlPerDay()).isCloseTo(2100.0, offset(0.01));
    }

    @Test
    void calculatesLiverTransplantTargetsWithHigherProteinRange() {
        NutritionTargets targets = calculator.calculateTargets(DiseaseCategory.LIVER_TRANSPLANT, 70.0);

        assertThat(targets.proteinTargetMinGPerDay()).isCloseTo(105.0, offset(0.01));
        assertThat(targets.proteinTargetMaxGPerDay()).isCloseTo(140.0, offset(0.01));
    }

    @Test
    void calculatesAcutePancreatitisAndGiCancerWithSameCaloricRange() {
        NutritionTargets pancreatitis = calculator.calculateTargets(DiseaseCategory.ACUTE_PANCREATITIS, 60.0);
        NutritionTargets giCancer = calculator.calculateTargets(DiseaseCategory.GI_CANCER, 60.0);

        assertThat(pancreatitis.caloricTargetMinKcalPerDay()).isEqualTo(giCancer.caloricTargetMinKcalPerDay());
        assertThat(pancreatitis.caloricTargetMaxKcalPerDay()).isEqualTo(giCancer.caloricTargetMaxKcalPerDay());
    }
}
