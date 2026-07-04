package com.hms.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.common.exception.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Stateless JWT security. RBAC is enforced via centralized URL-pattern rules
 * (method + path prefix) rather than per-endpoint {@code @PreAuthorize}
 * annotations sprinkled across ~90 controller methods - easier to audit as one
 * table and matches the module-grouped nature of the role spec. A stricter
 * per-field/per-action model would need method-level annotations instead.
 *
 * Known simplification: NURSE is restricted to exactly the modules named in the
 * role spec (vitals, MAR, progress notes, read-only patient demographics) and is
 * NOT granted read access to other clinical modules (complaints, diagnosis,
 * investigations, etc.) even though real-world nursing workflows often need it -
 * the given spec only lists those four capabilities for the Nurse role.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, 401, "Unauthorized", "Authentication required"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, 403, "Forbidden", "You do not have permission to access this resource")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        .requestMatchers("/api/v1/audit-trail/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/analytics/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers("/api/v1/nutrition/**").hasAnyRole("DIETITIAN", "DOCTOR", "ADMIN")
                        .requestMatchers("/api/v1/vitals/**").hasAnyRole("NURSE", "DOCTOR", "ADMIN")
                        .requestMatchers("/api/v1/ipd/mar/**").hasAnyRole("NURSE", "DOCTOR", "ADMIN")
                        .requestMatchers("/api/v1/ipd/progress-note/**").hasAnyRole("NURSE", "DOCTOR", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/prescriptions/**")
                        .hasAnyRole("DOCTOR", "PHARMACIST", "ADMIN")
                        .requestMatchers("/api/v1/prescriptions/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers("/api/v1/drugs/**").hasAnyRole("DOCTOR", "PHARMACIST", "NURSE", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/patients/**")
                        .hasAnyRole("DOCTOR", "NURSE", "ADMIN")
                        .requestMatchers("/api/v1/patients/**").hasAnyRole("DOCTOR", "ADMIN")

                        .anyRequest().hasAnyRole("DOCTOR", "ADMIN"))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeError(HttpServletResponse response, int status, String error, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiError.of(status, error, message, List.of())));
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
