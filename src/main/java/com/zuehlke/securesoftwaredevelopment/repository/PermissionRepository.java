package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.domain.Permission;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PermissionRepository {

    private final JdbcTemplate jdbc;

    public PermissionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * All distinct permissions granted to a user through any of their roles.
     */
    public List<Permission> findByUserId(int userId) {
        String sql = "SELECT DISTINCT p.id, p.name FROM permissions p" +
                " JOIN role_to_permissions rp ON rp.permissionId = p.id" +
                " JOIN user_to_roles ur ON ur.roleId = rp.roleId" +
                " WHERE ur.userId = ? ORDER BY p.id";
        return jdbc.query(sql, (rs, rowNum) -> new Permission(rs.getInt("id"), rs.getString("name")), userId);
    }
}
