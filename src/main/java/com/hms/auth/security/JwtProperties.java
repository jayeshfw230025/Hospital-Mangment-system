package com.hms.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hms.jwt")
public record JwtProperties(String secretKey, long accessTokenTtlSeconds, long refreshTokenTtlSeconds) {
}
