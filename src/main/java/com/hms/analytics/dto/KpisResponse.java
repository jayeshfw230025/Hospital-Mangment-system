package com.hms.analytics.dto;

import java.util.List;

public record KpisResponse(
        PatientVolumeResponse patientVolume,
        List<DiseaseDistributionEntry> topGiDiseases,
        OpdIpdRatioResponse opdIpdRatio,
        List<AlosEntry> alosByDiagnosis,
        ReadmissionRateResponse readmissionRate,
        List<ProcedureStatEntry> procedureStats,
        List<MortalityEntry> mortalityByDiagnosis,
        PatientSatisfactionResponse patientSatisfaction,
        ReferralPatternResponse referralPattern
) {
}
