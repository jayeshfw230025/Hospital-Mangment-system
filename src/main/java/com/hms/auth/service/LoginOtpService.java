package com.hms.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory OTP transaction store for the login flow, mirroring
 * {@link com.hms.patient.service.OtpService}. A placeholder until a real
 * SMS/email OTP gateway is integrated - logs the OTP instead of sending it.
 */
@Service
public class LoginOtpService {

    private static final Logger log = LoggerFactory.getLogger(LoginOtpService.class);
    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    private final Map<String, OtpTransaction> transactions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public String initiate(String username) {
        String txnId = UUID.randomUUID().toString();
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        transactions.put(txnId, new OtpTransaction(username, otp, Instant.now().plus(OTP_TTL)));

        log.info("Login OTP generated for username={}, txnId={}, otp={} (dev stub - replace with SMS/email gateway)",
                username, txnId, otp);

        return txnId;
    }

    public String currentOtp(String txnId) {
        OtpTransaction transaction = transactions.get(txnId);
        return transaction == null ? null : transaction.otp();
    }

    public OtpTransaction verify(String txnId, String otp) {
        OtpTransaction transaction = transactions.get(txnId);
        if (transaction == null) {
            throw new IllegalArgumentException("Invalid or expired login transaction");
        }
        if (Instant.now().isAfter(transaction.expiresAt())) {
            transactions.remove(txnId);
            throw new IllegalArgumentException("OTP has expired, please login again");
        }
        if (!transaction.otp().equals(otp)) {
            throw new IllegalArgumentException("Incorrect OTP");
        }
        transactions.remove(txnId);
        return transaction;
    }

    public record OtpTransaction(String username, String otp, Instant expiresAt) {
    }
}
