package com.hms.analytics.dto;

import java.util.List;

public record ReferralPatternResponse(List<NamedCount> topReferringDoctors,
                                       List<NamedCount> topReferringHospitals,
                                       List<NamedCount> admissionSourceDistribution) {
}
