package com.hms.audit.aspect;

import com.hms.audit.domain.AuditAction;
import com.hms.audit.service.AuditLogService;
import com.hms.auth.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Cross-cutting audit trail: intercepts every REST controller method (except
 * {@link com.hms.auth.web.AuthController}, whose LOGIN/LOGOUT actions are logged
 * explicitly by AuthService) and records who did what to which record.
 *
 * Simplifications, documented rather than silently made:
 * - Only successful calls are logged (an exception skips the entry).
 * - "old value" is never populated - that would require a per-entity fetch-before-write
 *   hook that doesn't exist generically; only the new/request value is captured.
 * - "related patient" is a best-effort heuristic: the first path variable whose name
 *   looks like a patient identifier (upid/patientId), not a guaranteed-correct mapping.
 */
@Aspect
@Component
public class AuditLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingAspect.class);

    private final AuditLogService auditLogService;

    public AuditLoggingAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @AfterReturning(pointcut = "execution(public * com.hms..web..*Controller.*(..)) "
            + "&& !within(com.hms.auth.web.AuthController)", returning = "result")
    public void logAccess(JoinPoint joinPoint, Object result) {
        try {
            HttpServletRequest request = currentRequest();
            if (request == null) {
                return;
            }

            AuditAction action = deriveAction(request.getMethod());
            if (action == null) {
                return;
            }

            String moduleName = deriveModuleName(joinPoint.getTarget().getClass());
            Map<String, String> pathVariables = pathVariables(request);
            String recordId = lastValue(pathVariables);
            String relatedPatientId = findPatientId(pathVariables);
            if (relatedPatientId == null) {
                relatedPatientId = findPatientIdInResponseBody(result);
            }
            Object newValue = action == AuditAction.VIEW ? null : extractRequestBody(joinPoint);

            AuthenticatedUser user = currentUser();
            auditLogService.record(
                    user == null ? null : user.userId(),
                    user == null ? null : user.username(),
                    user == null ? null : user.role(),
                    action, moduleName, recordId, relatedPatientId, null, newValue);
        } catch (Exception e) {
            log.warn("Failed to record audit log entry for {}", joinPoint.getSignature(), e);
        }
    }

    private AuthenticatedUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    private AuditAction deriveAction(String httpMethod) {
        return switch (httpMethod) {
            case "GET" -> AuditAction.VIEW;
            case "POST" -> AuditAction.CREATE;
            case "PUT", "PATCH" -> AuditAction.UPDATE;
            case "DELETE" -> AuditAction.DELETE;
            default -> null;
        };
    }

    private String deriveModuleName(Class<?> controllerClass) {
        String[] segments = controllerClass.getPackageName().split("\\.");
        return segments.length > 2 ? segments[2] : controllerClass.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> pathVariables(HttpServletRequest request) {
        Object attr = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return attr instanceof Map ? (Map<String, String>) attr : Map.of();
    }

    private String lastValue(Map<String, String> pathVariables) {
        String last = null;
        for (String value : pathVariables.values()) {
            last = value;
        }
        return last;
    }

    private String findPatientId(Map<String, String> pathVariables) {
        return pathVariables.entrySet().stream()
                .filter(e -> {
                    String key = e.getKey().toLowerCase();
                    return key.contains("patient") || key.equals("upid");
                })
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Falls back to inspecting the response body for a "upid"/"patientId" accessor
     * when the path itself carried no such variable - needed for creation endpoints
     * (e.g. patient registration) where the identifier only exists in the response.
     */
    private String findPatientIdInResponseBody(Object result) {
        Object body = result instanceof org.springframework.http.ResponseEntity<?> re ? re.getBody() : result;
        if (body == null) {
            return null;
        }
        try {
            Method dataAccessor = body.getClass().getMethod("data");
            body = dataAccessor.invoke(body);
        } catch (ReflectiveOperationException ignored) {
            // not an ApiResponse-shaped wrapper - inspect body itself
        }
        if (body == null) {
            return null;
        }
        for (Method m : body.getClass().getMethods()) {
            String name = m.getName().toLowerCase();
            if (m.getParameterCount() == 0 && (name.equals("upid") || name.equals("patientupid"))) {
                try {
                    Object value = m.invoke(body);
                    if (value instanceof String s && !s.isBlank()) {
                        return s;
                    }
                } catch (ReflectiveOperationException ignored) {
                    // skip
                }
            }
        }
        return null;
    }

    private Object extractRequestBody(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameters.length; i++) {
            for (Annotation annotation : parameters[i].getAnnotations()) {
                if (annotation instanceof RequestBody) {
                    return args[i];
                }
            }
        }
        return null;
    }
}
