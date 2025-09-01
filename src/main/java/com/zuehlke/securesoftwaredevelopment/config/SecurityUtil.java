package com.zuehlke.securesoftwaredevelopment.config;

import com.zuehlke.securesoftwaredevelopment.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityUtil")
public class SecurityUtil {

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof User) ? (User) auth.getPrincipal() : null;
    }

    public boolean hasPermission(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            return auth.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(permission));
        }
        return false;
    }

    public boolean isOwner(int resourceUserId) {
        User user = getCurrentUser();
        return user != null && user.getId() == resourceUserId;
    }

    public boolean canViewPerson(int personUserId) {
        return hasPermission("VIEW_PERSON");
    }

    public boolean canUpdatePerson(int personUserId) {
        return hasPermission("UPDATE_PERSON") && 
               (hasPermission("VIEW_PERSONS_LIST") || isOwner(personUserId));
    }
}
