package com.hms.auth.service;

import com.hms.audit.domain.AuditAction;
import com.hms.audit.service.AuditLogService;
import com.hms.auth.domain.RefreshToken;
import com.hms.auth.domain.User;
import com.hms.auth.dto.LoginInitiatedResponse;
import com.hms.auth.dto.LoginRequest;
import com.hms.auth.dto.MeResponse;
import com.hms.auth.dto.OtpVerifyRequest;
import com.hms.auth.dto.RefreshTokenRequest;
import com.hms.auth.dto.TokenResponse;
import com.hms.auth.repository.RefreshTokenRepository;
import com.hms.auth.repository.UserRepository;
import com.hms.auth.security.AuthenticatedUser;
import com.hms.auth.security.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginOtpService loginOtpService;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService, LoginOtpService loginOtpService,
                        AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginOtpService = loginOtpService;
        this.auditLogService = auditLogService;
    }

    public LoginInitiatedResponse initiateLogin(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        String txnId = loginOtpService.initiate(user.getUsername());
        return new LoginInitiatedResponse(txnId, true, "OTP sent to registered contact");
    }

    @Transactional
    public TokenResponse verifyOtpAndIssueTokens(OtpVerifyRequest request) {
        LoginOtpService.OtpTransaction txn = loginOtpService.verify(request.txnId(), request.otp());
        User user = userRepository.findByUsername(txn.username())
                .orElseThrow(() -> new IllegalStateException("User no longer exists"));

        TokenResponse tokens = issueTokens(user);
        auditLogService.record(user.getId(), user.getUsername(), user.getRole(), AuditAction.LOGIN,
                "auth", user.getId().toString(), null, null, null);
        return tokens;
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash(request.refreshToken()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (existing.isRevoked() || Instant.now().isAfter(existing.getExpiresAt())) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        User user = userRepository.findById(existing.getUserId())
                .orElseThrow(() -> new IllegalStateException("User no longer exists"));
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenHash(hash(request.refreshToken())).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            userRepository.findById(rt.getUserId()).ifPresent(user ->
                    auditLogService.record(user.getId(), user.getUsername(), user.getRole(), AuditAction.LOGOUT,
                            "auth", user.getId().toString(), null, null, null));
        });
    }

    public MeResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new IllegalStateException("Not authenticated");
        }
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new IllegalStateException("User no longer exists"));
        return new MeResponse(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole());
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name());
        String rawRefreshToken = UUID.randomUUID() + UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hash(rawRefreshToken))
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTokenTtlSeconds()))
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(accessToken, rawRefreshToken, "Bearer", jwtService.getAccessTokenTtlSeconds());
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
