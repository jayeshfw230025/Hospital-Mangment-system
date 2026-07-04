package com.hms.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(
        @NotBlank String txnId,
        @NotBlank String otp) {
}
