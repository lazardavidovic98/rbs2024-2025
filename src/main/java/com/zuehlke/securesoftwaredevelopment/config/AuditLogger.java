package com.zuehlke.securesoftwaredevelopment.config;

import com.zuehlke.securesoftwaredevelopment.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Writes audit records (who did what) marked with the AUDIT marker; logback-spring.xml routes them
 * to a separate logs/audit.log file.
 */
public class AuditLogger {

    public static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

    private final Logger log;

    private AuditLogger(Logger log) {
        this.log = log;
    }

    public static AuditLogger getAuditLogger(Class<?> clazz) {
        return new AuditLogger(LoggerFactory.getLogger(clazz));
    }

    public void audit(String description) {
        log.info(AUDIT, "userId={} - {}", currentUserId(), sanitize(description));
    }

    public void auditChange(Entity entity) {
        audit("Change " + entity);
    }

    /**
     * The user is resolved on every call. It used to be cached in a field of a logger shared by
     * all requests, so every audit record carried the id of the first user who triggered it.
     */
    private static String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return String.valueOf(((User) authentication.getPrincipal()).getId());
        }
        return "anonymous";
    }

    /**
     * Prevents log forging: a username such as "bob\n... userId=1 - Deleted user" cannot start a fake record.
     */
    private static String sanitize(String value) {
        return value == null ? "" : value.replaceAll("[\\r\\n\\t]", "_");
    }
}
