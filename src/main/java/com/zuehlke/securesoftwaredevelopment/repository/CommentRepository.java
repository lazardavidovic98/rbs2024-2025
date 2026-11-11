package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.Comment;
import com.zuehlke.securesoftwaredevelopment.domain.ViewComment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommentRepository {

    private static final AuditLogger AUDIT_LOGGER = AuditLogger.getAuditLogger(CommentRepository.class);

    private final JdbcTemplate jdbc;

    public CommentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void create(Comment comment) {
        jdbc.update("INSERT INTO comments(bookId, userId, comment) VALUES (?, ?, ?)",
                comment.getBookId(), comment.getUserId(), comment.getComment());
        AUDIT_LOGGER.audit("Added comment to book id=" + comment.getBookId());
    }

    /**
     * Comments of a book together with the author's display name, oldest first.
     */
    public List<ViewComment> getAllForBook(int bookId) {
        String sql = "SELECT p.firstName, p.lastName, c.comment" +
                " FROM comments c LEFT JOIN persons p ON p.id = c.userId" +
                " WHERE c.bookId = ? ORDER BY c.id";
        return jdbc.query(sql, (rs, rowNum) -> {
            String firstName = rs.getString("firstName");
            String author = firstName == null ? "Deleted user" : firstName + " " + rs.getString("lastName");
            return new ViewComment(author, rs.getString("comment"));
        }, bookId);
    }
}
