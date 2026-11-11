package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.Entity;
import com.zuehlke.securesoftwaredevelopment.domain.Rating;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class RatingRepository {

    private static final AuditLogger AUDIT_LOGGER = AuditLogger.getAuditLogger(RatingRepository.class);

    private final JdbcTemplate jdbc;

    public RatingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Inserts the user's rating of a book or replaces the previous one.
     */
    @Transactional
    public void createOrUpdate(Rating rating) {
        List<Integer> previous = jdbc.queryForList(
                "SELECT rating FROM ratings WHERE bookId = ? AND userId = ?",
                Integer.class, rating.getBookId(), rating.getUserId());

        if (previous.isEmpty()) {
            jdbc.update("INSERT INTO ratings(bookId, userId, rating) VALUES (?, ?, ?)",
                    rating.getBookId(), rating.getUserId(), rating.getRating());
            AUDIT_LOGGER.audit("Rated book id=" + rating.getBookId() + " with " + rating.getRating());
            return;
        }

        jdbc.update("UPDATE ratings SET rating = ? WHERE bookId = ? AND userId = ?",
                rating.getRating(), rating.getBookId(), rating.getUserId());
        AUDIT_LOGGER.auditChange(new Entity(
                "rating.update",
                "bookId=" + rating.getBookId() + ";userId=" + rating.getUserId(),
                "rating=" + previous.get(0),
                "rating=" + rating.getRating()));
    }

    public List<Rating> getAll(int bookId) {
        return jdbc.query("SELECT bookId, userId, rating FROM ratings WHERE bookId = ?",
                (rs, rowNum) -> new Rating(rs.getInt("bookId"), rs.getInt("userId"), rs.getInt("rating")),
                bookId);
    }
}
