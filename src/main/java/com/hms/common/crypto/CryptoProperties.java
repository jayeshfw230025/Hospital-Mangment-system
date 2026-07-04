package com.hms.common.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hms.crypto")
public record CryptoProperties(String secretKey) {
}
