package com.hms.auth.web;

import com.hms.auth.dto.LoginInitiatedResponse;
import com.hms.auth.dto.LoginRequest;
import com.hms.auth.dto.MeResponse;
import com.hms.auth.dto.OtpVerifyRequest;
import com.hms.auth.dto.RefreshTokenRequest;
import com.hms.auth.dto.TokenResponse;
import com.hms.auth.service.AuthService;
import com.hms.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "JWT login with OTP verification, refresh, logout")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Step 1: verify username/password, triggers an OTP")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginInitiatedResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.initiateLogin(request)));
    }

    @Operation(summary = "Step 2: verify the OTP, issues access + refresh tokens")
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<TokenResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.verifyOtpAndIssueTokens(request)));
    }

    @Operation(summary = "Exchange a valid refresh token for a new access token")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request)));
    }

    @Operation(summary = "Revoke a refresh token, ending the session")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.ok("Logged out", null));
    }

    @Operation(summary = "Current authenticated user's profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me() {
        return ResponseEntity.ok(ApiResponse.ok(authService.me()));
    }
}
