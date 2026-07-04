package com.hms.common.audit;

import com.hms.auth.security.AuthenticatedUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Falls back to "system" when there is no authenticated request context (e.g. a
 * service-layer test calling a service bean directly, or a startup seeder) so that
 * existing service-level tests continue to work unchanged now that real
 * authentication exists.
 */
@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String SYSTEM_USER = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user.username());
        }
        return Optional.of(SYSTEM_USER);
    }
}
