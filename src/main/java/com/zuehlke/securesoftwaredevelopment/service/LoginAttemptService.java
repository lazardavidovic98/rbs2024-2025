package com.zuehlke.securesoftwaredevelopment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Throttles online password guessing: after {@code maxAttempts} consecutive failures
 * an account is locked for {@code lockDuration}. A successful login resets the counter.
 */
@Service
public class LoginAttemptService {

    private final int maxAttempts;
    private final Duration lockDuration;
    private final Clock clock;
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(@Value("${security.login.max-attempts:5}") int maxAttempts,
                               @Value("${security.login.lock-duration:PT15M}") Duration lockDuration) {
        this(maxAttempts, lockDuration, Clock.systemUTC());
    }

    LoginAttemptService(int maxAttempts, Duration lockDuration, Clock clock) {
        this.maxAttempts = maxAttempts;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    public boolean isLocked(String username) {
        Attempts entry = attempts.get(key(username));
        return entry != null && entry.lockedUntil != null && clock.instant().isBefore(entry.lockedUntil);
    }

    public void loginFailed(String username) {
        attempts.compute(key(username), (k, entry) -> {
            Attempts next = entry == null || entry.isExpired(clock.instant()) ? new Attempts() : entry;
            next.failures++;
            if (next.failures >= maxAttempts) {
                next.lockedUntil = clock.instant().plus(lockDuration);
            }
            return next;
        });
    }

    public void loginSucceeded(String username) {
        attempts.remove(key(username));
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private static final class Attempts {
        private int failures;
        private Instant lockedUntil;

        private boolean isExpired(Instant now) {
            return lockedUntil != null && !now.isBefore(lockedUntil);
        }
    }
}
