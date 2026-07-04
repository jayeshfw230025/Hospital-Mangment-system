package com.hms.auth.security;

import com.hms.auth.domain.Role;

public record AuthenticatedUser(Long userId, String username, Role role) {
}
