package com.hms.auth;

import com.hms.auth.domain.Role;
import com.hms.auth.domain.User;
import com.hms.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds one demo user per role on first startup (dev/test convenience - no user
 * management CRUD endpoints exist in the given spec, only the auth endpoints, so
 * there is no admin UI to create users yet). Default password for every seeded
 * account is "Passw0rd!23" - change/remove before any real deployment.
 */
@Component
public class DefaultUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserSeeder.class);
    private static final String DEFAULT_PASSWORD = "Passw0rd!23";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        for (Role role : Role.values()) {
            String username = role.name().toLowerCase() + ".demo";
            User user = User.builder()
                    .username(username)
                    .passwordHash(encodedPassword)
                    .fullName("Demo " + role.name().charAt(0) + role.name().substring(1).toLowerCase())
                    .email(username + "@hms.local")
                    .role(role)
                    .active(true)
                    .build();
            userRepository.save(user);
        }

        log.info("Seeded {} default users (one per role), username pattern '<role>.demo', password '{}' "
                + "- for dev/demo only, replace before production", Role.values().length, DEFAULT_PASSWORD);
    }
}
