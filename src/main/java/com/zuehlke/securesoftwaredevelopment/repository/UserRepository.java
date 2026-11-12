package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private static final AuditLogger AUDIT_LOGGER = AuditLogger.getAuditLogger(UserRepository.class);

    private static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"));

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * @return the user with their BCrypt password hash, used only for authentication
     */
    public Optional<User> findUser(String username) {
        List<User> users = jdbc.query("SELECT id, username, password FROM users WHERE username = ?", USER_MAPPER, username);
        return users.stream().findFirst();
    }

    public Optional<String> findUsername(int id) {
        return jdbc.queryForList("SELECT username FROM users WHERE id = ?", String.class, id).stream().findFirst();
    }

    /**
     * @throws org.springframework.dao.DuplicateKeyException if the username is already taken
     */
    public void updateUsername(int id, String username) {
        jdbc.update("UPDATE users SET username = ? WHERE id = ?", username, id);
        AUDIT_LOGGER.audit("Changed username of user id=" + id);
    }

    /**
     * Deletes the account; profile, roles, ratings and comments go with it (ON DELETE CASCADE).
     */
    public void delete(int userId) {
        jdbc.update("DELETE FROM users WHERE id = ?", userId);
        AUDIT_LOGGER.audit("Deleted user id=" + userId);
    }
}
