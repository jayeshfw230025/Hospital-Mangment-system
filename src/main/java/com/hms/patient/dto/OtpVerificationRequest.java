package com.hms.patient.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerificationRequest(

        @NotBlank(message = "Transaction ID is required")
        String txnId,

        @NotBlank(message = "OTP is required")
        String otp
) {
}
