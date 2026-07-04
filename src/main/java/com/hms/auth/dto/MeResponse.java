package com.hms.auth.dto;

import com.hms.auth.domain.Role;

public record MeResponse(Long userId, String username, String fullName, String email, Role role) {
}
