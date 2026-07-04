package com.hms.nutrition.service;

import com.hms.nutrition.domain.DiseaseCategory;
import com.hms.nutrition.domain.MustRiskCategory;
import org.springframework.stereotype.Service;

/**
 * Implements the actual NRS-2002 and MUST screening algorithms (not just
 * pass-through fields) plus disease-specific caloric/protein targets per
 * Module 13's tables. Standard fluid maintenance (30 mL/kg/day) is used as the
 * default recommendation since, unlike calories/protein, the spec gives a single
 * generic field rather than disease-specific ranges.
 */
@Service
public class NutritionCalculatorService {

    private static final double DEFAULT_FLUID_ML_PER_KG_PER_DAY = 30.0;

    public NrsResult calculateNrs(Double weightLossPercent, Double bmi, Double dietaryIntakePercent,
                                   int diseaseSeverityScore, Integer age) {
        int nutritionalStatusScore = Math.max(
                weightLossImpairmentScore(weightLossPercent),
                Math.max(bmiImpairmentScore(bmi), dietaryIntakeImpairmentScore(dietaryIntakePercent)));

        int ageAdjustment = (age != null && age >= 70) ? 1 : 0;
        int total = nutritionalStatusScore + diseaseSeverityScore + ageAdjustment;

        return new NrsResult(nutritionalStatusScore, diseaseSeverityScore, ageAdjustment, total, total >= 3);
    }

    public MustResult calculateMust(Double bmi, Double weightLossPercent, boolean acuteDiseaseEffect) {
        int bmiScore = mustBmiScore(bmi);
        int weightLossScore = mustWeightLossScore(weightLossPercent);
        int acuteDiseaseScore = acuteDiseaseEffect ? 2 : 0;
        int total = bmiScore + weightLossScore + acuteDiseaseScore;

        MustRiskCategory category = total == 0 ? MustRiskCategory.LOW
                : total == 1 ? MustRiskCategory.MEDIUM
                : MustRiskCategory.HIGH;

        return new MustResult(bmiScore, weightLossScore, acuteDiseaseScore, total, category);
    }

    public NutritionTargets calculateTargets(DiseaseCategory diseaseCategory, double weightKg) {
        CaloricProteinRange range = rangeFor(diseaseCategory);
        return new NutritionTargets(
                round(weightKg * range.caloricMin()),
                round(weightKg * range.caloricMax()),
                round(weightKg * range.proteinMin()),
                round(weightKg * range.proteinMax()),
                round(weightKg * DEFAULT_FLUID_ML_PER_KG_PER_DAY)
        );
    }

    private int weightLossImpairmentScore(Double weightLossPercent) {
        if (weightLossPercent == null) {
            return 0;
        }
        if (weightLossPercent >= 15) {
            return 3;
        }
        if (weightLossPercent >= 10) {
            return 2;
        }
        if (weightLossPercent >= 5) {
            return 1;
        }
        return 0;
    }

    private int bmiImpairmentScore(Double bmi) {
        if (bmi == null) {
            return 0;
        }
        if (bmi < 18.5) {
            return 3;
        }
        if (bmi <= 20.5) {
            return 2;
        }
        return 0;
    }

    private int dietaryIntakeImpairmentScore(Double dietaryIntakePercent) {
        if (dietaryIntakePercent == null) {
            return 0;
        }
        if (dietaryIntakePercent < 25) {
            return 3;
        }
        if (dietaryIntakePercent < 50) {
            return 2;
        }
        if (dietaryIntakePercent < 75) {
            return 1;
        }
        return 0;
    }

    private int mustBmiScore(Double bmi) {
        if (bmi == null) {
            return 0;
        }
        if (bmi > 20) {
            return 0;
        }
        if (bmi >= 18.5) {
            return 1;
        }
        return 2;
    }

    private int mustWeightLossScore(Double weightLossPercent) {
        if (weightLossPercent == null) {
            return 0;
        }
        if (weightLossPercent < 5) {
            return 0;
        }
        if (weightLossPercent <= 10) {
            return 1;
        }
        return 2;
    }

    private CaloricProteinRange rangeFor(DiseaseCategory category) {
        return switch (category) {
            case CIRRHOSIS -> new CaloricProteinRange(25, 35, 1.2, 1.5);
            case ACUTE_PANCREATITIS -> new CaloricProteinRange(25, 30, 1.2, 1.5);
            case IBD -> new CaloricProteinRange(30, 35, 1.2, 1.5);
            case GI_CANCER -> new CaloricProteinRange(25, 30, 1.2, 1.5);
            case LIVER_TRANSPLANT -> new CaloricProteinRange(30, 35, 1.5, 2.0);
        };
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record CaloricProteinRange(double caloricMin, double caloricMax, double proteinMin, double proteinMax) {
    }
}
