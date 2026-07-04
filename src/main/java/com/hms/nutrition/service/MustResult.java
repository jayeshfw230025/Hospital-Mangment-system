package com.hms.nutrition.service;

import com.hms.nutrition.domain.MustRiskCategory;

public record MustResult(int bmiScore, int weightLossScore, int acuteDiseaseScore,
                          int totalScore, MustRiskCategory riskCategory) {
}
