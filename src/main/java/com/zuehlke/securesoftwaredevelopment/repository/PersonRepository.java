package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.Entity;
import com.zuehlke.securesoftwaredevelopment.domain.Person;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PersonRepository {

    private static final AuditLogger AUDIT_LOGGER = AuditLogger.getAuditLogger(PersonRepository.class);

    private static final String PERSON_COLUMNS = "id, firstName, lastName, email";

    private static final RowMapper<Person> PERSON_MAPPER = (rs, rowNum) -> new Person(
            String.valueOf(rs.getInt("id")),
            rs.getString("firstName"),
            rs.getString("lastName"),
            rs.getString("email"));

    private final JdbcTemplate jdbc;

    public PersonRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Person> getAll() {
        return jdbc.query("SELECT " + PERSON_COLUMNS + " FROM persons ORDER BY id", PERSON_MAPPER);
    }

    /**
     * Case-insensitive search by first or last name.
     */
    public List<Person> search(String searchTerm) {
        String pattern = SqlUtils.containsPattern(searchTerm);
        String sql = "SELECT " + PERSON_COLUMNS + " FROM persons" +
                " WHERE UPPER(firstName) LIKE ? ESCAPE '\\' OR UPPER(lastName) LIKE ? ESCAPE '\\'" +
                " ORDER BY id";
        return jdbc.query(sql, PERSON_MAPPER, pattern, pattern);
    }

    public Optional<Person> get(int personId) {
        List<Person> persons = jdbc.query("SELECT " + PERSON_COLUMNS + " FROM persons WHERE id = ?", PERSON_MAPPER, personId);
        return persons.stream().findFirst();
    }

    /**
     * Updates the profile and writes a before/after audit record.
     */
    public void update(Person update) {
        int id = Integer.parseInt(update.getId());
        Person before = get(id).orElseThrow(() -> new IllegalArgumentException("Unknown person " + id));

        String firstName = update.getFirstName() != null ? update.getFirstName() : before.getFirstName();
        String lastName = update.getLastName() != null ? update.getLastName() : before.getLastName();
        String email = update.getEmail() != null ? update.getEmail() : before.getEmail();

        jdbc.update("UPDATE persons SET firstName = ?, lastName = ?, email = ? WHERE id = ?",
                firstName, lastName, email, id);

        AUDIT_LOGGER.auditChange(new Entity(
                "person.update",
                String.valueOf(id),
                describe(before.getFirstName(), before.getLastName(), before.getEmail()),
                describe(firstName, lastName, email)));
    }

    private static String describe(String firstName, String lastName, String email) {
        return "firstName=" + firstName + ";lastName=" + lastName + ";email=" + email;
    }
}
