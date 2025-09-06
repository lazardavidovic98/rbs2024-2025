package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.NewBook;
import com.zuehlke.securesoftwaredevelopment.domain.Tag;
import com.zuehlke.securesoftwaredevelopment.domain.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BookRepository {

    private static final Logger LOG = LoggerFactory.getLogger(BookRepository.class);
    private static final AuditLogger AUDIT_LOGGER = AuditLogger.getAuditLogger(BookRepository.class);

    private DataSource dataSource;

    public BookRepository(DataSource dataSource) {

        this.dataSource = dataSource;
    }

    public List<Book> getAll() {
        List<Book> bookList = new ArrayList<>();
        String query = "SELECT id, name, author, description, price FROM book";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Book book = createBookFromResultSet(rs);
                bookList.add(book);
            }
        } catch (SQLException e) {
            LOG.warn("Failed to get all books !", e);
        }
        return bookList;
    }

    public List<Book> search(String searchTerm) throws SQLException {
        List<Book> bookList = new ArrayList<>();
        String query = "SELECT DISTINCT g.id, g.name, g.author, g.description, g.price FROM book g, book_to_tag gt, tags t" +
                " WHERE g.id = gt.bookId" +
                " AND gt.tagId = t.id" +
                " AND (UPPER(g.name) like UPPER('%" + searchTerm + "%')" +
                " OR UPPER(t.name) like UPPER('%" + searchTerm + "%'))";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                bookList.add(createBookFromResultSet(rs));
            }
        }
        return bookList;
    }

    public Book get(int bookId, List<Tag> tagList) {
        String query = "SELECT id, name, author, description, price FROM book WHERE id = " + bookId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Book book = createBookFromResultSet(rs);
                List<Tag> bookTags = new ArrayList<>();
                String query2 = "SELECT bookId, tagId FROM book_to_tag WHERE bookId = " + bookId;
                ResultSet rs2 = statement.executeQuery(query2);
                while (rs2.next()) {
                    Tag tag = tagList.stream().filter(g -> {
                        try {
                            return g.getId() == rs2.getInt(2);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }).findFirst().get();
                    bookTags.add(tag);
                }
                book.setTags(bookTags);
                return book;
            }
        } catch (SQLException e) {
            LOG.warn("Failed to get book {}", bookId, e);
        }

        return null;
    }

    public long create(NewBook book, List<Tag> tagsToInsert) {
        String query = "INSERT INTO book(name, author, description, price) VALUES(?, ?, ?, ?)";
        long id = 0;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ) {
            statement.setString(1, book.getName());
            statement.setString(2, book.getAuthor());
            statement.setString(3, book.getDescription());
            statement.setDouble(4, book.getPrice());
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getLong(1);
                long finalId = id;
                tagsToInsert.stream().forEach(tag -> {
                    String query2 = "INSERT INTO book_to_tag(bookId, tagId) VALUES (?, ?)";
                    try (PreparedStatement statement2 = connection.prepareStatement(query2);
                    ) {
                        statement2.setInt(1, (int) finalId);
                        statement2.setInt(2, tag.getId());
                        statement2.executeUpdate();
                    } catch (SQLException e) {
                        LOG.warn("Error assigning tag {} to book {}: {}", tag.getId(), finalId, e.getMessage());
                    }
                });
                
                AUDIT_LOGGER.audit("Successfully created book '" + book.getName() + "' with ID " + finalId);
            }
        } catch (SQLException e) {
            LOG.warn("Failed to create a new book with title {}", book.getName(), e);
        }
        return id;
    }

    public void delete(int bookId) {
        String query = "DELETE FROM book WHERE id = " + bookId;
        String query2 = "DELETE FROM ratings WHERE bookId = " + bookId;
        String query3 = "DELETE FROM comments WHERE bookId = " + bookId;
        String query4 = "DELETE FROM book_to_tag WHERE bookId = " + bookId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(query);
            statement.executeUpdate(query2);
            statement.executeUpdate(query3);
            statement.executeUpdate(query4);
            
            AUDIT_LOGGER.audit("Successfully deleted book with ID " + bookId);
        } catch (SQLException e) {
            LOG.warn("Failed to delete a book {}", bookId, e);
        }
    }

    private Book createBookFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        String author = rs.getString(3);
        String description = rs.getString(4);
        double price = rs.getDouble(5);
        return new Book(id, name, author, description, price, new ArrayList<>());
    }
}
