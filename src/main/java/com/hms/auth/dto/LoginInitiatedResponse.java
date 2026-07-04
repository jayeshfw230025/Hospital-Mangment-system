package com.hms.auth.dto;

public record LoginInitiatedResponse(String txnId, boolean otpRequired, String message) {
}
