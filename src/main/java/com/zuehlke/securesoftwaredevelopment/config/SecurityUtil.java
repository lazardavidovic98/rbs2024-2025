package com.zuehlke.securesoftwaredevelopment.config;

import com.zuehlke.securesoftwaredevelopment.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Object-level authorization rules used from {@code @PreAuthorize} expressions.
 */
@Component("securityUtil")
public class SecurityUtil {

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof User) ? (User) auth.getPrincipal() : null;
    }

    public boolean hasPermission(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission));
    }

    public boolean isOwner(int resourceUserId) {
        User user = getCurrentUser();
        return user != null && user.getId() == resourceUserId;
    }

    /**
     * Anyone may view their own profile; other profiles require VIEW_PERSON.
     */
    public boolean canViewPerson(int personUserId) {
        return isOwner(personUserId) || hasPermission("VIEW_PERSON");
    }

    /**
     * Users with UPDATE_PERSON may edit their own profile; editing others also requires VIEW_PERSONS_LIST.
     */
    public boolean canUpdatePerson(int personUserId) {
        return hasPermission("UPDATE_PERSON") && (isOwner(personUserId) || hasPermission("VIEW_PERSONS_LIST"));
    }

    /**
     * Same rule for ids coming from form fields; a non-numeric id is simply denied.
     */
    public boolean canUpdatePerson(String personUserId) {
        try {
            return canUpdatePerson(Integer.parseInt(personUserId));
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
