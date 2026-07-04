package com.hms.ipd.procedure.domain;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProcedureDetailValidator {

    public void validate(ProcedureType procedureType, Map<String, Object> details) {
        Set<String> required = procedureType.getRequiredDetailKeys();
        if (required.isEmpty()) {
            return;
        }

        Map<String, Object> safeDetails = details == null ? Map.of() : details;
        Set<String> missing = required.stream()
                .filter(key -> isBlank(safeDetails.get(key)))
                .collect(Collectors.toSet());

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing required detail(s) for procedure type "
                    + procedureType.getLabel() + ": " + missing);
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
