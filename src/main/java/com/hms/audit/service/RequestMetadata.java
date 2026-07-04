package com.hms.audit.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

record RequestMetadata(String ipAddress, String sessionId, String deviceInfo) {

    private static final RequestMetadata EMPTY = new RequestMetadata(null, null, null);

    static RequestMetadata captureCurrent() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return EMPTY;
            }
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            }
            String sessionId = request.getSession(false) != null ? request.getSession(false).getId() : null;
            String deviceInfo = request.getHeader("User-Agent");
            return new RequestMetadata(ip, sessionId, deviceInfo);
        } catch (IllegalStateException e) {
            return EMPTY;
        }
    }
}
