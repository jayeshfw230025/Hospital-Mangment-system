package com.hms.analytics.dto;

import com.hms.analytics.domain.Granularity;

import java.util.List;

public record PatientVolumeResponse(Granularity granularity, List<PatientVolumeBucket> series) {
}
