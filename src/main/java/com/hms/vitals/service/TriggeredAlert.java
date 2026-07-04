package com.hms.vitals.service;

import com.hms.vitals.domain.VitalParameter;

public record TriggeredAlert(VitalParameter parameter, String measuredValue, String message) {
}
