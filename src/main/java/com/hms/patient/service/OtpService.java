package com.hms.patient.service;

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
 * In-memory OTP transaction store for the ABHA link/verify flow. A placeholder
 * until the Redis session-store stage is implemented (Stage A caching layer) and
 * until real ABDM/SMS gateway integration replaces the log-based OTP delivery below.
 */
@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    private final Map<String, OtpTransaction> transactions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public String initiate(String upid, String abhaNumber) {
        String txnId = UUID.randomUUID().toString();
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        transactions.put(txnId, new OtpTransaction(upid, abhaNumber, otp, Instant.now().plus(OTP_TTL)));

        log.info("ABHA link OTP generated for UPID={}, txnId={}, otp={} (dev stub - replace with ABDM SMS gateway)",
                upid, txnId, otp);

        return txnId;
    }

    public String currentOtp(String txnId) {
        OtpTransaction transaction = transactions.get(txnId);
        return transaction == null ? null : transaction.otp();
    }

    public OtpTransaction verify(String txnId, String otp) {
        OtpTransaction transaction = transactions.get(txnId);
        if (transaction == null) {
            throw new IllegalArgumentException("Invalid or expired transaction ID");
        }
        if (Instant.now().isAfter(transaction.expiresAt())) {
            transactions.remove(txnId);
            throw new IllegalArgumentException("OTP has expired, please initiate ABHA linking again");
        }
        if (!transaction.otp().equals(otp)) {
            throw new IllegalArgumentException("Incorrect OTP");
        }
        transactions.remove(txnId);
        return transaction;
    }

    public record OtpTransaction(String upid, String abhaNumber, String otp, Instant expiresAt) {
    }
}
