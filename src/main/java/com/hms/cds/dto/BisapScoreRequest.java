package com.hms.cds.dto;

public record BisapScoreRequest(
        boolean bunOver25,
        boolean impairedMentalStatus,
        boolean sirsPresent,
        boolean ageOver60,
        boolean pleuralEffusion
) {
}
