package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.Book;
import com.zuehlke.securesoftwaredevelopment.domain.NewBook;
import com.zuehlke.securesoftwaredevelopment.domain.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Book catalogue. Every statement uses bind parameters; user input is never concatenated into SQL.
 */
@Repository
public class BookRepository {

    private static final AuditLogger AUDIT_LOGGER = AuditLogger.getAuditLogger(BookRepository.class);

    private static final String BOOK_COLUMNS = "b.id, b.name, b.author, b.description, b.price";

    private static final RowMapper<Book> BOOK_MAPPER = (rs, rowNum) -> new Book(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("author"),
            rs.getString("description"),
            rs.getDouble("price"),
            new ArrayList<>());

    private final JdbcTemplate jdbc;

    public BookRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Book> getAll() {
        return jdbc.query("SELECT " + BOOK_COLUMNS + " FROM book b ORDER BY b.id", BOOK_MAPPER);
    }

    /**
     * Case-insensitive search by book title or tag name.
     */
    public List<Book> search(String searchTerm) {
        String pattern = SqlUtils.containsPattern(searchTerm);
        String sql = "SELECT DISTINCT " + BOOK_COLUMNS +
                " FROM book b" +
                " LEFT JOIN book_to_tag bt ON bt.bookId = b.id" +
                " LEFT JOIN tags t ON t.id = bt.tagId" +
                " WHERE UPPER(b.name) LIKE ? ESCAPE '\\'" +
                " OR UPPER(t.name) LIKE ? ESCAPE '\\'" +
                " ORDER BY b.id";
        return jdbc.query(sql, BOOK_MAPPER, pattern, pattern);
    }

    public Optional<Book> get(int bookId) {
        List<Book> books = jdbc.query("SELECT " + BOOK_COLUMNS + " FROM book b WHERE b.id = ?", BOOK_MAPPER, bookId);
        if (books.isEmpty()) {
            return Optional.empty();
        }
        Book book = books.get(0);
        book.setTags(jdbc.query(
                "SELECT t.id, t.name FROM tags t JOIN book_to_tag bt ON bt.tagId = t.id WHERE bt.bookId = ? ORDER BY t.id",
                (rs, rowNum) -> new Tag(rs.getInt("id"), rs.getString("name")),
                bookId));
        return Optional.of(book);
    }

    /**
     * Inserts the book and its tag links in one transaction.
     */
    @Transactional
    public long create(NewBook book, List<Tag> tags) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO book(name, author, description, price) VALUES (?, ?, ?, ?)", new String[]{"id"});
            ps.setString(1, book.getName());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getDescription());
            ps.setDouble(4, book.getPrice());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        for (Tag tag : tags) {
            jdbc.update("INSERT INTO book_to_tag(bookId, tagId) VALUES (?, ?)", id, tag.getId());
        }

        AUDIT_LOGGER.audit("Created book id=" + id + " name='" + book.getName() + "'");
        return id;
    }

    /**
     * Deletes a book; ratings, comments and tag links are removed by ON DELETE CASCADE.
     */
    @Transactional
    public void delete(int bookId) {
        jdbc.update("DELETE FROM book WHERE id = ?", bookId);
        AUDIT_LOGGER.audit("Deleted book id=" + bookId);
    }
}
