package com.zuehlke.securesoftwaredevelopment.config;

import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.repository.UserRepository;
import com.zuehlke.securesoftwaredevelopment.service.LoginAttemptService;
import com.zuehlke.securesoftwaredevelopment.service.PermissionService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Username/password authentication against the users table.
 * <ul>
 *     <li>passwords are verified against BCrypt hashes,</li>
 *     <li>unknown usernames cost the same BCrypt check, so response time does not reveal which accounts exist,</li>
 *     <li>the error is identical for "unknown user" and "wrong password",</li>
 *     <li>repeated failures lock the account temporarily ({@link LoginAttemptService}).</li>
 * </ul>
 */
@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    private static final AuditLogger AUDIT_LOGGER = AuditLogger.getAuditLogger(DatabaseAuthenticationProvider.class);

    private static final String BAD_CREDENTIALS = "Invalid username or password";

    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final String dummyHash;

    public DatabaseAuthenticationProvider(UserRepository userRepository, PermissionService permissionService,
                                          PasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.permissionService = permissionService;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.dummyHash = passwordEncoder.encode("timing-equalization-dummy-password");
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();

        if (loginAttemptService.isLocked(username)) {
            AUDIT_LOGGER.audit("Login rejected, account temporarily locked: username=" + username);
            throw new LockedException("Account temporarily locked");
        }

        Optional<User> user = userRepository.findUser(username);
        boolean passwordMatches = passwordEncoder.matches(password, user.map(User::getPassword).orElse(dummyHash));

        if (!user.isPresent() || !passwordMatches) {
            loginAttemptService.loginFailed(username);
            AUDIT_LOGGER.audit("Login failed: username=" + username);
            throw new BadCredentialsException(BAD_CREDENTIALS);
        }

        loginAttemptService.loginSucceeded(username);
        AUDIT_LOGGER.audit("Login succeeded: username=" + username);

        // The principal kept in the session carries neither the password nor its hash
        User principal = new User(user.get().getId(), user.get().getUsername(), null);
        return new UsernamePasswordAuthenticationToken(principal, null, permissionService.getAuthorities(principal.getId()));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
