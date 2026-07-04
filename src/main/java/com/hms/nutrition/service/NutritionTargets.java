package com.hms.nutrition.service;

public record NutritionTargets(double caloricTargetMinKcalPerDay, double caloricTargetMaxKcalPerDay,
                                double proteinTargetMinGPerDay, double proteinTargetMaxGPerDay,
                                double fluidRequirementMlPerDay) {
}
