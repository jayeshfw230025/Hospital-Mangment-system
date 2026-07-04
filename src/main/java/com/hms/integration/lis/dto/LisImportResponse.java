package com.hms.integration.lis.dto;

import com.hms.investigation.dto.InvestigationReportResponse;

public record LisImportResponse(String message, InvestigationReportResponse report) {
}
