package com.hms.nutrition.service;

public record NrsResult(int nutritionalStatusScore, int diseaseSeverityScore, int ageAdjustment,
                         int totalScore, boolean atRisk) {
}
