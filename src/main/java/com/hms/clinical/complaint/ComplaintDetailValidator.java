package com.hms.clinical.complaint;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ComplaintDetailValidator {

    public void validate(ComplaintType complaintType, Map<String, Object> details) {
        Set<String> required = complaintType.getRequiredDetailKeys();
        if (required.isEmpty()) {
            return;
        }

        Map<String, Object> safeDetails = details == null ? Map.of() : details;
        Set<String> missing = required.stream()
                .filter(key -> isBlank(safeDetails.get(key)))
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing required detail(s) for complaint type "
                    + complaintType.getLabel() + ": " + missing);
        }
    }

    private boolean isBlank(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String s) {
            return s.isBlank();
        }
        return false;
    }
}
