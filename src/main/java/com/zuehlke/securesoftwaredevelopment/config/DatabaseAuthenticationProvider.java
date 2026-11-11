package com.zuehlke.securesoftwaredevelopment.config;

import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.repository.UserRepository;
import com.zuehlke.securesoftwaredevelopment.service.PermissionService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    private static final AuditLogger AUDIT_LOGGER = AuditLogger.getAuditLogger(DatabaseAuthenticationProvider.class);

    private static final String PASSWORD_WRONG_MESSAGE = "Authentication failed for username='%s',password='%s'";

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public DatabaseAuthenticationProvider(UserRepository userRepository, PermissionService permissionService) {
        this.userRepository = userRepository;
        this.permissionService = permissionService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        if (userRepository.validCredentials(username, password)) {
            User user = userRepository.findUser(username).orElseThrow(() -> new BadCredentialsException("Bad credentials"));
            AUDIT_LOGGER.audit("User with username: " + username + " - successfully logged in.");
            return new UsernamePasswordAuthenticationToken(user, password, permissionService.getAuthorities(user.getId()));
        }

        AUDIT_LOGGER.audit("Login failed for user with username: " + username);
        throw new BadCredentialsException(String.format(PASSWORD_WRONG_MESSAGE, username, password));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
