package com.hms.ipd.procedure.dto;

import java.util.Set;

public record ProcedureTypeResponse(String name, String label, Set<String> requiredDetailKeys) {
}
