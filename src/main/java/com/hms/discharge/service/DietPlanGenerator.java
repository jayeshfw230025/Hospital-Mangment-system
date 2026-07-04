package com.hms.discharge.service;

import com.hms.nutrition.domain.DiseaseCategory;
import com.hms.nutrition.service.NutritionCalculatorService;
import com.hms.nutrition.service.NutritionTargets;
import org.springframework.stereotype.Component;

/**
 * Auto-generates the discharge diet plan text from Module 13's disease-specific
 * targets, adding a food-avoid list per category (new domain knowledge for this
 * module, since Module 13 doesn't need a food list for its own screening purpose).
 */
@Component
public class DietPlanGenerator {

    private final NutritionCalculatorService nutritionCalculatorService;

    public DietPlanGenerator(NutritionCalculatorService nutritionCalculatorService) {
        this.nutritionCalculatorService = nutritionCalculatorService;
    }

    public String generate(DiseaseCategory diseaseCategory, double weightKg) {
        NutritionTargets targets = nutritionCalculatorService.calculateTargets(diseaseCategory, weightKg);

        return String.format(
                "Caloric target: %.0f-%.0f kcal/day. Protein target: %.0f-%.0f g/day. "
                        + "Fluid intake: approximately %.0f mL/day. %s",
                targets.caloricTargetMinKcalPerDay(), targets.caloricTargetMaxKcalPerDay(),
                targets.proteinTargetMinGPerDay(), targets.proteinTargetMaxGPerDay(),
                targets.fluidRequirementMlPerDay(),
                foodAvoidList(diseaseCategory));
    }

    private String foodAvoidList(DiseaseCategory category) {
        return switch (category) {
            case CIRRHOSIS -> "Avoid: added salt, canned/processed foods, alcohol, raw shellfish.";
            case ACUTE_PANCREATITIS -> "Avoid: fried/fatty foods, alcohol, red meat; advance diet as tolerated.";
            case IBD -> "Avoid: high-fibre raw vegetables during flares, dairy if lactose intolerant, spicy/fatty foods.";
            case GI_CANCER -> "Avoid: alcohol, tobacco, heavily processed or smoked foods.";
            case LIVER_TRANSPLANT -> "Avoid: raw/undercooked food (infection risk), grapefruit (drug interaction), excess salt.";
        };
    }
}
