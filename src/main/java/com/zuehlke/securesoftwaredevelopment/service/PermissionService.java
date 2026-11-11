package com.zuehlke.securesoftwaredevelopment.service;

import com.zuehlke.securesoftwaredevelopment.domain.Permission;
import com.zuehlke.securesoftwaredevelopment.repository.PermissionRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves the permissions (users -> roles -> permissions) that become Spring Security authorities.
 */
@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> get(int userId) {
        return permissionRepository.findByUserId(userId);
    }

    public List<GrantedAuthority> getAuthorities(int userId) {
        return get(userId).stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList());
    }
}
